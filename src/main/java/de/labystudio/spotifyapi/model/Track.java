package de.labystudio.spotifyapi.model;

import java.awt.image.BufferedImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: Track.class */
@Environment(EnvType.CLIENT)
public class Track {
    public static final int ID_LENGTH = 22;
    public final String id;
    public final String name;
    public final String artist;
    public final int length;
    public final BufferedImage coverArt;

    public Track(String str, String str2, String str3, int i, BufferedImage bufferedImage) {
        this.id = str;
        this.name = str2;
        this.artist = str3;
        this.length = i;
        this.coverArt = bufferedImage;
    }

    public boolean isIdValid() {
        return isTrackIdValid(this.id);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getArtist() {
        return this.artist;
    }

    public int getLength() {
        return this.length;
    }

    public BufferedImage getCoverArt() {
        return this.coverArt;
    }

    public boolean equals(Object obj) {
        return (obj instanceof Track) && this.id.equals(((Track) obj).id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return String.format("[%s] %s - %s", this.id, this.name, this.artist);
    }

    public static boolean isTrackIdValid(String str) {
        if (str == null) {
            return false;
        }
        char[] charArray = str.toCharArray();
        int length = charArray.length;
        for (int i = 0; i < length; i++) {
            char c = charArray[i];
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return !str.contains(" ") && str.length() == 22;
    }
}
