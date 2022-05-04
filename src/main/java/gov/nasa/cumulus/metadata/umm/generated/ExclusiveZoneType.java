
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Contains the excluded boundaries from the GPolygon.
 * 
 */
public class ExclusiveZoneType {

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("Boundaries")
    @Expose
    private List<BoundaryType> boundaries = new ArrayList<BoundaryType>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExclusiveZoneType() {
    }

    /**
     * 
     * @param boundaries
     */
    public ExclusiveZoneType(List<BoundaryType> boundaries) {
        super();
        this.boundaries = boundaries;
    }

    /**
     * 
     * (Required)
     * 
     */
    public List<BoundaryType> getBoundaries() {
        return boundaries;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setBoundaries(List<BoundaryType> boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ExclusiveZoneType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("boundaries");
        sb.append('=');
        sb.append(((this.boundaries == null)?"<null>":this.boundaries));
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
        result = ((result* 31)+((this.boundaries == null)? 0 :this.boundaries.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExclusiveZoneType) == false) {
            return false;
        }
        ExclusiveZoneType rhs = ((ExclusiveZoneType) other);
        return ((this.boundaries == rhs.boundaries)||((this.boundaries!= null)&&this.boundaries.equals(rhs.boundaries)));
    }

}
