package gov.nasa.cumulus.metadata.test;

import gov.nasa.cumulus.metadata.aggregator.IsoGranule;
import gov.nasa.cumulus.metadata.aggregator.MetadataAggregatorLambda;
import gov.nasa.cumulus.metadata.aggregator.MetadataFilesToEcho;
import gov.nasa.cumulus.metadata.aggregator.bo.TaskConfigBO;
import gov.nasa.cumulus.metadata.aggregator.factory.TaskConfigFactory;
import gov.nasa.cumulus.metadata.aggregator.processor.RelatedUrlsProcessor;
import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RelatedUrlsProcessorTest {
    TaskConfigBO taskConfigBO = new TaskConfigBO();

    @Before
    public void setUp() throws Exception {
        // Setup ArrayList<HashMap<String, String>> subTypeHashArray which is a data structure presenting relatedUrlSubTypeMap
        // Read relatedUrlSubTypeMap from collection_config/OPERA_L3_DSWX-S1_PROVISIONAL_V0.json which is a collection configuration json
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("collection_config/OPERA_L3_DSWX-S1_PROVISIONAL_V0.json").getFile());
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
        JSONObject collectionJSONObject = (JSONObject) obj;
        // extract the relatedUrlSubTypeMap from collection configuration meta structure
        JSONArray subTypeHasyArrayJson = (JSONArray)(((JSONObject)(collectionJSONObject.get("meta"))).get("relatedUrlSubTypeMap"));
        JSONObject config =  new JSONObject();
        config.put("relatedUrlSubTypeMap", subTypeHasyArrayJson);
        taskConfigBO =  TaskConfigFactory.createTaskConfigBO(config);
    }

    @Test
    public void testAppendSubTypes() throws IOException, ParseException, ParserConfigurationException,
            SAXException, XPathExpressionException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_1.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);
        mfte.getGranule().setName("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0");
        Document doc = null;
        XPath xpath = null;
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
        // use a unrelated .mp file to patch required field so mfte.createJson() would work.
        File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        mfte.getGranule().setName("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0");
        JSONObject granuleJson = mfte.createJson();

        org.json.simple.JSONArray files = new JSONArray();
        JSONObject f1 = new JSONObject();
        JSONObject f2 = new JSONObject();
        JSONObject f3 = new JSONObject();
        // construct the files:[] array under payload
        f1.put("source", "OPERA_L3_DSWX-S1_PROVISIONAL_V0/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B01_WTR.tif");
        f1.put("fileName", "OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B01_WTR.tif");
        f1.put("type", "data");
        f1.put("checksumType", "md5");
        f2.put("source", "OPERA_L3_DSWX-S1_PROVISIONAL_V0/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B02_BWTR.tif");
        f2.put("fileName", "OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B02_BWTR.tif");
        f2.put("type", "data");
        f2.put("checksumType", "md5");
        f3.put("source", "OPERA_L3_DSWX-S1_PROVISIONAL_V0/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4/OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B03_CONF.tif");
        f3.put("fileName", "OPERA_L3_DSWx-S1_T35TPJ_20240214T155240Z_20240502T001754Z_S1A_30_v0.4_B03_CONF.tif");
        f3.put("type", "data");
        f3.put("checksumType", "md5");
        files.add(f1);files.add(f2);files.add(f3);
        RelatedUrlsProcessor relatedUrlsProcessor = new RelatedUrlsProcessor();
        // Use appendSubTypes to generate the new Json including certain file items with subType
        JSONObject newGranuleJson = relatedUrlsProcessor.appendSubTypes(granuleJson,taskConfigBO, files);
        JSONArray relatedUrls =(JSONArray) newGranuleJson.get("RelatedUrls");
        ArrayList<HashMap<String, String>> subTypeHashArray = taskConfigBO.getSubTypeHashArray();
        /**
         * We loop through the RelatedUrls array.  for each relatedUrl item.  If the URL field match the regex of
         * a subTypeHash (which for example contains "regex": "^.*_B01_WTR\\.tif$",
         *         "subType": "BROWSE IMAGE SOURCE"), the Subtype field should match the
         * subTypeHash's subType value
         */
        for(int i = 0; i < relatedUrls.size(); i++)
        {
            JSONObject relatedUrl = (JSONObject)relatedUrls.get(i);
            String URLStr=relatedUrl.get("URL").toString();
            for(HashMap<String, String> subTypeHash : subTypeHashArray) {
                if(URLStr.matches(subTypeHash.get("regex"))) {
                    assertEquals(relatedUrl.get("Subtype"), subTypeHash.get("subType"));
                }
            }
        }
        // Compare the newGranuleJson with the pre-saved cmr.json file
        assertTrue(UnitTestUtil.compareFileWithGranuleJson("ummgResults/setSubType/OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.cmr.json", newGranuleJson));
    }
}
