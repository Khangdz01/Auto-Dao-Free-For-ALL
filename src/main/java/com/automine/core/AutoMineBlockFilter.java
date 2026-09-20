package com.automine.core;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineBlockFilter.class */
@Environment(EnvType.CLIENT)
public final class AutoMineBlockFilter {
    public static final int BRIDGE_COOLDOWN_TICKS = 2;
    public static final int SCAFFOLD_COOLDOWN_TICKS = 5;
    public static final float AIM_PITCH_MIN = 45.0f;
    public static final float AIM_PITCH_MAX = 85.0f;
    public static final int MAX_PLACED_CACHE = 64;
    public BlockPos activeBridgeTarget;
    public int bridgeCooldown;
    public final MinecraftClient client;
    public BlockPos activeScaffoldTarget;
    public int scaffoldCooldown;
    public int scaffoldStateTicks;
    public AutoMineScaffoldState scaffoldState = AutoMineScaffoldState.READY;
    public final LinkedHashSet<BlockPos> placedBlocks = new LinkedHashSet<>();

    public AutoMineBlockFilter(MinecraftClient client) {
        this.client = client;
    }

    public void reset() {
        this.scaffoldState = AutoMineScaffoldState.READY;
        this.activeScaffoldTarget = null;
        this.scaffoldCooldown = 0;
        this.scaffoldStateTicks = 0;
        this.activeBridgeTarget = null;
        this.bridgeCooldown = 0;
    }

    public Set<BlockPos> placedCells() {
        return this.placedBlocks;
    }

    public void forgetPlaced() {
        this.placedBlocks.clear();
    }

    public void rememberPlaced(BlockPos pos) {
        BlockPos addPos = pos.toImmutable();
        this.placedBlocks.remove(addPos);
        this.placedBlocks.add(addPos);
        while (this.placedBlocks.size() > 64) {
            Iterator<BlockPos> it = this.placedBlocks.iterator();
            it.next();
            it.remove();
        }
    }

    public int failedHops() {
        return this.scaffoldStateTicks;
    }

