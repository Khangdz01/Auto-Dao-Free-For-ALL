package de.labystudio.spotifyapi.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyConfiguration.class */
@Environment(EnvType.CLIENT)
public class SpotifyConfiguration {
    public final long exceptionReconnectDelay;
    public final boolean autoReconnect;
    public final Path nativesDirectory;

    /* JADX INFO: loaded from: SpotifyConfiguration$Builder.class */
    @Environment(EnvType.CLIENT)
    public static class Builder {
        public long exceptionReconnectDelay = 10000;
        public boolean autoReconnect = true;
        public Path nativesDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "spotify-api-natives");

        public Builder exceptionReconnectDelay(long j) {
            this.exceptionReconnectDelay = j;
            return this;
        }

        public Builder autoReconnect(boolean z) {
            this.autoReconnect = z;
            return this;
        }

        public Builder nativesDirectory(Path path) {
            this.nativesDirectory = path;
            return this;
        }

        public SpotifyConfiguration build() {
            return new SpotifyConfiguration(this.exceptionReconnectDelay, this.autoReconnect, this.nativesDirectory);
        }
    }

    public SpotifyConfiguration(long j, boolean z, Path path) {
        this.exceptionReconnectDelay = j;
        this.autoReconnect = z;
        this.nativesDirectory = path;
    }

    public long getExceptionReconnectDelay() {
        return this.exceptionReconnectDelay;
    }

    public boolean isAutoReconnect() {
        return this.autoReconnect;
    }

    public Path getNativesDirectory() {
        return this.nativesDirectory;
    }
}
