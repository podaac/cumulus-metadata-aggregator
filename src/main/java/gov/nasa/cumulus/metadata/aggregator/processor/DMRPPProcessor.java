package gov.nasa.cumulus.metadata.aggregator.processor;

import com.google.gson.*;
import com.vividsolutions.jts.io.ParseException;
import cumulus_message_adapter.message_parser.AdapterLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Iterator;

public class DMRPPProcessor extends ProcessorBase{
    private final String className = this.getClass().getName();
    public String process(String input, String ummgStr, String region, String revisionId)
            throws IOException, ParseException {
        try {
            ummgStr = cleanseOPeNDAPUrl(ummgStr);
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
            String output = createOutputMessage(input, cmrFileSize, new BigInteger(revisionId),
                    cmrFileName,
                    cmrBucket, cmrDir, this.collectionName);
            return output;
        } catch (IOException e) {
            AdapterLogger.LogError("Footprint processor exception:" + e);
            throw e;
        }
    }

    private String cleanseOPeNDAPUrl(String ummgStr) {
        Gson gsonBuilder = getGsonBuilder();
        JsonObject cmrJsonObj = JsonParser.parseString(ummgStr).getAsJsonObject();
        JsonArray relatedUrls = cmrJsonObj.getAsJsonArray("RelatedUrls");
        if (relatedUrls == null) {
            return ummgStr;
        }

        Iterator<JsonElement> iterator = relatedUrls.iterator();
        while(iterator.hasNext()){
            JsonElement node = iterator.next();
            JsonElement subTypeElement = node.getAsJsonObject().get("Subtype");
            if (subTypeElement != null && StringUtils.equalsIgnoreCase(
                    StringUtils.trim(subTypeElement.getAsString()), "OPENDAP DATA")) {
                AdapterLogger.LogInfo(this.className + " found OPENDAP LINK and removing: " + node);
                iterator.remove();
            }
        }
        return gsonBuilder.toJson(cmrJsonObj);
    }

}
