package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: SpotifyTrackCallback.class */
@Environment(EnvType.CLIENT)
public interface SpotifyTrackCallback {
    void update(String str, String str2, boolean z, long j, long j2, String str3);

    void clear();
}
