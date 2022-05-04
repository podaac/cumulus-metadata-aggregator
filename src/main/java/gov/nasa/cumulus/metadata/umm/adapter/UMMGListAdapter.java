package gov.nasa.cumulus.metadata.umm.adapter;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

public class UMMGListAdapter implements JsonSerializer<List<?>> {
    @Override
    public JsonElement serialize(List<?> src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src.isEmpty()) // exclusion is made here
            return null;

        JsonArray array = new JsonArray();

        for (Object child : src) {
            JsonElement element = context.serialize(child);
            array.add(element);
        }

        return array;
    }
}
