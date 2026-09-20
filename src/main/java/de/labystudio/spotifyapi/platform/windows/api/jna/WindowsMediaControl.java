package de.labystudio.spotifyapi.platform.windows.api.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: WindowsMediaControl.class */
@Environment(EnvType.CLIENT)
public interface WindowsMediaControl extends Library {
    boolean isSpotifyAvailable();

    long getPlaybackPosition();

    long getTrackDuration();

    Pointer getTrackTitle();

    Pointer getArtistName();

    int isPlaying();

    boolean getCoverArt(PointerByReference pointerByReference, NativeLongByReference nativeLongByReference);

    void freeString(Pointer pointer);

    void freeCoverArt(Pointer pointer);

    static WindowsMediaControl loadLibrary(Path path) {
        return (WindowsMediaControl) Native.load(path.toAbsolutePath().toString(), WindowsMediaControl.class);
    }
}
