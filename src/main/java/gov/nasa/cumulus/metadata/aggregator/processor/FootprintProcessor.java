package gov.nasa.cumulus.metadata.aggregator.processor;

import com.google.gson.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGCollectionAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGListAdapter;
import gov.nasa.cumulus.metadata.umm.adapter.UMMGMapAdapter;
import gov.nasa.cumulus.metadata.umm.generated.*;
import gov.nasa.cumulus.metadata.umm.model.GeometryTypeEnum;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;


/**
 * Example footprint file content (downloaded from bucket where footprint is written by forge)
 * {
 * "FOOTPRINT": "MULTIPOLYGON (((-51.7632 -70.3523, -45.4648 -76.2323, -39.17 -79.036, -170.2334 -76.7375, -148.5898 -76.7746, -129.0236 -75.0546, -113.865 -71.8159, -102.1022 -66.9065, -91.0256 -69.1905, -78.6429 -70.5453, -63.7206 -70.9887, -51.7632 -70.3523)), ((123.572 -87.28, 123.812 -87.283, 123.3883 -87.2863, 123.572 -87.28)))",
 * "EXTENT": "POLYGON ((-170.2334 -87.2863, -170.2334 -66.9065, 123.812 -66.9065, 123.812 -87.2863, -170.2334 -87.2863))"
 * }
 * <p>
 * The purpose of this class is mainly
 * download the cmr file
 * download the footprint file
 * read footprint string from footprint file, decode the WKT and append footprint coordinates to cmr.json file
 * finally post cmr.json file back to its original bucket location
 */

public class FootprintProcessor extends ProcessorBase{
    private final String className = this.getClass().getName();

    public static boolean isFootprintFileExisting(String input) {
        JsonObject inputKey = JsonParser.parseString(input).getAsJsonObject();
        JsonArray granules = inputKey.getAsJsonArray("input");
        JsonObject granule = granules.get(0).getAsJsonObject();
        JsonArray files = granule.get("files").getAsJsonArray();
        for (JsonElement f : files) {
            if (StringUtils.endsWith(
                    StringUtils.trim(
                            StringUtils.lowerCase(f.getAsJsonObject().get("fileName").getAsString())
                    ) // end of StringUtils.trim
                    , ".fp")) {
                AdapterLogger.LogInfo("Found footprint file in files array");
                return true;
            }
        }
        return false;
    }

    public String process(String input, String ummgStr, String region, String revisionId)
            throws IOException, ParseException {
        try {
            String cmrBucket = System.getenv().getOrDefault("INTERNAL_BUCKET", "");
            String cmrDir = System.getenv().getOrDefault("CMR_DIR", "");
            AdapterLogger.LogDebug(this.className + " internal bucket: " + cmrBucket + " CMR Dir: " + cmrDir);
            this.region = region;
            decodeVariables(input);
            this.workingDir = createWorkDir();
            JsonObject FPFileJsonObj = getFileJsonObjByFileTrailing(this.files, ".fp");
            String fpFileLocalFullPath = downloadFile(FPFileJsonObj);
            String newCMRStr = appendFootPrint(fpFileLocalFullPath, ummgStr);
            String cmrFileName = buildCMRFileName(this.granuleId, this.executionId);
            long cmrFileSize = uploadCMRJson(cmrBucket, cmrDir, this.collectionName, cmrFileName, newCMRStr);
            // Create the eTag for CMR file.
            String cmrETag = s3Utils.getS3ObjectETag(this.region, cmrBucket,
                    Paths.get(cmrDir, this.collectionName,
                            cmrFileName).toString());
            AdapterLogger.LogDebug(this.className + " cmr.json file size: " + cmrFileSize);
            String output = createOutputMessage(input, cmrFileSize, new BigInteger(revisionId),
                    cmrFileName,
                    cmrBucket, cmrDir, this.collectionName);
            // delete fp file from S3 and clean up local working directory
            s3Utils.delete(this.region, FPFileJsonObj.get("bucket").getAsString(),
                    FPFileJsonObj.get("key").getAsString());
            return output;
        } catch (IOException | ParseException e) {
            AdapterLogger.LogError("Footprint processor exception:" + e);
            throw e;
        } finally {
            deleteWorkDir(this.workingDir);
        }
    }

