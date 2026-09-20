package com.automine.core;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import com.automine.AutoMineClient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.SignItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;

/* JADX INFO: loaded from: AutoMineStaffDetector.class */
@Environment(EnvType.CLIENT)
public final class AutoMineStaffDetector {
    public static final float EXTRA_DETECTION_RADIUS = 2.0f;
    public static boolean signPlaced;
    public static boolean pausedByStaff;
    public static BlockPos placedSignPos;
    public static int signCooldownTicks;
    public static final String HEADER_TEXT = "👤 Online staffs :";
    public static final String DRAG_HINT = "⇕ kéo thả để di chuyển";
    public static final String PROP_STAFFHUD_X = "vdm.staffhud.x";
    public static final String PROP_STAFFHUD_Y = "vdm.staffhud.y";
    public static final String PROP_STAFFHUD_OWNER = "vdm.staffhud.owner";
    public static final String PROP_STAFFHUD_BEAT = "vdm.staffhud.beat";
    public static final String MOD_ID = "automine";
        public static final List<String> onlineStaffNames = new ArrayList();
    public static final List<Text> onlineStaffTexts = new ArrayList();
    public static final List<String> nearbyStaffNames = new ArrayList();

    public static AutoMineConfig getConfig() {
        return AutoMineClient.CONFIG;
    }

