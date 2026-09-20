package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class AutoMineFreecam {
    private static boolean active = false;

    private static double x;
    private static double y;
    private static double z;
    private static double prevX;
    private static double prevY;
    private static double prevZ;

    private static float yaw;
    private static float pitch;

    public static double sprintMultiplier = 2.5d;

    public static boolean isActive() {
        return active;
    }

    public static void toggle(MinecraftClient client) {
        if (active) {
            disable(client);
        } else {
            enable(client);
        }
    }

    public static void enable(MinecraftClient client) {
        if (active) return;
        if (client != null && client.player != null) {
            ClientPlayerEntity player = client.player;
            x = prevX = player.getX();
            y = prevY = player.getEyeY();
            z = prevZ = player.getZ();
            yaw = player.getYaw();
            pitch = player.getPitch();
            active = true;
            String keyName = AutoMineClient.getFreecamKeyName();
            AutoMineCoreImpl.say(client, "§aFreecam [BẬT] §7(Phím: §b[" + keyName + "]§7 | WASD: bay, Space: lên, Shift: xuống, Ctrl: tăng tốc)");
        }
    }

    public static void disable(MinecraftClient client) {
        if (!active) return;
        active = false;
        if (client != null && client.player != null) {
            AutoMineCoreImpl.say(client, "§cFreecam [TẮT]");
        }
    }

    public static void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        if (!active) return;
        float f = (float) cursorDeltaY * 0.15f;
        float g = (float) cursorDeltaX * 0.15f;
        pitch = MathHelper.clamp(pitch + f, -90.0f, 90.0f);
        yaw += g;
    }

    private static boolean isInputDown(MinecraftClient client, KeyBinding binding, int fallbackGlfwKey) {
        if (client == null) return false;
        if (binding != null && binding.isPressed()) {
            return true;
        }
        if (client.getWindow() == null) return false;
        long handle = client.getWindow().getHandle();
        if (handle == 0L) return false;

        if (binding != null) {
            try {
                int code = InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey()).getCode();
                if (code > 0 && GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        if (fallbackGlfwKey != -1) {
            try {
                if (GLFW.glfwGetKey(handle, fallbackGlfwKey) == GLFW.GLFW_PRESS) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    public static void onTick(MinecraftClient client) {
        if (!active) return;
        if (client == null || client.player == null) {
            active = false;
            return;
        }

        if (client.player.isDead() || client.player.getHealth() <= 0.0f) {
            disable(client);
            return;
        }

        prevX = x;
        prevY = y;
        prevZ = z;

        if (client.currentScreen != null) {
            return;
        }

        long handle = client.getWindow() != null ? client.getWindow().getHandle() : 0L;

        boolean forward = isInputDown(client, client.options.forwardKey, GLFW.GLFW_KEY_W);
        boolean back = isInputDown(client, client.options.backKey, GLFW.GLFW_KEY_S);
        boolean left = isInputDown(client, client.options.leftKey, GLFW.GLFW_KEY_A);
        boolean right = isInputDown(client, client.options.rightKey, GLFW.GLFW_KEY_D);
        boolean up = isInputDown(client, client.options.jumpKey, GLFW.GLFW_KEY_SPACE);
        boolean down = isInputDown(client, client.options.sneakKey, GLFW.GLFW_KEY_LEFT_SHIFT)
                    || (handle != 0L && GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS);
        boolean sprint = isInputDown(client, client.options.sprintKey, GLFW.GLFW_KEY_LEFT_CONTROL)
                      || (handle != 0L && GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS);

        double cfgSpeed = (AutoMineClient.CONFIG != null && AutoMineClient.CONFIG.freecamSpeed > 0)
                ? AutoMineClient.CONFIG.freecamSpeed : 1.0d;
        double currentSpeed = cfgSpeed * (sprint ? sprintMultiplier : 1.0d) * 0.5d;

        double forwardInput = 0;
        double strafeInput = 0;
        if (forward) forwardInput += 1;
        if (back) forwardInput -= 1;
        if (left) strafeInput -= 1;
        if (right) strafeInput += 1;

        if (forwardInput != 0 && strafeInput != 0) {
            forwardInput *= 0.70710678d;
            strafeInput *= 0.70710678d;
        }

        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        // 3D forward vector along look direction
        Vec3d forwardVec = new Vec3d(
            -Math.sin(radYaw) * Math.cos(radPitch),
            -Math.sin(radPitch),
            Math.cos(radYaw) * Math.cos(radPitch)
        );

        // Horizontal strafe vector (perpendicular to yaw)
        double strafeRad = Math.toRadians(yaw + 90.0d);
        Vec3d strafeVec = new Vec3d(-Math.sin(strafeRad), 0, Math.cos(strafeRad));

        double moveX = (forwardVec.x * forwardInput + strafeVec.x * strafeInput) * currentSpeed;
        double moveY = (forwardVec.y * forwardInput) * currentSpeed;
        double moveZ = (forwardVec.z * forwardInput + strafeVec.z * strafeInput) * currentSpeed;

        if (up) {
            moveY += currentSpeed;
        }
        if (down) {
            moveY -= currentSpeed;
        }

        x += moveX;
        y += moveY;
        z += moveZ;
    }

    public static double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevX, x);
    }

    public static double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevY, y);
    }

    public static double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevZ, z);
    }

    public static float getYaw(float tickDelta) {
        return yaw;
    }

    public static float getPitch(float tickDelta) {
        return pitch;
    }
}
