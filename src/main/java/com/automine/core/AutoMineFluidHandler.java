package com.automine.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineFluidHandler.class */
@Environment(EnvType.CLIENT)
public final class AutoMineFluidHandler {
    private static final int LOCK_DURATION = 15;
    private static final double REACH_SQ = 20.25d;
    private static final int AIM_STUCK_LIMIT = 10;
    private static final int SKIP_COOLDOWN = 100;
    private static final int MAX_CONSECUTIVE_FLUID_TICKS = 20;
    private static Method doItemUseMethod;
    private static int placeCooldown = 0;
    private static BlockPos lockedAnchor = null;
    private static Direction lockedFace = null;
    private static Vec3d lockedAim = null;
    private static BlockPos lockedFluid = null;
    private static int lockTicks = 0;
    private static long activeFluidKey = Long.MIN_VALUE;
    private static int aimStuckTicks = 0;
    private static final HashMap<Long, Integer> skippedFluids = new HashMap<>();
    private static int consecutiveFluidTicks = 0;
    private static boolean isEating = false;

    static {
        doItemUseMethod = null;
        try {
            doItemUseMethod = MinecraftClient.class.getDeclaredMethod("doItemUse", new Class[0]);
            doItemUseMethod.setAccessible(true);
        } catch (Throwable th) {
        }
    }

    public static boolean tick(AutoMineEngine autoMineEngine) {
        MinecraftClient client;
        if (autoMineEngine == null || (client = autoMineEngine.client) == null) {
            return false;
        }
        ClientPlayerEntity player = client.player;
        World world = client.world;
        if (player == null || world == null) {
            return false;
        }
        if (handleAutoEat(client, player, autoMineEngine)) {
            consecutiveFluidTicks = 0;
            return true;
        }
        if (handleFluid(autoMineEngine, client, player, world)) {
            return true;
        }
        consecutiveFluidTicks = 0;
        return false;
    }

