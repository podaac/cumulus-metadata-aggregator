package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import javax.xml.xpath.XPath;
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

}
