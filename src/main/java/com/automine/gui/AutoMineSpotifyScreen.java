package com.automine.gui;

import com.automine.core.SpotifyApiBridge;
import com.automine.core.SpotifyTrackInfo;
import com.automine.core.VdmFontRenderer;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;

/* JADX INFO: loaded from: AutoMineSpotifyScreen.class */
@Environment(EnvType.CLIENT)
public final class AutoMineSpotifyScreen extends Screen implements StyledScreen {
    public static final int LIST_WIDTH = 340;
    public static final int ITEM_HEIGHT = 24;
    public static final int LIST_TOP = 62;
    public final Screen parentScreen;
    public TextFieldWidget urlInputField;
    public List<SpotifyTrackInfo> searchResults;
    public String statusMessage;
    public int scrollOffset;
    public static HttpServer httpServer;
    public static boolean serverRunning;
    public static final Pattern YT_URL_PATTERN = Pattern.compile("(?:youtu\\.be/|v=|/shorts/|/embed/|/live/)([A-Za-z0-9_-]{11})");
    public static final Pattern YT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    public static final String TEMP_OVERLAY_DIR = System.getProperty("java.io.tmpdir") + "automine-yt-overlay";
    public static int serverPort = -1;

    public AutoMineSpotifyScreen(Screen parentScreen) {
        super(Text.literal("Xem Web"));
        this.searchResults = List.of();
        this.statusMessage = "§7Dán link YouTube rồi bấm §fXem ngay§7 — cửa sổ sẽ nổi đè lên game.";
        this.parentScreen = parentScreen;
    }

    public int panelLeft() {
        return (this.width / (2)) - (170);
    }

    public int panelBottom() {
        return this.height - (58);
    }

    public int visibleItemCount() {
        return Math.max(1, (panelBottom() - (62)) / (24));
    }

    public void init() {
        int itemHeight = panelLeft();
        this.urlInputField = new TextFieldWidget(this.textRenderer, itemHeight, 26, 164, 18, Text.literal("Tìm YouTube"));
        this.urlInputField.setMaxLength(300);
        this.urlInputField.setEditableColor(-1);
        this.urlInputField.setUneditableColor(-1);
        this.urlInputField.setPlaceholder(Text.literal("§7Tìm YouTube — hoặc dán LINK WEB bất kỳ (facebook, shopee…)"));
        addDrawableChild(this.urlInputField);
        addDrawableChild(AutoMineCustomButton.of((itemHeight + (340)) - (172), 26, 78, 18, "Tìm", customBtn1 -> {
            doSearch();
        }));
        addDrawableChild(AutoMineCustomButton.of((itemHeight + (340)) - (90), 26, 90, 18, "Xem ngay", customBtn2 -> {
            doOpenUrl();
        }));
        addDrawableChild(AutoMineCustomButton.of((this.width / (2)) - (60), this.height - (26), 120, 20, "Đóng", customBtn3 -> {
            close();
        }));
    }

    public void doSearch() {
        String strTrim = this.urlInputField.getText().trim();
        if (strTrim.isEmpty() || tryOpenSpecialUrl(strTrim)) {
            return;
        }
        this.statusMessage = "§7Đang tìm \"" + strTrim + "\"…";
        this.searchResults = List.of();
        this.scrollOffset = 0;
        SpotifyApiBridge.search(strTrim, list -> {
            this.client.execute(() -> {
                this.searchResults = list;
                this.statusMessage = list.isEmpty() ? "§cKhông thấy kết quả nào — thử từ khoá khác." : "§a" + list.size() + " kết quả — bấm một dòng để xem.";
            });
        }, str -> {
            this.client.execute(() -> {
                this.statusMessage = "§cLỗi tìm kiếm: " + str;
            });
        });
    }

