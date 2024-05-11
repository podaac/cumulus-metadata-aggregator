
package gov.nasa.cumulus.metadata.umm.generated;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * UMM-G
 * <p>
 * 
 * 
 */
@Generated("jsonschema2pojo")
public class Ummg166 {

    /**
     * The Universal Reference ID of the granule referred by the data provider. This ID is unique per data provider.
     * (Required)
     * 
     */
    @SerializedName("GranuleUR")
    @Expose
    private String granuleUR;
    /**
     * Dates related to activities involving the the granule and the data provider database with the exception for Delete. For Create, Update, and Insert the date is the date that the granule file is created, updated, or inserted into the provider database by the provider. Delete is the date that the CMR should delete the granule metadata record from its repository.
     * (Required)
     * 
     */
    @SerializedName("ProviderDates")
    @Expose
    private Set<ProviderDateType> providerDates = new LinkedHashSet<ProviderDateType>();
    /**
     * A reference to a collection metadata record's short name and version, or entry title to which this granule metadata record belongs.
     * (Required)
     * 
     */
    @SerializedName("CollectionReference")
    @Expose
    private CollectionReferenceType collectionReference;
    /**
     * Information about any physical constraints for accessing the data set.
     * 
     */
    @SerializedName("AccessConstraints")
    @Expose
    private AccessConstraintsType accessConstraints;
    /**
     * This entity stores the basic descriptive characteristics associated with a granule.
     * 
     */
    @SerializedName("DataGranule")
    @Expose
    private DataGranuleType dataGranule;
    /**
     * This entity stores basic descriptive characteristics related to the Product Generation Executable associated with a granule.
     * 
     */
    @SerializedName("PGEVersionClass")
    @Expose
    private PGEVersionClassType pGEVersionClass;
    /**
     * Information which describes the temporal extent of a specific granule.
     * 
     */
    @SerializedName("TemporalExtent")
    @Expose
    private TemporalExtentType temporalExtent;
    /**
     * This class contains attributes which describe the spatial extent of a granule. Spatial Extent includes any or all of Granule Localities, Horizontal Spatial Domain, and Vertical Spatial Domain.
     * 
     */
    @SerializedName("SpatialExtent")
    @Expose
    private SpatialExtentType spatialExtent;
    /**
     * This entity is used to store the characteristics of the orbit calculated spatial domain to include the model name, orbit number, start and stop orbit number, equator crossing date and time, and equator crossing longitude.
     * 
     */
    @SerializedName("OrbitCalculatedSpatialDomains")
    @Expose
    private Set<OrbitCalculatedSpatialDomainType> orbitCalculatedSpatialDomains = new LinkedHashSet<OrbitCalculatedSpatialDomainType>();
    /**
     * This entity contains the name of the geophysical parameter expressed in the data as well as associated quality flags and quality statistics. The quality statistics element contains measures of quality for the granule. The parameters used to set these measures are not preset and will be determined by the data producer. Each set of measures can occur many times either for the granule as a whole or for individual parameters. The quality flags contain the science, operational and automatic quality flags which indicate the overall quality assurance levels of specific parameter values within a granule.
     * 
     */
    @SerializedName("MeasuredParameters")
    @Expose
    private Set<MeasuredParameterType> measuredParameters = new LinkedHashSet<MeasuredParameterType>();
    /**
     * A reference to a platform in the parent collection that is associated with the acquisition of the granule. The platform must exist in the parent collection. For example, Platform types may include (but are not limited to): ADEOS-II, AEM-2, Terra, Aqua, Aura, BALLOONS, BUOYS, C-130, DEM, DMSP-F1,etc.
     * 
     */
    @SerializedName("Platforms")
    @Expose
    private Set<PlatformType> platforms = new LinkedHashSet<PlatformType>();
    /**
     * The name of the scientific program, field campaign, or project from which the data were collected. This element is intended for the non-space assets such as aircraft, ground systems, balloons, sondes, ships, etc. associated with campaigns. This element may also cover a long term project that continuously creates new data sets — like MEaSUREs from ISCCP and NVAP or CMARES from MISR. Project also includes the Campaign sub-element to support multiple campaigns under the same project.
     * 
     */
    @SerializedName("Projects")
    @Expose
    private Set<ProjectType> projects = new LinkedHashSet<ProjectType>();
    /**
     * Reference to an additional attribute in the parent collection. The attribute reference may contain a granule specific value that will override the value in the parent collection for this granule. An attribute with the same name must exist in the parent collection.
     * 
     */
    @SerializedName("AdditionalAttributes")
    @Expose
    private Set<AdditionalAttributeType> additionalAttributes = new LinkedHashSet<AdditionalAttributeType>();
    /**
     * This entity contains the identification of the input granule(s) for a specific granule.
     * 
     */
    @SerializedName("InputGranules")
    @Expose
    private Set<String> inputGranules = new LinkedHashSet<String>();
    /**
     * This entity stores the tiling identification system for the granule. The tiling identification system information is an alternative way to express granule's spatial coverage based on a certain two dimensional coordinate system defined by the providers. The name must match the name in the parent collection.
     * 
     */
    @SerializedName("TilingIdentificationSystem")
    @Expose
    private TilingIdentificationSystemType tilingIdentificationSystem;
    /**
     * A percentage value indicating how much of the area of a granule (the EOSDIS data unit) has been obscured by clouds. It is worth noting that there are many different measures of cloud cover within the EOSDIS data holdings and that the cloud cover parameter that is represented in the archive is dataset-specific.
     * 
     */
    @SerializedName("CloudCover")
    @Expose
    private Double cloudCover;
    /**
     * This element describes any data/service related URLs that include project home pages, services, related data archives/servers, metadata extensions, direct links to online software packages, web mapping services, links to images, or other data.
     * 
     */
    @SerializedName("RelatedUrls")
    @Expose
    private List<RelatedUrlType> relatedUrls = new ArrayList<RelatedUrlType>();
    /**
     * Represents the native projection of the granule if the granule has a native projection.
     * 
     */
    @SerializedName("NativeProjectionNames")
    @Expose
    private List<ProjectionNameType> nativeProjectionNames = new ArrayList<ProjectionNameType>();
    /**
     * Represents the native grid mapping of the granule, if the granule is gridded.
     * 
     */
    @SerializedName("GridMappingNames")
    @Expose
    private List<String> gridMappingNames = new ArrayList<String>();
    /**
     * This object requires any metadata record that is validated by this schema to provide information about the schema.
     * (Required)
     * 
     */
    @SerializedName("MetadataSpecification")
    @Expose
    private MetadataSpecificationType metadataSpecification;

