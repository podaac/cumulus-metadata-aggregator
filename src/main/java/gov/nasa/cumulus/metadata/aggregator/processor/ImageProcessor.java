package gov.nasa.cumulus.metadata.aggregator.processor;

import com.google.gson.*;
import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.umm.generated.RelatedUrlType;
import gov.nasa.cumulus.metadata.util.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class ImageProcessor extends ProcessorBase{
    private final String className = this.getClass().getName();

    public static boolean isImageFileExisting(String input) {
        JsonObject inputKey = JsonParser.parseString(input).getAsJsonObject();
        JsonArray granules = inputKey.getAsJsonArray("input");
        JsonObject granule = granules.get(0).getAsJsonObject();
        JsonArray fileArray = granule.get("files").getAsJsonArray();
        if(getFileJsonObjByFileTrailing(fileArray, ".png") != null)
            return true;
        else
            return false;
    }
    public String process(String input, String ummgStr, String region, String revisionId)
            throws IOException, URISyntaxException {
        try {
            String cmrBucket = System.getenv().getOrDefault("INTERNAL_BUCKET", "");
            String cmrDir = System.getenv().getOrDefault("CMR_DIR", "");
            AdapterLogger.LogDebug(this.className + " internal bucket: " + cmrBucket + " CMR Dir: " + cmrDir);
            this.region = region;
            decodeVariables(input);
            this.workingDir = createWorkDir();
            String newCMRStr = appendImageUrls(input, ummgStr);
            String cmrFileName = buildCMRFileName(this.granuleId, this.executionId);
            long cmrFileSize = uploadCMRJson(cmrBucket, cmrDir, this.collectionName, cmrFileName,
                    newCMRStr);
            // Create the MD5 hash for CMR file.
            String cmrETag = s3Utils.getS3ObjectETag(this.region, cmrBucket,
                    Paths.get(cmrDir, this.collectionName,
                            cmrFileName).toString());
            String output = createOutputMessage(input, cmrFileSize, new BigInteger(revisionId),
                    cmrFileName,
                    cmrBucket, cmrDir, this.collectionName);
            return output;
        } catch (IOException | URISyntaxException e) {
            AdapterLogger.LogError("Image processor exception:" + e);
            throw e;
        } finally {
            deleteWorkDir(this.workingDir);
        }
    }

    public String appendImageUrls(String input, String cmrString)
            throws IOException, URISyntaxException {
        try {
            Gson gsonBuilder = getGsonBuilder();
            JsonObject cmrJsonObj = JsonParser.parseString(cmrString).getAsJsonObject();
            JsonArray relatedUrls = cmrJsonObj.getAsJsonArray("RelatedUrls");
            // If cmrJson does not include relatedURLs jsonArray, then create one and then attached to cmrJsonObj
            if (relatedUrls == null) {
                relatedUrls = new JsonArray();
            }

            JsonObject inputJsonObj = JsonParser.parseString(input).getAsJsonObject();
            JsonArray granules = inputJsonObj.getAsJsonArray("input");
            JsonObject granule = granules.get(0).getAsJsonObject();
            // Parse config values
            JsonObject config = inputJsonObj.getAsJsonObject("config");
            String distribution_endpoint = config.get("distribution_endpoint").getAsString();

            files = granule.get("files").getAsJsonArray();
            JsonArray files = granule.get("files").getAsJsonArray();
            /*  At this point, the RelatedUrls array has been sorted with
                http/https scientific data
                other http files
                s3 scientific data
                other s3 file
             */
            // first split relatedUrls to 2 arrays, one including http/https resources and
            // the other array contain items which are NOT http/https resources
            JsonArray httpArray = new JsonArray();
            JsonArray otherItemsArray = new JsonArray();
            String[] httpStrs = {"http", "https"};
            relatedUrls.forEach(e -> {
                if(JSONUtils.isStrStarsWithIgnoreCase(e.getAsJsonObject().get("URL").getAsString(), httpStrs)) {
                    httpArray.add(e);
                } else {
                    otherItemsArray.add(e);
                }
            });
            for (JsonElement f : files) {
                JsonObject fileObj = f.getAsJsonObject();
                String filename = StringUtils.trim(f.getAsJsonObject().get("fileName").getAsString());
                if(isImageFile(filename)) {
                    String downloadUrl = getImageDownloadUrl(distribution_endpoint, fileObj.get("bucket").getAsString(),
                            fileObj.get("key").getAsString());
                    if(!isDownloadUrlAlreadyExist(relatedUrls, downloadUrl)) {
                        RelatedUrlType relatedUrlType = new RelatedUrlType();
                        relatedUrlType.setUrl(downloadUrl);
                        relatedUrlType.setType(RelatedUrlType.RelatedUrlTypeEnum.GET_RELATED_VISUALIZATION);
                        relatedUrlType.setSubtype(RelatedUrlType.RelatedUrlSubTypeEnum.DIRECT_DOWNLOAD);
                        relatedUrlType.setMimeType(getImageMimeType(filename));

                        String relatedUrlTypeStr = gsonBuilder.toJson(relatedUrlType);
                        // append image Url to the end of the http array which has all http/https resources
                        httpArray.add(JsonParser.parseString(relatedUrlTypeStr));
                    }
                }
            }
            // now add all elements from the other array into the end of the httpArray
            otherItemsArray.forEach(e -> httpArray.add(e)); // append otherItmesArray on the end of httpArray
            cmrJsonObj.add("RelatedUrls", httpArray);
            String newCMRStr = gsonBuilder.toJson(cmrJsonObj);
            return newCMRStr;
        } catch (URISyntaxException ipe) {
            AdapterLogger.LogFatal(this.className + " constructing relatedURLs error:" + ipe);
            throw ipe;
        }
    }

    public boolean isDownloadUrlAlreadyExist(JsonArray relatedUrls, String downloadUrl)
    {
        downloadUrl = StringUtils.trim(downloadUrl);
        for (JsonElement relatdUrl : relatedUrls) {
            String umg_downloadUrl = StringUtils.trim(relatdUrl.getAsJsonObject().get("URL").getAsString());
            if(StringUtils.compare(umg_downloadUrl, downloadUrl) ==0) return true;
        }
        return false;
    }

    public boolean isImageFile(String filename) {
        return StringUtils.endsWith(filename, ".jpg") ||
                StringUtils.endsWith(filename, ".png") ||
                StringUtils.endsWith(filename, ".bmp") ||
                StringUtils.endsWith(filename, ".gif") ||
                StringUtils.endsWith(filename, ".tiff");
    }

    public RelatedUrlType.MimeTypeEnum getImageMimeType(String filename) {
        RelatedUrlType.MimeTypeEnum determinedMimeType = RelatedUrlType.MimeTypeEnum.NOT_PROVIDED;
        if (StringUtils.endsWith(filename, ".png"))
            determinedMimeType = RelatedUrlType.MimeTypeEnum.IMAGE_PNG;
        else if (StringUtils.endsWith(filename, ".jpg"))
            determinedMimeType = RelatedUrlType.MimeTypeEnum.IMAGE_JPEG;
        else if (StringUtils.endsWith(filename, ".bmp"))
            determinedMimeType = RelatedUrlType.MimeTypeEnum.IMAGE_BMP;
        else if (StringUtils.endsWith(filename, ".gif"))
            determinedMimeType = RelatedUrlType.MimeTypeEnum.IMAGE_GIF;
        else if (StringUtils.endsWith(filename, ".tiff"))
            determinedMimeType = RelatedUrlType.MimeTypeEnum.IMAGE_TIFF;

        return determinedMimeType;
    }

    public String getImageDownloadUrl(String distribution_url, String bucket, String key)
            throws URISyntaxException{
        try {
            URIBuilder uriBuilder = new URIBuilder(distribution_url);
            return uriBuilder.setPath(uriBuilder.getPath() + "/" + Paths.get(bucket, key).toString()).build().normalize().toString();
        } catch (URISyntaxException uriSyntaxException) {
            throw uriSyntaxException;
        }
    }
}
