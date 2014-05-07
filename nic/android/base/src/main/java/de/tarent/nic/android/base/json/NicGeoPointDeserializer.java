package de.tarent.nic.android.base.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.entities.NicGeoPoint;

import java.lang.reflect.Type;

/**
 * This is a simple deserializer for our NicGeoPoint-interface which will be called by gson as needed.
 */
public class NicGeoPointDeserializer implements JsonDeserializer<NicGeoPoint> { // NOSONAR, gson wants instances.
    @Override
    public NicGeoPoint deserialize(JsonElement jsonElement,
                                   Type type,
                                   JsonDeserializationContext jsonDeserializationContext) {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new NicGeoPointImpl(jsonObject.get("mLatitudeE6").getAsInt(),
                jsonObject.get("mLongitudeE6").getAsInt());
    }
}