package gov.nasa.cumulus.metadata.aggregator.processor;

import com.google.gson.*;
import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.util.S3Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProcessorBase {
    private final String className = this.getClass().getName();

    String collectionName;
    String granuleId;
    JsonArray files;
    String workingDir;
    String region;
    String executionId;
    S3Utils s3Utils = new S3Utils();

    /**
     * Create a directory where we dump granule file, nc file and generated footprint file
     * based on UUID.randomUUID()
     *
     * @return the path to new temporary directory
     */
    protected String createWorkDir() throws IOException {
        try {
            Path path = Files.createTempDirectory(Paths.get("/tmp"), "workDir");
            return path.toString();
        } catch (IOException ioe) {
            AdapterLogger.LogError(this.className + " creating working dir failed: " + ioe.getMessage());
            throw ioe;
        }
    }

    /**
     * Find file object reference by an input JsonArray for files and
     * a  string which is trailing of the file (Ex. .fp  or .cmr.json).
     * case ignored
     *
     * @param files  file JsonElement
     * @param trail  file trailing
     * @return
     */
    public static JsonObject getFileJsonObjByFileTrailing(JsonArray files, String trail) {
        for (JsonElement f : files) {
            if (StringUtils.endsWith(
                    StringUtils.trim(
                            StringUtils.lowerCase(f.getAsJsonObject().get("fileName").getAsString())
                    ) // ene of trim
                    , trail)) {
                return f.getAsJsonObject();
            }
        }
        return null;
    }

    /**
     * Download file to local working directory
     *
     * @param jsonObject
     * @return
     */
    protected String downloadFile(JsonObject jsonObject) {
        AdapterLogger.LogInfo(this.className + " trying to download: " + jsonObject.get("fileName").getAsString());
        return s3Utils.download(this.region, jsonObject.get("bucket").getAsString(),
                jsonObject.get("key").getAsString(),
                Paths.get(this.workingDir, jsonObject.get("fileName").getAsString()).toString());
    }

    protected long uploadCMRJson( String cmrBucket, String cmrBaseDir, String collectionName, String cmrFileName,
                                  String newCMRStr)
            throws IOException {
        // create a new working directory
        AdapterLogger.LogError(this.className + " bucket:" + cmrBucket + " dir:" + cmrBaseDir +
                " collectionName:"+ collectionName + " cmrFileName:"+ cmrFileName);
        String cmrFileWorkDir = this.createWorkDir();
        try {
            // The local file does not need executionId in the fileName
            File file = new File(Paths.get(cmrFileWorkDir, cmrFileName).toString());
            FileUtils.writeStringToFile(file, newCMRStr, Charset.defaultCharset());
            s3Utils.upload(this.region, cmrBucket,
                    Paths.get(cmrBaseDir, collectionName, cmrFileName).toString(),
                    file);
            return file.length();
        } catch (IOException ioe) {
            AdapterLogger.LogError(this.className + " write CMR string error:" + ioe);
            throw ioe;
        } finally {
            FileUtils.forceDelete(new File(cmrFileWorkDir));
        }
    }

    Gson getGsonBuilder() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
    }

    /**
     * Delete the working directory and all its content
     *
     * @param workingDir : local working directory in full path
     * @return
     * @throws IOException
     */
    protected void deleteWorkDir(String workingDir) throws IOException {
        try {
            FileUtils.forceDelete(new File(workingDir));
        } catch (IOException ioe) {
            AdapterLogger.LogError(this.className + " delete working dir failed: " + ioe.getMessage());
            throw ioe;
        }
    }

    public void decodeVariables(String input) {
        JsonObject inputJsonObj = JsonParser.parseString(input).getAsJsonObject();
        JsonArray granules = inputJsonObj.getAsJsonArray("input");
        JsonObject granule = granules.get(0).getAsJsonObject();

        // Parse config values
        JsonObject config = inputJsonObj.getAsJsonObject("config");
        collectionName = config.get("collection").getAsString();
        /** if subworkflow is triggered by bulk operation.  there will be no executionId */

        executionId = config.get("executionId") != null? config.get("executionId").getAsString():
                UUID.randomUUID().toString();

        granuleId = granule.get("granuleId").getAsString();
        files = granule.get("files").getAsJsonArray();
    }

    public String buildCMRFileName(String granuleId, String executionId) {
        return granuleId+ "-" + executionId + ".cmr.json";
    }

    public String createOutputMessage(String input, long uploadedCMRFileSize, String eTag, BigInteger revisionId,
                                      String cmrFileName,
                                      String cmrBucket, String cmrDir, String collectionName) {
        JsonObject inputJsonObj = JsonParser.parseString(input).getAsJsonObject();
        JsonArray granules = inputJsonObj.getAsJsonArray("input");
        JsonObject granule = granules.get(0).getAsJsonObject();
        // change CMR.json file size
        JsonArray files = granule.get("files").getAsJsonArray();
        String filepath = Paths.get(cmrDir, collectionName, cmrFileName).toString();
        JsonObject f = new JsonObject();
        f.addProperty("bucket", cmrBucket);
        if(StringUtils.startsWith(filepath, "/")) {
            filepath = StringUtils.replaceOnce(filepath, "/", "");
        }
        f.addProperty("key",  filepath);
        f.addProperty("fileName", cmrFileName);
        f.addProperty("size", uploadedCMRFileSize);
        // get and remove .fp file item
        JsonObject fp = getFileJsonObjByFileTrailing(files, ".fp");
        if(fp != null) {
            files.remove(fp);
        }
        JsonObject old_cmr = getFileJsonObjByFileTrailing(files, ".cmr.json");
        if(old_cmr != null) {
            files.remove(old_cmr);
        }
        files.add(f);

        JsonObject outputJsonObj = new JsonObject();
        outputJsonObj.add("output", granules);
        outputJsonObj.addProperty("cmrRevisionId", revisionId);
        String outputSt = new Gson().toJson(outputJsonObj);
        return outputSt;
    }
}
