
package gov.nasa.cumulus.metadata.umm.generated;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * The longitude and latitude values of a spatially referenced point in degrees.
 * 
 */
@Generated("jsonschema2pojo")
public class PointType {

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    @SerializedName("Longitude")
    @Expose
    private Double longitude;
    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    @SerializedName("Latitude")
    @Expose
    private Double latitude;

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PointType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("longitude");
        sb.append('=');
        sb.append(((this.longitude == null)?"<null>":this.longitude));
        sb.append(',');
        sb.append("latitude");
        sb.append('=');
        sb.append(((this.latitude == null)?"<null>":this.latitude));
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
        result = ((result* 31)+((this.longitude == null)? 0 :this.longitude.hashCode()));
        result = ((result* 31)+((this.latitude == null)? 0 :this.latitude.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PointType) == false) {
            return false;
        }
        PointType rhs = ((PointType) other);
        return (((this.longitude == rhs.longitude)||((this.longitude!= null)&&this.longitude.equals(rhs.longitude)))&&((this.latitude == rhs.latitude)||((this.latitude!= null)&&this.latitude.equals(rhs.latitude))));
    }

}
