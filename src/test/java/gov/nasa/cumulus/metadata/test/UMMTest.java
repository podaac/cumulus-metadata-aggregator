package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import gov.nasa.cumulus.metadata.aggregator.*;
import gov.nasa.cumulus.metadata.umm.generated.TrackPassTileType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.xml.sax.SAXException;
import org.mockito.Mockito;

public class UMMTest {

	MockedStatic<CMRRestClientProvider> mockedCMRRestClientProvider = null;
	@Before
	public void initialize() throws  URISyntaxException, ParseException, IOException{
		System.out.println("MetadataFilesToEchoTest constructor is being called");
		UMMGranuleFile spiedUMMGranuleFile = Mockito.spy(UMMGranuleFile.class);
		Mockito.doReturn(true)
				.when(spiedUMMGranuleFile)
				.isSpatialValid(any());
		CMRLambdaRestClient mockedEchoLambdaRestClient= Mockito.mock(CMRLambdaRestClient.class);
		Mockito.doReturn(true)
				.when(mockedEchoLambdaRestClient)
				.isUMMGSpatialValid(any(), any(), any());

		mockedCMRRestClientProvider = mockStatic(CMRRestClientProvider.class);
		mockedCMRRestClientProvider.when(CMRRestClientProvider::getLambdaRestClient).thenReturn(mockedEchoLambdaRestClient);
	}
	
	@After
	public void cleanup(){
		mockedCMRRestClientProvider.close();
	}
    @Test
    public void testIsoRequiredFields() throws IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException, URISyntaxException {
        /*
         * These tests are based on a deliberately modified ISO file
         * located in the src/test/resources directory:
         *
         * Granule_ISOMENDS_SWOT_Sample_L1_HR_TileBased_20181202_edit3.xml
         *
         * And validates the minimal required fields are present,
         * and result in a successful UMM-G export.
         */

		String testDir = "src/test/resources";

        String testFile = "Granule_ISOMENDS_SWOT_Sample_L1_HR_TileBased_20181202_edit3.xml";
        String testFilePath = testDir + File.separator + testFile;

        String testConfigFile = "testCollection.config";
        String testConfigFilePath = testDir + File.separator + testConfigFile;

        String granuleId = "PODAAC-4248_SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03";

        MetadataFilesToEcho mtfe = new MetadataFilesToEcho(true);

        mtfe.readConfiguration(testConfigFilePath);
        mtfe.readIsoMetadataFile(testFilePath, "s3://public/datafile.nc");

        mtfe.getGranule().setName(granuleId);
        // debug breakpoint
        System.out.println("Breakpoint!");
        //write UMM-G to file
        mtfe.writeJson( testDir + "/" + granuleId + ".cmr.json");

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(testDir + "/" + granuleId + ".cmr.json"));
        JSONObject umm = (JSONObject) obj;


