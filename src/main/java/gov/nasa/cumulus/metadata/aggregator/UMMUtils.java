package gov.nasa.cumulus.metadata.aggregator;

import java.util.ArrayList;

import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.io.WKTWriter;
import gov.nasa.podaac.inventory.model.Dataset;
import gov.nasa.podaac.inventory.model.DatasetCitation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import static org.apache.commons.lang3.ArrayUtils.reverse;

public class UMMUtils {
    private static Log log = LogFactory.getLog(UMMUtils.class);
    protected static double splitMargin = 179.5d;

    /**
     * Split a list of coordinate into separate lists of coordinates. A coordinate is split into a separate list if
     * the coordinate differs wildly from the previous coordinate. For example, if one coordinate longitude is -175,
     * and the next coordinate longitude is 175, it can be assumed that those values represent a split.
     *
     * @param coordinateList The list of coordinates to be split into multiple lists of coordinates
     * @return A list of lists of coordinates, where each list represents a split.
     */
    public static List<List<Coordinate>> split(List<Coordinate> coordinateList) {
        List<List<Coordinate>> splitCoordList = new ArrayList<>();
        if (coordinateList == null) {
            log.warn("Unable to split given null coordinate list. Returning empty split.");
            return splitCoordList;
        }

        double margin = splitMargin;
        Coordinate prevCoord = null;
        Coordinate currentCoord = null;

        boolean lastInMargin = false;
        boolean firstInMargin = false;
        List<Coordinate> coords = new ArrayList<>();

        for (int i = 0; i < coordinateList.size(); i++) {
            currentCoord = coordinateList.get(i);

            // Check if the first point in the list of coordinates is within the margin
            // If so, continue to the next iteration of the loop
            if (Math.abs(currentCoord.x) > margin) {
                if (i == 0)
                    firstInMargin = true;
                lastInMargin = true;
                continue;
            }
            lastInMargin = false;

            // If the first point in the list was within the margin, add a new point
            // to the list with lon -180/180
            if (firstInMargin) {
                if (currentCoord.x < 0) {
                    coords.add(new Coordinate(-180, currentCoord.y));
                } else {
                    coords.add(new Coordinate(180, currentCoord.y));
                }
                // Set firstInMargin to false, so this code will only ever execute once (if ever)
                firstInMargin = false;
            }

            if (prevCoord == null) {
                prevCoord = currentCoord;
                coords.add(currentCoord);
            } else {
                Coordinate lastCoord = coords.get(coords.size() - 1);
                double yAverage = (lastCoord.y + currentCoord.y) / 2;
                // If two points appear that are wildly different from one another, consider that to be a 'split'.
                // Add the existing coordinates to the list of coordinate lists, and start a new coordinate
                // list for the current split.
                if (prevCoord.x > 150 && currentCoord.x < -150) {
                    log.debug("SPLIT!");
                    // Add a new coord with max lon, and with lat equal to the average between the current
                    // and previous lat.
                    coords.add(new Coordinate(180d, yAverage));
                    splitCoordList.add(coords);
                    coords = new ArrayList<>();
                    // Add a new coord with min lon, and with lat equal to the average between the current
                    // and previous lat.
                    coords.add(new Coordinate(-180d, yAverage));
                } else if (currentCoord.x > 150 && prevCoord.x < -150) {
                    log.debug("SPLIT!");
                    // Add a new coord with min lon, and with lat equal to the average between the current
                    // and previous lat.
                    coords.add(new Coordinate(-180d, yAverage));
                    splitCoordList.add(coords);
                    coords = new ArrayList<>();
                    // Add a new coord with max lon, and with lat equal to the average between the current
                    // and previous lat.
                    coords.add(new Coordinate(180d, yAverage));
                }
                coords.add(currentCoord);
                prevCoord = currentCoord;
            }
        }
        if (lastInMargin) {
            log.trace("Last coordinate was in margin or NaN...");
            if (prevCoord == null) {
                return splitCoordList;
            }
            if (prevCoord.x > 0) {
                coords.add(new Coordinate(180, currentCoord.y));
            } else {
                coords.add(new Coordinate(-180, currentCoord.y));
            }
        }

        splitCoordList.add(coords);
        return splitCoordList;
    }

    public static String getWKT(Geometry geometry) {
        WKTWriter writer = new WKTWriter();
        return writer.write(geometry);
    }

    public static ArrayList<Coordinate> closeUp(ArrayList<Coordinate> group) {
        int groupSize = group.size();
        if(group.get(0).x != group.get(groupSize-1).x || group.get(0).y != group.get(groupSize-1).y) {
            group.add(group.get(0));
        }
        return group;
    }

    /**
     * If the last coordinate is the same as first coordinate, then remove the last coordinate
     * The input Arraylist should NOT be damaged/(or size change) after this operation
     * @param group
     * @return
     */
    public static ArrayList<Coordinate> removeClosingCoordinate(ArrayList<Coordinate> group) {
        ArrayList<Coordinate> newGroup = (ArrayList<Coordinate>) group.clone();
        int newGroupSize = newGroup.size();
        if(newGroup.get(0).x == newGroup.get(newGroupSize-1).x && newGroup.get(0).y == newGroup.get(newGroupSize-1).y) {
            newGroup.remove(newGroupSize -1);
        }
        return newGroup;
    }

