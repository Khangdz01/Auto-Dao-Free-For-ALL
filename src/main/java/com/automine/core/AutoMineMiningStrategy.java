package com.automine.core;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineMiningStrategy.class */
@Environment(EnvType.CLIENT)
public final class AutoMineMiningStrategy {
    public static final double CENTER_OFFSET_TOLERANCE = 0.12d;
    public static final double STEP_ALIGN_THRESHOLD = 0.45d;
    public static final double CORNER_ROUNDING_RADIUS = 0.7d;
    public static final double MIN_MOVE_DELTA = 0.06d;
    public static final int STUCK_TICKS_THRESHOLD = 12;
    public static final double JUMP_CLEARANCE_HEIGHT = 1.2d;
    public static final int MAX_PATH_TICKS = 50;
    public static final int ALIGN_TIMEOUT_TICKS = 40;
    public static final float LOOK_PITCH_DOWN = 8.0f;
    public static final float LOOK_PITCH_UP = 12.0f;
    public static final double DIG_REACH_MARGIN = 0.75d;
    public static final float MAX_TURN_PITCH = 50.0f;
    public static final float MAX_TURN_YAW = 40.0f;
    public static final int IDLE_RESET_TICKS = 600;
    public static final double LOOK_AHEAD_DISTANCE = 0.15d;
    public static final int MAX_JUMP_RETRY = 6;
    public static final double EPSILON_SQ = 0.0016d;
    public static final int STUCK_RECOVERY_TICKS = 40;
    public static final int MAX_CELL_EVALUATIONS = 100;
    public static final int MAX_STEP_AHEAD = 3;
    public static final int MAX_BREAK_TICKS = 200;
    public static final int MAX_SCAFFOLD_HEIGHT = 6;
    public final MinecraftClient client;
    public final AutoMineConfig config;
    public final AutoMineMovementHelper movementHelper;
    public final AutoMineBlockBreaker blockBreaker;
    public final AutoMineBlockFilter blockFilter;
    public final AutoMineSelection selection;
    public List<BlockPos> pathNodes;
    public BlockPos currentTargetPos;
    public int pathIndex;
    public int stuckTicks;
    public int alignTicks;
    public int repathTicks;
    public BlockPos lastPlayerPos;
    public int jumpCooldown;
    public BlockPos scaffoldTarget;
    public boolean isAscending;
    public boolean isDescending;
    public BlockPos activeBreakPos;
    public int breakDurationTicks;
    public int faceEvaluationScore;
    public Direction.Axis primaryTravelAxis;
    public double targetPosX;
    public double targetPosZ;
    public int strafeDirection;
    public boolean isAligning;
    public int pauseTicks;
    public int retryCount;
    public Direction targetFacing;
    public int scaffoldStage;
    public int consecutiveFails;
    public BlockPos lastFailedPos;
    public static final int PRIORITY_NORMAL = 1;
    public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final int[] Y_OFFSETS = {0, 1, -1};
    public double bestPathDistance = Double.MAX_VALUE;
    public int lastEvaluatedTick = Integer.MIN_VALUE;
    public int lastClearTick = Integer.MIN_VALUE;
    public double lastYawTarget = Double.NaN;
    public int bridgePlaceCooldown = 0;
    public int bridgeCooldown = 0;

    public AutoMineMiningStrategy(MinecraftClient client, AutoMineConfig autoMineConfig, AutoMineMovementHelper autoMineMovementHelper, AutoMineBlockBreaker autoMineBlockBreaker, AutoMineSelection autoMineSelection) {
        this.client = client;
        this.config = autoMineConfig;
        this.movementHelper = autoMineMovementHelper;
        this.blockBreaker = autoMineBlockBreaker;
        this.blockFilter = new AutoMineBlockFilter(client);
        this.selection = autoMineSelection;
    }

