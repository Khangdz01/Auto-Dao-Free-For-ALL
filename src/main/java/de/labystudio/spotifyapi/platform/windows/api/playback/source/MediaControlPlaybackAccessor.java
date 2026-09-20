package de.labystudio.spotifyapi.platform.windows.api.playback.source;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: MediaControlPlaybackAccessor.class */
@Environment(EnvType.CLIENT)
public class MediaControlPlaybackAccessor implements PlaybackAccessor {
    public final WindowsMediaControl mediaControl;
    public long playbackPosition;
    public long trackDuration;
    public boolean isPlaying;
    public String title;
    public String artist;
    public byte[] coverArt;

    public MediaControlPlaybackAccessor(WindowsMediaControl windowsMediaControl) {
        if (windowsMediaControl == null) {
            throw new IllegalArgumentException("MediaControl cannot be null");
        }
        this.mediaControl = windowsMediaControl;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public void updatePlayback() {
        this.playbackPosition = this.mediaControl.getPlaybackPosition();
        if (this.playbackPosition == -1) {
            throw new IllegalStateException("Playback information unavailable");
        }
        int iIsPlaying = this.mediaControl.isPlaying();
        if (iIsPlaying < 0) {
            throw new IllegalStateException("Failed to retrieve playback state");
        }
        this.isPlaying = iIsPlaying == 1;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public void updateTrack() {
        this.trackDuration = this.mediaControl.getTrackDuration();
        if (this.trackDuration <= 0) {
            throw new IllegalStateException("Track duration is invalid or unavailable");
        }
        Pointer trackTitle = this.mediaControl.getTrackTitle();
        if (trackTitle == null) {
            throw new IllegalStateException("Track title pointer is null");
        }
        this.title = trackTitle.getString(0L, "UTF-8");
        this.mediaControl.freeString(trackTitle);
        Pointer artistName = this.mediaControl.getArtistName();
        if (artistName == null) {
            throw new IllegalStateException("Artist name pointer is null");
        }
        this.artist = artistName.getString(0L, "UTF-8");
        this.mediaControl.freeString(artistName);
        PointerByReference pointerByReference = new PointerByReference();
        NativeLongByReference nativeLongByReference = new NativeLongByReference();
        if (this.mediaControl.getCoverArt(pointerByReference, nativeLongByReference)) {
            Pointer value = pointerByReference.getValue();
            if (value == null) {
                this.coverArt = null;
            } else {
                this.coverArt = value.getByteArray(0L, nativeLongByReference.getValue().intValue());
                this.mediaControl.freeCoverArt(value);
            }
        }
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public boolean isValid() {
        return this.playbackPosition >= 0 && this.trackDuration > 0 && this.playbackPosition <= this.trackDuration;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public int getLength() {
        return (int) this.trackDuration;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public int getPosition() {
        return (int) this.playbackPosition;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public String getTitle() {
        return this.title;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public String getArtist() {
        return this.artist;
    }

    @Override // de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor
    public byte[] getCoverArt() {
        if (this.coverArt != null) {
            return this.coverArt;
        }
        return null;
    }
}
