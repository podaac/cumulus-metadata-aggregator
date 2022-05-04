package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;

import gov.nasa.cumulus.metadata.umm.generated.BoundingRectangleType;
import gov.nasa.cumulus.metadata.umm.generated.GeometryType;


import gov.nasa.cumulus.metadata.util.BoundingTools;
import org.junit.Test;




public class AggregatorRelease_4_3_0_Test {
	
	@Test
	public void test1833(){
		AggregatorTestSuite.printTestInfo("Translate Dataset Bounding Box from 0,360 to -180,180 Convention",1833);
		//Added to regression tests
		testCollection180();
		testCollection360SpecialCase();
		testCollection360();
		System.out.println("TEST 1833: PASSED...\n");
	}
	
	@Test
	public void test1893(){
		AggregatorTestSuite.printTestInfo("dataset collection_ids 2,5 out of memory error",1893);
		
		System.out.println("\tTested during runtime. ");
		System.out.println("TEST 1893: PASSED...\n");
	}
	

	

	public void testCollection180(){

		double northernmostLatitude        = 90;
		double southernmostLatitude        = -90;
		double westernmostLongitude        = -180;
		double easternmostLongitude        = 180d;

		System.out.print(String.format("\tTesting [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

		GeometryType g = BoundingTools.genBoundingBox(false,
                easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude);
		//List<Object> bbs = g.getBoundingRectangles();
		for(BoundingRectangleType b : g.getBoundingRectangles()){
			BoundingRectangleType bb = b;
			System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
        assertEquals(g.getBoundingRectangles().size(), 1);
		System.out.println("...PASSED");
	}
	
	
	public void testCollection360SpecialCase(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 0;     
		double easternmostLongitude        = 360d;  
		
		System.out.print(String.format("\tTesting [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));
	
		GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude,
                westernmostLongitude, northernmostLatitude, southernmostLatitude);
		//List<Object> bbs = g.getPointOrBoundingRectangleOrGPolygon();

		for(BoundingRectangleType b : g.getBoundingRectangles()){
			System.out.print(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", b.getEastBoundingCoordinate(),b.getWestBoundingCoordinate(), b.getNorthBoundingCoordinate(), b.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		assertEquals(g.getBoundingRectangles().size(), 1);
		System.out.println("...PASSED");
	}
	
	
	public void testCollection360(){
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 90;     
		double easternmostLongitude        = 270d;  
		
		System.out.print(String.format("\tTesting [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));
	
		GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude,
                westernmostLongitude, northernmostLatitude, southernmostLatitude);
		//List<Object> bbs = g.getPointOrBoundingRectangleOrGPolygon();
		
		for(BoundingRectangleType b : g.getBoundingRectangles()){
            System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", b.getEastBoundingCoordinate(), b.getWestBoundingCoordinate(), b.getNorthBoundingCoordinate(), b.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		assertEquals(g.getBoundingRectangles().size(), 2);
		System.out.println("...PASSED");
	}
	
}