        System.out.println(String.format("GranuleUR is not provided by ISO XML, "
                + "defined and supplied via datafile name - suffix: %s", granuleId));
        assertEquals(granuleId,umm.get("GranuleUR"));
        /*
         * Check to make sure the required fields exist in the UMM-G
         */
        assertNotNull(umm.get("ProviderDates"));
        // We should have two provider date entries, insert and update
        assertEquals(2, ((JSONArray)umm.get("ProviderDates")).size());
        assertNotNull(((JSONArray)umm.get("ProviderDates")).get(0));
        assertNotNull(((JSONArray)umm.get("ProviderDates")).get(1));
        JSONObject j1 = (JSONObject) ((JSONArray)umm.get("ProviderDates")).get(0);
        JSONObject j2 = (JSONObject) ((JSONArray)umm.get("ProviderDates")).get(1);
        assertNotNull(j1.get("Type"));
        assertNotNull(j1.get("Date"));
        assertNotNull(j2.get("Type"));
        assertNotNull(j1.get("Date"));
        boolean option1 = j1.get("Type").toString().equals("Insert") && j2.get("Type").toString().equals("Update");
        boolean option2 = j2.get("Type").toString().equals("Insert") && j1.get("Type").toString().equals("Update");
        // exactly one of the above should be 'true' but not both
        if ((option1 || option2) && !(option1 && option2)) {
            System.out.println("Found exactly one Insert and one Update field in ProviderDates.");
        } else {
            fail("Did not find exactly one Insert and one Update field in ProviderDates");
        }
        assertNotNull(umm.get("MetadataSpecification"));
        testMetadataSpec(umm, "1.6.5");
        // These tests are based on testCollection.config, and will need
        // to be changed if the test resource changes.
        JSONObject cr = (JSONObject)umm.get("CollectionReference");
        assertEquals("1",cr.get("Version"));
        assertEquals("L1B_HR_SLC",cr.get("ShortName"));
        /*
         * Now check to make sure various optional fields that have been
         * intentionally removed or broken, don't exist in the final UMM-G.
         */
        assertNull(umm.get("InputGranules"));
        assertNull(umm.get("Platforms"));
        assertNull(umm.get("ArchiveAndDistributionInformation"));
        assertNull(umm.get("MeasuredParameters"));
    }

    /**
     * This is a simple test to validate the UMM-G metadata spec against
     * a provided version string
     * @param umm   the JSONObject to check/test
     * @param ver   the expected UMM-G version, as decimal delimited string
     */
    private void testMetadataSpec(JSONObject umm, String ver) {
        JSONObject ms = (JSONObject) umm.get("MetadataSpecification");
        assertEquals(ms.get("Version"), ver);
        assertEquals(ms.get("URL"),"https://cdn.earthdata.nasa.gov/umm/granule/v"+ver);
        assertEquals(ms.get("Name"),"UMM-G");
    }


	/* Disabling this test since input format has changed, specifically for SWOTTrack - cycle pass tile */
	public void testIso2UmmMappings()
			throws XPathExpressionException, ParserConfigurationException, IOException,
			SAXException, ParseException, URISyntaxException {
		/*
		 * These tests are based on the ISO file located in the 
		 * src/test/resources directory. They validate the mapping of ISO to
		 * UMM-G. If the underlying ISO file changes, these tests will need to
		 * be updated. 
		 */
		//given an ISO file...
		//Granule_ISOMENDS_SWOT_Sample_L1_HR_TileBased_20181202_edit2.xml
		String testDir = "src/test/resources";

		String testFile = "Granule_ISOMENDS_SWOT_Sample_L1_HR_TileBased_20181202_edit2.xml";
		String testFilePath = testDir + File.separator + testFile;
		
		String testConfigFile = "testCollection.config";
		String testConfigFilePath = testDir + File.separator + testConfigFile;
		
		String granuleId = "SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03";
		
		MetadataFilesToEcho mtfe = new MetadataFilesToEcho(true);
		
		mtfe.readConfiguration(testConfigFilePath);
		mtfe.readIsoMetadataFile(testFilePath, "s3://public/datafile.nc");
		
		mtfe.getGranule().setName(granuleId);

		//write UMM-G to file
		mtfe.writeJson( testDir + "/" + granuleId + ".cmr.json");
		//the CMR file should have the following values... 
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(testDir + "/" + granuleId + ".cmr.json"));
        JSONObject umm = (JSONObject) obj;
        
        
        System.out.println(String.format("GranuleUR is not provided by ISO XML, "
        		+ "defined and supplied via datafile name - suffix: %s", granuleId));
        assertEquals(granuleId,umm.get("GranuleUR"));
        
        //InputGranules
        JSONArray a = (JSONArray) umm.get("InputGranules");
        	String[] _inputs = 
        		{
        			"SWOT_L0B_HR_Frame_001_005_011F_20210612T072103_20210612T072113_PGA200_03.nc",
        			"SWOT_L0B_HR_Frame_001_005_012F_20210612T072113_20210612T072123_PGA200_01.nc", 
        			"SWOT_L0B_HR_Frame_001_005_012F_20210612T072113_20210612T072123_PGA200_01.rc.xml"
    			};
        	ArrayList<String> inputs = new ArrayList<String>(3);
        	for (String s : _inputs) {
    			inputs.add(String.valueOf(s));
    		}
        for(int i=0; i < a.size(); i++){
        	if(!inputs.contains((String)a.get(i))){
        		fail("input array does not contain "+a.get(i));
        	}
        }
        
        
        //TemporalExtent/RangeDateTime
        
        JSONObject rdt = (JSONObject)((JSONObject) umm.get("TemporalExtent")).get("RangeDateTime");
        assertEquals((String)rdt.get("BeginningDateTime"), "2018-07-17T00:00:00.000Z");
        assertEquals((String)rdt.get("EndingDateTime"), "2018-07-17T23:59:59.999Z");
        
        //MetadataSpecification
        testMetadataSpec(umm, "1.6.3");
        
        //Platforms
        JSONObject p = (JSONObject) ((JSONArray)umm.get("Platforms")).get(0);
        assertEquals(p.get("ShortName"),"SWOT");
        assertEquals(((JSONObject)((JSONArray)p.get("Instruments")).get(0)).get("ShortName"),"KaRIn");
        
        //ProviderDates
        /*
         * These are generated by the mtfe code, and so we don't test them for an exact date.
        a = (JSONArray)umm.get("ProviderDates");
        for (int i=0; i<a.size();i++){
        	JSONObject date = (JSONObject)a.get(i);
        	if(date.get("Type").equals("Insert")){
        		assertEquals(date.get("Date"), "");
        	}
        	else if(date.get("Type").equals("Update")){
        		assertEquals(date.get("Date"),"");
        	}
        	else
        		fail();
        }*/
        
		//MeasuredParameters
        JSONObject param = (JSONObject)((JSONArray)umm.get("MeasuredParameters")).get(0);
        assertEquals("amplitude_hh", param.get("ParameterName"));
        
        assertEquals(20.5, ((JSONObject)param.get("QAStats")).get("QAPercentMissingData"));
        assertEquals(10.5, ((JSONObject)param.get("QAStats")).get("QAPercentOutOfBoundsData"));
        
        //SpatialExtent
        JSONObject hsd = (JSONObject)((JSONObject)umm.get("SpatialExtent")).get("HorizontalSpatialDomain");
        JSONObject orbit = (JSONObject) hsd.get("Orbit");
        assertEquals(((Double)orbit.get("StartLatitude")), new Double(-76.34));
        assertEquals(((Double)orbit.get("EndLatitude")), new Double(78.954));
        assertEquals(((Double)orbit.get("AscendingCrossing")), new Double(88.92));
        assertEquals(((String)orbit.get("StartDirection")), "D");
        assertEquals(((String)orbit.get("EndDirection")), "D");