    /**
     * The Universal Reference ID of the granule referred by the data provider. This ID is unique per data provider.
     * (Required)
     * 
     */
    public String getGranuleUR() {
        return granuleUR;
    }

    /**
     * The Universal Reference ID of the granule referred by the data provider. This ID is unique per data provider.
     * (Required)
     * 
     */
    public void setGranuleUR(String granuleUR) {
        this.granuleUR = granuleUR;
    }

    /**
     * Dates related to activities involving the the granule and the data provider database with the exception for Delete. For Create, Update, and Insert the date is the date that the granule file is created, updated, or inserted into the provider database by the provider. Delete is the date that the CMR should delete the granule metadata record from its repository.
     * (Required)
     * 
     */
    public Set<ProviderDateType> getProviderDates() {
        return providerDates;
    }

    /**
     * Dates related to activities involving the the granule and the data provider database with the exception for Delete. For Create, Update, and Insert the date is the date that the granule file is created, updated, or inserted into the provider database by the provider. Delete is the date that the CMR should delete the granule metadata record from its repository.
     * (Required)
     * 
     */
    public void setProviderDates(Set<ProviderDateType> providerDates) {
        this.providerDates = providerDates;
    }

    /**
     * A reference to a collection metadata record's short name and version, or entry title to which this granule metadata record belongs.
     * (Required)
     * 
     */
    public CollectionReferenceType getCollectionReference() {
        return collectionReference;
    }

