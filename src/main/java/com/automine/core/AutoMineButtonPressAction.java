package com.automine.core;

import com.automine.gui.AutoMineCustomButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: AutoMineButtonPressAction.class */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface AutoMineButtonPressAction {
    void onPress(AutoMineCustomButton customBtn1);
}
