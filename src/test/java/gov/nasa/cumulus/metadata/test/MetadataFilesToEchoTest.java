package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import gov.nasa.cumulus.metadata.aggregator.MetadataFilesToEcho;

import gov.nasa.cumulus.metadata.aggregator.UMMGranule;
import gov.nasa.cumulus.metadata.aggregator.UMMUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class MetadataFilesToEchoTest {

	
	@Test
	public void testParseCommonHandlerFiles(){
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.mp").getFile());
		MetadataFilesToEcho mfte = new MetadataFilesToEcho();
		try {
			mfte.readCommonMetadataFile(file.getAbsolutePath(), "s3://a/path/to/s3");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(new Long(1491622211000l),mfte.getGranule().getStartTimeLong());
	}
	
	
	@Test
	public void testParseFootprintFiles(){
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.fp.xml").getFile());
		MetadataFilesToEcho mfte = new MetadataFilesToEcho();
		try {
			mfte.readFootprintFile(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(2,mfte.getGranule().getGranuleCharacterSet().size());
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
	public void testParseConfigFiles(){
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
		MetadataFilesToEcho mfte = new MetadataFilesToEcho();
		try {
			mfte.readConfiguration(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals("MODIS_T-JPL-L2P-v2014.0",mfte.getDataset().getShortName());
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
		Integer orbitNumber = (Integer) ((JSONObject)orbitArray.get(0)).get("OrbitNumber");
		assertEquals(orbitNumber, new Integer((int)870));
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
		Integer beginOrbit = (Integer) ((JSONObject)orbitArray.get(0)).get("BeginOrbitNumber");
		Integer endOrbit = (Integer) ((JSONObject)orbitArray.get(0)).get("EndOrbitNumber");
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
		assertEquals(374, track.get("Cycle"));
		assertEquals(172, ((JSONObject) ((JSONArray) track.get("Passes")).get(0)).get("Pass"));
	}
	
//	@Test
//	public void testGenerate(){
//		ClassLoader classLoader = getClass().getClassLoader();
//		File commonFile = new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.mp").getFile());
//		File footprintFile= new File(classLoader.getResource("20170408033000-JPL-L2P_GHRSST-SSTskin-MODIS_T-N-v02.0-fv01.0.nc.fp.xml").getFile());
//		File cfgFile = new File(classLoader.getResource("MODIS_T-JPL-L2P-v2014.0.cmr.cfg").getFile());
//		File msgFile = new File(classLoader.getResource("cumulus_message_example.json").getFile());
//
//		
//		MetadataFilesToEcho mfte = new MetadataFilesToEcho();
//		
//		try {
//			CumulusMessage message = new Gson().fromJson(IOUtils.toString(new FileInputStream(msgFile), "UTF-8"), CumulusMessage.class);
//			
//			//assume a single granule...
//			CumulusGranule cg = message.getPayload().getGranules().get(0);
//			for(CumulusGranuleFile cgf:cg.getFiles()){
//				
//				if(cgf.getFilename().endsWith(".mp"))
//					continue;
//				
//				
//				GranuleReference gr = new GranuleReference();
//				gr.setPath(cgf.getFilename());
//				gr.setStatus(GranuleStatus.ONLINE.toString());
//				gr.setType(GranuleArchiveType.DATA.toString());
//				mfte.getGranule().add(gr);
//			}
//			
//			mfte.readConfiguration(cfgFile.getAbsolutePath());
//			mfte.readFootprintFile(footprintFile.getAbsolutePath());
//			mfte.readCommonMetadataFile(commonFile.getAbsolutePath(), "s3://a/path/to/s3");
//			mfte.processGranule();
//			if(!mfte.validateXML()){
//				fail();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
//		System.out.println(mfte.getXML());
//	}
	

	
}
