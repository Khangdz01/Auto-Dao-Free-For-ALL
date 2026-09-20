package com.automine.gui;

import com.automine.AutoMineClient;
import com.automine.core.AutoMineIntRow;
import com.automine.core.AutoMineStringRow;
import com.automine.core.AutoMineBooleanRow;
import com.automine.core.AutoMineButtonRow;
import com.automine.core.AutoMineDoubleRow;
import com.automine.core.AutoMineSpacerRow;
import com.automine.core.AutoMineTextRow;
import com.automine.core.AutoMinePanel;
import com.automine.core.AutoMineDividerRow;
import com.automine.core.AutoMineConfig;
import com.automine.core.AutoMineEngine;
import com.automine.core.AutoMineSelection;
import com.automine.core.AutoMineStaffDetector;
import com.automine.core.VdmFontRenderer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.automine.core.AutoMineProgressNotifier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: AutoMineMainScreen.class */
@Environment(EnvType.CLIENT)
public final class AutoMineMainScreen extends Screen implements StyledScreen {
    public static final int COLOR_BG = -652337630;
    public static final int COLOR_PANEL_BG = 352321535;
    public static final int COLOR_PANEL_BORDER = 1291845632;
    public static final int COLOR_TEXT_WHITE = -1;
    public static final int COLOR_BTN_BG = 536870911;
    public static final int COLOR_BTN_HOVER = -3552303;
    public static final int COLOR_BTN_TEXT = -1;
    public static final int COLOR_ACCENT = -7697004;
    public static final int COLOR_ACCENT_HOVER = 402653183;
    public static final int COLOR_SLIDER_BG = -13948112;
    public static final int COLOR_SLIDER_FG = -1;
    public static final int COLOR_SLIDER_HANDLE = 822083583;
    public static final int COLOR_MUTED = -4604990;
    public static final int PANEL_WIDTH = 200;
    public static final int ROW_HEIGHT = 24;
    public static final int ROW_MARGIN = 8;
    public final Screen parentScreen;
    public final List<AutoMinePanel> panels;
    public AutoMinePanel draggedPanel;
    public boolean isDraggingHud;
    public double dragHudOffsetX;
    public double dragHudOffsetY;
    public boolean bindingFreecamKey = false;

    public AutoMineMainScreen(Screen parentScreen) {
        super(Text.literal("Khangdzlaanh"));
        this.panels = new ArrayList();
        this.parentScreen = parentScreen;
    }

    public static AutoMineSelection getSelection() {
        return AutoMineClient.SELECTION;
    }

    public static AutoMineEngine getEngine() {
        return AutoMineClient.ENGINE;
    }

    public static AutoMineConfig getConfig() {
        return AutoMineClient.CONFIG;
    }

    public void init() {
        this.panels.clear();
        initPanels();
        layoutPanels();
    }

