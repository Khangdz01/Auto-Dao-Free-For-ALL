package com.automine;

import com.automine.core.AutoMineBoxRenderer;
import com.automine.core.AutoMineConfig;
import com.automine.core.AutoMineEngine;
import com.automine.core.AutoMineSelection;
import com.automine.core.AutoMineSpotifyManager;
import com.automine.core.AutoMineStaffDetector;
import com.automine.core.IAutoMineCore;
import com.automine.gui.LicenseScreen;
import com.automine.security.LicenseManager;
import com.automine.security.SecurePayloadLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

/* JADX INFO: loaded from: AutoMineClient.class */
@Environment(EnvType.CLIENT)
public final class AutoMineClient implements ClientModInitializer {
    public static AutoMineConfig CONFIG;
    public static AutoMineSelection SELECTION;
    public static AutoMineEngine ENGINE;
    public static AutoMineSpotifyManager SPOTIFY = new AutoMineSpotifyManager();
    public static String VERSION = "1.0.0";
    private static KeyBinding keyMenu;
    private static KeyBinding keyStart;
    private static KeyBinding keyStop;
    private static KeyBinding keyPos1;
    private static KeyBinding keyPos2;
    private static KeyBinding keyLicense;
    private static KeyBinding keyFreecam;

    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer("autodao").ifPresent(modContainer -> {
            VERSION = "v" + modContainer.getMetadata().getVersion().getFriendlyString();
        });
        LicenseManager.init();
        AutoMineBoxRenderer.register();
        CONFIG = AutoMineConfig.loadOrCreate(FabricLoader.getInstance().getConfigDir());
        keyMenu = key("key.automine.menu", 86); // GLFW_KEY_V
        keyStart = key("key.automine.start", InputUtil.UNKNOWN_KEY.getCode());
        keyStop = key("key.automine.stop", InputUtil.UNKNOWN_KEY.getCode());
        keyPos1 = key("key.automine.pos1", InputUtil.UNKNOWN_KEY.getCode());
        keyPos2 = key("key.automine.pos2", InputUtil.UNKNOWN_KEY.getCode());
        keyLicense = key("key.automine.license", InputUtil.UNKNOWN_KEY.getCode());
        int savedFreecamKey = (CONFIG != null && CONFIG.freecamKey != 0) ? CONFIG.freecamKey : 85;
        keyFreecam = key("key.automine.freecam", savedFreecamKey);
        HudElementRegistry.addLast(Identifier.of(AutoMineStaffDetector.MOD_ID, "status"), (context, blockPredicate) -> {
            IAutoMineCore core = SecurePayloadLoader.getCore();
            if (core != null) {
                core.renderStatusHud(context);
            }
        });
        HudElementRegistry.addLast(Identifier.of(AutoMineStaffDetector.MOD_ID, "staff"), (context2, blockPredicate2) -> {
            IAutoMineCore core = SecurePayloadLoader.getCore();
            if (core != null) {
                core.renderStaffHud(context2);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> {
            com.automine.core.AutoMineFreecam.disable(client);
            IAutoMineCore core = SecurePayloadLoader.getCore();
            if (core != null) {
                core.onDisconnect();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client2 -> {
            if (client2.player == null) {
                return;
            }
            IAutoMineCore core = SecurePayloadLoader.getCore();
            if (LicenseManager.isAuthorized() && core != null) {
                core.init(client2);
            }
            boolean menuFired = false;
            while (keyMenu.wasPressed()) {
                menuFired = true;
                if (LicenseManager.isAuthorized() && core != null) {
                    core.openMenu(client2, null);
                } else {
                    client2.setScreen(new LicenseScreen(null));
                }
            }
            
            while (keyPos1.wasPressed()) {
                if (menuFired || isSameKey(keyPos1, keyMenu)) continue;
                if (LicenseManager.isAuthorized() && core != null) {
                    core.setPos1(client2);
                } else {
                    say(client2, "§cVui lòng kích hoạt bản quyền trước!");
                }
            }
            while (keyPos2.wasPressed()) {
                if (menuFired || isSameKey(keyPos2, keyMenu)) continue;
                if (LicenseManager.isAuthorized() && core != null) {
                    core.setPos2(client2);
                } else {
                    say(client2, "§cVui lòng kích hoạt bản quyền trước!");
                }
            }
            while (keyStart.wasPressed()) {
                if (menuFired || isSameKey(keyStart, keyMenu)) continue;
                if (LicenseManager.isAuthorized() && core != null) {
                    core.startMine(client2);
                } else {
                    say(client2, "§cVui lòng kích hoạt bản quyền trước!");
                }
            }
            while (keyStop.wasPressed()) {
                if (menuFired || isSameKey(keyStop, keyMenu)) continue;
                if (core != null) {
                    core.stopMine(client2);
                }
            }

            boolean freecamTriggered = false;
            while (keyFreecam.wasPressed()) {
                freecamTriggered = true;
            }
            int boundCode = -1;
            try {
                boundCode = InputUtil.fromTranslationKey(keyFreecam.getBoundKeyTranslationKey()).getCode();
            } catch (Throwable ignored) {
            }
            if (boundCode > 0 && client2.currentScreen == null && client2.getWindow() != null) {
                long handle = client2.getWindow().getHandle();
                boolean isDown = handle != 0L && org.lwjgl.glfw.GLFW.glfwGetKey(handle, boundCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (isDown && !freecamKeyDownLast) {
                    freecamTriggered = true;
                }
                freecamKeyDownLast = isDown;
            } else {
                freecamKeyDownLast = false;
            }
            if (freecamTriggered) {
                if (!menuFired && !isSameKey(keyFreecam, keyMenu)) {
                    com.automine.core.AutoMineFreecam.toggle(client2);
                }
            }

            com.automine.core.AutoMineFreecam.onTick(client2);
            if (LicenseManager.isAuthorized() && core != null) {
                core.onTick(client2);
            }
        });
    }

    private static boolean freecamKeyDownLast = false;

    public static KeyBinding getKeyFreecam() {
        return keyFreecam;
    }

    public static void setFreecamKey(int keyCode) {
        if (keyFreecam != null) {
            InputUtil.Key key = (keyCode == InputUtil.UNKNOWN_KEY.getCode() || keyCode <= 0) ?
                InputUtil.UNKNOWN_KEY : InputUtil.Type.KEYSYM.createFromCode(keyCode);
            keyFreecam.setBoundKey(key);
            KeyBinding.updateKeysByCode();
        }
        if (CONFIG != null) {
            CONFIG.freecamKey = keyCode;
            CONFIG.save();
        }
    }

    public static String getFreecamKeyName() {
        if (keyFreecam == null || keyFreecam.isUnbound()) {
            return "Chưa gán";
        }
        return keyFreecam.getBoundKeyLocalizedText().getString();
    }

    public static KeyBinding key(String str, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(str, InputUtil.Type.KEYSYM, defaultKey, KeyBinding.Category.MISC));
    }

    private static boolean isSameKey(KeyBinding a, KeyBinding b) {
        if (a == null || b == null || a.isUnbound() || b.isUnbound()) {
            return false;
        }
        return a.getBoundKeyTranslationKey().equals(b.getBoundKeyTranslationKey());
    }

    public static void say(MinecraftClient client, String str) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§6[Khangdzlaanh] §r" + str), false);
        }
    }
}
