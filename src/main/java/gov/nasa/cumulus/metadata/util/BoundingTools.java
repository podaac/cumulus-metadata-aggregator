package gov.nasa.cumulus.metadata.util;

import gov.nasa.cumulus.metadata.umm.generated.BoundingRectangleType;
import gov.nasa.cumulus.metadata.umm.generated.GeometryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BoundingTools {

    private static Log log = LogFactory.getLog(BoundingTools.class);

    public static BigDecimal convertBoundingVal(Double value, boolean lon) {
        return convertBoundingVal(BigDecimal.valueOf(value), lon);
    }

    /**
     * Checks to see if a given string can be parsed into a double value.
     * @param input The string to try and parse
     * @return      True if the string can be parsed into a double without
     *              throwing an exception, otherwise False,
     */
    public static boolean isParseable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Checks to see if all provided string values can be parsed into
     * double values.
     *
     * @param input The list of strings to try and parse
     * @return      True if all provided strings can be parsed
     *              into double values without an exception,
     *              otherwise False.
     */
    public static boolean allParsable(String... input) {
        for (String s : input) {
            if (!isParseable(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that all of the specified coordinates fall within at least one of the
     * two valid ranges for bounding rectangles; either 0 to 360, or -180 to 180.
     * <br>
     * If any of the values are outside of the maximum possible range of -180 to +360
     * then return true to let the caller know that the values are likely invalid.
     * <br><br>
     * @param coords    the values to check, all in Double format
     * @return          true if any value is invalid, false if all are valid
     */
    public static boolean coordsInvalid(Double... coords) {
        for (Double d : coords) {
            // the two bounding ranges are 0 to 360, and -180 to +180
            // so if we have a value out side of that range, something is wrong
            if (d < -180.0 || d > 360.0) {
                // return true, indicating the coordinates are invalid/bad
                log.info("Found invalid value: " + d);
                return true;
            }
        }
        // if we get all the way through the loop, then all the values
        // provided were 'valid' i.e. between -180.0 and +360.0
        return false;
    }

    public static BigDecimal convertBoundingVal(BigDecimal value, boolean lon) {
        double val = value.doubleValue();

        double convertedVal = 0;
        if (lon) {
            if (val > 180) {
                convertedVal = val - 360;
            } else {
                convertedVal = val;
            }
        } else {
            if (val > 90) {
                convertedVal = val - 90;
            } else if (val < -90) {
                convertedVal = val + 90;
            } else {
                convertedVal = val;
            }
        }
        if(val != convertedVal)
            log.warn("Collection has longitude range of 0-360. Performing Automated conversion to -180/180 range. Converted value ["+val+"] to ["+convertedVal+"]");

        BigDecimal newVal = BigDecimal.valueOf(convertedVal);
        newVal = newVal.setScale(3, RoundingMode.HALF_UP);

        return newVal;
    }

    public static List<BoundingRectangleType> genRectangle(boolean rangeIs360, double east, double west, double north,
                                                       double south) {
        List<BoundingRectangleType> rectsList = new ArrayList<>();
        BoundingRectangleType br = new BoundingRectangleType();

        BoundingRectangleType br2;

        if (rangeIs360) {
            BigDecimal bdeast = convertBoundingVal(east,true);
            BigDecimal bdwest = convertBoundingVal(west,true);

            /*
             * If special case for 0 and 360
             */
            //System.out.println("EAST " + east);
            //System.out.println("WEST " + west);
            if (east == 360d && west==0d) {
                //System.out.println("**** SPECIAL CASE ****");
                br.setWestBoundingCoordinate(-180.0);
                br.setEastBoundingCoordinate(180.0);
                //don't need to convert the lat ones, they are all in the -90/90 range
                br.setNorthBoundingCoordinate(north);
                br.setSouthBoundingCoordinate(south);
                rectsList.add(br);
                return rectsList;
            }

            if (bdeast.compareTo(bdwest) == 1) {
                br.setWestBoundingCoordinate(convertBoundingVal(west, true).doubleValue());
                br.setEastBoundingCoordinate(convertBoundingVal(east, true).doubleValue());
                //don't need to convert the lat ones, they are all in the -90/90 range
                br.setNorthBoundingCoordinate(north);
                br.setSouthBoundingCoordinate(south);

                rectsList.add(br);
            } else {
                log.debug("East Longitude is less than West Longitude: separating bounding boxes.");
                br2 = new BoundingRectangleType();

                if (bdwest.compareTo(BigDecimal.valueOf(180)) != 0) {
                    br.setWestBoundingCoordinate(convertBoundingVal(west, true).doubleValue());
                    br.setEastBoundingCoordinate(180.0);
                    br.setNorthBoundingCoordinate(north);
                    br.setSouthBoundingCoordinate(south);

                    rectsList.add(br);
                }

                br2.setWestBoundingCoordinate(-180.0);
                br2.setEastBoundingCoordinate(convertBoundingVal(east, true).doubleValue());
                br2.setNorthBoundingCoordinate(north);
                br2.setSouthBoundingCoordinate(south);
                rectsList.add(br2);

            }
        } else {
            if (east >= west) {
                br.setWestBoundingCoordinate(west);
                br.setEastBoundingCoordinate(east);
                //don't need to convert the lat ones, they are all in the -90/90 range
                br.setNorthBoundingCoordinate(north);
                br.setSouthBoundingCoordinate(south);

                rectsList.add(br);
            } else {
                log.debug("East Longitude is less than West Longitude: separating bounding boxes.");
                br2 = new BoundingRectangleType();

                if (west != 180d) {

                    br.setWestBoundingCoordinate(west);
                    br.setEastBoundingCoordinate(180.0);
                    br.setNorthBoundingCoordinate(north);
                    br.setSouthBoundingCoordinate(south);

                    rectsList.add(br);
                }
                if (east != -180d) {

                    br2.setWestBoundingCoordinate(-180.0);
                    br2.setEastBoundingCoordinate(east);
                    br2.setNorthBoundingCoordinate(north);
                    br2.setSouthBoundingCoordinate(south);
                    rectsList.add(br2);
                }
            }
        }
        return rectsList;
    }

    public static GeometryType genBoundingBox(boolean rangeIs360, double east, double west, double north, double south){

        GeometryType rectGeometry = new GeometryType();
        Set<BoundingRectangleType> rectSet = rectGeometry.getBoundingRectangles();

        rectSet.addAll(genRectangle(rangeIs360, east, west, north, south));

        return rectGeometry;
    }
}
