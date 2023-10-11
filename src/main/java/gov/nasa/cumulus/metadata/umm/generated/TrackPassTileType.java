
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This element stores a track pass and its tile information. It will allow a user to search by pass number and their tiles that are contained with in a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit. This element will then hold a list of 1/2 orbits and their tiles that together represent the granules spatial extent.
 * 
 */
@Generated("jsonschema2pojo")
public class TrackPassTileType {

    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a pass number that exists in the granule and will allow a user to search by pass number that is contained within a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit.
     * (Required)
     * 
     */
    @SerializedName("Pass")
    @Expose
    private Integer pass;
    /**
     * A tile is a subset of a pass' spatial extent. This element holds a list of tile identifiers that exist in the granule and will allow a user to search by tile identifier that is contained within a pass number within a cycle number. Though intended to be generic, this comes from a SWOT mission requirement where a tile is a spatial extent that encompasses either a square scanning swath to the left or right of the ground track or a rectangle that includes a full scanning swath both to the left and right of the ground track.
     * 
     */
    @SerializedName("Tiles")
    @Expose
    private List<String> tiles = new ArrayList<String>();

    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a pass number that exists in the granule and will allow a user to search by pass number that is contained within a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit.
     * (Required)
     * 
     */
    public Integer getPass() {
        return pass;
    }

    /**
     * A pass number identifies a subset of a granule's spatial extent. This element holds a pass number that exists in the granule and will allow a user to search by pass number that is contained within a cycle number.  While trying to keep this generic for all to use, this comes from a SWOT requirement where a pass represents a 1/2 orbit.
     * (Required)
     * 
     */
    public void setPass(Integer pass) {
        this.pass = pass;
    }

    /**
     * A tile is a subset of a pass' spatial extent. This element holds a list of tile identifiers that exist in the granule and will allow a user to search by tile identifier that is contained within a pass number within a cycle number. Though intended to be generic, this comes from a SWOT mission requirement where a tile is a spatial extent that encompasses either a square scanning swath to the left or right of the ground track or a rectangle that includes a full scanning swath both to the left and right of the ground track.
     * 
     */
    public List<String> getTiles() {
        return tiles;
    }

    /**
     * A tile is a subset of a pass' spatial extent. This element holds a list of tile identifiers that exist in the granule and will allow a user to search by tile identifier that is contained within a pass number within a cycle number. Though intended to be generic, this comes from a SWOT mission requirement where a tile is a spatial extent that encompasses either a square scanning swath to the left or right of the ground track or a rectangle that includes a full scanning swath both to the left and right of the ground track.
     * 
     */
    public void setTiles(List<String> tiles) {
        this.tiles = tiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TrackPassTileType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("pass");
        sb.append('=');
        sb.append(((this.pass == null)?"<null>":this.pass));
        sb.append(',');
        sb.append("tiles");
        sb.append('=');
        sb.append(((this.tiles == null)?"<null>":this.tiles));
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
        result = ((result* 31)+((this.pass == null)? 0 :this.pass.hashCode()));
        result = ((result* 31)+((this.tiles == null)? 0 :this.tiles.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TrackPassTileType) == false) {
            return false;
        }
        TrackPassTileType rhs = ((TrackPassTileType) other);
        return (((this.pass == rhs.pass)||((this.pass!= null)&&this.pass.equals(rhs.pass)))&&((this.tiles == rhs.tiles)||((this.tiles!= null)&&this.tiles.equals(rhs.tiles))));
    }

}
