
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity holds all types of online URL associated with the granule such as guide document or ordering site etc.
 * 
 */
public class RelatedUrlType {

    /**
     * The URL for the relevant resource.
     * (Required)
     * 
     */
    @SerializedName("URL")
    @Expose
    private String url;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("Type")
    @Expose
    private RelatedUrlType.RelatedUrlTypeEnum type;
    @SerializedName("Subtype")
    @Expose
    private RelatedUrlType.RelatedUrlSubTypeEnum subtype;
    /**
     * Description of the web page at this URL.
     * 
     */
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Format")
    @Expose
    private RelatedUrlType.DataFormatEnum format;
    @SerializedName("MimeType")
    @Expose
    private RelatedUrlType.MimeTypeEnum mimeType;
    /**
     * The size of the resource.
     * 
     */
    @SerializedName("Size")
    @Expose
    private Double size;
    /**
     * The unit of the file size.
     * 
     */
    @SerializedName("SizeUnit")
    @Expose
    private RelatedUrlType.FileSizeUnitEnum sizeUnit;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RelatedUrlType() {
    }

    /**
     * 
     * @param size
     * @param subtype
     * @param format
     * @param description
     * @param sizeUnit
     * @param mimeType
     * @param type
     * @param url
     */
    public RelatedUrlType(String url, RelatedUrlType.RelatedUrlTypeEnum type, RelatedUrlType.RelatedUrlSubTypeEnum subtype, String description, RelatedUrlType.DataFormatEnum format, RelatedUrlType.MimeTypeEnum mimeType, Double size, RelatedUrlType.FileSizeUnitEnum sizeUnit) {
        super();
        this.url = url;
        this.type = type;
        this.subtype = subtype;
        this.description = description;
        this.format = format;
        this.mimeType = mimeType;
        this.size = size;
        this.sizeUnit = sizeUnit;
    }

    /**
     * The URL for the relevant resource.
     * (Required)
     * 
     */
    public String getUrl() {
        return url;
    }

    /**
     * The URL for the relevant resource.
     * (Required)
     * 
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * (Required)
     * 
     */
    public RelatedUrlType.RelatedUrlTypeEnum getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setType(RelatedUrlType.RelatedUrlTypeEnum type) {
        this.type = type;
    }

    public RelatedUrlType.RelatedUrlSubTypeEnum getSubtype() {
        return subtype;
    }

    public void setSubtype(RelatedUrlType.RelatedUrlSubTypeEnum subtype) {
        this.subtype = subtype;
    }

    /**
     * Description of the web page at this URL.
     * 
     */
    public String getDescription() {
        return description;
    }

    /**
     * Description of the web page at this URL.
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public RelatedUrlType.DataFormatEnum getFormat() {
        return format;
    }

    public void setFormat(RelatedUrlType.DataFormatEnum format) {
        this.format = format;
    }

    public RelatedUrlType.MimeTypeEnum getMimeType() {
        return mimeType;
    }

    public void setMimeType(RelatedUrlType.MimeTypeEnum mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * The size of the resource.
     * 
     */
    public Double getSize() {
        return size;
    }

    /**
     * The size of the resource.
     * 
     */
    public void setSize(Double size) {
        this.size = size;
    }

    /**
     * The unit of the file size.
     * 
     */
    public RelatedUrlType.FileSizeUnitEnum getSizeUnit() {
        return sizeUnit;
    }

