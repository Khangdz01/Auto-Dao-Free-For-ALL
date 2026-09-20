package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineIntRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineIntRow implements AutoMineConfigRow {
    public final String label;
    public final IntSupplier get;
    public final IntConsumer set;
    public final int step;

    public AutoMineIntRow(AutoMineMainScreen autoMineMainScreen, String str, IntSupplier intSupplier, IntConsumer intConsumer, int i) {
        this.label = str;
        this.get = intSupplier;
        this.set = intConsumer;
        this.step = i;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 16;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        boolean hovered = AutoMineMainScreen.isHovered(i, i2, i3, height(), i4, i5);
        if (hovered) {
            VdmFontRenderer.roundedRect(context, i + (4), i2, i3 - (8), height() - (1), 4, 402653183);
        }
        context.drawTextWithShadow(textRenderer, Text.literal(VdmFontRenderer.ellipsize(textRenderer, this.label, (i3 - (8)) - (52))), i + (8), i2 + (4), hovered ? -1 : -3552303);
        String strValueOf = String.valueOf(this.get.getAsInt());
        int itemCount = textRenderer.getWidth(strValueOf);
        context.drawTextWithShadow(textRenderer, Text.literal("−"), (i + i3) - (44), i2 + (4), -4604990);
        context.drawTextWithShadow(textRenderer, Text.literal(strValueOf), ((i + i3) - (26)) - (itemCount / (2)), i2 + (4), -1);
        context.drawTextWithShadow(textRenderer, Text.literal("+"), (i + i3) - (12), i2 + (4), -4604990);
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void click(double d, int i, int i2) {
        if (d >= 150.0d && d <= 164.0d) {
            this.set.accept(this.get.getAsInt() - this.step);
            AutoMineMainScreen.getConfig().save();
        } else if (d >= 182.0d) {
            this.set.accept(this.get.getAsInt() + this.step);
            AutoMineMainScreen.getConfig().save();
        }
    }
}