    /**
     * input line string format
     * long lat long lat long lat
     * ie, each long/lat are divided by spaces
     * @param line
     * @return  Array of Coordinate objects
     */
    public static ArrayList<Coordinate> lineString2Coordinates(String line) {
        String[] lineArray = line.split(" ");

        Double currLon = null;
        Double currLat = null;
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        // UMMUtils.split function can split a line to multiple lines.
        // Hence, we are creating a coordinates of a line in this for loop
        for (int i = 0; i < lineArray.length; i += 2) {
            Double lon = Double.parseDouble(lineArray[i + 1]);
            if (lon > 180.0)
                lon = lon - 360.0;
            Double lat = Double.parseDouble(lineArray[i]);

            if (!(lon.equals(currLon) && lat.equals(currLat))) {
                Coordinate c = new Coordinate();
                c.x = lon;
                c.y = lat;
                coordinates.add(c);
            }
            currLon = lon;
            currLat = lat;
        }
        return coordinates;
    }
    public static ArrayList<Coordinate> reconstructPolygonsOver2Lines(ArrayList<Coordinate> group1, ArrayList<Coordinate> group2) {
        // re-construct 1st and 3rd to a polygon.
        // the 2nd group is a polygon itself.
        UMMUtils ummUtils = new UMMUtils();
        group1 = ummUtils.removeClosingCoordinate(group1);
        group2 = ummUtils.removeClosingCoordinate(group2);
        // concate 2 lineStrings.
        int i =0, group2Size = group2.size();
        for(i=0; i<group2Size -1; i ++) {
            group1.add(group2.get(i));
        }

        group1 = ummUtils.closeUp(group1);
        return group1;
    }

    /**
     * Input a list of coordinates and determine if they represent a global bounding box
     * the size ==3 or size ==5 logic is because the input could be 4 corners or
     * 4 corners + last coordinate connect to the first
     * @param coordinates
     * @return
     */
    public static boolean isGlobalBoundingBox(ArrayList<Coordinate> coordinates) {
        if( (coordinates.size()==4 || coordinates.size() ==5)
            &&
            coordinates.stream().filter(a -> (a.x == 180.0 && a.y==90.0)).findAny().orElse(null)!=null
            &&
            coordinates.stream().filter(a -> (a.x == 180.0 && a.y==-90.0)).findAny().orElse(null)!=null
            &&
            coordinates.stream().filter(a -> (a.x == -180.0 && a.y==-90.0)).findAny().orElse(null)!=null
            &&
            coordinates.stream().filter(a -> (a.x == -180.0 && a.y==90.0)).findAny().orElse(null)!=null
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function is to take a counterclockwise or clockwise sequence indicator and translate the input coordination
     * array based on the desired orientation.
     * @param desiredOrientation : CGAlgorithms.COUNTERCLOCKWISE  or  CGAlgorithms.CLOCKWISE
     * @param coord  an array of coordinates which is a valid geometry
     * @return  an array of coordinates in the desired counterclockwise or clockwise sequence
     */
    public static Coordinate[] ensureOrientation(
            final int desiredOrientation, final Coordinate... coord) {
        if (coord.length == 0) {
            return coord;
        }
        final int orientation = CGAlgorithms.isCCW(coord) ? CGAlgorithms.COUNTERCLOCKWISE
                : CGAlgorithms.CLOCKWISE;

        if (orientation != desiredOrientation) {
            final Coordinate[] reverse = coord.clone();
            reverse(reverse);

            return reverse;
        }
        return coord;
    }
    public static ArrayList<Coordinate> eliminateDuplicates(ArrayList<Coordinate> inputCoordinates) {
        int size = inputCoordinates.size();
        ArrayList<Coordinate> removeCandidateCoordinates = new ArrayList<>();
        int i = 0; size = inputCoordinates.size();
        /**
         * The purpose of this loop is to create a list of to-be removed objects
         */
        while(i < (size -2)) {
            Coordinate currCoordinate = inputCoordinates.get(i);
            Coordinate nextCoordinate = inputCoordinates.get((i+1));  // to do , how about larger than size -1
            if(tooClose(currCoordinate, nextCoordinate , 0.5)) {
                removeCandidateCoordinates.add(nextCoordinate);
            }
            i++;
        }
        i=0; size = removeCandidateCoordinates.size();
        for(i=0;i<size;i++) {
            inputCoordinates.remove(removeCandidateCoordinates.get(i));
        }
        return inputCoordinates;
    }

    /**
     * This is a function to determine if 2 coordinates are too close.  Using simple algorithm now.
     * can be replaced by other more complicated distancing algorithm.
     * @param coordinate1
     * @param coordinate2
     * @param threshhold
     * @return
     */
    public static boolean tooClose(Coordinate coordinate1, Coordinate coordinate2, double threshhold) {
        if(Math.abs(coordinate1.x-coordinate2.x) <= threshhold && Math.abs(coordinate1.y -coordinate2.y) <=threshhold ) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDatasetVersion(Dataset dataset) {
        Set<DatasetCitation> datasetCitationSet = dataset.getCitationSet();
        // Bad patch for now. Currently, there seems to be a one-to-one relationship, but the schema is many-to-one.
        // Verify how/where the the primary citation for a dataset is indicated and use that one.
        DatasetCitation datasetCitation = datasetCitationSet.iterator().next();
        String version = datasetCitation.getVersion();

        return version;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.equals("null");
    }

    public static boolean notNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static Double longitudeTypeNormalizer(Double value) {
        value = value < -180 ? -180 : value;
        value = value > 180 ? value - 360 : value;
        return value;
    }

}

