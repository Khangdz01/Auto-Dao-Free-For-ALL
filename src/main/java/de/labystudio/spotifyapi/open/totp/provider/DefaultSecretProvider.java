package de.labystudio.spotifyapi.open.totp.provider;

import de.labystudio.spotifyapi.open.totp.model.Secret;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: DefaultSecretProvider.class */
@Environment(EnvType.CLIENT)
public class DefaultSecretProvider implements SecretProvider {
    public final Secret secret;

    public DefaultSecretProvider(Secret secret) {
        this.secret = secret;
    }

    @Override // de.labystudio.spotifyapi.open.totp.provider.SecretProvider
    public Secret getSecret() {
        return this.secret;
    }
}
