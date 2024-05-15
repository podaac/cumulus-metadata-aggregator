package gov.nasa.cumulus.metadata.aggregator.processor;

import gov.nasa.cumulus.metadata.aggregator.bo.TaskConfigBO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * this class is a post-processing of created umm-g
 * by appending relatedUrl items into RelatedUrls array based on
 *
 * if any item within payload.granules[0].files[] matched collection configurations meta.subTypeMap array's
 * regex
 */
public class RelatedUrlsProcessor {
    public void RelatedUrlsProcessor() {
    }

    public JSONObject appendSubTypes(JSONObject granuleJson, TaskConfigBO taskConfigBO, JSONArray files) {
        ArrayList<HashMap<String, String>> subTypeHashArray=taskConfigBO.getSubTypeHashArray();
        if(subTypeHashArray==null || subTypeHashArray.size()==0) {
            return granuleJson;
        }
        JSONArray relateUrlsArray =  (JSONArray) granuleJson.get("RelatedUrls");
        // 1: Loop through the existing RelatedUrls array to find any match for the subType regex.
        //    for the finding match, set Subtype to
        for(int i=0; i<relateUrlsArray.size(); i++) {
            JSONObject relatedUrlJson = (JSONObject) relateUrlsArray.get(i);
            String URLStr=relatedUrlJson.get("URL").toString();
            for(HashMap<String, String> subTypeHash : subTypeHashArray) {
                if(URLStr.matches(subTypeHash.get("regex"))) {
                    relatedUrlJson.put("Subtype", subTypeHash.get("subType"));
                }
            }
        }
        // 2: Loop through the existing Input files:[] array to find any match for the subType regex.
        //    for the finding match, append a new RelatedUrl item with Subtype set to subTypeHash.get("subType")
        for(Object file: files) {
            JSONObject fileJson = (JSONObject) file;
            String source = fileJson.get("source").toString();
            for(HashMap<String, String> subTypeHash : subTypeHashArray) {
                if(source.matches(subTypeHash.get("regex"))) {
                    JSONObject relatedUrl = new JSONObject();
                    relatedUrl.put("URL", source);
                    relatedUrl.put("Type", "GET DATA");
                    relatedUrl.put("Subtype", subTypeHash.get("subType"));
                    relateUrlsArray.add(relatedUrl);
                }
            }
        }
        return granuleJson;
    }

}
