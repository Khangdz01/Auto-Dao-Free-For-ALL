package com.automine.core;

import com.automine.AutoMineClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineUpdateService.class */
@Environment(EnvType.CLIENT)
public final class AutoMineUpdateService {
    public static final int COLOR_WATER_EMBED = 3447003;
    public static final int COLOR_LAVA_EMBED = 15548997;
    public static final long ALERT_COOLDOWN_MS = 30000;
    public static boolean alertTriggered;
    public static long lastAlertTime;
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static void tick(MinecraftClient client) {
        if (client == null) {
            return;
        }
        AutoMineConfig autoMineConfig = AutoMineClient.CONFIG;
        AutoMineEngine autoMineEngine = AutoMineClient.ENGINE;
        ClientPlayerEntity player = client.player;
        if (autoMineConfig == null || autoMineEngine == null || player == null || !autoMineConfig.alertFluid) {
            alertTriggered = false;
            return;
        }
        boolean inLava = player.isInLava();
        boolean z = !inLava && player.isTouchingWater();
        if (!inLava && !z) {
            alertTriggered = false;
            return;
        }
        if (autoMineEngine.state() == AutoMineState.RUNNING && !alertTriggered) {
            alertTriggered = true;
            long jCurrentTimeMillis = System.currentTimeMillis();
            if (jCurrentTimeMillis - lastAlertTime >= ALERT_COOLDOWN_MS) {
                lastAlertTime = jCurrentTimeMillis;
                if (autoMineConfig.alertWebhook != null && !autoMineConfig.alertWebhook.isBlank()) {
                    sendDiscordAlert(client, player, inLava);
                }
            }
        }
    }

    public static void sendDiscordAlert(MinecraftClient client, ClientPlayerEntity player, boolean z) {
        String str;
        AutoMineConfig autoMineConfig = AutoMineClient.CONFIG;
        if (autoMineConfig == null || (str = autoMineConfig.alertWebhook) == null || str.isBlank() || !isValidDiscordWebhook(str)) {
            return;
        }
        String changelog = escapeJson(player.getName().getString());
        String latestVer = escapeJson(client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "singleplayer");
        String str2 = player.getBlockX() + " " + player.getBlockY() + " " + player.getBlockZ();
        String downloadUrl = escapeJson(AutoMineClient.ENGINE != null ? AutoMineClient.ENGINE.statusLine() : "");
        String str3 = z ? "  Phát hiện DUNG NHAM khi đang đào!" : "  Phát hiện NƯỚC khi đang đào";
        int i = z ? COLOR_LAVA_EMBED : COLOR_WATER_EMBED;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"username\":\"AutoMine\",");
        sb.append("\"content\":\"").append(formatDiscordMention(autoMineConfig.alertDiscordId)).append("\",");
        sb.append("\"allowed_mentions\":{\"parse\":[\"users\"]},");
        sb.append("\"embeds\":[{\"title\":\"").append(escapeJson(str3)).append("\",").append("\"color\":").append(i).append(",").append("\"description\":\"Máy đào đang xử lý ").append(z ? "**dung nham**." : "**nước**.").append("\",").append("\"thumbnail\":{\"url\":\"https://mc-heads.net/avatar/").append(changelog).append("/100.png\"},").append("\"fields\":[").append("{\"name\":\"  Người chơi\",\"value\":\"`").append(changelog).append("`\",\"inline\":true},").append("{\"name\":\"  Máy chủ\",\"value\":\"`").append(latestVer).append("`\",\"inline\":true},").append("{\"name\":\"  Toạ độ\",\"value\":\"`").append(str2).append("`\",\"inline\":true},").append("{\"name\":\"⛏  Trạng thái\",\"value\":\"`").append(downloadUrl).append("`\",\"inline\":false}").append("],\"footer\":{\"text\":\"AutoMine\",").append("\"icon_url\":\"https://mc-heads.net/avatar/").append(changelog).append("/32.png\"},").append("\"timestamp\":\"").append(Instant.now()).append("\"}]}");
        postWebhookJson(str, sb.toString());
    }

    public static String formatDiscordMention(String str) {
        if (str == null) {
            return "";
        }
        String strReplaceAll = str.replaceAll("[^0-9]", "");
        return strReplaceAll.isEmpty() ? "" : "<@" + strReplaceAll + ">";
    }

    public static boolean isValidDiscordWebhook(String str) {
        if (str == null) {
            return false;
        }
        String lowerCase = str.trim().toLowerCase();
        return (lowerCase.startsWith("https://discord.com/api/webhooks/") || lowerCase.startsWith("https://discordapp.com/api/webhooks/") || lowerCase.startsWith("https://ptb.discord.com/api/webhooks/") || lowerCase.startsWith("https://canary.discord.com/api/webhooks/")) && lowerCase.length() > 40;
    }

    public static void postWebhookJson(String str, String str2) {
        try {
            HTTP_CLIENT.sendAsync(HttpRequest.newBuilder().uri(URI.create(str.trim())).timeout(Duration.ofSeconds(10L)).header("Content-Type", "application/json").header("User-Agent", "AutoMine/1.0").POST(HttpRequest.BodyPublishers.ofString(str2)).build(), HttpResponse.BodyHandlers.ofString()).thenAccept(httpResponse -> {
                int iStatusCode = httpResponse.statusCode();
                if (iStatusCode == 200 || iStatusCode == 204) {
                }
            }).exceptionally(th -> {
                return null;
            });
        } catch (Exception e) {
        }
    }

    public static void sendPlayerMessage(String str) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§b[AutoMine] §r" + str), false);
                }
            });
        }
    }

    public static String escapeJson(String str) {
        return str == null ? "" : str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
