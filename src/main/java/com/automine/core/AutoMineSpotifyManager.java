package com.automine.core;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.model.MediaKey;
import java.awt.image.BufferedImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import com.automine.AutoMineClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/* JADX INFO: loaded from: AutoMineSpotifyManager.class */
@Environment(EnvType.CLIENT)
public final class AutoMineSpotifyManager {
    public static final int CARD_W = 200;
    public static final int CARD_H = 65;
    public volatile String currentTitle;
    public volatile String currentArtist;
    public volatile boolean isPlaying;
    public volatile int trackLength;
    public volatile int trackPosition;
    public volatile BufferedImage coverImage;
    public SpotifyAPI spotifyApi;
    public boolean connecting;
    public boolean hasSentRequest;
    public long lastTrackFetchTime;
    public volatile boolean connected;
    public SpotifyMediaPoller spotifyAuthHelper;
    public volatile String lastTrackId;
    public volatile long lastProgressUpdateTime;
    public static final Identifier SPOTIFY_COVER_ID = Identifier.of(AutoMineStaffDetector.MOD_ID, "spotify_cover");

    public void initConnection() {
        if (!this.connecting && !this.connected) {
            this.connecting = true;
            new Thread(this::connectInternal, "AutoMine-Spotify").start();
        }
    }

    public void playPause() {
        sendMediaKey(MediaKey.PLAY_PAUSE);
    }

    public void next() {
        sendMediaKey(MediaKey.NEXT);
    }

    public void previous() {
        sendMediaKey(MediaKey.PREV);
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public String nowTitle() {
        return this.currentTitle != null ? this.currentTitle : "";
    }

    public String nowArtist() {
        return this.currentArtist != null ? this.currentArtist : "";
    }

    public void sendMediaKey(MediaKey mediaKey) {
        if (mediaKey != null) {
            sendMediaKeyNative(mediaKey);
        }
    }

    public void render(DrawContext context) {
        renderCard(context);
    }

    public void renderCard(DrawContext context) {
        if (context == null) return;
        int x = AutoMineClient.CONFIG != null ? AutoMineClient.CONFIG.spotifyX : 10;
        int y = AutoMineClient.CONFIG != null ? AutoMineClient.CONFIG.spotifyY : 10;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;
        VdmFontRenderer.roundedRect(context, x, y, CARD_W, CARD_H, 6, 0xD018181C);
        VdmFontRenderer.roundedBorder(context, x, y, CARD_W, CARD_H, 6, 1, 0x40FFFFFF);
        
        VdmFontRenderer.roundedRect(context, x + 6, y + 6, 52, 52, 4, 0xFF28282E);
        drawMusicNote(context, x + 26, y + 26, 0xFF1DB954);
        
        String title = (this.currentTitle != null && !this.currentTitle.isBlank()) ? this.currentTitle : (this.connected ? "Đang chờ bài hát..." : "Chưa kết nối Spotify");
        String artist = (this.currentArtist != null && !this.currentArtist.isBlank()) ? this.currentArtist : (this.connected ? "Spotify" : "Khởi động app Spotify trên PC");
        
        String elTitle = VdmFontRenderer.ellipsize(client.textRenderer, title, CARD_W - 68);
        String elArtist = VdmFontRenderer.ellipsize(client.textRenderer, artist, CARD_W - 68);
        
        context.drawText(client.textRenderer, elTitle, x + 64, y + 12, 0xFFFFFFFF, false);
        context.drawText(client.textRenderer, elArtist, x + 64, y + 26, 0xFFAAAAAA, false);
        
        String status = this.isPlaying ? "▶ Đang phát" : "⏸ Đang dừng";
        context.drawText(client.textRenderer, status, x + 64, y + 42, 0xFF1DB954, false);
    }

    public void uploadCoverTexture(MinecraftClient client, BufferedImage bufferedImage) {
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean isCornerPixel(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        if (i < i4 && i2 < i4) {
            i5 = i4;
            i6 = i4;
        } else if (i >= i3 - i4 && i2 < i4) {
            i5 = (i3 - (1)) - i4;
            i6 = i4;
        } else if (i < i4 && i2 >= i3 - i4) {
            i5 = i4;
            i6 = (i3 - (1)) - i4;
        } else {
            if (i < i3 - i4 || i2 < i3 - i4) {
                return false;
            }
            i5 = (i3 - (1)) - i4;
            i6 = (i3 - (1)) - i4;
        }
        float f = i - i5;
        float f2 = i2 - i6;
        return Math.sqrt((double) ((f * f) + (f2 * f2))) > ((double) i4) ? true : false;
    }

    public static void drawMusicNote(DrawContext context, int i, int i2, int i3) {
        int i4 = 7;
        int i5 = 5;
        int i6 = (i - (i4 / (2))) - (2);
        int i7 = i2 + (5);
        context.fill(i6, i7, i6 + i4, i7 + i5, i3);
        int i8 = i + (2);
        context.fill(i8, i7 - (3), i8 + i4, (i7 - (3)) + i5, i3);
        int i9 = 2;
        int i10 = 16;
        int i11 = (i6 + i4) - i9;
        int i12 = (i7 - i10) + (i5 / (2));
        context.fill(i11, i12, i11 + i9, i7 + (i5 / (2)), i3);
        int i13 = (i8 + i4) - i9;
        int i14 = ((i7 - (3)) - i10) + (i5 / (2));
        context.fill(i13, i14, i13 + i9, (i7 - (3)) + (i5 / (2)), i3);
        int iMin = Math.min(i12, i14);
        context.fill(i11, iMin, i13 + i9, iMin + (3), i3);
    }

    public static String formatTime(int i) {
        int i2 = i / (1000);
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(i2 / (60));
        objArr[1] = Integer.valueOf(i2 % (60));
        return String.format("%d:%02d", objArr);
    }

    public static /* synthetic */ String getCoverTextureName() {
        return "automine_spotify_cover";
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    public /* synthetic */ void sendMediaKeyNative(MediaKey mediaKey) {
        int i;
        try {
            switch (SpotifyMediaKeySwitchMap.$SwitchMap$de$labystudio$spotifyapi$model$MediaKey[mediaKey.ordinal()]) {
                case 1:
                    i = -77;
                    break;
                case 2:
                    i = -80;
                    break;
                case 3:
                    i = -79;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
            int i2 = i;
            User32Library.INSTANCE.keybd_event((byte) i2, (byte) 0, 0, 0L);
            User32Library.INSTANCE.keybd_event((byte) i2, (byte) 0, 2, 0L);
        } catch (Throwable th) {
            SpotifyAPI spotifyAPI = this.spotifyApi;
            if (spotifyAPI != null) {
                try {
                    spotifyAPI.pressMediaKey(mediaKey);
                } catch (Throwable th2) {
                }
            }
        }
    }

    public /* synthetic */ void connectInternal() {
        try {
            this.spotifyApi = SpotifyAPIFactory.create();
            this.spotifyApi.registerListener(new AutoMineSpotifyListener(this));
            this.spotifyApi.initialize();
        } catch (Throwable th) {
            this.currentArtist = "Không nối được Spotify";
        }
    }
}
