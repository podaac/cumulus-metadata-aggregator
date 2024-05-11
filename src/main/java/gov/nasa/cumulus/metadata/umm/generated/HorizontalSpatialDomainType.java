
package gov.nasa.cumulus.metadata.umm.generated;

import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Information about a granule with horizontal spatial coverage.
 * 
 */
@Generated("jsonschema2pojo")
public class HorizontalSpatialDomainType {

    /**
     * The appropriate numeric or alpha code used to identify the various zones in the granule's grid coordinate system.
     * 
     */
    @SerializedName("ZoneIdentifier")
    @Expose
    private String zoneIdentifier;
    /**
     * This entity holds the geometry representing the spatial coverage information of a granule.
     * 
     */
    @SerializedName("Geometry")
    @Expose
    private GeometryType geometry;
    /**
     * This entity stores orbital coverage information of the granule. This coverage is an alternative way of expressing granule spatial coverage. This information supports orbital backtrack searching on a granule.
     * 
     */
    @SerializedName("Orbit")
    @Expose
    private OrbitType orbit;
    /**
     * This element stores track information of the granule. Track information is used to allow a user to search for granules whose spatial extent is based on an orbital cycle, pass, and tile mapping. Though it is derived from the SWOT mission requirements, it is intended that this element type be generic enough so that other missions can make use of it. While track information is a type of spatial domain, it is expected that the metadata provider will provide geometry information that matches the spatial extent of the track information.
     * 
     */
    @SerializedName("Track")
    @Expose
    private TrackType track;

    /**
     * The appropriate numeric or alpha code used to identify the various zones in the granule's grid coordinate system.
     * 
     */
    public String getZoneIdentifier() {
        return zoneIdentifier;
    }

    /**
     * The appropriate numeric or alpha code used to identify the various zones in the granule's grid coordinate system.
     * 
     */
    public void setZoneIdentifier(String zoneIdentifier) {
        this.zoneIdentifier = zoneIdentifier;
    }

    /**
     * This entity holds the geometry representing the spatial coverage information of a granule.
     * 
     */
    public GeometryType getGeometry() {
        return geometry;
    }

    /**
     * This entity holds the geometry representing the spatial coverage information of a granule.
     * 
     */
    public void setGeometry(GeometryType geometry) {
        this.geometry = geometry;
    }

    /**
     * This entity stores orbital coverage information of the granule. This coverage is an alternative way of expressing granule spatial coverage. This information supports orbital backtrack searching on a granule.
     * 
     */
    public OrbitType getOrbit() {
        return orbit;
    }

    /**
     * This entity stores orbital coverage information of the granule. This coverage is an alternative way of expressing granule spatial coverage. This information supports orbital backtrack searching on a granule.
     * 
     */
    public void setOrbit(OrbitType orbit) {
        this.orbit = orbit;
    }

    /**
     * This element stores track information of the granule. Track information is used to allow a user to search for granules whose spatial extent is based on an orbital cycle, pass, and tile mapping. Though it is derived from the SWOT mission requirements, it is intended that this element type be generic enough so that other missions can make use of it. While track information is a type of spatial domain, it is expected that the metadata provider will provide geometry information that matches the spatial extent of the track information.
     * 
     */
    public TrackType getTrack() {
        return track;
    }

    /**
     * This element stores track information of the granule. Track information is used to allow a user to search for granules whose spatial extent is based on an orbital cycle, pass, and tile mapping. Though it is derived from the SWOT mission requirements, it is intended that this element type be generic enough so that other missions can make use of it. While track information is a type of spatial domain, it is expected that the metadata provider will provide geometry information that matches the spatial extent of the track information.
     * 
     */
    public void setTrack(TrackType track) {
        this.track = track;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(HorizontalSpatialDomainType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("zoneIdentifier");
        sb.append('=');
        sb.append(((this.zoneIdentifier == null)?"<null>":this.zoneIdentifier));
        sb.append(',');
        sb.append("geometry");
        sb.append('=');
        sb.append(((this.geometry == null)?"<null>":this.geometry));
        sb.append(',');
        sb.append("orbit");
        sb.append('=');
        sb.append(((this.orbit == null)?"<null>":this.orbit));
        sb.append(',');
        sb.append("track");
        sb.append('=');
        sb.append(((this.track == null)?"<null>":this.track));
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
        result = ((result* 31)+((this.zoneIdentifier == null)? 0 :this.zoneIdentifier.hashCode()));
        result = ((result* 31)+((this.geometry == null)? 0 :this.geometry.hashCode()));
        result = ((result* 31)+((this.orbit == null)? 0 :this.orbit.hashCode()));
        result = ((result* 31)+((this.track == null)? 0 :this.track.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HorizontalSpatialDomainType) == false) {
            return false;
        }
        HorizontalSpatialDomainType rhs = ((HorizontalSpatialDomainType) other);
        return (((((this.zoneIdentifier == rhs.zoneIdentifier)||((this.zoneIdentifier!= null)&&this.zoneIdentifier.equals(rhs.zoneIdentifier)))&&((this.geometry == rhs.geometry)||((this.geometry!= null)&&this.geometry.equals(rhs.geometry))))&&((this.orbit == rhs.orbit)||((this.orbit!= null)&&this.orbit.equals(rhs.orbit))))&&((this.track == rhs.track)||((this.track!= null)&&this.track.equals(rhs.track))));
    }

}
