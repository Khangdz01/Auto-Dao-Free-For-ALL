package com.automine.core;

import com.automine.gui.AutoMineMainScreen;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMinePanel.class */
@Environment(EnvType.CLIENT)
public final class AutoMinePanel {
    public final String title;
    public final String icon;
    public final List<AutoMineConfigRow> entries = new ArrayList();
    public int x;
    public int y;
    public double dragOffX;
    public double dragOffY;

    public AutoMinePanel(AutoMineMainScreen autoMineMainScreen, String str, String str2) {
        this.title = str;
        this.icon = str2;
    }

    public void add(AutoMineConfigRow configRow) {
        this.entries.add(configRow);
    }

    public int height() {
        int iHeight = 27;
        Iterator<AutoMineConfigRow> it = this.entries.iterator();
        while (it.hasNext()) {
            iHeight += it.next().height();
        }
        return iHeight + (8);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean inHeader(double d, double d2) {
        return (d < ((double) this.x) || d > ((double) (this.x + (200))) || d2 < ((double) this.y) || d2 > ((double) (this.y + (24)))) ? false : true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean inBody(double d, double d2) {
        return (d < ((double) this.x) || d > ((double) (this.x + (200))) || d2 <= ((double) (this.y + (24))) || d2 > ((double) (this.y + height()))) ? false : true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void render(DrawContext context, TextRenderer textRenderer, int i, int i2) {
        int iHeight = height();
        VdmFontRenderer.roundedRect(context, this.x + (1), this.y + (2), 200, iHeight, 8, 1291845632);
        VdmFontRenderer.roundedRect(context, this.x, this.y, 200, iHeight, 8, -652337630);
        VdmFontRenderer.roundedBorder(context, this.x, this.y, 200, iHeight, 8, 1, -32768);
        context.drawText(textRenderer, Text.literal(this.icon), this.x + (8), this.y + (8), -3552303, false);
        context.drawTextWithShadow(textRenderer, Text.literal(this.title), this.x + (8) + textRenderer.getWidth(this.icon) + (6), this.y + (8), -1);
        context.fill(this.x + (8), (this.y + (24)) - (2), (this.x + (200)) - (8), (this.y + (24)) - (1), 536870911);
        int iHeight2 = this.y + (24) + (3);
        for (AutoMineConfigRow configRow : this.entries) {
            configRow.render(context, textRenderer, this.x, iHeight2, 200, i, i2);
            iHeight2 += configRow.height();
        }
    }

    public void click(double d, double d2) {
        int iHeight = this.y + (24) + (3);
        for (AutoMineConfigRow configRow : this.entries) {
            if (d2 >= iHeight && d2 < iHeight + configRow.height()) {
                configRow.click(d - ((double) this.x), this.x, iHeight);
                return;
            }
            iHeight += configRow.height();
        }
    }
}
