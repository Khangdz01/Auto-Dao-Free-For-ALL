package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineStatusHud.class */
@Environment(EnvType.CLIENT)
public final class AutoMineStatusHud {
    public static final int MARGIN = 4;
    public static final int LINE_SPACING = 10;
    public static final int COLOR_INFO = -11141291;
    public static final int COLOR_COORD = -8080;
    public static final int COLOR_STATUS = -5196096;

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }
        AutoMineEngine autoMineEngine = AutoMineClient.ENGINE;
        AutoMineSelection autoMineSelection = AutoMineClient.SELECTION;
        if (autoMineEngine == null || autoMineSelection == null) {
            return;
        }
        boolean zIsActive = autoMineEngine.isActive();
        int i = (autoMineSelection.pos1() == null && autoMineSelection.pos2() == null) ? 0 : 1;
        if (zIsActive || i != 0) {
            int i2 = 4;
            if (zIsActive) {
                context.drawTextWithShadow(client.textRenderer, Text.literal("Khangdzlaanh » " + autoMineEngine.statusLine()), 4, i2, -32768);
                i2 += 10;
                BlockPos activeTargetPos = autoMineEngine.activeTarget();
                if (activeTargetPos != null) {
                    context.drawTextWithShadow(client.textRenderer, Text.literal("  đang đào " + activeTargetPos.toShortString()), 4, i2, -5196096);
                    i2 += 10;
                }
            }
            if (i != 0) {
                context.drawTextWithShadow(client.textRenderer, Text.literal("  điểm 1: " + formatPos(autoMineSelection.pos1()) + "   điểm 2: " + formatPos(autoMineSelection.pos2())), 4, i2, -8080);
                int i3 = i2 + 10;
                if (!autoMineSelection.isComplete() || zIsActive) {
                    return;
                }
                context.drawTextWithShadow(client.textRenderer, Text.literal("  vùng " + autoMineSelection.describe() + " — /start để đào"), 4, i3, -5196096);
            }
        }
    }

    public static String formatPos(BlockPos pos) {
        return pos == null ? "—" : pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
