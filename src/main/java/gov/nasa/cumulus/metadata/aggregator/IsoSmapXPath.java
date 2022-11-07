package gov.nasa.cumulus.metadata.aggregator;

/**
 * Class to define ISO-SMAP XPath Expressions
 */
public final class IsoSmapXPath extends IsoXPath {
    private static final String BASE = "/gmd:DS_Series/gmd:composedOf/gmd:DS_DataSet/gmd:has/gmi:MI_Metadata";
    private static final String CI_CITATION = "/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation";
    public static final String PRODUCTION_DATE_TIME = BASE + "/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep/gmd:dateTime/gco:DateTime";
    public static final String CREATION_DATE_TIME = BASE + CI_CITATION + "/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime";
    public static final String BEGINNING_DATE_TIME = BASE + "/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition";
    public static final String ENDING_DATE_TIME = BASE + "/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition";

    public static final String PRODUCER_GRANULE_ID = BASE + CI_CITATION + "/gmd:title/gmx:FileName";
    public static final String CRID = BASE + CI_CITATION + "/gmd:identifier[0]/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static final String IDENTIFIERS = BASE + CI_CITATION + "/gmd:identifier[1]/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static final String IDENTIFIER_DESCRIPTION = BASE + CI_CITATION + "/gmd:identifier[1]/gmd:MD_Identifier/gmd:description/gco:CharacterString";

    public static final String ORBIT = BASE + "/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent[@id=\"boundingExtent\"]/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"Orbit\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";

    // OrbitNumber, EquatorCrossingLongitude, EquatorCrossingDateTime
    public static final String OrbitCalculatedSpatialDomains = BASE + "/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent[@id=\"boundingExtent\"]/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"OrbitCalculatedSpatialDomains0\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";

    public static final String GRANULE_INPUT = BASE + CI_CITATION + "/gmd:title/gmx:FileName";
    public static final String POLYGON = "/gmd:DS_Series/gmd:composedOf/gmd:DS_DataSet/gmd:has/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";
}
