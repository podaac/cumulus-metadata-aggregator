package gov.nasa.cumulus.metadata.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import gov.nasa.cumulus.metadata.aggregator.processor.FootprintProcessor;
import gov.nasa.cumulus.metadata.umm.generated.PointType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FootprintProcessorTest {
    String cmrString = "";
    String cmaString = null;
    String fpFileContentString = "";

    @Before
    public void initialize() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File inputCMRJsonFile = new File(classLoader.getResource("20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.cmr.json").getFile());
            cmrString = new String(Files.readAllBytes(inputCMRJsonFile.toPath()));

            File inputCMAJsonFile = new File(classLoader.getResource("cumulus_message_input_example.json").getFile());
            cmaString = new String(Files.readAllBytes(inputCMAJsonFile.toPath()));

            File inputFPFile = new File(classLoader.getResource("20200101000000-JPL-L2P_GHRSST-SSTskin-MODIS_A-D-v02.0-fv01.0.fp").getFile());
            fpFileContentString = new String(Files.readAllBytes(inputFPFile.toPath()));

        } catch (IOException ioe) {
            System.out.println("Test initialization failed: " + ioe);
            ioe.printStackTrace();
        }
    }

    @Test
    public void testGeometryToUMMGWithLineStringFP() {
        try {
            String geoString = "LINESTRING (43.1194 79.8751, 9.8517 81.5042, -23.3044 79.9641, -35.1561 78.2523, -44.6727 75.9396, -52.6218 72.8378, -59.0926 68.8923, -63.958 64.4799, -68.1291 59.1085, -75.2346 44.4007, -81.0492 24.9071, -95.4895 -35.8151, -100.8391 -50.6081, -106.8658 -61.1601, -111.0622 -65.9733, -115.8955 -69.9133, -121.5885 -73.1472, -128.2971 -75.7524, -146.0519 -79.463, -170.2643 -81.2958, 75.7749 21.38, 68.3968 46.232, 64.5215 54.8119, 60.1857 61.6451)";
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(geoString);
            FootprintProcessor processor = new FootprintProcessor();
            String newCMRStr = processor.geometryToUMMG(geometry, cmrString);
            // Use JsonObject instead of model to verify the constructed CMR String
            JsonElement jsonElement = new JsonParser().parse(newCMRStr);
            JsonObject cmrJsonObj = jsonElement.getAsJsonObject();
            JsonArray linesArray= cmrJsonObj.getAsJsonObject("SpatialExtent").getAsJsonObject("HorizontalSpatialDomain")
                    .getAsJsonObject("Geometry").getAsJsonArray("Lines");
            assertTrue(linesArray.isJsonArray());
            assertEquals(1,linesArray.size());
            // check 2nd igem in the array
            JsonArray pointArray = linesArray.get(0).getAsJsonObject().getAsJsonArray("Points");
            assertTrue(pointArray.isJsonArray());
            assertEquals(24,pointArray.size());
            Double latitude = pointArray.get(23).getAsJsonObject().get("Latitude").getAsDouble();
            assertEquals(Double.valueOf(61.6451), latitude);
        } catch (ParseException pe) {
            System.out.println("testGeometryToUMMGWithLineStringFP Error:" + pe);
            pe.printStackTrace();
        }
    }

    @Test
    public void testGeometryToUMMGWithMultiLineStringFP() {
        try {
            String geoString = "MULTILINESTRING ((22.1951 42.2331, 16.0353 20.3693, 4.2922 -30.2658, 0.4187 -42.8917, -3.5888 -52.6411, -9.2351 -61.91, -16.0867 -68.7499, -24.6833 -73.7318, -35.8498 -77.3296, -48.1711 -79.5036, -63.3272 -80.8903, -100.4051 -81.1938, -128.5633 -78.4314, -138.9349 -75.975, -147.2896 -72.6951, -153.1297 -69.1484, -158.0739 -64.7661, -162.373 -59.3209, -166.1594 -52.6401, -172.8224 -34.5864, -180 -5.3166), (180 -5.3166, 170.6866 33.8828, 166.7583 45.8069, 162.5645 55.1002, 157.6929 62.5248, 151.8358 68.3558, 144.7398 72.8036, 135.9875 76.161, 116.584 79.8271, 89.8509 81.4364, 59.6364 80.804, 36.3837 77.9546, 23.1199 74.0524, 13.1563 68.2696, 5.5503 60.04, -0.6038 48.4513))";
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(geoString);
            FootprintProcessor processor = new FootprintProcessor();
            String newCMRStr = processor.geometryToUMMG(geometry, cmrString);
            // Use JsonObject instead of model to verify the constructed CMR String
            JsonElement jsonElement = new JsonParser().parse(newCMRStr);
            JsonObject cmrJsonObj = jsonElement.getAsJsonObject();
            JsonArray linesArray= cmrJsonObj.getAsJsonObject("SpatialExtent").getAsJsonObject("HorizontalSpatialDomain")
                    .getAsJsonObject("Geometry").getAsJsonArray("Lines");
            assertTrue(linesArray.isJsonArray());
            assertEquals(2,linesArray.size());
            // check 2nd igem in the array
            JsonArray pointArray = linesArray.get(1).getAsJsonObject().getAsJsonArray("Points");
            assertTrue(pointArray.isJsonArray());
            assertEquals(16,pointArray.size());
            Double latitude = pointArray.get(15).getAsJsonObject().get("Latitude").getAsDouble();
            assertEquals(Double.valueOf(48.4513), latitude);
        } catch (ParseException pe) {
            System.out.println("testGeometryToUMMGWithMultiLineStringFP Error:" + pe);
            pe.printStackTrace();
        }
    }

    @Test
    public void testGeometryToUMMGWithPolygonFP() {
        try {
            String geoString = "POLYGON ((-42.4563 -54.50084, -53.92347 -58.68337, -67.02517 -61.476, -81.60721 -62.8354, -96.47899 -62.63845, -95.47823 -57.68784, -82.51818 -57.81616, -69.88293 -56.61983, -58.28036 -54.16859, -47.67236 -50.47298, -42.4563 -54.50084))";
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(geoString);
            FootprintProcessor processor = new FootprintProcessor();
            String newCMRStr = processor.geometryToUMMG(geometry, cmrString);
            // Use JsonObject instead of model to verify the constructed CMR String
            JsonElement jsonElement = new JsonParser().parse(newCMRStr);
            JsonObject cmrJsonObj = jsonElement.getAsJsonObject();
            JsonArray gPolygonArray= cmrJsonObj.getAsJsonObject("SpatialExtent").getAsJsonObject("HorizontalSpatialDomain")
                    .getAsJsonObject("Geometry").getAsJsonArray("GPolygons");
            assertTrue(gPolygonArray.isJsonArray());
            assertEquals(1,gPolygonArray.size());
            // check 2nd igem in the array
            JsonArray pointArray = gPolygonArray.get(0).getAsJsonObject().getAsJsonObject("Boundary")
                    .getAsJsonArray("Points");
            assertTrue(pointArray.isJsonArray());
            assertEquals(11,pointArray.size());
            Double latitude = pointArray.get(10).getAsJsonObject().get("Latitude").getAsDouble();
            assertEquals(Double.valueOf(-54.50084), latitude);
        } catch (ParseException pe) {
            System.out.println("testGeometryToUMMGWithPolygonFP Error:" + pe);
            pe.printStackTrace();
        }
    }
    
    @Test
    public void testGeometryToUMMGWithPolygonWithHolesFP() throws ParseException {
        
        String geoString = "POLYGON ((-180 -90, -180 -71.3994, -175.14178 -68.5248, -162.83525 -70" +
         ".22509, -149.612 -70.93377, -135.74252 -70.62437, -123.01198 -69.31587, -117.27825 -73.16094, -110" +
          ".25503 -76.16162, -90.93015 -80.18354, -65.75008 -81.87383, -36.9085 -81.58469, -28.65802 -84.17944, " +
           "-16.544 -85.817, 25.395 -87.315, 61.2565 -86.99682, 85.72546 -85.61375, 98.7547 -83.41988, 106.90735 " +
            "-79.77406, 134.06645 -79.86031, 157.50354 -77.81788, 170.15286 -75.16526, 180 -71.3994, 180 -90, 90 " +
             "-90, 0 -90, -90 -90, -180 -90), (23.84973 -87.30936, 25.02681 -87.31821, 25.103 -87.318, 23.84973 " +
              "-87.30936))";
              
        final WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(geoString);
        FootprintProcessor processor = new FootprintProcessor();
        String newCMRStr = processor.geometryToUMMG(geometry, cmrString);
        // Use JsonObject instead of model to verify the constructed CMR String
        JsonElement jsonElement = new JsonParser().parse(newCMRStr);
        JsonObject cmrJsonObj = jsonElement.getAsJsonObject();
        JsonArray gPolygonArray= cmrJsonObj.getAsJsonObject("SpatialExtent").getAsJsonObject("HorizontalSpatialDomain")
                .getAsJsonObject("Geometry").getAsJsonArray("GPolygons");
        assertTrue(gPolygonArray.isJsonArray());
        assertEquals(1,gPolygonArray.size());
        // Ensure the inner polygon points are no in the resulting point array
        JsonArray pointArray = gPolygonArray.get(0).getAsJsonObject().getAsJsonObject("Boundary")
                .getAsJsonArray("Points");
        assertTrue(pointArray.isJsonArray());
        assertEquals(28,pointArray.size());
        // Ensure the inner polygon points are in the exclusion zone point array
        JsonArray exclusionZone = gPolygonArray.get(0).getAsJsonObject().getAsJsonObject("ExclusiveZone").getAsJsonArray("Boundaries").get(0).getAsJsonObject().getAsJsonArray("Points");
        assertEquals(4,exclusionZone.size());
    }

    @Test
    public void testGeometryToUMMGWithMultiPolygonFP() {
        try {
            String geoString = "MULTIPOLYGON (((-111.9544 -0.9509, -116.2529 16.2903, -120.6858 29.9048, -125.5866 40.9519, -131.168 49.8449, -139.5216 58.514, -150.0154 64.973, -163.1201 69.4971, -180 72.3943, -180 89.1358, -150.1221 88.771, -129.4545 87.6218, -119.9373 85.5193, -114.5003 81.4352, -111.5117 75.5964, -108.8645 66.0564, -96.6588 2.4989, -111.9544 -0.9509)), ((180 72.3943, 165.3457 73.361, 150.3927 73.2403, 135.8107 72.008, 123.6451 69.8473, 113.6286 66.8634, 104.9988 62.8624, 97.8406 57.8898, 91.8021 51.8071, 83.6725 38.999, 76.7941 21.2214, 69.7472 -6.9187, 57.4107 -71.5836, 53.9102 -80.8553, 49.0822 -85.3613, 40.0903 -87.6821, 20.5092 -88.8541, -89.5338 -88.7732, -103.5059 -87.9975, -112.2627 -86.5393, -117.4931 -83.9796, -120.8295 -79.6801, -125.2427 -64.7844, -137.7474 1.0682, -122.4402 4.5183, -115.4475 -22.4797, -107.6618 -41.4253, -103.1775 -48.6444, -97.9605 -54.8336, -92.1886 -59.789, -85.4903 -63.8905, -77.9391 -67.127, -68.7925 -69.7764, -46.7842 -72.8784, -21.3213 -72.9948, 1.7328 -69.9572, 15.8856 -65.4674, 27.0474 -58.9437, 35.7824 -50.1551, 43.0533 -38.1672, 48.8665 -23.4444, 54.1664 -4.3334, 69.0936 70.7681, 71.9396 78.7813, 75.5919 83.5292, 83.0052 86.7315, 97.9919 88.36, 180 89.1358, 180 72.3943)))";
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(geoString);
            FootprintProcessor processor = new FootprintProcessor();
            String newCMRStr = processor.geometryToUMMG(geometry, cmrString);
            // Use JsonObject instead of model to verify the constructed CMR String
            JsonElement jsonElement = new JsonParser().parse(newCMRStr);
            JsonObject cmrJsonObj = jsonElement.getAsJsonObject();
            JsonArray gPolygonArray= cmrJsonObj.getAsJsonObject("SpatialExtent").getAsJsonObject("HorizontalSpatialDomain")
                    .getAsJsonObject("Geometry").getAsJsonArray("GPolygons");
            assertTrue(gPolygonArray.isJsonArray());
            assertEquals(2,gPolygonArray.size());
            // check 2nd igem in the array
            JsonArray pointArray = gPolygonArray.get(1).getAsJsonObject().getAsJsonObject("Boundary")
                    .getAsJsonArray("Points");
            assertTrue(pointArray.isJsonArray());
            assertEquals(49,pointArray.size());
            Double latitude = pointArray.get(48).getAsJsonObject().get("Latitude").getAsDouble();
            assertEquals(Double.valueOf(72.3943), latitude);
        } catch (ParseException pe) {
            System.out.println("testGeometryToUMMGWithMultiPolygonFP Error:" + pe);
            pe.printStackTrace();
        }
    }

    @Test
    public void testGetPointArray() {
        try {
            String geoString = "POLYGON ((-42.4563 -54.50084, -53.92347 -58.68337, -67.02517 -61.476, -81.60721 -62.8354, -96.47899 -62.63845, -95.47823 -57.68784, -82.51818 -57.81616, -69.88293 -56.61983, -58.28036 -54.16859, -47.67236 -50.47298, -42.4563 -54.50084))";
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(geoString);
            FootprintProcessor processor = new FootprintProcessor();
            List<PointType> points =  processor.getPointArray(geometry);
            points.size();
            assertEquals(11, points.size());
            assertEquals(Double.valueOf(-42.4563), points.get(10).getLongitude());
        } catch (ParseException pe) {
            System.out.println("testGetPointArray Error:" + pe);
            pe.printStackTrace();
        }
    }

    @Test
    public void testGetStringByType() {
        FootprintProcessor processor = new FootprintProcessor();
        String fpStr =  processor.getStringByType(fpFileContentString,"FOOTPRINT");
        String extentStr =  processor.getStringByType(fpFileContentString,"EXTENT");
        fpStr.getBytes(StandardCharsets.UTF_8);
        extentStr.getBytes();
        assertTrue(StringUtils.startsWith(fpStr, "MULTIPOLYGON"));
        assertTrue(StringUtils.startsWith(extentStr, "POLYGON"));
    }

    @Test
    public void testCreateOutputMessage() {
        FootprintProcessor processor = new FootprintProcessor();
        String output =  processor.createOutputMessage(cmaString, 334411,
                new BigInteger("3244"), "granuleId-3344-22.cmr.json", "my-private",
                "CMR", "collectionName");
        JsonElement jsonElement = new JsonParser().parse(output);
        JsonArray granules = jsonElement.getAsJsonObject().get("output").getAsJsonArray();
        JsonArray files = granules.get(0).getAsJsonObject().get("files").getAsJsonArray();
        JsonObject foundFP =  processor.getFileJsonObjByFileTrailing(files, ".fp");
        assertEquals(null, foundFP);
        JsonObject foundCMR =  processor.getFileJsonObjByFileTrailing(files, ".cmr.json");
        Long  cmrFileSize =  foundCMR.get("size").getAsLong();
        BigInteger  revisionId =  jsonElement.getAsJsonObject().get("cmrRevisionId").getAsBigInteger();
        assertEquals(334411, cmrFileSize.longValue());
        assertEquals(revisionId.compareTo(new BigInteger("3244")), 0);
    }
}
