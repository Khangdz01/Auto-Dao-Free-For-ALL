package com.automine.modules;

import com.automine.AutoMineClient;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AutoSellModule {
    private static boolean enabled = false;
    private static String command = "sell all";
    private static int threshold = 2; // trigger when <= 2 slots free
    private static int cooldownTicks = 0;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean val) {
        enabled = val;
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String cmd) {
        command = cmd;
    }

    public static int getThreshold() {
        return threshold;
    }

    public static void setThreshold(int t) {
        threshold = t;
    }

    public static void tick(MinecraftClient client) {
        if (!enabled || client == null) return;
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || player.networkHandler == null) return;

        // Count empty slots in player inventory (hotbar 0..8 + main storage 9..35)
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack == null || stack.isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots <= threshold) {
            try {
                // sendChatCommand without the leading slash
                String cleanCmd = command.startsWith("/") ? command.substring(1) : command;
                player.networkHandler.sendChatCommand(cleanCmd);
                cooldownTicks = 100; // 5 second cooldown
                AutoMineClient.say(client, "§6[AutoSell] §aTúi đồ đầy (còn " + emptySlots + " ô trống). Đã tự động gửi /" + cleanCmd + "!");
            } catch (Throwable ignored) {
            }
        }
    }
}
