package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

/* JADX INFO: loaded from: AutoMineBlockBreaker.class */
@Environment(EnvType.CLIENT)
public final class AutoMineBlockBreaker {
    public static final float MAX_TURN_SPEED = 32.0f;
    public static final double AIM_EPSILON_SQ = 1.0E-4d;
    public final MinecraftClient client;
    public BlockPos activeBreakPos;
    public Vec3d activeHitVec;
    public double lastEyeX = Double.NaN;
    public double lastEyeY;
    public double lastEyeZ;
    public BlockPos lastAimPos;
    public Vec3d lastAimHitVec;

    public AutoMineBlockBreaker(MinecraftClient client) {
        this.client = client;
    }

    public boolean aimOnly(BlockPos pos, double d) {
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;
        if (player == null || world == null || world.getBlockState(pos).isAir()) {
            return false;
        }
        Vec3d rotVec1 = (pos.equals(this.lastAimPos) && this.lastAimHitVec != null && hasLineOfSight(player, pos, this.lastAimHitVec, d)) ? this.lastAimHitVec : findVisibleFaceHitVec(player, pos, d);
        if (rotVec1 == null) {
            return false;
        }
        this.lastAimPos = pos.toImmutable();
        this.lastAimHitVec = rotVec1;
        if (!isCrosshairOnBlock(pos)) {
            AutoMineRotationHelper.turnTo(player, rotVec1, 32.0f);
            return true;
        }
        return true;
    }

    /* JADX WARN: Code duplicated, block: B:19:0x00ab  */
    public boolean isCrosshairOnBlock(BlockPos pos) {
        boolean z;
        ClientPlayerEntity player = this.client.player;
        if (player == null) {
            return false;
        }
        Vec3d playerPosVec = player.getEyePos();
        boolean zIsNaN = Double.isNaN(this.lastEyeX);
        double d = playerPosVec.x - this.lastEyeX;
        double d2 = playerPosVec.y - this.lastEyeY;
        double d3 = playerPosVec.z - this.lastEyeZ;
        this.lastEyeX = playerPosVec.x;
        this.lastEyeY = playerPosVec.y;
        this.lastEyeZ = playerPosVec.z;
        if (zIsNaN || (d * d) + (d2 * d2) + (d3 * d3) > 1.0E-4d) {
            return false;
        }
        return AutoMineBlockHelper.isAimingAt(this.client, pos, 4.5d);
    }

