package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineBooleanRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineBooleanRow implements AutoMineConfigRow {
    public final String label;
    public final BooleanSupplier get;
    public final Consumer<Boolean> set;

    public AutoMineBooleanRow(AutoMineMainScreen autoMineMainScreen, String str, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer) {
        this.label = str;
        this.get = booleanSupplier;
        this.set = consumer;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 16;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        boolean asBoolean = this.get.getAsBoolean();
        boolean hovered = AutoMineMainScreen.isHovered(i, i2, i3, height(), i4, i5);
        if (asBoolean || hovered) {
            VdmFontRenderer.roundedRect(context, i + (4), i2, i3 - (8), height() - (1), 4, asBoolean ? 822083583 : 402653183);
        }
        if (asBoolean) {
            VdmFontRenderer.roundedRect(context, i + (6), i2 + (3), 2, height() - (7), 1, -1);
        }
        context.drawTextWithShadow(textRenderer, Text.literal(VdmFontRenderer.ellipsize(textRenderer, this.label, (i3 - (16)) - (asBoolean ? 6 : 2))), i + (8) + (asBoolean ? 4 : 0), i2 + (4), (asBoolean || hovered) ? -1 : -3552303);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.automine.core.AutoMineConfigRow
    public void click(double d, int i, int i2) {
        this.set.accept(Boolean.valueOf(!this.get.getAsBoolean() ? true : false));
        AutoMineMainScreen.getConfig().save();
    }
}