    public static int findBuildingBlock(ClientPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack heldStack = inventory.getStack(i);
            if (!heldStack.isEmpty()) {
                Item blockItem = heldStack.getItem();
                if ((blockItem instanceof BlockItem) && ((BlockItem) blockItem).getBlock().getDefaultState().isOpaqueFullCube()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean hasBuildingBlock(ClientPlayerEntity player) {
        return findBuildingBlock(player) >= 0;
    }

    public boolean fillCell(ClientPlayerEntity player, BlockPos pos, double d) {
        int iFindBuildingBlock;
        ClientWorld world = this.client.world;
        if (world == null || this.client.interactionManager == null || !world.getBlockState(pos).isReplaceable() || (iFindBuildingBlock = findBuildingBlock(player)) < 0) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        if (inventory.getSelectedSlot() != iFindBuildingBlock) {
            inventory.setSelectedSlot(iFindBuildingBlock);
        }
        if (this.scaffoldCooldown > 0) {
            this.scaffoldCooldown--;
            return true;
        }
        if (tryPlaceBlock(world, pos)) {
            this.scaffoldCooldown = 2;
            return true;
        }
        return false;
    }

    public static Vec3d findPlacementHitVec(World world, ClientPlayerEntity player, BlockPos pos, double d) {
        Vec3d playerPosVec = player.getEyePos();
        Vec3d posVec = null;
        double d2 = d * d;
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(offsetPos);
            if (!blockState.isAir() && blockState.getFluidState().isEmpty() && blockState.isOpaqueFullCube()) {
                Vec3d faceCenterVec = AutoMineBlockHelper.faceCenter(offsetPos, direction.getOpposite());
                double posX = playerPosVec.squaredDistanceTo(faceCenterVec);
                if (posX <= d2) {
                    d2 = posX;
                    posVec = faceCenterVec;
                }
            }
        }
        return posVec;
    }

    public static boolean canPlaceAgainst(World world, ClientPlayerEntity player, Vec3d posVec, BlockPos pos, Vec3d targetVec, Direction direction) {
        return true;
    }

    public static boolean canFillFrom(World world, ClientPlayerEntity player, BlockPos pos, BlockPos pos2, double d) {
        Vec3d posVec = new Vec3d(((double) pos.getX()) + 0.5d, pos.getY() + player.getStandingEyeHeight(), ((double) pos.getZ()) + 0.5d);
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos2.offset(direction);
            BlockState blockState = world.getBlockState(offsetPos);
            if (blockState.isOpaqueFullCube() && blockState.getFluidState().isEmpty()) {
                Vec3d faceCenterVec = AutoMineBlockHelper.faceCenter(offsetPos, direction.getOpposite());
                if (posVec.squaredDistanceTo(faceCenterVec) <= d * d && canPlaceAgainst(world, player, posVec, offsetPos, faceCenterVec, direction.getOpposite())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tick(ClientPlayerEntity player, boolean[] zArr) {
        int iFindBuildingBlock;
        int i;
        ClientWorld world = this.client.world;
        if (world == null || this.client.interactionManager == null || (iFindBuildingBlock = findBuildingBlock(player)) < 0) {
            return false;
        }
        if (this.bridgeCooldown >= 3) {
            this.bridgeCooldown = 0;
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        if (inventory.getSelectedSlot() != iFindBuildingBlock) {
            inventory.setSelectedSlot(iFindBuildingBlock);
        }
        if (this.scaffoldCooldown > 0) {
            this.scaffoldCooldown--;
        }
        switch (this.scaffoldState.ordinal()) {
            case 0:
                lookDownForScaffold(player);
                if (player.isOnGround() && this.scaffoldCooldown == 0 && player.getPitch() >= 85.0f) {
                    this.activeScaffoldTarget = player.getBlockPos();
                    zArr[0] = true;
                    this.scaffoldState = AutoMineScaffoldState.RISING;
                    return true;
                }
                return true;
            case 1:
                if (this.activeScaffoldTarget == null) {
                    this.scaffoldState = AutoMineScaffoldState.READY;
                    return true;
                }
                BlockPos pos = new BlockPos(player.getBlockX(), this.activeScaffoldTarget.getY(), player.getBlockZ());
                lookDownForScaffold(player);
                if (!world.getBlockState(pos).isAir()) {
                    this.scaffoldState = AutoMineScaffoldState.PLACED;
                    this.scaffoldStateTicks = 0;
                    this.activeBridgeTarget = pos.toImmutable();
                    return true;
                }
                if (player.isOnGround()) {
                    this.scaffoldStateTicks++;
                    this.scaffoldState = AutoMineScaffoldState.READY;
                    this.scaffoldCooldown = 5;
                    return true;
                }
                if (this.scaffoldCooldown == 0 && isAboveBlock(player, pos) && tryPlaceBlock(world, pos)) {
                    this.scaffoldCooldown = 4;
                    return true;
                }
                return true;
            case 2:
                if (player.isOnGround()) {
                    this.scaffoldState = AutoMineScaffoldState.SETTLE;
                    this.scaffoldCooldown = 5;
                    return true;
                }
                return true;
            case 3:
                if (this.scaffoldCooldown == 0) {
                    if (this.activeBridgeTarget == null || !world.getBlockState(this.activeBridgeTarget).isAir()) {
                        i = 0;
                    } else {
                        int i2 = this.bridgeCooldown + 1;
                        i = i2;
                        this.bridgeCooldown = i2;
                    }
                    this.bridgeCooldown = i;
                    this.activeBridgeTarget = null;
                    this.scaffoldState = AutoMineScaffoldState.READY;
                    this.activeScaffoldTarget = null;
                    return true;
                }
                return true;
            default:
                return true;
        }
    }

    public static void lookDownForScaffold(ClientPlayerEntity player) {
        player.setPitch(Math.min(90.0f, player.getPitch() + 45.0f));
    }

    public static boolean isAboveBlock(ClientPlayerEntity player, BlockPos pos) {
        return (player.getBoundingBox().minY > ((((double) pos.getY()) + 1.0d) - 0.02d) ? 1 : (player.getBoundingBox().minY == ((((double) pos.getY()) + 1.0d) - 0.02d) ? 0 : -1)) >= 0;
    }

    /* JADX WARN: Code duplicated, block: B:55:0x01dc  */
    public boolean tryPlaceBlock(World world, BlockPos pos) {
        double deltaY;
        if (world == null || pos == null || this.client == null || this.client.interactionManager == null || this.client.player == null) {
            return false;
        }
        ClientPlayerEntity player = this.client.player;
        HitResult blockHitResult = this.client.crosshairTarget;
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hitResult2 = (BlockHitResult) blockHitResult;
            if (blockHitResult.getType() == HitResult.Type.BLOCK && hitResult2.getBlockPos().offset(hitResult2.getSide()).equals(pos)) {
                if (this.client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult2).isAccepted()) {
                    player.swingHand(Hand.MAIN_HAND);
                }
                AutoMineMovementHelper.sneakLockTicks = 4;
                rememberPlaced(pos);
                return true;
            }
        }
        Vec3d playerPosVec = player.getEyePos();
        BlockPos pos2 = null;
        Direction direction = null;
        Vec3d posVec = null;
        double d = 20.25d;
        for (Direction direction2 : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction2);
            BlockState blockState = world.getBlockState(offsetPos);
            if (!blockState.isAir() && blockState.getFluidState().isEmpty() && blockState.isOpaqueFullCube()) {
                Direction oppositeDir = direction2.getOpposite();
                double deltaX = ((double) offsetPos.getX()) + 0.5d + (((double) oppositeDir.getOffsetX()) * 0.5d);
                double deltaZ = ((double) offsetPos.getZ()) + 0.5d + (((double) oppositeDir.getOffsetZ()) * 0.5d);
                if (oppositeDir.getAxis() == Direction.Axis.Y) {
                    deltaY = ((double) offsetPos.getY()) + 0.5d + (((double) oppositeDir.getOffsetY()) * 0.5d);
                } else {
                    deltaY = ((double) offsetPos.getY()) + 0.5d;
                }
                Vec3d targetVec = new Vec3d(deltaX, deltaY, deltaZ);
                double posX = playerPosVec.squaredDistanceTo(targetVec);
                if (posX <= d) {
                    BlockHitResult raycastHit = world.raycast(new RaycastContext(playerPosVec, targetVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
                    if (raycastHit != null && raycastHit.getType() == HitResult.Type.BLOCK) {
                        boolean zEquals = raycastHit.getBlockPos().equals(offsetPos);
                        boolean zEquals2 = raycastHit.getBlockPos().offset(raycastHit.getSide()).equals(pos);
                        if (zEquals || zEquals2) {
                            d = posX;
                            pos2 = offsetPos;
                            direction = oppositeDir;
                            posVec = targetVec;
                        }
                    } else {
                        d = posX;
                        pos2 = offsetPos;
                        direction = oppositeDir;
                        posVec = targetVec;
                    }
                }
            }
        }
        if (pos2 == null || direction == null || posVec == null) {
            return false;
        }
        double d2 = posVec.x - playerPosVec.x;
        double d3 = posVec.y - playerPosVec.y;
        double d4 = posVec.z - playerPosVec.z;
        double dSqrt = Math.sqrt((d2 * d2) + (d4 * d4));
        if (this.client.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(posVec, direction, pos2, false)).isAccepted()) {
            player.swingHand(Hand.MAIN_HAND);
        }
        AutoMineMovementHelper.sneakLockTicks = 4;
        rememberPlaced(pos);
        return true;
    }
}
