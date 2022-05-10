package gov.nasa.cumulus.metadata.aggregator;


import gov.nasa.podaac.inventory.model.Granule;

import java.math.BigDecimal;

public class UMMGranule extends Granule {
    private Integer cycle;
    private Integer pass;
    private Integer orbitNumber;
    private Integer startOrbit;
    private Integer endOrbit;
    /* Bounding Box 4 points */
    private Double bbxNorthernLatitude;
    private Double bbxSouthernLatitude;
    private Double bbxEasternLongitude;
    private Double bbxWesternLongitude;
    private BigDecimal equatorCrossingLongitude;
    private String equatorCrossingDateTime;

    public Integer getPass() {
        return pass;
    }

    public void setPass(Integer pass) {
        this.pass = pass;
    }

    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

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

}
