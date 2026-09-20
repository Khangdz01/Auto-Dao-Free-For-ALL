package com.automine.mixin;

import com.automine.gui.VdmTheme;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={TextRenderer.class})
public class TextRendererMixin {
    public static final StyleSpriteSource AUTOMINE_FONT = new StyleSpriteSource.Font(Identifier.of((String)"automine", (String)"sleek"));

    @ModifyVariable(method={"getGlyphs"}, at=@At(value="HEAD"), argsOnly=true)
    public StyleSpriteSource automine$modFont(StyleSpriteSource tooltip) {
        StyleSpriteSource.Font tooltipPositioner;
        if (tooltip instanceof StyleSpriteSource.Font && (tooltipPositioner = (StyleSpriteSource.Font)tooltip).id().equals((Object)MinecraftClient.DEFAULT_FONT_ID) && VdmTheme.isModScreen(MinecraftClient.getInstance().currentScreen)) {
            return AUTOMINE_FONT;
        }
        return tooltip;
    }
}

