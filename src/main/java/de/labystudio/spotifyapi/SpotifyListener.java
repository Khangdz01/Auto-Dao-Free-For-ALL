package de.labystudio.spotifyapi;

import de.labystudio.spotifyapi.model.Track;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyListener.class */
@Environment(EnvType.CLIENT)
public interface SpotifyListener {
    void onConnect();

    void onTrackChanged(Track track);

    void onPositionChanged(int i);

    void onPlayBackChanged(boolean z);

    void onSync();

    void onDisconnect(Exception exc);
}
