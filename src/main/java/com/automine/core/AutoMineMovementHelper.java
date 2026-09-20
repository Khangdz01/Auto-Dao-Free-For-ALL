package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PlayerInput;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineMovementHelper.class */
@Environment(EnvType.CLIENT)
public final class AutoMineMovementHelper extends Input {
    public float forwardInput;
    public float sidewaysInput;
    public boolean jumping;
    public boolean sprinting;
    public boolean sneaking;
    public static int sneakLockTicks = 0;
    public static boolean preventJump = false;

    public void set(float f, float f2, boolean z, boolean z2) {
        set(f, f2, z, z2, false);
    }

    public void set(float f, float f2, boolean z, boolean z2, boolean z3) {
        this.forwardInput = clampInput(f);
        this.sidewaysInput = clampInput(f2);
        this.jumping = z;
        this.sprinting = z2;
        this.sneaking = z3;
    }

    public void stop() {
        this.forwardInput = 0.0f;
        this.sidewaysInput = 0.0f;
        this.jumping = false;
        this.sprinting = false;
        this.sneaking = false;
    }

    public void tick() {
        if (sneakLockTicks > 0) {
            sneakLockTicks--;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client != null ? client.player : null;
        boolean z = false;
        if (player != null) {
            try {
                z = player.isTouchingWater() || player.isInLava() || player.isSubmergedInWater();
                if (!z && client.world != null) {
                    BlockPos playerPos = player.getBlockPos();
                    ClientWorld world = client.world;
                    z = (world.getBlockState(playerPos).getFluidState().isEmpty() && world.getBlockState(playerPos.up()).getFluidState().isEmpty()) ? false : true;
                }
            } catch (Throwable th) {
            }
        }
        boolean z2 = !z && (this.sneaking || sneakLockTicks > 0);
        this.playerInput = new PlayerInput(this.forwardInput > 0.0f, this.forwardInput < 0.0f, this.sidewaysInput > 0.0f, this.sidewaysInput < 0.0f, z || (this.jumping && !preventJump), z2, this.sprinting && !z2);
        float f = (this.sidewaysInput * this.sidewaysInput) + (this.forwardInput * this.forwardInput);
        Vec2f vec2 = new Vec2f(this.sidewaysInput, this.forwardInput);
        if (f > 1.0f) {
            vec2 = vec2.normalize();
        }
        this.movementVector = vec2;
    }

    public static float clampInput(float f) {
        return Math.max(-1.0f, Math.min(1.0f, f));
    }

    public static boolean isNearLedge() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return false;
        }
        ClientPlayerEntity player = client.player;
        World world = client.world;
        if (!player.isOnGround()) {
            return false;
        }
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        int iFloor = (int) Math.floor(playerY - 0.1d);
        for (double[] dArr : new double[][]{new double[]{-0.28d, 0.0d}, new double[]{0.28d, 0.0d}, new double[]{0.0d, -0.28d}, new double[]{0.0d, 0.28d}, new double[]{-0.2d, -0.2d}, new double[]{0.2d, -0.2d}, new double[]{-0.2d, 0.2d}, new double[]{0.2d, 0.2d}}) {
            BlockPos pos = new BlockPos((int) Math.floor(playerX + dArr[0]), iFloor, (int) Math.floor(playerZ + dArr[1]));
            if (!AutoMineBlockHelper.walkableOn(world, pos) && AutoMineBlockHelper.passable(world, pos.up()) && !AutoMineBlockHelper.walkableOn(world, pos.down()) && !AutoMineBlockHelper.walkableOn(world, pos.down(2)) && !AutoMineBlockHelper.walkableOn(world, pos.down(3)) && !AutoMineBlockHelper.isWater(world, pos) && !AutoMineBlockHelper.isWater(world, pos.down()) && !AutoMineBlockHelper.isWater(world, pos.down(2))) {
                return true;
            }
        }
        return false;
    }
}
