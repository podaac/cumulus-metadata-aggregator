package gov.nasa.cumulus.metadata.aggregator;

/**
 * Class to define SWOT archive.xml XPath Expressions
 */
public final class SwotArchiveXmlXPath {
    public static final String CREATION_DATE_TIME = "/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/ProductionDateTime";
    public static final String BEGINNING_DATE_TIME = "/GranuleMetaDataFile/GranuleURMetaData/RangeDateTime/RangeBeginningDateTime";
    public static final String ENDING_DATE_TIME = "/GranuleMetaDataFile/GranuleURMetaData/RangeDateTime/RangeEndingDateTime";

    public static final String DATA_FILE_FILE_NAME = "/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/LocalGranuleID";
    public static final String DATA_FILE_FILE_DESCRIPTION = "/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/Description";
    public static final String ARCHIVE_CYCLE="/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/CycleID";
    public static final String ARCHIVE_PASS="/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/PassID";
    public static final String ARCHIVE_TILE="/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/TileID";
    public static final String archiveScene="/GranuleMetaDataFile/GranuleURMetaData/ECSDataGranule/SceneID";
}