    /**
     * A reference to a collection metadata record's short name and version, or entry title to which this granule metadata record belongs.
     * (Required)
     * 
     */
    public void setCollectionReference(CollectionReferenceType collectionReference) {
        this.collectionReference = collectionReference;
    }

    /**
     * Information about any physical constraints for accessing the data set.
     * 
     */
    public AccessConstraintsType getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * Information about any physical constraints for accessing the data set.
     * 
     */
    public void setAccessConstraints(AccessConstraintsType accessConstraints) {
        this.accessConstraints = accessConstraints;
    }

    /**
     * This entity stores the basic descriptive characteristics associated with a granule.
     * 
     */
    public DataGranuleType getDataGranule() {
        return dataGranule;
    }

    /**
     * This entity stores the basic descriptive characteristics associated with a granule.
     * 
     */
    public void setDataGranule(DataGranuleType dataGranule) {
        this.dataGranule = dataGranule;
    }

    /**
     * This entity stores basic descriptive characteristics related to the Product Generation Executable associated with a granule.
     * 
     */
    public PGEVersionClassType getPGEVersionClass() {
        return pGEVersionClass;
    }

    /**
     * This entity stores basic descriptive characteristics related to the Product Generation Executable associated with a granule.
     * 
     */
    public void setPGEVersionClass(PGEVersionClassType pGEVersionClass) {
        this.pGEVersionClass = pGEVersionClass;
    }

    /**
     * Information which describes the temporal extent of a specific granule.
     * 
     */
    public TemporalExtentType getTemporalExtent() {
        return temporalExtent;
    }

    /**
     * Information which describes the temporal extent of a specific granule.
     * 
     */
    public void setTemporalExtent(TemporalExtentType temporalExtent) {
        this.temporalExtent = temporalExtent;
    }

    /**
     * This class contains attributes which describe the spatial extent of a granule. Spatial Extent includes any or all of Granule Localities, Horizontal Spatial Domain, and Vertical Spatial Domain.
     * 
     */
    public SpatialExtentType getSpatialExtent() {
        return spatialExtent;
    }

    /**
     * This class contains attributes which describe the spatial extent of a granule. Spatial Extent includes any or all of Granule Localities, Horizontal Spatial Domain, and Vertical Spatial Domain.
     * 
     */
    public void setSpatialExtent(SpatialExtentType spatialExtent) {
        this.spatialExtent = spatialExtent;
    }

    /**
     * This entity is used to store the characteristics of the orbit calculated spatial domain to include the model name, orbit number, start and stop orbit number, equator crossing date and time, and equator crossing longitude.
     * 
     */
    public Set<OrbitCalculatedSpatialDomainType> getOrbitCalculatedSpatialDomains() {
        return orbitCalculatedSpatialDomains;
    }

    /**
     * This entity is used to store the characteristics of the orbit calculated spatial domain to include the model name, orbit number, start and stop orbit number, equator crossing date and time, and equator crossing longitude.
     * 
     */
    public void setOrbitCalculatedSpatialDomains(Set<OrbitCalculatedSpatialDomainType> orbitCalculatedSpatialDomains) {
        this.orbitCalculatedSpatialDomains = orbitCalculatedSpatialDomains;
    }

    /**
     * This entity contains the name of the geophysical parameter expressed in the data as well as associated quality flags and quality statistics. The quality statistics element contains measures of quality for the granule. The parameters used to set these measures are not preset and will be determined by the data producer. Each set of measures can occur many times either for the granule as a whole or for individual parameters. The quality flags contain the science, operational and automatic quality flags which indicate the overall quality assurance levels of specific parameter values within a granule.
     * 
     */
    public Set<MeasuredParameterType> getMeasuredParameters() {
        return measuredParameters;
    }

