package de.labystudio.spotifyapi.platform.osx;

import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI;
import de.labystudio.spotifyapi.platform.osx.api.spotify.SpotifyAppleScript;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: OSXSpotifyApi.class */
@Environment(EnvType.CLIENT)
public class OSXSpotifyApi extends AbstractTickSpotifyAPI {
    public Track currentTrack;
    public boolean isPlaying;
    public long lastTimePositionUpdated;
    public final SpotifyAppleScript appleScript = new SpotifyAppleScript();
    public boolean connected = false;
    public int currentPosition = -1;

    /* JADX INFO: loaded from: OSXSpotifyApi$AnonymousClass1.class */
    @Environment(EnvType.CLIENT)
    static class AnonymousClass1 {
        public static final int[] $SwitchMap$de$labystudio$spotifyapi$model$MediaKey = new int[MediaKey.values().length];

        static {
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.PLAY_PAUSE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.NEXT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$de$labystudio$spotifyapi$model$MediaKey[MediaKey.PREV.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    @Override // de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI
    public void onTick() throws Exception {
        String trackId = this.appleScript.getTrackId();
        if (!this.connected && !trackId.isEmpty()) {
            this.connected = true;
            this.listeners.forEach(spotifyListener -> {
                spotifyListener.onConnect();
            });
        }
        if (!Objects.equals(trackId, this.currentTrack == null ? null : this.currentTrack.getId())) {
            String trackName = this.appleScript.getTrackName();
            String trackArtist = this.appleScript.getTrackArtist();
            int trackLength = this.appleScript.getTrackLength();
            boolean z = !hasTrack();
            Track track = new Track(trackId, trackName, trackArtist, trackLength, null);
            this.currentTrack = track;
            this.listeners.forEach(spotifyListener2 -> {
                spotifyListener2.onTrackChanged(track);
            });
            if (!z) {
                updatePosition(0);
            }
        }
        boolean playerState = this.appleScript.getPlayerState();
        if (playerState != this.isPlaying) {
            this.isPlaying = playerState;
            this.listeners.forEach(spotifyListener3 -> {
                spotifyListener3.onPlayBackChanged(playerState);
            });
        }
        int playerPosition = this.appleScript.getPlayerPosition();
        if (!hasPosition() || Math.abs(playerPosition - getPosition()) > 1000) {
            updatePosition(playerPosition);
        }
        this.listeners.forEach(spotifyListener4 -> {
            spotifyListener4.onSync();
        });
    }

    @Override // de.labystudio.spotifyapi.platform.AbstractTickSpotifyAPI, de.labystudio.spotifyapi.SpotifyAPI
    public void stop() {
        super.stop();
        this.connected = false;
        this.currentTrack = null;
        this.currentPosition = -1;
        this.isPlaying = false;
        this.lastTimePositionUpdated = 0L;
    }

    public void updatePosition(int i) {
        if (i == this.currentPosition) {
            return;
        }
        this.currentPosition = i;
        this.lastTimePositionUpdated = System.currentTimeMillis();
        this.listeners.forEach(spotifyListener -> {
            spotifyListener.onPositionChanged(i);
        });
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void pressMediaKey(MediaKey mediaKey) {
        try {
            switch (AnonymousClass1.$SwitchMap$de$labystudio$spotifyapi$model$MediaKey[mediaKey.ordinal()]) {
                case 1:
                    this.appleScript.playPause();
                    break;
                case 2:
                    this.appleScript.nextTrack();
                    break;
                case 3:
                    this.appleScript.previousTrack();
                    break;
            }
        } catch (Exception e) {
            this.listeners.forEach(spotifyListener -> {
                spotifyListener.onDisconnect(e);
            });
            this.connected = false;
        }
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public int getPosition() {
        if (!hasPosition()) {
            throw new IllegalStateException("Position is not known yet");
        }
        if (!this.isPlaying) {
            return this.currentPosition;
        }
        return this.currentPosition + ((int) (System.currentTimeMillis() - this.lastTimePositionUpdated));
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public Track getTrack() {
        return this.currentTrack;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean isConnected() {
        return this.connected;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean hasPosition() {
        return this.currentPosition != -1;
    }
}
