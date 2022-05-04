package gov.nasa.cumulus.metadata.aggregator;

import gov.nasa.podaac.inventory.model.GranuleArchive;

public class IsoGranuleArchive extends GranuleArchive {
    private String sizeUnit;
    private String mimeType;
    private String checksumAlgorithm;
    private double QAPercentMissingData;

    public String getSizeUnit() {
        return this.sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    public double getQAPercentMissingData() {
        return this.QAPercentMissingData;
    }

    public void setQAPercentMissingData(double QAPercentMissingData) {
        this.QAPercentMissingData = QAPercentMissingData;
    }
}
