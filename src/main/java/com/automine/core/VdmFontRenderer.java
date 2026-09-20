package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: VdmFontRenderer.class */
@Environment(EnvType.CLIENT)
public final class VdmFontRenderer {
    public static final int ACCENT = -14829228;
    public static final int CARD_BG = -586018284;
    public static final int TRACK_BG = -12961222;
    public static final int TEXT_MAIN = -1;
    public static final int TEXT_DIM = -5592406;
    public static final int TEXT_FAINT = -7829368;

    public static void roundedRect(DrawContext context, int i, int i2, int i3, int i4, int i5, int i6) {
        if (i5 > i3 / (2)) {
            i5 = i3 / (2);
        }
        if (i5 > i4 / (2)) {
            i5 = i4 / (2);
        }
        if (i5 <= 0) {
            context.fill(i, i2, i + i3, i2 + i4, i6);
            return;
        }
        if (i4 - ((2) * i5) > 0) {
            context.fill(i, i2 + i5, i + i3, (i2 + i4) - i5, i6);
        }
        for (int i7 = 0; i7 < i5; i7++) {
            double d = (((double) i5) - 0.5d) - ((double) i7);
            int iRound = (int) Math.round(((double) i5) - Math.sqrt((((double) i5) * ((double) i5)) - (d * d)));
            if (iRound < 0) {
                iRound = 0;
            }
            context.fill(i + iRound, i2 + i7, (i + i3) - iRound, i2 + i7 + (1), i6);
            context.fill(i + iRound, ((i2 + i4) - (1)) - i7, (i + i3) - iRound, (i2 + i4) - i7, i6);
        }
    }

    public static void roundedBorder(DrawContext context, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        int iCeil;
        if (i5 > i3 / (2)) {
            i5 = i3 / (2);
        }
        if (i5 > i4 / (2)) {
            i5 = i4 / (2);
        }
        for (int i8 = 0; i8 < i6; i8++) {
            context.fill(i + i5, i2 + i8, (i + i3) - i5, i2 + i8 + (1), i7);
            context.fill(i + i5, ((i2 + i4) - (1)) - i8, (i + i3) - i5, (i2 + i4) - i8, i7);
            context.fill(i + i8, i2 + i5, i + i8 + (1), (i2 + i4) - i5, i7);
            context.fill(((i + i3) - (1)) - i8, i2 + i5, (i + i3) - i8, (i2 + i4) - i5, i7);
        }
        for (int i9 = 0; i9 < i5; i9++) {
            int iCeil2 = (int) Math.ceil(((double) i5) - Math.sqrt((((double) i5) * ((double) i5)) - (((double) (i5 - i9)) * ((double) (i5 - i9)))));
            for (int i10 = 0; i10 < i6; i10++) {
                double d = (i5 - i10) - (1);
                if (d <= 0.0d) {
                    iCeil = 0;
                } else {
                    double d2 = i5 - i9;
                    iCeil = d2 > d ? (int) Math.ceil(i5) : (int) Math.ceil(((double) i5) - Math.sqrt((d * d) - (d2 * d2)));
                }
                int i11 = iCeil;
                int iMin = Math.min(iCeil2, i11);
                int iMax = Math.max(iCeil2, i11);
                context.fill(i + iMin, i2 + i9, i + iMax + (1), i2 + i9 + (1), i7);
                context.fill(((i + i3) - iMax) - (1), i2 + i9, (i + i3) - iMin, i2 + i9 + (1), i7);
                context.fill(i + iMin, ((i2 + i4) - (1)) - i9, i + iMax + (1), (i2 + i4) - i9, i7);
                context.fill(((i + i3) - iMax) - (1), ((i2 + i4) - (1)) - i9, (i + i3) - iMin, (i2 + i4) - i9, i7);
            }
        }
    }

    public static String ellipsize(TextRenderer textRenderer, String str, int i) {
        String str2;
        if (textRenderer.getWidth(str) <= i) {
            return str;
        }
        String strSubstring = str;
        while (true) {
            str2 = strSubstring;
            if (str2.isEmpty() || textRenderer.getWidth(str2 + "...") <= i) {
                break;
            }
            strSubstring = str2.substring(0, str2.length() - (1));
        }
        return str2 + "...";
    }
}
