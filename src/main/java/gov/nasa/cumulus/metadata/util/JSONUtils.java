package gov.nasa.cumulus.metadata.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import gov.nasa.cumulus.metadata.aggregator.UMMUtils;
import gov.nasa.cumulus.metadata.umm.generated.RelatedUrlType;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class JSONUtils {

    /**
     * This method recursively checks the json object for empty entries
     *  and removes them accordingly.
     *
     *  This is based on the following base cases:
     *  1) The json object we're checking is a normal, single k/v entry,
     *      in which case we check and if it's null or empty, remove
     *      the object from the parent json.
     *  2) The object is a child Json Object, recurse and call method
     *      with the child json object as the new parameter.
     *  3) The object is a JSONArray, we check the array to see if contains
     *      a simple list of entries, or if it contains JsonObjects.
     *      a) if it's a list of entries, iteratively check them all to
     *          determine if they're null or empty, and if so, delete
     *          the array from the parent.
     *      b) if it's an array of JsonObjects, iterate over the array,
     *          but recurse and call this method with the child json object.
     *
     * @param json  The JSON Object to check and prune for empty entries.
     */
    public static boolean cleanJSON(Object json){
        boolean valueExist = false;
        if (json instanceof String) {
            // Case 1: if the value is string
            String str = (String) json;
            if (UMMUtils.notNullOrEmpty(str)) {
                valueExist = true;
            }
        } else if (json instanceof Number) {
            // Case 1: if the value is number
            String str = json.toString();
            if (UMMUtils.notNullOrEmpty(str)) {
                valueExist = true;
            }
        } else if (json instanceof JSONObject) {
            // Case 2: child is a JSONObject
            JSONObject obj = (JSONObject) json;
            Iterator<String> iter = obj.keySet().iterator();
            ArrayList<String> fields = new ArrayList<>();
            while (iter.hasNext()) {
                fields.add(iter.next());
            }
            for (String field : fields) {
                Object value = obj.get(field);
                // handle edge case for the DataGranule > Identifiers section
                if (field.equalsIgnoreCase("Identifiers")) {
                    JSONArray arr = (JSONArray) value;
                    for (int i=0; i < arr.size(); i++) {
                        if (identifierOk((JSONObject) arr.get(i))){
                            valueExist = true;
                        } else {
                            arr.remove(i);
                            i--;
                        }
                    }
                }
                // now handle non DG/Identifier values
                if (cleanJSON(value)) {
                    valueExist = true;
                } else {
                    obj.remove(field);
                }
            }
        } else if (json instanceof JSONArray) {
            // Case 3: child is a JSONArray
            JSONArray arr = (JSONArray) json;
            for (int i=0; i < arr.size(); i++) {
                if (cleanJSON(arr.get(i))){
                    valueExist = true;
                } else {
                    arr.remove(i);
                    i--;
                }
            }
        }
        return valueExist;
    }

    public static boolean identifierOk(JSONObject json) {
        boolean keyOk = UMMUtils.notNullOrEmpty((String) json.get("IdentifierType"));
        boolean valOk = UMMUtils.notNullOrEmpty((String) json.get("Identifier"));
        return keyOk && valOk;
    }

    /**
     * Translate from google gson JsonObject to org.json.simple.JSONObject
     * @param input
     * @return
     * @throws ParseException
     */
    public static JSONObject GsonToJSONObj(JsonObject input) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jo = (JSONObject) parser.parse(input.toString());;
        return jo;
    }

    /**
     * Translate from google gson JsonObject to org.json.simple.JSONObject
     * @param input
     * @return
     * @throws ParseException
     */
    public static JSONArray GsonArrayToJSONArray(JsonArray input) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jarray = (JSONArray) parser.parse(input.toString());;
        return jarray;
    }

    public static JSONObject sortRelatedUrls(JSONObject input)throws ParseException {
        /**
         * JSONArray extends ArrayList implements List, JSONAware, JSONStreamAware
         * so JSONArray maintains insert order
         */
        JSONArray unsortedRelatedUrls = (JSONArray)input.get("RelatedUrls");
        JSONArray sortedRelatedUrls = new JSONArray();
        ArrayList<JSONObject> toBeRemovedItems = new ArrayList<>();
        // first, extract URL starts with http/https and Type == GET DATA to added into sortedRelatedUrls
        for(int i =0; i< unsortedRelatedUrls.size(); i++) {
            JSONObject relatedUrl = (JSONObject) unsortedRelatedUrls.get(i);
            if( isStartingWithHttpHttps((String)relatedUrl.get("URL"))
                    &&
                    isGETTYPE((String)relatedUrl.get("Type"))
                 ) {
                sortedRelatedUrls.add(relatedUrl);
                toBeRemovedItems.add(relatedUrl);
            }
        }
        unsortedRelatedUrls = shrinkUnsortedRelatedUrls(unsortedRelatedUrls, toBeRemovedItems);
        toBeRemovedItems.clear();

        // other http/https files
        for(int i =0; i< unsortedRelatedUrls.size(); i++) {
            JSONObject relatedUrl = (JSONObject) unsortedRelatedUrls.get(i);
            if(isStartingWithHttpHttps((String)relatedUrl.get("URL"))) {
                sortedRelatedUrls.add(relatedUrl);
                toBeRemovedItems.add(relatedUrl);
            }
        }
        unsortedRelatedUrls = shrinkUnsortedRelatedUrls(unsortedRelatedUrls, toBeRemovedItems);
        toBeRemovedItems.clear();

        // s3 link to scientific data
        for(int i =0; i< unsortedRelatedUrls.size(); i++) {
            JSONObject relatedUrl = (JSONObject) unsortedRelatedUrls.get(i);
            if(isGETTYPE((String)relatedUrl.get("Type"))
                &&
                    isStartingWithS3((String)relatedUrl.get("URL"))) {
                sortedRelatedUrls.add(relatedUrl);
                toBeRemovedItems.add(relatedUrl);
            }
        }
        unsortedRelatedUrls = shrinkUnsortedRelatedUrls(unsortedRelatedUrls, toBeRemovedItems);
        toBeRemovedItems.clear();
        // other s3 links
        for(int i =0; i< unsortedRelatedUrls.size(); i++) {
            JSONObject relatedUrl = (JSONObject) unsortedRelatedUrls.get(i);
            if(isStartingWithS3((String)relatedUrl.get("URL"))) {
                sortedRelatedUrls.add(relatedUrl);
                toBeRemovedItems.add(relatedUrl);
            }
        }
        unsortedRelatedUrls = shrinkUnsortedRelatedUrls(unsortedRelatedUrls, toBeRemovedItems);
        toBeRemovedItems.clear();

        // left of item in unsorted array
        for(Object e: unsortedRelatedUrls) {
            JSONObject relatedUrl = (JSONObject) e;
            sortedRelatedUrls.add(relatedUrl);
        }
        input.remove("RelatedUrls");
        input.put("RelatedUrls", sortedRelatedUrls);
        return input;
    }

    public static JSONArray shrinkUnsortedRelatedUrls(JSONArray unsortedRelatedUrls,
                                                           ArrayList<JSONObject> toBeRemovedItems){
        toBeRemovedItems.forEach(item -> {
            unsortedRelatedUrls.remove(item);
        });
        return unsortedRelatedUrls;
    }

    public static boolean isStartingWithHttpHttps(String s) {
        s = StringUtils.trim(s);
        if(StringUtils.startsWithIgnoreCase(s,"http://") || StringUtils.startsWithIgnoreCase(s,"https://")) {
            return true;
        }
        return false;
    }

    public static boolean isStartingWithS3(String s) {
        s = StringUtils.trim(s);
        if(StringUtils.startsWithIgnoreCase(s,"s3://") ) {
            return true;
        }
        return false;
    }

    public static boolean isGETTYPE(String s) {
        s = StringUtils.trim(s);
        if(StringUtils.equalsIgnoreCase(s,RelatedUrlType.RelatedUrlTypeEnum.GET_DATA.value()) ) {
            return true;
        }
        return false;
    }


}
