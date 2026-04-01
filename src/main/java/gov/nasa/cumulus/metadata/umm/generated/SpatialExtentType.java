
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This class contains attributes which describe the spatial extent of a granule. Spatial Extent includes any or all of Granule Localities, Horizontal Spatial Domain, and Vertical Spatial Domain.
 * 
 */
public class SpatialExtentType {

    /**
     * This entity stores information used at the granule level to describe the labeling of granules with compounded time/space text values and which are subsequently used to define more phenomenological-based granules, thus the locality type and description are contained.
     * 
     */
    @SerializedName("GranuleLocalities")
    @Expose
    private Set<String> granuleLocalities = new LinkedHashSet<String>();
    /**
     * Information about a granule with horizontal spatial coverage.
     * 
     */
    @SerializedName("HorizontalSpatialDomain")
    @Expose
    private HorizontalSpatialDomainType horizontalSpatialDomain;
    /**
     * This represents the domain value and type for the granule's vertical spatial domain.
     * 
     */
    @SerializedName("VerticalSpatialDomains")
    @Expose
    private Set<VerticalSpatialDomainType> verticalSpatialDomains = new LinkedHashSet<VerticalSpatialDomainType>();

    /**
     * This entity stores information used at the granule level to describe the labeling of granules with compounded time/space text values and which are subsequently used to define more phenomenological-based granules, thus the locality type and description are contained.
     * 
     */
    public Set<String> getGranuleLocalities() {
        return granuleLocalities;
    }

    /**
     * This entity stores information used at the granule level to describe the labeling of granules with compounded time/space text values and which are subsequently used to define more phenomenological-based granules, thus the locality type and description are contained.
     * 
     */
    public void setGranuleLocalities(Set<String> granuleLocalities) {
        this.granuleLocalities = granuleLocalities;
    }

    /**
     * Information about a granule with horizontal spatial coverage.
     * 
     */
    public HorizontalSpatialDomainType getHorizontalSpatialDomain() {
        return horizontalSpatialDomain;
    }

    /**
     * Information about a granule with horizontal spatial coverage.
     * 
     */
    public void setHorizontalSpatialDomain(HorizontalSpatialDomainType horizontalSpatialDomain) {
        this.horizontalSpatialDomain = horizontalSpatialDomain;
    }

    /**
     * This represents the domain value and type for the granule's vertical spatial domain.
     * 
     */
    public Set<VerticalSpatialDomainType> getVerticalSpatialDomains() {
        return verticalSpatialDomains;
    }

    /**
     * This represents the domain value and type for the granule's vertical spatial domain.
     * 
     */
    public void setVerticalSpatialDomains(Set<VerticalSpatialDomainType> verticalSpatialDomains) {
        this.verticalSpatialDomains = verticalSpatialDomains;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SpatialExtentType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("granuleLocalities");
        sb.append('=');
        sb.append(((this.granuleLocalities == null)?"<null>":this.granuleLocalities));
        sb.append(',');
        sb.append("horizontalSpatialDomain");
        sb.append('=');
        sb.append(((this.horizontalSpatialDomain == null)?"<null>":this.horizontalSpatialDomain));
        sb.append(',');
        sb.append("verticalSpatialDomains");
        sb.append('=');
        sb.append(((this.verticalSpatialDomains == null)?"<null>":this.verticalSpatialDomains));
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
        result = ((result* 31)+((this.granuleLocalities == null)? 0 :this.granuleLocalities.hashCode()));
        result = ((result* 31)+((this.horizontalSpatialDomain == null)? 0 :this.horizontalSpatialDomain.hashCode()));
        result = ((result* 31)+((this.verticalSpatialDomains == null)? 0 :this.verticalSpatialDomains.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SpatialExtentType) == false) {
            return false;
        }
        SpatialExtentType rhs = ((SpatialExtentType) other);
        return ((((this.granuleLocalities == rhs.granuleLocalities)||((this.granuleLocalities!= null)&&this.granuleLocalities.equals(rhs.granuleLocalities)))&&((this.horizontalSpatialDomain == rhs.horizontalSpatialDomain)||((this.horizontalSpatialDomain!= null)&&this.horizontalSpatialDomain.equals(rhs.horizontalSpatialDomain))))&&((this.verticalSpatialDomains == rhs.verticalSpatialDomains)||((this.verticalSpatialDomains!= null)&&this.verticalSpatialDomains.equals(rhs.verticalSpatialDomains))));
    }

}
