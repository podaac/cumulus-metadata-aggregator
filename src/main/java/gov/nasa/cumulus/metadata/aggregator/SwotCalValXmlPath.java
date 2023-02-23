package gov.nasa.cumulus.metadata.aggregator;

public class SwotCalValXmlPath extends IsoXPath {
    public static final String BEGINNING_DATE_TIME = "/GranuleMetaDataFile/Extent/TemporalExtent/StartDateTime";
    public static final String ENDING_DATE_TIME = "/GranuleMetaDataFile/Extent/TemporalExtent/EndDateTime";
    public static final String CREATION_DATE_TIME = "/GranuleMetaDataFile/DataGranuleMembers[1]/DataGranuleMember/ProductionDateTime";
    public static final String WEST_BOUNDING_COORDINATE = "/GranuleMetaDataFile/Extent/SpatialExtent/WestBoundLongitude";
    public static final String SOUTH_BOUNDING_COORDINATE = "/GranuleMetaDataFile/Extent/SpatialExtent/SouthBoundLatitude";
    public static final String EAST_BOUNDING_COORDINATE = "/GranuleMetaDataFile/Extent/SpatialExtent/EastBoundLongitude";
    public static final String NORTH_BOUNDING_COORDINATE = "/GranuleMetaDataFile/Extent/SpatialExtent/NorthBoundLatitude";
}