    public static List<String> onlineStaff() {
        return onlineStaffNames;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void resumeIfPaused() {
        if (pausedByStaff) {
            AutoMineClient.ENGINE.resume();
            pausedByStaff = false;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void tick(MinecraftClient client) {
        AutoMineConfig config = getConfig();
        if (client.player == null || client.world == null || client.getNetworkHandler() == null || config == null || !(config.staffHud || config.autoSign)) {
            onlineStaffNames.clear();
            onlineStaffTexts.clear();
            nearbyStaffNames.clear();
            signPlaced = false;
            resumeIfPaused();
            return;
        }
        onlineStaffNames.clear();
        onlineStaffTexts.clear();
        String[] strArrSplit = config.staffNames.split(",");
        for (PlayerListEntry playerEntry : client.getNetworkHandler().getPlayerList()) {
            String strName = playerEntry.getProfile().name();
            if (config.isFriend(strName)) {
                continue;
            }
            int length = strArrSplit.length;
            for (int i = 0; i < length; i++) {
                if (strArrSplit[i].trim().equalsIgnoreCase(strName)) {
                    onlineStaffNames.add(strName);
                    onlineStaffTexts.add(playerEntry.getDisplayName() != null ? playerEntry.getDisplayName() : Text.literal(strName));
                    break;
                }
            }
        }
        if (!config.autoSign) {
            signPlaced = false;
            nearbyStaffNames.clear();
            resumeIfPaused();
            return;
        }
        nearbyStaffNames.clear();
        int iMax = Math.max(1, config.staffRadius);
        float f = pausedByStaff ? iMax + 2.0f : iMax;
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player != client.player) {
                String strName2 = player.getGameProfile().name();
                if (config.isFriend(strName2)) {
                    continue;
                }
                int length2 = strArrSplit.length;
                for (int i2 = 0; i2 < length2; i2++) {
                    if (strArrSplit[i2].trim().equalsIgnoreCase(strName2) && client.player.distanceTo(player) <= f) {
                        nearbyStaffNames.add(strName2);
                        break;
                    }
                }
            }
        }
        if (nearbyStaffNames.isEmpty()) {
            resumeIfPaused();
            signPlaced = false;
            placedSignPos = null;
            return;
        }
        if (!pausedByStaff) {
            AutoMineClient.ENGINE.pause();
            pausedByStaff = true;
            client.player.sendMessage(Text.literal("§e⏸ Staff §f" + String.join(", ", nearbyStaffNames) + "§e vào gần (≤" + iMax + " block) — Auto Sign TẠM DỪNG đào, đứng im. Tắt §fAuto Sign§e nếu muốn chạy tiếp."), false);
            if (client.currentScreen instanceof HandledScreen) {
                client.player.closeHandledScreen();
            }
        }
        if (signPlaced) {
            return;
        }
        tryPlaceSign(client);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void tryPlaceSign(MinecraftClient client) {
        if (client.currentScreen instanceof AbstractSignEditScreen) {
            if (placedSignPos != null) {
                String[] tokens = splitSignLines(getConfig().signText);
                client.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(placedSignPos, true, tokens[0], tokens[1], tokens[2], tokens[3]));
            }
            client.setScreen((Screen) null);
            signPlaced = true;
            return;
        }
        if (client.currentScreen != null || client.interactionManager == null) {
            return;
        }
        if (signCooldownTicks > 0) {
            signCooldownTicks -= 1;
            return;
        }
        signCooldownTicks = 10;
        PlayerInventory inventory = client.player.getInventory();
        int i = -1;
        for (int i2 = 0; i2 < (9); i2++) {
            if (inventory.getStack(i2).getItem() instanceof SignItem) {
                i = i2;
                break;
            }
        }
        if (i < 0) {
            for (int i3 = 9; i3 < (36); i3++) {
                if (inventory.getStack(i3).getItem() instanceof SignItem) {
                    client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, i3, inventory.getSelectedSlot(), SlotActionType.SWAP, client.player);
                    return;
                }
            }
            return;
        }
        inventory.setSelectedSlot(i);
        if (client.player.getMainHandStack().getItem() instanceof SignItem) {
            BlockPos playerPos = client.player.getBlockPos();
            Direction[] directions = new Direction[4];
            directions[0] = client.player.getHorizontalFacing();
            directions[1] = client.player.getHorizontalFacing().rotateYClockwise();
            directions[2] = client.player.getHorizontalFacing().rotateYCounterclockwise();
            directions[3] = client.player.getHorizontalFacing().getOpposite();
            int length = directions.length;
            for (int i4 = 0; i4 < length; i4++) {
                BlockPos offsetPos = playerPos.offset(directions[i4]);
                BlockState blockState = client.world.getBlockState(offsetPos);
                BlockState downBlockState = client.world.getBlockState(offsetPos.down());
                if (blockState.isReplaceable() && !downBlockState.isReplaceable()) {
                    BlockHitResult blockHitResult = new BlockHitResult(Vec3d.ofCenter(offsetPos.down()).add(0.0d, 0.5d, 0.0d), Direction.UP, offsetPos.down(), false);
                    placedSignPos = offsetPos;
                    if (client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHitResult) instanceof ActionResult.Success) {
                        client.player.swingHand(Hand.MAIN_HAND);
                        return;
                    }
                    return;
                }
            }
        }
    }

    public static String[] splitSignLines(String str) {
        String[] strArr = new String[4];
        strArr[0] = "";
        strArr[1] = "";
        strArr[2] = "";
        strArr[3] = "";
        if (str == null) {
            return strArr;
        }
        String[] strArrSplit = str.split("\\|");
        for (int i = 0; i < (4) && i < strArrSplit.length; i++) {
            String strTrim = strArrSplit[i].trim();
            strArr[i] = strTrim.length() > (15) ? strTrim.substring(0, 15) : strTrim;
        }
        return strArr;
    }

    public static String getSignStatusText() {
        return signPlaced ? "✔ Đã đặt bảng — đứng im" : "⏳ Đang đặt bảng...";
    }

    public static List<Text> getStaffDisplayList(boolean z) {
        if (onlineStaffTexts.isEmpty()) {
            return z ? List.of(Text.literal("§2SR MOD §fShowered")) : List.of();
        }
        return onlineStaffTexts;
    }

    public static int[] hudRect(MinecraftClient client, int i, boolean z) {
        AutoMineConfig config = getConfig();
        TextRenderer textRenderer = client.textRenderer;
        List<Text> staffList = getStaffDisplayList(z);
        int itemCount = textRenderer.getWidth(" Online staffs :");
        Iterator<Text> it = staffList.iterator();
        while (it.hasNext()) {
            itemCount = Math.max(itemCount, textRenderer.getWidth(it.next()));
        }
        if (config.autoSign) {
            itemCount = Math.max(itemCount, textRenderer.getWidth(getSignStatusText()));
        }
        if (z) {
            itemCount = Math.max(itemCount, textRenderer.getWidth(DRAG_HINT));
        }
        int iMax = Math.max(itemCount, textRenderer.getWidth(getModIdText())) + (16);
        int size = (16) + (staffList.size() * (11)) + (config.autoSign ? 11 : 0) + (11) + (z ? 11 : 0);
        int hudX = getIntProperty(PROP_STAFFHUD_X) != (-2147483648) ? getIntProperty(PROP_STAFFHUD_X) : config.staffHudX;
        int hudY = getIntProperty(PROP_STAFFHUD_Y) != (-2147483648) ? getIntProperty(PROP_STAFFHUD_Y) : config.staffHudY;
        int iMin = hudX >= 0 ? Math.min(hudX, Math.max(0, i - iMax)) : (i - iMax) - (6);
        int iMax2 = Math.max(0, hudY >= 0 ? hudY : 5);
        int[] iArr = new int[4];
        iArr[0] = iMin;
        iArr[1] = iMax2;
        iArr[2] = iMax;
        iArr[3] = size;
        return iArr;
    }

    public static String getModIdText() {
        return "mod: automine";
    }

    public static int getIntProperty(String str) {
        try {
            return Integer.parseInt(System.getProperty(str, ""));
        } catch (NumberFormatException e) {
            return -2147483648;
        }
    }

    public static void moveHud(int i, int i2, int i3, int i4) {
        AutoMineConfig config = getConfig();
        config.staffHudX = Math.max(0, Math.min(i, i3 - (40)));
        config.staffHudY = Math.max(0, Math.min(i2, i4 - (20)));
        System.setProperty(PROP_STAFFHUD_X, Integer.toString(config.staffHudX));
        System.setProperty(PROP_STAFFHUD_Y, Integer.toString(config.staffHudY));
    }

    public static void saveHudPos() {
        getConfig().save();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        AutoMineConfig config = getConfig();
        if (client.player == null || config == null || !config.staffHud || onlineStaffTexts.isEmpty() || client.options.hudHidden) {
            releaseOwnership();
            return;
        }
        if (checkOwnershipHeartbeat()) {
            int hudX = getIntProperty(PROP_STAFFHUD_X);
            int hudY = getIntProperty(PROP_STAFFHUD_Y);
            if (hudX != (-2147483648) && (hudX != config.staffHudX || hudY != config.staffHudY)) {
                config.staffHudX = hudX;
                config.staffHudY = hudY;
                config.save();
            }
            renderHudInternal(context, client, hudRect(client, context.getScaledWindowWidth(), false), false);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void renderPreview(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (getConfig() == null || !getConfig().staffHud) {
            return;
        }
        renderHudInternal(context, client, hudRect(client, context.getScaledWindowWidth(), true), true);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean checkOwnershipHeartbeat() {
        long j;
        long jCurrentTimeMillis = System.currentTimeMillis();
        String property = System.getProperty(PROP_STAFFHUD_OWNER, "");
        try {
            j = Long.parseLong(System.getProperty(PROP_STAFFHUD_BEAT, "0"));
        } catch (NumberFormatException e) {
            j = 0L;
        }
        if (!property.isEmpty() && !property.equals(MOD_ID) && jCurrentTimeMillis - j < (1000L)) {
            return false;
        }
        System.setProperty(PROP_STAFFHUD_OWNER, MOD_ID);
        System.setProperty(PROP_STAFFHUD_BEAT, Long.toString(jCurrentTimeMillis));
        return true;
    }

    public static void releaseOwnership() {
        if (MOD_ID.equals(System.getProperty(PROP_STAFFHUD_OWNER, ""))) {
            System.setProperty(PROP_STAFFHUD_OWNER, "");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void renderHudInternal(DrawContext context, MinecraftClient client, int[] iArr, boolean z) {
        AutoMineConfig config = getConfig();
        TextRenderer textRenderer = client.textRenderer;
        VdmFontRenderer.roundedRect(context, iArr[0], iArr[1], iArr[2], iArr[3], 6, -434891752);
        VdmFontRenderer.roundedBorder(context, iArr[0], iArr[1], iArr[2], iArr[3], 6, 1, -32768);
        int i = iArr[0] + (8);
        int i2 = iArr[1] + (5);
        context.drawText(textRenderer, " Online staffs :", i, i2, -1, true);
        int i3 = i2 + 13;
        Iterator<Text> it = getStaffDisplayList(z).iterator();
        while (it.hasNext()) {
            context.drawText(textRenderer, it.next(), i, i3, -1, true);
            i3 += 11;
        }
        if (config.autoSign) {
            context.drawText(textRenderer, getSignStatusText(), i, i3, -6577749, true);
            i3 += 11;
        }
        context.drawText(textRenderer, getModIdText(), i, i3, -10394002, true);
        int i4 = i3 + 11;
        if (z) {
            context.drawText(textRenderer, DRAG_HINT, i, i4, -10394002, true);
        }
    }
}
