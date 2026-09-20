package com.automine.mixin;

import com.automine.Bridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(value = {MinecraftClient.class})
public abstract class MinecraftClientMixin {

    @Shadow
    private void handleBlockBreaking(boolean breaking) {
    }

    @Shadow
    private int attackCooldown;

    @Unique
    private boolean automine$breakingHandledThisTick = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void automine$resetTickFlag(CallbackInfo ci) {
        this.automine$breakingHandledThisTick = false;
    }

    @ModifyVariable(method = {"handleBlockBreaking"}, at = @At(value = "HEAD"), argsOnly = true)
    public boolean automine$forceBreaking(boolean breaking) {
        return breaking || Bridge.shouldForceBreaking();
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"))
    private void automine$resetAttackCooldown(boolean breaking, CallbackInfo ci) {
        if (Bridge.shouldForceBreaking()) {
            this.attackCooldown = 0;
            this.automine$breakingHandledThisTick = true;
        }
    }

    @Redirect(
        method = "handleBlockBreaking",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;"
        )
    )
    private HitResult automine$redirectCrosshairInBreaking(MinecraftClient self) {
        if (Bridge.shouldForceBreaking()) {
            HitResult target = Bridge.getMiningHitResult();
            if (target != null) {
                return target;
            }
        }
        return self.crosshairTarget;
    }

    @Redirect(method = {"handleInputEvents"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    public boolean automine$holdUseKeyWhileEating(KeyBinding binding) {
        MinecraftClient self = (MinecraftClient) (Object) this;
        if (binding == self.options.useKey && Bridge.isHoldingUseKey(self)) {
            return true;
        }
        return binding.isPressed();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void automine$tickBackgroundMining(CallbackInfo ci) {
        MinecraftClient self = (MinecraftClient) (Object) this;
        if (Bridge.isMiningRunning()) {
            // Keep window from pausing on focus lost
            if (self.options.pauseOnLostFocus) {
                self.options.pauseOnLostFocus = false;
            }
            // Only trigger breaking if handleInputEvents did NOT already execute it this tick
            // (e.g. GUI menu is open or client was in a state where handleInputEvents skipped it).
            // This prevents duplicate 2x handleBlockBreaking calls per tick that trigger server speedmine rejections.
            if (!this.automine$breakingHandledThisTick && Bridge.shouldForceBreaking()) {
                this.attackCooldown = 0;
                this.automine$breakingHandledThisTick = true;
                this.handleBlockBreaking(true);
            }
        }
    }

    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    private void automine$preventPauseOnLostFocus(boolean pauseOnly, CallbackInfo ci) {
        if (pauseOnly && Bridge.isMiningRunning()) {
            ci.cancel();
        }
    }
}
