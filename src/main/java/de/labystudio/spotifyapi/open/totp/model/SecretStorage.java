package de.labystudio.spotifyapi.open.totp.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SecretStorage.class */
@Environment(EnvType.CLIENT)
public class SecretStorage {
    public String validUntil;
    public Secret[] secrets;

    public String getValidUntil() {
        return this.validUntil;
    }

    public Secret[] getSecrets() {
        return this.secrets;
    }

    public Secret getLatestSecret() {
        if (this.secrets == null || this.secrets.length == 0) {
            return null;
        }
        Secret secret = this.secrets[0];
        for (Secret secret2 : this.secrets) {
            if (secret2.getVersion() > secret.getVersion()) {
                secret = secret2;
            }
        }
        return secret;
    }
}
