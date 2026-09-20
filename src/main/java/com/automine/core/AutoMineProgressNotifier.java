package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class AutoMineProgressNotifier {
    private static int lastMilestone = -1;
    private static long startTime = 0;
    private static int tickCounter = 0;
    private static boolean startAnnounced = false;
    private static boolean completedSent = false;
    private static AutoMineState lastState = AutoMineState.IDLE;

    private AutoMineProgressNotifier() {
    }

    public static void reset() {
        lastMilestone = -1;
        startTime = System.currentTimeMillis();
        tickCounter = 0;
        startAnnounced = false;
        completedSent = false;
        lastState = AutoMineState.IDLE;
    }

    public static void onStart(MinecraftClient client, AutoMineSelection selection, AutoMinePlan plan) {
        reset();
        startTime = System.currentTimeMillis();
        startAnnounced = true;
        lastMilestone = 0;
        lastState = AutoMineState.RUNNING;

        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || !config.webhookEnabled || config.webhookUrl.isBlank()) {
            return;
        }

        int totalBlocks = (int) (selection != null ? selection.volume() : (plan != null && plan.selection != null ? plan.selection.volume() : 0));
        String desc = String.format(
                "**Bắt đầu quá trình đào**\n" +
                "• Mục tiêu: **%d** blocks\n" +
                "• Vùng chọn: %s -> %s\n" +
                "• %s",
                totalBlocks,
                selection != null && selection.pos1() != null ? selection.pos1().toShortString() : "?",
                selection != null && selection.pos2() != null ? selection.pos2().toShortString() : "?",
                formatCoords(client)
        );

        sendWebhook(client, "⛏ AutoMine — Bắt đầu đào", desc, 0x00FF88);
    }

    public static void onTick(MinecraftClient client, AutoMineEngine engine, AutoMinePlan plan, int blocksMined) {
        if (client == null || engine == null) {
            return;
        }

        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || !config.webhookEnabled || config.webhookUrl.isBlank()) {
            return;
        }

        int totalBlocks = (int) (plan != null && plan.selection != null ? plan.selection.volume() : 0);
        double percent = (totalBlocks > 0) ? ((double) blocksMined / totalBlocks) * 100.0d : 0.0d;
        if (plan != null && percent <= 0.0d) {
            percent = plan.progress() * 100.0d;
        }

        // Check milestones (20, 40, 60, 80, 100)
        if (config.webhookMilestones && percent >= 20.0d && percent < 100.0d) {
            int m = (((int) percent) / 20) * 20;
            if (m > lastMilestone) {
                lastMilestone = m;
                String desc = String.format(
                        "**Tiến độ: %d%%**\n" +
                        "• Đã đào: **%d / %d** blocks\n" +
                        "• Layer: %s\n" +
                        "• Thời gian đã chạy: %s\n" +
                        "• %s",
                        m,
                        blocksMined,
                        totalBlocks,
                        plan != null ? plan.describe() : "N/A",
                        formatElapsed(System.currentTimeMillis() - startTime),
                        formatCoords(client)
                );
                sendWebhook(client, "⛏ AutoMine — Mốc " + m + "%", desc, 0xFFAA00);
            }
        }

        // Periodic report (e.g. every N minutes)
        int intervalTicks = Math.max(1, config.webhookIntervalMinutes) * 60 * 20;
        tickCounter++;
        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            String desc = String.format(
                    "**Báo cáo định kỳ (%d phút)**\n" +
                    "• Trạng thái: **%s** (%s)\n" +
                    "• Tiến độ: **%.1f%%** (%d / %d)\n" +
                    "• Layer: %s\n" +
                    "• Thời gian chạy: %s\n" +
                    "• %s",
                    config.webhookIntervalMinutes,
                    engine.state().name(),
                    (engine.statusDetail == null || engine.statusDetail.isEmpty()) ? "Đang đào" : engine.statusDetail,
                    percent,
                    blocksMined,
                    totalBlocks,
                    plan != null ? plan.describe() : "N/A",
                    formatElapsed(System.currentTimeMillis() - startTime),
                    formatCoords(client)
            );
            sendWebhook(client, "📊 AutoMine — Cập nhật định kỳ", desc, 0x55AAFF);
        }
    }

    public static void onStateChange(MinecraftClient client, AutoMineState newState, String reason) {
        if (newState == lastState) {
            return;
        }
        AutoMineState oldState = lastState;
        lastState = newState;

        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || !config.webhookEnabled || config.webhookUrl.isBlank() || !config.webhookStateChange) {
            return;
        }

        if (newState == AutoMineState.PAUSED) {
            String desc = String.format(
                    "**Đã tạm dừng**\n" +
                    "• Lý do: %s\n" +
                    "• %s",
                    reason != null && !reason.isBlank() ? reason : "Người dùng tạm dừng",
                    formatCoords(client)
            );
            sendWebhook(client, "⏸ AutoMine — Tạm dừng", desc, 0xFF5555);
        } else if (newState == AutoMineState.RUNNING && oldState == AutoMineState.PAUSED) {
            String desc = String.format(
                    "**Tiếp tục đào**\n" +
                    "• %s",
                    formatCoords(client)
            );
            sendWebhook(client, "▶ AutoMine — Tiếp tục", desc, 0x55FF55);
        }
    }

    public static void onComplete(MinecraftClient client, boolean success, int blocksMined, AutoMinePlan plan) {
        if (completedSent) {
            return;
        }
        completedSent = true;
        lastMilestone = 100;
        lastState = AutoMineState.DONE;

        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || !config.webhookEnabled || config.webhookUrl.isBlank()) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        int totalBlocks = (int) (plan != null && plan.selection != null ? plan.selection.volume() : blocksMined);
        String title = success ? "✅ AutoMine — Hoàn thành 100%" : "❌ AutoMine — Dừng / Kết thúc";
        String desc = String.format(
                "**%s**\n" +
                "• Tổng blocks đã đào: **%d** / %d\n" +
                "• Tổng thời gian: %s\n" +
                "• Vùng đào: %s\n" +
                "• %s\n" +
                "\n*Ra nhặt đồ hoặc kiểm tra rương!*",
                success ? "Đã đào xong toàn bộ vùng chọn!" : "Quá trình đào đã kết thúc.",
                blocksMined,
                totalBlocks,
                formatElapsed(elapsed),
                plan != null ? plan.describe() : "N/A",
                formatCoords(client)
        );

        sendWebhook(client, title, desc, success ? 0x00FF88 : 0xFF3333);
    }

    public static void sendTest(MinecraftClient client) {
        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || config.webhookUrl.isBlank()) {
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§c[AutoMine] Chưa nhập Webhook URL!"), false);
            }
            return;
        }

        String desc = String.format(
                "**Webhook hoạt động bình thường!**\n" +
                "• Mod: **AutoMine 1.0.0**\n" +
                "• %s",
                formatCoords(client)
        );
        sendWebhook(client, "🔧 AutoMine — TEST Webhook", desc, 0x00D9FF);
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[AutoMine] Đã gửi thông báo TEST qua Discord Webhook!"), false);
        }
    }

    private static void sendWebhook(MinecraftClient client, String title, String desc, int color) {
        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || config.webhookUrl.isBlank()) {
            return;
        }
        String username = "AutoMine";
        try {
            if (client != null && client.player != null) {
                username = "AutoMine (" + client.player.getName().getString() + ")";
            }
        } catch (Throwable th) {
        }
        AutoMineDiscordWebhook.send(config.webhookUrl.trim(), username, "", title, desc, color);
    }

    private static String formatCoords(MinecraftClient client) {
        AutoMineConfig config = AutoMineClient.CONFIG;
        if (config == null || !config.webhookIncludeCoords || client == null || client.player == null) {
            return "";
        }
        int x = client.player.getBlockX();
        int y = client.player.getBlockY();
        int z = client.player.getBlockZ();
        String worldName = client.world != null ? client.world.getRegistryKey().getValue().getPath() : "?";
        return String.format("Vị trí: [%d, %d, %d] | World: %s", x, y, z, worldName);
    }

    private static String formatElapsed(long millis) {
        if (millis <= 0) return "0s";
        long s = millis / 1000;
        long m = s / 60;
        long h = m / 60;
        if (h > 0) {
            return String.format("%dh %02dm %02ds", h, m % 60, s % 60);
        } else if (m > 0) {
            return String.format("%dm %02ds", m, s % 60);
        } else {
            return String.format("%ds", s);
        }
    }
}
