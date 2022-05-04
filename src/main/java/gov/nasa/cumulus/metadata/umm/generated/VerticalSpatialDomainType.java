
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity contains the type and value for the granule's vertical spatial domain.
 * 
 */
public class VerticalSpatialDomainType {

    @SerializedName("Type")
    @Expose
    private VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum type;
    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this for Atmosphere profiles or for a specific value.
     * 
     */
    @SerializedName("Value")
    @Expose
    private String value;
    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MaximumValue to represent a range of values (Min and Max).
     * 
     */
    @SerializedName("MinimumValue")
    @Expose
    private String minimumValue;
    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MinimumValue to represent a range of values (Min and Max).
     * 
     */
    @SerializedName("MaximumValue")
    @Expose
    private String maximumValue;
    /**
     * Describes the unit of the vertical extent value.
     * 
     */
    @SerializedName("Unit")
    @Expose
    private VerticalSpatialDomainType.Unit unit;

    /**
     * No args constructor for use in serialization
     * 
     */
    public VerticalSpatialDomainType() {
    }

    /**
     * 
     * @param minimumValue
     * @param unit
     * @param type
     * @param value
     * @param maximumValue
     */
    public VerticalSpatialDomainType(VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum type, String value, String minimumValue, String maximumValue, VerticalSpatialDomainType.Unit unit) {
        super();
        this.type = type;
        this.value = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.unit = unit;
    }

    public VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum getType() {
        return type;
    }

    public void setType(VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum type) {
        this.type = type;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this for Atmosphere profiles or for a specific value.
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this for Atmosphere profiles or for a specific value.
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MaximumValue to represent a range of values (Min and Max).
     * 
     */
    public String getMinimumValue() {
        return minimumValue;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MaximumValue to represent a range of values (Min and Max).
     * 
     */
    public void setMinimumValue(String minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MinimumValue to represent a range of values (Min and Max).
     * 
     */
    public String getMaximumValue() {
        return maximumValue;
    }

    /**
     * Describes the extent of the area of vertical space covered by the granule. Use this and MinimumValue to represent a range of values (Min and Max).
     * 
     */
    public void setMaximumValue(String maximumValue) {
        this.maximumValue = maximumValue;
    }

    /**
     * Describes the unit of the vertical extent value.
     * 
     */
    public VerticalSpatialDomainType.Unit getUnit() {
        return unit;
    }

    /**
     * Describes the unit of the vertical extent value.
     * 
     */
    public void setUnit(VerticalSpatialDomainType.Unit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(VerticalSpatialDomainType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null)?"<null>":this.value));
        sb.append(',');
        sb.append("minimumValue");
        sb.append('=');
        sb.append(((this.minimumValue == null)?"<null>":this.minimumValue));
        sb.append(',');
        sb.append("maximumValue");
        sb.append('=');
        sb.append(((this.maximumValue == null)?"<null>":this.maximumValue));
        sb.append(',');
        sb.append("unit");
        sb.append('=');
        sb.append(((this.unit == null)?"<null>":this.unit));
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
        result = ((result* 31)+((this.minimumValue == null)? 0 :this.minimumValue.hashCode()));
        result = ((result* 31)+((this.unit == null)? 0 :this.unit.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.value == null)? 0 :this.value.hashCode()));
        result = ((result* 31)+((this.maximumValue == null)? 0 :this.maximumValue.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VerticalSpatialDomainType) == false) {
            return false;
        }
        VerticalSpatialDomainType rhs = ((VerticalSpatialDomainType) other);
        return ((((((this.minimumValue == rhs.minimumValue)||((this.minimumValue!= null)&&this.minimumValue.equals(rhs.minimumValue)))&&((this.unit == rhs.unit)||((this.unit!= null)&&this.unit.equals(rhs.unit))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.value == rhs.value)||((this.value!= null)&&this.value.equals(rhs.value))))&&((this.maximumValue == rhs.maximumValue)||((this.maximumValue!= null)&&this.maximumValue.equals(rhs.maximumValue))));
    }


    /**
     * Describes the unit of the vertical extent value.
     * 
     */
    public enum Unit {

        @SerializedName("Fathoms")
        FATHOMS("Fathoms"),
        @SerializedName("Feet")
        FEET("Feet"),
        @SerializedName("HectoPascals")
        HECTO_PASCALS("HectoPascals"),
        @SerializedName("Kilometers")
        KILOMETERS("Kilometers"),
        @SerializedName("Meters")
        METERS("Meters"),
        @SerializedName("Millibars")
        MILLIBARS("Millibars"),
        @SerializedName("PoundsPerSquareInch")
        POUNDS_PER_SQUARE_INCH("PoundsPerSquareInch"),
        @SerializedName("Atmosphere")
        ATMOSPHERE("Atmosphere"),
        @SerializedName("InchesOfMercury")
        INCHES_OF_MERCURY("InchesOfMercury"),
        @SerializedName("InchesOfWater")
        INCHES_OF_WATER("InchesOfWater");
        private final String value;
        private final static Map<String, VerticalSpatialDomainType.Unit> CONSTANTS = new HashMap<String, VerticalSpatialDomainType.Unit>();

        static {
            for (VerticalSpatialDomainType.Unit c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Unit(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static VerticalSpatialDomainType.Unit fromValue(String value) {
            VerticalSpatialDomainType.Unit constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum VerticalSpatialDomainTypeEnum {

        @SerializedName("Atmosphere Layer")
        ATMOSPHERE_LAYER("Atmosphere Layer"),
        @SerializedName("Pressure")
        PRESSURE("Pressure"),
        @SerializedName("Altitude")
        ALTITUDE("Altitude"),
        @SerializedName("Depth")
        DEPTH("Depth");
        private final String value;
        private final static Map<String, VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum> CONSTANTS = new HashMap<String, VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum>();

        static {
            for (VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private VerticalSpatialDomainTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum fromValue(String value) {
            VerticalSpatialDomainType.VerticalSpatialDomainTypeEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
