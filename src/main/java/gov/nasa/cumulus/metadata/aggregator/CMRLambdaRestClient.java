/**
 * This class re-write (not overwrite) the token access functionality in its parent class: CMRRestClient
 * by store the token file on the S3 bucket
 *  Read ipx cert file from S3 bucket
 *  get passphrase from secret manager
 *  store token.json on S3 bucket
 * *
 */
package gov.nasa.cumulus.metadata.aggregator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nasa.cumulus.metadata.util.S3Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;

import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;

import cumulus_message_adapter.message_parser.AdapterLogger;

public class CMRLambdaRestClient extends CMRRestClient {
    // CMR request header including content type with schema version
    private final static String content_type="application/vnd.nasa.cmr.umm+json;version="+Constants.UMMG_VERSION;
    String className = this.getClass().getName();
    String region="";
    String tknBucket="";
    String tknFilePath="";
    // Working directory where we save and read token json file
    String workingDir="";
    public CMRLambdaRestClient(String cert, String pass, String tknHost,
                               String cmrHost, String provider, String region, String tknBucket, String tknFilePath,
                               String workingDir) throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, URISyntaxException {
        super(true);
        this.certFile = cert;
        this.password = pass;
        this.provider = provider;
        try {
            this.httpClient = httpClientTrustingAllSSLCerts();
        } catch (NoSuchAlgorithmException| KeyManagementException| KeyStoreException|
                IOException| CertificateException| UnrecoverableKeyException e) {
            AdapterLogger.LogError("Http SSL based client creation error: " + e);
            throw e;
        }
        this.parser = new JSONParser();
        this.echoHost = cmrHost;
        URIBuilder uriBuilder = new URIBuilder(tknHost);
        this.tokenHost = uriBuilder.setPath(uriBuilder.getPath() + "/gettoken").build().normalize().toString();
        AdapterLogger.LogInfo(this.className + " final token url:" + this.tokenHost);
        this.validHost = this.tokenHost.replaceAll("gettoken", "validate");
        this.region = region;
        this.tknBucket = tknBucket;
        this.tknFilePath = tknFilePath;
        this.workingDir = workingDir;
    }

    public CMRLambdaRestClient() {
        super(true);
    }

    public void cleanTempDir() throws IOException {
        try {
            FileUtils.deleteDirectory(new File(this.workingDir));
            AdapterLogger.LogError(this.className + " working directory deleted:" + this.workingDir);
        } catch (IOException ioe) {
            AdapterLogger.LogError(this.className + " working directory deletion exception:" + ioe);
            throw ioe;
        }
    }

