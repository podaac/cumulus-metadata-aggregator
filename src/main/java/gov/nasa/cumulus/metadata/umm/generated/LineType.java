
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This entity holds the horizontal spatial coverage of a line. A line area contains at lease two points.
 * 
 */
public class LineType {

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
        sb.append(LineType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof LineType) == false) {
            return false;
        }
        LineType rhs = ((LineType) other);
        return ((this.points == rhs.points)||((this.points!= null)&&this.points.equals(rhs.points)));
    }

}
