package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import gov.nasa.cumulus.metadata.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

}
