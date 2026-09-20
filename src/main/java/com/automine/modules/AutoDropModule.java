package com.automine.modules;

import com.automine.AutoMineClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;

public final class AutoDropModule {
    private static boolean enabled = false;
    private static int tickCounter = 0;
    private static final Set<String> JUNK_NAMES = new HashSet<>(Arrays.asList(
        "cobblestone",
        "cobbled_deepslate",
        "dirt",
        "gravel",
        "netherrack",
        "andesite",
        "diorite",
        "granite",
        "tuff",
        "sand",
        "sandstone",
        "flint"
    ));

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean val) {
        enabled = val;
    }

    public static Set<String> getJunkList() {
        return Collections.unmodifiableSet(JUNK_NAMES);
    }

    public static boolean addJunk(String name) {
        String clean = name.toLowerCase().trim();
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }
        return JUNK_NAMES.add(clean);
    }

    public static boolean removeJunk(String name) {
        String clean = name.toLowerCase().trim();
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }
        return JUNK_NAMES.remove(clean);
    }

    public static String getJunkListAsString() {
        return String.join(",", JUNK_NAMES);
    }

    public static void setJunkListFromString(String str) {
        if (str == null || str.trim().isEmpty()) return;
        JUNK_NAMES.clear();
        for (String part : str.split(",")) {
            String clean = part.trim().toLowerCase();
            if (!clean.isEmpty()) {
                if (clean.startsWith("minecraft:")) {
                    clean = clean.substring("minecraft:".length());
                }
                JUNK_NAMES.add(clean);
            }
        }
    }

    public static boolean isJunk(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            Identifier id = Registries.ITEM.getId(stack.getItem());
            if (id != null) {
                return JUNK_NAMES.contains(id.getPath());
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static void tick(MinecraftClient client) {
        if (!enabled || client == null) return;
        tickCounter++;
        if (tickCounter % 10 != 0) return; // check every 10 ticks (0.5s)

        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || player.currentScreenHandler == null) return;

        // Scan inventory slots 9 to 44 (main inventory in PlayerScreenHandler)
        for (int slot = 9; slot < 45; slot++) {
            try {
                ItemStack stack = player.currentScreenHandler.getSlot(slot).getStack();
                if (isJunk(stack)) {
                    client.interactionManager.clickSlot(
                        player.currentScreenHandler.syncId,
                        slot,
                        1, // Drop whole stack
                        SlotActionType.THROW, // SlotActionType.THROW
                        player
                    );
                    return; // Drop 1 stack per cycle to avoid packet spam
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
