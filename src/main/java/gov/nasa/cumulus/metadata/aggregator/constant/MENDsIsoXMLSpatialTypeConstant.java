package gov.nasa.cumulus.metadata.aggregator.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MENDsIsoXMLSpatialTypeConstant {
    public static final String FOOTPRINT = "footprint";
    public static final String BBOX = "bbox";
    public static final String ORBIT = "orbit";
    public static final List<String> isoXMLSpatialTypeList = Arrays.asList(FOOTPRINT, BBOX, ORBIT);
}
