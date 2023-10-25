package gov.nasa.cumulus.metadata.state;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public enum MENDsIsoXMLSpatialTypeEnum {

    FOOTPRINT("footprint"), ORBIT("orbit"), BBOX("bbox"), NONE("none");
    private String Status;
    private static String FOOTPRINT_STR="footprint";
    private static String ORBIT_STR="orbit";
    private static String BBOX_STR="bbox";
    private MENDsIsoXMLSpatialTypeEnum(String type) {
        this.Status = type;
    }

    public String toString()
    {
        return this.Status;
    }

    public static List<String> getEnumValuList() {
        final List<String> isoXMLSpatialTypeList = Arrays.asList(FOOTPRINT_STR, BBOX_STR, ORBIT_STR);
        return isoXMLSpatialTypeList;
    }

    public static MENDsIsoXMLSpatialTypeEnum getEnum(String val)
    {
        val= StringUtils.trim(val);
        if(StringUtils.equals(val, FOOTPRINT_STR))
            return FOOTPRINT;
        else if (StringUtils.equals(val, ORBIT_STR))
            return ORBIT;
        else if (StringUtils.equals(val, BBOX_STR))
            return BBOX;
        else
            return NONE;
    }

    public String getID()
    {
        return this.Status;
    }
}
