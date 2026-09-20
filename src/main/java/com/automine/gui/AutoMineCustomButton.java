package com.automine.gui;

import com.automine.core.AutoMineButtonPressAction;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

/* JADX INFO: loaded from: AutoMineCustomButton.class */
@Environment(EnvType.CLIENT)
public final class AutoMineCustomButton extends ClickableWidget {
    public final AutoMineButtonPressAction pressAction;
    public final Supplier<Boolean> selectedSupplier;

    public AutoMineCustomButton(int i, int i2, int i3, int i4, Text text, AutoMineButtonPressAction pressAction, Supplier<Boolean> supplier) {
        super(i, i2, i3, i4, text);
        this.pressAction = pressAction;
        this.selectedSupplier = supplier;
    }

    public static AutoMineCustomButton of(int i, int i2, int i3, int i4, String str, AutoMineButtonPressAction pressAction) {
        return new AutoMineCustomButton(i, i2, i3, i4, Text.literal(str), pressAction, null);
    }

    public void onClick(Click click, boolean z) {
        if (this.pressAction != null) {
            this.pressAction.onPress(this);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void renderWidget(DrawContext context, int i, int i2, float f) {
        boolean selected = this.selectedSupplier != null && Boolean.TRUE.equals(this.selectedSupplier.get());
        VdmTheme.button(context, MinecraftClient.getInstance().textRenderer, getX(), getY(), this.width, this.height, getMessage(), selected || isHovered(), this.active);
    }

    public void appendClickableNarrations(NarrationMessageBuilder clientWorld) {
        appendDefaultNarrations(clientWorld);
    }
}
