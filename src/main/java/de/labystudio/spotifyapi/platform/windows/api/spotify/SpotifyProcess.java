package de.labystudio.spotifyapi.platform.windows.api.spotify;

import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.windows.api.WinProcess;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.playback.source.MediaControlPlaybackAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyProcess.class */
@Environment(EnvType.CLIENT)
public class SpotifyProcess extends WinProcess {
    public static final boolean DEBUG;
    public static final String PREFIX_SPOTIFY_TRACK = "spotify:track:";
    public static final long[] OFFSETS_TRACK_ID;
    public final long addressTrackId;
    public final PlaybackAccessor playbackAccessor;

    public SpotifyProcess(WindowsMediaControl windowsMediaControl) {
        super("Spotify.exe");
        if (DEBUG) {
            System.out.println("Spotify process loaded! Searching for addresses...");
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.addressTrackId = findTrackIdAddress();
        if (DEBUG) {
            System.out.println("Scanning took " + (System.currentTimeMillis() - jCurrentTimeMillis) + "ms");
        }
        if (windowsMediaControl == null) {
            throw new IllegalArgumentException("MediaControl not available");
        }
        this.playbackAccessor = new MediaControlPlaybackAccessor(windowsMediaControl);
    }

    public long findTrackIdAddress() {
        Psapi.ModuleInfo moduleInfo = getModuleInfo("chrome_elf.dll");
        if (moduleInfo == null) {
            throw new IllegalStateException("Could not find chrome_elf.dll module");
        }
        long baseOfDll = moduleInfo.getBaseOfDll();
        long jFindAddressOfText = -1;
        long jMin = Long.MAX_VALUE;
        long jMax = Long.MIN_VALUE;
        for (long j : OFFSETS_TRACK_ID) {
            jMin = Math.min(jMin, j);
            jMax = Math.max(jMax, j);
            long j2 = baseOfDll + j;
            if (Track.isTrackIdValid(readTrackId(j2))) {
                jFindAddressOfText = j2;
                break;
            }
        }
        if (jFindAddressOfText == -1) {
            if (DEBUG) {
                System.out.println("Could not find track id with hardcoded offsets. Trying to find it dynamically...");
            }
            long j3 = (jMax - jMin) * 3;
            jFindAddressOfText = findAddressOfText((baseOfDll + jMin) - j3, baseOfDll + jMax + j3, PREFIX_SPOTIFY_TRACK, (j4, i) -> {
                return Track.isTrackIdValid(readTrackId(j4));
            });
        }
        if (jFindAddressOfText == -1) {
            throw new IllegalStateException("Could not find track id in memory");
        }
        if (DEBUG) {
            System.out.printf("Found track id address: %s (+%s) [%s%s]%n", Long.toHexString(jFindAddressOfText), Long.toHexString(jFindAddressOfText - baseOfDll), PREFIX_SPOTIFY_TRACK, readTrackId(jFindAddressOfText));
        }
        return jFindAddressOfText;
    }

    public String readTrackId(long j) {
        return readString(j + 14, 22);
    }

    public String readTrackId() {
        return readTrackId(this.addressTrackId);
    }

    public PlaybackAccessor getPlaybackAccessor() {
        return this.playbackAccessor;
    }

    static {
        DEBUG = System.getProperty("SPOTIFY_API_DEBUG") != null;
        OFFSETS_TRACK_ID = new long[]{1621136, 1395296, 1374768, 1073560, 1362416, 1057144, 1350128, 1044456};
    }
}
