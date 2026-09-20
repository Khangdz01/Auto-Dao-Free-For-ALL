package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutoMineDigTask {
    APPROACH,
    REPOSITION,
    AIM_LOCK,
    DIG_LOCKED
}
