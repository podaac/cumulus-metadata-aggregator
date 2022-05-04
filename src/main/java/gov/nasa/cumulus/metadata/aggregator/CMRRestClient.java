//Copyright 2008, by the California Institute of Technology.
//ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
package gov.nasa.cumulus.metadata.aggregator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.net.ssl.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Objects;

public class CMRRestClient {

    public enum HttpOp {
        SEND,
        DELETE,
        GET,
        PUT,
        POST;
    }

    private static Log log = LogFactory.getLog(CMRRestClient.class);

    String certFile = null;
    String password = null;
    String echoHost = null;
    String tokenHost = null;
    String searchHost = null;
    Path tokenFile = null;

    String provider = null;
    DefaultHttpClient httpClient = null;
    JSONParser parser = null;
    String token = null;
    String validHost = null;
    static boolean CMR = true;

    public CMRRestClient(String cert, String pass, String cmrHost,
                         String provider, String tknHost, String tknFile) {
        this.certFile = cert;
        this.password = pass;
        this.provider = provider;

        try {
            this.httpClient = httpClientTrustingAllSSLCerts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.parser = new JSONParser();
        this.tokenFile = Paths.get(tknFile);
        this.echoHost = cmrHost;
        this.searchHost = cmrHost.replaceAll("ingest/", "search/");
        this.tokenHost = tknHost;
        this.validHost = tknHost.replaceAll("gettoken", "validate");
    }

    public CMRRestClient(boolean isLambda) {
        if (isLambda)
            log.info("CMRLambdaRestClient initialized hence calling this form of constructor");
    }

    public CMRRestClient() {
        try {
            this.httpClient = httpClientTrustingAllSSLCerts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs an error/unexpected response from an
     * http operation.
     *
     * @param code   the response code from the server
     * @param body   the response message from the server
     * @param source the http operation that we were trying
     *               to perform, that resulted in the error.
     *               <br><br>
     *               e.g. SEND, DELETE, POST, etc.
     * @throws IOException
     */
    private void logHttpError(int code, String body, HttpOp source) throws IOException {
        log.error("Unexpected response code[" + code + "]: " + body);
        switch (source) {
            case SEND:
                throw new IOException("Error submitting entry to CMR.");
            case DELETE:
                throw new IOException("Error Deleting entry from CMR.");
            default:
                throw new IOException("Unexpected error.");
        }
    }

    /**
     * Logs an HttpResponse
     *
     * @param response the httpresponse to process
     * @param source   flag to denote if this was a send
     *                 request, or delete request; used for
     *                 logging appropriate error message
     * @throws IOException if we received anything other
     *                     than a 200 or 201 response
     */
    private void logHttpResponse(HttpResponse response, HttpOp source) throws IOException {
        int code = response.getStatusLine().getStatusCode();
        String body = null;
        switch (code) {
            case 200:
                // fall through since we log 200 the same as 201 response
            case 201:
                body = readInputStream(response.getEntity().getContent());
                log.info("response code[" + code + "]: " + body);
                break;
            case 204:
                log.info("response code[" + code + "]");
                break;
            default:
                body = readInputStream(response.getEntity().getContent());
                // handoff to our error logging method
                logHttpError(code, body, source);
        }
    }

    /**
     * Parses the url string for specific keywords, to determine
     * if we should be performing a 'POST' operation, instead of the
     * typical/usual 'PUT' operation.
     * <br><br>
     * Common examples: when sending data to the CMR validate endpoint,
     *                  and sending bulk updates for granules
     *
     * @param input  the url, as a string, to search for
     *               flagged keywords.
     * @return       true if any keyword/pattern is matched,
     *               else false.
     */
    protected boolean isPostOperation(String input) {
        String url = input.toLowerCase();
        return url.contains("validate") ||
                url.contains("bulk-update") ||
                url.contains("translate") ||
                url.endsWith("clear-scroll") ||
                url.endsWith("subscriptions");
    }

    /**
     *
     * @param url
     * @param entity
     * @throws IOException
     */
    private void send(String url, HttpEntity entity) throws IOException {
        HttpEntityEnclosingRequestBase request;
        boolean isPost = isPostOperation(url);
        if (isPost) {
            request = new HttpPost(url);
        } else {
            request = new HttpPut(url);
        }
        request.setEntity(entity);
        // get the echo auth token, will throw an exception if none
        String tkn = getToken();
        //Set the Token/Authorization Header
        request.setHeader("Authorization", tkn);
        // Send the request
        HttpResponse response = httpClient.execute(request);
        // log the response
        if (isPost) {
            logHttpResponse(response, HttpOp.POST);
        } else {
            logHttpResponse(response, HttpOp.SEND);
        }
        // close the connection
        request.releaseConnection();
    }

    public void closeScrollSession(String scrollJson) throws IOException {
        log.debug("Sending close-scroll request: " + scrollJson);
        StringBuilder builder = new StringBuilder();
        builder.append(searchHost);
        builder.append("clear-scroll");
        log.debug("Clear-scroll url: " + builder);
        // now convert and send the actual json message
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(scrollJson);
            requestEntity.setContentType("application/json");
            send(builder.toString(), requestEntity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }

    protected HttpPost buildSearchRequest(String url, String scrollId) throws IOException {
        HttpPost request = new HttpPost(url);
        // get the echo auth token, will throw an exception if none
        String tkn = getToken();
        //Set the Token/Authorization Header
        request.setHeader("Authorization", tkn);
        request.setHeader("Accept", "application/vnd.nasa.cmr.umm+json");
        if (UMMUtils.notNullOrEmpty(scrollId)) {
            request.setHeader("CMR-Scroll-Id", scrollId);
        }
        return request;
    }

    private String performSearch(HttpPost request) throws IOException {
        HttpResponse response = httpClient.execute(request);
        try {
            String cmrScrollId = null;
            for (Header h : response.getAllHeaders()) {
                if (h.getName().contains("CMR-Scroll-Id")) {
                    cmrScrollId = h.getValue();
                    log.debug("Found CMR-Scroll-Id in response: " + cmrScrollId);
                }
            }
            // if we are in the middle of an existing scroll session, we need
            // to make sure we pass the cmr-scroll-id back to the caller
            if (UMMUtils.notNullOrEmpty(cmrScrollId)) {
                // we use stringbuilder since it's faster than regex
                StringBuilder builder = new StringBuilder();
                // manually insert the leading open bracket; '{'
                builder.append("{");
                // now insert the cmr-scroll-id key and value
                builder.append("\"cmr-scroll-id\":");
                builder.append(cmrScrollId);
                // and don't forget the comma so the final json still parses
                builder.append(",");
                //now append the original response, but strip the leading bracket: '{'
                builder.append(new BasicResponseHandler().handleResponse(response).substring(1));
                return builder.toString();
            } else {
                String respString = new BasicResponseHandler().handleResponse(response);
                return respString;
            }
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String search(String url, String scrollId) throws IOException {
        HttpPost request = buildSearchRequest(url, scrollId);
        // Send the request
        String response = performSearch(request);
        return response;
    }

    protected static String escapeName(String name) {
        Objects.requireNonNull(name, "dataset name must not be null");
        return name.replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }

    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String search(String url) throws IOException {
        return search(url, null);
    }

    protected HttpDelete buildDeleteObject(String url) {
        return new HttpDelete(url);
    }

    private void delete(String url) throws IOException {
        HttpDelete httpDelete = buildDeleteObject(url);
        // get the echo auth token, will throw an exception if none
        String tkn = getToken();
        //Set the Token/Authorization Header
        httpDelete.setHeader("Authorization", tkn);
        // Send the request
        HttpResponse response = httpClient.execute(httpDelete);
        // log the response
        logHttpResponse(response, HttpOp.DELETE);
        // close the connection
        httpDelete.releaseConnection();
    }

    private void sendGranule(String granuleUr, File file) throws Exception {

        String url = echoHost + "/providers/"
                + provider + "/granules/" + granuleUr;

        // Set the body of the request to the granule1.xml file
        //FileEntity requestEntity = new FileEntity(new File( granuleFile  ));
        FileEntity requestEntity = new FileEntity(file);

        // Set the ContentType header
        requestEntity.setContentType("application/echo10+xml");
        send(url, requestEntity);
    }

    /**
     * Wrapper function to delete a dataset from CMR
     * based on the given dataset 'long_name'
     * <br><br>
     *
     * @param longName the dataset 'long_name' for the collection
     *                 to delete, with spaces and parenthesis escaped
     */
    public void deleteDataset(String longName) {
        String url = echoHost + "providers/" + provider + "/collections/" + longName;
        log.debug("deleting from: " + url);
        try {
            this.delete(url);
        } catch (IOException e) {
            log.info("Error while attempting to delete dataset");
            e.printStackTrace();
        }

    }

    public void sendGranule(UMMGranuleFile granule) throws IOException {
        String url = echoHost + "providers/"
                + provider + "/granules/" + URLEncoder.encode(granule.getNativeId());
        log.debug(url);
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(granule.toJSONString());
            requestEntity.setContentType("application/vnd.nasa.cmr.umm+json;version=" + granule.getUmmVersion());
            send(url, requestEntity);
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendDataset(JSONObject json, String nativeId) throws IOException {
        String url = echoHost + "providers/"
                + provider + "/collections/" + URLEncoder.encode(nativeId);
        sendDataset(json.toJSONString(), url);
    }

    public void sendDataset(String jsonString, String url) throws IOException {
        log.debug(url);
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(jsonString);
            requestEntity.setContentType("application/vnd.nasa.cmr.umm+json");
            send(url, requestEntity);
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void validateDataset(JSONObject json, String nativeId) throws IOException {
        String url = echoHost + "providers/"
                + provider + "/validate/collection/" + URLEncoder.encode(nativeId);
        validateDataset(json.toJSONString(), url);
    }

    public void validateDataset(String jsonString, String url) throws IOException {
        log.debug(url);
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(jsonString);
            requestEntity.setContentType("application/vnd.nasa.cmr.umm+json");
            send(url, requestEntity);
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void validateGranule(UMMGranuleFile granule) throws IOException {
        String url = echoHost + "providers/"
                + provider + "/validate/granule/" + URLEncoder.encode(granule.getNativeId());
        log.debug(url);
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(granule.toJSONString());
            requestEntity.setContentType("application/vnd.nasa.cmr.umm+json;version=" + granule.getUmmVersion());
            send(url, requestEntity);
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param bulkJson
     * @param provider
     * @return
     * @throws IOException
     */
    public String sendBulkUpdate(String bulkJson, String provider) throws IOException {
        log.debug("Sending granule bulk update request.");
        StringBuilder builder = new StringBuilder();
        builder.append(echoHost);
        builder.append("providers/");
        if (UMMUtils.notNullOrEmpty(provider)) {
            // if we were passed a custom provider, use it
            builder.append(provider);
        } else {
            // if not, use the provider specified in distribute config
            builder.append(this.provider);
        }
        builder.append("/bulk-update/granules");
        log.debug("Final bulk update request URL: " + builder);
        // now convert and send the actual json message
        StringEntity requestEntity;
        try {
            requestEntity = new StringEntity(bulkJson);
            requestEntity.setContentType("application/json");
            send(builder.toString(), requestEntity);
        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param concept
     * @param provider
     * @param granules
     * @return
     */
    private String getByConcept(String concept, String provider,
                                boolean granules, String scrollSessionId) {
        StringBuilder builder = new StringBuilder();
        builder.append(searchHost);
        if (granules) {
            builder.append("granules?collection_concept_id=");
        } else {
            builder.append("collections?concept_id=");
        }
        builder.append(concept);
        // now handle the provider parameter
        builder.append("&provider=");
        if (UMMUtils.notNullOrEmpty(provider)) {
            builder.append(provider);
        } else {
            builder.append(this.provider);
        }
        if (granules) {
            if (UMMUtils.isNullOrEmpty(scrollSessionId)) {
                builder.append("&scroll=defer");
                builder.append("&page_size=2000");
            } else {
                builder.append("&scroll");
            }
        }
        log.debug("final getByConcept url: " + builder);
        try {
            String respString;
            if (UMMUtils.notNullOrEmpty(scrollSessionId)) {
                respString = search(builder.toString(), scrollSessionId);
            } else {
                respString = search(builder.toString());
            }
            return respString;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param concept
     * @param provider
     * @return
     */
    public String getGranulesByConcept(String concept, String provider, String sessionID) {
        log.debug("Searching CMR for granules from dataset: " + concept);
        return getByConcept(concept, provider, true, sessionID);
    }

    /**
     *
     * @param concept
     * @param provider
     * @return
     */
    public String getCollectionByConcept(String concept, String provider) {
        log.debug("Searching CMR for collection: " + concept);
        return getByConcept(concept, provider, false, null);
    }

    /**
     *
     * @param shortName
     * @param provider
     * @return
     */
    public String searchByShortName(String shortName, String provider) {
        log.debug("Searching CMR for existing dataset: " + shortName);
        StringBuilder builder = new StringBuilder();
        builder.append(searchHost);
        builder.append("collections.umm_json?short_name=");
        builder.append(escapeName(shortName));
        if (UMMUtils.notNullOrEmpty(provider)) {
            builder.append("&provider=");
            builder.append(provider);
        }
        log.debug("Url search string: " + builder);
        try {
            String respString = search(builder.toString());
            return respString;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected DefaultHttpClient httpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        // DMAS 5.4: PODAAC-1572, NAMS auth change
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(new File(certFile)),
                password.toCharArray());
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(clientStore, password.toCharArray());
        KeyManager[] keymanagers = kmfactory.getKeyManagers();

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(keymanagers, getTrustingManager(), new java.security.SecureRandom());

        SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
        Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        return httpclient;
    }

    private TrustManager[] getTrustingManager() {
        return new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

        }};
    }

    /**
     * Wrapper function to get an existing token, or hand-off
     * to login method if no token is currently set.
     *
     * @return the token.
     * @throws Exception
     */
    private String getToken() throws IOException {
        // get the current time now to ensure we never exceed session time
        long runTime = Instant.now().toEpochMilli();
        JSONObject jsonTkn = readTokenFile();
        long fileTime = getTokenStartTime(jsonTkn);
        if (jsonTkn == null || sessionExpired(fileTime, runTime)) {
            log.debug("Generating new NAMS token...");
            try {
                this.token = buildToken(runTime);
            } catch (Exception e) {
                //ERROR getting new token from NAMS.
                throw new IOException("Could not retrieve token...");
            }
        } else {
            log.debug("Session active, using saved token");
            this.token = (String) jsonTkn.get("token");
        }
        return this.token;
    }

    /**
     * Check if we've exceeded the one hour session time limit
     * err on the side of caution, and use 3500 seconds
     * instead of 3600 seconds, to account for latency/lag
     *
     * @param fileTime the session start time from the token file
     * @param runTime  the time we called the getToken method
     * @return boolean, true if session is expired
     */
    protected boolean sessionExpired(long fileTime, long runTime) {
        if (fileTime == Long.MAX_VALUE) {
            return true;
        } else {
            return runTime > (fileTime + 3500000);
        }
    }

    protected boolean validateToken(String token) throws IOException {
        JSONObject json = new JSONObject();
        json.put("token", token);
        log.debug("Validating new NAMS token...");
        int code;
        HttpPost httpPost = new HttpPost(validHost);
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json.toJSONString()));
            HttpResponse response = httpClient.execute(httpPost);
            code = response.getStatusLine().getStatusCode();
        } finally {
            httpPost.releaseConnection();
        }
        return code == 200;
    }

    protected long getTokenStartTime(JSONObject token) {
        if (token == null) {
            return Long.MAX_VALUE;
        } else {
            return (long) token.get("authTime");
        }
    }

    private boolean writeTokenFile(long timeStamp, String token) {
        JSONObject json = new JSONObject();
        json.put("authTime", timeStamp);
        json.put("token", token);
        try {
            log.debug("Writing new token to file");
            Files.write(tokenFile, json.toString().getBytes());
            return true;
        } catch (IOException e) {
            log.error("Failed to write token to file: " + tokenFile);
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject readTokenFile() {
        JSONObject json = null;
        try (FileReader reader = new FileReader(String.valueOf(tokenFile))) {
            json = (JSONObject) parser.parse(reader);
        } catch (FileNotFoundException e) {
            log.info("No existing echo.token file found");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            log.error("Error parsing echo.token JSON");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Logs in and returns a token.
     *
     * @return the token, as string
     * @throws IOException
     */
    private String buildToken(long newAuthTime) throws IOException {
        String tkn = null;
        // DMAS 5.4: PODAAC-1572, NAMS auth change
        HttpGet httpGet = new HttpGet(this.tokenHost);
        try {
            log.debug("start cmr token request");
            HttpResponse response = httpClient.execute(httpGet);
            log.info("httpresponse raw:" + response);
            String namsResponse = readInputStream(response.getEntity().getContent());
            // Parse out the token value from the JSON response
            tkn = this.parseNAMSResponse(namsResponse);
            // validate and save the token in our token file
            if (validateToken(tkn)) {
                writeTokenFile(newAuthTime, tkn);
            }
        } catch (ClientProtocolException e) {
            log.error("Protocol error while attempting to post login");
            e.printStackTrace();
        } catch (ParseException e) {
            log.error("Failed to find sm_token in NAMS response");
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return tkn;
    }

    /**
     * parses the JSON response from NAMS gettoken api
     *
     * @param response the NAMS http get response, as string
     * @return the sm_token from the response
     * @throws ParseException
     */
    protected String parseNAMSResponse(String response) throws ParseException {
        JSONObject json = (JSONObject) parser.parse(response);
        return json.get("sm_token").toString();
    }

    /**
     * Reads an input stream and returns the contents as a string.
     *
     * @param is the input stream to read from
     * @return the contents of input stream, as string
     * @throws IOException
     */
    protected static String readInputStream(InputStream is) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(is))) {
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (builder.length() != 0) {
                    builder.append("\n");
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }

}
