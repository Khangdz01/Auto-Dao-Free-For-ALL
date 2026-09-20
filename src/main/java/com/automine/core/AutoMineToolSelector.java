package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineToolSelector.class */
@Environment(EnvType.CLIENT)
public final class AutoMineToolSelector {

    public static int selectBest(ClientPlayerEntity player, BlockState blockState) {
        PlayerInventory inventory = player.getInventory();
        int windowId = inventory.getSelectedSlot();
        float f = -1.0f;
        int i = 0;
        for (int i2 = 0; i2 < (9); i2++) {
            ItemStack heldStack = inventory.getStack(i2);
            float miningSpeed = heldStack.isEmpty() ? 1.0f : heldStack.getMiningSpeedMultiplier(blockState);
            int i3 = (heldStack.isEmpty() || !heldStack.isSuitableFor(blockState)) ? 0 : 1;
            if ((i3 != 0 && i == 0) || (i3 == i && miningSpeed > f)) {
                f = miningSpeed;
                windowId = i2;
                i = i3;
            }
        }
        if (windowId != inventory.getSelectedSlot()) {
            inventory.setSelectedSlot(windowId);
        }
        return windowId;
    }
}