    public boolean tickArmed(BlockPos pos, double d) {
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;
        if (player == null || world == null || this.client.interactionManager == null) {
            return false;
        }
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isAir()) {
            cancel();
            return false;
        }
        Vec3d rotVec2 = resolveHitVec(player, pos, d);
        if (rotVec2 == null) {
            if (!pos.equals(this.activeBreakPos)) {
                cancel();
                return false;
            }
            return false;
        }
        if (!pos.equals(this.activeBreakPos)) {
            this.client.interactionManager.cancelBlockBreaking();
            this.activeBreakPos = pos.toImmutable();
        }
        this.activeHitVec = rotVec2;
        AutoMineToolSelector.selectBest(player, blockState);
        if (!isCrosshairOnBlock(pos)) {
            AutoMineRotationHelper.turnTo(player, rotVec2, 32.0f);
            return true;
        }
        return true;
    }

    public boolean canReach(BlockPos pos, double d) {
        ClientPlayerEntity player = this.client.player;
        return player != null && resolveHitVec(player, pos, d) != null;
    }

    public boolean canReachFrom(BlockPos pos, BlockPos pos2, double d) {
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;
        if (player == null || world == null) {
            return false;
        }
        Vec3d posVec = new Vec3d(((double) pos.getX()) + 0.5d, pos.getY() + player.getStandingEyeHeight(), ((double) pos.getZ()) + 0.5d);
        Vec3d raycastVec = Vec3d.ofCenter(pos2);
        if (posVec.squaredDistanceTo(raycastVec) > d * d) {
            return false;
        }
        BlockHitResult raycastHit = world.raycast(new RaycastContext(posVec, raycastVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        return raycastHit.getType() == HitResult.Type.BLOCK && raycastHit.getBlockPos().equals(pos2);
    }

    public Vec3d resolveHitVec(ClientPlayerEntity player, BlockPos pos, double d) {
        if (pos.equals(this.activeBreakPos) && this.activeHitVec != null && hasLineOfSight(player, pos, this.activeHitVec, d)) {
            return this.activeHitVec;
        }
        if (pos.equals(this.lastAimPos) && this.lastAimHitVec != null && hasLineOfSight(player, pos, this.lastAimHitVec, d)) {
            return this.lastAimHitVec;
        }
        return findVisibleFaceHitVec(player, pos, d);
    }

    public Vec3d findVisibleFaceHitVec(ClientPlayerEntity player, BlockPos pos, double d) {
        Vec3d raycastVec = Vec3d.ofCenter(pos);
        if (hasLineOfSight(player, pos, raycastVec, d)) {
            return raycastVec;
        }
        double deltaX = pos.getX();
        double deltaY = pos.getY();
        double deltaZ = pos.getZ();
        if (deltaY <= player.getY() + 0.5d) {
            for (Vec3d posVec : new Vec3d[]{new Vec3d(deltaX + 0.5d, deltaY + 0.9d, deltaZ + 0.5d), new Vec3d(deltaX + 0.3d, deltaY + 0.9d, deltaZ + 0.5d), new Vec3d(deltaX + 0.7d, deltaY + 0.9d, deltaZ + 0.5d), new Vec3d(deltaX + 0.5d, deltaY + 0.9d, deltaZ + 0.3d), new Vec3d(deltaX + 0.5d, deltaY + 0.9d, deltaZ + 0.7d)}) {
                if (hasLineOfSight(player, pos, posVec, d)) {
                    return posVec;
                }
            }
        }
        for (Direction direction : sortDirectionsByFacing(player.getEyePos(), raycastVec)) {
            Vec3d faceCenterVec = AutoMineBlockHelper.faceCenter(pos, direction);
            if (hasLineOfSight(player, pos, faceCenterVec, d)) {
                return faceCenterVec;
            }
        }
        return null;
    }

    public static Direction[] sortDirectionsByFacing(Vec3d posVec, Vec3d targetVec) {
        Direction[] directions = (Direction[]) Direction.values().clone();
        double[] dArr = new double[directions.length];
        for (int i = 0; i < directions.length; i++) {
            dArr[i] = (((double) directions[i].getOffsetX()) * (posVec.x - targetVec.x)) + (((double) directions[i].getOffsetY()) * (posVec.y - targetVec.y)) + (((double) directions[i].getOffsetZ()) * (posVec.z - targetVec.z));
        }
        for (int i2 = 1; i2 < directions.length; i2++) {
            Direction direction = directions[i2];
            double d = dArr[i2];
            int i3 = i2 - 1;
            while (i3 >= 0 && dArr[i3] < d) {
                directions[i3 + 1] = directions[i3];
                dArr[i3 + 1] = dArr[i3];
                i3--;
            }
            directions[i3 + 1] = direction;
            dArr[i3 + 1] = d;
        }
        return directions;
    }

    public boolean hasLineOfSight(ClientPlayerEntity player, BlockPos pos, Vec3d posVec, double d) {
        Vec3d playerPosVec = player.getEyePos();
        if (playerPosVec.squaredDistanceTo(posVec) > d * d) {
            return false;
        }
        BlockHitResult raycastHit = player.getEntityWorld().raycast(new RaycastContext(playerPosVec, posVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        return raycastHit.getType() == HitResult.Type.BLOCK && raycastHit.getBlockPos().equals(pos);
    }

    public void disarm() {
        this.activeBreakPos = null;
        this.activeHitVec = null;
    }

    public void cancel() {
        this.activeBreakPos = null;
        this.activeHitVec = null;
        this.lastAimPos = null;
        this.lastAimHitVec = null;
        if (this.client.interactionManager != null) {
            this.client.interactionManager.cancelBlockBreaking();
        }
    }

    public BlockPos aiming() {
        return this.activeBreakPos;
    }
}
