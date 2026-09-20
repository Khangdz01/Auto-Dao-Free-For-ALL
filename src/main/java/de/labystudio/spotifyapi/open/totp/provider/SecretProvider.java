package de.labystudio.spotifyapi.open.totp.provider;

import de.labystudio.spotifyapi.open.totp.model.Secret;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SecretProvider.class */
@Environment(EnvType.CLIENT)
public interface SecretProvider {
    Secret getSecret() throws IOException;
}
