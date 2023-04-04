package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;


import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import gov.nasa.cumulus.metadata.umm.generated.RelatedUrlType;
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
    public void testIsStartWithStrings() {
        String elements[] = {"http", "https"};
        String httpStr = "http://distribution_url/resource.nc";
        assertEquals(JSONUtils.isStrStarsWithIgnoreCase(httpStr, elements), true);
        httpStr = "https://distribution_url/resource.nc";
        assertEquals(JSONUtils.isStrStarsWithIgnoreCase(httpStr, elements), true);
        httpStr = "  http://distribution_url/resource.nc";  // test with space
        assertEquals(JSONUtils.isStrStarsWithIgnoreCase(httpStr, elements), true);

        httpStr = "  s3://my_bucket/my_folder/resource.nc";  // test with space
        assertEquals(JSONUtils.isStrStarsWithIgnoreCase(httpStr, elements), false);


    }
    @Test
    public void testIsGETDataType() {
        assertEquals(JSONUtils.isGETDataType("GET DATA"), true);
        assertEquals(JSONUtils.isGETDataType(" GET DATA "), true);
        assertEquals(JSONUtils.isGETDataType(" Get data "), true);
        //test with space
        assertEquals(JSONUtils.isGETDataType(" GEET Type "), false);
    }

    @Test
    public void testRelatedUrlsSorting() throws  ParseException{
        String cmr_filename= "20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0-unsortedUrls.cmr.json";
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File inputCMRJsonFile = new File(classLoader.getResource(cmr_filename).getFile());
            String cmrString = new String(Files.readAllBytes(inputCMRJsonFile.toPath()));
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(cmrString);
            json = JSONUtils.sortRelatedUrls(json);
            //check the first item must be http/https resource scientific data
            JSONArray relatedUrlsArray = (JSONArray)json.get("RelatedUrls");
            JSONObject firstJSONObject = (JSONObject)relatedUrlsArray.get(0);
            assertEquals(firstJSONObject.get("URL").toString(), "https://vtdmnpv139.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-protected/20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.nc");
            assertEquals(firstJSONObject.get("Type").toString(), RelatedUrlType.RelatedUrlTypeEnum.GET_DATA.value());
            //check the 6th item must be http/https resource scientific data
            JSONObject sixthJSONObject = (JSONObject)relatedUrlsArray.get(6);
            assertEquals(sixthJSONObject.get("URL").toString(), "s3://my-bucket/folder/20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.nc");
            assertEquals(sixthJSONObject.get("Type").toString(), RelatedUrlType.RelatedUrlTypeEnum.GET_DATA.value());
        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

}
