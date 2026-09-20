package com.automine.mixin;

import com.automine.gui.VdmTheme;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(value=EnvType.CLIENT)
@Mixin(targets={"net/minecraft/client/font/TextRenderer$Drawer"})
public class TextRendererItalicMixin {
    @ModifyVariable(method={"accept"}, at=@At(value="HEAD"), argsOnly=true, require=0)
    public Style automine$italic(Style style) {
        if (style != null && !style.isItalic() && VdmTheme.isModScreen(MinecraftClient.getInstance().currentScreen)) {
            return style.withItalic(Boolean.valueOf(true));
        }
        return style;
    }
}

