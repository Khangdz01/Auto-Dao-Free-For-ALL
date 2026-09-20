package com.automine.mixin;

import com.automine.gui.VdmTheme;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={PressableWidget.class})
public class PressableWidgetMixin {
    @Inject(method={"renderWidget"}, at={@At(value="HEAD")}, cancellable=true)
    public void automine$flatStyle(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!VdmTheme.isModScreen(mc.currentScreen)) {
            return;
        }
        PressableWidget self = (PressableWidget)(Object)this;
        VdmTheme.button(context, mc.textRenderer, self.getX(), self.getY(), self.getWidth(), self.getHeight(), self.getMessage(), self.isHovered(), self.active);
        ci.cancel();
    }
}

