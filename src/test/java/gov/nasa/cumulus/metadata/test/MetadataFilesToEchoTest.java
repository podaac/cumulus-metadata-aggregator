package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.nasa.cumulus.metadata.aggregator.*;

import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;
import java.util.stream.Collectors;


public class MetadataFilesToEchoTest {


    @Test
    public void testParseCommonHandlerFiles() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.mp").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readCommonMetadataFile(file.getAbsolutePath(), "s3://a/path/to/s3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(new Long(1491622211000l), mfte.getGranule().getStartTimeLong());
    }


    @Test
    public void testParseFootprintFiles() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.fp.xml").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readFootprintFile(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(2, mfte.getGranule().getGranuleCharacterSet().size());
    }

    @Test
    public void testSetDatasetValues() throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject boundingBox = (JSONObject) parser.parse("{\"boundingBox\": { \"latMin\": -90.0, \"lonMin\": -180.0, \"latMax\": 90.0, \"lonMax\": 180.0}}");
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();

        mfte.setDatasetValues("MODIS_T-JPL-L2P-v2019.0", "2019.0", false, (JSONObject) boundingBox.get("boundingBox"));
        assertEquals("MODIS_T-JPL-L2P-v2019.0", mfte.getDataset().getShortName());
        assertEquals("2019.0", UMMUtils.getDatasetVersion(mfte.getDataset()));

        assertEquals(UMMGranule.class, mfte.getGranule().getClass());
        UMMGranule granule = (UMMGranule) mfte.getGranule();
        assertEquals(-180, granule.getBbxWesternLongitude().intValue());
        assertEquals(90, granule.getBbxNorthernLatitude().intValue());
        assertEquals(180, granule.getBbxEasternLongitude().intValue());
        assertEquals(-90, granule.getBbxSouthernLatitude().intValue());
    }

    @Test
    public void testParseConfigFiles() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readConfiguration(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertEquals("MODIS_T-JPL-L2P-v2014.0", mfte.getDataset().getShortName());
        assertEquals("4.2", mfte.getDataset().getCitationSet().iterator().next().getVersion());
    }

    @Test
    public void testCreateJson()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readConfiguration(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        File file2 = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0");

        JSONObject granule = mfte.createJson();
        System.out.println(granule.toJSONString());
        assertEquals("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0", granule.get("GranuleUR"));
    }

    @Test
    /**
     * use a fake cmr.cfg file
     */
    public void testCreateJsonWithOrbitNumber()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readConfiguration(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        File file2 = new File(classLoader.getResource("RSS_SMAP_SSS_L2C_r00870_20150401T004312_2015091_FNL_V04.0.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("SMAP_RSS_L2_SSS_V4");

        JSONObject granule = mfte.createJson();
        System.out.println(granule.toJSONString());
        JSONArray orbitArray = (JSONArray) granule.get("OrbitCalculatedSpatialDomains");
        Integer orbitNumber = (Integer) ((JSONObject) orbitArray.get(0)).get("OrbitNumber");
        assertEquals(orbitNumber, new Integer(870));
    }

    @Test
    /**
     * use a fake cmr.cfg file
     */
    public void testCreateJsonWithStartEndOrbitNumber()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readConfiguration(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        File file2 = new File(classLoader.getResource("RSS_smap_SSS_L3_monthly_2015_04_FNL_v04.0.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("SMAP_RSS_L3_SSS_SMI_MONTHLY_V4");

        JSONObject granule = mfte.createJson();
        System.out.println(granule.toJSONString());
        JSONArray orbitArray = (JSONArray) granule.get("OrbitCalculatedSpatialDomains");
        Integer beginOrbit = (Integer) ((JSONObject) orbitArray.get(0)).get("BeginOrbitNumber");
        Integer endOrbit = (Integer) ((JSONObject) orbitArray.get(0)).get("EndOrbitNumber");
        assertEquals(beginOrbit, new Integer("870"));
        assertEquals(endOrbit, new Integer("1308"));
    }

    @Test
    public void testCreateJsonWithCyclePass()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        try {
            mfte.readConfiguration(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3.png");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638");

        JSONObject granule = mfte.createJson();
        assertEquals("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638", granule.get("GranuleUR"));

        JSONObject track = (JSONObject) ((JSONObject) ((JSONObject) granule.get("SpatialExtent")).get("HorizontalSpatialDomain")).get("Track");
        assertEquals(new Long(374), track.get("Cycle"));
        assertEquals(new Long(172), ((JSONObject) ((JSONArray) track.get("Passes")).get(0)).get("Pass"));
    }

    @Test
    public void testSwotArchive()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.archive.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();

        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            mfte.readSwotArchiveXmlFile(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402");

        JSONObject granule = mfte.createJson();
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        String jsonStr = gsonBuilder.toJson(granule);
        assertEquals("SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402", granule.get("GranuleUR"));

        JSONObject track = (JSONObject) ((JSONObject) ((JSONObject) granule.get("SpatialExtent")).get("HorizontalSpatialDomain")).get("Track");
        assertEquals(new Long(22), track.get("Cycle"));
        assertEquals(new Long(33), ((JSONObject) ((JSONArray) track.get("Passes")).get(0)).get("Pass"));
    }

    @Test
    public void testCreateTrackTypeWithException () {
        String input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
        ClassLoader classLoader = getClass().getClassLoader();
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        // if passed in UNKNOWN for either cycle or pass, then NumberFormatException will be thrown
        assertThrows(NumberFormatException.class, () -> mfte.createTrackType("UNKNOWN", "UNKNOWN", "UNKNOWN"));
        // if  both cycle and pass string are re-presenting Integer
        TrackType trackType = mfte.createTrackType("3", "2", "UNKNOWN");
        assertEquals(trackType.getCycle().intValue(), 3);
        List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
        TrackPassTileType trackPassTileType = trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass().intValue(), 2);
        assertEquals(trackPassTileType.getTiles().get(0), "UNKNOWN");
    }
    @Test
    public void testMarshellCyclePassTileSceneStrToAchiveType() {
        String input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
        ClassLoader classLoader = getClass().getClassLoader();
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        IsoGranule isoGranule = mfte.createIsoCyclePassTile(input);
        TrackType trackType = isoGranule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer(5));
        List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
        assertEquals(trackPassTileTypes.size(), 3);
        TrackPassTileType trackPassTileType = trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass(), new Integer(40));
        List<String> tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 7);
        assertEquals(tiles.get(0), "4L");
        assertEquals(tiles.get(6), "8R");
        List<AdditionalAttributeType> additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
        assertEquals(additionalAttributeTypes.size(), 3);
        // The following test purposely make tile 40R-50R breaking the code.  However, pass 41, 6R 6L shall
        // still appear in the tiles array
        input = "Cycle: 5 Pass: [40, Tiles: 40R-50R 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7-10F]";
        isoGranule = mfte.createIsoCyclePassTile(input);
        trackType = isoGranule.getTrackType();
        trackPassTileTypes = trackType.getPasses();
        assertEquals(trackPassTileTypes.size(), 3);
        trackPassTileType = trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass(), new Integer(40));
        tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 0);
        trackPassTileType = trackPassTileTypes.get(1); //[41, Tiles: 6R 6L]
        assertEquals(trackPassTileType.getPass(), new Integer(41));
        tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 2);
        assertEquals(tiles.get(0), "6R");
        assertEquals(tiles.get(1), "6L");
        trackPassTileType = trackPassTileTypes.get(2);  // [42, Tiles: 7-10F]
        assertEquals(trackPassTileType.getPass(), new Integer(42));
        tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 4);
        assertEquals(tiles.get(0), "7F");
        assertEquals(tiles.get(3), "10F");
        additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
        assertEquals(additionalAttributeTypes.size(), 2);
        AdditionalAttributeType additionalAttributeType = additionalAttributeTypes.get(1);  // TILE, 7F, 8F, 9F, 10F
        assertEquals(additionalAttributeType.getName(), "TILE");
        List<String> tileValues = additionalAttributeType.getValues();  //7F, 8F, 9F, 10F
        assertEquals(tileValues.get(0), "7F");
        assertEquals(tileValues.get(3), "10F");

    }

    @Test
    public void testReadIsoMendsMetadataFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_L2_HR_PIXCVec_001_113_164R_20160905T002836_20160905T002846_TI0000_01.nc.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
            // Verify the values here:
            TrackType trackType = isoGranule.getTrackType();
            assertEquals(trackType.getCycle(), new Integer(5));
            List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
            assertEquals(trackPassTileTypes.size(), 3);
            TrackPassTileType trackPassTileType = trackPassTileTypes.get(0);
            assertEquals(trackPassTileType.getPass(), new Integer(40));
            List<String> tiles = trackPassTileType.getTiles();
            assertEquals(tiles.size(), 7);
            assertEquals(tiles.get(0), "4L");
            assertEquals(tiles.get(6), "8R");
            List<AdditionalAttributeType> additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
            assertEquals(additionalAttributeTypes.size(), 3);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAll() throws ParseException, IOException, URISyntaxException {

        // Simple "publishAll" is true

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_1.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            // Verify the values here:

            // Confirm additional attributes has been filled
            List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
            assertEquals(aat.size(), 10);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("HlsDataset",
                    "SensorProductID",
                    "Accode",
                    "MeanSunAzimuthAngle",
                    "MeanSunZenithAngle",
                    "NBAR_SolarZenith",
                    "MeanViewAzimuthAngle",
                    "MeanViewZenithAngle",
                    "SpatialCoverage",
                    "PercentCloudCover");

            if(!checkForKey.equals(keys)){
                fail(String.format("List mismatch:\n" +
                        Arrays.toString(keys.toArray()) + "\n" +
                        Arrays.toString(checkForKey.toArray())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_appendFieldToJSON() throws ParseException, IOException, URISyntaxException {

        // publish all is set to true and having a dedicated field added to JSON (CloudCover (JSON) mapped to PercentCloudCover (XML))

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_2.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            // Verify the values here:

            // Confirm additional attributes has been filled
            List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
            assertEquals(aat.size(), 10);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("HlsDataset",
                    "SensorProductID",
                    "Accode",
                    "MeanSunAzimuthAngle",
                    "MeanSunZenithAngle",
                    "NBAR_SolarZenith",
                    "MeanViewAzimuthAngle",
                    "MeanViewZenithAngle",
                    "SpatialCoverage",
                    "PercentCloudCover");

            if(!checkForKey.equals(keys)){
                fail(String.format("List mismatch:\n" +
                        Arrays.toString(keys.toArray()) + "\n" +
                        Arrays.toString(checkForKey.toArray())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("some_random_granule_name");

        JSONObject granule = mfte.createJson();
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        String jsonStr = gsonBuilder.toJson(granule);

        // Ensure jsonStr has cloud coverage field (value from PercentCloudCover in iso.xml)

        JSONParser parser = new JSONParser();
        JSONObject resultJSON = (JSONObject) parser.parse(jsonStr);

        try {
            if (resultJSON.containsKey("CloudCover")) {
                long value = (long) resultJSON.get("CloudCover");
                assertEquals(76, value);
            } else {
                fail("Cloud Cover key missing from jsonStr");
            }
        }catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishSpecific() throws ParseException, IOException, URISyntaxException {

        // PublishAll is false and publish list is filled in

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_3.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            // Verify the values here:

            // Confirm additional attributes has been filled
            List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
            assertEquals(aat.size(), 1);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("PercentCloudCover");

            if(!checkForKey.equals(keys)){
                fail(String.format("List mismatch:\n" +
                        Arrays.toString(keys.toArray()) + "\n" +
                        Arrays.toString(checkForKey.toArray())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAllWithSpecific() throws ParseException, IOException, URISyntaxException {

        // This case is when publishAll is True but somehow a publish list is set also; just publish all then

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_4.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            // Verify the values here:

            // Confirm additional attributes has been filled
            List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
            assertEquals(aat.size(), 10);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("HlsDataset",
                    "SensorProductID",
                    "Accode",
                    "MeanSunAzimuthAngle",
                    "MeanSunZenithAngle",
                    "NBAR_SolarZenith",
                    "MeanViewAzimuthAngle",
                    "MeanViewZenithAngle",
                    "SpatialCoverage",
                    "PercentCloudCover");

            if(!checkForKey.equals(keys)){
                fail(String.format("List mismatch:\n" +
                        Arrays.toString(keys.toArray()) + "\n" +
                        Arrays.toString(checkForKey.toArray())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAllEmptyCatchError() throws ParseException, IOException, URISyntaxException {

        // This case is when publishAll key doesn't exist, should throw exception

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_5.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
            fail("Exception was not raised...?");
        } catch (Exception e) {
            // Expected catch
            assertEquals("publishAll key is missing from additionalAttribute", e.getMessage());
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAllEmptyCatchError_2() throws ParseException, IOException, URISyntaxException {

        // This case is when publishAll key doesn't exist, should throw exception

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_6.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
            fail("Exception was not raised...?");
        } catch (Exception e) {
            // Expected catch
            assertEquals("publishAll key is empty or null from additionalAttribute", e.getMessage());
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAllEmptyCatchError_3() throws ParseException, IOException, URISyntaxException {

        // This case is when publishAll key doesn't exist, should throw exception

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_7.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

        } catch (Exception e) {
            fail("Some issue when creating mfte");
        }

        try{
            mfte.getGranule().setName("some_random_granule_name");

            JSONObject granule = mfte.createJson();
            Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                    .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                    .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                    .create();
            String jsonStr = gsonBuilder.toJson(granule);
        } catch (Exception e){
            fail("Issue when generating JSON");
        }
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_appendFieldToJSON_String() throws ParseException, IOException, URISyntaxException {

        // publish all is set to true and having a dedicated field added to JSON (CloudCover (JSON) mapped to PercentCloudCover (XML))

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_8.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            doc = mfte.makeDoc(file.getAbsolutePath());
            xpath = mfte.makeXpath(doc);
            IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

            File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
            try {
                mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

            // Verify the values here:

            // Confirm additional attributes has been filled
            List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
            assertEquals(aat.size(), 1);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("SensorProductID");

            if(!checkForKey.equals(keys)){
                fail(String.format("List mismatch:\n" +
                        Arrays.toString(keys.toArray()) + "\n" +
                        Arrays.toString(checkForKey.toArray())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("some_random_granule_name");

        JSONObject granule = mfte.createJson();
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        String jsonStr = gsonBuilder.toJson(granule);

        // Ensure jsonStr has cloud coverage field (value from PercentCloudCover in iso.xml)

        JSONParser parser = new JSONParser();
        JSONObject resultJSON = (JSONObject) parser.parse(jsonStr);

        try {
            if (resultJSON.containsKey("SensorProductID_CMR")) {
                String value = (String) resultJSON.get("SensorProductID_CMR");
                assertEquals("LC08_L1TP_027038_20210906_20210915_02_T1; LC08_L1TP_027039_20210906_20210915_02_T1", value);
            } else {
                fail("SensorProductID_CMR key missing from jsonStr");
            }
        }catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCreateJsonWithPNGFile()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        mfte.readConfiguration(file.getAbsolutePath());
        File file2 = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.mp").getFile());
        mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3.png");

        mfte.getGranule().setName("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0");

        JSONObject granule = mfte.createJson();
        System.out.println(granule.toJSONString());
        assertEquals("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0", granule.get("GranuleUR"));

        JSONObject temp = (JSONObject) ((JSONArray) granule.get("RelatedUrls")).get(0);
        String mimeType = (String) temp.get("MimeType");
        String subType = (String) temp.get("Subtype");

        assertEquals("image/png", mimeType);
        assertEquals("DIRECT DOWNLOAD", subType);

    }
    
    @Test
    public void testCreateJsonSwotCalVal() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOTCalVal_WM_ADCP_L0_RiverRay1_20220727T191701_20220727T192858_20220920T142800.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();
        
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readSwotCalValXmlFile(file.getAbsolutePath());
        
        mfte.getGranule().setName("SWOTCalVal_WM_ADCP_L0_RiverRay1_20220727T191701_20220727T192858_20220920T142800");
        
        JSONObject granule = mfte.createJson();
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        String jsonStr = gsonBuilder.toJson(granule);
        assertEquals("SWOTCalVal_WM_ADCP_L0_RiverRay1_20220727T191701_20220727T192858_20220920T142800", granule.get("GranuleUR"));

        JSONObject temporal = (JSONObject)((JSONObject) granule.get("TemporalExtent")).get("RangeDateTime");
        String productionTime = ((JSONObject)granule.get("DataGranule")).get("ProductionDateTime").toString();
        
        assertEquals("2022-07-27T19:28:58.000Z", temporal.get("EndingDateTime").toString());
        assertEquals("2022-07-27T19:17:01.000Z", temporal.get("BeginningDateTime").toString());
        assertEquals("2022-09-20T14:28:00.000Z", productionTime);
        
        JSONObject bbox = (JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("BoundingRectangles")).get(0);
        
        
        double west = Double.parseDouble(bbox.get("WestBoundingCoordinate").toString());
        double east = Double.parseDouble(bbox.get("EastBoundingCoordinate").toString());
        double south = Double.parseDouble(bbox.get("SouthBoundingCoordinate").toString());
        double north = Double.parseDouble(bbox.get("NorthBoundingCoordinate").toString());
        
        assert -123.304 == west;
        assert -123.029 == east;
        assert 44.506 == south;
        assert 44.697 == north;
    }
}
