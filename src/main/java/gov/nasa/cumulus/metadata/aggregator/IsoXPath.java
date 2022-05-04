package gov.nasa.cumulus.metadata.aggregator;

/**
 * Class to define ISO-MENDS XPath Expressions
 */
public final class IsoXPath {
    public static final String PRODUCTION_DATE_TIME = "/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep/gmd:dateTime/gco:DateTime";
    public static final String CREATION_DATE_TIME = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime";
    public static final String BEGINNING_DATE_TIME = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition";
    public static final String ENDING_DATE_TIME = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition";
    public static final String POLYGON = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";
    public static final String WEST_BOUNDING_COORDINATE = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal";
    public static final String SOUTH_BOUNDING_COORDINATE = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal";
    public static final String EAST_BOUNDING_COORDINATE = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal";
    public static final String NORTH_BOUNDING_COORDINATE = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal";
    public static final String DATA_FILE = "/gmi:MI_Metadata/gmd:describes/gmx:MX_DataSet/gmx:dataFile[@xlink:href]";
    public static final String DATA_FILE_FILE_NAME = "gmx:MX_DataFile/gmx:fileName/gmx:FileName";
    public static final String DATA_FILE_FILE_DESCRIPTION = "gmx:MX_DataFile/gmx:fileDescription/gco:CharacterString";
    public static final String DATA_FILE_FILE_FORMAT = "gmx:MX_DataFile/gmx:fileFormat/gmd:MD_Format/gmd:name/gco:CharacterString";
    public static final String DATA_FILE_FILE_MIME_TYPE = "gmx:MX_DataFile/gmx:fileType/gmx:MimeFileType";

    public static final String PARAMETER_NAME = "/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString";
    public static final String QA_PERCENT_MISSING_DATA = "/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/gmd:MD_Band/gmd:otherProperty/gco:Record/eos:AdditionalAttributes/eos:AdditionalAttribute[eos:reference/eos:EOS_AdditionalAttributeDescription/eos:name/gco:CharacterString[text()=\"QAPercentMissingData\"]]/eos:value/gco:CharacterString";
    public static final String QA_PERCENT_OUT_OF_BOUNDS_DATA = "/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_CoverageDescription/gmd:dimension/gmd:MD_Band/gmd:otherProperty/gco:Record/eos:AdditionalAttributes/eos:AdditionalAttribute[eos:reference/eos:EOS_AdditionalAttributeDescription/eos:name/gco:CharacterString[text()=\"QAPercentOutOfBoundsData\"]]/eos:value/gco:CharacterString";

    public static final String PRODUCER_GRANULE_ID = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier[gmd:codeSpace/gco:CharacterString[text()=\"gov.nasa.esdis.umm.producergranuleid\"]]/gmd:code/gco:CharacterString";
    public static final String CRID = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier[gmd:codeSpace/gco:CharacterString[text()=\"gov.nasa.esdis.umm.crid\"]]/gmd:code/gco:CharacterString";
    public static final String IDENTIFIERS = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier[gmd:codeSpace/gco:CharacterString[text()=\"gov.nasa.esdis.umm.otherid\"]]";
    public static final String IDENTIFIER_CODE = "gmd:code/gco:CharacterString";
    public static final String IDENTIFIER_DESCRIPTION = "gmd:description/gco:CharacterString";

    public static final String REPROCESSING_PLANNED = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString";
    public static final String REPROCESSING_ACTUAL = "/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep/gmd:description/gco:CharacterString";

    public static final String SWOT_TRACK = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"SWOTTrack\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static final String ORBIT = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"Orbit\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";

    public static final String PLATFORM = "/gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/eos:EOS_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static final String INSTRUMENT = "/gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/eos:EOS_Platform/gmi:instrument/eos:EOS_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";

    public static final String GRANULE_INPUT = "/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmi:LE_Source[gmd:description/gco:CharacterString[text()=\"GranuleInput\"]]/gmd:sourceCitation/gmd:CI_Citation/gmd:title/gmx:FileName";
    public static final String PGE_VERSION_CLASS = "/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep[gmd:description/gco:CharacterString[text()=\"PGEVersionClass\"]]/gmi:processingInformation/eos:EOS_Processing/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
}
