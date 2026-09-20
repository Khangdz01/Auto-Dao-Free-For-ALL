package com.automine.core;

import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: AutoMineSpotifyListener.class */
@Environment(EnvType.CLIENT)
public class AutoMineSpotifyListener implements SpotifyListener {
    public final /* synthetic */ AutoMineSpotifyManager this$0;

    public AutoMineSpotifyListener(AutoMineSpotifyManager autoMineSpotifyManager) {
        this.this$0 = autoMineSpotifyManager;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onConnect() {
        this.this$0.connected = true;
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onTrackChanged(Track track) {
        this.this$0.currentTitle = track.getName();
        this.this$0.currentArtist = track.getArtist();
        this.this$0.trackLength = track.getLength();
        this.this$0.coverImage = track.getCoverArt();
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onPositionChanged(int i) {
        this.this$0.trackPosition = i;
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onPlayBackChanged(boolean z) {
        this.this$0.isPlaying = z;
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onSync() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onDisconnect(Exception exc) {
        this.this$0.connected = false;
        this.this$0.isPlaying = false;
        this.this$0.currentTitle = "Chưa phát nhạc";
        this.this$0.currentArtist = "Mở Spotify lên nhé";
        this.this$0.coverImage = null;
    }
}