    /**
     * This entity contains the name of the geophysical parameter expressed in the data as well as associated quality flags and quality statistics. The quality statistics element contains measures of quality for the granule. The parameters used to set these measures are not preset and will be determined by the data producer. Each set of measures can occur many times either for the granule as a whole or for individual parameters. The quality flags contain the science, operational and automatic quality flags which indicate the overall quality assurance levels of specific parameter values within a granule.
     * 
     */
    public void setMeasuredParameters(Set<MeasuredParameterType> measuredParameters) {
        this.measuredParameters = measuredParameters;
    }

    /**
     * A reference to a platform in the parent collection that is associated with the acquisition of the granule. The platform must exist in the parent collection. For example, Platform types may include (but are not limited to): ADEOS-II, AEM-2, Terra, Aqua, Aura, BALLOONS, BUOYS, C-130, DEM, DMSP-F1,etc.
     * 
     */
    public Set<PlatformType> getPlatforms() {
        return platforms;
    }

    /**
     * A reference to a platform in the parent collection that is associated with the acquisition of the granule. The platform must exist in the parent collection. For example, Platform types may include (but are not limited to): ADEOS-II, AEM-2, Terra, Aqua, Aura, BALLOONS, BUOYS, C-130, DEM, DMSP-F1,etc.
     * 
     */
    public void setPlatforms(Set<PlatformType> platforms) {
        this.platforms = platforms;
    }

    /**
     * The name of the scientific program, field campaign, or project from which the data were collected. This element is intended for the non-space assets such as aircraft, ground systems, balloons, sondes, ships, etc. associated with campaigns. This element may also cover a long term project that continuously creates new data sets — like MEaSUREs from ISCCP and NVAP or CMARES from MISR. Project also includes the Campaign sub-element to support multiple campaigns under the same project.
     * 
     */
    public Set<ProjectType> getProjects() {
        return projects;
    }

    /**
     * The name of the scientific program, field campaign, or project from which the data were collected. This element is intended for the non-space assets such as aircraft, ground systems, balloons, sondes, ships, etc. associated with campaigns. This element may also cover a long term project that continuously creates new data sets — like MEaSUREs from ISCCP and NVAP or CMARES from MISR. Project also includes the Campaign sub-element to support multiple campaigns under the same project.
     * 
     */
    public void setProjects(Set<ProjectType> projects) {
        this.projects = projects;
    }

    /**
     * Reference to an additional attribute in the parent collection. The attribute reference may contain a granule specific value that will override the value in the parent collection for this granule. An attribute with the same name must exist in the parent collection.
     * 
     */
    public Set<AdditionalAttributeType> getAdditionalAttributes() {
        return additionalAttributes;
    }

