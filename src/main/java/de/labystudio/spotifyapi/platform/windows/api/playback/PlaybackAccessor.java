package de.labystudio.spotifyapi.platform.windows.api.playback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: PlaybackAccessor.class */
@Environment(EnvType.CLIENT)
public interface PlaybackAccessor {
    void updatePlayback();

    void updateTrack();

    boolean isValid();

    int getLength();

    int getPosition();

    boolean isPlaying();

    String getTitle();

    String getArtist();

    byte[] getCoverArt();

    default boolean hasTrackLength() {
        return getLength() > 0;
    }

    default boolean hasTrackPosition() {
        return getPosition() >= 0 && hasTrackLength() && getPosition() <= getLength();
    }
}
