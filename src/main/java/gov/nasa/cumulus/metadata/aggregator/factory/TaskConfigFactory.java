package gov.nasa.cumulus.metadata.aggregator.factory;

import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.metadata.aggregator.bo.TaskConfigBO;
import gov.nasa.cumulus.metadata.state.MENDsIsoXMLSpatialTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskConfigFactory {
    static String className = "gov.nasa.cumulus.metadata.aggregator.factory.TaskConfigFactory";

    public static TaskConfigBO createTaskConfigBO(JSONObject config) {
        TaskConfigBO taskConfigBO = new TaskConfigBO();
        //Construct isoXMLSpatialTypeHashset
        JSONArray isoXMLSpatialTypeJsonArray = (JSONArray) config.get("isoXMLSpatialType");
        if(isoXMLSpatialTypeJsonArray !=null && isoXMLSpatialTypeJsonArray.size()>0) {
            taskConfigBO.setIsoXMLSpatialTypeHashSet(createIsoXMLSpatialTypeSet(isoXMLSpatialTypeJsonArray));
        }
        //Construct subTypeHashArray
        JSONArray subTypeTypeJsonArray = (JSONArray) config.get("relatedUrlSubTypeMap");
        if(subTypeTypeJsonArray!=null && subTypeTypeJsonArray.size()>0) {
            taskConfigBO.setSubTypeHashArray(createSubTypeHashArray(subTypeTypeJsonArray));
        }
        return taskConfigBO;

    }

    public static HashSet<MENDsIsoXMLSpatialTypeEnum> createIsoXMLSpatialTypeSet(JSONArray isoXMLSpatialTypeConfigJSONArray) throws IllegalArgumentException{
        HashSet<MENDsIsoXMLSpatialTypeEnum> isoSpatialTypes = new HashSet<>();
        // if not containing isoXMLTypes, then return an empty HashSet
        if(isoXMLSpatialTypeConfigJSONArray == null || isoXMLSpatialTypeConfigJSONArray.size()==0) {
            return isoSpatialTypes;
        }
        isoXMLSpatialTypeConfigJSONArray.forEach(item -> {
            String t = (String) item;
            MENDsIsoXMLSpatialTypeEnum en = MENDsIsoXMLSpatialTypeEnum.getEnum(getIsoXMLSpatialTypeStr(t));
            isoSpatialTypes.add(en);
        });
        AdapterLogger.LogDebug(className + " isoSpatialTypes HashSet: " + isoSpatialTypes);
        return isoSpatialTypes;
    }

    public static String getIsoXMLSpatialTypeStr(String token) {
        final String trimmedToken = StringUtils.trim(token);
        String s;
        try {
            s = MENDsIsoXMLSpatialTypeEnum.getEnumValuList().stream()
                    .filter(e -> StringUtils.equals(trimmedToken, e)).findFirst().get();
        } catch (java.util.NoSuchElementException e) {
            s = "";
        }
        return s;
    }

    public static ArrayList<HashMap<String, String>> createSubTypeHashArray(JSONArray jsonArray) {
        ArrayList<HashMap<String, String>> subTypeHashArray = new ArrayList<>();
        if(jsonArray !=null && jsonArray.size()>0) {
            jsonArray.forEach(item -> {
                String regex = ((JSONObject) item).get("regex").toString();
                String subType = ((JSONObject) item).get("subType").toString();
                HashMap<String, String> mapper = new HashMap<>();
                mapper.put("regex", regex);
                mapper.put("subType", subType);
                subTypeHashArray.add(mapper);
            });
        }
        return subTypeHashArray;
    }

}