    public static boolean isMiningCorridorOrFace(AutoMineEngine autoMineEngine, ClientPlayerEntity player, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        List<BlockPos> listFaceCells = autoMineEngine != null ? autoMineEngine.faceCells() : null;
        if (listFaceCells != null && listFaceCells.contains(pos)) {
            return true;
        }
        if (autoMineEngine != null && autoMineEngine.pendingFluidPos != null && autoMineEngine.pendingFluidPos.equals(pos)) {
            return true;
        }
        if (player != null) {
            BlockPos playerPos = player.getBlockPos();
            if (pos.equals(playerPos) || pos.equals(playerPos.up())) {
                return true;
            }
            Direction horizontalDir = player.getHorizontalFacing();
            BlockPos offsetPos = playerPos.offset(horizontalDir);
            BlockPos offsetPos2 = offsetPos.offset(horizontalDir);
            if (pos.equals(offsetPos) || pos.equals(offsetPos.up()) || pos.equals(offsetPos2) || pos.equals(offsetPos2.up())) {
                return true;
            }
            BlockPos faceCenterPos = autoMineEngine != null ? autoMineEngine.faceCenter() : null;
            if (faceCenterPos != null) {
                int iMin = Math.min(playerPos.getX(), faceCenterPos.getX());
                int iMax = Math.max(playerPos.getX(), faceCenterPos.getX());
                int iMin2 = Math.min(playerPos.getY(), faceCenterPos.getY());
                int iMax2 = Math.max(playerPos.getY() + 1, faceCenterPos.getY() + 1);
                int iMin3 = Math.min(playerPos.getZ(), faceCenterPos.getZ());
                int iMax3 = Math.max(playerPos.getZ(), faceCenterPos.getZ());
                if (pos.getX() >= iMin && pos.getX() <= iMax && pos.getY() >= iMin2 && pos.getY() <= iMax2 && pos.getZ() >= iMin3 && pos.getZ() <= iMax3) {
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public static boolean isFluidLeaking(AutoMineEngine autoMineEngine, World world, BlockPos pos, ClientPlayerEntity player) {
        if (world == null || pos == null) {
            return false;
        }
        BlockPos playerPos = player != null ? player.getBlockPos() : null;
        if (playerPos != null && (playerPos.equals(pos) || playerPos.up().equals(pos))) {
            return true;
        }
        Direction[] directions = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        List<BlockPos> listFaceCells = autoMineEngine != null ? autoMineEngine.faceCells() : null;
        for (Direction direction : directions) {
            BlockPos offsetPos = pos.offset(direction);
            if (!isSolidBlock(world, offsetPos)) {
                if (playerPos != null && offsetPos.getSquaredDistance(playerPos) <= 9.0d) {
                    return true;
                }
                if (listFaceCells != null && !listFaceCells.isEmpty()) {
                    for (BlockPos pos2 : listFaceCells) {
                        if (offsetPos.equals(pos2) || offsetPos.getSquaredDistance(pos2) <= 4.0d) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean handleFluid(AutoMineEngine autoMineEngine, MinecraftClient client, ClientPlayerEntity player, World world) {
        PlacementTarget placementTargetFindBestPlacement;
        if (placeCooldown > 0) {
            placeCooldown--;
        }
        if (lockTicks > 0) {
            lockTicks--;
        } else {
            lockedAnchor = null;
            lockedFace = null;
            lockedAim = null;
            lockedFluid = null;
        }
        if (!skippedFluids.isEmpty()) {
            skippedFluids.replaceAll((l, num) -> {
                return Integer.valueOf(num.intValue() - 1);
            });
            skippedFluids.values().removeIf(num2 -> {
                return num2.intValue() <= 0;
            });
        }
        BlockPos playerPos = player.getBlockPos();
        Vec3d playerPosVec = player.getEyePos();
        consecutiveFluidTicks++;
        if (consecutiveFluidTicks > 20) {
            consecutiveFluidTicks = 0;
            lockedAnchor = null;
            lockedFace = null;
            lockedAim = null;
            lockedFluid = null;
            lockTicks = 0;
            aimStuckTicks = 0;
            activeFluidKey = Long.MIN_VALUE;
            AutoMineMovementHelper.preventJump = false;
            for (int i = 2; i >= -2; i--) {
                for (int i2 = -3; i2 <= 3; i2++) {
                    for (int i3 = -3; i3 <= 3; i3++) {
                        BlockPos subPos = playerPos.add(i2, i, i3);
                        if (isFluid(world, subPos)) {
                            skippedFluids.put(Long.valueOf(subPos.asLong()), 80);
                        }
                    }
                }
            }
            return false;
        }
        boolean z = player.isTouchingWater() || player.isInLava() || isFluid(world, playerPos) || isFluid(world, playerPos.up());
        boolean z2 = isSpongeStack(player.getOffHandStack()) || hasItemInInventory(player, AutoMineFluidHandler::isSpongeStack);
        boolean z3 = AutoMineBlockFilter.hasBuildingBlock(player) || hasItemInInventory(player, AutoMineFluidHandler::isBuildingBlock);
        if (!z2 && !z3) {
            if (z) {
                autoMineEngine.stopWithError("§c[AutoMine] Hết block/bọt biển — dừng để an toàn!");
                autoMineEngine.stop();
                return true;
            }
            return false;
        }
        if (lockedAnchor != null && lockedFace != null && lockedAim != null && lockedFluid != null) {
            if (!skippedFluids.containsKey(Long.valueOf(lockedFluid.asLong())) && playerPosVec.squaredDistanceTo(lockedAim) <= REACH_SQ && isSolidBlock(world, lockedAnchor) && isFluid(world, lockedFluid) && isFluidLeaking(autoMineEngine, world, lockedFluid, player)) {
                return aimAndPlace(autoMineEngine, client, player, world, lockedAnchor, lockedFace, lockedAim, lockedFluid);
            }
            lockedAnchor = null;
            lockedFace = null;
            lockedAim = null;
            lockedFluid = null;
            lockTicks = 0;
            aimStuckTicks = 0;
            activeFluidKey = Long.MIN_VALUE;
        }
        PlacementTarget placementTarget = null;
        double d = Double.MAX_VALUE;
        for (int i4 = 3; i4 >= -3; i4--) {
            for (int i5 = -4; i5 <= 4; i5++) {
                for (int i6 = -4; i6 <= 4; i6++) {
                    BlockPos blockPosResult = playerPos.add(i5, i4, i6);
                    if (isFluid(world, blockPosResult) && !skippedFluids.containsKey(Long.valueOf(blockPosResult.asLong())) && playerPosVec.squaredDistanceTo(new Vec3d(((double) blockPosResult.getX()) + 0.5d, ((double) blockPosResult.getY()) + 0.5d, ((double) blockPosResult.getZ()) + 0.5d)) <= REACH_SQ && isFluidLeaking(autoMineEngine, world, blockPosResult, player) && (placementTargetFindBestPlacement = findBestPlacement(autoMineEngine, world, blockPosResult, playerPosVec, player)) != null) {
                        double deltaY = (placementTargetFindBestPlacement.distSq - (((double) blockPosResult.getY()) * 200.0d)) - (isSourceBlock(world, blockPosResult) ? 5000.0d : 0.0d);
                        if (deltaY < d) {
                            d = deltaY;
                            placementTarget = placementTargetFindBestPlacement;
                        }
                    }
                }
            }
        }
        if (placementTarget == null) {
            aimStuckTicks = 0;
            activeFluidKey = Long.MIN_VALUE;
            return false;
        }
        long fluidPosKey = placementTarget.fluidPos.asLong();
        if (fluidPosKey != activeFluidKey) {
            aimStuckTicks = 0;
            activeFluidKey = fluidPosKey;
        }
        lockedAnchor = placementTarget.anchor;
        lockedFace = placementTarget.face;
        lockedAim = placementTarget.aimPoint;
        lockedFluid = placementTarget.fluidPos;
        lockTicks = LOCK_DURATION;
        return aimAndPlace(autoMineEngine, client, player, world, placementTarget.anchor, placementTarget.face, placementTarget.aimPoint, placementTarget.fluidPos);
    }

    private static PlacementTarget findPlacementForTarget(AutoMineEngine autoMineEngine, World world, BlockPos pos, BlockPos pos2, Vec3d posVec, ClientPlayerEntity player) {
        double deltaY;
        BlockHitResult raycastHit;
        if (pos == null || pos2 == null) {
            return null;
        }
        BlockPos playerPos = player.getBlockPos();
        if (isMiningCorridorOrFace(autoMineEngine, player, pos)) {
            return null;
        }
        PlacementTarget placementTarget = null;
        double d = Double.MAX_VALUE;
        for (Direction direction : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP}) {
            BlockPos offsetPos = pos.offset(direction);
            if (isSolidBlock(world, offsetPos) && !offsetPos.equals(playerPos) && !offsetPos.equals(playerPos.up())) {
                Direction oppositeDir = direction.getOpposite();
                if (!isMiningCorridorOrFace(autoMineEngine, player, offsetPos.offset(oppositeDir))) {
                    if (oppositeDir.getAxis() == Direction.Axis.Y) {
                        deltaY = ((double) offsetPos.getY()) + 0.5d + (((double) oppositeDir.getOffsetY()) * 0.45d);
                    } else {
                        deltaY = ((double) offsetPos.getY()) + 0.5d;
                    }
                    Vec3d targetVec = new Vec3d(((double) offsetPos.getX()) + 0.5d + (((double) oppositeDir.getOffsetX()) * 0.45d), deltaY, ((double) offsetPos.getZ()) + 0.5d + (((double) oppositeDir.getOffsetZ()) * 0.45d));
                    double posX = posVec.squaredDistanceTo(targetVec);
                    if (posX <= REACH_SQ && (((double) oppositeDir.getOffsetX()) * (posVec.x - targetVec.x)) + (((double) oppositeDir.getOffsetY()) * (posVec.y - targetVec.y)) + (((double) oppositeDir.getOffsetZ()) * (posVec.z - targetVec.z)) > 0.01d && (raycastHit = world.raycast(new RaycastContext(posVec, targetVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player))) != null && raycastHit.getType() == HitResult.Type.BLOCK) {
                        BlockPos hitBlockPos = raycastHit.getBlockPos();
                        Direction sideDir = raycastHit.getSide();
                        boolean z = hitBlockPos.equals(offsetPos) && sideDir == oppositeDir;
                        BlockPos offsetPos2 = hitBlockPos.offset(sideDir);
                        boolean z2 = offsetPos2.equals(pos) || offsetPos2.equals(pos2);
                        if ((z || z2) && posX < d) {
                            d = posX;
                            placementTarget = new PlacementTarget(offsetPos, oppositeDir, targetVec, pos2, posX);
                        }
                    }
                }
            }
        }
        return placementTarget;
    }

    private static PlacementTarget findBestPlacement(AutoMineEngine autoMineEngine, World world, BlockPos pos, Vec3d posVec, ClientPlayerEntity player) {
        PlacementTarget placementTargetFindPlacementForTarget;
        PlacementTarget placementTargetFindPlacementForTarget2;
        if (!isMiningCorridorOrFace(autoMineEngine, player, pos) && (placementTargetFindPlacementForTarget2 = findPlacementForTarget(autoMineEngine, world, pos, pos, posVec, player)) != null) {
            return placementTargetFindPlacementForTarget2;
        }
        PlacementTarget placementTarget = null;
        double d = Double.MAX_VALUE;
        for (Direction direction : new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            BlockPos offsetPos = pos.offset(direction);
            if (!isSolidBlock(world, offsetPos) && !isMiningCorridorOrFace(autoMineEngine, player, offsetPos) && (placementTargetFindPlacementForTarget = findPlacementForTarget(autoMineEngine, world, offsetPos, pos, posVec, player)) != null && placementTargetFindPlacementForTarget.distSq < d) {
                d = placementTargetFindPlacementForTarget.distSq;
                placementTarget = placementTargetFindPlacementForTarget;
            }
        }
        return placementTarget;
    }

    private static boolean aimAndPlace(AutoMineEngine autoMineEngine, MinecraftClient client, ClientPlayerEntity player, World world, BlockPos pos, Direction direction, Vec3d posVec, BlockPos pos2) {
        Vec3d playerPosVec = player.getEyePos();
        double posX = playerPosVec.squaredDistanceTo(posVec);
        boolean zIsLava = AutoMineBlockHelper.isLava(world, pos2);
        if (posX > REACH_SQ) {
            AutoMineMovementHelper.preventJump = false;
            return false;
        }
        autoMineEngine.movementHelper.stop();
        AutoMineMovementHelper.preventJump = true;
        if (autoMineEngine.pendingFluidPos != null && (pos.offset(direction).equals(autoMineEngine.pendingFluidPos) || pos2.equals(autoMineEngine.pendingFluidPos))) {
            autoMineEngine.pendingFluidPos = null;
            autoMineEngine.blockBreaker.disarm();
        }
        prepareFluidItem(client, player, world, pos, direction, zIsLava);
        double d = posVec.x - playerPosVec.x;
        double d2 = posVec.y - playerPosVec.y;
        double d3 = posVec.z - playerPosVec.z;
        double dSqrt = Math.sqrt((d * d) + (d3 * d3));
        float degrees = (float) (Math.toDegrees(Math.atan2(d3, d)) - 90.0d);
        float targetPitch = MathHelper.clamp((float) (-Math.toDegrees(Math.atan2(d2, dSqrt))), -90.0f, 90.0f);
        AutoMineRotationHelper.stepYawTo(player, degrees, 60.0f);
        float currentPitch = player.getPitch();
        player.setPitch(currentPitch + MathHelper.clamp(targetPitch - currentPitch, -30.0f, 30.0f));
        HitResult targetHit = player.raycast(4.5d, 0.0f, false);
        if (targetHit instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) targetHit;
            BlockPos hitBlockPos = blockHitResult.getBlockPos();
            Direction sideDir = blockHitResult.getSide();
            BlockPos offsetPos = hitBlockPos.offset(sideDir);
            boolean z = hitBlockPos.equals(pos) && sideDir == direction;
            boolean zEquals = offsetPos.equals(pos2);
            boolean zEquals2 = offsetPos.equals(pos.offset(direction));
            if ((z || zEquals || zEquals2) && placeCooldown <= 0) {
                if (client.interactionManager.interactBlock(player, Hand.MAIN_HAND, blockHitResult).isAccepted()) {
                    player.swingHand(Hand.MAIN_HAND);
                }
                placeCooldown = 2;
                AutoMineMovementHelper.sneakLockTicks = 2;
                lockedAnchor = null;
                lockedFace = null;
                lockedAim = null;
                lockedFluid = null;
                lockTicks = 0;
                aimStuckTicks = 0;
                activeFluidKey = Long.MIN_VALUE;
                consecutiveFluidTicks = 0;
                AutoMineMovementHelper.preventJump = false;
                BlockPos addPos = offsetPos.toImmutable();
                autoMineEngine.placedBlockCache.add(addPos);
                if (autoMineEngine.strategy != null) {
                    autoMineEngine.strategy.placedCells().add(addPos);
                }
                if (addPos.equals(autoMineEngine.pendingFluidPos)) {
                    autoMineEngine.pendingFluidPos = null;
                    autoMineEngine.blockBreaker.disarm();
                }
                autoMineEngine.statusDetail = "chặn " + (zIsLava ? "lava" : "nước") + " tại " + offsetPos.toShortString();
                return true;
            }
        }
        aimStuckTicks++;
        if (aimStuckTicks >= 10) {
            skippedFluids.put(Long.valueOf(pos2.asLong()), 100);
            lockedAnchor = null;
            lockedFace = null;
            lockedAim = null;
            lockedFluid = null;
            lockTicks = 0;
            aimStuckTicks = 0;
            activeFluidKey = Long.MIN_VALUE;
            consecutiveFluidTicks = 0;
            AutoMineMovementHelper.preventJump = false;
            autoMineEngine.statusDetail = "bỏ qua nước " + pos2.toShortString() + " (không đặt được — nhường engine đi tiếp)";
            return false;
        }
        autoMineEngine.statusDetail = "ngắm chặn " + (zIsLava ? "lava" : "nước") + " tại " + pos.offset(direction).toShortString() + " (" + aimStuckTicks + "/10)";
        return true;
    }

    public static boolean isSourceBlock(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        try {
            return world.getBlockState(pos).getFluidState().isStill();
        } catch (Throwable th) {
            return false;
        }
    }

    private static boolean isSolidBlock(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        try {
            return (world.getBlockState(pos).isAir() || isFluid(world, pos)) ? false : true;
        } catch (Throwable th) {
            return false;
        }
    }

    private static void prepareFluidItem(MinecraftClient client, ClientPlayerEntity player, World world, BlockPos pos, Direction direction, boolean z) {
        BlockPos offsetPos = pos.offset(direction);
        PlayerInventory inventory = player.getInventory();
        if (!z && isWaterBlock(world, offsetPos)) {
            if (isSpongeStack(player.getOffHandStack())) {
                return;
            }
            int iPrepareItemInHotbar = prepareItemInHotbar(client, player, AutoMineFluidHandler::isSpongeStack);
            if (iPrepareItemInHotbar >= 0) {
                setSelectedSlot(inventory, iPrepareItemInHotbar);
                return;
            }
        }
        int iFindBuildingBlock = AutoMineBlockFilter.findBuildingBlock(player);
        if (iFindBuildingBlock < 0) {
            iFindBuildingBlock = prepareItemInHotbar(client, player, AutoMineFluidHandler::isBuildingBlock);
        }
        if (iFindBuildingBlock >= 0 && inventory.getSelectedSlot() != iFindBuildingBlock) {
            setSelectedSlot(inventory, iFindBuildingBlock);
        }
    }

    public static boolean isFluid(World world, BlockPos pos) {
        return isWaterBlock(world, pos) || AutoMineBlockHelper.isLava(world, pos);
    }

    public static boolean isWaterBlock(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        try {
            return AutoMineBlockHelper.isWater(world, pos);
        } catch (Throwable th) {
            return false;
        }
    }

    private static boolean handleAutoEat(MinecraftClient client, ClientPlayerEntity player, AutoMineEngine autoMineEngine) {
        int playerLevel = 20;
        try {
            playerLevel = player.getHungerManager().getFoodLevel();
        } catch (Throwable th) {
        }
        if (!isEating && playerLevel <= 10 && prepareItemInHotbar(client, player, AutoMineFluidHandler::isFood) >= 0) {
            isEating = true;
        }
        if (isEating) {
            if (playerLevel >= 20) {
                isEating = false;
                if (client.options != null && client.options.useKey != null) {
                    client.options.useKey.setPressed(false);
                    return false;
                }
                return false;
            }
            int iPrepareItemInHotbar = prepareItemInHotbar(client, player, AutoMineFluidHandler::isFood);
            if (iPrepareItemInHotbar < 0) {
                isEating = false;
                return false;
            }
            setSelectedSlot(player.getInventory(), iPrepareItemInHotbar);
            if (client.options != null && client.options.useKey != null) {
                client.options.useKey.setPressed(true);
            }
            autoMineEngine.movementHelper.stop();
            autoMineEngine.statusDetail = "đang ăn thức ăn (" + playerLevel + "/20)...";
            return true;
        }
        return false;
    }

    public static boolean hasItemInInventory(ClientPlayerEntity player, Predicate<ItemStack> predicate) {
        if (player == null) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack heldStack = inventory.getStack(i);
            if (!heldStack.isEmpty() && predicate.test(heldStack)) {
                return true;
            }
        }
        return false;
    }

    public static int prepareItemInHotbar(MinecraftClient client, ClientPlayerEntity player, Predicate<ItemStack> predicate) {
        if (player == null) {
            return -1;
        }
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack heldStack = inventory.getStack(i);
            if (!heldStack.isEmpty() && predicate.test(heldStack)) {
                return i;
            }
        }
        for (int i2 = 9; i2 < 36; i2++) {
            ItemStack offHandStack = inventory.getStack(i2);
            if (!offHandStack.isEmpty() && predicate.test(offHandStack)) {
                int i3 = 8;
                for (int i4 = 0; i4 < 9; i4++) {
                    if (inventory.getStack(i4).isEmpty()) {
                        i3 = i4;
                        break;
                    }
                }
                if (client != null && client.interactionManager != null && player.playerScreenHandler != null) {
                    client.interactionManager.clickSlot(player.playerScreenHandler.syncId, i2, i3, SlotActionType.SWAP, player);
                } else {
                    ItemStack slotStack = inventory.getStack(i3);
                    inventory.setStack(i3, offHandStack);
                    inventory.setStack(i2, slotStack);
                }
                return i3;
            }
        }
        return -1;
    }

    public static boolean isBuildingBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        Item stackItem = itemStack.getItem();
        if (stackItem instanceof BlockItem) {
            try {
                String lowerCase = stackItem.getTranslationKey().toLowerCase();
                return (lowerCase.contains("torch") || lowerCase.contains("sign") || lowerCase.contains("sapling") || lowerCase.contains("flower") || lowerCase.contains("rail") || lowerCase.contains("ladder") || lowerCase.contains("banner") || lowerCase.contains("carpet") || lowerCase.contains("wire") || lowerCase.contains("lever") || lowerCase.contains("button") || lowerCase.contains("pressure_plate") || lowerCase.contains("door") || lowerCase.contains("trapdoor") || lowerCase.contains("fence_gate") || lowerCase.contains("redstone") || lowerCase.contains("repeater") || lowerCase.contains("comparator")) ? false : true;
            } catch (Throwable th) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpongeStack(ItemStack itemStack) {
        return (itemStack == null || itemStack.isEmpty() || itemStack.getItem() != Blocks.SPONGE.asItem()) ? false : true;
    }

    public static boolean isFood(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        try {
            String lowerCase = itemStack.getItem().getTranslationKey().toLowerCase();
            return lowerCase.contains("cooked_") || lowerCase.contains("bread") || lowerCase.contains("carrot") || lowerCase.contains("potato") || lowerCase.contains("apple") || lowerCase.contains("berry") || lowerCase.contains("berries") || lowerCase.contains("melon") || lowerCase.contains("pie") || lowerCase.contains("stew") || lowerCase.contains("soup") || lowerCase.contains("cookie") || lowerCase.contains("steak") || lowerCase.contains("porkchop") || lowerCase.contains("mutton") || lowerCase.contains("chicken") || lowerCase.contains("beef") || lowerCase.contains("salmon") || lowerCase.contains("cod");
        } catch (Throwable th) {
            return false;
        }
    }

    public static void setSelectedSlot(PlayerInventory inventory, int i) {
        if (inventory == null || i < 0 || i >= 9) {
            return;
        }
        try {
            inventory.getClass().getMethod("setSelectedSlot", Integer.TYPE).invoke(inventory, Integer.valueOf(i));
        } catch (Throwable th) {
            try {
                Field declaredField = inventory.getClass().getDeclaredField("selectedSlot");
                declaredField.setAccessible(true);
                declaredField.setInt(inventory, i);
            } catch (Throwable th2) {
            }
        }
    }

    /* JADX INFO: loaded from: AutoMineFluidHandler$PlacementTarget.class */
    private static final class PlacementTarget {
        final BlockPos anchor;
        final Direction face;
        final Vec3d aimPoint;
        final BlockPos fluidPos;
        final double distSq;

        PlacementTarget(BlockPos pos, Direction direction, Vec3d posVec, BlockPos pos2, double d) {
            this.anchor = pos;
            this.face = direction;
            this.aimPoint = posVec;
            this.fluidPos = pos2;
            this.distSq = d;
        }
    }
}
