package com.automine.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AutoMineDiscordWebhook {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private AutoMineDiscordWebhook() {
    }

    public static CompletableFuture<Boolean> send(String webhookUrl, String username, String content, String title, String desc, int color) {
        if (webhookUrl == null || webhookUrl.isBlank() || !webhookUrl.startsWith("http")) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            String json = buildJson(username, content, title, desc, color);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl.trim()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(r -> r.statusCode() >= 200 && r.statusCode() < 300)
                    .exceptionally(e -> false);
        } catch (Throwable th) {
            return CompletableFuture.completedFuture(false);
        }
    }

    public static CompletableFuture<Boolean> sendText(String webhookUrl, String username, String content) {
        return send(webhookUrl, username, content, null, null, 0x00FF88);
    }

    private static String buildJson(String username, String content, String title, String desc, int color) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"username\":").append(quote((username == null || username.isBlank()) ? "AutoMine Bot" : username)).append(",");
        sb.append("\"content\":").append(quote(content != null ? content : ""));
        if (title != null || desc != null) {
            sb.append(",\"embeds\":[{");
            if (title != null) {
                sb.append("\"title\":").append(quote(title)).append(",");
            }
            if (desc != null) {
                sb.append("\"description\":").append(quote(desc)).append(",");
            }
            sb.append("\"color\":").append(color);
            sb.append("}]");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String quote(String s) {
        return s == null ? "\"\"" : "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }
}