    public void initPanels() {
        AutoMinePanel panel = new AutoMinePanel(this, "Đào", "⛏");
        panel.add(new AutoMineButtonRow(this, () -> {
            return "Đặt điểm 1 (chỗ đang đứng)";
        }, () -> {
            setSelectionPoint(1);
        }, null));
        panel.add(new AutoMineButtonRow(this, () -> {
            return "Đặt điểm 2 (góc đối diện)";
        }, () -> {
            setSelectionPoint(2);
        }, null));
        panel.add(new AutoMineButtonRow(this, () -> {
            return "Xoá vùng";
        }, () -> {
            getSelection().clear();
        }, null));
        panel.add(new AutoMineTextRow(this, () -> {
            return "Vùng: " + getSelection().describe();
        }));
        panel.add(new AutoMineSpacerRow());
        panel.add(new AutoMineButtonRow(this, () -> {
            return "▶ Bắt đầu đào";
        }, () -> {
            if (getEngine().start() == null) {
                close();
            }
        }, () -> {
            return getSelection().isComplete();
        }));
        panel.add(new AutoMineButtonRow(this, () -> {
            return "⏸ Tạm dừng";
        }, () -> {
            getEngine().pause();
        }, null));
        panel.add(new AutoMineButtonRow(this, () -> {
            return "▶ Tiếp tục";
        }, () -> {
            getEngine().resume();
        }, null));
        panel.add(new AutoMineButtonRow(this, () -> {
            return "⏹ Dừng";
        }, () -> {
            getEngine().stop();
        }, null));
        panel.add(new AutoMineSpacerRow());
        panel.add(new AutoMineTextRow(this, () -> {
            return getEngine().statusLine();
        }));
        panel.add(new AutoMineDividerRow(this));
        this.panels.add(panel);

        AutoMinePanel dividerRow = new AutoMinePanel(this, "Tuỳ chọn", "⚙");
        dividerRow.add(new AutoMineBooleanRow(this, "Chạy nhanh", () -> {
            return getConfig().allowSprint;
        }, bool -> {
            getConfig().allowSprint = bool.booleanValue();
        }));
        dividerRow.add(new AutoMineBooleanRow(this, "Xây trụ leo lên", () -> {
            return getConfig().allowPlace;
        }, bool2 -> {
            getConfig().allowPlace = bool2.booleanValue();
        }));
        dividerRow.add(new AutoMineBooleanRow(this, "Hiện khung vùng", () -> {
            return getConfig().renderSelection;
        }, bool3 -> {
            getConfig().renderSelection = bool3.booleanValue();
        }));
        dividerRow.add(new AutoMineBooleanRow(this, "Vét sạch tầng", () -> {
            return getConfig().sweepLayer;
        }, bool4 -> {
            getConfig().sweepLayer = bool4.booleanValue();
        }));
        dividerRow.add(new AutoMineBooleanRow(this, "Đào xuyên GUI / Unfocus", () -> {
            return getConfig().backgroundMining;
        }, boolBg -> {
            getConfig().backgroundMining = boolBg.booleanValue();
        }));
        dividerRow.add(new AutoMineButtonRow(this, () -> {
            return "Freecam (" + AutoMineClient.getFreecamKeyName() + "): " + (com.automine.core.AutoMineFreecam.isActive() ? "§a[BẬT]" : "§c[TẮT]");
        }, () -> {
            com.automine.core.AutoMineFreecam.toggle(this.client);
        }, null));
        dividerRow.add(new AutoMineButtonRow(this, () -> {
            if (this.bindingFreecamKey) {
                return "§e[BẤM PHÍM BẤT KỲ... (ESC xoá)]";
            }
            return "Đổi phím Freecam: §b[" + AutoMineClient.getFreecamKeyName() + "]";
        }, () -> {
            this.bindingFreecamKey = !this.bindingFreecamKey;
        }, null));
        dividerRow.add(new AutoMineIntRow(this, "Tốc độ bay Freecam", () -> {
            return getConfig().freecamSpeed;
        }, speedVal -> {
            getConfig().freecamSpeed = clamp(speedVal, 1, 10);
            getConfig().save();
        }, 1));
        this.panels.add(dividerRow);

        AutoMinePanel intRow = new AutoMinePanel(this, "Thiết lập", "☰");
        intRow.add(new AutoMineIntRow(this, "Cao mỗi tầng", () -> {
            return getConfig().layerHeight;
        }, i -> {
            getConfig().layerHeight = clamp(i, 1, 6);
        }, 1));
        intRow.add(new AutoMineIntRow(this, "Rộng mặt đào", () -> {
            return getConfig().passWidth;
        }, i2 -> {
            getConfig().passWidth = clamp(i2, 1, 5);
        }, 1));
        intRow.add(new AutoMineDoubleRow(this, "Tầm với (block)", () -> {
            return getConfig().reachDistance;
        }, d -> {
            getConfig().reachDistance = d;
        }));
        intRow.add(new AutoMineSpacerRow());
        intRow.add(new AutoMineBooleanRow(this, "Tự động ăn táo vàng", () -> {
            return getConfig().autoEat;
        }, bool5 -> {
            getConfig().autoEat = bool5.booleanValue();
        }));
        intRow.add(new AutoMineIntRow(this, "Ăn khi mất (thanh)", () -> {
            return getConfig().autoEatThreshold;
        }, i3 -> {
            getConfig().autoEatThreshold = clamp(i3, 1, 10);
        }, 1));
        intRow.add(new AutoMineSpacerRow());
        intRow.add(new AutoMineBooleanRow(this, "Quăng exp sửa cúp", () -> {
            return getConfig().expRepair;
        }, bool6 -> {
            getConfig().expRepair = bool6.booleanValue();
        }));
        intRow.add(new AutoMineIntRow(this, "Bền cúp còn", () -> {
            return getConfig().expRepairThreshold;
        }, i4 -> {
            getConfig().expRepairThreshold = clamp(i4, 1, 1000);
        }, 10));
        intRow.add(new AutoMineButtonRow(this, () -> {
            return "Test ném exp (10 bình)";
        }, () -> {
            getEngine().startExpTest();
            close();
        }, null));
        this.panels.add(intRow);

        AutoMinePanel filterPanel = new AutoMinePanel(this, "Lọc block", "⚑");
        filterPanel.add(new AutoMineBooleanRow(this, "Bật lọc block", () -> {
            return getConfig().filterBlocks;
        }, boolFilter -> {
            getConfig().filterBlocks = boolFilter.booleanValue();
        }));
        filterPanel.add(new AutoMineBooleanRow(this, "Chế độ Blacklist (bỏ qua)", () -> {
            return getConfig().filterAsBlacklist;
        }, boolBlacklist -> {
            getConfig().filterAsBlacklist = boolBlacklist.booleanValue();
            getConfig().syncActiveFilter();
            getConfig().save();
        }));
        filterPanel.add(new AutoMineButtonRow(this, () -> {
            return "✦ CHỌN & QUẢN LÝ BLOCK (+ / -) ➔";
        }, () -> {
            if (this.client != null) {
                this.client.setScreen(new AutoMineBlockFilterScreen(this));
            }
        }, null));
        filterPanel.add(new AutoMineButtonRow(this, () -> {
            return "+ Thêm block đang cầm";
        }, () -> {
            if (this.client != null && this.client.player != null) {
                ItemStack stack = this.client.player.getMainHandStack();
                if (!stack.isEmpty()) {
                    Block block = Block.getBlockFromItem(stack.getItem());
                    if (block != null && block != Blocks.AIR) {
                        String id = Registries.BLOCK.getId(block).getPath();
                        getConfig().addFilterBlock(id);
                    }
                }
            }
        }, null));
        filterPanel.add(new AutoMineButtonRow(this, () -> {
            return "🗑 Xoá danh sách đang chọn";
        }, () -> {
            getConfig().clearFilterBlocks();
        }, null));
        filterPanel.add(new AutoMineTextRow(this, () -> {
            int wlCount = getConfig().whitelistBlockList().size();
            int blCount = getConfig().blacklistBlockList().size();
            String modeStr = getConfig().filterAsBlacklist ? ("Blacklist (" + blCount + ")") : ("Whitelist (" + wlCount + ")");
            return "Đang dùng: " + modeStr + " | Lọc: " + (getConfig().filterBlocks ? "§aBẬT" : "§cTẮT");
        }));
        this.panels.add(filterPanel);

        AutoMinePanel stringRow = new AutoMinePanel(this, "Bảo vệ & Bạn bè", "🛡");
        stringRow.add(new AutoMineBooleanRow(this, "Staff List HUD", () -> {
            return getConfig().staffHud;
        }, bool7 -> {
            getConfig().staffHud = bool7.booleanValue();
        }));
        stringRow.add(new AutoMineBooleanRow(this, "Auto Sign khi staff GẦN", () -> {
            return getConfig().autoSign;
        }, bool8 -> {
            getConfig().autoSign = bool8.booleanValue();
        }));
        stringRow.add(new AutoMineIntRow(this, "Phạm vi staff (block)", () -> {
            return getConfig().staffRadius;
        }, i5 -> {
            getConfig().staffRadius = clamp(i5, 1, 128);
        }, 1));
        stringRow.add(new AutoMineButtonRow(this, () -> {
            return "👥 Danh sách staff (" + getConfig().staffList().size() + ") ▸";
        }, () -> {
            if (this.client != null) {
                this.client.setScreen(new AutoMineMiningScreen(this));
            }
        }, null));
        stringRow.add(new AutoMineSpacerRow());
        stringRow.add(new AutoMineStringRow(this, "Whitelist bạn bè (ngăn ,)", () -> {
            return getConfig().friendNames;
        }, strFriends -> {
            getConfig().friendNames = strFriends;
        }));
        stringRow.add(new AutoMineSpacerRow());
        stringRow.add(new AutoMineBooleanRow(this, "Báo Discord nước/lava", () -> {
            return getConfig().alertFluid;
        }, bool9 -> {
            getConfig().alertFluid = bool9.booleanValue();
        }));
        stringRow.add(new AutoMineStringRow(this, "Webhook Discord", () -> {
            return getConfig().alertWebhook;
        }, str -> {
            getConfig().alertWebhook = str;
        }));
        stringRow.add(new AutoMineStringRow(this, "Discord ID (ping)", () -> {
            return getConfig().alertDiscordId;
        }, str2 -> {
            getConfig().alertDiscordId = str2;
        }));
        stringRow.add(new AutoMineSpacerRow());
        stringRow.add(new AutoMineStringRow(this, "Chữ bảng (ngăn |)", () -> {
            return getConfig().signText;
        }, str3 -> {
            getConfig().signText = str3.isBlank() ? getConfig().signText : str3;
        }));
        stringRow.add(new AutoMineStringRow(this, "Tên staff (ngăn ,)", () -> {
            return getConfig().staffNames;
        }, str4 -> {
            getConfig().staffNames = str4.isBlank() ? getConfig().staffNames : str4;
        }));
        stringRow.add(new AutoMineTextRow(this, () -> {
            return AutoMineStaffDetector.onlineStaff().isEmpty() ? "Chưa có staff online" : "⚠ " + String.join(", ", AutoMineStaffDetector.onlineStaff());
        }));
        this.panels.add(stringRow);

        AutoMinePanel webhookPanel = new AutoMinePanel(this, "Báo cáo Webhook", "📡");
        webhookPanel.add(new AutoMineBooleanRow(this, "Bật Webhook tiến độ", () -> {
            return getConfig().webhookEnabled;
        }, boolWh -> {
            getConfig().webhookEnabled = boolWh.booleanValue();
        }));
        webhookPanel.add(new AutoMineStringRow(this, "Discord Webhook URL", () -> {
            return getConfig().webhookUrl;
        }, strWhUrl -> {
            getConfig().webhookUrl = strWhUrl;
        }));
        webhookPanel.add(new AutoMineBooleanRow(this, "Báo mốc 20%", () -> {
            return getConfig().webhookMilestones;
        }, boolMs -> {
            getConfig().webhookMilestones = boolMs.booleanValue();
        }));
        webhookPanel.add(new AutoMineBooleanRow(this, "Báo Pause / Resume", () -> {
            return getConfig().webhookStateChange;
        }, boolSc -> {
            getConfig().webhookStateChange = boolSc.booleanValue();
        }));
        webhookPanel.add(new AutoMineIntRow(this, "Chu kỳ báo (phút)", () -> {
            return getConfig().webhookIntervalMinutes;
        }, iMin -> {
            getConfig().webhookIntervalMinutes = clamp(iMin, 1, 60);
        }, 1));
        webhookPanel.add(new AutoMineBooleanRow(this, "Kèm toạ độ đào", () -> {
            return getConfig().webhookIncludeCoords;
        }, boolCoords -> {
            getConfig().webhookIncludeCoords = boolCoords.booleanValue();
        }));
        webhookPanel.add(new AutoMineButtonRow(this, () -> {
            return "🔔 Gửi tin nhắn TEST";
        }, () -> {
            AutoMineProgressNotifier.sendTest(this.client);
        }, null));
        this.panels.add(webhookPanel);

        AutoMinePanel mediaPanel = new AutoMinePanel(this, "Giải trí & Tiện ích", "🎬");
        mediaPanel.add(new AutoMineButtonRow(this, () -> {
            return "🎵 Xem TikTok";
        }, () -> {
            new AutoMineSpotifyScreen(this).openUrlInBrowser("https://www.tiktok.com");
        }, null));
        mediaPanel.add(new AutoMineButtonRow(this, () -> {
            return "▶ Xem Web / YouTube";
        }, () -> {
            openSpotifyScreen();
        }, null));
        mediaPanel.add(new AutoMineButtonRow(this, () -> {
            return "♫ Mở trình nhạc";
        }, () -> {
            openStaffScreen();
        }, null));
        mediaPanel.add(new AutoMineSpacerRow());
        mediaPanel.add(new AutoMineTextRow(this, () -> {
            String artist = nowArtist();
            String title = nowTitle();
            if (artist.isEmpty() && title.isEmpty()) {
                return "Chưa kết nối Spotify";
            }
            return "♫ " + title + " - " + artist;
        }));
        this.panels.add(mediaPanel);
    }

