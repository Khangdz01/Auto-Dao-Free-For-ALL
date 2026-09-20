package com.automine.core;

import com.automine.gui.AutoMineConfigScreen;
import com.automine.gui.AutoMineMainScreen;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineStringRow.class */
@Environment(EnvType.CLIENT)
public final class AutoMineStringRow implements AutoMineConfigRow {
    public final String label;
    public final Supplier<String> get;
    public final Consumer<String> set;
    public final /* synthetic */ AutoMineMainScreen this$0;

    public AutoMineStringRow(AutoMineMainScreen autoMineMainScreen, String str, Supplier<String> supplier, Consumer<String> consumer) {
        this.this$0 = autoMineMainScreen;
        this.label = str;
        this.get = supplier;
        this.set = consumer;
    }

    @Override // com.automine.core.AutoMineConfigRow
    public int height() {
        return 16;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.automine.core.AutoMineConfigRow
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2, int i3, int i4, int i5) {
        boolean hovered = AutoMineMainScreen.isHovered(i, i2, i3, height(), i4, i5);
        if (hovered) {
            VdmFontRenderer.roundedRect(context, i + (4), i2, i3 - (8), height() - (1), 4, 402653183);
        }
        String strEllipsize = VdmFontRenderer.ellipsize(textRenderer, this.label, (i3 - (16)) - (46));
        context.drawTextWithShadow(textRenderer, Text.literal(strEllipsize), i + (8), i2 + (4), hovered ? -1 : -3552303);
        String str = this.get.get();
        String strEllipsize2 = (str == null || str.isBlank()) ? "— bấm để điền —" : VdmFontRenderer.ellipsize(textRenderer, str, Math.max(40, ((i3 - (16)) - textRenderer.getWidth(strEllipsize)) - (6)));
        context.drawText(textRenderer, Text.literal(strEllipsize2), ((i + i3) - (8)) - textRenderer.getWidth(strEllipsize2), i2 + (4), -7697004, false);
    }

    @Override // com.automine.core.AutoMineConfigRow
    public void click(double d, int i, int i2) {
        if (net.minecraft.client.MinecraftClient.getInstance() != null) {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(new AutoMineConfigScreen(this.this$0, this.label, this.get, this.set));
        }
    }
}
