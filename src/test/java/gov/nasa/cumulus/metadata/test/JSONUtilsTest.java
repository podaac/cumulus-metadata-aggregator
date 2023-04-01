package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;


import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import gov.nasa.cumulus.metadata.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class JSONUtilsTest {
    @Test
    public void testGsonToJSONObj() throws IOException, ParseException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.cmr.json").getFile());
        String cmrStr = FileUtils.readFileToString(file, "UTF-8");
        JsonObject jsonObject = JsonParser.parseString(cmrStr).getAsJsonObject();
        JSONObject translatedSimpleJsonObj = JSONUtils.GsonToJSONObj(jsonObject);
        /**
         * After translation, do a few "Point checks"
         */
        assertEquals(translatedSimpleJsonObj.containsKey("SpatialExtent"), true);
        JSONObject spatialExtent = (JSONObject)translatedSimpleJsonObj.get("SpatialExtent");
        JSONObject horizontalSpatialDomain = (JSONObject)spatialExtent.get("HorizontalSpatialDomain");
        JSONObject geometry = (JSONObject)horizontalSpatialDomain.get("Geometry");
        JSONArray  boundingRectangles = (JSONArray)geometry.get("BoundingRectangles");
        assertEquals(boundingRectangles.size(), 2);
        JSONObject dataGranule = (JSONObject)translatedSimpleJsonObj.get("DataGranule");
        String productionDateTime = (String)dataGranule.get("ProductionDateTime");
        assertEquals(productionDateTime, "2020-02-29T12:20:15.000Z");
    }

    @Test
    public void testIsStartingWithHttpHttps() throws IOException, ParseException{
        assertEquals(JSONUtils.isStartingWithHttpHttps("http://aabbcc"), true);
        assertEquals(JSONUtils.isStartingWithHttpHttps("https://myresourc.com"), true);
        //test with space
        assertEquals(JSONUtils.isStartingWithHttpHttps(" http://mm.ee.com "), true);
        assertEquals(JSONUtils.isStartingWithHttpHttps(" https://mm.com/resource  "), true);

        // test false condition
        assertEquals(JSONUtils.isStartingWithHttpHttps(" htt:p:// "), false);
        assertEquals(JSONUtils.isStartingWithHttpHttps(" https;//  "), false);
    }
    @Test
    public void testIsStartingWithS3() throws IOException, ParseException{
        assertEquals(JSONUtils.isStartingWithS3("s3://mybucket/myfolder/file.txt"), true);
        //test with space
        assertEquals(JSONUtils.isStartingWithS3(" s3://mybucket/myfolder/file.txt "), true);

        // test false condition
        assertEquals(JSONUtils.isStartingWithS3(" s3:p://mybucket/myfolder/file.txt "), false);
        assertEquals(JSONUtils.isStartingWithS3(" s3;//mybucket/myfolder/file.txt  "), false);
    }
    @Test
    public void testIsGETTYPE() throws IOException, ParseException {
        assertEquals(JSONUtils.isGETTYPE("GET DATA"), true);
        assertEquals(JSONUtils.isGETTYPE(" GET DATA "), true);
        assertEquals(JSONUtils.isGETTYPE(" Get data "), true);
        //test with space
        assertEquals(JSONUtils.isStartingWithS3(" GEET Type "), false);
    }

    @Test
    public void testSorting() throws  ParseException{
        String cmr_filename= "20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0-unsortedUrls.cmr.json";
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File inputCMRJsonFile = new File(classLoader.getResource(cmr_filename).getFile());
            String cmrString = new String(Files.readAllBytes(inputCMRJsonFile.toPath()));
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(cmrString);
            json = JSONUtils.sortRelatedUrls(json);
            //TODO check sorted result
            assertEquals(true, true);
        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

}
