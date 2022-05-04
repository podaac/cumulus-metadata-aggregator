package gov.nasa.cumulus.metadata.umm.model;

import gov.nasa.podaac.inventory.model.GranuleArchive;

public class UMMGranuleArchive extends GranuleArchive {
    private String checksumAlgorithm;
    public String getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }
}