    public void reset() {
        this.alignTicks = 0;
        this.repathTicks = 0;
        this.lastPlayerPos = null;
        this.jumpCooldown = 0;
        this.scaffoldTarget = null;
        this.bestPathDistance = Double.MAX_VALUE;
        this.pathNodes = null;
        this.currentTargetPos = null;
        this.pathIndex = 0;
        this.stuckTicks = 0;
        this.isAscending = false;
        this.isDescending = false;
        this.activeBreakPos = null;
        this.breakDurationTicks = 0;
        this.faceEvaluationScore = 0;
        this.primaryTravelAxis = null;
        this.strafeDirection = 0;
        this.pauseTicks = 0;
        this.targetFacing = null;
        this.scaffoldStage = 0;
        this.consecutiveFails = 0;
        this.lastFailedPos = null;
        this.bridgePlaceCooldown = 0;
        this.isAligning = false;
        this.lastYawTarget = Double.NaN;
        this.blockFilter.reset();
    }

    public Set<BlockPos> placedCells() {
        return this.blockFilter.placedCells();
    }

    public void forgetPlaced() {
        this.blockFilter.forgetPlaced();
    }

    public boolean isClimbing() {
        return this.isAscending;
    }

    public boolean isBridging() {
        return this.targetFacing != null;
    }

    public void stopBridging() {
        this.targetFacing = null;
        this.scaffoldStage = 0;
        this.consecutiveFails = 0;
        this.bridgePlaceCooldown = 0;
        this.bridgeCooldown = 25;
        AutoMineMovementHelper.preventJump = false;
        AutoMineMovementHelper.sneakLockTicks = 0;
        this.movementHelper.stop();
    }

    public boolean isBreakingCeiling() {
        return this.isDescending;
    }

    public boolean pillarUp(ClientPlayerEntity player) {
        return navigateTowardsTarget(player);
    }

    public boolean navigateTowardsTarget(ClientPlayerEntity player) {
        if (!this.config.allowPlace) {
            return false;
        }
        if (this.isDescending) {
            return followPath(player);
        }
        if (this.blockFilter.failedHops() > 3) {
            if (tryUnstuck(player)) {
                return followPath(player);
            }
            this.isAscending = false;
            return false;
        }
        this.blockBreaker.cancel();
        if (alignToCellCenter(player)) {
            return true;
        }
        boolean[] zArr = {false};
        if (!this.blockFilter.tick(player, zArr)) {
            this.isAscending = false;
            return false;
        }
        if (this.blockFilter.failedHops() == 0) {
            this.faceEvaluationScore = 0;
        }
        this.isAscending = true;
        this.movementHelper.set(0.0f, 0.0f, zArr[0], false);
        return true;
    }

    public boolean tryUnstuck(ClientPlayerEntity player) {
        ClientWorld world = this.client.world;
        if (world == null || this.faceEvaluationScore >= 6) {
            return false;
        }
        BlockPos downPos = player.getBlockPos().up(2);
        if (AutoMineBlockHelper.passable(world, downPos) || !AutoMineBlockHelper.isBreakable(world, downPos) || !this.selection.contains(downPos, 1)) {
            return false;
        }
        this.isDescending = true;
        this.activeBreakPos = downPos.toImmutable();
        this.breakDurationTicks = 0;
        this.faceEvaluationScore++;
        return true;
    }

    public boolean followPath(ClientPlayerEntity player) {
        ClientWorld world = this.client.world;
        if (world == null || this.activeBreakPos == null) {
            return breakObstaclesOnPath(false);
        }
        this.isAscending = true;
        this.movementHelper.stop();
        if (AutoMineBlockHelper.passable(world, this.activeBreakPos)) {
            this.blockFilter.reset();
            return breakObstaclesOnPath(true);
        }
        int i = this.breakDurationTicks + 1;
        this.breakDurationTicks = i;
        if (i > 200 || !this.blockBreaker.tickArmed(this.activeBreakPos, getEffectiveReach())) {
            return breakObstaclesOnPath(false);
        }
        return true;
    }

    public boolean breakObstaclesOnPath(boolean z) {
        this.isDescending = false;
        this.activeBreakPos = null;
        this.breakDurationTicks = 0;
        this.blockBreaker.cancel();
        if (!z) {
            this.isAscending = false;
        }
        return z;
    }

