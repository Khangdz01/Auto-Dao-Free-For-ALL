package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineDividerRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineDividerRow implements AutoMineConfigRow {

    public AutoMineDividerRow(AutoMineMainScreen autoMineMainScreen) {
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return (AutoMineMainScreen.getEngine().plan() == null || AutoMineMainScreen.getEngine().state() != AutoMineState.RUNNING) ? 0 : 10;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        if (height() == 0) {
            return;
        }
        int i6 = i3 - (16);
        int iRound = Math.round(i6 * AutoMineMainScreen.getEngine().plan().progress());
        VdmFontRenderer.roundedRect(context, i + (8), i2 + (2), i6, 4, 2, -13948112);
        if (iRound > 0) {
            VdmFontRenderer.roundedRect(context, i + (8), i2 + (2), iRound, 4, 2, -1);
        }
    }
}
