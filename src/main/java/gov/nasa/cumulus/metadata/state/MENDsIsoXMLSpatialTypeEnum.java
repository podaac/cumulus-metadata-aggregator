package gov.nasa.cumulus.metadata.state;

import gov.nasa.cumulus.metadata.aggregator.constant.MENDsIsoXMLSpatialTypeConstant;
import org.apache.commons.lang3.StringUtils;

public enum MENDsIsoXMLSpatialTypeEnum {
    FOOTPRINT("footprint"), ORBIT("orbit"), BBOX("bbox"), NONE("none");
    private String Status;
    private MENDsIsoXMLSpatialTypeEnum(String type) {
        this.Status = type;
    }

    public String toString()
    {
        return this.Status;
    }

    public static MENDsIsoXMLSpatialTypeEnum getEnum(String val)
    {
        val= StringUtils.trim(val);
        if(StringUtils.equals(val, MENDsIsoXMLSpatialTypeConstant.FOOTPRINT))
            return FOOTPRINT;
        else if (StringUtils.equals(val, MENDsIsoXMLSpatialTypeConstant.ORBIT))
            return ORBIT;
        else if (StringUtils.equals(val, MENDsIsoXMLSpatialTypeConstant.BBOX))
            return BBOX;
        else
            return NONE;
    }

    public String getID()
    {
        return this.Status;
    }
}
