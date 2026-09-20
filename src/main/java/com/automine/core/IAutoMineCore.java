package com.automine.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public interface IAutoMineCore {
    void init();

    void init(MinecraftClient client);

    void onTick(MinecraftClient client);

    boolean shouldForceBreaking();

    boolean isHoldingUseKey(Object keyBinding);

    void renderStatusHud(DrawContext context);

    void renderStaffHud(DrawContext context);

    void renderBoxes();

    void openMenu(MinecraftClient client, Screen parent);

    void setPos1(MinecraftClient client);

    void setPos2(MinecraftClient client);

    void startMine(MinecraftClient client);

    void stopMine(MinecraftClient client);

    Screen createMainScreen(Screen parent);

    void onDisconnect();
}

