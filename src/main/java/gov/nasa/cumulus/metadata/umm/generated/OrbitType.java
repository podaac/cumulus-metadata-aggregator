
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity stores orbital coverage information of the granule. This coverage is an alternative way of expressing granule spatial coverage. This information supports orbital backtrack searching on a granule.
 * 
 */
@Generated("jsonschema2pojo")
public class OrbitType {

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    @SerializedName("AscendingCrossing")
    @Expose
    private Double ascendingCrossing;
    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    @SerializedName("StartLatitude")
    @Expose
    private Double startLatitude;
    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    @SerializedName("StartDirection")
    @Expose
    private OrbitType.OrbitDirectionTypeEnum startDirection;
    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    @SerializedName("EndLatitude")
    @Expose
    private Double endLatitude;
    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    @SerializedName("EndDirection")
    @Expose
    private OrbitType.OrbitDirectionTypeEnum endDirection;

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public Double getAscendingCrossing() {
        return ascendingCrossing;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public void setAscendingCrossing(Double ascendingCrossing) {
        this.ascendingCrossing = ascendingCrossing;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public Double getStartLatitude() {
        return startLatitude;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    public OrbitType.OrbitDirectionTypeEnum getStartDirection() {
        return startDirection;
    }

    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    public void setStartDirection(OrbitType.OrbitDirectionTypeEnum startDirection) {
        this.startDirection = startDirection;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public Double getEndLatitude() {
        return endLatitude;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    public OrbitType.OrbitDirectionTypeEnum getEndDirection() {
        return endDirection;
    }

    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * (Required)
     * 
     */
    public void setEndDirection(OrbitType.OrbitDirectionTypeEnum endDirection) {
        this.endDirection = endDirection;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(OrbitType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ascendingCrossing");
        sb.append('=');
        sb.append(((this.ascendingCrossing == null)?"<null>":this.ascendingCrossing));
        sb.append(',');
        sb.append("startLatitude");
        sb.append('=');
        sb.append(((this.startLatitude == null)?"<null>":this.startLatitude));
        sb.append(',');
        sb.append("startDirection");
        sb.append('=');
        sb.append(((this.startDirection == null)?"<null>":this.startDirection));
        sb.append(',');
        sb.append("endLatitude");
        sb.append('=');
        sb.append(((this.endLatitude == null)?"<null>":this.endLatitude));
        sb.append(',');
        sb.append("endDirection");
        sb.append('=');
        sb.append(((this.endDirection == null)?"<null>":this.endDirection));
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
        result = ((result* 31)+((this.startLatitude == null)? 0 :this.startLatitude.hashCode()));
        result = ((result* 31)+((this.endDirection == null)? 0 :this.endDirection.hashCode()));
        result = ((result* 31)+((this.startDirection == null)? 0 :this.startDirection.hashCode()));
        result = ((result* 31)+((this.endLatitude == null)? 0 :this.endLatitude.hashCode()));
        result = ((result* 31)+((this.ascendingCrossing == null)? 0 :this.ascendingCrossing.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrbitType) == false) {
            return false;
        }
        OrbitType rhs = ((OrbitType) other);
        return ((((((this.startLatitude == rhs.startLatitude)||((this.startLatitude!= null)&&this.startLatitude.equals(rhs.startLatitude)))&&((this.endDirection == rhs.endDirection)||((this.endDirection!= null)&&this.endDirection.equals(rhs.endDirection))))&&((this.startDirection == rhs.startDirection)||((this.startDirection!= null)&&this.startDirection.equals(rhs.startDirection))))&&((this.endLatitude == rhs.endLatitude)||((this.endLatitude!= null)&&this.endLatitude.equals(rhs.endLatitude))))&&((this.ascendingCrossing == rhs.ascendingCrossing)||((this.ascendingCrossing!= null)&&this.ascendingCrossing.equals(rhs.ascendingCrossing))));
    }


    /**
     * Orbit start and end direction. A for ascending orbit and D for descending.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum OrbitDirectionTypeEnum {

        @SerializedName("A")
        A("A"),
        @SerializedName("D")
        D("D");
        private final String value;
        private final static Map<String, OrbitType.OrbitDirectionTypeEnum> CONSTANTS = new HashMap<String, OrbitType.OrbitDirectionTypeEnum>();

        static {
            for (OrbitType.OrbitDirectionTypeEnum c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OrbitDirectionTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static OrbitType.OrbitDirectionTypeEnum fromValue(String value) {
            OrbitType.OrbitDirectionTypeEnum constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
