package gov.nasa.cumulus.metadata.umm.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collection;

public class UMMGCollectionAdapter implements JsonSerializer<Collection<?>> {
    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        if (src == null || src.isEmpty())
            return null;

        JsonArray array = new JsonArray();

        for (Object child : src) {
            JsonElement element = context.serialize(child);
            array.add(element);
        }

        return array;
    }
}
