
package gov.nasa.cumulus.metadata.umm.generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * A GPolygon specifies an area on the earth represented by a main boundary with optional boundaries for regions excluded from the main boundary.
 * 
 */
public class GPolygonType {

    /**
     * A boundary is set of points connected by straight lines representing a polygon on the earth. It takes a minimum of three points to make a boundary. Points must be specified in counter-clockwise order and closed (the first and last vertices are the same).
     * (Required)
     * 
     */
    @SerializedName("Boundary")
    @Expose
    private BoundaryType boundary;
    /**
     * Contains the excluded boundaries from the GPolygon.
     * 
     */
    @SerializedName("ExclusiveZone")
    @Expose
    private ExclusiveZoneType exclusiveZone;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GPolygonType() {
    }

    /**
     * 
     * @param boundary
     * @param exclusiveZone
     */
    public GPolygonType(BoundaryType boundary, ExclusiveZoneType exclusiveZone) {
        super();
        this.boundary = boundary;
        this.exclusiveZone = exclusiveZone;
    }

    /**
     * A boundary is set of points connected by straight lines representing a polygon on the earth. It takes a minimum of three points to make a boundary. Points must be specified in counter-clockwise order and closed (the first and last vertices are the same).
     * (Required)
     * 
     */
    public BoundaryType getBoundary() {
        return boundary;
    }

    /**
     * A boundary is set of points connected by straight lines representing a polygon on the earth. It takes a minimum of three points to make a boundary. Points must be specified in counter-clockwise order and closed (the first and last vertices are the same).
     * (Required)
     * 
     */
    public void setBoundary(BoundaryType boundary) {
        this.boundary = boundary;
    }

    /**
     * Contains the excluded boundaries from the GPolygon.
     * 
     */
    public ExclusiveZoneType getExclusiveZone() {
        return exclusiveZone;
    }

    /**
     * Contains the excluded boundaries from the GPolygon.
     * 
     */
    public void setExclusiveZone(ExclusiveZoneType exclusiveZone) {
        this.exclusiveZone = exclusiveZone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GPolygonType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("boundary");
        sb.append('=');
        sb.append(((this.boundary == null)?"<null>":this.boundary));
        sb.append(',');
        sb.append("exclusiveZone");
        sb.append('=');
        sb.append(((this.exclusiveZone == null)?"<null>":this.exclusiveZone));
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
        result = ((result* 31)+((this.boundary == null)? 0 :this.boundary.hashCode()));
        result = ((result* 31)+((this.exclusiveZone == null)? 0 :this.exclusiveZone.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GPolygonType) == false) {
            return false;
        }
        GPolygonType rhs = ((GPolygonType) other);
        return (((this.boundary == rhs.boundary)||((this.boundary!= null)&&this.boundary.equals(rhs.boundary)))&&((this.exclusiveZone == rhs.exclusiveZone)||((this.exclusiveZone!= null)&&this.exclusiveZone.equals(rhs.exclusiveZone))));
    }

}
