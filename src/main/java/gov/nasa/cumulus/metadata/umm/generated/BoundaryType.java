
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * A boundary is set of points connected by straight lines representing a polygon on the earth. It takes a minimum of three points to make a boundary. Points must be specified in counter-clockwise order and closed (the first and last vertices are the same).
 * 
 */
@Generated("jsonschema2pojo")
public class BoundaryType {

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("Points")
    @Expose
    private List<PointType> points = new ArrayList<PointType>();

    /**
     * 
     * (Required)
     * 
     */
    public List<PointType> getPoints() {
        return points;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setPoints(List<PointType> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BoundaryType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("points");
        sb.append('=');
        sb.append(((this.points == null)?"<null>":this.points));
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
        result = ((result* 31)+((this.points == null)? 0 :this.points.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoundaryType) == false) {
            return false;
        }
        BoundaryType rhs = ((BoundaryType) other);
        return ((this.points == rhs.points)||((this.points!= null)&&this.points.equals(rhs.points)));
    }

}
