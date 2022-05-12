package gov.nasa.cumulus.metadata.util;

import gov.nasa.cumulus.metadata.aggregator.UMMUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
}
