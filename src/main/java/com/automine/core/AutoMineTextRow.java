package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineTextRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineTextRow implements AutoMineConfigRow {
    public final Supplier<String> text;

    public AutoMineTextRow(AutoMineMainScreen autoMineMainScreen, Supplier<String> supplier) {
        this.text = supplier;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 12;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        String str = this.text.get();
        if (str == null || str.isEmpty()) {
            return;
        }
        context.drawText(textRenderer, Text.literal(VdmFontRenderer.ellipsize(textRenderer, str, i3 - (16))), i + (8), i2 + (2), -7697004, false);
    }
}
