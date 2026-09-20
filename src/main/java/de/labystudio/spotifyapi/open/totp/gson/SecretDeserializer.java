package de.labystudio.spotifyapi.open.totp.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SecretDeserializer.class */
@Environment(EnvType.CLIENT)
public class SecretDeserializer implements JsonDeserializer<Secret> {
    /* JADX INFO: Thrown type has an unknown type hierarchy: com.google.gson.JsonParseException */
    /* JADX INFO: renamed from: deserialize, reason: merged with bridge method [inline-methods] */
    @Override
    public Secret deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        int asInt = asJsonObject.get("version").getAsInt();
        JsonElement jsonElement2 = asJsonObject.get("secret");
        if (!jsonElement2.isJsonArray()) {
            if (jsonElement2.isJsonPrimitive() && jsonElement2.getAsJsonPrimitive().isString()) {
                return Secret.fromString(jsonElement2.getAsString(), asInt);
            }
            throw new JsonParseException("Invalid secret format: " + String.valueOf(jsonElement2));
        }
        JsonArray asJsonArray = jsonElement2.getAsJsonArray();
        int[] iArr = new int[asJsonArray.size()];
        for (int i = 0; i < asJsonArray.size(); i++) {
            iArr[i] = asJsonArray.get(i).getAsInt();
        }
        return Secret.fromNumbers(iArr, asInt);
    }
}
