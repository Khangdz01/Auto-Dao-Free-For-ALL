package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutoMinePathNode {
    MOVING,
    ARRIVED,
    BLOCKED;
}
