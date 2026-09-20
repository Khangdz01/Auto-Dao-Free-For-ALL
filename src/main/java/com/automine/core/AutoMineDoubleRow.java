package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineDoubleRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineDoubleRow implements AutoMineConfigRow {
    public final String label;
    public final DoubleSupplier get;
    public final DoubleConsumer set;

    public AutoMineDoubleRow(AutoMineMainScreen autoMineMainScreen, String str, DoubleSupplier doubleSupplier, DoubleConsumer doubleConsumer) {
        this.label = str;
        this.get = doubleSupplier;
        this.set = doubleConsumer;
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
        Object[] objArr = new Object[1];
        objArr[0] = Double.valueOf(this.get.getAsDouble());
        String str = String.format("%.1f", objArr);
        int itemCount = textRenderer.getWidth(str);
        context.drawTextWithShadow(textRenderer, Text.literal("−"), (i + i3) - (44), i2 + (4), -4604990);
        context.drawTextWithShadow(textRenderer, Text.literal(str), ((i + i3) - (26)) - (itemCount / (2)), i2 + (4), -1);
        context.drawTextWithShadow(textRenderer, Text.literal("+"), (i + i3) - (12), i2 + (4), -4604990);
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void click(double d, int i, int i2) {
        if (d >= 150.0d && d <= 164.0d) {
            this.set.accept(Math.max(3.0d, this.get.getAsDouble() - 0.5d));
            AutoMineMainScreen.getConfig().save();
        } else if (d >= 182.0d) {
            this.set.accept(Math.min(6.0d, this.get.getAsDouble() + 0.5d));
            AutoMineMainScreen.getConfig().save();
        }
    }
}
