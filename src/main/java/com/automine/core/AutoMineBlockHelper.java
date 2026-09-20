package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;

/* JADX INFO: loaded from: AutoMineBlockHelper.class */
@Environment(EnvType.CLIENT)
public final class AutoMineBlockHelper {

    public static boolean isLava(BlockState blockState) {
        return blockState.getFluidState().isIn(FluidTags.LAVA);
    }

    public static boolean isLava(World world, BlockPos pos) {
        return isLava(world.getBlockState(pos));
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean lavaAdjacent(World world, BlockPos pos) {
        Direction[] allDirections = Direction.values();
        int length = allDirections.length;
        for (int i = 0; i < length; i++) {
            if (isLava(world, pos.offset(allDirections[i]))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWater(World world, BlockPos pos) {
        return world.getBlockState(pos).getFluidState().isIn(FluidTags.WATER);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean isBreakable(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
            return false;
        }
        return blockState.getHardness(world, pos) >= 0.0f ? true : false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean passable(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return (isLava(blockState) || !blockState.getCollisionShape(world, pos).isEmpty()) ? false : true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean walkableOn(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return (isLava(blockState) || !blockState.getFluidState().isEmpty()) ? false : Block.isFaceFullSquare(blockState.getCollisionShape(world, pos), Direction.UP);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean standable(World world, BlockPos pos) {
        return (walkableOn(world, pos.down()) && passable(world, pos) && passable(world, pos.up())) ? true : false;
    }

    public static Vec3d faceCenter(BlockPos pos, Direction direction) {
        return Vec3d.ofCenter(pos).add(((double) direction.getOffsetX()) * 0.5d, ((double) direction.getOffsetY()) * 0.5d, ((double) direction.getOffsetZ()) * 0.5d);
    }

    public static Direction exposedFaceToward(BlockView blockView, BlockPos pos, Vec3d posVec) {
        Vec3d raycastVec = Vec3d.ofCenter(pos);
        double d = posVec.x - raycastVec.x;
        double d2 = posVec.y - raycastVec.y;
        double d3 = posVec.z - raycastVec.z;
        Direction direction = null;
        double d4 = -1.7976931348623157E308d;
        Direction direction2 = Direction.UP;
        double d5 = -1.7976931348623157E308d;
        Direction[] allDirections = Direction.values();
        int length = allDirections.length;
        for (int i = 0; i < length; i++) {
            Direction direction3 = allDirections[i];
            double viewAngle = (((double) direction3.getOffsetX()) * d) + (((double) direction3.getOffsetY()) * d2) + (((double) direction3.getOffsetZ()) * d3);
            if (viewAngle > d5) {
                d5 = viewAngle;
                direction2 = direction3;
            }
            BlockState blockState = blockView.getBlockState(pos.offset(direction3));
            if ((blockState.isAir() || !blockState.isOpaqueFullCube()) && viewAngle > d4) {
                d4 = viewAngle;
                direction = direction3;
            }
        }
        return direction != null ? direction : direction2;
    }

    public static boolean isAimingAt(MinecraftClient client, BlockPos pos, double maxReach) {
        if (client == null || pos == null) {
            return false;
        }
        HitResult blockHitResult = client.crosshairTarget;
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hitResult = (BlockHitResult) blockHitResult;
            if (hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(pos)) {
                return true;
            }
        }
        if (client.player != null && client.world != null) {
            Vec3d eye = client.player.getEyePos();
            Vec3d rot = client.player.getRotationVec(1.0f);
            double reach = maxReach > 0 ? maxReach : Math.max(4.5d, client.player.getBlockInteractionRange());
            Vec3d end = eye.add(rot.multiply(reach));
            BlockHitResult bhr = client.world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(pos)) {
                return true;
            }
            Vec3d center = Vec3d.ofCenter(pos);
            if (eye.squaredDistanceTo(center) <= reach * reach) {
                Vec3d toCenter = center.subtract(eye).normalize();
                if (rot.dotProduct(toCenter) > 0.985d) {
                    return true;
                }
            }
        }
        return false;
    }
}
