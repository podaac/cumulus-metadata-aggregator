package gov.nasa.cumulus.metadata.aggregator;

/**
 * Class to define Sentinel-6 Manifest XPath Expressions
 */
public final class ManifestXPath {
    public static final String CREATION_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6:generalProductInformation/sentinel6:creationTime";
    public static final String BEGINNING_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"acquisitionPeriod\"]/metadataWrap/xmlData/sentinel-safe:acquisitionPeriod/sentinel-safe:startTime";
    public static final String ENDING_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"acquisitionPeriod\"]/metadataWrap/xmlData/sentinel-safe:acquisitionPeriod/sentinel-safe:stopTime";
    public static final String LINE = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"measurementFrameSet\"]/metadataWrap/xmlData/sentinel-safe:frameSet/sentinel-safe:footPrint/gml:posList";
    public static final String CYCLE = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"measurementOrbitReference\"]/metadataWrap/xmlData/sentinel-safe:orbitReference/sentinel-safe:cycleNumber";
    public static final String PASS = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"measurementOrbitReference\"]/metadataWrap/xmlData/sentinel-safe:orbitReference/sentinel-safe:relativePassNumber[@type=\"start\"]";
    public static final String AUX_CREATION_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6aux:generalProductInformation/sentinel6aux:creationTime";
    public static final String AUX_BEGINNING_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6aux:generalProductInformation/sentinel6aux:validityStartTime";
    public static final String AUX_ENDING_DATE_TIME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6aux:generalProductInformation/sentinel6aux:validityStopTime";
    public static final String AUX_FILE_NAME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6aux:generalProductInformation/sentinel6aux:fileName";
    public static final String PRODUCT_NAME = "/xfdu:XFDU/metadataSection/metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/sentinel6:generalProductInformation/sentinel6:productName";
}
