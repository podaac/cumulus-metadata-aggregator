package gov.nasa.cumulus.metadata.test;

import static org.junit.Assert.*;

import java.util.*;

import gov.nasa.cumulus.metadata.util.BoundingTools;
import gov.nasa.cumulus.metadata.umm.generated.BoundingRectangleType;
import gov.nasa.cumulus.metadata.umm.generated.GeometryType;


import org.junit.Test;


public class AggregatorReleaseRegressionTest {

	@Test
	public void testNormal(){
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = -180;     
		double easternmostLongitude        = 180d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));
		GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude, westernmostLongitude,
                northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
			BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testNormal180(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 50;     
		double easternmostLongitude        = -180d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testNormalSplit(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 50;     
		double easternmostLongitude        = -150d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 2);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testNormalOn180(){
		//System.out.println("testNormalOn180...");
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 180;     
		double easternmostLongitude        = -30d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		assertEquals(bbs.size(), 1);
		System.out.println("...PASSED");
	}
	
	@Test
	public void testNormalOnNegative180(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = -180;     
		double easternmostLongitude        = -30d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		
		System.out.println("...PASSED");
	}
	
	
	@Test 
	public void testPrintout(){
		System.out.println("-----------------------------");
		System.out.println("Running 0-360 tests..");
		System.out.println("-----------------------------");
		
	}
	
	
	
	
	/*
	 * TESTING 0-360 range
	 */
	
	@Test
	public void testPositive(){
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 50;     
		double easternmostLongitude        = 80d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));
        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
		Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testPositive180(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 50;     
		double easternmostLongitude        = 180d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		
		System.out.println("...PASSED");
	}
	
	@Test
	public void testPositiveSplit(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 50;     
		double easternmostLongitude        = 320d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 2);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testPositiveSplitMerid(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 180;     
		double easternmostLongitude        = 320d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testPositiveCrossMerid(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 0;     
		double easternmostLongitude        = 190d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 2);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testPositiveCrossZero(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 320;     
		double easternmostLongitude        = 30d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude, westernmostLongitude, northernmostLatitude,
                southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test 
	public void testCollectionBBoxTest(){
		System.out.println("-----------------------------");
		System.out.println("Running Collection 0-360 tests...");
		System.out.println("-----------------------------");
	}
	
	@Test
	public void testCollection180(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = -180;     
		double easternmostLongitude        = 180d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(false, easternmostLongitude,
                westernmostLongitude, northernmostLatitude, southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		assertEquals(bbs.size(), 1);
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		System.out.println("...PASSED");
	}
	
	@Test
	public void testCollection360SpecialCase(){
	
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 0;     
		double easternmostLongitude        = 360d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude,
                westernmostLongitude, northernmostLatitude, southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		assertEquals(bbs.size(), 1);
		System.out.println("...PASSED");
	}
	
	@Test
	public void testCollection360(){
		double northernmostLatitude        = 90;     
		double southernmostLatitude        = -90;     
		double westernmostLongitude        = 90;     
		double easternmostLongitude        = 270d;  
		
		System.out.print(String.format("Testing [east: %f, west: %f, north: %f, south: %f ]",easternmostLongitude, westernmostLongitude, northernmostLatitude, southernmostLatitude));

        GeometryType g = BoundingTools.genBoundingBox(true, easternmostLongitude,
                westernmostLongitude, northernmostLatitude, southernmostLatitude);
        Set<BoundingRectangleType> bbs = g.getBoundingRectangles();
		
		for(Object b : bbs){
            BoundingRectangleType bb = (BoundingRectangleType)b;
			//System.out.println(String.format("BBox: [east: %f, west: %f, north: %f, south: %f ]", bb.getEastBoundingCoordinate(),bb.getWestBoundingCoordinate(), bb.getNorthBoundingCoordinate(), bb.getSouthBoundingCoordinate()));
			//bb.toString();
		}
		assertEquals(bbs.size(), 2);
		System.out.println("...PASSED");
	}
	
	@Test
	public void testBoundingrounding(){
		System.out.println("TestingBoundingRounding");
		
		System.out.println("TestingBoundingRounding 179.999999d, -179.9999999, 89.99999, -89.99999");
        GeometryType g = BoundingTools.genBoundingBox(false, 179.999999d, -179.9999999, 89.99999, -89.99999);
		System.out.println(g.getBoundingRectangles().toString());
		
		System.out.println("TestingBoundingRounding 179.99, -179.99, 89.9, -89.9");
		g = BoundingTools.genBoundingBox(false, 179.99, -179.99, 89.9, -89.9);
		System.out.println(g.getBoundingRectangles().toString());
		
		
	}
	
	
	
}
