package gov.nasa.cumulus.metadata.aggregator;


import gov.nasa.cumulus.metadata.umm.generated.AdditionalAttributeType;
import gov.nasa.cumulus.metadata.umm.generated.TrackType;
import gov.nasa.podaac.inventory.model.Granule;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class UMMGranule extends Granule {
    /**
     * A generated type which represents the cycle and pass
     */
    private TrackType trackType;
    /**
     * A generated type which represents the pass and associate Tiles under Track
     */
    private List<String> basinIds;
    private List<AdditionalAttributeType> additionalAttributeTypes;
    private Integer orbitNumber;
    private Integer startOrbit;
    private Integer endOrbit;
    private String tile;
    /* Bounding Box 4 points */
    private Double bbxNorthernLatitude;
    private Double bbxSouthernLatitude;
    private Double bbxEasternLongitude;
    private Double bbxWesternLongitude;
    private BigDecimal equatorCrossingLongitude;
    private String equatorCrossingDateTime;
    private JSONObject dynamicAttributeNameMapping;

    public Integer getOrbitNumber() {
        return orbitNumber;
    }

    public void setOrbitNumber(Integer orbitNumber) {
        this.orbitNumber = orbitNumber;
    }
    public Integer getStartOrbit() {
        return startOrbit;
    }

    public void setStartOrbit(Integer startOrbit) {
        this.startOrbit = startOrbit;
    }

    public Integer getEndOrbit() {
        return endOrbit;
    }

    public void setEndOrbit(Integer endOrbit) {
        this.endOrbit = endOrbit;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    public TrackType getTrackType() {
        return trackType;
    }

    public void setTrackType(TrackType trackType) {
        this.trackType = trackType;
    }

    public List<String> getBasinIds() {
        return basinIds;
    }

    public void setBasinIds(List<String> basinIds) {
        this.basinIds = basinIds;
    }

    public List<AdditionalAttributeType> getAdditionalAttributeTypes() {
        return additionalAttributeTypes;
    }

    public void setAdditionalAttributeTypes(List<AdditionalAttributeType> additionalAttributeTypes) {
        this.additionalAttributeTypes = additionalAttributeTypes;
    }

    public Double getBbxNorthernLatitude() {
        return bbxNorthernLatitude;
    }

    public void setBbxNorthernLatitude(Double bbxNorthernLatitude) {
        this.bbxNorthernLatitude = bbxNorthernLatitude;
    }

    public Double getBbxSouthernLatitude() {
        return bbxSouthernLatitude;
    }

    public void setBbxSouthernLatitude(Double bbxSouthernLatitude) {
        this.bbxSouthernLatitude = bbxSouthernLatitude;
    }

    public Double getBbxEasternLongitude() {
        return bbxEasternLongitude;
    }

    public void setBbxEasternLongitude(Double bbxEasternLongitude) {
        this.bbxEasternLongitude = bbxEasternLongitude;
    }

    public Double getBbxWesternLongitude() {
        return bbxWesternLongitude;
    }

    public void setBbxWesternLongitude(Double bbxWesternLongitude) {
        this.bbxWesternLongitude = bbxWesternLongitude;
    }

    public String getEquatorCrossingDateTime() {
        return equatorCrossingDateTime;
    }

    public void setEquatorCrossingDateTime(String equatorCrossingDateTime) {
        this.equatorCrossingDateTime = equatorCrossingDateTime;
    }

    public BigDecimal getEquatorCrossingLongitude() {
        return equatorCrossingLongitude;
    }

    public void setEquatorCrossingLongitude(BigDecimal equatorCrossingLongitude) {
        this.equatorCrossingLongitude = equatorCrossingLongitude;
    }

    public JSONObject getDynamicAttributeNameMapping() {
        return dynamicAttributeNameMapping;
    }

    public void setDynamicAttributeNameMapping(JSONObject dynamicAttributeNameMapping) {
        this.dynamicAttributeNameMapping = dynamicAttributeNameMapping;
    }
}