    public void doOpenUrl() {
        String strTrim = this.urlInputField.getText().trim();
        if (strTrim.isEmpty() || tryOpenSpecialUrl(strTrim)) {
            return;
        }
        openUrlInBrowser("https://www.youtube.com/results?search_query=" + urlEncode(strTrim));
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean tryOpenSpecialUrl(String str) {
        String albumName = extractYoutubeId(str);
        if (albumName != null) {
            openUrlInBrowser("https://www.youtube.com/watch?v=" + albumName);
            return true;
        }
        if (str.contains("youtube.com/") || str.contains("youtu.be/")) {
            openUrlInBrowser(str.startsWith("http") ? str : "https://" + str);
            return true;
        }
        if (str.startsWith("http://") || str.startsWith("https://")) {
            openUrlInBrowser(str);
            return true;
        }
        if (!str.matches("[\\w.-]+\\.[a-zA-Z]{2,}(/\\S*)?")) {
            return false;
        }
        openUrlInBrowser("https://" + str);
        return true;
    }

    public static String extractYoutubeId(String str) {
        if (YT_ID_PATTERN.matcher(str).matches()) {
            return str;
        }
        Matcher matcher = YT_URL_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public void openUrlInBrowser(String str) {
        String trackTitle = prepareUrl(str);
        if (launchCustomBrowser(trackTitle)) {
            this.statusMessage = "§aĐã mở — kéo dải xám TRÊN CÙNG cửa sổ để dời; kéo mép để to/nhỏ.";
            return;
        }
        try {
            Util.getOperatingSystem().open(trackTitle);
            this.statusMessage = "§eKhông thấy Chrome/Edge — mở bằng trình duyệt mặc định (cửa sổ riêng).";
        } catch (Throwable th) {
            this.statusMessage = "§cKhông mở được trình duyệt: " + th.getMessage();
        }
    }

    public static String prepareUrl(String str) {
        String albumName = extractYoutubeId(str);
        if (albumName == null) {
            return str;
        }
        String trackArtist = createLocalServerUrl(albumName);
        return trackArtist != null ? trackArtist : "https://www.youtube.com/watch?v=" + albumName + "&autoplay=1";
    }

    public static synchronized String createLocalServerUrl(String str) {
        ensureServerRunning();
        if (serverPort < 0) {
            return null;
        }
        return "http://127.0.0.1:" + serverPort + "/?v=" + str;
    }

    public static synchronized void ensureServerRunning() {
        if (httpServer != null) {
            return;
        }
        try {
            HttpServer httpServerCreate = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            httpServerCreate.createContext("/", httpExchange -> {
                String query = httpExchange.getRequestURI().getQuery();
                String strSubstring = "";
                if (query != null) {
                    String[] strArrSplit = query.split("&");
                    int length = strArrSplit.length;
                    for (int i = 0; i < length; i++) {
                        String str = strArrSplit[i];
                        if (str.startsWith("v=")) {
                            strSubstring = str.substring(2);
                        }
                    }
                }
                byte[] bytes = ("<!doctype html><html><head><meta charset=\"utf-8\"><title>YouTube</title><style>html,body{margin:0;height:100%;background:#000;overflow:hidden}body{display:flex;flex-direction:column}#bar{flex:0 0 30px;height:30px;background:rgba(20,20,24,.96);cursor:move;user-select:none;border-bottom:1px solid rgba(255,255,255,.10);display:flex;align-items:center;justify-content:center}#bar::after{content:'';width:46px;height:4px;border-radius:3px;background:rgba(255,255,255,.28)}#bar:hover{background:rgba(34,34,40,.96)}iframe{border:0;width:100%;flex:1 1 auto;display:block}</style></head><body><div id=\"bar\"></div><iframe src=\"https://www.youtube.com/embed/" + strSubstring.replaceAll("[^A-Za-z0-9_-]", "") + "?autoplay=1&fs=1&rel=0&modestbranding=1&playsinline=1&origin=" + ("http://127.0.0.1:" + httpExchange.getLocalAddress().getPort()) + "\" allow=\"autoplay; fullscreen; picture-in-picture; encrypted-media\" allowfullscreen></iframe><script>(function(){var b=document.getElementById('bar'),on=false,px=0,py=0;b.addEventListener('pointerdown',function(e){on=true;px=e.screenX;py=e.screenY;b.setPointerCapture(e.pointerId);e.preventDefault();});b.addEventListener('pointermove',function(e){if(!on)return;var dx=e.screenX-px,dy=e.screenY-py;if(dx||dy){window.moveBy(dx,dy);px=e.screenX;py=e.screenY;}});var end=function(e){on=false;try{b.releasePointerCapture(e.pointerId);}catch(_){}};b.addEventListener('pointerup',end);b.addEventListener('pointercancel',end);})();</script></body></html>").getBytes(StandardCharsets.UTF_8);
                httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                httpExchange.sendResponseHeaders(200, bytes.length);
                OutputStream responseBody = httpExchange.getResponseBody();
                try {
                    responseBody.write(bytes);
                    if (responseBody != null) {
                        responseBody.close();
                    }
                } catch (Throwable th) {
                    if (responseBody != null) {
                        try {
                            responseBody.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            });
            httpServerCreate.setExecutor((Executor) null);
            httpServerCreate.start();
            httpServer = httpServerCreate;
            serverPort = httpServerCreate.getAddress().getPort();
        } catch (Throwable th) {
            httpServer = null;
            serverPort = -1;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean launchCustomBrowser(String str) {
        String trackDuration = findBrowserPath();
        if (trackDuration == null) {
            return false;
        }
        registerShutdownHook();
        int i = 520;
        int i2 = 374;
        int hotbarIndex = 1360;
        int slotIndex = 700;
        try {
            Window window = MinecraftClient.getInstance().getWindow();
            hotbarIndex = window.getX() + Math.max(0, (window.getWidth() - i) - (16));
            slotIndex = window.getY() + Math.max(0, (window.getHeight() - i2) - (56));
        } catch (Throwable th) {
        }
        int i3 = hotbarIndex;
        int i4 = slotIndex;
        Thread thread = new Thread(() -> {
            try {
                killOldOverlayProcesses();
                String[] strArr = new String[10];
                strArr[0] = trackDuration;
                strArr[1] = "--app=" + str;
                strArr[2] = "--user-data-dir=" + TEMP_OVERLAY_DIR;
                strArr[3] = "--window-size=" + i + "," + i2;
                strArr[4] = "--window-position=" + i3 + "," + i4;
                strArr[5] = "--no-first-run";
                strArr[6] = "--no-default-browser-check";
                strArr[7] = "--disable-session-crashed-bubble";
                strArr[8] = "--noerrdialogs";
                strArr[9] = "--autoplay-policy=no-user-gesture-required";
                new ProcessBuilder(strArr).start();
                makeWindowTopmost();
            } catch (Throwable th2) {
            }
        }, "yt-overlay-launch");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public static void killOldOverlayProcesses() {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                String[] strArr = new String[8];
                strArr[0] = "powershell";
                strArr[1] = "-NoProfile";
                strArr[2] = "-ExecutionPolicy";
                strArr[3] = "Bypass";
                strArr[4] = "-WindowStyle";
                strArr[5] = "Hidden";
                strArr[6] = "-Command";
                strArr[7] = "Get-CimInstance Win32_Process -Filter \"Name='chrome.exe' OR Name='msedge.exe'\" | Where-Object { $_.CommandLine -like '*automine-yt-overlay*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }";
                new ProcessBuilder(strArr).start().waitFor(4L, TimeUnit.SECONDS);
                Thread.sleep(400L);
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static synchronized void registerShutdownHook() {
        if (serverRunning) {
            return;
        }
        serverRunning = true;
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(AutoMineSpotifyScreen::cleanupOverlay, "yt-overlay-cleanup"));
        } catch (Throwable th) {
        }
    }

    public static Process killOverlayProcess() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return null;
        }
        try {
            String[] strArr = new String[8];
            strArr[0] = "powershell";
            strArr[1] = "-NoProfile";
            strArr[2] = "-ExecutionPolicy";
            strArr[3] = "Bypass";
            strArr[4] = "-WindowStyle";
            strArr[5] = "Hidden";
            strArr[6] = "-Command";
            strArr[7] = "Get-CimInstance Win32_Process -Filter \"Name='chrome.exe' OR Name='msedge.exe'\" | Where-Object { $_.CommandLine -like '*automine-yt-overlay*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }";
            return new ProcessBuilder(strArr).start();
        } catch (Throwable th) {
            return null;
        }
    }

    public static void cleanupOverlay() {
        killOverlayProcess();
    }

    public static void onGameStopping() {
        try {
            Process activeProcess = killOverlayProcess();
            if (activeProcess != null) {
                activeProcess.waitFor(2L, TimeUnit.SECONDS);
            }
        } catch (Throwable th) {
        }
    }

    public static String findBrowserPath() {
        String[] strArr = new String[5];
        strArr[0] = getEnvVar("ProgramFiles") + "\\Google\\Chrome\\Application\\chrome.exe";
        strArr[1] = getEnvVar("ProgramFiles(x86)") + "\\Google\\Chrome\\Application\\chrome.exe";
        strArr[2] = getEnvVar("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe";
        strArr[3] = getEnvVar("ProgramFiles(x86)") + "\\Microsoft\\Edge\\Application\\msedge.exe";
        strArr[4] = getEnvVar("ProgramFiles") + "\\Microsoft\\Edge\\Application\\msedge.exe";
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            String str = strArr[i];
            if (str != null && new File(str).isFile()) {
                return str;
            }
        }
        return null;
    }

    public static String getEnvVar(String str) {
        String str2 = System.getenv(str);
        return str2 == null ? "" : str2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void makeWindowTopmost() {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            Thread thread = new Thread(() -> {
                try {
                    File file = new File(System.getProperty("java.io.tmpdir"), "automine-yt-topmost.ps1");
                    Files.writeString(file.toPath(), "$ErrorActionPreference=\"SilentlyContinue\"\nAdd-Type @\"\nusing System;using System.Runtime.InteropServices;\npublic struct RC{public int L,T,R,B;}\npublic class WT{\n [DllImport(\"user32.dll\")] public static extern bool SetWindowPos(IntPtr h,IntPtr a,int x,int y,int cx,int cy,uint f);\n [DllImport(\"user32.dll\")] public static extern bool GetWindowRect(IntPtr h,out RC r);\n [DllImport(\"user32.dll\")] public static extern uint GetDpiForWindow(IntPtr h);\n [DllImport(\"gdi32.dll\")] public static extern IntPtr CreateRectRgn(int a,int b,int c,int d);\n [DllImport(\"user32.dll\")] public static extern int SetWindowRgn(IntPtr h,IntPtr r,bool b);\n}\n\"@\n$deadline=(Get-Date).AddSeconds(45)\nwhile((Get-Date) -lt $deadline){\n  $procs = Get-CimInstance Win32_Process -Filter \"Name='chrome.exe' OR Name='msedge.exe'\" | Where-Object { $_.CommandLine -like '*automine-yt-overlay*' }\n  foreach($pr in $procs){\n    $h=(Get-Process -Id $pr.ProcessId).MainWindowHandle\n    if($h -ne 0){\n      $wr=New-Object RC; [WT]::GetWindowRect($h,[ref]$wr) | Out-Null\n      $ww=$wr.R-$wr.L; $wh=$wr.B-$wr.T\n      $dpi=[WT]::GetDpiForWindow($h); if($dpi -le 0){ $dpi=96 }\n      $strip=[int][Math]::Ceiling(34.0*$dpi/96.0)\n      if($ww -gt 0 -and $wh -gt $strip){\n        $rgn=[WT]::CreateRectRgn(0,$strip,$ww,$wh)\n        [WT]::SetWindowRgn($h,$rgn,$true) | Out-Null\n      }\n      [WT]::SetWindowPos($h,[IntPtr]-1,0,0,0,0,3) | Out-Null\n    }\n  }\n  Start-Sleep -Milliseconds 500\n}\n", new OpenOption[0]);
                    String[] strArr = new String[8];
                    strArr[0] = "powershell";
                    strArr[1] = "-NoProfile";
                    strArr[2] = "-ExecutionPolicy";
                    strArr[3] = "Bypass";
                    strArr[4] = "-WindowStyle";
                    strArr[5] = "Hidden";
                    strArr[6] = "-File";
                    strArr[7] = file.getAbsolutePath();
                    new ProcessBuilder(strArr).start();
                } catch (Throwable th) {
                }
            }, "yt-overlay-topmost");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean keyPressed(KeyInput keyInput) {
        if ((keyInput.key() != (257) && keyInput.key() != (335)) || !this.urlInputField.isFocused()) {
            return super.keyPressed(keyInput);
        }
        doSearch();
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        this.scrollOffset = Math.max(0, Math.min(Math.max(0, this.searchResults.size() - visibleItemCount()), this.scrollOffset - ((int) Math.signum(d4))));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseClicked(Click click, boolean z) {
        int hoveredItemIndex;
        if (click.button() != 0 || (hoveredItemIndex = getItemIndexAt(click.x(), click.y())) < 0 || hoveredItemIndex >= this.searchResults.size()) {
            return super.mouseClicked(click, z);
        }
        SpotifyTrackInfo spotifyTrackInfo = this.searchResults.get(hoveredItemIndex);
        String strVideoId = spotifyTrackInfo.videoId();
        if (strVideoId != null) {
            openUrlInBrowser("https://www.youtube.com/watch?v=" + strVideoId);
            this.statusMessage = "§aĐang phát: §f" + spotifyTrackInfo.title();
        } else {
            openUrlInBrowser("https://www.youtube.com/results?search_query=" + urlEncode(spotifyTrackInfo.query()));
        }
        return true;
    }

    public int getItemIndexAt(double d, double d2) {
        int itemHeight = panelLeft();
        return (d < ((double) itemHeight) || d > ((double) (itemHeight + (340))) || d2 < 62.0d || d2 > ((double) panelBottom())) ? -1 : this.scrollOffset + ((int) ((d2 - 62.0d) / 24.0d));
    }

    public void renderBackground(DrawContext context, int i, int i2, float f) {
        super.renderBackground(context, i, i2, f);
        int itemHeight = panelLeft();
        VdmFontRenderer.roundedRect(context, itemHeight - (8), 12, 356, this.height - (24), 8, -267119592);
        VdmFontRenderer.roundedBorder(context, itemHeight - (8), 12, 356, this.height - (24), 8, 1, 587202559);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void render(DrawContext context, int i, int i2, float f) {
        int i3;
        super.render(context, i, i2, f);
        int itemHeight = panelLeft();
        context.drawText(this.textRenderer, " Xem Web · ▶ YouTube", itemHeight, 16, -43691, true);
        int iconSize = visibleItemCount();
        for (int i4 = 0; i4 < iconSize && (i3 = this.scrollOffset + i4) < this.searchResults.size(); i4++) {
            SpotifyTrackInfo spotifyTrackInfo = this.searchResults.get(i3);
            int i5 = (62) + (i4 * (24));
            boolean hovered = !(i < itemHeight || i > itemHeight + 340 || i2 < i5 || i2 > i5 + 22);
            VdmFontRenderer.roundedRect(context, itemHeight, i5, 340, 22, 4, hovered ? 620756991 : 352321535);
            context.drawText(this.textRenderer, truncateString(spotifyTrackInfo.title(), 46), itemHeight + (6), i5 + (3), -1, false);
            if (spotifyTrackInfo.subtitle() != null && !spotifyTrackInfo.subtitle().isEmpty()) {
                context.drawText(this.textRenderer, "§7" + truncateString(spotifyTrackInfo.subtitle(), 52), itemHeight + (6), i5 + (12), -6577749, false);
            }
        }
        if (this.searchResults.size() > iconSize) {
            context.drawText(this.textRenderer, "§8▲▼ lăn chuột để xem thêm", itemHeight, panelBottom() + (2), -7829368, false);
        }
        context.drawText(this.textRenderer, ellipsizeText(this.statusMessage, 340), itemHeight, this.height - (44), -1, true);
    }

    public static String truncateString(String str, int i) {
        if (str == null) {
            return "";
        }
        return str.length() > i ? str.substring(0, i - (1)) + "…" : str;
    }

    public String ellipsizeText(String str, int i) {
        if (str == null) {
            return "";
        }
        if (this.textRenderer.getWidth(str) <= i) {
            return str;
        }
        while (str.length() > (1) && this.textRenderer.getWidth(str + "…") > i) {
            str = str.substring(0, str.length() - (1));
        }
        return str + "…";
    }

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parentScreen);
        }
    }
}
