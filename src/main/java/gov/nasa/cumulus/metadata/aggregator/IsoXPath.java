package gov.nasa.cumulus.metadata.aggregator;

/**
 * Base class to define ISO XPath Expressions
 */
public class IsoXPath {
    public static String PRODUCTION_DATE_TIME;
    public static String CREATION_DATE_TIME;
    public static String BEGINNING_DATE_TIME;
    public static String ENDING_DATE_TIME;

    public static String PRODUCER_GRANULE_ID;
    public static String CRID;
    public static String IDENTIFIERS;
    public static String IDENTIFIER_CODE = "gmd:code/gco:CharacterString";
    public static String IDENTIFIER_DESCRIPTION;

    public static String SWOT_TRACK = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"SWOTTrack\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static String ORBIT;

    public static String PLATFORM = "/gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/eos:EOS_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static String INSTRUMENT = "/gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/eos:EOS_Platform/gmi:instrument/eos:EOS_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";

    public static String GRANULE_INPUT;
    public static String PGE_VERSION_CLASS = "/gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep[gmd:description/gco:CharacterString[text()=\"PGEVersionClass\"]]/gmi:processingInformation/eos:EOS_Processing/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
}
