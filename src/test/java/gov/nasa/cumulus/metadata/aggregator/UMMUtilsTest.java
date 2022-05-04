package gov.nasa.cumulus.metadata.aggregator;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class UMMUtilsTest {

    ArrayList<Coordinate> rawPolygon;
    @Before
    public void setUp() {
        String lineStringCrossDateLine = "-65.651437 16.946757 -62.667631 45.037134 -55.246907 64.391453 -45.584413 76.453312 -34.831615 84.452250 -23.515790 90.307655 -11.903385 95.058664 -0.152979 99.333793 11.620201 103.604332 23.302128 108.340948 34.747230 114.174772 45.701111 122.147466 55.616589 134.139907 63.318602 153.280438 66.638623 -178.779367 66.647546 -177.176856 65.647562 -177.171288 65.638639 -178.773799 62.325601 153.398546 54.689196 134.513996 44.931393 122.785851 34.159689 114.983966 22.848425 109.232102 11.244584 104.531107 -0.493947 100.273868 -12.245287 95.998400 -23.894427 91.233200 -35.291048 85.340462 -46.180882 77.255948 -56.026709 65.017479 -63.600942 45.396204 -66.645779 17.052980 -65.651437 16.946757";
        rawPolygon = UMMUtils.lineString2Coordinates(lineStringCrossDateLine);
        assertTrue(rawPolygon.get(0).x == rawPolygon.get(rawPolygon.size()-1).x
                &&
                rawPolygon.get(0).y == rawPolygon.get(rawPolygon.size()-1).y);
    }
    @Test
    public void testSplit() {
        List<List<Coordinate>> splittedGeos  = UMMUtils.split(this.rawPolygon);
        assertEquals(splittedGeos.size(), 3);
    }

    @Test
    public void testCloseUp() {
        int originalSize = this.rawPolygon.size();
        ArrayList<Coordinate> afterRemoveClosingCoordinates = UMMUtils.removeClosingCoordinate(this.rawPolygon);
        ArrayList<Coordinate> afterCloseUpCoordinates = UMMUtils.closeUp(afterRemoveClosingCoordinates);
        assertEquals(originalSize, afterCloseUpCoordinates.size());
        assertTrue(afterCloseUpCoordinates.get(0).x == afterCloseUpCoordinates.get(afterCloseUpCoordinates.size()-1).x
                &&
                afterCloseUpCoordinates.get(0).y == afterCloseUpCoordinates.get(afterCloseUpCoordinates.size()-1).y);
    }
    @Test
    public void testReconstructPolygonsOver2Lines() {
        // The sample 2 polygon will be splitted to 3 geos.  the 1st and 3rd should be connected.
        // the 2nd is itself a polygon.
        List<List<Coordinate>> splittedGeos  = UMMUtils.split(this.rawPolygon);
        ArrayList<Coordinate> polygonArray = UMMUtils.reconstructPolygonsOver2Lines(
                (ArrayList<Coordinate>) splittedGeos.get(0), (ArrayList<Coordinate>) splittedGeos.get(2));
        assertTrue(polygonArray.get(0).x == polygonArray.get(polygonArray.size()-1).x
                &&
                polygonArray.get(0).y == polygonArray.get(polygonArray.size()-1).y);
        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygonObj = geometryFactory.createPolygon(polygonArray.stream().toArray(Coordinate[]::new));
        // output polygon WKT just for debugging purpose
        System.out.println("Polygon in WKT: " + UMMUtils.getWKT(polygonObj));
        assertTrue(polygonObj.isValid());

    }


    @Test
    public void testRemoveClosingCoordinate() {
        int originalSize = this.rawPolygon.size();
        ArrayList<Coordinate> afterRemoveClosingCoordinate = UMMUtils.removeClosingCoordinate(this.rawPolygon);
        assertTrue(originalSize > afterRemoveClosingCoordinate.size());
        assertEquals(originalSize -1, afterRemoveClosingCoordinate.size());
        // Now we are testing the input is NOT a closing polygon.  i.e.  first coordinate != last coordinate
        this.rawPolygon.remove(this.rawPolygon.size()-1);
        originalSize = this.rawPolygon.size();
        afterRemoveClosingCoordinate = UMMUtils.removeClosingCoordinate(this.rawPolygon);
        assertEquals(originalSize, afterRemoveClosingCoordinate.size());
    }
    @Test
    public void testisGlobalBoundingBox() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(180.00, 90.00));
        coordinates.add(new Coordinate(-180.00, -90.00));
        coordinates.add(new Coordinate(-180.00, 90.00));
        coordinates.add(new Coordinate(180.00, -90.00));
        assertTrue(UMMUtils.isGlobalBoundingBox(coordinates));
        // Add one more coordinate to connect back to the beginning coordinate
        coordinates.add(new Coordinate(180.00, 90.00));
        assertTrue(UMMUtils.isGlobalBoundingBox(coordinates));
        // Test case when we DO NOT have global bunding box
        coordinates.removeAll(coordinates);
        coordinates.add(new Coordinate(179, 90.00));
        coordinates.add(new Coordinate(-180.00, -90.00));
        coordinates.add(new Coordinate(-180.00, 90.00));
        coordinates.add(new Coordinate(180.00, -90.00));
        coordinates.add(new Coordinate(179.00, 90.00));
        assertFalse(UMMUtils.isGlobalBoundingBox(coordinates));
    }

    /**
     *  Test with input : clockwise coordinate array (in WKT format) but desiring counterclockwise coordinate array
     *  expected output : array is reversed.
     * @throws ParseException
     */
    @Test
    public void testEnsureClockwise2CounterclockwisePolygon() throws ParseException {
        String clockwisePolygonWKT = "POLYGON ((45.261678 -65.651128, 73.36379 -62.672926, 92.727136 -55.255283, 104.794012 -45.594717, 112.793698 -34.847018, 119.388686 -21.866727, 124.038596 -10.227008, 128.286371 1.53493, 132.592243 13.300228, 137.440637 24.95787, 143.503681 36.35432, 151.910742 47.204265, 164.708551 56.895658, 180 60.5098565, 180 59.540938499999996, 165.042695 55.953136, 152.515619 46.407947, 144.294922 35.742816, 138.323735 24.488681, 133.515639 12.91638, 129.225468 1.191278, 124.979139 -10.566681, 120.317335 -22.237685, 113.685289 -35.29986, 105.596514 -46.191366, 93.352928 -56.035273, 73.722584 -63.606343, 45.36766 -66.645496, 45.261678 -65.651128))";
        WKTReader wktReader = new WKTReader();
        Geometry geometry = wktReader.read(clockwisePolygonWKT);
        Coordinate[] coordinates = geometry.getCoordinates();
        // the original input array's trailing 3 coordinates will become leading 3 coordinates
        Coordinate[] reversedCoordinates = UMMUtils.ensureOrientation(CGAlgorithms.COUNTERCLOCKWISE, coordinates);
        assertTrue(reversedCoordinates[0].x == Double.valueOf(45.261678) &&
                reversedCoordinates[0].y == Double.valueOf(-65.651128));
        assertTrue(reversedCoordinates[1].x == Double.valueOf(45.36766) &&
                reversedCoordinates[1].y == Double.valueOf(-66.645496));
        assertTrue(reversedCoordinates[2].x == Double.valueOf(73.722584) &&
                reversedCoordinates[2].y == Double.valueOf(-63.606343));
    }

    /**
     *  Test with input : counterclockwise coordinate array with desiring counterclockwise coordinate array
     *  expected output : array is NOT reversed.
     * @throws ParseException
     */
    @Test
    public void testEnsureCounterclockwise2CounterclockwisePolygon() throws ParseException {
        String clockwisePolygonWKT = "POLYGON ((-66.1897 63.1972, -83.1304 57.1864, -94.5228 49.4054, -102.406 40.6845, -108.219 31.4461, -112.822 21.9109, -116.737 12.2069, -120.316 2.41971, -123.831 -7.38306, -127.542 -17.1368, -131.754 -26.7669, -136.888 -36.1707, -143.61 -45.1822, -153.041 -53.498, -166.976 -60.5213, -180 -62.8283, -180 -62.83005, -166.975 -60.5231, -153.039 -53.4995, -143.608 -45.1833, -136.886 -36.1716, -131.752 -26.7677, -127.541 -17.1375, -123.829 -7.3837, -120.314 2.4191, -116.736 12.2063, -112.82 21.9103, -108.217 31.4454, -102.403 40.6837, -94.5204 49.4045, -83.1277 57.1854, -66.187 63.1959, -66.1897 63.1972))";
        WKTReader wktReader = new WKTReader();
        Geometry geometry = wktReader.read(clockwisePolygonWKT);
        Coordinate[] coordinates = geometry.getCoordinates();
        Coordinate[] sameSequenceCoordinates = UMMUtils.ensureOrientation(CGAlgorithms.COUNTERCLOCKWISE, coordinates);
        assertTrue(sameSequenceCoordinates[0].x == Double.valueOf(-66.1897) &&
                sameSequenceCoordinates[0].y == Double.valueOf(63.1972));
        assertTrue(sameSequenceCoordinates[1].x == Double.valueOf(-83.1304) &&
                sameSequenceCoordinates[1].y == Double.valueOf(57.1864));
        assertTrue(sameSequenceCoordinates[2].x == Double.valueOf(-94.5228) &&
                sameSequenceCoordinates[2].y == Double.valueOf(49.4054));
    }

    @Test
    public void testEliminateDuplicates() {
        String clockwisePolygonWKT = "POLYGON ((28.534598 66.28649, 54.363823 61.455936, 71.118225 52.944349, 76.917536 47.994611, 76.984789 47.925301, 85.598984 37.158599, 91.76673 25.830245, 94.320035 20.060693, 94.329517 20.037878, 98.874935 8.339168, 103.097799 -3.424018, 105.230711 -9.296105, 105.226364 -9.284667, 109.817374 -20.91907, 115.35099 -32.31046, 118.751053 -37.872095, 118.707569 -37.811877, 127.541783 -48.316596, 141.265964 -57.552968, 151.078945 -61.288943, 150.984232 -61.263836, 177.481243 -65.48654, -153.271048 -63.7904, -141.305983 -60.899163, -141.071103 -61.871187, -153.036168 -64.762425, 177.53941 -66.484847, 150.82728 -62.251442, 150.723142 -62.223503, 140.910161 -58.487529, 126.984099 -49.146649, 117.943137 -38.456581, 117.897859 -38.393689, 114.497796 -32.832054, 108.917966 -21.356181, 104.296211 -9.651837, 104.290795 -9.63751, 102.157882 -3.765423, 97.933745 8.00129, 93.397408 19.675701, 93.405581 19.656003, 90.852276 25.425555, 84.720781 36.680311, 76.204302 47.300128, 76.268342 47.233988, 70.469031 52.183727, 53.911562 60.56405, 28.351521 65.303391, 28.534598 66.28649))";
        try {
            WKTReader wktReader = new WKTReader();
            Geometry geometry = wktReader.read(clockwisePolygonWKT);
            Coordinate[] coordinates = geometry.getCoordinates();
            ArrayList<Coordinate> beforeShrinkCoordinates = new ArrayList<>(Arrays.asList(coordinates));
            assertTrue(beforeShrinkCoordinates.size() ==49);
            ArrayList<Coordinate> afterShrinkCoordinates = UMMUtils.eliminateDuplicates(beforeShrinkCoordinates);
            assertTrue(afterShrinkCoordinates.size() ==39);
        } catch (Exception e) {
            // force test case to fail if getting exception
            assertTrue(false);
        }

    }

    @Test
    public void testTooClose() {
        String clockwisePolygonWKT = "POLYGON ((28.534598 66.28649, 54.363823 61.455936, 71.118225 52.944349, 76.917536 47.994611, 76.984789 47.925301, 85.598984 37.158599, 91.76673 25.830245, 94.320035 20.060693, 94.329517 20.037878, 98.874935 8.339168, 103.097799 -3.424018, 105.230711 -9.296105, 105.226364 -9.284667, 109.817374 -20.91907, 115.35099 -32.31046, 118.751053 -37.872095, 118.707569 -37.811877, 127.541783 -48.316596, 141.265964 -57.552968, 151.078945 -61.288943, 150.984232 -61.263836, 177.481243 -65.48654, -153.271048 -63.7904, -141.305983 -60.899163, -141.071103 -61.871187, -153.036168 -64.762425, 177.53941 -66.484847, 150.82728 -62.251442, 150.723142 -62.223503, 140.910161 -58.487529, 126.984099 -49.146649, 117.943137 -38.456581, 117.897859 -38.393689, 114.497796 -32.832054, 108.917966 -21.356181, 104.296211 -9.651837, 104.290795 -9.63751, 102.157882 -3.765423, 97.933745 8.00129, 93.397408 19.675701, 93.405581 19.656003, 90.852276 25.425555, 84.720781 36.680311, 76.204302 47.300128, 76.268342 47.233988, 70.469031 52.183727, 53.911562 60.56405, 28.351521 65.303391, 28.534598 66.28649))";
        try {
            Coordinate c1 = new Coordinate();
            c1.x = 150.82728;
            c1.y = -62.251442;
            Coordinate c2 = new Coordinate();
            c2.x = 150.723142;
            c2.y = -62.223503;
            assertTrue(UMMUtils.tooClose(c1, c2, 0.5));
            c1.x = 120.82728;
            c1.y = -62.251442;
            c2.x = 130.723142;
            c2.y = -62.223503;
            assertFalse(UMMUtils.tooClose(c1, c2, 0.5));

        } catch (Exception e) {
            // force test case to fail if getting exception
            assertTrue(false);
        }

    }

}
