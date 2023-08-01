package gov.nasa.cumulus.metadata.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gov.nasa.cumulus.metadata.aggregator.processor.ImageProcessor;
import gov.nasa.cumulus.metadata.umm.generated.RelatedUrlType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ImageProcessorTest {
    String cmrString = "";
    String cmaString = null;

    @Before
    public void initialize() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File inputCMRJsonFile = new File(classLoader.getResource("20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.cmr.json").getFile());
            cmrString = new String(Files.readAllBytes(inputCMRJsonFile.toPath()));

            File inputCMAJsonFile = new File(classLoader.getResource("cumulus_message_input_example.json").getFile());
            cmaString = new String(Files.readAllBytes(inputCMAJsonFile.toPath()));

        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

    @Test
    public void testIsImageFile() {
        ImageProcessor imageProcessor = new ImageProcessor();
        assertTrue(imageProcessor.isImageFile("s3://aaaa/bbb/granuleId.jpg"));
        assertTrue(imageProcessor.isImageFile("s3://aaaa/bbb/granuleId.png"));
        assertTrue(imageProcessor.isImageFile("s3://aaaa/bbb/granuleId.bmp"));
        assertTrue(imageProcessor.isImageFile("s3://aaaa/bbb/granuleId.gif"));
        assertTrue(imageProcessor.isImageFile("s3://aaaa/bbb/granuleId.tiff"));
    }

    @Test
    public void testGetImageMimeType() {
        ImageProcessor imageProcessor = new ImageProcessor();
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.pdf"),
                RelatedUrlType.MimeTypeEnum.NOT_PROVIDED);
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.jpg"),
                RelatedUrlType.MimeTypeEnum.IMAGE_JPEG);
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.png"),
                RelatedUrlType.MimeTypeEnum.IMAGE_PNG);
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.bmp"),
                RelatedUrlType.MimeTypeEnum.IMAGE_BMP);
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.gif"),
                RelatedUrlType.MimeTypeEnum.IMAGE_GIF);
        assertEquals(
                imageProcessor.getImageMimeType("s3://my-public-bucket/collection_name/granule/id/aa.tiff"),
                RelatedUrlType.MimeTypeEnum.IMAGE_TIFF);
    }

    @Test
    public void testGetImageDownloadUrl() {
        try {
            ImageProcessor imageProcessor = new ImageProcessor();
            String downloadUri = imageProcessor.getImageDownloadUrl("https://distribution/xxx/bb/download",
                    "my-public-bucket","/collection_name/granuleId/Image1.jpg");
            assertEquals(downloadUri,
                    "https://distribution/xxx/bb/download/my-public-bucket/collection_name/granuleId/Image1.jpg");
        } catch  (URISyntaxException uriSyntaxException) {
            System.out.println(uriSyntaxException);
            fail();
        }
    }

    /**
     * This test purposely make getImageDownloadUrl throwing URISyntaxException
     * by passing illegal character '^' as distribution_url.
     *
     * fail() will force the test case to fail. Since the test is to force URISyntaxException
     * to be thrown, it is a failed case if not thrown.
     *
     */
    @Test
    public void testGetImageDownloadUrl_URISyntaxException() {
        try {
            ImageProcessor imageProcessor = new ImageProcessor();
            String downloadUri = imageProcessor.getImageDownloadUrl("https://distribution/xxx/bb/download^12334",
                    "my-public-bucket","s3://my-public-bucket/collection_name/granuleId/image1.jpg");
            fail();
        } catch  (URISyntaxException uriSyntaxException) {
            assertTrue(true);
        }
    }

    @Test
    public void testAppendImageUrl() {

        try {
            /**
             * From the input message , the distribution_endpoint is set to be:
             *     "distribution_endpoint": "https://distribution_endpoint.jpl.nasa.gov/s3distribute/",
             *
             *      "filename": "s3://dyen-cumulus-public/dataset-image/MODIS_A-JPL-L2P-v2019.0/sst.png",
             *      "s3://dyen-cumulus-public/dataset-image/MODIS_A-JPL-L2P-v2019.0/standard-deviation.jpg",
             */
            ImageProcessor imageProcessor = new ImageProcessor();
            String newCMRStr = imageProcessor.appendImageUrl(cmaString, cmrString);
            JsonObject cmrJsonObj = JsonParser.parseString(newCMRStr).getAsJsonObject();
            JsonArray relatedUrls = cmrJsonObj.getAsJsonArray("RelatedUrls");
            int count = findTimesOfAppearance(relatedUrls,
                    "https://distribution_endpoint.jpl.nasa.gov/s3distribute/dyen-cumulus-public/dataset-image/MODIS_A-JPL-L2P-v2019.0/sst.png");
            assertEquals(count, 1);

            count = findTimesOfAppearance(relatedUrls,
                    "https://distribution_endpoint.jpl.nasa.gov/s3distribute/dyen-cumulus-public/dataset-image/MODIS_A-JPL-L2P-v2019.0/standard-deviation.jpg");
            assertEquals(count, 1);

            // test description
            for (JsonElement relatedUrl : relatedUrls) {
                JsonObject fileObj = relatedUrl.getAsJsonObject();
                String ummg_downloadUrl = StringUtils.trim(fileObj.get("URL").getAsString());
                if(ummg_downloadUrl.equals("https://distribution_endpoint.jpl.nasa.gov/s3distribute/dyen-cumulus-public/dataset-image/MODIS_A-JPL-L2P-v2019.0/sst.png")){
                    assertEquals(fileObj.get("Description").getAsString(), "sst");
                }
            }

        } catch (URISyntaxException | IOException pe) {
            System.out.println("testAppendImageUrl Error:" + pe);
            pe.printStackTrace();
        }
    }


    int findTimesOfAppearance(JsonArray relatedUrls, String downloadUrl) {
        int count = 0;
        downloadUrl = StringUtils.trim(downloadUrl);
        for (JsonElement relatedUrl : relatedUrls) {
            String ummg_downloadUrl = StringUtils.trim(relatedUrl.getAsJsonObject().get("URL").getAsString());
            if(StringUtils.compare(ummg_downloadUrl, downloadUrl) ==0) count ++;
        }
        return count;
    }

    @Test
    public void testIsDownloadUrlAlreadyExist() {
        ImageProcessor imageProcessor = new ImageProcessor();
        JsonObject cmrJsonObj = new JsonParser().parse(cmrString).getAsJsonObject();
        JsonArray relatedUrls = cmrJsonObj.getAsJsonArray("RelatedUrls");
        boolean isAlreadyExist = imageProcessor.isDownloadUrlAlreadyExist(relatedUrls,
                "https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-public/MODIS_A-JPL-L2P-v2019.0/20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.sses_standard_deviation.png");
        assertTrue(isAlreadyExist);
        /**
         * Capitalize the first character of the download string and the comparison result should be false
         */
        isAlreadyExist = imageProcessor.isDownloadUrlAlreadyExist(relatedUrls,
                "https://Jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-public/MODIS_A-JPL-L2P-v2019.0/20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.sses_standard_deviation.png");
        assertFalse(isAlreadyExist);
    }

    @Test
    public void testCreateOutputMessage() {
        ImageProcessor processor = new ImageProcessor();
        String output =  processor.createOutputMessage(cmaString, 334411,
                new BigInteger("3244"), "granuleId-3344-22.cmr.json", "my-private",
                "CMR", "collectionName");
        JsonElement jsonElement = JsonParser.parseString(output);
        JsonArray granules = jsonElement.getAsJsonObject().get("output").getAsJsonArray();
        JsonArray files = granules.get(0).getAsJsonObject().get("files").getAsJsonArray();

        JsonObject foundCMR =  processor.getFileJsonObjByFileTrailing(files, ".cmr.json");
        assertEquals(foundCMR.get("bucket").getAsString(), "my-private");
        assertEquals(foundCMR.get("key").getAsString(), "CMR/collectionName/granuleId-3344-22.cmr.json");
        assertEquals(foundCMR.get("fileName").getAsString(), "granuleId-3344-22.cmr.json");
        Long  cmrFileSize =  foundCMR.get("size").getAsLong();
        BigInteger  revisionId =  jsonElement.getAsJsonObject().get("cmrRevisionId").getAsBigInteger();
        assertEquals(334411, cmrFileSize.longValue());
        assertEquals(revisionId.compareTo(new BigInteger("3244")), 0);
    }
}
