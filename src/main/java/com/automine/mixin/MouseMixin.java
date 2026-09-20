package com.automine.mixin;

import com.automine.core.AutoMineFreecam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
        )
    )
    private void automine$redirectMouseLook(ClientPlayerEntity player, double dx, double dy) {
        if (AutoMineFreecam.isActive()) {
            AutoMineFreecam.changeLookDirection(dx, dy);
        } else {
            player.changeLookDirection(dx, dy);
        }
    }
}