    /**
     * Reference to an additional attribute in the parent collection. The attribute reference may contain a granule specific value that will override the value in the parent collection for this granule. An attribute with the same name must exist in the parent collection.
     * 
     */
    public void setAdditionalAttributes(Set<AdditionalAttributeType> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    /**
     * This entity contains the identification of the input granule(s) for a specific granule.
     * 
     */
    public Set<String> getInputGranules() {
        return inputGranules;
    }

    /**
     * This entity contains the identification of the input granule(s) for a specific granule.
     * 
     */
    public void setInputGranules(Set<String> inputGranules) {
        this.inputGranules = inputGranules;
    }

    /**
     * This entity stores the tiling identification system for the granule. The tiling identification system information is an alternative way to express granule's spatial coverage based on a certain two dimensional coordinate system defined by the providers. The name must match the name in the parent collection.
     * 
     */
    public TilingIdentificationSystemType getTilingIdentificationSystem() {
        return tilingIdentificationSystem;
    }

    /**
     * This entity stores the tiling identification system for the granule. The tiling identification system information is an alternative way to express granule's spatial coverage based on a certain two dimensional coordinate system defined by the providers. The name must match the name in the parent collection.
     * 
     */
    public void setTilingIdentificationSystem(TilingIdentificationSystemType tilingIdentificationSystem) {
        this.tilingIdentificationSystem = tilingIdentificationSystem;
    }

    /**
     * A percentage value indicating how much of the area of a granule (the EOSDIS data unit) has been obscured by clouds. It is worth noting that there are many different measures of cloud cover within the EOSDIS data holdings and that the cloud cover parameter that is represented in the archive is dataset-specific.
     * 
     */
    public Double getCloudCover() {
        return cloudCover;
    }

    /**
     * A percentage value indicating how much of the area of a granule (the EOSDIS data unit) has been obscured by clouds. It is worth noting that there are many different measures of cloud cover within the EOSDIS data holdings and that the cloud cover parameter that is represented in the archive is dataset-specific.
     * 
     */
    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    /**
     * This element describes any data/service related URLs that include project home pages, services, related data archives/servers, metadata extensions, direct links to online software packages, web mapping services, links to images, or other data.
     * 
     */
    public List<RelatedUrlType> getRelatedUrls() {
        return relatedUrls;
    }

    /**
     * This element describes any data/service related URLs that include project home pages, services, related data archives/servers, metadata extensions, direct links to online software packages, web mapping services, links to images, or other data.
     * 
     */
    public void setRelatedUrls(List<RelatedUrlType> relatedUrls) {
        this.relatedUrls = relatedUrls;
    }

    /**
     * Represents the native projection of the granule if the granule has a native projection.
     * 
     */
    public List<ProjectionNameType> getNativeProjectionNames() {
        return nativeProjectionNames;
    }

    /**
     * Represents the native projection of the granule if the granule has a native projection.
     * 
     */
    public void setNativeProjectionNames(List<ProjectionNameType> nativeProjectionNames) {
        this.nativeProjectionNames = nativeProjectionNames;
    }

    /**
     * Represents the native grid mapping of the granule, if the granule is gridded.
     * 
     */
    public List<String> getGridMappingNames() {
        return gridMappingNames;
    }

    /**
     * Represents the native grid mapping of the granule, if the granule is gridded.
     * 
     */
    public void setGridMappingNames(List<String> gridMappingNames) {
        this.gridMappingNames = gridMappingNames;
    }

    /**
     * This object requires any metadata record that is validated by this schema to provide information about the schema.
     * (Required)
     * 
     */
    public MetadataSpecificationType getMetadataSpecification() {
        return metadataSpecification;
    }

    /**
     * This object requires any metadata record that is validated by this schema to provide information about the schema.
     * (Required)
     * 
     */
    public void setMetadataSpecification(MetadataSpecificationType metadataSpecification) {
        this.metadataSpecification = metadataSpecification;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Ummg166 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("granuleUR");
        sb.append('=');
        sb.append(((this.granuleUR == null)?"<null>":this.granuleUR));
        sb.append(',');
        sb.append("providerDates");
        sb.append('=');
        sb.append(((this.providerDates == null)?"<null>":this.providerDates));
        sb.append(',');
        sb.append("collectionReference");
        sb.append('=');
        sb.append(((this.collectionReference == null)?"<null>":this.collectionReference));
        sb.append(',');
        sb.append("accessConstraints");
        sb.append('=');
        sb.append(((this.accessConstraints == null)?"<null>":this.accessConstraints));
        sb.append(',');
        sb.append("dataGranule");
        sb.append('=');
        sb.append(((this.dataGranule == null)?"<null>":this.dataGranule));
        sb.append(',');
        sb.append("pGEVersionClass");
        sb.append('=');
        sb.append(((this.pGEVersionClass == null)?"<null>":this.pGEVersionClass));
        sb.append(',');
        sb.append("temporalExtent");
        sb.append('=');
        sb.append(((this.temporalExtent == null)?"<null>":this.temporalExtent));
        sb.append(',');
        sb.append("spatialExtent");
        sb.append('=');
        sb.append(((this.spatialExtent == null)?"<null>":this.spatialExtent));
        sb.append(',');
        sb.append("orbitCalculatedSpatialDomains");
        sb.append('=');
        sb.append(((this.orbitCalculatedSpatialDomains == null)?"<null>":this.orbitCalculatedSpatialDomains));
        sb.append(',');
        sb.append("measuredParameters");
        sb.append('=');
        sb.append(((this.measuredParameters == null)?"<null>":this.measuredParameters));
        sb.append(',');
        sb.append("platforms");
        sb.append('=');
        sb.append(((this.platforms == null)?"<null>":this.platforms));
        sb.append(',');
        sb.append("projects");
        sb.append('=');
        sb.append(((this.projects == null)?"<null>":this.projects));
        sb.append(',');
        sb.append("additionalAttributes");
        sb.append('=');
        sb.append(((this.additionalAttributes == null)?"<null>":this.additionalAttributes));
        sb.append(',');
        sb.append("inputGranules");
        sb.append('=');
        sb.append(((this.inputGranules == null)?"<null>":this.inputGranules));
        sb.append(',');
        sb.append("tilingIdentificationSystem");
        sb.append('=');
        sb.append(((this.tilingIdentificationSystem == null)?"<null>":this.tilingIdentificationSystem));
        sb.append(',');
        sb.append("cloudCover");
        sb.append('=');
        sb.append(((this.cloudCover == null)?"<null>":this.cloudCover));
        sb.append(',');
        sb.append("relatedUrls");
        sb.append('=');
        sb.append(((this.relatedUrls == null)?"<null>":this.relatedUrls));
        sb.append(',');
        sb.append("nativeProjectionNames");
        sb.append('=');
        sb.append(((this.nativeProjectionNames == null)?"<null>":this.nativeProjectionNames));
        sb.append(',');
        sb.append("gridMappingNames");
        sb.append('=');
        sb.append(((this.gridMappingNames == null)?"<null>":this.gridMappingNames));
        sb.append(',');
        sb.append("metadataSpecification");
        sb.append('=');
        sb.append(((this.metadataSpecification == null)?"<null>":this.metadataSpecification));
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
        result = ((result* 31)+((this.nativeProjectionNames == null)? 0 :this.nativeProjectionNames.hashCode()));
        result = ((result* 31)+((this.collectionReference == null)? 0 :this.collectionReference.hashCode()));
        result = ((result* 31)+((this.projects == null)? 0 :this.projects.hashCode()));
        result = ((result* 31)+((this.dataGranule == null)? 0 :this.dataGranule.hashCode()));
        result = ((result* 31)+((this.metadataSpecification == null)? 0 :this.metadataSpecification.hashCode()));
        result = ((result* 31)+((this.cloudCover == null)? 0 :this.cloudCover.hashCode()));
        result = ((result* 31)+((this.tilingIdentificationSystem == null)? 0 :this.tilingIdentificationSystem.hashCode()));
        result = ((result* 31)+((this.orbitCalculatedSpatialDomains == null)? 0 :this.orbitCalculatedSpatialDomains.hashCode()));
        result = ((result* 31)+((this.platforms == null)? 0 :this.platforms.hashCode()));
        result = ((result* 31)+((this.granuleUR == null)? 0 :this.granuleUR.hashCode()));
        result = ((result* 31)+((this.measuredParameters == null)? 0 :this.measuredParameters.hashCode()));
        result = ((result* 31)+((this.providerDates == null)? 0 :this.providerDates.hashCode()));
        result = ((result* 31)+((this.relatedUrls == null)? 0 :this.relatedUrls.hashCode()));
        result = ((result* 31)+((this.spatialExtent == null)? 0 :this.spatialExtent.hashCode()));
        result = ((result* 31)+((this.gridMappingNames == null)? 0 :this.gridMappingNames.hashCode()));
        result = ((result* 31)+((this.temporalExtent == null)? 0 :this.temporalExtent.hashCode()));
        result = ((result* 31)+((this.accessConstraints == null)? 0 :this.accessConstraints.hashCode()));
        result = ((result* 31)+((this.pGEVersionClass == null)? 0 :this.pGEVersionClass.hashCode()));
        result = ((result* 31)+((this.additionalAttributes == null)? 0 :this.additionalAttributes.hashCode()));
        result = ((result* 31)+((this.inputGranules == null)? 0 :this.inputGranules.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Ummg166) == false) {
            return false;
        }
        Ummg166 rhs = ((Ummg166) other);
        return (((((((((((((((((((((this.nativeProjectionNames == rhs.nativeProjectionNames)||((this.nativeProjectionNames!= null)&&this.nativeProjectionNames.equals(rhs.nativeProjectionNames)))&&((this.collectionReference == rhs.collectionReference)||((this.collectionReference!= null)&&this.collectionReference.equals(rhs.collectionReference))))&&((this.projects == rhs.projects)||((this.projects!= null)&&this.projects.equals(rhs.projects))))&&((this.dataGranule == rhs.dataGranule)||((this.dataGranule!= null)&&this.dataGranule.equals(rhs.dataGranule))))&&((this.metadataSpecification == rhs.metadataSpecification)||((this.metadataSpecification!= null)&&this.metadataSpecification.equals(rhs.metadataSpecification))))&&((this.cloudCover == rhs.cloudCover)||((this.cloudCover!= null)&&this.cloudCover.equals(rhs.cloudCover))))&&((this.tilingIdentificationSystem == rhs.tilingIdentificationSystem)||((this.tilingIdentificationSystem!= null)&&this.tilingIdentificationSystem.equals(rhs.tilingIdentificationSystem))))&&((this.orbitCalculatedSpatialDomains == rhs.orbitCalculatedSpatialDomains)||((this.orbitCalculatedSpatialDomains!= null)&&this.orbitCalculatedSpatialDomains.equals(rhs.orbitCalculatedSpatialDomains))))&&((this.platforms == rhs.platforms)||((this.platforms!= null)&&this.platforms.equals(rhs.platforms))))&&((this.granuleUR == rhs.granuleUR)||((this.granuleUR!= null)&&this.granuleUR.equals(rhs.granuleUR))))&&((this.measuredParameters == rhs.measuredParameters)||((this.measuredParameters!= null)&&this.measuredParameters.equals(rhs.measuredParameters))))&&((this.providerDates == rhs.providerDates)||((this.providerDates!= null)&&this.providerDates.equals(rhs.providerDates))))&&((this.relatedUrls == rhs.relatedUrls)||((this.relatedUrls!= null)&&this.relatedUrls.equals(rhs.relatedUrls))))&&((this.spatialExtent == rhs.spatialExtent)||((this.spatialExtent!= null)&&this.spatialExtent.equals(rhs.spatialExtent))))&&((this.gridMappingNames == rhs.gridMappingNames)||((this.gridMappingNames!= null)&&this.gridMappingNames.equals(rhs.gridMappingNames))))&&((this.temporalExtent == rhs.temporalExtent)||((this.temporalExtent!= null)&&this.temporalExtent.equals(rhs.temporalExtent))))&&((this.accessConstraints == rhs.accessConstraints)||((this.accessConstraints!= null)&&this.accessConstraints.equals(rhs.accessConstraints))))&&((this.pGEVersionClass == rhs.pGEVersionClass)||((this.pGEVersionClass!= null)&&this.pGEVersionClass.equals(rhs.pGEVersionClass))))&&((this.additionalAttributes == rhs.additionalAttributes)||((this.additionalAttributes!= null)&&this.additionalAttributes.equals(rhs.additionalAttributes))))&&((this.inputGranules == rhs.inputGranules)||((this.inputGranules!= null)&&this.inputGranules.equals(rhs.inputGranules))));
    }

}
