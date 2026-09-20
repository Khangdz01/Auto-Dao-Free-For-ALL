package com.automine.gui;

import com.automine.AutoMineClient;
import com.automine.core.SpotifyApiBridge;
import com.automine.core.SpotifyTrackInfo;
import com.automine.core.VdmFontRenderer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;

/* JADX INFO: loaded from: AutoMineStaffScreen.class */
@Environment(EnvType.CLIENT)
public final class AutoMineStaffScreen extends Screen implements StyledScreen {
    public static final int LIST_WIDTH = 340;
    public static final int ITEM_HEIGHT = 24;
    public static final int LIST_TOP = 62;
    public final Screen parentScreen;
    public boolean isDragging;
    public double dragStartX;
    public double dragStartY;
    public TextFieldWidget searchField;
    public List<SpotifyTrackInfo> trackList;
    public String statusText;
    public String subStatusText;
    public int scrollOffset;
    public boolean isSearching;

    public AutoMineStaffScreen(Screen parentScreen) {
        super(Text.literal("Spotify"));
        this.trackList = List.of();
        this.statusText = "";
        this.subStatusText = "";
        this.parentScreen = parentScreen;
    }

    public int panelLeft() {
        return (this.width / (2)) - (170);
    }

    public int panelBottom() {
        return this.height - (72);
    }

