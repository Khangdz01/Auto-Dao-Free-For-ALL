package com.automine.core;

import de.labystudio.spotifyapi.model.MediaKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyMediaKeySwitchMap.class */
@Environment(EnvType.CLIENT)
public /* synthetic */ class SpotifyMediaKeySwitchMap {
    public static final /* synthetic */ int[] $SwitchMap$de$labystudio$spotifyapi$model$MediaKey = new int[MediaKey.values().length];

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
