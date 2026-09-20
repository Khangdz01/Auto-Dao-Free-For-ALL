package de.labystudio.spotifyapi.open.totp.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SecretSerializer.class */
@Environment(EnvType.CLIENT)
public class SecretSerializer implements JsonSerializer<Secret> {
    public JsonElement serialize(Secret secret, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", Integer.valueOf(secret.getVersion()));
        jsonObject.addProperty("secret", secret.getSecretAsString());
        return jsonObject;
    }
}
