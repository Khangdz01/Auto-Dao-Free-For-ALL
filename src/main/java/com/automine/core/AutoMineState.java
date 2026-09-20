package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutoMineState {
    IDLE,
    RUNNING,
    PAUSED,
    DONE
}
