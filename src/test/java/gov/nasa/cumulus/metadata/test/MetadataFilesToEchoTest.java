package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nasa.cumulus.metadata.aggregator.*;

import gov.nasa.cumulus.metadata.aggregator.factory.UmmgPojoFactory;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import org.apache.commons.collections.iterators.ObjectArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import sun.java2d.jules.JulesRenderingEngine;

import javax.sound.midi.Track;

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
    public void testReadSwotIsoXmlFile()
            throws ParseException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("SWOT_L2_HR_PIXCVec_001_113_164R_20160905T002836_20160905T002846_TI0000_01.nc.iso.xml").getFile());
        File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
        MetadataFilesToEcho mfte = new MetadataFilesToEcho();

        try {
            mfte.readConfiguration(cfgFile.getAbsolutePath());
            mfte.readSwotIsoXmlFile(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        mfte.getGranule().setName("SWOT_L2_HR_PIXCVec_001_113_164R_20160905T002836_20160905T002846_TI0000_01");

        JSONObject granule = mfte.createJson();
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();
        String jsonStr = gsonBuilder.toJson(granule);
        assertEquals("SWOT_L2_HR_PIXCVec_001_113_164R_20160905T002836_20160905T002846_TI0000_01", granule.get("GranuleUR"));

        JSONObject track = (JSONObject) ((JSONObject) ((JSONObject) granule.get("SpatialExtent")).get("HorizontalSpatialDomain")).get("Track");
        assertEquals(new Long(22), track.get("Cycle"));
        assertEquals(new Long(33), ((JSONObject) ((JSONArray) track.get("Passes")).get(0)).get("Pass"));
    }

    @Test
    public void testMarshellCyclePassTileSceneStrToAchiveType1() {
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
        TrackPassTileType trackPassTileType=trackPassTileTypes.get(0);
        assertEquals(trackPassTileType.getPass(), new Integer(40));
        List<String> tiles = trackPassTileType.getTiles();
        assertEquals(tiles.size(), 7);
        assertEquals(tiles.get(0), "4L");
        assertEquals(tiles.get(6), "8R");
        List<AdditionalAttributeType> additionalAttributeTypes = isoGranule.getAdditionalAttributeTypes();
        assertEquals(additionalAttributeTypes.size(), 3);
    }

	@Test
	public void testMarshellCyclePassTileSceneStrToAchiveType2() {
        String input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-8R] [41, Tiles: 6R 6L] [42, Tiles: 7F]";
//        input = "Cycle: 5 Pass: [40, Tiles: 4-5L 4-5R] [41, Tiles: 6R 6L] [42, Tiles: 7F] Cycle: 6 Pass: [50, Tiles: 4-5L 4-5R] [51, Tiles: 6R 6L] [52, Tiles: 7F]";
        String toBeProcessedStr = StringUtils.upperCase(StringUtils.trim(input));
        Pattern p_cycle = Pattern.compile("CYCLE\\s*:\\s*\\d+\\s*?");
        Matcher m_cycle = p_cycle.matcher(toBeProcessedStr);
        String cycleStr = "";
        ArrayList<String> cycleStrs = new ArrayList<>();
        while(m_cycle.find()) {
            cycleStr = m_cycle.group();
            System.out.println(cycleStr);
            cycleStrs.add(cycleStr.trim());
        }
        int numberOfCycles = cycleStrs.size();
        int i = 0;
        // Current UMMG schema 1.6.3 supports only one cycle. Although it is possible that
        //  one granule contains 2 cycles as the edge case.
        ArrayList<String> cyclePassStrs = new ArrayList<>();
        while(i +1 < numberOfCycles) {
            String cyclePassStr = StringUtils.substring(toBeProcessedStr,
                    StringUtils.indexOf(toBeProcessedStr, cycleStrs.get(i)),
                    StringUtils.indexOf(toBeProcessedStr, cycleStrs.get(i+1)));
            System.out.println("cyclePass processing candidat:" + cyclePassStr);
            cyclePassStrs.add(cyclePassStr);
            toBeProcessedStr = StringUtils.replace(toBeProcessedStr, cyclePassStr, "");
            i++;
        }
        toBeProcessedStr = StringUtils.trim(toBeProcessedStr);
        // Processed the last Cycle OR if there is only one cycle, we are processing the first==last cycle here
        if(StringUtils.isNotEmpty(toBeProcessedStr) && StringUtils.startsWithIgnoreCase(toBeProcessedStr, "CYCLE")) {
            System.out.println("Processing the last or first cycle" + toBeProcessedStr);
            cyclePassStrs.add(toBeProcessedStr);
        }
        cyclePassStrs.forEach(cps ->
        {
            TrackType trackType = createTrackType(cps, p_cycle);
            UmmgPojoFactory ummgPojoFactory = UmmgPojoFactory.getInstance();
            List<AdditionalAttributeType> additionalAttributeTypes=
                    ummgPojoFactory.trackTypeToAdditionalAttributeType(trackType);
        });
        cycleStr = StringUtils.replace(cycleStr, "CYCLE", "");
        cycleStr = StringUtils.trim(StringUtils.replace(cycleStr, ":", ""));
        System.out.println(cycleStr);
	}

    public TrackType createTrackType(String cyclePassTileStr, Pattern p_cycle) {
        Matcher m_cycle = p_cycle.matcher(cyclePassTileStr);
        String cycleStr=null;
        while(m_cycle.find()) {
            cycleStr = m_cycle.group();
        }
        String cycleNoStr = StringUtils.trim(
            StringUtils.replace(
                StringUtils.replaceIgnoreCase(cycleStr, "CYCLE","")
                ,":",""));
        System.out.println("Cycle:" + cycleNoStr);
        TrackType trackType  = new TrackType();
        trackType.setCycle(NumberUtils.createInteger(cycleNoStr));

        Pattern p_pass = Pattern.compile("\\[.*?\\]");
        Matcher m_pass = p_pass.matcher(cyclePassTileStr);
        /**
         * The matcher will resolve to the following tokens:
         * [40, TILES: 4-5L 4-5R]
         * [41, TILES: 6R 6L]
         * [42, TILES: 7F]
         * The following loop process each token by extracting the passes and tilles
         */
        ArrayList<TrackPassTileType> trackPassTileTypes = new ArrayList<>();
        while(m_pass.find()) {
            TrackPassTileType trackPassTileType = new TrackPassTileType();
            String passTilesStr = m_pass.group();
            passTilesStr =StringUtils.replace(
                StringUtils.replace(passTilesStr,"[",""),"]","");
            passTilesStr = passTilesStr.replaceAll("TILES\\s*:\\s*?", "");
            String[] passTiles = StringUtils.split(passTilesStr, ",");
            String passStr = StringUtils.trim(passTiles[0]);
            trackPassTileType.setPass(NumberUtils.createInteger(passStr));
            List<String> tiles = getTiles(StringUtils.trim(passTiles[1]));
            trackPassTileType.setTiles(tiles);
            trackPassTileTypes.add(trackPassTileType);
        }
        trackType.setPasses(trackPassTileTypes);
        System.out.println("xxxxxx");
        return trackType;
    }

    /**
     *  5-8L 4K means, tile 5L, 6L, 7L, 8L and 4K.  because dash means a range separator
     *
     * @param tilesStr  ex 5-6L 4K
     * @return
     */
    public List<String> getTiles(String tilesStr) {
        ArrayList<String> tiles = new ArrayList<>();
        String[] tileRanges = StringUtils.split(tilesStr, " ");
        // Using Apache Commons Collections
        ObjectArrayIterator iterator = new ObjectArrayIterator(tileRanges);

        while (iterator.hasNext()) {
            String tileRangStr=(String) iterator.next();
            if(StringUtils.containsIgnoreCase(tileRangStr,"-")){
                String[] tileRangeTokens = StringUtils.split(tileRangStr, "-");
                String endTileStr = tileRangeTokens[tileRangeTokens.length -1];
                // find the letter for the last token
                String tileMarkChar = endTileStr.substring(endTileStr.length() - 1);
                Integer startTileNum = NumberUtils.createInteger(StringUtils.trim(tileRangeTokens[0]));
                Integer endTileNumStr = NumberUtils.createInteger(
                        StringUtils.trim(
                                StringUtils.substring(endTileStr, 0, endTileStr.length()-1)));
               for(Integer i = startTileNum; i<=endTileNumStr; i++ ) {
                   tiles.add(i + tileMarkChar);
               }
            } else { // if it is not a range of tiles like : 5-8L
                tiles.add(tileRangStr);
            }

        }
        return tiles;
    }



}