/* UMM-G only accepts either bounding Geometry or Orbit, not both.  Since the input xml has "Orbit", that takes priority
   so this part of the test was commented out and a new orbit check was added.
   TODO - convert this into a split test, one for ISO with orbit, and one for ISO without

        JSONObject geom = (JSONObject) hsd.get("Geometry");
        
        //Geometry/GPolygons
        JSONObject bndry = (JSONObject)((JSONObject)((JSONArray) geom.get("GPolygons")).get(0)).get("Boundary");
        JSONArray pnts = (JSONArray) bndry.get("Points");
        
        for(int i=0; i< pnts.size(); i++){
        	
        	JSONObject pt = (JSONObject) pnts.get(i);
        	if(((Double)pt.get("Latitude")).equals(new Double(-11))){
        		assertEquals(((Double)pt.get("Longitude")),new Double(-17));
        	}else if(((Double)pt.get("Latitude")).equals(new Double(-10))){
        		assertEquals(((Double)pt.get("Longitude")),new Double(10));
        	}else if(((Double)pt.get("Latitude")).equals(10d)){
        		assertEquals(new Double(Math.abs(((Double)pt.get("Longitude")))),new Double(10));
        	}
        	else{
        		fail(String.format("Found invalid point: %f, %f", pt.get("Latitude"), pt.get("Longitude")));
        	}
        }
        //Geometry/BoundingRectangles
        JSONObject br = (JSONObject) ((JSONArray) geom.get("BoundingRectangles")).get(0);
        assertEquals(br.get("WestBoundingCoordinate"), new Double(-180));
        assertEquals(br.get("SouthBoundingCoordinate"), new Double(-85.045));
        assertEquals(br.get("EastBoundingCoordinate"), new Double(179.999));
        assertEquals(br.get("NorthBoundingCoordinate"), new Double(85.045));
*/
        
        //Track
        JSONObject track = (JSONObject) hsd.get("Track");
        assertEquals(track.get("Cycle"), new Long(5));
        JSONArray passes = (JSONArray) track.get("Passes");
        
        ArrayList<Long> passVals = new ArrayList<Long>(Arrays.asList(new Long(40), new Long(41), new Long(42)));
        ArrayList<String> tileVals= new ArrayList<String>(Arrays.asList("4L","5L","5R", "6R", "7F"));
        
        
        for(int i = 0; i < passes.size(); i++){
        	JSONObject pass = (JSONObject) passes.get(i);
        	assertTrue(passVals.contains(pass.get("Pass")));

        	JSONArray tiles  = (JSONArray) pass.get("Tiles");
        	for(int j = 0; j < tiles.size(); j++){
        		assertTrue(tileVals.contains(tiles.get(j)));
        	}
        }
        
        //PGEVersionClass
        JSONObject pgev = (JSONObject) umm.get("PGEVersionClass");
        assertEquals("PGE_L1B_HR_SLC", pgev.get("PGEName"));
        assertEquals("1.1.4", pgev.get("PGEVersion"));
        
        //DataGranule
        JSONObject dg = (JSONObject)umm.get("DataGranule");
              
      //DataGranule/ArchiveAndDistributionInformation
        JSONArray files = (JSONArray) dg.get("ArchiveAndDistributionInformation");
        for(int i = 0; i < files.size(); i++){
        	JSONObject f = (JSONObject) files.get(i);
        	if(f.get("Name").equals("SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03.nc")){
        		assertEquals("KB",f.get("SizeUnit"));
        		assertEquals("NETCDF-4",f.get("Format"));
        		assertEquals(new Long(123),f.get("Size"));
        		assertEquals("application/x-netcdf",f.get("MimeType"));
        		assertEquals("E51569BF48DD0FD0640C6503A46D4753",((JSONObject)f.get("Checksum")).get("Value"));
        		assertEquals("MD5",((JSONObject)f.get("Checksum")).get("Algorithm"));
        	}
        	else if(f.get("Name").equals("SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03.iso.xml")){
        		assertEquals("KB",f.get("SizeUnit"));
        		assertEquals("XML",f.get("Format"));
        		assertEquals(new Long(203),f.get("Size"));
        		assertEquals("application/xml",f.get("MimeType"));
        		assertEquals("E51569BF48DD0FD0640C6503A46D4753",((JSONObject)f.get("Checksum")).get("Value"));
        		assertEquals("MD5",((JSONObject)f.get("Checksum")).get("Algorithm"));
        	}
        	else if(f.get("Name").equals("SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03.png")){
        		assertEquals("KB",f.get("SizeUnit"));
        		assertEquals("PNG",f.get("Format"));
        		assertEquals(new Long(230),f.get("Size"));
        		assertEquals("image/png",f.get("MimeType"));
        		assertEquals("E51569BF48DD0FD0640C6503A46D4753",((JSONObject)f.get("Checksum")).get("Value"));
        		assertEquals("MD5",((JSONObject)f.get("Checksum")).get("Algorithm"));
        	}
        	else{
        		fail("Could not find file with name " + f.get("Name"));
        	}
        }
        
        
        //DataGranule/DayNightFlag
        assertEquals("Unspecified",dg.get("DayNightFlag"));
        
        //DataGranule/Identifiers
        JSONArray ids = (JSONArray) dg.get("Identifiers");
        for (int i =0; i< ids.size(); i++){
        	JSONObject id = (JSONObject) ids.get(i);
        	if(id.get("IdentifierType").equals("ProducerGranuleId")){
        		assertEquals("SWOT_L1B_HR_SLC_001_005_001L_20210612T072103_20210612T07215_PGA200_03.nc",id.get("Identifier"));
        	}
        	else if(id.get("IdentifierType").equals("CRID")){
        		assertEquals("PGA200",id.get("Identifier"));
        	}
        	else if(id.get("IdentifierType").equals("Other")){
        		
        		if(id.get("IdentifierName").equals("SASVersionId")){
        			assertEquals("7.8.9",id.get("Identifier"));
        		}else if(id.get("IdentifierName").equals("PGEVersionId")){
        			assertEquals("4.5.6",id.get("Identifier"));
        		}else if(id.get("IdentifierName").equals("ScienceAlgorithmVersionId")){
        			assertEquals("017",id.get("Identifier"));
        		}else if(id.get("IdentifierName").equals("ProductCounter")){
        			assertEquals("03",id.get("Identifier"));
        		}else{
        			fail("Could not find identifierName " + id.get("IdentifierName"));
        		}
        	}
        	else{
        		fail("Could not find identifier " + id.get("IdentifierType"));
        	}
        }
        
        assertEquals("One Post-Calibration bulk reprocessing and one End-of-mission bulk reprocessing",dg.get("ReprocessingPlanned"));
        assertEquals("2018-07-19T12:01:01.000Z",dg.get("ProductionDateTime"));
        
        //CollectionReference
        JSONObject cr = (JSONObject)umm.get("CollectionReference");
        assertEquals("1",cr.get("Version"));
        assertEquals("L1B_HR_SLC",cr.get("ShortName"));
        
        
        /*
         * "RelatedUrls": [
    {
      "Type": "GET DATA",
      "Description": "The base directory location for the granule.",
      "URL": "s3://public/datafile.nc"
    }
  ]
         */
        //RelatedUrls
        JSONObject rurl = (JSONObject)((JSONArray)umm.get("RelatedUrls")).get(0);
		assertEquals("GET DATA", rurl.get("Type"));
		assertEquals("The base directory location for the granule.", rurl.get("Description"));
		assertEquals("s3://public/datafile.nc", rurl.get("URL"));
		//fail("Not yet implemented");
	}

	@Test
	public void testSentinelManifest2UmmMappings()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		String testFile = "S6A_P4_0__ACQ_____20210414T001438_20210414T002150_20200429T143331_0432_002_127_063_EUM__OPE_NR_TST.SEN6.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L0_ALT_ACQ.config";
		String granuleId = "S6A_P4_0__ACQ_____20210414T001438_20210414T002150_20200429T143331_0432_002_127_063_EUM__OPE_NR_TST.SEN6";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		//TemporalExtent/RangeDateTime
		JSONObject rdt = (JSONObject) ((JSONObject) umm.get("TemporalExtent" )).get("RangeDateTime" );
		assertEquals((String) rdt.get("BeginningDateTime" ), "2021-04-14T00:14:38.000Z" );
		assertEquals((String) rdt.get("EndingDateTime" ), "2021-04-14T00:21:49.532Z" );

		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );

		//Track
		JSONObject track = (JSONObject) hsd.get("Track" );
		assertEquals(track.get("Cycle" ), new Long(2));
		JSONArray passes = (JSONArray) track.get("Passes" );
		assertEquals(((JSONObject) passes.get(0)).get("Pass"), new Long(127));

		JSONObject geom = (JSONObject) hsd.get("Geometry" );
		//Footprint
		// In this case, we have a small polygon which does NOT cross dateline. Hence, it will not be divided to
		// 2 polygons
		Object boundaryObj = ((JSONObject)(((JSONArray) geom.get("GPolygons")).get(0))).get("Boundary");
		JSONArray pnts = (JSONArray) ((JSONObject)boundaryObj).get("Points");

		JSONObject firstPoint = (JSONObject) pnts.get(0);
		assertEquals(new Double(-45.4871), ((Double) firstPoint.get("Latitude" )));
		assertEquals(new Double(-132.544), ((Double) firstPoint.get("Longitude" )));

		JSONObject midPoint = (JSONObject) pnts.get(3);
		assertEquals(new Double( -51.5451), ((Double) midPoint.get("Latitude" )));
		assertEquals(new Double(-139.042), ((Double) midPoint.get("Longitude" )));

		JSONObject lastPoint = (JSONObject) pnts.get(5);
		assertEquals(new Double(-45.4871), ((Double) lastPoint.get("Latitude" )));
		assertEquals(new Double(-132.544), ((Double) lastPoint.get("Longitude" )));

		//DataGranule
		JSONObject dg = (JSONObject) umm.get("DataGranule" );
		assertEquals("2020-04-29T14:33:31.000Z", dg.get("ProductionDateTime" ));

		//CollectionReference
		JSONObject cr = (JSONObject) umm.get("CollectionReference" );
		assertEquals("E", cr.get("Version" ));
		assertEquals("JASON_CS_S6A_L0_ALT_ACQ", cr.get("ShortName" ));

		JSONObject productName = (JSONObject) ((JSONArray) umm.get("AdditionalAttributes")).get(0);
		assertEquals("ProviderDataSource", productName.get("Name"));
		assertEquals(granuleId, ((JSONArray) productName.get("Values")).get(0));
	}

	@Test
	public void testSentinelManifestOverIDL()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		// this test file will split to 3 geos (over dateline) and we will reconnect the 1st and 3rd line to polygon
		String testFile = "S6A_P4_2__LR_STD__ST_022_132_20210619T002429_20210619T012042_F02.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L0_ALT_ACQ.config";
		String granuleId ="S6A_P4_2__LR_STD__ST_022_132_20210619T002429_20210619T012042_F02";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);
		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );

		JSONObject geom = (JSONObject) hsd.get("Geometry" );
		//Footprint
		// In this case, we have a small polygon which does NOT cross dateline. Hence, it will not be divided to
		// 2 polygons
		Object boundaryObj = ((JSONObject)(((JSONArray) geom.get("GPolygons")).get(0))).get("Boundary");
		JSONArray pnts = (JSONArray) ((JSONObject)boundaryObj).get("Points");

		JSONObject firstPoint = (JSONObject) pnts.get(0);
		assertEquals(Double.valueOf(66.644644), ((Double) firstPoint.get("Latitude" )));
		assertEquals(Double.valueOf(140.378601), ((Double) firstPoint.get("Longitude" )));

		JSONObject midPoint = (JSONObject) pnts.get(3);
		assertEquals(Double.valueOf(58.947656), ((Double) midPoint.get("Latitude" )));
		assertEquals(Double.valueOf(180.0), ((Double) midPoint.get("Longitude" )));

		JSONObject lastPoint = (JSONObject) pnts.get(5);
		assertEquals(Double.valueOf(63.594104), ((Double) lastPoint.get("Latitude" )));
		assertEquals(Double.valueOf(168.727685), ((Double) lastPoint.get("Longitude" )));

		//2nd polygon
		boundaryObj = ((JSONObject)(((JSONArray) geom.get("GPolygons")).get(1))).get("Boundary");
		pnts = (JSONArray) ((JSONObject)boundaryObj).get("Points");

		firstPoint = (JSONObject) pnts.get(0);
		assertEquals(Double.valueOf(59.804021000000006), ((Double) firstPoint.get("Latitude" )));
		assertEquals(Double.valueOf(-180), ((Double) firstPoint.get("Longitude" )));

		midPoint = (JSONObject) pnts.get(15);
		assertEquals(Double.valueOf(-66.647778), ((Double) midPoint.get("Latitude" )));
		assertEquals(Double.valueOf(-53.840211), ((Double) midPoint.get("Longitude" )));

		lastPoint = (JSONObject) pnts.get(29);
		assertEquals(Double.valueOf(56.013938), ((Double) lastPoint.get("Latitude" )));
		assertEquals(Double.valueOf(-171.655155), ((Double) lastPoint.get("Longitude" )));

		assertTrue(UnitTestUtil.compareFileWithGranuleJson("ummgResults/sentinel6/S6A_P4_2__LR_STD__ST_022_132_20210619T002429_20210619T012042_F02_overIde.json",umm));
	}

	@Test
	public void testSentinelManifestL0TooFewCoordinates()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		// this test file will split to 3 geos (over dateline) and we will reconnect the 1st and 3rd line to polygon
		String testFile = "L0-1.xml";
		String testConfigFile = "JASON_CS_S6A_L0_ALT_ACQ.config";
		String granuleId ="S6A_P4_2__LR_STD__ST_022_132_20210619T002429_20210619T012042_F02";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);
		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );
		JSONArray boundingCoordinates = (JSONArray) ((JSONObject)hsd.get("Geometry")).get("BoundingRectangles");
		assertEquals(boundingCoordinates.size(),1);

		//Geometry/BoundingRectangles
		JSONObject gbbx = (JSONObject)boundingCoordinates.get(0);
		assertEquals(gbbx.get("WestBoundingCoordinate"), Double.valueOf(-180));
		assertEquals(gbbx.get("SouthBoundingCoordinate"), Double.valueOf(-90.00));
		assertEquals(gbbx.get("EastBoundingCoordinate"), Double.valueOf(180.00));
		assertEquals(gbbx.get("NorthBoundingCoordinate"), Double.valueOf(90.00));
	}

	@Test
	public void testSentinelManifestNotOverIDL()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		// this test file will split to 1 geos (over dateline) and we will reconnect the 1st and 3rd line to polygon
		String testFile = "S6A_P4_2__LR_STD__ST_022_131_20210618T232816_20210619T002429_F02.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L0_ALT_ACQ.config";
		String granuleId ="S6A_P4_2__LR_STD__ST_022_131_20210618T232816_20210619T002429_F02";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );

		JSONObject geom = (JSONObject) hsd.get("Geometry" );
		//Footprint
		// In this case, we have a small polygon which does NOT cross dateline. Hence, it will not be divided to
		// 2 polygons
		Object boundaryObj = ((JSONObject)(((JSONArray) geom.get("GPolygons")).get(0))).get("Boundary");
		JSONArray pnts = (JSONArray) ((JSONObject)boundaryObj).get("Points");

		JSONObject firstPoint = (JSONObject) pnts.get(0);
		assertEquals(Double.valueOf(-65.649768), ((Double) firstPoint.get("Latitude" )));
		assertEquals(Double.valueOf(-25.561001), ((Double) firstPoint.get("Longitude" )));

		JSONObject midPoint = (JSONObject) pnts.get(16);
		assertEquals(Double.valueOf(65.64749), ((Double) midPoint.get("Latitude" )));
		assertEquals(Double.valueOf(140.321732), ((Double) midPoint.get("Longitude" )));

		JSONObject lastPoint = (JSONObject) pnts.get(31);
		assertEquals(Double.valueOf(-62.663981), ((Double) lastPoint.get("Latitude" )));
		assertEquals(Double.valueOf(2.525361), ((Double) lastPoint.get("Longitude" )));
		assertTrue(UnitTestUtil.compareFileWithGranuleJson("ummgResults/sentinel6/S6A_P4_2__LR_STD__ST_022_131_20210618T232816_20210619T002429_F02_notOverIDL.json", umm));
	}

	@Test
	/**
	 * Since the input xml has weird set of coordinates.  It should return a global bounding box.
	 */
	public void testSentinelManifestIDLErrorCase()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		// this test file will split to 1 geos (over dateline) and we will reconnect the 1st and 3rd line to polygon
		String testFile = "error_lr.xml";
		String testConfigFile = "JASON_CS_S6A_L0_ALT_ACQ.config";
		String granuleId ="S6A_P4_2__LR_STD__ST_022_131_20210618T232816_20210619T002429_F02";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );

		JSONObject geom = (JSONObject) hsd.get("Geometry" );
		//Footprint
		// In this case, we have a small polygon which does NOT cross dateline. Hence, it will not be divided to
		// 2 polygons
		Object firstBoundBoxObj = ((JSONArray)geom.get("BoundingRectangles")).get(0);
		JSONObject bouningdbox = (JSONObject)firstBoundBoxObj;

		assertEquals(Double.valueOf(-180.0), ((Double) bouningdbox.get("WestBoundingCoordinate")));
		assertEquals(Double.valueOf(-90.0), ((Double) bouningdbox.get("SouthBoundingCoordinate")));

		assertEquals(Double.valueOf(180.0), ((Double) bouningdbox.get("EastBoundingCoordinate")));
		assertEquals(Double.valueOf(90.0), ((Double) bouningdbox.get("NorthBoundingCoordinate")));
	}

	@Test
	public void testSentinelAuxManifest2UmmMappings()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		String testFile = "S6A_P4_1__ECHO_AX_20170304T025713_20210411T232934_20200505T141849__________________EUM__OPE_NR_TST.SEN6.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L1_ALT_ECHO_AX.config";
		String granuleId = "S6A_P4_1__ECHO_AX_20170304T025713_20210411T232934_20200505T141849__________________EUM__OPE_NR_TST.SEN6";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		//TemporalExtent/RangeDateTime
		JSONObject rdt = (JSONObject) ((JSONObject) umm.get("TemporalExtent" )).get("RangeDateTime" );
		assertEquals("2017-03-04T02:57:13.000Z", (String) rdt.get("BeginningDateTime" ));
		assertEquals("2021-04-11T23:29:34.000Z", (String) rdt.get("EndingDateTime" ));

		//DataGranule
		JSONObject dg = (JSONObject) umm.get("DataGranule" );
		assertEquals("2020-05-05T14:18:49.000Z", dg.get("ProductionDateTime" ));

		//CollectionReference
		JSONObject cr = (JSONObject) umm.get("CollectionReference" );
		assertEquals("E", cr.get("Version" ));
		assertEquals("JASON_CS_S6A_L1_ALT_ECHO_AX", cr.get("ShortName" ));

		JSONObject productName = (JSONObject) ((JSONArray) umm.get("AdditionalAttributes")).get(0);
		assertEquals("ProviderDataSource", productName.get("Name"));
		assertEquals(granuleId, ((JSONArray) productName.get("Values")).get(0));
	}

	@Test
	public void testSentinelManifestL1Footprint()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		String testFile = "S6A_P4_1B_LR______20210412T234541_20210413T004154_20200428T194602_3373_002_100_050_EUM__OPE_NT_TST.SEN6.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L1_ALT_ECHO_AX.config";
		String granuleId = "S6A_P4_1B_LR______20210412T234541_20210413T004154_20200428T194602_3373_002_100_050_EUM__OPE_NT_TST.SEN6";
		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		//SpatialExtent
		JSONObject hsd = (JSONObject) ((JSONObject) umm.get("SpatialExtent" )).get("HorizontalSpatialDomain" );

		JSONObject geom = (JSONObject) hsd.get("Geometry" );
		assertNull(geom.get("BoundingRectangles"));

		//Footprint
		// In this case, we have a small polygon which does NOT cross dateline. Hence, it will not be divided to
		// 2 polygons
		Object boundaryObj = ((JSONObject)(((JSONArray) geom.get("GPolygons")).get(0))).get("Boundary");
		JSONArray pnts = (JSONArray) ((JSONObject)boundaryObj).get("Points");

		assertEquals(33, pnts.size());

		DecimalFormat decimalFormat = new DecimalFormat("###.###");
		JSONObject firstPoint = (JSONObject) pnts.get(0);
		assertEquals("-64.654", decimalFormat.format(firstPoint.get("Latitude")));
		assertEquals("-167.571", decimalFormat.format(firstPoint.get("Longitude")));

		JSONObject lastPoint = (JSONObject) pnts.get(31);
		assertEquals("-58.844", decimalFormat.format(lastPoint.get("Latitude")));
		assertEquals("-144.155", decimalFormat.format(lastPoint.get("Longitude")));

		JSONObject productName = (JSONObject) ((JSONArray) umm.get("AdditionalAttributes")).get(0);
		assertEquals("ProviderDataSource", productName.get("Name"));
		assertEquals(granuleId, ((JSONArray) productName.get("Values")).get(0));
	}

	@Test
	public void testSwotL02UmmMappings()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, URISyntaxException {
		//given a SWOT L0 archive.xml file...
		//SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.archive.xml
		String testDir = "src/test/resources";
		String testFile = "SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.archive.xml";
		String testFilePath = testDir + File.separator + testFile;

		String testConfigFile = "testCollection.config";
		String testConfigFilePath = testDir + File.separator + testConfigFile;

		String granuleId = "SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1";

		MetadataFilesToEcho mtfe = new MetadataFilesToEcho(false);

		mtfe.readConfiguration(testConfigFilePath);
		mtfe.readSwotArchiveXmlFile(testFilePath);
		// simulate cumulus json array input through payload
		// Since https://bugs.earthdata.nasa.gov/browse/PCESA-2305, the ArchiveAndDistributionInformation
		// array structure will be constructed by using input_granules from the cumulus payload message
		JSONArray input_granules = new JSONArray();
		JSONArray files = new JSONArray();
		JSONObject f1 = new JSONObject();
		f1.put("name", "SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.nc");
		f1.put("size", 167890.0);
		f1.put("checksum", "kkkkmmmmeeeee");
		f1.put("checksumType","md5");
		files.add(f1);
		JSONObject f2 = new JSONObject();
		f2.put("name", "SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1.xml");
		f2.put("size", 167890.2);
		f2.put("checksum", "kkkkmmmmeeeee2");
		f2.put("checksumType","md5");
		files.add(f2);

		JSONObject granule_object = new JSONObject();
		granule_object.put("files", files);
		input_granules.add(granule_object);

		mtfe.setGranuleFileSizeAndChecksum(input_granules);
		//SWOT_IVK_20210612T081400_20210612T072103_20210612T080137_O_APID1402.PTM_1
		mtfe.getGranule().setName(granuleId);

		//write UMM-G to file
		mtfe.writeJson(testDir + "/" + granuleId + ".cmr.json");
		//the CMR file should have the following values...

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(testDir + "/" + granuleId + ".cmr.json"));
		JSONObject umm = (JSONObject) obj;

		assertEquals(granuleId, umm.get("GranuleUR"));

		//TemporalExtent/RangeDateTime
		JSONObject rdt = (JSONObject) ((JSONObject) umm.get("TemporalExtent")).get("RangeDateTime");
		assertEquals("2021-06-12T07:33:57.000Z", (String) rdt.get("BeginningDateTime"));
		assertEquals("2021-06-12T07:47:28.000Z", (String) rdt.get("EndingDateTime"));

		//DataGranule
		JSONObject dg = (JSONObject) umm.get("DataGranule");
		assertEquals("2021-06-12T08:14:00.000Z", dg.get("ProductionDateTime"));

		assertEquals(2, ((JSONArray) dg.get("ArchiveAndDistributionInformation")).size());
		JSONObject file = (JSONObject) ((JSONArray) dg.get("ArchiveAndDistributionInformation")).get(0);
		assertEquals(new Long(167890), file.get("SizeInBytes"));
		JSONObject checksumObj =  (JSONObject)file.get("Checksum");
		assertEquals("kkkkmmmmeeeee", checksumObj.get("Value"));
		assertEquals("MD5", checksumObj.get("Algorithm"));

		//SpatialExtent
		JSONObject hsd = (JSONObject)((JSONObject)umm.get("SpatialExtent")).get("HorizontalSpatialDomain");

		JSONObject geom = (JSONObject) hsd.get("Geometry");

		//Geometry/BoundingRectangles
		JSONObject br = (JSONObject) ((JSONArray) geom.get("BoundingRectangles")).get(0);
		assertEquals(-180.0, br.get("WestBoundingCoordinate"));
		assertEquals(-90.0, br.get("SouthBoundingCoordinate"));
		assertEquals(180.0, br.get("EastBoundingCoordinate"));
		assertEquals(90.0, br.get("NorthBoundingCoordinate"));
	}


	@Test
	public void testSentinelManifestNoProductName()
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		String testFile = "S6A_P4_2__LR_RED__NR_002_085_20180627T061817_20180627T081400_F00.xfdumanifest.xml";
		String testConfigFile = "JASON_CS_S6A_L2_ALT_LR_RED_OST_NRT_F.config";
		String granuleId = "S6A_P4_2__LR_RED__NR_002_085_20180627T061817_20180627T081400_F00";

		JSONObject umm = parseXfduManifest(testFile, testConfigFile, granuleId);

		assertNull(umm.get("AdditionalAttributes"));
	}


	@Test
	public void testGetUmmChecksumAlgorithm() {
		assertEquals("SHA-512", UMMGranuleFile.getUmmChecksumAlgorithm("SHA512"));
		assertEquals("SHA-512", UMMGranuleFile.getUmmChecksumAlgorithm("SHA-512"));
		assertEquals("MD5", UMMGranuleFile.getUmmChecksumAlgorithm("MD5"));
	}

	@Test
	public void testAddLine() {
		String footprint = "-64.9117 63.6536 -64.9117 63.6536 -64.9117 63.6536 -64.9117 63.6579 -64.9117 63.6579 -64.9117 63.6579 -64.9117 63.6536";
		JSONObject geometry = new JSONObject();
		UMMGranuleFile.addLine(geometry, footprint);
		JSONArray points = ((JSONArray) ((JSONObject) ((JSONArray) geometry.get("Lines")).get(0)).get("Points"));
		assertEquals(2, points.size());
		assertEquals(63.6536, ((JSONObject) points.get(0)).get("Longitude"));
		assertEquals(-64.9117, ((JSONObject) points.get(0)).get("Latitude"));
		assertEquals(63.6579, ((JSONObject) points.get(1)).get("Longitude"));
		assertEquals(-64.9117, ((JSONObject) points.get(1)).get("Latitude"));
	}
	/**
	 * Test the creation of Track JSONObject through POJO
	 * the POJO is TrackType which is a member of UMMGranule
	 */

	@Test
	public void testCreateTrack()  throws ParseException{
		TrackType trackType = new TrackType();
		trackType.setCycle(new Integer(22));
		TrackPassTileType trackPassTileType1 = new TrackPassTileType();
		trackPassTileType1.setPass(11);
		List<String> tiles1 = new ArrayList<>();
		tiles1.add("tile1-1");
		tiles1.add("tile1-2");
		trackPassTileType1.setTiles(tiles1);
		TrackPassTileType trackPassTileType2 = new TrackPassTileType();
		trackPassTileType2.setPass(22);
		List<String> tiles2 = new ArrayList<>();
		tiles2.add("tile2-1");
		tiles2.add("tile2-2");
		trackPassTileType2.setTiles(tiles2);
		ArrayList<TrackPassTileType> trackPassTileTypeArrayList = new ArrayList<>();
		trackPassTileTypeArrayList.add(trackPassTileType1);
		trackPassTileTypeArrayList.add(trackPassTileType2);
		trackType.setPasses(trackPassTileTypeArrayList);
		UMMGranule ummGranule = new UMMGranule();
		ummGranule.setTrackType(trackType);
		UMMGranuleFile ummGranuleFile = new UMMGranuleFile(ummGranule, null, true);

		JSONObject trackJsonObj = ummGranuleFile.createUMMGTrack(ummGranule);
		assertEquals(trackJsonObj.containsKey("Cycle"), true);
		assertEquals(trackJsonObj.containsKey("Passes"), true);
		JSONArray passes = (JSONArray) trackJsonObj.get("Passes");
		assertEquals(passes.size(), 2);
		JSONObject pass0 = (JSONObject)passes.get(0);
		JSONObject pass1 = (JSONObject)passes.get(1);
		assertTrue(((Long)pass1.get("Pass")).longValue()==(new Long(22)).longValue());
		JSONArray tiles = (JSONArray)pass1.get("Tiles");
		assertEquals(tiles.size(), 2);
		String tile = (String)tiles.get(1);
		assertTrue(StringUtils.equals(tile, "tile2-2"));
	}

	private JSONObject parseXfduManifest(String testFile, String testConfigFile, String granuleId)
			throws XPathExpressionException, ParserConfigurationException,
			IOException, SAXException, ParseException, java.text.ParseException, URISyntaxException {
		String testDir = "src/test/resources";
		String testFilePath = testDir + File.separator + testFile;
		String testConfigFilePath = testDir + File.separator + testConfigFile;

		MetadataFilesToEcho mtfe = new MetadataFilesToEcho(false);

		mtfe.readConfiguration(testConfigFilePath);
		mtfe.readSentinelManifest(testFilePath);

		mtfe.getGranule().setName(granuleId);
		//write UMM-G to file
		mtfe.writeJson(testDir + "/" + granuleId + ".cmr.json");

		//the CMR file should have the following values...
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(testDir + "/" + granuleId + ".cmr.json"));
		return (JSONObject) obj;
	}


}
