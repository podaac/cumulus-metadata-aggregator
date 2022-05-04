
package gov.nasa.cumulus.metadata.umm.generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity holds the horizontal spatial coverage of a bounding box.
 * 
 */
public class BoundingRectangleType {

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    @SerializedName("WestBoundingCoordinate")
    @Expose
    private Double westBoundingCoordinate;
    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    @SerializedName("NorthBoundingCoordinate")
    @Expose
    private Double northBoundingCoordinate;
    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    @SerializedName("EastBoundingCoordinate")
    @Expose
    private Double eastBoundingCoordinate;
    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    @SerializedName("SouthBoundingCoordinate")
    @Expose
    private Double southBoundingCoordinate;

    /**
     * No args constructor for use in serialization
     * 
     */
    public BoundingRectangleType() {
    }

    /**
     * 
     * @param eastBoundingCoordinate
     * @param northBoundingCoordinate
     * @param southBoundingCoordinate
     * @param westBoundingCoordinate
     */
    public BoundingRectangleType(Double westBoundingCoordinate, Double northBoundingCoordinate, Double eastBoundingCoordinate, Double southBoundingCoordinate) {
        super();
        this.westBoundingCoordinate = westBoundingCoordinate;
        this.northBoundingCoordinate = northBoundingCoordinate;
        this.eastBoundingCoordinate = eastBoundingCoordinate;
        this.southBoundingCoordinate = southBoundingCoordinate;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public Double getWestBoundingCoordinate() {
        return westBoundingCoordinate;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public void setWestBoundingCoordinate(Double westBoundingCoordinate) {
        this.westBoundingCoordinate = westBoundingCoordinate;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public Double getNorthBoundingCoordinate() {
        return northBoundingCoordinate;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public void setNorthBoundingCoordinate(Double northBoundingCoordinate) {
        this.northBoundingCoordinate = northBoundingCoordinate;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public Double getEastBoundingCoordinate() {
        return eastBoundingCoordinate;
    }

    /**
     * The longitude value of a spatially referenced point, in degrees. Longitude values range from -180 to 180.
     * (Required)
     * 
     */
    public void setEastBoundingCoordinate(Double eastBoundingCoordinate) {
        this.eastBoundingCoordinate = eastBoundingCoordinate;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public Double getSouthBoundingCoordinate() {
        return southBoundingCoordinate;
    }

    /**
     * The latitude value of a spatially referenced point, in degrees. Latitude values range from -90 to 90.
     * (Required)
     * 
     */
    public void setSouthBoundingCoordinate(Double southBoundingCoordinate) {
        this.southBoundingCoordinate = southBoundingCoordinate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BoundingRectangleType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("westBoundingCoordinate");
        sb.append('=');
        sb.append(((this.westBoundingCoordinate == null)?"<null>":this.westBoundingCoordinate));
        sb.append(',');
        sb.append("northBoundingCoordinate");
        sb.append('=');
        sb.append(((this.northBoundingCoordinate == null)?"<null>":this.northBoundingCoordinate));
        sb.append(',');
        sb.append("eastBoundingCoordinate");
        sb.append('=');
        sb.append(((this.eastBoundingCoordinate == null)?"<null>":this.eastBoundingCoordinate));
        sb.append(',');
        sb.append("southBoundingCoordinate");
        sb.append('=');
        sb.append(((this.southBoundingCoordinate == null)?"<null>":this.southBoundingCoordinate));
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
        result = ((result* 31)+((this.eastBoundingCoordinate == null)? 0 :this.eastBoundingCoordinate.hashCode()));
        result = ((result* 31)+((this.northBoundingCoordinate == null)? 0 :this.northBoundingCoordinate.hashCode()));
        result = ((result* 31)+((this.southBoundingCoordinate == null)? 0 :this.southBoundingCoordinate.hashCode()));
        result = ((result* 31)+((this.westBoundingCoordinate == null)? 0 :this.westBoundingCoordinate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoundingRectangleType) == false) {
            return false;
        }
        BoundingRectangleType rhs = ((BoundingRectangleType) other);
        return (((((this.eastBoundingCoordinate == rhs.eastBoundingCoordinate)||((this.eastBoundingCoordinate!= null)&&this.eastBoundingCoordinate.equals(rhs.eastBoundingCoordinate)))&&((this.northBoundingCoordinate == rhs.northBoundingCoordinate)||((this.northBoundingCoordinate!= null)&&this.northBoundingCoordinate.equals(rhs.northBoundingCoordinate))))&&((this.southBoundingCoordinate == rhs.southBoundingCoordinate)||((this.southBoundingCoordinate!= null)&&this.southBoundingCoordinate.equals(rhs.southBoundingCoordinate))))&&((this.westBoundingCoordinate == rhs.westBoundingCoordinate)||((this.westBoundingCoordinate!= null)&&this.westBoundingCoordinate.equals(rhs.westBoundingCoordinate))));
    }

}
