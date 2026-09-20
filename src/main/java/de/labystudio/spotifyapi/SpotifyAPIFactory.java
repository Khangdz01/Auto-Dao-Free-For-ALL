package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.config.SpotifyConfiguration;
import de.labystudio.spotifyapi.platform.osx.OSXSpotifyApi;
import de.labystudio.spotifyapi.platform.windows.WinSpotifyAPI;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyAPIFactory.class */
@Environment(EnvType.CLIENT)
public class SpotifyAPIFactory {
    public static SpotifyAPI create() {
        String lowerCase = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (lowerCase.contains("win")) {
            return new WinSpotifyAPI();
        }
        if (lowerCase.contains("mac")) {
            return new OSXSpotifyApi();
        }
        throw new IllegalStateException("Unsupported OS: " + lowerCase);
    }

    public static SpotifyAPI createInitialized() {
        return create().initialize();
    }

    public static SpotifyAPI createInitialized(SpotifyConfiguration spotifyConfiguration) {
        return create().initialize(spotifyConfiguration);
    }

    public static CompletableFuture<SpotifyAPI> createInitializedAsync() {
        return CompletableFuture.supplyAsync(SpotifyAPIFactory::createInitialized);
    }

    public static CompletableFuture<SpotifyAPI> createInitializedAsync(SpotifyConfiguration spotifyConfiguration) {
        return CompletableFuture.supplyAsync(() -> {
            return createInitialized(spotifyConfiguration);
        });
    }
}