    public boolean alignToCellCenter(ClientPlayerEntity player) {
        if (!player.isOnGround()) {
            return false;
        }
        BlockPos playerPos = player.getBlockPos();
        double deltaX = (((double) playerPos.getX()) + 0.5d) - player.getX();
        double deltaZ = (((double) playerPos.getZ()) + 0.5d) - player.getZ();
        if ((Math.abs(deltaX) <= 0.15d && Math.abs(deltaZ) <= 0.15d) || this.retryCount > 40) {
            this.retryCount = 0;
            return false;
        }
        this.retryCount++;
        this.isAscending = true;
        steerTowards(player, deltaX, deltaZ);
        return true;
    }

    public AutoMinePathNode moveTo(BlockPos pos) {
        AutoMinePathNode pathNode1;
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;
        if (player == null || world == null) {
            return AutoMinePathNode.BLOCKED;
        }
        if (this.isDescending) {
            return followPath(player) ? AutoMinePathNode.MOVING : AutoMinePathNode.BLOCKED;
        }
        BlockPos playerPos = player.getBlockPos();
        if (this.isAscending && playerPos.getY() >= pos.getY() && player.isOnGround()) {
            this.isAscending = false;
        }
        if (this.isAscending) {
            if (navigateTowardsTarget(player)) {
                return AutoMinePathNode.MOVING;
            }
            return AutoMinePathNode.BLOCKED;
        }
        if (playerPos.equals(pos)) {
            stopBridging();
            if (!AutoMineBlockHelper.walkableOn(world, pos.down()) && this.config.allowPlace) {
                this.movementHelper.set(0.0f, 0.0f, false, false, true);
                this.blockFilter.fillCell(player, pos.down(), getEffectiveReach());
                return AutoMinePathNode.MOVING;
            }
            if (this.scaffoldTarget != null && this.scaffoldTarget.equals(pos)) {
                if (distanceSqToBlock(player, pos) <= 0.2025d) {
                    this.movementHelper.stop();
                    return AutoMinePathNode.ARRIVED;
                }
                this.scaffoldTarget = null;
            }
            if (distanceSqToBlock(player, pos) > 0.16d) {
                int i = this.jumpCooldown + 1;
                this.jumpCooldown = i;
                if (i <= 6) {
                    this.blockBreaker.cancel();
                    steerTowards(player, (((double) pos.getX()) + 0.5d) - player.getX(), (((double) pos.getZ()) + 0.5d) - player.getZ(), false);
                    return AutoMinePathNode.MOVING;
                }
            }
            this.movementHelper.stop();
            reset();
            this.scaffoldTarget = pos.toImmutable();
            return AutoMinePathNode.ARRIVED;
        }
        if (this.targetFacing != null && (pathNode1 = evaluateStepNode(player, world, playerPos, pos)) != null) {
            return pathNode1;
        }
        this.jumpCooldown = 0;
        this.scaffoldTarget = null;
        if (isWithinReach(player, pos)) {
            this.movementHelper.stop();
            reset();
            return AutoMinePathNode.BLOCKED;
        }
        if (canStandOn(player, world, playerPos, pos)) {
            return AutoMinePathNode.MOVING;
        }
        if (this.bridgeCooldown > 0) {
            this.bridgeCooldown--;
        }
        if (this.config.allowPlace && this.bridgeCooldown <= 0 && AutoMineBlockFilter.hasBuildingBlock(player) && Math.abs(playerPos.getY() - pos.getY()) <= 1 && ((playerPos.getX() - pos.getX()) * (playerPos.getX() - pos.getX())) + ((playerPos.getZ() - pos.getZ()) * (playerPos.getZ() - pos.getZ())) <= 49.0d) {
            Direction facingDir = axisDirection(resolveTravelAxis(playerPos, pos), playerPos, pos);
            BlockPos offsetPos = playerPos.offset(facingDir);
            BlockPos upPos = offsetPos.down();
            if (AutoMineBlockHelper.passable(world, offsetPos) && !AutoMineBlockHelper.walkableOn(world, upPos)) {
                this.targetFacing = facingDir;
                this.scaffoldStage = 0;
                this.consecutiveFails = 0;
                this.lastFailedPos = playerPos.toImmutable();
                AutoMinePathNode pathNode2 = evaluateStepNode(player, world, playerPos, pos);
                if (pathNode2 != null) {
                    return pathNode2;
                }
            }
        }
        return findPathTo(player, world, playerPos, pos);
    }