    public String getToken()
    throws IOException, ParseException {
        // get the current time now to ensure we never exceed session time
        long runTime = Instant.now().toEpochMilli();
        JSONObject jsonTkn = readToken();
        long fileTime = getTokenStartTime(jsonTkn);
        if (jsonTkn == null || sessionExpired(fileTime, runTime)) {
            AdapterLogger.LogDebug(this.className + " Generating new NAMS token...");
            try {
                this.token = buildToken(runTime);
            } catch (Exception e) {
                //ERROR getting new token from NAMS.
                throw new IOException("Could not retrieve token..." + e);
            }
        } else {
            AdapterLogger.LogDebug(this.className + " Session active, using saved token");
            this.token = (String) jsonTkn.get("token");
        }
        return this.token;
    }
    private String buildToken(long newAuthTime) throws IOException, ParseException {
        String tkn = null;
        HttpGet httpGet = new HttpGet(this.tokenHost);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            AdapterLogger.LogInfo(this.className + " buildToken response code:"+
                            response.getStatusLine().getStatusCode());
            String namsResponse = readInputStream(response.getEntity().getContent());
            if(StringUtils.contains(namsResponse, "sm_token")) { //DO NOT log detail token info
                AdapterLogger.LogInfo(this.className + " nameResponse containing sm_token");
            } else {
                AdapterLogger.LogInfo(this.className + " nameResponse:" + namsResponse);
            }
            // Parse out the token value from the JSON response
            tkn = this.parseNAMSResponse(namsResponse);
            // validate and save the token in our token file
            if (validateToken(tkn)) {
                AdapterLogger.LogDebug("token validated");
                writeToken(newAuthTime, tkn);
            }
        } catch ( IOException | ParseException  e) {
            AdapterLogger.LogError(this.className + " Error in build token:" +e);
            throw e;
        }  finally {
            httpGet.releaseConnection();
        }
        return tkn;
    }

    public BigInteger getGranuleRevisionId(String provider, String collection_short_name, String granule_id)
    throws URISyntaxException, IOException, ParseException {
        BigInteger revisionId = BigInteger.ZERO;
        try {
            URIBuilder uriBuilder = new URIBuilder(this.echoHost);
            String queryGranuleURI = uriBuilder.setPath(uriBuilder.getPath() + "/search/granules.umm_json")
                    .setParameter("short_name", collection_short_name)
                    .setParameter("granule_ur",granule_id)
                    .setParameter("provider", provider)
                    .build().normalize().toString();
            HttpResponse httpResponse = search(queryGranuleURI);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            InputStream inputStream = httpResponse.getEntity().getContent();
            AdapterLogger.LogInfo(this.className + " getCMRRevisionId response code : "+ statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
                revisionId = decodeRevisionString(text);
            }
        } catch (URISyntaxException | IOException | ParseException e) {
            AdapterLogger.LogError(this.className + " exception getting granule revison-id: " + e);
            throw  e;
        }
        return revisionId;
    }

    /**
     * Must query through the /search/concepts/conceptId.native to get the trustworthy
     * UMMG json.
     * The https://cmr.uat.earthdata.nasa.gov/search/granules.umm_json?concept_id=G1238611022-POCUMULUS
     * might be going through some format translation and not as native/trustworthy as
     * /search/concepts/conceptId.native
     * @param conceptId
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    public String getGranuleByNativeConceptId(String conceptId)
            throws URISyntaxException, IOException, ParseException {

        String respBodyStr="";
        try {
            URIBuilder uriBuilder = new URIBuilder(this.echoHost);
            String queryGranuleURI = uriBuilder.setPath(uriBuilder.getPath() + "/search/concepts/"
            + conceptId+".native")
                    .build().normalize().toString();
            HttpResponse httpResponse = search(queryGranuleURI);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            InputStream inputStream = httpResponse.getEntity().getContent();
            AdapterLogger.LogInfo(this.className + " getCMRRevisionId response code : "+ statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                respBodyStr = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
                AdapterLogger.LogDebug(this.className + " getGranuleByConceptId response body: " + respBodyStr);
            } else {
                throw new IOException("UMMG Query Error Response code: " + statusCode);
            }
        } catch (URISyntaxException | IOException | ParseException e) {
            AdapterLogger.LogError(this.className + " exception getting ummg by conceptId.NATIVE: " + e);
            throw  e;
        }
        return respBodyStr;
    }

    /**
     * Decode the response granule json (query by granuleId and collection shortname) to find the
     * granule's revision_id
     * Note: BigInteger can hold much larger value than Long
     * @param responseJson
     * @return revison-id in BigInteger
     */
    public BigInteger decodeRevisionString(String responseJson) {
        JsonObject inputKey =JsonParser.parseString(responseJson).getAsJsonObject();
        JsonArray granules = inputKey.getAsJsonArray("items");
        /**
         * If not items array has size not equal to 1 then either this granule has not
         * been existing in CMR yet or we got a serious problem.  query UMMG and post UMMG
         * is still under design for post-ingest workflows.  Hence, return -1 for now.
         */
        if(granules.size() != 1) {
            AdapterLogger.LogError(this.className + " Not finding exactly one granule based on granleId");
            return BigInteger.valueOf(-1);
        }
        JsonObject granule = granules.get(0).getAsJsonObject();
        JsonObject meta = granule.getAsJsonObject("meta");
        return meta.get("revision-id").getAsBigInteger();
    }

    private HttpResponse search(String url) throws IOException, ParseException {
        HttpGet request = new HttpGet(url);
        // get the echo auth token, will throw an exception if none
        String tkn = getToken();
        //Set the Authorization/token Header
        request.setHeader("Authorization", tkn);
        request.setHeader("Accept", "application/vnd.nasa.cmr.umm+json");
        // Send the request
        HttpResponse response = httpClient.execute(request);
        return response;
    }


    /**
     * This is a function to handle read token file if MetadataAggregator is running as lambda
     * It will download the token file from S3 to local /tmp/workingDir/token.json
     */
    private JSONObject readToken()
    throws IOException, ParseException{
        S3Utils s3Utils = new S3Utils();
        // if token file does not exist on S3, return a null object
        if(!s3Utils.isObjectExist(this.region, this.tknBucket, this.tknFilePath)) {
            AdapterLogger.LogDebug(this.className + " tknFile not found on cloud, returning null");
            return null;
        }
        String localTokenFilePath = s3Utils.download(this.region, this.tknBucket, this.tknFilePath,
                Paths.get(this.workingDir, "token.json").toString());
        JSONObject json = null;
        try (FileReader reader = new FileReader(String.valueOf(localTokenFilePath))) {
            json = (JSONObject) parser.parse(reader);
        } catch ( IOException | ParseException e) {
            AdapterLogger.LogError(this.className +  "Read token Exception:" + e);
            throw e;
        }
        return json;
    }

    private void writeToken(long timeStamp, String token)
    throws IOException{
        S3Utils s3Utils = new S3Utils();
        JSONObject json = new JSONObject();
        json.put("authTime", timeStamp);
        json.put("token", token);
        AdapterLogger.LogDebug(this.className + " Writing new token to file");
        Path localTknFilePath = Files.write(Paths.get(this.workingDir, "token.json"),
                json.toString().getBytes());
        s3Utils.upload(region, this.tknBucket, this.tknFilePath, new File(localTknFilePath.toString()) );
    }

    private HttpResponse send(String url, HttpEntity entity)
            throws ParseException, IOException {
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
        //Set the Authorization/token Header
        request.setHeader("Authorization", tkn);
        // please be aware the content-type includes ummg schema version information
        request.setHeader("Content-Type", content_type);
        // Send the request
        HttpResponse response = httpClient.execute(request);
        return response;
    }

    public HttpResponse validateUMMG(String provider, String granuleId,  String strUMMG)
            throws URISyntaxException, IOException, ParseException {
        try {

            URIBuilder uriBuilder = new URIBuilder(this.echoHost);
            String validateUMMGUri = uriBuilder.setPath(uriBuilder.getPath() + "/ingest/providers/"
                    + provider +"/validate/granule/" + granuleId)
                    .build().normalize().toString();
            AdapterLogger.LogDebug("validateUri:" + validateUMMGUri);
            HttpEntity httpEntity = new StringEntity(strUMMG, "utf-8");
            HttpResponse httpResponse = send(validateUMMGUri, httpEntity);
            return httpResponse;
        } catch (URISyntaxException | IOException | ParseException e) {
            throw  e;
        }
    }

    /**
     * check if UMMG has a spatial error, specifically.
     * if return true means UMMG is valid
     * return false means UMMG has Spatial error, specifically
     * otherwise, throws exception
     * @param provider
     * @param granuleId
     * @param strUMMG
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean isUMMGSpatialValid(String provider, String granuleId, String strUMMG)
            throws URISyntaxException, IOException, ParseException {
        AdapterLogger.LogInfo(this.className + " UMMG validation provider: "+ provider + " granuleId: " + granuleId +
        " ummg: " + strUMMG);
        HttpResponse httpResponse = validateUMMG(provider, granuleId, strUMMG);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        InputStream inputStream = httpResponse.getEntity().getContent();
        AdapterLogger.LogInfo(this.className + " UMMG validation response code : "+ statusCode);
        String respBodyStr = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        AdapterLogger.LogDebug(this.className + " UMMG validation response body: " + respBodyStr);
        if (statusCode == HttpStatus.SC_OK) {
            AdapterLogger.LogDebug(this.className + " UMMG validation succeeded");
            return true;
        } else if(isSpatialErrorString(respBodyStr)){
            AdapterLogger.LogDebug(this.className + " UMMG validation spatial error");
            return false;
        } else {
            AdapterLogger.LogDebug(this.className + " UMMG validation Non-spatial error");
            throw new IOException("Some other error other than Geometry Spatial validation error");
        }
    }

    /**
     * sample error response body when CMR detect spatial error
     * {
     *     "errors": [
     *         {
     *             "path": [
     *                 "SpatialCoverage",
     *                 "Geometries",
     *                 0
     *             ],
     *             "errors": [
     *                 "Spatial validation error: The polygon boundary contains both the North and South Poles. A polygon can contain at most one pole. Please check the order of your points.\n You may have provided them in the wrong order (clockwise vs counter-clockwise)"
     *             ]
     *         }
     *     ]
     * }
     * @param errorStr
     * @return
     */
    public boolean isSpatialErrorString(String errorStr) {
        // instead of marshalling errorString to JSONObject, use a simple string match method
        if(StringUtils.containsIgnoreCase(errorStr, "errors")
                && StringUtils.containsIgnoreCase(errorStr, "SpatialCoverage")
                && StringUtils.containsIgnoreCase(errorStr, "Geometries")
                && StringUtils.containsIgnoreCase(errorStr, "Spatial validation error")) {
            return true;
        } else {
            return false;
        }
    }
}
