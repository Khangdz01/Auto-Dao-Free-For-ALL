package com.automine;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.hit.HitResult;

@Environment(value=EnvType.CLIENT)
public final class Bridge {
    public static volatile BooleanSupplier forceBreaking = () -> false;
    public static volatile Predicate<Object> holdingUseKey = mc -> false;
    public static volatile BooleanSupplier isMiningRunning = () -> false;
    public static volatile Supplier<HitResult> targetHitResult = () -> null;

    public static boolean shouldForceBreaking() {
        return forceBreaking.getAsBoolean();
    }

    public static boolean isHoldingUseKey(Object mc) {
        return holdingUseKey.test(mc);
    }

    public static boolean isMiningRunning() {
        return isMiningRunning.getAsBoolean();
    }

    public static HitResult getMiningHitResult() {
        return targetHitResult.get();
    }
}