    /* JADX WARN: Code duplicated, block: B:10:0x0036  */
    public boolean canStandOn(ClientPlayerEntity player, World world, BlockPos pos, BlockPos pos2) {
        boolean z;
        List<BlockPos> listFind;
        if (this.pathNodes == null || !pos2.equals(this.currentTargetPos) || this.pathIndex >= this.pathNodes.size()) {
            z = true;
        } else {
            int i = this.stuckTicks + 1;
            this.stuckTicks = i;
            if (i > 40) {
                z = true;
            } else {
                z = false;
            }
        }
        if (z) {
            this.pathNodes = AutoMinePathfinder.find(world, pos, pos2, Math.max(1, 3));
            if (this.pathNodes == null) {
                List<BlockPos> list = null;
                for (Direction direction : HORIZONTAL_DIRECTIONS) {
                    BlockPos pos3 = null;
                    for (int i2 : Y_OFFSETS) {
                        BlockPos downPos = pos2.offset(direction).up(i2);
                        if (AutoMineBlockHelper.standable(world, downPos)) {
                            pos3 = downPos;
                            break;
                        }
                    }
                    if (pos3 != null && (listFind = AutoMinePathfinder.find(world, pos, pos3, Math.max(1, 3))) != null && !listFind.isEmpty() && (list == null || listFind.size() < list.size())) {
                        list = listFind;
                    }
                }
                this.pathNodes = list;
            }
            if (this.pathNodes != null && !this.pathNodes.isEmpty()) {
                if (this.pathNodes.size() > ((Math.abs(pos2.getX() - pos.getX()) + Math.abs(pos2.getY() - pos.getY()) + Math.abs(pos2.getZ() - pos.getZ())) * 4) + 20) {
                    this.pathNodes = null;
                }
            }
            this.currentTargetPos = pos2;
            this.pathIndex = 0;
            this.stuckTicks = 0;
        }
        if (this.pathNodes == null || this.pathNodes.isEmpty()) {
            return false;
        }
        while (this.pathIndex < this.pathNodes.size() && this.pathNodes.get(this.pathIndex).equals(pos)) {
            this.pathIndex++;
        }
        if (this.pathIndex >= this.pathNodes.size()) {
            this.pathNodes = null;
            return false;
        }
        BlockPos pos4 = this.pathNodes.get(this.pathIndex);
        if (Math.abs(pos4.getX() - pos.getX()) > 1 || Math.abs(pos4.getZ() - pos.getZ()) > 1) {
            this.pathNodes = null;
            return false;
        }
        lookAtBlock(player, pos4, this.pathIndex == this.pathNodes.size() - 1, pos4.getY() > pos.getY(), (distanceSqToBlock(player, pos2) > 2.5d ? 1 : (distanceSqToBlock(player, pos2) == 2.5d ? 0 : -1)) > 0);
        return true;
    }

    public void lookAtBlock(ClientPlayerEntity player, BlockPos pos, boolean z, boolean z2, boolean z3) {
        double deltaX = (((double) pos.getX()) + 0.5d) - player.getX();
        double deltaZ = (((double) pos.getZ()) + 0.5d) - player.getZ();
        double d = (deltaX * deltaX) + (deltaZ * deltaZ);
        this.blockBreaker.cancel();
        if (z && d <= 0.48999999999999994d) {
            this.isAligning = false;
            steerTowards(player, deltaX, deltaZ);
            return;
        }
        if ((this.isAligning || d >= 0.5625d) && calculateYawTo(player, deltaX, deltaZ) > 50.0f) {
            this.movementHelper.stop();
            this.strafeDirection = 0;
        } else if (isHeadingTowards(player, Direction.getFacing(deltaX, 0.0d, deltaZ))) {
            this.movementHelper.stop();
        } else {
            this.movementHelper.set(1.0f, 0.0f, z2 && player.isOnGround(), this.config.allowSprint && z3 && !(z && (d > 1.44d ? 1 : (d == 1.44d ? 0 : -1)) <= 0) && !z2);
        }
    }

