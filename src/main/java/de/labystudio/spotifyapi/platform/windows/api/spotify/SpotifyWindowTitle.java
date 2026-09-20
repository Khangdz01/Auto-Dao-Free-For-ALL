package de.labystudio.spotifyapi.platform.windows.api.spotify;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyWindowTitle.class */
@Environment(EnvType.CLIENT)
public class SpotifyWindowTitle {
    public static final String DELIMITER = " - ";
    public static final SpotifyWindowTitle UNKNOWN = new SpotifyWindowTitle("Unknown", "No song playing");
    public final String name;
    public final String artist;

    public SpotifyWindowTitle(String str, String str2) {
        this.name = str;
        this.artist = str2;
    }

    public String getTrackName() {
        return this.name;
    }

    public String getTrackArtist() {
        return this.artist;
    }

    public String toString() {
        return this.name + " - " + this.artist;
    }

    public static SpotifyWindowTitle of(String str) {
        if (!str.contains(DELIMITER)) {
            return null;
        }
        String[] strArrSplit = str.split(DELIMITER);
        return new SpotifyWindowTitle(strArrSplit[1], strArrSplit[0]);
    }
}