    public void layoutPanels() {
        int iMax = Math.max(1, (this.width - (8)) / (208));
        int i = 44;
        int iMax2 = 0;
        for (int i2 = 0; i2 < this.panels.size(); i2++) {
            AutoMinePanel panel = this.panels.get(i2);
            int i3 = i2 % iMax;
            if (i3 == 0 && i2 > 0) {
                i += iMax2 + (8);
                iMax2 = 0;
            }
            panel.x = (8) + (i3 * (208));
            panel.y = i;
            iMax2 = Math.max(iMax2, panel.height());
        }
        String[] strArrSplit = getConfig().guiPanels.split("\\|");
        for (int i4 = 0; i4 < this.panels.size(); i4++) {
            AutoMinePanel dividerRow = this.panels.get(i4);
            if (i4 < strArrSplit.length) {
                String[] strArrSplit2 = strArrSplit[i4].split(",");
                if (strArrSplit2.length == (2)) {
                    try {
                        dividerRow.x = Integer.parseInt(strArrSplit2[0].trim());
                        dividerRow.y = Integer.parseInt(strArrSplit2[1].trim());
                    } catch (NumberFormatException e) {
                    }
                }
            }
            dividerRow.x = clamp(dividerRow.x, 0, Math.max(0, this.width - (40)));
            dividerRow.y = clamp(dividerRow.y, 0, Math.max(0, this.height - (24)));
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void savePanelPositions() {
        StringBuilder sb = new StringBuilder();
        for (AutoMinePanel panel : this.panels) {
            if (sb.length() > 0) {
                sb.append(124 );
            }
            sb.append(panel.x).append(44 ).append(panel.y);
        }
        getConfig().guiPanels = sb.toString();
        getConfig().save();
    }

    public void setSelectionPoint(int i) {
        if (this.client == null || this.client.player == null) {
            return;
        }
        if (i == (1)) {
            getSelection().setPos1(this.client.player.getBlockPos());
        } else {
            getSelection().setPos2(this.client.player.getBlockPos());
        }
    }

    public static int clamp(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    public void renderBackground(DrawContext context, int i, int i2, float f) {
        if (this.client == null || this.client.world == null) {
            super.renderBackground(context, i, i2, f);
        } else {
            applyBlur(context);
        }
    }

    public void render(DrawContext context, int i, int i2, float f) {
        super.render(context, i, i2, f);
        String strEllipsize = VdmFontRenderer.ellipsize(this.textRenderer, getEngine().statusLine(), Math.max(40, (this.width - (16)) - (((26) + this.textRenderer.getWidth("Khangdzlaanh")) + (12))));
        VdmFontRenderer.roundedRect(context, 9, 9, this.width - (16), 26, 8, 1291845632);
        VdmFontRenderer.roundedRect(context, 8, 8, this.width - (16), 26, 8, -652337630);
        context.fill(16, 17, 20, 25, -32768);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Khangdzlaanh"), 26, 17, -1);
        context.drawTextWithShadow(this.textRenderer, Text.literal(strEllipsize), (this.width - (16)) - this.textRenderer.getWidth(strEllipsize), 17, -7697004);
        Iterator<AutoMinePanel> it = this.panels.iterator();
        while (it.hasNext()) {
            it.next().render(context, this.textRenderer, i, i2);
        }
        AutoMineStaffDetector.renderPreview(context);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseClicked(Click click, boolean z) {
        if (click.button() != 0) {
            return super.mouseClicked(click, z);
        }
        int[] iArrHudRect = AutoMineStaffDetector.hudRect(this.client, this.width, true);
        if (getConfig() != null && getConfig().staffHud && click.x() >= iArrHudRect[0] && click.x() <= iArrHudRect[0] + iArrHudRect[2] && click.y() >= iArrHudRect[1] && click.y() <= iArrHudRect[1] + iArrHudRect[3]) {
            this.isDraggingHud = true;
            this.dragHudOffsetX = click.x() - ((double) iArrHudRect[0]);
            this.dragHudOffsetY = click.y() - ((double) iArrHudRect[1]);
            return true;
        }
        for (int size = this.panels.size() - (1); size >= 0; size--) {
            AutoMinePanel panel = this.panels.get(size);
            if (panel.inHeader(click.x(), click.y())) {
                this.draggedPanel = panel;
                panel.dragOffX = click.x() - ((double) panel.x);
                panel.dragOffY = click.y() - ((double) panel.y);
                this.panels.remove(panel);
                this.panels.add(panel);
                return true;
            }
            if (panel.inBody(click.x(), click.y())) {
                panel.click(click.x(), click.y());
                return true;
            }
        }
        return super.mouseClicked(click, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseDragged(Click click, double d, double d2) {
        if (this.isDraggingHud) {
            AutoMineStaffDetector.moveHud((int) (click.x() - this.dragHudOffsetX), (int) (click.y() - this.dragHudOffsetY), this.width, this.height);
            return true;
        }
        if (this.draggedPanel == null) {
            return super.mouseDragged(click, d, d2);
        }
        this.draggedPanel.x = clamp((int) (click.x() - this.draggedPanel.dragOffX), 0, Math.max(0, this.width - (40)));
        this.draggedPanel.y = clamp((int) (click.y() - this.draggedPanel.dragOffY), 0, Math.max(0, this.height - (24)));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseReleased(Click click) {
        if (this.isDraggingHud) {
            this.isDraggingHud = false;
            AutoMineStaffDetector.saveHudPos();
            return true;
        }
        if (this.draggedPanel == null) {
            return super.mouseReleased(click);
        }
        this.draggedPanel = null;
        savePanelPositions();
        return true;
    }

    @Override
    public void close() {
        this.bindingFreecamKey = false;
        if (getConfig() != null) {
            getConfig().save();
        }
        if (this.client == null || this.parentScreen == null) {
            super.close();
        } else {
            this.client.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (this.bindingFreecamKey) {
            int key = keyInput.key();
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                AutoMineClient.setFreecamKey(net.minecraft.client.util.InputUtil.UNKNOWN_KEY.getCode());
            } else {
                AutoMineClient.setFreecamKey(key);
            }
            this.bindingFreecamKey = false;
            return true;
        }
        return super.keyPressed(keyInput);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean shouldPause() {
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean isHovered(int i, int i2, int i3, int i4, int i5, int i6) {
        return (i5 < i || i5 > i + i3 || i6 < i2 || i6 >= i2 + i4) ? false : true;
    }

    public static /* synthetic */ String nowArtist() {
        return AutoMineClient.SPOTIFY != null ? AutoMineClient.SPOTIFY.nowArtist() : "";
    }

    public static /* synthetic */ String nowTitle() {
        return AutoMineClient.SPOTIFY != null ? AutoMineClient.SPOTIFY.nowTitle() : "";
    }

    public /* synthetic */ void openSpotifyScreen() {
        if (this.client != null) {
            this.client.setScreen(new AutoMineSpotifyScreen(this));
        }
    }

    public static /* synthetic */ String webYoutubeLabel() {
        return " Xem Web / YouTube";
    }

    public /* synthetic */ void openStaffScreen() {
        if (this.client != null) {
            this.client.setScreen(new AutoMineStaffScreen(this));
        }
    }

    public static /* synthetic */ String musicPlayerLabel() {
        return "♫ Mở trình nhạc";
    }
}
