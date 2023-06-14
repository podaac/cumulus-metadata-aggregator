package gov.nasa.cumulus.metadata.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IsoGranule extends UMMGranule {

    private String parameterName;
    private double QAPercentMissingData;
    private double QAPercentOutOfBoundsData;
    private String producerGranuleId;
    private String crid;
    private HashMap<String, String> identifiers;
    private String reprocessingPlanned;
    private String reprocessingActual;
    private String swotTrack;
    private String orbit;
    private String polygon;
    private List<String> inputGranules;
    private String PGEVersionClass;

    private IsoType isoType;
    private int orientation;

    public IsoGranule() {
        this.identifiers = new HashMap<>();
        this.inputGranules = new ArrayList<>();
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public double getQAPercentMissingData() {
        return this.QAPercentMissingData;
    }

    public void setQAPercentMissingData(double QAPercentMissingData) {
        this.QAPercentMissingData = QAPercentMissingData;
    }

    public double getQAPercentOutOfBoundsData() {
        return this.QAPercentOutOfBoundsData;
    }

    public void setQAPercentOutOfBoundsData(double QAPercentOutOfBoundsData) {
        this.QAPercentOutOfBoundsData = QAPercentOutOfBoundsData;
    }

    public String getProducerGranuleId() {
        return this.producerGranuleId;
    }

    public void setProducerGranuleId(String producerGranuleId) {
        this.producerGranuleId = producerGranuleId;
    }

    public String getCrid() {
        return this.crid;
    }

    public void setCrid(String crid) {
        this.crid = crid;
    }

    public String getReprocessingPlanned() {
        return this.reprocessingPlanned;
    }

    public void setReprocessingPlanned(String reprocessingPlanned) {
        this.reprocessingPlanned = reprocessingPlanned;
    }

    public String getReprocessingActual() {
        return this.reprocessingActual;
    }

    public void setReprocessingActual(String reprocessingActual) {
        this.reprocessingActual = reprocessingActual;
    }

    public String getSwotTrack() {
        return this.swotTrack;
    }

    public void setSwotTrack(String swotTrack) {
        this.swotTrack = swotTrack;
    }

    public String getOrbit() {
        return this.orbit;
    }

    public void setOrbit(String orbit) {
        this.orbit = orbit;
    }

    public String getPolygon() {
        return this.polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void addIdentifier(String name, String value) {
        this.identifiers.put(name, value);
    }

    public HashMap<String, String> getIdentifiers() {
        return this.identifiers;
    }

    public void addInputGranule(String inputGranule) {
        this.inputGranules.add(inputGranule);
    }

    public List<String> getInputGranules() {
        return this.inputGranules;
    }

    public String getPGEVersionClass() {
        return this.PGEVersionClass;
    }

    public void setPGEVersionClass(String PGEVersionClass) {
        this.PGEVersionClass = PGEVersionClass;
    }

    public IsoType getIsoType() {
        return isoType;
    }

    public void setIsoType(IsoType isoType) {
        this.isoType = isoType;
    }
}