    public int visibleItemCount() {
        return Math.max(1, (panelBottom() - (62)) / (24));
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void init() {
        int staffListLeft = panelLeft();
        this.searchField = new TextFieldWidget(this.textRenderer, staffListLeft, 26, 222, 18, Text.literal("Tìm bài"));
        this.searchField.setMaxLength(160);
        this.searchField.setPlaceholder(Text.literal("§7Tên bài / ca sĩ — hoặc dán link playlist Spotify"));
        addDrawableChild(this.searchField);
        addDrawableChild(AutoMineCustomButton.of((staffListLeft + (340)) - (114), 26, 54, 18, "Tìm", customBtn1 -> {
            searchSpotify();
        }));
        addDrawableChild(AutoMineCustomButton.of((staffListLeft + (340)) - (56), 26, 56, 18, "Top VN", customBtn2 -> {
            loadPlaylist("37i9dQZEVXbLdGSmz6xilI");
        }));
        int i = this.width / (2);
        int i2 = this.height - (58);
        addDrawableChild(AutoMineCustomButton.of(i - (95), i2, 44, 20, "|<<", customBtn3 -> {
            if (AutoMineClient.SPOTIFY != null) {
                AutoMineClient.SPOTIFY.previous();
            }
        }));
        addDrawableChild(new AutoMineCustomButton(i - (47), i2, 94, 20, Text.literal("Phát / Dừng"), customBtn4 -> {
            if (AutoMineClient.SPOTIFY != null) {
                AutoMineClient.SPOTIFY.playPause();
            }
        }, () -> {
            return Boolean.valueOf(AutoMineClient.SPOTIFY != null && AutoMineClient.SPOTIFY.isPlaying());
        }));
        addDrawableChild(AutoMineCustomButton.of(i + (51), i2, 44, 20, ">>|", customBtn5 -> {
            if (AutoMineClient.SPOTIFY != null) {
                AutoMineClient.SPOTIFY.next();
            }
        }));
        addDrawableChild(AutoMineCustomButton.of(i - (60), this.height - (32), 120, 20, "Đóng", customBtn6 -> {
            close();
        }));
        if (this.isSearching || !this.trackList.isEmpty()) {
            return;
        }
        this.isSearching = true;
        loadPlaylist(AutoMineClient.CONFIG.musicPlaylist);
    }

    public void searchSpotify() {
        String strTrim = this.searchField.getText().trim();
        if (strTrim.isEmpty()) {
            return;
        }
        String strExtractPlaylistId = SpotifyApiBridge.extractPlaylistId(strTrim);
        if (strExtractPlaylistId == null) {
            this.subStatusText = "Đang tìm \"" + strTrim + "\"…";
            SpotifyApiBridge.search(strTrim, list -> {
                runOnClient(() -> {
                    this.trackList = list;
                    this.statusText = "Kết quả cho \"" + strTrim + "\"";
                    this.scrollOffset = 0;
                    this.subStatusText = "";
                });
            }, str -> {
                runOnClient(() -> {
                    this.subStatusText = str;
                });
            });
        } else {
            AutoMineClient.CONFIG.musicPlaylist = strExtractPlaylistId;
            AutoMineClient.CONFIG.save();
            loadPlaylist(strExtractPlaylistId);
        }
    }

    public void loadPlaylist(String str) {
        this.subStatusText = "Đang tải playlist…";
        SpotifyApiBridge.loadPlaylist(str, (str2, list) -> {
            runOnClient(() -> {
                this.trackList = list;
                this.statusText = str2 + " (" + list.size() + " bài)";
                this.scrollOffset = 0;
                this.subStatusText = "";
            });
        }, str3 -> {
            runOnClient(() -> {
                this.subStatusText = str3;
            });
        });
    }

    public void playTrack(SpotifyTrackInfo spotifyTrackInfo) {
        if (spotifyTrackInfo == null) return;
        this.subStatusText = "Đang mở: " + spotifyTrackInfo.title() + "…";
        if (AutoMineClient.SPOTIFY != null) {
            SpotifyApiBridge.play(spotifyTrackInfo, AutoMineClient.SPOTIFY, str -> {
                runOnClient(() -> {
                    this.subStatusText = str;
                });
            });
        }
    }

    public void runOnClient(Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }

    public void renderBackground(DrawContext context, int i, int i2, float f) {
        if (this.client == null || this.client.world == null) {
            super.renderBackground(context, i, i2, f);
        } else {
            applyBlur(context);
        }
        int staffListLeft = panelLeft();
        int i3 = 18;
        int staffListTop = (panelBottom() - (18)) + (8);
        VdmFontRenderer.roundedRect(context, staffListLeft - (8), i3 + (3), 360, staffListTop, 8, 1275068416);
        VdmFontRenderer.roundedRect(context, staffListLeft - (10), i3, 360, staffListTop, 8, -1475473898);
        VdmFontRenderer.roundedBorder(context, staffListLeft - (11), i3 - (1), 362, staffListTop + (2), 9, 1, 773699924);
        VdmFontRenderer.roundedBorder(context, staffListLeft - (10), i3, 360, staffListTop, 8, 1, -2142878130);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void render(DrawContext context, int i, int i2, float f) {
        super.render(context, i, i2, f);
        int staffListLeft = panelLeft();
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Kéo thẻ nhạc tới chỗ muốn đặt — bấm một bài để phát."), this.width / (2), 8, -5592406);
        context.drawTextWithShadow(this.textRenderer, Text.literal(VdmFontRenderer.ellipsize(this.textRenderer, this.statusText, 340)), staffListLeft, 50, -14829228);
        int staffListWidth = visibleItemCount();
        int iMin = Math.min(this.trackList.size(), this.scrollOffset + staffListWidth);
        for (int i5 = this.scrollOffset; i5 < iMin; i5++) {
            SpotifyTrackInfo spotifyTrackInfo = this.trackList.get(i5);
            int i6 = (62) + ((i5 - this.scrollOffset) * (24));
            int i7 = (i < staffListLeft || i > staffListLeft + (340) || i2 < i6 || i2 >= i6 + (24)) ? 1 : 0;
            if (i7 != 0) {
                VdmFontRenderer.roundedRect(context, staffListLeft - (4), i6, 348, 22, 3, -13750732);
            }
            TextRenderer textRenderer = this.textRenderer;
            String strEllipsize = VdmFontRenderer.ellipsize(this.textRenderer, spotifyTrackInfo.title(), 332);
            int i8 = i6 + (3);
            int textColor = (i7 != 0) ? -14829228 : -1;
            context.drawText(textRenderer, strEllipsize, staffListLeft, i8, textColor, false);
            context.drawText(this.textRenderer, VdmFontRenderer.ellipsize(this.textRenderer, spotifyTrackInfo.subtitle(), 332), staffListLeft, i6 + (13), -5592406, false);
        }
        if (this.trackList.size() > staffListWidth) {
            int i9 = 62;
            int staffListTop = panelBottom() - (62);
            int iMax = Math.max(12, (staffListTop * staffListWidth) / this.trackList.size());
            int iMax2 = i9 + (((staffListTop - iMax) * this.scrollOffset) / Math.max(1, this.trackList.size() - staffListWidth));
            context.fill(staffListLeft + (340) + (4), i9, staffListLeft + (340) + (7), i9 + staffListTop, -12961222);
            context.fill(staffListLeft + (340) + (4), iMax2, staffListLeft + (340) + (7), iMax2 + iMax, -14829228);
        }
        if (!this.subStatusText.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(VdmFontRenderer.ellipsize(this.textRenderer, this.subStatusText, this.width - (20))), this.width / (2), panelBottom() + (2), -5592406);
        }
        if (AutoMineClient.SPOTIFY != null) {
            AutoMineClient.SPOTIFY.renderCard(context);
        }
        if (this.isDragging) {
            VdmFontRenderer.roundedBorder(context, AutoMineClient.CONFIG.spotifyX - (2), AutoMineClient.CONFIG.spotifyY - (2), 204, 69, 6, 1, -1);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseClicked(Click click, boolean z) {
        int trackIndex;
        int i = AutoMineClient.CONFIG.spotifyX;
        int i2 = AutoMineClient.CONFIG.spotifyY;
        if (click.button() == 0 && click.x() >= i && click.x() <= i + (200) && click.y() >= i2 && click.y() <= i2 + (65)) {
            this.isDragging = true;
            this.dragStartX = click.x() - ((double) i);
            this.dragStartY = click.y() - ((double) i2);
            return true;
        }
        if (super.mouseClicked(click, z)) {
            return true;
        }
        int staffListLeft = panelLeft();
        if (click.button() != 0 || click.x() < staffListLeft - (4) || click.x() > staffListLeft + (340) + (4) || click.y() < 62.0d || click.y() >= panelBottom() || (trackIndex = this.scrollOffset + ((int) ((click.y() - 62.0d) / 24.0d))) < 0 || trackIndex >= this.trackList.size()) {
            return false;
        }
        playTrack(this.trackList.get(trackIndex));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseDragged(Click click, double d, double d2) {
        if (!this.isDragging) {
            return super.mouseDragged(click, d, d2);
        }
        AutoMineClient.CONFIG.spotifyX = clamp((int) (click.x() - this.dragStartX), 0, Math.max(0, this.width - (200)));
        AutoMineClient.CONFIG.spotifyY = clamp((int) (click.y() - this.dragStartY), 0, Math.max(0, this.height - (65)));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseReleased(Click click) {
        if (!this.isDragging) {
            return super.mouseReleased(click);
        }
        this.isDragging = false;
        AutoMineClient.CONFIG.save();
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        int staffListLeft = panelLeft();
        if (d < staffListLeft - (10) || d > staffListLeft + (340) + (10) || d2 < 54.0d || d2 > panelBottom() + (8)) {
            return super.mouseScrolled(d, d2, d3, d4);
        }
        this.scrollOffset = clamp(this.scrollOffset - ((int) Math.signum(d4)), 0, Math.max(0, this.trackList.size() - visibleItemCount()));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean keyPressed(KeyInput keyInput) {
        if (this.searchField == null || !this.searchField.isFocused() || (keyInput.key() != (257) && keyInput.key() != (335))) {
            return super.keyPressed(keyInput);
        }
        searchSpotify();
        return true;
    }

    public static int clamp(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    public void close() {
        if (this.client == null || this.parentScreen == null) {
            super.close();
        } else {
            this.client.setScreen(this.parentScreen);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean shouldPause() {
        return false;
    }
}
