package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineWandHandler.class */
@Environment(EnvType.CLIENT)
public final class AutoMineWandHandler {
    private static boolean wasAttackPressed = false;
    private static boolean wasUsePressed = false;

    public static boolean isHoldingWand(ClientPlayerEntity player) {
        ItemStack itemResultStack;
        return (player == null || (itemResultStack = player.getInventory().getSelectedStack()) == null || itemResultStack.isEmpty() || itemResultStack.getItem() != Items.BLAZE_ROD) ? false : true;
    }

    public static void tick(MinecraftClient client) {
        boolean z;
        boolean z2;
        if (client == null || client.player == null || client.world == null || client.currentScreen != null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (!isHoldingWand(player)) {
            wasAttackPressed = false;
            wasUsePressed = false;
            return;
        }
        if (client.options == null) {
            return;
        }
        if (client.options.attackKey != null) {
            boolean isAttackPressed = client.options.attackKey.isPressed();
            boolean z3 = false;
            while (true) {
                z2 = z3;
                if (!client.options.attackKey.wasPressed()) {
                    break;
                } else {
                    z3 = true;
                }
            }
            if (z2 || (isAttackPressed && !wasAttackPressed)) {
                handlePos1(client, player);
            }
            wasAttackPressed = isAttackPressed;
        }
        if (client.options.useKey != null) {
            boolean isUsePressed = client.options.useKey.isPressed();
            boolean z4 = false;
            while (true) {
                z = z4;
                if (!client.options.useKey.wasPressed()) {
                    break;
                } else {
                    z4 = true;
                }
            }
            if (z || (isUsePressed && !wasUsePressed)) {
                handlePos2(client, player);
            }
            wasUsePressed = isUsePressed;
        }
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0026  */
    private static void handlePos1(MinecraftClient client, ClientPlayerEntity player) {
        BlockPos playerPos;
        HitResult blockHitResult = client.crosshairTarget;
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hitResult2 = (BlockHitResult) blockHitResult;
            if (hitResult2.getType() == HitResult.Type.BLOCK) {
                playerPos = hitResult2.getBlockPos();
            } else {
                playerPos = player.getBlockPos();
            }
        } else {
            playerPos = player.getBlockPos();
        }
        if (AutoMineClient.SELECTION != null) {
            AutoMineClient.SELECTION.setPos1(playerPos);
            AutoMineClient.say(client, "§a[Pos 1] §fĐã chọn điểm 1: §e" + playerPos.toShortString());
        }
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0026  */
    private static void handlePos2(MinecraftClient client, ClientPlayerEntity player) {
        BlockPos playerPos;
        HitResult blockHitResult = client.crosshairTarget;
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hitResult2 = (BlockHitResult) blockHitResult;
            if (hitResult2.getType() == HitResult.Type.BLOCK) {
                playerPos = hitResult2.getBlockPos();
            } else {
                playerPos = player.getBlockPos();
            }
        } else {
            playerPos = player.getBlockPos();
        }
        if (AutoMineClient.SELECTION != null) {
            AutoMineClient.SELECTION.setPos2(playerPos);
            AutoMineClient.say(client, "§b[Pos 2] §fĐã chọn điểm 2: §e" + playerPos.toShortString());
        }
    }
}
