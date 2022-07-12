package gov.nasa.cumulus.metadata.aggregator.processor;

import com.google.gson.JsonObject;
import com.vividsolutions.jts.io.ParseException;
import cumulus_message_adapter.message_parser.AdapterLogger;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;

public class DMRPPProcessor extends ProcessorBase{
    private final String className = this.getClass().getName();
    public String process(String input, String ummgStr, String region, String revisionId)
            throws IOException, ParseException {
        try {
            String cmrBucket = System.getenv().getOrDefault("INTERNAL_BUCKET", "");
            String cmrDir = System.getenv().getOrDefault("CMR_DIR", "");
            AdapterLogger.LogDebug(this.className + " internal bucket: " + cmrBucket + " CMR Dir: " + cmrDir);
            this.region = region;
            decodeVariables(input);
            String cmrFileName = buildCMRFileName(this.granuleId, this.executionId);
            long cmrFileSize = uploadCMRJson(cmrBucket, cmrDir, this.collectionName, cmrFileName, ummgStr);
            // Create the eTag for CMR file.
            String cmrETag = s3Utils.getS3ObjectETag(this.region, cmrBucket,
                    Paths.get(cmrDir, this.collectionName,
                            cmrFileName).toString());
            AdapterLogger.LogDebug(this.className + " cmr.json file size: " + cmrFileSize);
            String output = createOutputMessage(input, cmrFileSize, cmrETag, new BigInteger(revisionId),
                    cmrFileName,
                    cmrBucket, cmrDir, this.collectionName);
            return output;
        } catch (IOException e) {
            AdapterLogger.LogError("Footprint processor exception:" + e);
            throw e;
        }
    }

}
