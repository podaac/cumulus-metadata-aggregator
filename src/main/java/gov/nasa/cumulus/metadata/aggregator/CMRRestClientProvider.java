/** Copyright 2008, by the California Institute of Technology.
    ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.

    This class is implementing as a factory of CMR client.  CMR client provides the
    functionalities of access CMR (get, store tokens, search, validate, POST and PUT)

    This class is also the container of CMR cloud metadata such as
    provider
    CMR environmnet


 */
package gov.nasa.cumulus.metadata.aggregator;

import cumulus_message_adapter.message_parser.AdapterLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.*;

import gov.nasa.cumulus.metadata.util.S3Utils;

public class CMRRestClientProvider {
    private static String className = "gov.nasa.cumulus.metadata.aggregator.CMRRestClientProvider";
    private static CMRRestClient erc = null;
    private static CMRLambdaRestClient _elrc = null;
    /*
    CMR Provider.  Ex. POCUMULUS or POCLOUD
     */
    private static String provider;

    public static CMRLambdaRestClient getClient(String lambdaInputStr,
                                                String launchPadCryptoDir, String cmrURL, String region,
                                                String tokenBucket, String tokenFile)
    throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException,
            CertificateException, UnrecoverableKeyException, URISyntaxException {
        JsonObject inputJsonObj = JsonParser.parseString(lambdaInputStr).getAsJsonObject();
        JsonObject configJsonObj = inputJsonObj.getAsJsonObject("config");
        JsonObject launchpadConfigJsonObj = configJsonObj.getAsJsonObject("launchpadConfig");
        String systemBucket = configJsonObj.get("systemBucket").getAsString();
        String provider = configJsonObj.get("provider").getAsString();
        String apiEndPoint = launchpadConfigJsonObj.get("api").getAsString();
        String certificateFileName = launchpadConfigJsonObj.get("certificate").getAsString();
        String passphraseSecretName = launchpadConfigJsonObj.get("passphraseSecretName").getAsString();

        AdapterLogger.LogDebug(className + " pfx secret name: " + passphraseSecretName);
        String launchpadCertPassPhrase = getSecret(passphraseSecretName, region);

        // download the pfx file from s3 bucket.
        S3Utils s3Utils = new S3Utils();
        String workingDir = createWorkDir();
        String pfxFullPath = s3Utils.download(region, systemBucket,
                Paths.get(launchPadCryptoDir,certificateFileName).toString(),
                Paths.get(workingDir, certificateFileName).toString());
        _elrc = new CMRLambdaRestClient(pfxFullPath, launchpadCertPassPhrase,
                 apiEndPoint, cmrURL, provider,  region, tokenBucket, tokenFile, workingDir);
        return _elrc;
    }

    /**
     * get the static instance of CMRLambdaRestClient which is the component being used
     * to access CMR in cloud environment.
     *
     * To prevent getting null, the CMRLambdaRestClient must be called first with proper
     * cloud environment setup.
     * @return
     */
    public static CMRLambdaRestClient getLambdaRestClient() {
        return _elrc;
    }

    /**
     * Create a directory where we place temporary files
     * based on UUID.randomUUID()
     *
     * @return the path to new temporary directory
     */
    private static String createWorkDir() throws IOException {
        try {
            Path path = Files.createTempDirectory(Paths.get("/tmp"), "workDir");
            return path.toString();
        } catch (IOException ioe) {
            throw ioe;
        }
    }
    /**
     *
     * @param secretName : The name of the secret
     * @param region : Ex. us-west-2
     */
    public static String getSecret(String secretName, String region) {
        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
        // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
        // We rethrow the exception by default.

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        }

        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            return  getSecretValueResult.getSecretString();
        }
        else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }
    public static String getProvider() {
        return provider;
    }

    public static void setProvider(String provider) {
        CMRRestClientProvider.provider = provider;
    }
    
}
