package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineRotationHelper.class */
@Environment(EnvType.CLIENT)
public final class AutoMineRotationHelper {
    private static Vec3d smoothedAimPoint = null;

    public static boolean stepYawTo(ClientPlayerEntity player, float f, float f2) {
        if (player == null) {
            return false;
        }
        float currentYaw = player.getYaw();
        float yawDiff = MathHelper.wrapDegrees(f - currentYaw);
        float fAbs = Math.abs(yawDiff);
        if (fAbs <= 0.3f) {
            return false;
        }
        float fMin = Math.min(1.0f, fAbs / 45.0f);
        float fMax = Math.max(3.0f, Math.min(20.0f, f2 * (0.25f + (0.75f * fMin * (2.0f - fMin)))));
        player.setYaw(currentYaw + MathHelper.clamp(yawDiff * 0.35f, -fMax, fMax));
        return fAbs > fMax;
    }

    public static boolean turnTo(ClientPlayerEntity player, Vec3d posVec, float f) {
        if (player == null || posVec == null) {
            return false;
        }
        if (smoothedAimPoint == null || smoothedAimPoint.squaredDistanceTo(posVec) > 9.0d) {
            smoothedAimPoint = posVec;
        }
        smoothedAimPoint = new Vec3d(smoothedAimPoint.x + ((posVec.x - smoothedAimPoint.x) * 0.35d), smoothedAimPoint.y + ((posVec.y - smoothedAimPoint.y) * 0.35d), smoothedAimPoint.z + ((posVec.z - smoothedAimPoint.z) * 0.35d));
        Vec3d playerPosVec = player.getEyePos();
        double d = smoothedAimPoint.x - playerPosVec.x;
        double d2 = smoothedAimPoint.y - playerPosVec.y;
        double d3 = smoothedAimPoint.z - playerPosVec.z;
        double dSqrt = Math.sqrt((d * d) + (d3 * d3));
        float degrees = (float) (Math.toDegrees(Math.atan2(d3, d)) - 90.0d);
        float targetPitch = MathHelper.clamp((float) (-Math.toDegrees(Math.atan2(d2, dSqrt))), -90.0f, 90.0f);
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();
        float yawDiff = MathHelper.wrapDegrees(degrees - currentYaw);
        float f2 = targetPitch - currentPitch;
        float fAbs = Math.abs(yawDiff);
        float fAbs2 = Math.abs(f2);
        if (fAbs <= 0.6f && fAbs2 <= 0.6f) {
            return true;
        }
        float fMin = Math.min(1.0f, fAbs / 35.0f);
        float fMax = Math.max(2.5f, Math.min(22.0f, f * (0.25f + (0.75f * fMin * fMin * (3.0f - (2.0f * fMin))))));
        float fMin2 = Math.min(1.0f, fAbs2 / 25.0f);
        float fMax2 = Math.max(2.0f, Math.min(16.0f, f * (0.25f + (0.75f * fMin2 * fMin2 * (3.0f - (2.0f * fMin2))))));
        float yawStep = MathHelper.clamp(yawDiff * 0.35f, -fMax, fMax);
        float pitchStep = MathHelper.clamp(f2 * 0.35f, -fMax2, fMax2);
        player.setYaw(currentYaw + yawStep);
        player.setPitch(MathHelper.clamp(currentPitch + pitchStep, -90.0f, 90.0f));
        return fAbs < 3.5f && fAbs2 < 3.5f;
    }

    private AutoMineRotationHelper() {
    }

    public static float yawTo(double d, double d2) {
        return (float) (Math.toDegrees(Math.atan2(d2, d)) - 90.0d);
    }
}