    public float calculateYawTo(ClientPlayerEntity player, double d, double d2) {
        float fYawTo = AutoMineRotationHelper.yawTo(d, d2);
        float fAbs = Math.abs(MathHelper.wrapDegrees(fYawTo - player.getYaw()));
        if (!this.isAligning) {
            if (fAbs < 8.0f) {
                return 0.0f;
            }
            this.isAligning = true;
        }
        this.isAligning = AutoMineRotationHelper.stepYawTo(player, fYawTo, 12.0f);
        return fAbs;
    }

    public void steerTowards(ClientPlayerEntity player, double d, double d2) {
        steerTowards(player, d, d2, false);
    }

    public void steerTowards(ClientPlayerEntity player, double d, double d2, boolean z) {
        double radians = Math.toRadians(player.getYaw());
        double dSin = Math.sin(radians);
        double dCos = Math.cos(radians);
        this.movementHelper.set(normalizeInput((d2 * dCos) - (d * dSin)), normalizeInput((d2 * dSin) + (d * dCos)), false, false, z);
    }

    public static float normalizeInput(double d) {
        double dAbs = Math.abs(d);
        if (dAbs < 0.08d) {
            return 0.0f;
        }
        if (dAbs < 0.3d) {
            return (float) (d > 0.0d ? 0.35d : -0.35d);
        }
        return d > 0.0d ? 1.0f : -1.0f;
    }

    public boolean isHeadingTowards(ClientPlayerEntity player, Direction direction) {
        return false;
    }

