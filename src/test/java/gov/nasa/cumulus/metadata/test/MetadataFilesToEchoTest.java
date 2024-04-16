package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nasa.cumulus.metadata.aggregator.*;

import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;

import gov.nasa.podaac.inventory.model.GranuleCharacter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.xml.sax.SAXException;
import org.mockito.Mockito;
import org.w3c.dom.Document;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.List;
import java.util.Map;


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

        mfte.setDatasetValues("MODIS_T-JPL-L2P-v2019.0", "2019.0", false, (JSONObject) boundingBox.get("boundingBox"), null);
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

        mfte.readConfiguration(file.getAbsolutePath());


        File file2 = new File(classLoader.getResource("RSS_smap_SSS_L3_monthly_2015_04_FNL_v04.0.nc.mp").getFile());
        mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");


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

        mfte.readConfiguration(file.getAbsolutePath());


        File file2 = new File(classLoader.getResource("JA1_GPN_2PeP374_172_20120303_112035_20120303_121638.nc.mp").getFile());
        try {
            mfte.readCommonMetadataFile(file2.getAbsolutePath(), "s3://a/path/to/s3");
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
            throws ParseException, IOException, URISyntaxException, ParserConfigurationException, SAXException,
            XPathExpressionException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.archive.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();


        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readSwotArchiveXmlFile(file.getAbsolutePath());


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
    public void testCreateTrackTypeWithException () throws IOException, ParseException{
        String input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
        ClassLoader classLoader = getClass().getClassLoader();
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        mfte.readConfiguration(cfgFile.getAbsolutePath());

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
    public void testMarshellCyclePassTileSceneStrToAchiveType() throws IOException, ParseException{
        String input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
        ClassLoader classLoader = getClass().getClassLoader();
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        mfte.readConfiguration(cfgFile.getAbsolutePath());

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
        /**
         * Following section test empty pass. Ex. Cycle: 001, Pass: []
         */
        input = "Cycle: 001, Pass: []";
        isoGranule = mfte.createIsoCyclePassTile(input);
        trackType = isoGranule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer("1"));

        /**
         * Cycle: 483 Pass: [10, Tiles: 72-84R 111-111R 72-84L 110-111L]
         */
        input = "Cycle: 483 Pass: [10, Tiles: 72-84R 111-111R 72-84L 110-111L]";
        isoGranule = mfte.createIsoCyclePassTile(input);
        trackType = isoGranule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer("483"));

        List<TrackPassTileType> passes = trackType.getPasses();
        tiles = passes.get(0).getTiles();
        assertEquals(tiles.size(), 29);
        assertEquals(tiles.get(0), "72R");
        assertEquals(tiles.get(28), "111L");

        input = "Cycle: 406, Pass: [40, Tiles: 4-5L 4-5R] [41, Tiles: 6R 6L], BasinID: 123";
        isoGranule = mfte.createIsoCyclePassTile(input);
        trackType = isoGranule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer("406"));

        passes = trackType.getPasses();
        tiles = passes.get(0).getTiles();
        assertEquals(tiles.size(), 4);
        assertEquals(tiles.get(0), "4L");
        assertEquals(tiles.get(1), "5L");
        tiles = passes.get(1).getTiles();
        assertEquals(tiles.size(), 2);
        assertEquals(tiles.get(0), "6R");
        assertEquals(tiles.get(1), "6L");
        additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
        additionalAttributeType = additionalAttributeTypes.get(2);
        assertEquals(additionalAttributeType.getName(), "BasinID");
        List<String> basinIdStrs = additionalAttributeType.getValues();
        assertEquals(basinIdStrs.get(0), "123");
    }

    @Test
    public void testMarshellCyclePassTileSceneStrToAchiveTypeWithNull() throws IOException, ParseException {
        String input = "Cycle:  Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
        ClassLoader classLoader = getClass().getClassLoader();
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        mfte.readConfiguration(cfgFile.getAbsolutePath());
        try {
            IsoGranule isoGranule = mfte.createIsoCyclePassTile(input);
        } catch (NumberFormatException nfe) {
            assertEquals(nfe.getMessage(), "Zero length string");
        }

        try {
            input = "Cycle:22  Pass: [ Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
            IsoGranule isoGranule = mfte.createIsoCyclePassTile(input);
        } catch (NumberFormatException nfe) {
            assertTrue(true);
        }

    }
    @Test
    public void testReadIsoMendsMetadataFile() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_L2_HR_PIXCVec_001_113_164R_20160905T002836_20160905T002846_TI0000_01.nc.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;

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

        /**
         * Test the behavior of reading SWOT ISO MENDS Orbit and Footprint
         */
        file = new File(classLoader.getResource("SWOT_L2_HR_RiverAvg_487_SI_35_20230410T200018_20230411T195056_TGB0_01.zip.iso.xml").getFile());
        cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        mfte = new MetadataFilesToEcho(true);


        mfte.readConfiguration(cfgFile.getAbsolutePath());
        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
        isoGranule.getOrbit();
        Set<GranuleCharacter> granuleCharacters = isoGranule.getGranuleCharacterSet();
        for (GranuleCharacter granuleCharacter : granuleCharacters) {
            if (granuleCharacter.getDatasetElement().getElementDD().getShortName().equals("line")) {
                assertTrue(StringUtils.equals("46.7666666666667 151.802777777778 51.353523932563 179.39615512424 51.3618572658963 179.44615512424 51.3673094007704 179.460468207465 51.3720831976997 179.470818074544 51.9544606526693 179.77399359809 51.962745836046 179.775655449761 65.0256 180.0 65.0243570963542 -179.993114725749 64.2422505696615 -173.124080403646 64.2416666666667 -173.0875 64.2589111328125 -172.942587619358 64.3993570963542 -172.234684583876 66.0076904296875 -169.718114556207 66.0260301378038 -169.70074496799 66.0760314941406 -169.659073554145 66.0902187771267 -169.657690429687 66.1322906494141 -169.675703599718 66.1409630669488 -169.684376017253 71.3826697455512 -175.542419433594 71.4159271240235 -175.726031833225 71.4173094007704 -175.740315416124 71.5993445502387 -178.950753445095 71.6086161295573 -179.125728691949 71.6076221042209 -179.174432712131 71.6005043877496 -179.364869689941 71.5840138753255 -179.63235405816 71.5756805419922 -179.756760321723 71.5339 180.0 71.5409488254123 179.982556491428 76.1909840901693 152.824263509115 76.7576266818576 149.457624986437 76.7590138753255 149.384906344944 76.2006429036458 138.826448059082 75.8756427341037 135.72644788954 75.8408372667101 135.68353644477 71.075 130.025 69.1791666666667 128.695833333333 69.1199666341146 128.666011216905 67.6083333333333 128.1375 67.59375 128.133802117242 66.4433797200521 128.049646674262 66.4350755479601 128.050353325738 66.4208333333333 128.054166666667 65.9953955756294 128.247048102485 55.5633509318034 135.546684095595 55.5125 135.604166666667 46.7844919840495 151.737613932292 46.7714508056641 151.764506530762 46.7672841389974 151.781173197428 46.7666666666667 151.802777777778",
                        StringUtils.trim(granuleCharacter.getValue())));
            }
        }
    }


    @Test
    public void testReadIsoMendsMetadataFile_Pass_Cycle_LeadingZeros() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_L1B_LR_INTF_407_024_20230122T040505_20230122T045618_PIA0_01.nc.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);
        // Verify the values here:
        TrackType trackType = isoGranule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer(407));
        List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
        assertEquals(trackPassTileTypes.size(), 1);
        TrackPassTileType trackPassTileType = trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass(), new Integer(24));
        List<String> tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 0);
        List<AdditionalAttributeType> additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
        assertEquals(additionalAttributeTypes.size(), 0);
    }
    
        @Test
    public void testReadSwotArchiveMetadataFile_Pass_Cycle_LeadingZeros() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_INT_KCAL_Dyn_403_008_20230117T150452_20230117T155629_PIA0_01.archive.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        mfte.readSwotArchiveXmlFile(file.getAbsolutePath());
        UMMGranule granule = (UMMGranule) mfte.getGranule();
        // Verify the values here:
        TrackType trackType = granule.getTrackType();
        assertEquals(trackType.getCycle(), new Integer(403));
        List<TrackPassTileType> trackPassTileTypes = trackType.getPasses();
        assertEquals(trackPassTileTypes.size(), 1);
        TrackPassTileType trackPassTileType = trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass(), new Integer(8));
        List<String> tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 1);
        List<AdditionalAttributeType> additionalAttributeTypes = granule.getAdditionalAttributeTypes();
        assertEquals(additionalAttributeTypes.size(), 1);
    }

    @Test
    public void testReadIsoMendsMetadataFileAdditionalFields_publishAll() throws ParseException, IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException {

        // Simple "publishAll" is true

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("OPERA_L3_DSWX-HLS_PROVISIONAL_V0_test_1.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;

        mfte.readConfiguration(cfgFile.getAbsolutePath());
        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

        // Verify the values here:

        // Confirm additional attributes has been filled
        List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
        assertEquals(aat.size(), 11);

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
                "PercentCloudCover",
                "MGRS_TILE_ID"
        );

        if(!checkForKey.equals(keys)){
            fail(String.format("List mismatch:\n" +
                    Arrays.toString(keys.toArray()) + "\n" +
                    Arrays.toString(checkForKey.toArray())));
        }
    }
    
    /**
     * Test that confirms MGRS_TILE_ID additional attribute is present even when no other additional attributes
     * are present
     */
    @Test
    public void testReadIsoMendsMetadataFileNoAdditionalFieldsMGRS() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParseException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("OPERA_L3_DSWx_HLS_T14RNV_20210906T170251Z_20221026T184342Z_L8_30_v0.0.iso.xml").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true);

        Document doc = null;
        XPath xpath = null;

        doc = mfte.makeDoc(file.getAbsolutePath());
        xpath = mfte.makeXpath(doc);
        IsoGranule isoGranule = mfte.readIsoMendsMetadataFile("s3://mybucket/mygranule.nc",  doc,  xpath);

        // Confirm additional attribute has been filled
        List<AdditionalAttributeType> aat = isoGranule.getAdditionalAttributeTypes();
        assertEquals(aat.size(), 1);
        assertEquals(aat.get(0).getName(), "MGRS_TILE_ID");
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
            assertEquals(aat.size(), 11);  // 10 additional attributes + MGRS_TILE_ID

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
                    "PercentCloudCover",
                    "MGRS_TILE_ID"
            );

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
            assertEquals(aat.size(), 2);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("PercentCloudCover", "MGRS_TILE_ID");

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
            assertEquals(aat.size(), 11);

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
                    "PercentCloudCover",
                    "MGRS_TILE_ID"
            );

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
            assertEquals(aat.size(), 2);

            List<String> keys = aat.stream().map(AdditionalAttributeType::getName).collect(Collectors.toList());

            List<String> checkForKey = Arrays.asList("SensorProductID", "MGRS_TILE_ID");

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
        // load pre-saved file and compare with generated granule JSONObject
        // SWOTCalVal_WM_ADCP_L0_RiverRay1_20220727T191701_20220727T192858_20220920T142800_swotCalVal_ummg.json
        // load pre-saved file and perform json comparison
        assertTrue(compareFileWithGranuleJson("ummgResults/swotCalVal/SWOTCalVal_WM_ADCP_L0_RiverRay1_20220727T191701_20220727T192858_20220920T142800_swotCalVal_ummg.json", granule));
    }

    public boolean compareFileWithGranuleJson(String filePath, JSONObject granuleJson) throws
            IOException, ParseException{
        ClassLoader classLoader = getClass().getClassLoader();
        File preSavedJsonFile = new File(classLoader.getResource(filePath).getFile());
        String readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JSONObject readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
        // remove ProviderDates structure because it always has most current datetime
        // the ProviderDates saved in file is different than the provider dates generated on the fly
        granuleJson.remove("ProviderDates");
        readInJsonObj.remove("ProviderDates");
        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granuleJson.toJSONString()));
        return true;  // if reached this point, return true
    }
    @Test
    public void testSWOTCreateJsonSWOTIsoXMLSpatialType() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException, URISyntaxException {
        // Create isoXmlSpatial Hashtable which contains footprint, orbit then passed the hashtable to MetadataFilesToEcho contructor
        MetadataAggregatorLambda lambda = new MetadataAggregatorLambda();
        org.json.simple.JSONArray array = new JSONArray();
        array.add("footprint");
        array.add("orbit");
        array.add("bbox");

        //HashSet<MENDsIsoXMLSpatialTypeEnum> h = lambda.createIsoXMLSpatialTypeSet("[footprint,orbit]");
        HashSet<MENDsIsoXMLSpatialTypeEnum> h = lambda.createIsoXMLSpatialTypeSet(array);
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("swotIsoXMLSpatialTypeTestData/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01.nc.iso.xml").getFile());

        // constructor to set isIso =true and the isoXmlSpatialHashtable
        MetadataFilesToEcho mfte = new MetadataFilesToEcho(true, h);
        mfte.getGranule().setName("SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01");
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readIsoMetadataFile(file.getAbsolutePath(), "s3://fake_bucket/fake_dir/fake.nc.iso.xml");
        //////  Star Mocking code of isSpatialValid
        UMMGranuleFile mockedUMMGranuleFile = Mockito.mock(UMMGranuleFile.class);
        Mockito.spy(mockedUMMGranuleFile);
        Mockito.doReturn(true)
                .when(mockedUMMGranuleFile)
                .isSpatialValid(any());
        CMRLambdaRestClient mockedEchoLambdaRestClient= Mockito.mock(CMRLambdaRestClient.class);
        Mockito.doReturn(true)
                .when(mockedEchoLambdaRestClient)
                .isUMMGSpatialValid(any(), any(), any());

        MockedStatic<CMRRestClientProvider> mockedECHORestClientProvider = mockStatic(CMRRestClientProvider.class);
        when(CMRRestClientProvider.getLambdaRestClient()).thenReturn(mockedEchoLambdaRestClient);
        //////  END Mocking code of isSpatialValid

        JSONObject granule = mfte.createJson();
        JSONArray polygonPoints =((JSONArray)((JSONObject) ((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("GPolygons")).get(0)).get("Boundary")).get("Points"));
        Double latitude=(Double)((JSONObject)polygonPoints.get(0)).get("Latitude");
        Double longitude=(Double)((JSONObject)polygonPoints.get(0)).get("Longitude");
        assert latitude.doubleValue() ==-77.089598228;
        assert longitude.doubleValue() == -121.56652283899999;

        latitude=(Double)((JSONObject)polygonPoints.get(27)).get("Latitude");
        longitude=(Double)((JSONObject)polygonPoints.get(27)).get("Longitude");
        assert latitude.doubleValue()==56.734470077;
        assert longitude.doubleValue()==-21.668558564000023;

        latitude=(Double)((JSONObject)polygonPoints.get(64)).get("Latitude");
        longitude=(Double)((JSONObject)polygonPoints.get(64)).get("Longitude");
        assert latitude.doubleValue()==-77.089598228;
        assert longitude.doubleValue()== -121.56652283899999;

        JSONObject orbit = ((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Orbit"));
        assert ((Double)orbit.get("StartLatitude"))==-77.66;  //-77.66
        assert ((Double)orbit.get("EndLatitude"))==77.66;// -> {Double@5567} 77.66
        assert ((Double)orbit.get("AscendingCrossing"))==-38.05; //-> {Double@5569} -38.05
        assertTrue(StringUtils.equals((String)orbit.get("StartDirection"), "A")); //-> A
        assertTrue(StringUtils.equals((String)orbit.get("EndDirection"), "A")); //EndDirection -> A

        JSONObject bbox = (JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("BoundingRectangles")).get(0);
        assert ((BigDecimal)bbox.get("WestBoundingCoordinate")).compareTo(new BigDecimal("-121.76947499999999990905052982270717620849609375")) ==0;
        assert ((BigDecimal)bbox.get("SouthBoundingCoordinate")).compareTo(new BigDecimal("-78.271941999999995687176124192774295806884765625"))==0;
        assert ((BigDecimal)bbox.get("EastBoundingCoordinate")).compareTo(new BigDecimal("45.675058000000035463017411530017852783203125"))==0;
        assert ((BigDecimal)bbox.get("NorthBoundingCoordinate")).compareTo(new BigDecimal("78.272067999999990206561051309108734130859375"))==0;


        // verify the pre-saved ummg.json file is equal to the granule json just built
        assertTrue(compareFileWithGranuleJson("ummgResults/swotIsoXMLSpatialType/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01_footprintOrbitBBox.json", granule));
//        File preSavedJsonFile = new File(classLoader.getResource("ummgResults/swotIsoXMLSpatialType/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01_footprintOrbitBBox.json").getFile());
//        String readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
//
//        JSONParser parser = new JSONParser();
//        JSONObject readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
//        // remove ProviderDates structure because it always has most current datetime
//        // the ProviderDates saved in file is different than the provider dates generated on the fly
//        granule.remove("ProviderDates");
//        readInJsonObj.remove("ProviderDates");
//        // Use jackson ObjectMapper to convert string to JSONObject for comparison.
//        // the org.simple.json and google gson library does not work well with comparison when some field has NUMBER type
//        ObjectMapper mapper = new ObjectMapper();
//        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granule.toJSONString()));
        /**
         * Test isoXMLSpatial:[footprint]
         */
        clearVariables4IsoXMLSpatialTest(array, h, granule);  // clear variables first
        array.add("footprint");
        h = lambda.createIsoXMLSpatialTypeSet(array);
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));
        mfte = new MetadataFilesToEcho(true, h);
        mfte.getGranule().setName("SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01");
        cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readIsoMetadataFile(file.getAbsolutePath(), "s3://fake_bucket/fake_dir/fake.nc.iso.xml");
        granule = mfte.createJson();
        polygonPoints =((JSONArray)((JSONObject) ((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("GPolygons")).get(0)).get("Boundary")).get("Points"));
        latitude=(Double)((JSONObject)polygonPoints.get(0)).get("Latitude");
        longitude=(Double)((JSONObject)polygonPoints.get(0)).get("Longitude");
        assert latitude.doubleValue() ==-77.089598228;
        assert longitude.doubleValue() == -121.56652283899999;

        latitude=(Double)((JSONObject)polygonPoints.get(27)).get("Latitude");
        longitude=(Double)((JSONObject)polygonPoints.get(27)).get("Longitude");
        assert latitude.doubleValue()==56.734470077;
        assert longitude.doubleValue()==-21.668558564000023;

        latitude=(Double)((JSONObject)polygonPoints.get(64)).get("Latitude");
        longitude=(Double)((JSONObject)polygonPoints.get(64)).get("Longitude");
        assert latitude.doubleValue()==-77.089598228;
        assert longitude.doubleValue()== -121.56652283899999;

        orbit = ((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Orbit"));
        assert orbit==null;

        bbox = (JSONObject) (((JSONObject)((JSONObject)((JSONObject) granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("BoundingRectangles"));
        assert bbox == null;
        // load pre-saved file and perform json comparison
        preSavedJsonFile = new File(classLoader.getResource("ummgResults/swotIsoXMLSpatialType/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01_footprint.json").getFile());
        readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
        readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
        // remove ProviderDates structure because it always has most current datetime
        // the ProviderDates saved in file is different than the provider dates generated on the fly
        granule.remove("ProviderDates");
        readInJsonObj.remove("ProviderDates");
        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granule.toJSONString()));

        /**
         * Test isoXMLSpatial:[bbox]
         */
        clearVariables4IsoXMLSpatialTest(array, h, granule);  // clear variables first
        array.add("bbox");
        h = lambda.createIsoXMLSpatialTypeSet(array);
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));
        mfte = new MetadataFilesToEcho(true, h);
        mfte.getGranule().setName("SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01");
        cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readIsoMetadataFile(file.getAbsolutePath(), "s3://fake_bucket/fake_dir/fake.nc.iso.xml");
        granule = mfte.createJson();
        JSONArray GPolygons = (JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("GPolygons");
        assert GPolygons ==null;

        orbit = ((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Orbit"));
        assert orbit==null;

        bbox = (JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"))
                .get("BoundingRectangles")).get(0);
        assert ((BigDecimal)bbox.get("WestBoundingCoordinate")).compareTo(new BigDecimal("-121.76947499999999990905052982270717620849609375")) ==0;
        assert ((BigDecimal)bbox.get("SouthBoundingCoordinate")).compareTo(new BigDecimal("-78.271941999999995687176124192774295806884765625"))==0;
        assert ((BigDecimal)bbox.get("EastBoundingCoordinate")).compareTo(new BigDecimal("45.675058000000035463017411530017852783203125"))==0;
        assert ((BigDecimal)bbox.get("NorthBoundingCoordinate")).compareTo(new BigDecimal("78.272067999999990206561051309108734130859375"))==0;
        // load pre-saved file and perform json comparison
        preSavedJsonFile = new File(classLoader.getResource("ummgResults/swotIsoXMLSpatialType/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01_bbox.json").getFile());
        readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
        readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
        // remove ProviderDates structure because it always has most current datetime
        // the ProviderDates saved in file is different than the provider dates generated on the fly
        granule.remove("ProviderDates");
        readInJsonObj.remove("ProviderDates");
        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granule.toJSONString()));
        /**
         * Test isoXMLSpatial:[orbit]
         */
        clearVariables4IsoXMLSpatialTest(array, h, granule);  // clear variables first
        array.add("orbit");
        h = lambda.createIsoXMLSpatialTypeSet(array);
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.FOOTPRINT));
        assertTrue(h.contains(MENDsIsoXMLSpatialTypeEnum.ORBIT));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.BBOX));
        assertFalse(h.contains(MENDsIsoXMLSpatialTypeEnum.NONE));
        mfte = new MetadataFilesToEcho(true, h);
        mfte.getGranule().setName("SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01");
        cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        mfte.readConfiguration(cfgFile.getAbsolutePath());
        mfte.readIsoMetadataFile(file.getAbsolutePath(), "s3://fake_bucket/fake_dir/fake.nc.iso.xml");
        granule = mfte.createJson();
        JSONObject geometry = ((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry"));
        assert geometry ==null;

        orbit = ((JSONObject)((JSONObject)((JSONObject)granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Orbit"));
        assert ((Double)orbit.get("StartLatitude"))==-77.66;  //-77.66
        assert ((Double)orbit.get("EndLatitude"))==77.66;// -> {Double@5567} 77.66
        assert ((Double)orbit.get("AscendingCrossing"))==-38.05; //-> {Double@5569} -38.05
        assertTrue(StringUtils.equals((String)orbit.get("StartDirection"), "A")); //-> A
        assertTrue(StringUtils.equals((String)orbit.get("EndDirection"), "A")); //EndDirection -> A
        // No Geometry object since there is neither footprint nor bbox
        bbox = (((JSONObject)((JSONObject)((JSONObject) granule
                .get("SpatialExtent"))
                .get("HorizontalSpatialDomain"))
                .get("Geometry")));
        assert bbox == null;
        // load pre-saved file and perform json comparison
        preSavedJsonFile = new File(classLoader.getResource("ummgResults/swotIsoXMLSpatialType/SWOT_L2_LR_SSH_Basic_006_143_20231107T150730_20231107T155607_PIB0_01_orbit.json").getFile());
        readInJsonStr = FileUtils.readFileToString(preSavedJsonFile, StandardCharsets.UTF_8);
        readInJsonObj = (JSONObject) parser.parse(readInJsonStr);
        // remove ProviderDates structure because it always has most current datetime
        // the ProviderDates saved in file is different than the provider dates generated on the fly
        granule.remove("ProviderDates");
        readInJsonObj.remove("ProviderDates");
        assertEquals(mapper.readTree(readInJsonObj.toJSONString()), mapper.readTree(granule.toJSONString()));
    }
    @Test
    public void test_Example() throws IOException, ParseException {
        // Create a sample JSONObject
        JSONObject obj = new JSONObject();
        obj.put("name", "John Doe");
        obj.put("age", 30);
        obj.put("city", "New York");

        // Write JSONObject to f

        FileUtils.writeStringToFile(new File("/tmp/tmp.json"), obj.toJSONString(), StandardCharsets.UTF_8);

        // Read JSONObject from file
        String readInStr = FileUtils.readFileToString(new File("/tmp/tmp.json"), StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        JSONObject loadedObj = (JSONObject) parser.parse(readInStr);

        // Compare original and loaded JSONObject
        assert obj.hashCode() == loadedObj.hashCode();
        boolean isEqual = obj.equals(loadedObj);
        System.out.println("Objects are equal? " + isEqual);
        compareJson(obj, loadedObj);
    }

    public void compareJson(JSONObject obj1, JSONObject obj2) {
        // Check for different keys
        for (Object key : obj1.keySet()) {
            if (!obj2.containsKey(key)) {
                System.out.println("Unequal field: " + key + " (missing in object2)");
            }
        }

        for (Object key : obj2.keySet()) {
            if (!obj1.containsKey(key)) {
                System.out.println("Unequal field: " + key + " (missing in object1)");
            }
        }

        // Compare values for existing keys
        for (Object key : obj1.keySet()) {
            Object value1 = obj1.get(key);
            Object value2 = obj2.get(key);

            if (value1 instanceof JSONObject && value2 instanceof JSONObject) {
                // Recursive comparison for nested objects
                compareJson((JSONObject) value1, (JSONObject) value2);
            } else if (value1 instanceof JSONArray && value2 instanceof JSONArray) {
                // Handle JSONArray comparison if needed (implement your logic)
            } else if (!value1.equals(value2)) {
                System.out.println("Unequal field: " + key + ", value1: " + value1 + ", value2: " + value2);
            }
        }
    }

    private void clearVariables4IsoXMLSpatialTest(JSONArray isoXMLSpatialArray, HashSet isoXMLSpatialHashSet, JSONObject granule) {
        isoXMLSpatialArray.clear();
        isoXMLSpatialHashSet.clear();
        granule.clear();
    }
}