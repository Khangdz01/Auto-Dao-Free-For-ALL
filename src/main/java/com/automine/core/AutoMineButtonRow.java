package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;

/* JADX INFO: loaded from: AutoMineButtonRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineButtonRow implements AutoMineConfigRow {
    public final Supplier<String> label;
    public final Runnable action;
    public final BooleanSupplier enabled;

    public AutoMineButtonRow(AutoMineMainScreen autoMineMainScreen, Supplier<String> supplier, Runnable runnable, BooleanSupplier booleanSupplier) {
        this.label = supplier;
        this.action = runnable;
        this.enabled = booleanSupplier;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean isEnabled() {
        return (this.enabled == null || this.enabled.getAsBoolean());
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 16;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        int i6;
        int i7 = (isEnabled() && AutoMineMainScreen.isHovered(i, i2, i3, height(), i4, i5)) ? 1 : 0;
        if (i7 != 0) {
            VdmFontRenderer.roundedRect(context, i + (4), i2, i3 - (8), height() - (1), 4, 402653183);
        }
        MutableText textComponent = Text.literal(VdmFontRenderer.ellipsize(textRenderer, this.label.get(), i3 - (16)));
        int i8 = i + (8);
        int i9 = i2 + (4);
        if (isEnabled()) {
            i6 = i7 != 0 ? -1 : -3552303;
        } else {
            i6 = -7697004;
        }
        context.drawTextWithShadow(textRenderer, textComponent, i8, i9, i6);
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void click(double d, int i, int i2) {
        if (isEnabled()) {
            this.action.run();
        }
    }
}