    public AutoMinePathNode evaluateStepNode(ClientPlayerEntity player, World world, BlockPos pos, BlockPos pos2) {
        Direction direction = this.targetFacing;
        if (direction == null) {
            return null;
        }
        if (this.bridgePlaceCooldown > 0) {
            this.bridgePlaceCooldown--;
        }
        if (pos.equals(pos2)) {
            stopBridging();
            return AutoMinePathNode.ARRIVED;
        }
        double deltaX = (((double) pos2.getX()) + 0.5d) - player.getX();
        double deltaZ = (((double) pos2.getZ()) + 0.5d) - player.getZ();
        if ((deltaX * deltaX) + (deltaZ * deltaZ) <= 0.49d && Math.abs(player.getY() - ((double) pos2.getY())) < 0.6d) {
            stopBridging();
            return AutoMinePathNode.ARRIVED;
        }
        BlockPos upPos = pos.down();
        BlockPos offsetPos = pos.offset(direction);
        BlockPos southPos = offsetPos.down();
        boolean zWalkableOn = AutoMineBlockHelper.walkableOn(world, upPos);
        boolean zWalkableOn2 = AutoMineBlockHelper.walkableOn(world, southPos);
        if (zWalkableOn && zWalkableOn2 && !this.blockFilter.placedCells().contains(southPos)) {
            stopBridging();
            player.setYaw(AutoMineRotationHelper.yawTo(direction.getOffsetX(), direction.getOffsetZ()));
            player.setPitch(15.0f);
            return AutoMinePathNode.MOVING;
        }
        if (!pos.equals(this.lastFailedPos)) {
            this.lastFailedPos = pos.toImmutable();
            this.scaffoldStage = 0;
        }
        int i = this.scaffoldStage + 1;
        this.scaffoldStage = i;
        if (i > 140) {
            stopBridging();
            return AutoMinePathNode.BLOCKED;
        }
        int iFindBuildingBlock = AutoMineBlockFilter.findBuildingBlock(player);
        if (iFindBuildingBlock < 0) {
            stopBridging();
            return AutoMinePathNode.BLOCKED;
        }
        if (player.getInventory().getSelectedSlot() != iFindBuildingBlock) {
            player.getInventory().setSelectedSlot(iFindBuildingBlock);
        }
        if (!zWalkableOn) {
            this.blockBreaker.cancel();
            this.movementHelper.set(0.0f, 0.0f, false, false, true);
            AutoMineMovementHelper.preventJump = true;
            AutoMineMovementHelper.sneakLockTicks = 6;
            if (this.bridgePlaceCooldown <= 0 && this.blockFilter.fillCell(player, upPos, getEffectiveReach())) {
                this.consecutiveFails = 0;
                this.bridgePlaceCooldown = 4;
            } else {
                int i2 = this.consecutiveFails + 1;
                this.consecutiveFails = i2;
                if (i2 > 40) {
                    stopBridging();
                    return AutoMinePathNode.BLOCKED;
                }
            }
            return AutoMinePathNode.MOVING;
        }
        if (!AutoMineBlockHelper.passable(world, offsetPos) || !AutoMineBlockHelper.passable(world, offsetPos.up())) {
            stopBridging();
            player.setYaw(AutoMineRotationHelper.yawTo(direction.getOffsetX(), direction.getOffsetZ()));
            player.setPitch(15.0f);
            return AutoMinePathNode.MOVING;
        }
        double deltaY = ((double) upPos.getX()) + 0.5d;
        double valX = ((double) upPos.getZ()) + 0.5d;
        AutoMineRotationHelper.stepYawTo(player, AutoMineRotationHelper.yawTo(-direction.getOffsetX(), -direction.getOffsetZ()) - MathHelper.clamp((float) (((((double) direction.getOffsetX()) * (player.getZ() - valX)) - (((double) direction.getOffsetZ()) * (player.getX() - deltaY))) * 25.0d), -6.0f, 6.0f), 25.0f);
        float currentPitch = player.getPitch();
        player.setPitch(currentPitch + MathHelper.clamp(78.0f - currentPitch, -15.0f, 15.0f));
        AutoMineMovementHelper.preventJump = true;
        AutoMineMovementHelper.sneakLockTicks = Math.max(AutoMineMovementHelper.sneakLockTicks, 4);
        this.movementHelper.set(-1.0f, 0.0f, false, false, true);
        if (zWalkableOn2) {
            this.consecutiveFails = 0;
            return AutoMinePathNode.MOVING;
        }
        if (((player.getX() - deltaY) * ((double) direction.getOffsetX())) + ((player.getZ() - valX) * ((double) direction.getOffsetZ())) < 0.2d) {
            return AutoMinePathNode.MOVING;
        }
        if (this.bridgePlaceCooldown > 0) {
            return AutoMinePathNode.MOVING;
        }
        HitResult blockHitResult = this.client.crosshairTarget;
        boolean z = false;
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hitResult2 = (BlockHitResult) blockHitResult;
            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos hitBlockPos = hitResult2.getBlockPos();
                Direction sideDir = hitResult2.getSide();
                if ((hitBlockPos.offset(sideDir).equals(southPos) || (hitBlockPos.equals(upPos) && sideDir == direction)) && this.client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult2).isAccepted()) {
                    player.swingHand(Hand.MAIN_HAND);
                    z = true;
                    this.bridgePlaceCooldown = 4;
                    this.blockFilter.rememberPlaced(southPos);
                }
            }
        }
        if (!z && this.blockFilter.fillCell(player, southPos, getEffectiveReach())) {
            z = true;
            this.bridgePlaceCooldown = 4;
        }
        if (z) {
            this.consecutiveFails = 0;
            this.scaffoldStage = 0;
        } else {
            int i3 = this.consecutiveFails + 1;
            this.consecutiveFails = i3;
            if (i3 > 60) {
                stopBridging();
                return AutoMinePathNode.BLOCKED;
            }
        }
        return AutoMinePathNode.MOVING;
    }

    public AutoMinePathNode findPathTo(ClientPlayerEntity player, World world, BlockPos pos, BlockPos pos2) {
        boolean z = pos.getX() == pos2.getX() && pos.getZ() == pos2.getZ();
        if (z && pos.getY() < pos2.getY() && navigateTowardsTarget(player)) {
            return AutoMinePathNode.MOVING;
        }
        if (z && pos.getY() > pos2.getY()) {
            this.movementHelper.stop();
            BlockPos upPos = pos.down();
            return AutoMineBlockHelper.passable(world, upPos) ? AutoMinePathNode.MOVING : determinePassability(upPos);
        }
        if (!z) {
            Direction facingDir = axisDirection(resolveTravelAxis(pos, pos2), pos, pos2);
            BlockPos offsetPos = pos.offset(facingDir);
            BlockPos pos3 = new BlockPos(offsetPos.getX(), this.lastClearTick != Integer.MIN_VALUE ? this.lastClearTick : pos.getY() + 1, offsetPos.getZ());
            BlockPos pos4 = new BlockPos(offsetPos.getX(), pos.getY() + 1, offsetPos.getZ());
            boolean z2 = !AutoMineBlockHelper.passable(world, pos3);
            boolean z3 = !AutoMineBlockHelper.passable(world, pos4);
            if (z2 || z3) {
                boolean z4 = Math.abs(MathHelper.wrapDegrees(AutoMineRotationHelper.yawTo((double) facingDir.getOffsetX(), (double) facingDir.getOffsetZ()) - player.getYaw())) <= 40.0f;
                this.movementHelper.set(z4 ? 1.0f : 0.0f, 0.0f, false, z4 && this.config.allowSprint && (distanceSqToBlock(player, pos2) > 2.5d ? 1 : (distanceSqToBlock(player, pos2) == 2.5d ? 0 : -1)) > 0);
                AutoMinePathNode nearestPathNode = (!(z2 && this.blockBreaker.canReach(pos3, getEffectiveReach())) && z3) ? determinePassability(pos4) : determinePassability(pos3);
                AutoMinePathNode autoMinePathNode = nearestPathNode;
                if (autoMinePathNode == AutoMinePathNode.BLOCKED) {
                    this.movementHelper.stop();
                }
                return autoMinePathNode;
            }
            this.blockBreaker.cancel();
            if (calculateYawTo(player, facingDir.getOffsetX(), facingDir.getOffsetZ()) > 50.0f) {
                this.movementHelper.stop();
                this.strafeDirection = 0;
                return AutoMinePathNode.MOVING;
            }
            if (isHeadingTowards(player, facingDir)) {
                this.movementHelper.stop();
                return AutoMinePathNode.MOVING;
            }
            if (!AutoMineBlockHelper.walkableOn(world, offsetPos.down())) {
                if (this.config.allowPlace && this.bridgeCooldown <= 0 && AutoMineBlockFilter.hasBuildingBlock(player)) {
                    this.targetFacing = facingDir;
                    this.scaffoldStage = 0;
                    this.consecutiveFails = 0;
                    this.lastFailedPos = pos.toImmutable();
                    AutoMinePathNode pathNode1 = evaluateStepNode(player, world, pos, pos2);
                    if (pathNode1 != null) {
                        return pathNode1;
                    }
                }
                this.movementHelper.stop();
                return AutoMinePathNode.BLOCKED;
            }
            boolean stepUp = !AutoMineBlockHelper.passable(world, offsetPos) && AutoMineBlockHelper.passable(world, pos4);
            boolean shouldJump = (stepUp || (player.horizontalCollision && player.isOnGround()));
            this.movementHelper.set(1.0f, 0.0f, shouldJump && player.isOnGround(), this.config.allowSprint && (distanceSqToBlock(player, pos2) > 2.5d ? 1 : (distanceSqToBlock(player, pos2) == 2.5d ? 0 : -1)) > 0);
            return AutoMinePathNode.MOVING;
        }
        this.primaryTravelAxis = null;
        if (pos.getY() < pos2.getY()) {
            if (this.blockBreaker.canReach(pos2, this.config.reachDistance)) {
                this.movementHelper.stop();
                return AutoMinePathNode.ARRIVED;
            }
            double deltaX = (((double) pos2.getX()) + 0.5d) - player.getX();
            double deltaZ = (((double) pos2.getZ()) + 0.5d) - player.getZ();
            if ((deltaX * deltaX) + (deltaZ * deltaZ) > 0.64d) {
                steerTowards(player, deltaX, deltaZ, false);
                return AutoMinePathNode.MOVING;
            }
            if (pos2.getY() - pos.getY() > 4 && navigateTowardsTarget(player)) {
                return AutoMinePathNode.MOVING;
            }
            this.isAscending = false;
            return AutoMinePathNode.BLOCKED;
        }
        this.movementHelper.stop();
        return AutoMinePathNode.MOVING;
    }

    public void setLayerFloor(int i) {
        this.lastEvaluatedTick = i;
    }

    public void setDigAimY(int i) {
        this.lastClearTick = i;
    }

    public void setReach(double d) {
        this.targetPosX = Math.max(this.config.reachDistance, d);
    }

    public double getEffectiveReach() {
        return this.targetPosX > 0.0d ? this.targetPosX : this.config.reachDistance;
    }

    public AutoMinePathNode determinePassability(BlockPos pos) {
        ClientWorld world = this.client.world;
        if (!this.selection.contains(pos, 1)) {
            return AutoMinePathNode.BLOCKED;
        }
        if (!AutoMineBlockHelper.isBreakable(world, pos)) {
            return AutoMinePathNode.BLOCKED;
        }
        if (isSafeToMine(pos)) {
            if (!this.blockBreaker.tickArmed(pos, getEffectiveReach())) {
                return AutoMinePathNode.BLOCKED;
            }
            return AutoMinePathNode.MOVING;
        }
        if (!this.blockBreaker.aimOnly(pos, getEffectiveReach())) {
            return AutoMinePathNode.BLOCKED;
        }
        return AutoMinePathNode.MOVING;
    }

    /* JADX WARN: Code duplicated, block: B:9:0x002d  */
    public boolean isSafeToMine(BlockPos pos) {
        return AutoMineBlockHelper.isAimingAt(this.client, pos, getEffectiveReach());
    }

    /* JADX WARN: Code duplicated, block: B:15:0x006e  */
    public boolean isWithinReach(ClientPlayerEntity player, BlockPos pos) {
        int i;
        double distToTarget = distanceSqToBlock(player, pos) + ((double) Math.abs(player.getBlockY() - pos.getY()));
        BlockPos aimingPos = this.blockBreaker.aiming();
        if (aimingPos != null && !aimingPos.equals(this.lastPlayerPos)) {
            this.repathTicks = 0;
        }
        this.lastPlayerPos = aimingPos;
        if (this.bestPathDistance - distToTarget > 0.0015d) {
            this.alignTicks = 0;
            this.repathTicks = 0;
        } else {
            if (aimingPos != null) {
                int i2 = this.repathTicks + 1;
                this.repathTicks = i2;
                if (i2 <= 600) {
                    i = 0;
                } else {
                    int i3 = this.alignTicks + 1;
                    i = i3;
                    this.alignTicks = i3;
                }
            } else {
                int i4 = this.alignTicks + 1;
                i = i4;
                this.alignTicks = i4;
            }
            this.alignTicks = i;
        }
        this.bestPathDistance = distToTarget;
        return this.alignTicks > 50;
    }

    public static double distanceSqToBlock(ClientPlayerEntity player, BlockPos pos) {
        double deltaX = (((double) pos.getX()) + 0.5d) - player.getX();
        double deltaZ = (((double) pos.getZ()) + 0.5d) - player.getZ();
        return (deltaX * deltaX) + (deltaZ * deltaZ);
    }

    public Direction.Axis resolveTravelAxis(BlockPos pos, BlockPos pos2) {
        if (this.primaryTravelAxis != null && axisDistance(this.primaryTravelAxis, pos, pos2) != 0) {
            return this.primaryTravelAxis;
        }
        this.primaryTravelAxis = Math.abs(pos2.getX() - pos.getX()) >= Math.abs(pos2.getZ() - pos.getZ()) ? Direction.Axis.X : Direction.Axis.Z;
        return this.primaryTravelAxis;
    }

    public static int axisDistance(Direction.Axis axis, BlockPos pos, BlockPos pos2) {
        return axis == Direction.Axis.X ? pos2.getX() - pos.getX() : pos2.getZ() - pos.getZ();
    }

    public static Direction axisDirection(Direction.Axis axis, BlockPos pos, BlockPos pos2) {
        int axisDist = axisDistance(axis, pos, pos2);
        if (axis == Direction.Axis.X) {
            return axisDist >= 0 ? Direction.EAST : Direction.WEST;
        }
        return axisDist >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