    /**
     * The unit of the file size.
     * 
     */
    public void setSizeUnit(RelatedUrlType.FileSizeUnitEnum sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RelatedUrlType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("url");
        sb.append('=');
        sb.append(((this.url == null)?"<null>":this.url));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("subtype");
        sb.append('=');
        sb.append(((this.subtype == null)?"<null>":this.subtype));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("format");
        sb.append('=');
        sb.append(((this.format == null)?"<null>":this.format));
        sb.append(',');
        sb.append("mimeType");
        sb.append('=');
        sb.append(((this.mimeType == null)?"<null>":this.mimeType));
        sb.append(',');
        sb.append("size");
        sb.append('=');
        sb.append(((this.size == null)?"<null>":this.size));
        sb.append(',');
        sb.append("sizeUnit");
        sb.append('=');
        sb.append(((this.sizeUnit == null)?"<null>":this.sizeUnit));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.size == null)? 0 :this.size.hashCode()));
        result = ((result* 31)+((this.subtype == null)? 0 :this.subtype.hashCode()));
        result = ((result* 31)+((this.format == null)? 0 :this.format.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.sizeUnit == null)? 0 :this.sizeUnit.hashCode()));
        result = ((result* 31)+((this.mimeType == null)? 0 :this.mimeType.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.url == null)? 0 :this.url.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RelatedUrlType) == false) {
            return false;
        }
        RelatedUrlType rhs = ((RelatedUrlType) other);
        return (((((((((this.size == rhs.size)||((this.size!= null)&&this.size.equals(rhs.size)))&&((this.subtype == rhs.subtype)||((this.subtype!= null)&&this.subtype.equals(rhs.subtype))))&&((this.format == rhs.format)||((this.format!= null)&&this.format.equals(rhs.format))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.sizeUnit == rhs.sizeUnit)||((this.sizeUnit!= null)&&this.sizeUnit.equals(rhs.sizeUnit))))&&((this.mimeType == rhs.mimeType)||((this.mimeType!= null)&&this.mimeType.equals(rhs.mimeType))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.url == rhs.url)||((this.url!= null)&&this.url.equals(rhs.url))));
    }

    public enum DataFormatEnum {

        @SerializedName("ASCII")
        ASCII("ASCII"),
        @SerializedName("BINARY")
        BINARY("BINARY"),
        @SerializedName("BMP")
        BMP("BMP"),
        @SerializedName("BUFR")
        BUFR("BUFR"),
        @SerializedName("CSV")
        CSV("CSV"),
        @SerializedName("GEOTIFF")
        GEOTIFF("GEOTIFF"),
        @SerializedName("GIF")
        GIF("GIF"),
        @SerializedName("GEOTIFFINT16")
        GEOTIFFINT_16("GEOTIFFINT16"),
        @SerializedName("GEOTIFFFLOAT32")
        GEOTIFFFLOAT_32("GEOTIFFFLOAT32"),
        @SerializedName("GRIB")
        GRIB("GRIB"),
        @SerializedName("GZIP")
        GZIP("GZIP"),
        @SerializedName("HDF4")
        HDF_4("HDF4"),
        @SerializedName("HDF5")
        HDF_5("HDF5"),
        @SerializedName("HDF-EOS2")
        HDF_EOS_2("HDF-EOS2"),
        @SerializedName("HDF-EOS5")
        HDF_EOS_5("HDF-EOS5"),
        @SerializedName("HTML")
        HTML("HTML"),
        @SerializedName("ICARTT")
        ICARTT("ICARTT"),
        @SerializedName("JPEG")
        JPEG("JPEG"),
        @SerializedName("JSON")
        JSON("JSON"),
        @SerializedName("KML")
        KML("KML"),
        @SerializedName("NETCDF-3")
        NETCDF_3("NETCDF-3"),
        @SerializedName("NETCDF-4")
        NETCDF_4("NETCDF-4"),
        @SerializedName("NETCDF-CF")
        NETCDF_CF("NETCDF-CF"),
        @SerializedName("PNG")
        PNG("PNG"),
        @SerializedName("PNG24")
        PNG_24("PNG24"),
        @SerializedName("TAR")
        TAR("TAR"),
        @SerializedName("TIFF")
        TIFF("TIFF"),
        @SerializedName("XLSX")
        XLSX("XLSX"),
        @SerializedName("XML")
        XML("XML"),
        @SerializedName("ZIP")
        ZIP("ZIP"),
        @SerializedName("DMRPP")
        DMRPP("DMRPP"),
        @SerializedName("Not provided")
        NOT_PROVIDED("Not provided");
        private final String value;
        private final static Map<String, RelatedUrlType.DataFormatEnum> CONSTANTS = new HashMap<String, RelatedUrlType.DataFormatEnum>();

        static {
            for (RelatedUrlType.DataFormatEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private DataFormatEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static RelatedUrlType.DataFormatEnum fromValue(String value) {
            RelatedUrlType.DataFormatEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * The unit of the file size.
     * 
     */
    public enum FileSizeUnitEnum {

        @SerializedName("KB")
        KB("KB"),
        @SerializedName("MB")
        MB("MB"),
        @SerializedName("GB")
        GB("GB"),
        @SerializedName("TB")
        TB("TB"),
        @SerializedName("PB")
        PB("PB"),
        @SerializedName("NA")
        NA("NA");
        private final String value;
        private final static Map<String, RelatedUrlType.FileSizeUnitEnum> CONSTANTS = new HashMap<String, RelatedUrlType.FileSizeUnitEnum>();

        static {
            for (RelatedUrlType.FileSizeUnitEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private FileSizeUnitEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static RelatedUrlType.FileSizeUnitEnum fromValue(String value) {
            RelatedUrlType.FileSizeUnitEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum MimeTypeEnum {

        @SerializedName("application/json")
        APPLICATION_JSON("application/json"),
        @SerializedName("application/xml")
        APPLICATION_XML("application/xml"),
        @SerializedName("application/x-netcdf")
        APPLICATION_X_NETCDF("application/x-netcdf"),
        @SerializedName("application/x-hdfeos")
        APPLICATION_X_HDFEOS("application/x-hdfeos"),
        @SerializedName("application/gml+xml")
        APPLICATION_GML_XML("application/gml+xml"),
        @SerializedName("application/vnd.google-earth.kml+xml")
        APPLICATION_VND_GOOGLE_EARTH_KML_XML("application/vnd.google-earth.kml+xml"),
        @SerializedName("image/gif")
        IMAGE_GIF("image/gif"),
        @SerializedName("image/tiff")
        IMAGE_TIFF("image/tiff"),
        @SerializedName("image/bmp")
        IMAGE_BMP("image/bmp"),
        @SerializedName("text/csv")
        TEXT_CSV("text/csv"),
        @SerializedName("text/xml")
        TEXT_XML("text/xml"),
        @SerializedName("application/pdf")
        APPLICATION_PDF("application/pdf"),
        @SerializedName("application/x-hdf")
        APPLICATION_X_HDF("application/x-hdf"),
        @SerializedName("application/x-hdf5")
        APPLICATION_X_HDF_5("application/x-hdf5"),
        @SerializedName("application/octet-stream")
        APPLICATION_OCTET_STREAM("application/octet-stream"),
        @SerializedName("application/vnd.google-earth.kmz")
        APPLICATION_VND_GOOGLE_EARTH_KMZ("application/vnd.google-earth.kmz"),
        @SerializedName("image/jpeg")
        IMAGE_JPEG("image/jpeg"),
        @SerializedName("image/png")
        IMAGE_PNG("image/png"),
        @SerializedName("image/vnd.collada+xml")
        IMAGE_VND_COLLADA_XML("image/vnd.collada+xml"),
        @SerializedName("text/html")
        TEXT_HTML("text/html"),
        @SerializedName("text/plain")
        TEXT_PLAIN("text/plain"),
        @SerializedName("application/zip")
        APPLICATION_ZIP("application/zip"),
        @SerializedName("application/gzip")
        APPLICATION_GZIP("application/gzip"),
        @SerializedName("application/tar")
        APPLICATION_TAR("application/tar"),
        @SerializedName("application/tar+gzip")
        APPLICATION_TAR_GZIP("application/tar+gzip"),
        @SerializedName("application/tar+zip")
        APPLICATION_TAR_ZIP("application/tar+zip"),
        @SerializedName("application/vnd.opendap.dap4.dmrpp+xml")
        APPLICATION_VND_OPENDAP_DAP_4_DMRPP_XML("application/vnd.opendap.dap4.dmrpp+xml"),
        @SerializedName("Not provided")
        NOT_PROVIDED("Not provided");
        private final String value;
        private final static Map<String, RelatedUrlType.MimeTypeEnum> CONSTANTS = new HashMap<String, RelatedUrlType.MimeTypeEnum>();

        static {
            for (RelatedUrlType.MimeTypeEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private MimeTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static RelatedUrlType.MimeTypeEnum fromValue(String value) {
            RelatedUrlType.MimeTypeEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum RelatedUrlSubTypeEnum {

        @SerializedName("MOBILE APP")
        MOBILE_APP("MOBILE APP"),
        @SerializedName("APPEARS")
        APPEARS("APPEARS"),
        @SerializedName("DATA COLLECTION BUNDLE")
        DATA_COLLECTION_BUNDLE("DATA COLLECTION BUNDLE"),
        @SerializedName("DATA TREE")
        DATA_TREE("DATA TREE"),
        @SerializedName("DATACAST URL")
        DATACAST_URL("DATACAST URL"),
        @SerializedName("DIRECT DOWNLOAD")
        DIRECT_DOWNLOAD("DIRECT DOWNLOAD"),
        @SerializedName("EOSDIS DATA POOL")
        EOSDIS_DATA_POOL("EOSDIS DATA POOL"),
        @SerializedName("Earthdata Search")
        EARTHDATA_SEARCH("Earthdata Search"),
        @SerializedName("GIOVANNI")
        GIOVANNI("GIOVANNI"),
        @SerializedName("GoLIVE Portal")
        GO_LIVE_PORTAL("GoLIVE Portal"),
        @SerializedName("IceBridge Portal")
        ICE_BRIDGE_PORTAL("IceBridge Portal"),
        @SerializedName("LAADS")
        LAADS("LAADS"),
        @SerializedName("LANCE")
        LANCE("LANCE"),
        @SerializedName("MIRADOR")
        MIRADOR("MIRADOR"),
        @SerializedName("MODAPS")
        MODAPS("MODAPS"),
        @SerializedName("NOAA CLASS")
        NOAA_CLASS("NOAA CLASS"),
        @SerializedName("NOMADS")
        NOMADS("NOMADS"),
        @SerializedName("Order")
        ORDER("Order"),
        @SerializedName("PORTAL")
        PORTAL("PORTAL"),
        @SerializedName("Subscribe")
        SUBSCRIBE("Subscribe"),
        @SerializedName("USGS EARTH EXPLORER")
        USGS_EARTH_EXPLORER("USGS EARTH EXPLORER"),
        @SerializedName("VERTEX")
        VERTEX("VERTEX"),
        @SerializedName("VIRTUAL COLLECTION")
        VIRTUAL_COLLECTION("VIRTUAL COLLECTION"),
        @SerializedName("MAP")
        MAP("MAP"),
        @SerializedName("WORLDVIEW")
        WORLDVIEW("WORLDVIEW"),
        @SerializedName("LIVE ACCESS SERVER (LAS)")
        LIVE_ACCESS_SERVER_LAS("LIVE ACCESS SERVER (LAS)"),
        @SerializedName("MAP VIEWER")
        MAP_VIEWER("MAP VIEWER"),
        @SerializedName("SIMPLE SUBSET WIZARD (SSW)")
        SIMPLE_SUBSET_WIZARD_SSW("SIMPLE SUBSET WIZARD (SSW)"),
        @SerializedName("SUBSETTER")
        SUBSETTER("SUBSETTER"),
        @SerializedName("GRADS DATA SERVER (GDS)")
        GRADS_DATA_SERVER_GDS("GRADS DATA SERVER (GDS)"),
        @SerializedName("MAP SERVICE")
        MAP_SERVICE("MAP SERVICE"),
        @SerializedName("OPENDAP DATA")
        OPENDAP_DATA("OPENDAP DATA"),
        @SerializedName("OpenSearch")
        OPEN_SEARCH("OpenSearch"),
        @SerializedName("SERVICE CHAINING")
        SERVICE_CHAINING("SERVICE CHAINING"),
        @SerializedName("TABULAR DATA STREAM (TDS)")
        TABULAR_DATA_STREAM_TDS("TABULAR DATA STREAM (TDS)"),
        @SerializedName("THREDDS DATA")
        THREDDS_DATA("THREDDS DATA"),
        @SerializedName("WEB COVERAGE SERVICE (WCS)")
        WEB_COVERAGE_SERVICE_WCS("WEB COVERAGE SERVICE (WCS)"),
        @SerializedName("WEB FEATURE SERVICE (WFS)")
        WEB_FEATURE_SERVICE_WFS("WEB FEATURE SERVICE (WFS)"),
        @SerializedName("WEB MAP SERVICE (WMS)")
        WEB_MAP_SERVICE_WMS("WEB MAP SERVICE (WMS)"),
        @SerializedName("WEB MAP TILE SERVICE (WMTS)")
        WEB_MAP_TILE_SERVICE_WMTS("WEB MAP TILE SERVICE (WMTS)"),
        @SerializedName("ALGORITHM DOCUMENTATION")
        ALGORITHM_DOCUMENTATION("ALGORITHM DOCUMENTATION"),
        @SerializedName("ALGORITHM THEORETICAL BASIS DOCUMENT (ATBD)")
        ALGORITHM_THEORETICAL_BASIS_DOCUMENT_ATBD("ALGORITHM THEORETICAL BASIS DOCUMENT (ATBD)"),
        @SerializedName("ANOMALIES")
        ANOMALIES("ANOMALIES"),
        @SerializedName("CASE STUDY")
        CASE_STUDY("CASE STUDY"),
        @SerializedName("DATA CITATION POLICY")
        DATA_CITATION_POLICY("DATA CITATION POLICY"),
        @SerializedName("DATA QUALITY")
        DATA_QUALITY("DATA QUALITY"),
        @SerializedName("DATA RECIPE")
        DATA_RECIPE("DATA RECIPE"),
        @SerializedName("DELIVERABLES CHECKLIST")
        DELIVERABLES_CHECKLIST("DELIVERABLES CHECKLIST"),
        @SerializedName("GENERAL DOCUMENTATION")
        GENERAL_DOCUMENTATION("GENERAL DOCUMENTATION"),
        @SerializedName("HOW-TO")
        HOW_TO("HOW-TO"),
        @SerializedName("IMPORTANT NOTICE")
        IMPORTANT_NOTICE("IMPORTANT NOTICE"),
        @SerializedName("INSTRUMENT/SENSOR CALIBRATION DOCUMENTATION")
        INSTRUMENT_SENSOR_CALIBRATION_DOCUMENTATION("INSTRUMENT/SENSOR CALIBRATION DOCUMENTATION"),
        @SerializedName("MICRO ARTICLE")
        MICRO_ARTICLE("MICRO ARTICLE"),
        @SerializedName("PI DOCUMENTATION")
        PI_DOCUMENTATION("PI DOCUMENTATION"),
        @SerializedName("PROCESSING HISTORY")
        PROCESSING_HISTORY("PROCESSING HISTORY"),
        @SerializedName("PRODUCT HISTORY")
        PRODUCT_HISTORY("PRODUCT HISTORY"),
        @SerializedName("PRODUCT QUALITY ASSESSMENT")
        PRODUCT_QUALITY_ASSESSMENT("PRODUCT QUALITY ASSESSMENT"),
        @SerializedName("PRODUCT USAGE")
        PRODUCT_USAGE("PRODUCT USAGE"),
        @SerializedName("PRODUCTION HISTORY")
        PRODUCTION_HISTORY("PRODUCTION HISTORY"),
        @SerializedName("PUBLICATIONS")
        PUBLICATIONS("PUBLICATIONS"),
        @SerializedName("READ-ME")
        READ_ME("READ-ME"),
        @SerializedName("REQUIREMENTS AND DESIGN")
        REQUIREMENTS_AND_DESIGN("REQUIREMENTS AND DESIGN"),
        @SerializedName("SCIENCE DATA PRODUCT SOFTWARE DOCUMENTATION")
        SCIENCE_DATA_PRODUCT_SOFTWARE_DOCUMENTATION("SCIENCE DATA PRODUCT SOFTWARE DOCUMENTATION"),
        @SerializedName("SCIENCE DATA PRODUCT VALIDATION")
        SCIENCE_DATA_PRODUCT_VALIDATION("SCIENCE DATA PRODUCT VALIDATION"),
        @SerializedName("USER FEEDBACK PAGE")
        USER_FEEDBACK_PAGE("USER FEEDBACK PAGE"),
        @SerializedName("USER'S GUIDE")
        USER_S_GUIDE("USER'S GUIDE"),
        @SerializedName("DMR++")
        DMR("DMR++"),
        @SerializedName("DMR++ MISSING DATA")
        DMR_MISSING_DATA("DMR++ MISSING DATA");
        private final String value;
        private final static Map<String, RelatedUrlType.RelatedUrlSubTypeEnum> CONSTANTS = new HashMap<String, RelatedUrlType.RelatedUrlSubTypeEnum>();

        static {
            for (RelatedUrlType.RelatedUrlSubTypeEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private RelatedUrlSubTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static RelatedUrlType.RelatedUrlSubTypeEnum fromValue(String value) {
            RelatedUrlType.RelatedUrlSubTypeEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum RelatedUrlTypeEnum {

        @SerializedName("DOWNLOAD SOFTWARE")
        DOWNLOAD_SOFTWARE("DOWNLOAD SOFTWARE"),
        @SerializedName("EXTENDED METADATA")
        EXTENDED_METADATA("EXTENDED METADATA"),
        @SerializedName("GET DATA")
        GET_DATA("GET DATA"),
        @SerializedName("GET DATA VIA DIRECT ACCESS")
        GET_DATA_VIA_DIRECT_ACCESS("GET DATA VIA DIRECT ACCESS"),
        @SerializedName("GET RELATED VISUALIZATION")
        GET_RELATED_VISUALIZATION("GET RELATED VISUALIZATION"),
        @SerializedName("GOTO WEB TOOL")
        GOTO_WEB_TOOL("GOTO WEB TOOL"),
        @SerializedName("PROJECT HOME PAGE")
        PROJECT_HOME_PAGE("PROJECT HOME PAGE"),
        @SerializedName("USE SERVICE API")
        USE_SERVICE_API("USE SERVICE API"),
        @SerializedName("VIEW RELATED INFORMATION")
        VIEW_RELATED_INFORMATION("VIEW RELATED INFORMATION");
        private final String value;
        private final static Map<String, RelatedUrlType.RelatedUrlTypeEnum> CONSTANTS = new HashMap<String, RelatedUrlType.RelatedUrlTypeEnum>();

        static {
            for (RelatedUrlType.RelatedUrlTypeEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private RelatedUrlTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static RelatedUrlType.RelatedUrlTypeEnum fromValue(String value) {
            RelatedUrlType.RelatedUrlTypeEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
