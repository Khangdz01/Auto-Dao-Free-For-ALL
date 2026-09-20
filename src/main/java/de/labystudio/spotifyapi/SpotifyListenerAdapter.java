package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.model.Track;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyListenerAdapter.class */
@Environment(EnvType.CLIENT)
public class SpotifyListenerAdapter implements SpotifyListener {
    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onConnect() {
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onTrackChanged(Track track) {
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onPositionChanged(int i) {
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onPlayBackChanged(boolean z) {
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onSync() {
    }

    @Override // de.labystudio.spotifyapi.SpotifyListener
    public void onDisconnect(Exception exc) {
    }
}