    /**
     * footprint WKT (Well-Known Text) represents MultiPolygon, Polygon MultilineString and Linestring
     * We will be using vividsolutions JTS library to transfer the WKT to datastructure.
     * <p>
     * Hence the getGeometryType API could return the following strings (vividsolutions did not implement them as
     * enum, sorry).
     * LineString
     * MultiLineString
     * Polygon
     * MultiPolygon
     * <p>
     * Used self implemented GeometryTypeEnum in this class
     */
    public String appendFootPrint(String fpFileLocalFullPath, String cmrStr)
            throws IOException, ParseException {
        try {
            File fpFile = new File(fpFileLocalFullPath);
            String fpStr = FileUtils.readFileToString(fpFile, Charset.defaultCharset());
            fpStr = getStringByType(fpStr, "FOOTPRINT");
            AdapterLogger.LogDebug("Read in footprint string: " + fpStr);
            final WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(fpStr);
            return geometryToUMMG(geometry, cmrStr);

        } catch (IOException | ParseException ioe) {
            AdapterLogger.LogFatal(this.className + " reading footprint file error:" + ioe);
            throw ioe;
        }
    }

    /**
     * fotprint file content is a json string including 2 keys
     * FOOTPRINT:
     * EXTENT:
     *
     * @param fpFileContent the content of Footprint file
     * @param type          either FOOTPRINT or EXTENT
     * @return A FOOTPRINT string or an EXTENT string
     */
    public String getStringByType(String fpFileContent, String type) {
        JsonObject fpJsonObj = JsonParser.parseString(fpFileContent).getAsJsonObject();
        return fpJsonObj.get(type).getAsString();
    }

    /**
     * Example LineString : LINESTRING (30 10, 10 30, 40 40)
     * MultiLineString: MULTILINESTRING ((10 10, 20 20, 10 40),
     * (40 40, 30 30, 40 20, 30 10))
     *
     * @param geometry
     * @param cmrStr   cmr json string read from cmr file
     */
    public String geometryToUMMG(Geometry geometry, String cmrStr) {
        Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeHierarchyAdapter(Collection.class, new UMMGCollectionAdapter())
                .registerTypeHierarchyAdapter(List.class, new UMMGListAdapter())
                .registerTypeHierarchyAdapter(Map.class, new UMMGMapAdapter())
                .create();

        JsonObject cmrJsonObj = JsonParser.parseString(cmrStr).getAsJsonObject();
        JsonObject spatialExtentJsonObj = cmrJsonObj.getAsJsonObject("SpatialExtent");
        SpatialExtentType spatialExtentType = spatialExtentJsonObj != null
                ? gsonBuilder.fromJson(gsonBuilder.toJson(spatialExtentJsonObj), SpatialExtentType.class)
                : new SpatialExtentType();

        GeometryType geometryType = spatialExtentType.getHorizontalSpatialDomain().getGeometry();
        geometryType.getGPolygons().clear();
        geometryType.getLines().clear();

        LinkedHashSet<LineType> lineTypes = new LinkedHashSet<>();
        LinkedHashSet<GPolygonType> gPolygonTypes = new LinkedHashSet<>();

        processGeometryRecursively(geometry, lineTypes, gPolygonTypes);

        if (!lineTypes.isEmpty()) geometryType.setLines(lineTypes);
        if (!gPolygonTypes.isEmpty()) geometryType.setGPolygons(gPolygonTypes);

        spatialExtentType = removeBBX(spatialExtentType);
        String spatialExtentStr = gsonBuilder.toJson(spatialExtentType);
        AdapterLogger.LogInfo(this.className + " SpatialExtent:" + spatialExtentStr);

        cmrJsonObj.add("SpatialExtent", gsonBuilder.toJsonTree(spatialExtentType).getAsJsonObject());
        String outputCMRStr = gsonBuilder.toJson(cmrJsonObj);
        AdapterLogger.LogInfo(this.className + " CMR String after appending Footprint:" + spatialExtentStr);

        return outputCMRStr;
    }

