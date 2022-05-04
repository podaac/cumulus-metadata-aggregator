package gov.nasa.cumulus.metadata.util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import cumulus_message_adapter.message_parser.AdapterLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * This is an utility class which provides functions to access S3 bucket.
 * Ex. upload, download and get eTag.  Since AmazonS3Client object is thread safe,
 * we are declaring a member function to act as a factory of AmazonS3Client and reuse such client.
 */
public class S3Utils {
    private boolean amazonS3ClientCreated;
    private final String className = this.getClass().getName();
    private AmazonS3 s3Client = null;

    private AmazonS3 getS3Client(String region) {
        if (!this.amazonS3ClientCreated ){
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .build();
            this.amazonS3ClientCreated = true;
        }
        return s3Client;
    }
    /**
     * Get eTag of an S3 object
     *
     * @param bucket
     * @param key
     */
    public String getS3ObjectETag(String region, String bucket, String key) {
        AmazonS3 s3Client = getS3Client(region);
        AdapterLogger.LogDebug(this.className + " Trying to tag bucket:" + bucket + " key:" +
                key);
        S3Object s3Object = s3Client.getObject(bucket, key);
        ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
        return objectMetadata.getETag();
    }

    /**
     * Download a file from S3
     *
     * @param bucket                 the bucket the file is located in
     * @param key                    the key of the file
     * @param outputFileAbsolutePath the absolute path of where to download this S3 file to
     * @return the absolute path of the downloaded file
     */
    public String download(String region, String bucket, String key, String outputFileAbsolutePath) {
        AdapterLogger.LogInfo(this.className + " Downloading from bucket: " + bucket + " key: " + key
                + " outputFileAbsolutePath: " + outputFileAbsolutePath);
        AmazonS3 s3Client = this.getS3Client(region);
        File file = new File(outputFileAbsolutePath);
        if (!StringUtils.isBlank(bucket) && !StringUtils.isBlank(key)) {
            s3Client.getObject(new GetObjectRequest(
                    bucket, key), file);
            return file.getAbsolutePath();
        } else {
            return "";
        }
    }

    /**
     * Upload a file to S3
     *
     * @param bucket the bucket to upload the file to
     * @param key    the key to upload to file into
     * @param file   the file to upload
     * @return The S3 URI of the uploaded file
     */
    public void upload(String region, String bucket, String key, File file) {
        AdapterLogger.LogDebug("Uploading to bucket: " + bucket + " key: " + key + " file: " + file);
        AmazonS3 s3Client = getS3Client(region);
        try {
            AdapterLogger.LogInfo(this.className + " Uploading to bucket: " + bucket + " key:" + key);
            s3Client.putObject(new PutObjectRequest(bucket, key, file));
            AdapterLogger.LogInfo(this.className + " Finished uploading an object: ");
        } catch (AmazonServiceException ase) {
            AdapterLogger.LogError(this.className + " Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            AdapterLogger.LogError(this.className + " Error Message:    " + ase.getMessage());
            AdapterLogger.LogError(this.className + " HTTP Status Code: " + ase.getStatusCode());
            AdapterLogger.LogError(this.className + " AWS Error Code:   " + ase.getErrorCode());
            AdapterLogger.LogError(this.className + " Error Type:       " + ase.getErrorType());
            AdapterLogger.LogError(this.className + " Request ID:       " + ase.getRequestId());
            throw ase;
        } catch (AmazonClientException ace) {
            AdapterLogger.LogError(this.className + " Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            AdapterLogger.LogError(this.className + " Error Message: " + ace.getMessage());
            throw ace;
        }
    }

    /**
     * Delete object from S3 bucket
     *
     * @param bucket
     * @param key
     */
    public void delete(String region, String bucket, String key) {
        AmazonS3 s3Client = getS3Client(region);
        AdapterLogger.LogInfo(this.className + " Deleting bucket: " + bucket + " key:" + key);
        s3Client.deleteObject(bucket, key);
    }

    /**
     * Check if object existed on S3.
     *
     * @param bucket
     * @param key
     */
    public boolean isObjectExist(String region, String bucket, String key) {
        AmazonS3 s3Client = getS3Client(region);
        AdapterLogger.LogInfo(this.className + " Checking object exists in bucket: " + bucket + " key:" + key);
        return s3Client.doesObjectExist(bucket, key);
    }
}
