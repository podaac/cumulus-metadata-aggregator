package gov.nasa.cumulus.metadata.test;

import gov.nasa.cumulus.metadata.aggregator.MetadataAggregatorLambda;
import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataAggregatorLambdaTest {
    String cmrString = "";
    String cmaString = null;

    @Before
    public void initialize() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            File inputCMAJsonFile = new File(classLoader.getResource("cumulus_message_input_example.json").getFile());
            cmaString = new String(Files.readAllBytes(inputCMAJsonFile.toPath()));

        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

    @Test
    public void testGetConceptId() throws ParseException {
        MetadataAggregatorLambda lambda = new MetadataAggregatorLambda();
        String conceptId = lambda.getConceptId(this.cmaString);
        assertEquals(conceptId, "G1238611022-POCUMULUS");
    }
    @Test
    public void testGetIsoXMLSpatialTypeArray() {
        MetadataAggregatorLambda lambda = new MetadataAggregatorLambda();
        assertEquals(lambda.getIsoXMLSpatialTypeStr("footprint"), "footprint");
        assertEquals(lambda.getIsoXMLSpatialTypeStr("orbit"), "orbit");
        assertEquals(lambda.getIsoXMLSpatialTypeStr("bbox"), "bbox");
        assertEquals(lambda.getIsoXMLSpatialTypeStr("xxxx"), "");
    }

    @Test
    public void testCreateIsoXMLSpatialTypeSet() {
        MetadataAggregatorLambda lambda = new MetadataAggregatorLambda();
        org.json.simple.JSONArray array = new JSONArray();
        array.add("footprint");
        array.add("orbit");
        //HashSet<MENDsIsoXMLSpatialTypeEnum> h = lambda.createIsoXMLSpatialTypeSet("[footprint,orbit]");
        HashSet<MENDsIsoXMLSpatialTypeEnum> h = lambda.createIsoXMLSpatialTypeSet(array);
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
        h = lambda.createIsoXMLSpatialTypeSet(array);
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));
        // last 2 items in the input array will result in NONE added into the HashSet and overwite each other
        assertTrue(h.size()==4);
    }

}