    /**
     *
     * @param geometry : input geometry
     * @param lineTypes : Output lineType Array, in case input geometry is single line or multiple line types
     * @param gPolygonTypes : Output gPolygonType array in case input geometry is MultiPolygon or Polygon
     *
     *  Since java can not return multiple parameters.  We are coding in a way to make 2 input parameters to carry
     *                      the output
     */
    private void processGeometryRecursively(Geometry geometry, LinkedHashSet<LineType> lineTypes,
                                            LinkedHashSet<GPolygonType> gPolygonTypes) {
        String geometryTypeStr = geometry.getGeometryType();

        if (StringUtils.equalsIgnoreCase(geometryTypeStr, GeometryTypeEnum.LineString.toString())) {
            // Process single LineString
            LineType lineType = new LineType();
            List<PointType> pointTypes = getPointArray(geometry);
            lineType.setPoints(pointTypes);
            lineTypes.add(lineType);

        } else if (StringUtils.equalsIgnoreCase(geometryTypeStr, GeometryTypeEnum.MultiLineString.toString())) {
            // Process each LineString in MultiLineString
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                processGeometryRecursively(geometry.getGeometryN(i), lineTypes, gPolygonTypes);
            }

        } else if (StringUtils.equalsIgnoreCase(geometryTypeStr, GeometryTypeEnum.Polygon.toString())) {
            // Process single Polygon
            GPolygonType gPolygonType = createGPolygonType((Polygon) geometry);
            gPolygonTypes.add(gPolygonType);

        } else if (StringUtils.equalsIgnoreCase(geometryTypeStr, GeometryTypeEnum.MultiPolygon.toString())) {
            // Process each Polygon in MultiPolygon
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                processGeometryRecursively(geometry.getGeometryN(i), lineTypes, gPolygonTypes);
            }
        } else {
            AdapterLogger.LogFatal(this.className + " Severe Problem: Footprint is not LineString, " +
                    "MultiLineString, Polygon, or MultiPolygon");
        }
    }

    private GPolygonType createGPolygonType(Polygon polygon) {
        GPolygonType gPolygonType = new GPolygonType();

        // Set Exterior Ring
        List<PointType> exteriorPoints = getPointArray(polygon.getExteriorRing());
        BoundaryType boundaryType = new BoundaryType();
        boundaryType.setPoints(exteriorPoints);
        gPolygonType.setBoundary(boundaryType);

        // Set Interior Rings (if any)
        if (polygon.getNumInteriorRing() > 0) {
            ExclusiveZoneType exclusiveZoneType = new ExclusiveZoneType();
            List<BoundaryType> interiorBoundaries = new ArrayList<>();

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                LineString interiorRing = polygon.getInteriorRingN(i);
                List<PointType> interiorPoints = getPointArray(interiorRing);
                BoundaryType interiorBoundary = new BoundaryType();
                interiorBoundary.setPoints(interiorPoints);
                interiorBoundaries.add(interiorBoundary);
            }
            exclusiveZoneType.setBoundaries(interiorBoundaries);
            gPolygonType.setExclusiveZone(exclusiveZoneType);
        }

        return gPolygonType;
    }

    private SpatialExtentType removeBBX(SpatialExtentType spatialExtentType) {
        // SpqtialExtent requires (could be none of the following):
        //     "anyOf": [{
        //        "required": ["GranuleLocalities"]
        //      }, {
        //        "required": ["HorizontalSpatialDomain"]
        //      }, {
        //        "required": ["VerticalSpatialDomains"]GranuleLocalities
        //////   and
        // HorizontalSpatialDomain requires
        // "oneOf": [{
        //        "required": ["Geometry"]
        //      }, {
        //        "required": ["Orbit"]
        //      }]
        /// and
        // Geometry requires
        // "anyOf": [{
        //        "required": ["Points"]
        //      }, {
        //        "required": ["BoundingRectangles"]
        //      }, {
        //        "required": ["GPolygons"]
        //      }, {
        //        "required": ["Lines"]
        //      }]
        HorizontalSpatialDomainType horizontalSpatialDomainType = spatialExtentType.getHorizontalSpatialDomain();
        if(horizontalSpatialDomainType != null) {
            GeometryType geometryType = horizontalSpatialDomainType.getGeometry();
            if (geometryType !=null) {
                // Either GPolygon has greater than 0 appearance or Lines has greater than 0 appearance
                // then remove the BoundRectangles.
                Set<GPolygonType> gPolygonTypeSet = geometryType.getGPolygons();
                Set<LineType> lineTypeSet = geometryType.getLines();
                if((gPolygonTypeSet!=null && !gPolygonTypeSet.isEmpty())
                    ||
                    (lineTypeSet!=null && !lineTypeSet.isEmpty())) {
                    //since gPolygon is not null or an empty set them remove bounding rectangle here
                    Set<BoundingRectangleType> boundingRectangleTypeSet = geometryType.getBoundingRectangles();
                    if (boundingRectangleTypeSet!=null && !boundingRectangleTypeSet.isEmpty()) {
                        boundingRectangleTypeSet.clear();
                    }
                }
            }
        }
        return spatialExtentType;
    }

    /**
     * A single geometry
     *
     * @param geometry
     */
    public List<PointType> getPointArray(Geometry geometry) {
        List<PointType> points = new ArrayList<>();
        Coordinate[] coordinates;

        if (geometry instanceof LineString) {
            coordinates = geometry.getCoordinates();
        } else if (geometry instanceof Polygon) {
            coordinates = ((Polygon) geometry).getExteriorRing().getCoordinates();
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }

        // Check if coordinates are in clockwise order and reverse if needed
        if (coordinates.length > 3) {
            if (!CGAlgorithms.isCCW(coordinates)) {
                coordinates = reverseCoordinates(coordinates);
            }
        }

        for (Coordinate coordinate : coordinates) {
            PointType point = new PointType();
            point.setLongitude(coordinate.x);
            point.setLatitude(coordinate.y);
            points.add(point);
        }
        return points;
    }

    private Coordinate[] reverseCoordinates(Coordinate[] coordinates) {
        Coordinate[] reversed = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            reversed[i] = coordinates[coordinates.length - 1 - i];
        }
        return reversed;
    }

    /**
     * A single geometry
     *
     * @param geometry
     */
    public List<PointType> getReversedPointArray(Geometry geometry) {
        List<PointType> points = new ArrayList<>();
        Coordinate[] coordinates = ((Polygon)geometry).getExteriorRing().getCoordinates();
        int size = coordinates.length;
        for (int j = size - 1; j > -1; j--) {
            PointType p = new PointType();
            p.setLongitude(Double.valueOf(coordinates[j].x));
            p.setLatitude(Double.valueOf(coordinates[j].y));
            points.add(p);
        }
        return points;
    }

    public ExclusiveZoneType getExclusiveZones(Geometry geometry) {

        List<BoundaryType> boundaries = new ArrayList<>();
        Geometry hole = null;

        for (int i = 0; i < ((Polygon)geometry).getNumInteriorRing(); i++) {

            List<PointType> points = new ArrayList<>();
            BoundaryType boundary = new BoundaryType();

            hole = ((Polygon)geometry).getInteriorRingN(i);
            Coordinate[] coordinates = hole.getCoordinates();
            int size = coordinates.length;

            for (int j = 0; j < size; j++) {
                PointType p = new PointType();
                p.setLongitude(Double.valueOf(coordinates[j].x));
                p.setLatitude(Double.valueOf(coordinates[j].y));
                points.add(p);
            }

            boundary.setPoints(points);
            boundaries.add(boundary);
        }
        ExclusiveZoneType exclusiveZoneType = new ExclusiveZoneType();
        exclusiveZoneType.setBoundaries(boundaries);
        return exclusiveZoneType;
    }


}
