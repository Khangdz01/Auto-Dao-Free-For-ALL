package com.automine.mixin;

import com.automine.core.AutoMineFreecam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    private boolean thirdPerson;

    @Inject(method = "update", at = @At("TAIL"))
    private void automine$updateFreecam(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (AutoMineFreecam.isActive()) {
            this.thirdPerson = true;
            this.setRotation(AutoMineFreecam.getYaw(tickDelta), AutoMineFreecam.getPitch(tickDelta));
            this.setPos(AutoMineFreecam.getX(tickDelta), AutoMineFreecam.getY(tickDelta), AutoMineFreecam.getZ(tickDelta));
        }
    }

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    private void automine$forceThirdPersonInFreecam(CallbackInfoReturnable<Boolean> cir) {
        if (AutoMineFreecam.isActive()) {
            cir.setReturnValue(true);
        }
    }
}
