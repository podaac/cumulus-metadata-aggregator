package gov.nasa.cumulus.metadata.aggregator.bo;

import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskConfigBO {
    HashSet<MENDsIsoXMLSpatialTypeEnum> isoXMLSpatialTypeHashSet;
    ArrayList<HashMap<String, String>> subTypeHashArray = new ArrayList<>();

    public TaskConfigBO() {}

    public HashSet<MENDsIsoXMLSpatialTypeEnum> getIsoXMLSpatialTypeHashSet() {
        return isoXMLSpatialTypeHashSet;
    }

    public void setIsoXMLSpatialTypeHashSet(HashSet<MENDsIsoXMLSpatialTypeEnum> isoXMLSpatialTypeHashSet) {
        this.isoXMLSpatialTypeHashSet = isoXMLSpatialTypeHashSet;
    }

    public ArrayList<HashMap<String, String>> getSubTypeHashArray() {
        return subTypeHashArray;
    }

    public void setSubTypeHashArray(ArrayList<HashMap<String, String>> subTypeHashArray) {
        this.subTypeHashArray = subTypeHashArray;
    }
}
