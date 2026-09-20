package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineAutoEat.class */
@Environment(EnvType.CLIENT)
public final class AutoMineAutoEat {
    public static final int EAT_COOLDOWN_TICKS = 20;
    public static int lastFoodLevel = 20;
    public static boolean isEating = false;
    public static int previousSlot = -1;
    public static int eatingStartFoodLevel = 20;
    public static int cooldownTicks = 0;

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean isHoldingUseKey(MinecraftClient client) {
        return (isEating && client.player != null && client.player.isUsingItem()) ? true : false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean checkAndEat(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            return false;
        }
        if (!AutoMineClient.CONFIG.autoEat) {
            if (isEating) {
                restorePreviousSlot(player);
            }
            return false;
        }
        int playerLevel = player.getHungerManager().getFoodLevel();
        if (isEating) {
            if (player.isUsingItem()) {
                return true;
            }
            if (playerLevel <= eatingStartFoodLevel) {
                cooldownTicks = 20;
            }
            restorePreviousSlot(player);
            lastFoodLevel = playerLevel;
            return false;
        }
        if (cooldownTicks > 0) {
            cooldownTicks -= 1;
            return false;
        }
        if (playerLevel > lastFoodLevel) {
            lastFoodLevel = playerLevel;
        }
        if (lastFoodLevel - playerLevel < AutoMineClient.CONFIG.autoEatThreshold * (2)) {
            return false;
        }
        int gappleSlot = findGoldenAppleSlot(player);
        if (gappleSlot < 0) {
            return false;
        }
        previousSlot = player.getInventory().getSelectedSlot();
        player.getInventory().setSelectedSlot(gappleSlot);
        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        eatingStartFoodLevel = playerLevel;
        isEating = true;
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void restorePreviousSlot(ClientPlayerEntity player) {
        if (previousSlot >= 0 && previousSlot < (9)) {
            player.getInventory().setSelectedSlot(previousSlot);
        }
        isEating = false;
        previousSlot = -1;
    }

    public static int findGoldenAppleSlot(ClientPlayerEntity player) {
        for (int i = 0; i < (9); i++) {
            ItemStack heldStack = player.getInventory().getStack(i);
            if (heldStack.getItem() == Items.GOLDEN_APPLE || heldStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                return i;
            }
        }
        return -1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void reset() {
        isEating = false;
        previousSlot = -1;
        lastFoodLevel = 20;
        eatingStartFoodLevel = 20;
        cooldownTicks = 0;
    }
}
