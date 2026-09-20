package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineSpacerRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineSpacerRow implements AutoMineConfigRow {

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 5;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
    }
}
