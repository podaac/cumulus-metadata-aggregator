package gov.nasa.cumulus.metadata.aggregator;

public final class IsoSwotXPath extends IsoXPath {
    public static final String POLYGON = "/gmd:DS_Series/gmd:composedOf/gmd:DS_DataSet/gmd:has/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";
    public static final String CYCLE_PASS_TILE_SCENE = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription[@id=\"SWOTTrack\"]/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString";
    public static final String CREATION_DATE_TIME  = "/gmi:MI_Metadata/gmd:dateStamp/gco:DateTime";
    public static final String BEGINNING_DATE_TIME = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition";
    public static final String ENDING_DATE_TIME    = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition";

}
