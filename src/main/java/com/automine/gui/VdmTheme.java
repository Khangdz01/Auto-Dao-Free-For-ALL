package com.automine.gui;

import com.automine.gui.StyledScreen;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

@Environment(value=EnvType.CLIENT)
public final class VdmTheme {
    public static final int SCRIM = -653850615;
    public static final int SURFACE = -233959662;
    public static final int BORDER = Short.MIN_VALUE;
    public static final int BORDER_LIGHT = -26624;
    public static final int TITLE = -1;
    public static final int TEXT = -6577749;
    public static final int TEXT_ON = -1512722;
    public static final int TEXT_DIM = -10394002;
    public static final int BTN_BG = -14277077;
    public static final int BTN_BG_HOVER = -13619146;
    public static final int BTN_BG_OFF = -14803421;
    public static final int FIELD_BG = -14474456;
    public static final int GROUP_BG = 638982688;
    public static final int SEPARATOR = 0x2EFFFFFF;
    public static final int ACCENT = Short.MIN_VALUE;
    public static final int ACCENT_FAINT = 1509916672;
    public static final Map<Class<?>, Boolean> MOD_SCREENS = new WeakHashMap();

    public static boolean isModScreen(Object screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof StyledScreen) {
            return true;
        }
        Class<?> type = screen.getClass();
        Boolean known = MOD_SCREENS.get(type);
        if (known != null) {
            return known;
        }
        boolean found = false;
        block0: for (Class<?> c = type; c != null && !found; c = c.getSuperclass()) {
            for (Class<?> itf : c.getInterfaces()) {
                String name = itf.getSimpleName();
                if (!name.equals("VdmStyledScreen") && !name.equals("StyledScreen")) continue;
                found = true;
                continue block0;
            }
        }
        MOD_SCREENS.put(type, found);
        return found;
    }

    public static void roundedRect(DrawContext g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((r = Math.min(r, Math.min(w / 2, h / 2))) <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        if (h - 2 * r > 0) {
            g.fill(x, y + r, x + w, y + h - r, color);
        }
        for (int dy = 0; dy < r; ++dy) {
            double cy = (double)r - 0.5 - (double)dy;
            int inset = (int)Math.round((double)r - Math.sqrt((double)r * (double)r - cy * cy));
            if (inset < 0) {
                inset = 0;
            }
            g.fill(x + inset, y + dy, x + w - inset, y + dy + 1, color);
            g.fill(x + inset, y + h - 1 - dy, x + w - inset, y + h - dy, color);
        }
    }

    public static void roundedBorder(DrawContext g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.min(r, Math.min(w / 2, h / 2));
        g.fill(x + r, y, x + w - r, y + 1, color);
        g.fill(x + r, y + h - 1, x + w - r, y + h, color);
        g.fill(x, y + r, x + 1, y + h - r, color);
        g.fill(x + w - 1, y + r, x + w, y + h - r, color);
        for (int dy = 0; dy < r; ++dy) {
            double cy = (double)r - 0.5 - (double)dy;
            int inset = (int)Math.round((double)r - Math.sqrt((double)r * (double)r - cy * cy));
            if (inset < 0) {
                inset = 0;
            }
            g.fill(x + inset, y + dy, x + inset + 1, y + dy + 1, color);
            g.fill(x + w - inset - 1, y + dy, x + w - inset, y + dy + 1, color);
            g.fill(x + inset, y + h - 1 - dy, x + inset + 1, y + h - dy, color);
            g.fill(x + w - inset - 1, y + h - 1 - dy, x + w - inset, y + h - dy, color);
        }
    }

    public static void backdrop(Screen screen, DrawContext g) {
        g.fill(0, 0, screen.width, screen.height, 1342572296);
    }

    public static void title(Screen screen, DrawContext g, TextRenderer font, String text, int y) {
        g.drawCenteredTextWithShadow(font, Text.literal((String)text), screen.width / 2, y, -1);
        int mid = screen.width / 2;
        int half = Math.max(40, font.getWidth(text) / 2 + 20);
        g.fill(24, y + 12, screen.width - 24, y + 13, 0x2EFFFFFF);
        g.fill(mid - half, y + 12, mid + half, y + 13, 1509916672);
    }

    public static void button(DrawContext g, TextRenderer font, int x, int y, int w, int h, Text label, boolean hovered, boolean active) {
        int bg = !active ? -14803421 : (hovered ? -13619146 : -14277077);
        VdmTheme.roundedRect(g, x, y, w, h, 4, bg);
        VdmTheme.roundedBorder(g, x, y, w, h, 4, !active ? Short.MIN_VALUE : (hovered ? -26624 : Short.MIN_VALUE));
        Text drawn = label;
        String raw = label.getString();
        if (font.getWidth(raw) > w - 8) {
            drawn = Text.literal((String)VdmTheme.ellipsize(font, raw, w - 8));
        }
        g.drawCenteredTextWithShadow(font, drawn, x + w / 2, y + (h - 8) / 2, active ? -1512722 : -10394002);
    }

    public static String ellipsize(TextRenderer font, String text, int maxWidth) {
        if (text == null || font.getWidth(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String out = text;
        while (!out.isEmpty() && font.getWidth(out + "\u2026") > maxWidth) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "\u2026";
    }

    public static void field(DrawContext g, int x, int y, int w, int h, boolean focused) {
        VdmTheme.roundedRect(g, x, y, w, h, 4, -14474456);
        VdmTheme.roundedBorder(g, x, y, w, h, 4, focused ? -26624 : Short.MIN_VALUE);
    }

    public static void group(DrawContext g, int x, int y, int w, int h) {
        VdmTheme.roundedRect(g, x + 1, y + 2, w, h, 8, 0x59000000);
        VdmTheme.roundedRect(g, x, y, w, h, 8, -652337630);
        VdmTheme.roundedBorder(g, x, y, w, h, 8, Short.MIN_VALUE);
    }

    public static void groupLabel(DrawContext g, TextRenderer font, String text, int x, int y) {
        g.drawText(font, Text.literal((String)text), x, y, -10394002, false);
    }
}

