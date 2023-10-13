
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This element stores track information of the granule. Track information is used to allow a user to search for granules whose spatial extent is based on an orbital cycle, pass, and tile mapping. Though it is derived from the SWOT mission requirements, it is intended that this element type be generic enough so that other missions can make use of it. While track information is a type of spatial domain, it is expected that the metadata provider will provide geometry information that matches the spatial extent of the track information.
 * 
 */
@Generated("jsonschema2pojo")
public class TrackType {

    /**
     * An integer that represents a specific set of orbital spatial extents defined by passes and tiles. Though intended to be generic, this comes from a SWOT mission requirement where each cycle represents a set of 1/2 orbits. Each 1/2 orbit is called a 'pass'. During science mode, a cycle represents 21 days of 14 full orbits or 588 passes.
     * (Required)
     * 
     */
    @SerializedName("Cycle")
    @Expose
    private Integer cycle;
    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a list of pass numbers and their tiles that exist in the granule. It will allow a user to search by pass number and its tiles that are contained with in a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit. This element will then hold a list of 1/2 orbits and their tiles that together represent the granule's spatial extent.
     * 
     */
    @SerializedName("Passes")
    @Expose
    private List<TrackPassTileType> passes = new ArrayList<TrackPassTileType>();

    /**
     * An integer that represents a specific set of orbital spatial extents defined by passes and tiles. Though intended to be generic, this comes from a SWOT mission requirement where each cycle represents a set of 1/2 orbits. Each 1/2 orbit is called a 'pass'. During science mode, a cycle represents 21 days of 14 full orbits or 588 passes.
     * (Required)
     * 
     */
    public Integer getCycle() {
        return cycle;
    }

    /**
     * An integer that represents a specific set of orbital spatial extents defined by passes and tiles. Though intended to be generic, this comes from a SWOT mission requirement where each cycle represents a set of 1/2 orbits. Each 1/2 orbit is called a 'pass'. During science mode, a cycle represents 21 days of 14 full orbits or 588 passes.
     * (Required)
     * 
     */
    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a list of pass numbers and their tiles that exist in the granule. It will allow a user to search by pass number and its tiles that are contained with in a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit. This element will then hold a list of 1/2 orbits and their tiles that together represent the granule's spatial extent.
     * 
     */
    public List<TrackPassTileType> getPasses() {
        return passes;
    }

    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a list of pass numbers and their tiles that exist in the granule. It will allow a user to search by pass number and its tiles that are contained with in a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit. This element will then hold a list of 1/2 orbits and their tiles that together represent the granule's spatial extent.
     * 
     */
    public void setPasses(List<TrackPassTileType> passes) {
        this.passes = passes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TrackType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("cycle");
        sb.append('=');
        sb.append(((this.cycle == null)?"<null>":this.cycle));
        sb.append(',');
        sb.append("passes");
        sb.append('=');
        sb.append(((this.passes == null)?"<null>":this.passes));
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
        result = ((result* 31)+((this.passes == null)? 0 :this.passes.hashCode()));
        result = ((result* 31)+((this.cycle == null)? 0 :this.cycle.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TrackType) == false) {
            return false;
        }
        TrackType rhs = ((TrackType) other);
        return (((this.passes == rhs.passes)||((this.passes!= null)&&this.passes.equals(rhs.passes)))&&((this.cycle == rhs.cycle)||((this.cycle!= null)&&this.cycle.equals(rhs.cycle))));
    }

}
