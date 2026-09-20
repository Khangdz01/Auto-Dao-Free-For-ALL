package com.automine.mixin;

import com.automine.gui.VdmTheme;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(value=EnvType.CLIENT)
@Mixin(value={TextFieldWidget.class})
public class TextFieldWidgetMixin {
    @Redirect(method={"renderWidget"}, require=0, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    public void automine$flatField(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (VdmTheme.isModScreen(MinecraftClient.getInstance().currentScreen)) {
            VdmTheme.field(context, x, y, width, height, ((TextFieldWidget)(Object)this).isFocused());
            return;
        }
        context.drawGuiTexture(pipeline, sprite, x, y, width, height);
    }
}

