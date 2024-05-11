package gov.nasa.cumulus.metadata.test;

import gov.nasa.cumulus.metadata.aggregator.MetadataAggregatorLambda;
import gov.nasa.cumulus.metadata.aggregator.factory.TaskConfigFactory;
import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskConfigFactoryTest {

    @Test
    public void testGetIsoXMLSpatialTypeStr() {
        assertEquals(TaskConfigFactory.getIsoXMLSpatialTypeStr("footprint"), "footprint");
        assertEquals(TaskConfigFactory.getIsoXMLSpatialTypeStr("orbit"), "orbit");
        assertEquals(TaskConfigFactory.getIsoXMLSpatialTypeStr("bbox"), "bbox");
        assertEquals(TaskConfigFactory.getIsoXMLSpatialTypeStr("xxxx"), "");
    }

    @Test
    public void testCreateIsoXMLSpatialTypeSet() {
        org.json.simple.JSONArray array = new JSONArray();
        array.add("footprint");
        array.add("orbit");
        HashSet<MENDsIsoXMLSpatialTypeEnum> h = TaskConfigFactory.createIsoXMLSpatialTypeSet(array);
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));

        array.clear();
        array.add("footprint");
        array.add("orbit");
        array.add("bbox");
        array.add("eebb");
        array.add("ccmm");
        //h = lambda.createIsoXMLSpatialTypeSet("[footprint,orbit,bbox,eebb,ccmm]");
        h = TaskConfigFactory.createIsoXMLSpatialTypeSet(array);
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));
        // last 2 items in the input array will result in NONE added into the HashSet and overwite each other
        assertTrue(h.size()==4);
    }

    @Test
    public void testCreateSubTypeHashArray() throws FileNotFoundException, ParseException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("collection_config/OPERA_L3_DSWX-S1_PROVISIONAL_V0.json").getFile());
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
        JSONObject collectionJSONObject = (JSONObject) obj;
        // extract the relatedUrlSubTypeMap from collection configuration meta structure
        JSONArray subTypeHasyArrayJson = (JSONArray)(((JSONObject)(collectionJSONObject.get("meta"))).get("relatedUrlSubTypeMap"));
        ArrayList<HashMap<String, String>> subTypeHashArray =  TaskConfigFactory.createSubTypeHashArray(subTypeHasyArrayJson);
        HashMap<String,String> subTypeHash = subTypeHashArray.get(0);
        String regexStr = subTypeHash.get("regex");
        String subTypeStr = subTypeHash.get("subType");
        assertTrue(StringUtils.equals(regexStr, "^.*_B01_WTR\\.tif$"));
        assertTrue(StringUtils.equals(subTypeStr, "BROWSE IMAGE SOURCE"));
        subTypeHash = subTypeHashArray.get(1);
        regexStr = subTypeHash.get("regex");
        subTypeStr = subTypeHash.get("subType");
        assertTrue(StringUtils.equals(regexStr, "^.*_B02_BWTR\\.tif$"));
        assertTrue(StringUtils.equals(subTypeStr, "IceBridge Portal"));
    }
}
