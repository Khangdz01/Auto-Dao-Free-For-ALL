package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record SpotifyTrackInfo(String title, String subtitle, String videoId, String query) {
}
