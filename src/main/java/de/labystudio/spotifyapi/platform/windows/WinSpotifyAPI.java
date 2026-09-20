package de.labystudio.spotifyapi.platform.windows;

import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.windows.api.WinApi;
import de.labystudio.spotifyapi.platform.windows.api.jna.WindowsMediaControl;
import de.labystudio.spotifyapi.platform.windows.api.playback.PlaybackAccessor;
import de.labystudio.spotifyapi.platform.windows.api.spotify.SpotifyProcess;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: WinSpotifyAPI.class */
@Environment(EnvType.CLIENT)
public class WinSpotifyAPI extends AbstractTickSpotifyAPI {
    public static WindowsMediaControl mediaControl;
    public SpotifyProcess process;
    public Track currentTrack;
    public boolean isPlaying;
    public long lastTimePositionUpdated;
    public int currentPosition = -1;
    public boolean hasTrackPosition = false;
    public long prevLastReportedPosition = -1;

    /* JADX INFO: loaded from: WinSpotifyAPI$AnonymousClass1.class */
    @Environment(EnvType.CLIENT)
    static class AnonymousClass1 {
        public static final int[] $SwitchMap$de$labystudio$spotifyapi$model$MediaKey = new int[MediaKey.values().length];

        static {
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.NEXT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.PREV.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.PLAY_PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    @Override // de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI
    public void onInitialized() {
        try {
            initializeMediaControl(this.configuration.getNativesDirectory());
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void initializeMediaControl(Path path) throws IOException {
        if (mediaControl != null) {
            return;
        }
        String str = "/natives/windows-x" + (System.getProperty("os.arch").contains("64") ? '@' : 'V') + "/windowsmediacontrol.dll";
        InputStream resourceAsStream = SpotifyProcess.class.getResourceAsStream(str);
        try {
            if (resourceAsStream == null) {
                throw new IOException("Could not find native library: " + str);
            }
            Path pathResolve = path.resolve("windowsmediacontrol.dll");
            try {
                if (!Files.exists(path, new LinkOption[0])) {
                    Files.createDirectories(path, new FileAttribute[0]);
                }
                Files.copy(resourceAsStream, pathResolve, StandardCopyOption.REPLACE_EXISTING);
                mediaControl = WindowsMediaControl.loadLibrary(pathResolve);
                if (resourceAsStream != null) {
                    resourceAsStream.close();
                }
            } catch (IOException e) {
                throw new IOException("Failed to copy native library to " + String.valueOf(pathResolve), e);
            }
        } catch (Throwable th) {
            if (resourceAsStream != null) {
                resourceAsStream.close();
            }
            throw th;
        }
    }

    @Override // de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI
    public void onTick() {
        if (!isConnected()) {
            this.process = new SpotifyProcess(mediaControl);
            this.listeners.forEach(spotifyListener -> {
                spotifyListener.onConnect();
            });
        }
        String trackId = this.process.readTrackId();
        if (!Track.isTrackIdValid(trackId)) {
            throw new IllegalStateException("Invalid track ID: " + trackId);
        }
        PlaybackAccessor playbackAccessor = this.process.getPlaybackAccessor();
        playbackAccessor.updatePlayback();
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            playbackAccessor.updateTrack();
            String title = playbackAccessor.getTitle();
            String artist = playbackAccessor.getArtist();
            String name = this.currentTrack == null ? null : this.currentTrack.getName();
            String artist2 = this.currentTrack == null ? null : this.currentTrack.getArtist();
            if (!Objects.equals(title, name) || !Objects.equals(artist, artist2)) {
                Track track = new Track(trackId, title, artist, playbackAccessor.getLength(), toBufferedImage(playbackAccessor.getCoverArt()));
                this.currentTrack = track;
                this.listeners.forEach(spotifyListener2 -> {
                    spotifyListener2.onTrackChanged(track);
                });
            }
        }
        boolean zIsPlaying = playbackAccessor.isPlaying();
        if (zIsPlaying != this.isPlaying) {
            this.isPlaying = zIsPlaying;
            this.listeners.forEach(spotifyListener3 -> {
                spotifyListener3.onPlayBackChanged(zIsPlaying);
            });
        }
        if (playbackAccessor.hasTrackPosition()) {
            this.hasTrackPosition = true;
            int position = playbackAccessor.getPosition();
            if (this.prevLastReportedPosition != position) {
                this.prevLastReportedPosition = position;
                boolean z = ((long) Math.abs(position - getPosition())) > 1000;
                this.currentPosition = position;
                this.lastTimePositionUpdated = System.currentTimeMillis();
                if (z) {
                    this.listeners.forEach(spotifyListener4 -> {
                        spotifyListener4.onPositionChanged(this.currentPosition);
                    });
                }
            }
        } else {
            this.currentPosition = -1;
            this.hasTrackPosition = false;
            this.lastTimePositionUpdated = System.currentTimeMillis();
        }
        this.listeners.forEach(spotifyListener5 -> {
            spotifyListener5.onSync();
        });
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public int getPosition() {
        if (!hasPosition()) {
            throw new IllegalStateException("Position is not known yet. Pause the song for a second and try again.");
        }
        if (!this.isPlaying) {
            return this.currentPosition;
        }
        long jCurrentTimeMillis = ((long) this.currentPosition) + (System.currentTimeMillis() - this.lastTimePositionUpdated);
        return hasTrack() ? (int) Math.min(jCurrentTimeMillis, this.currentTrack.getLength()) : (int) jCurrentTimeMillis;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean hasPosition() {
        if (isConnected()) {
            return this.hasTrackPosition;
        }
        return false;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void pressMediaKey(MediaKey mediaKey) {
        if (!isConnected()) {
            throw new IllegalStateException("Spotify is not connected");
        }
        switch (AnonymousClass1.$SwitchMap$de$labystudio$spotifyapi$model$MediaKey[mediaKey.ordinal()]) {
            case 1:
                this.process.pressKey(WinApi.VK_MEDIA_NEXT_TRACK);
                break;
            case 2:
                this.process.pressKey(WinApi.VK_MEDIA_PREV_TRACK);
                break;
            case 3:
                this.process.pressKey(WinApi.VK_MEDIA_PLAY_PAUSE);
                break;
        }
        onInternalTick();
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean isConnected() {
        return this.process != null && this.process.isOpen();
    }

    @Override // de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI, de.labystudio.spotifyapi.SpotifyAPI
    public void stop() {
        super.stop();
        if (this.process != null) {
            this.process.close();
            this.process = null;
        }
        this.currentTrack = null;
        this.currentPosition = -1;
        this.hasTrackPosition = false;
        this.isPlaying = false;
        this.lastTimePositionUpdated = 0L;
        this.prevLastReportedPosition = -1L;
    }

    public BufferedImage toBufferedImage(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(bArr));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
