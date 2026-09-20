package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineConfigRow.class */
@Environment(EnvType.CLIENT)
public interface AutoMineConfigRow {
    int height();

    void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5);

    default void click(double d, int i, int i2) {
    }
}
