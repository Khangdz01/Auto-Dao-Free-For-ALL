package com.automine.core;

import com.automine.AutoMineClient;
import com.automine.Bridge;
import com.automine.gui.AutoMineMainScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.Screen;

/* JADX INFO: loaded from: AutoMineCoreImpl.class */
public class AutoMineCoreImpl implements IAutoMineCore {
    public static AutoMineConfig CONFIG;
    public static AutoMineSelection SELECTION;
    public static AutoMineEngine ENGINE;
    public static AutoMineSpotifyManager SPOTIFY;
    private AutoMineConfig config;
    private AutoMineSelection selection;
    private AutoMineEngine engine;
    private AutoMineStatusHud statusHud;
    private boolean initialized = false;

    public static void say(MinecraftClient client, String str) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§6[Khangdzlaanh] §r" + str), false);
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void init() {
        init(MinecraftClient.getInstance());
    }

    @Override // com.automine.core.IAutoMineCore
    public void init(MinecraftClient client) {
        if (this.initialized) {
            return;
        }
        this.config = AutoMineConfig.loadOrCreate(FabricLoader.getInstance().getConfigDir());
        this.selection = new AutoMineSelection();

        // Restore saved selection box area
        if (this.config.selectionPos1 != null && !this.config.selectionPos1.isEmpty()) {
            String[] parts = this.config.selectionPos1.split(",");
            if (parts.length == 3) {
                try {
                    this.selection.pos1 = new BlockPos(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                    );
                } catch (Throwable ignored) {
                }
            }
        }
        if (this.config.selectionPos2 != null && !this.config.selectionPos2.isEmpty()) {
            String[] parts = this.config.selectionPos2.split(",");
            if (parts.length == 3) {
                try {
                    this.selection.pos2 = new BlockPos(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                    );
                } catch (Throwable ignored) {
                }
            }
        }

        // Auto-save selection box area on any change (pos1, pos2, clear)
        this.selection.setOnModified(() -> {
            if (this.config != null) {
                this.config.selectionPos1 = this.selection.pos1 != null ?
                    (this.selection.pos1.getX() + "," + this.selection.pos1.getY() + "," + this.selection.pos1.getZ()) : "";
                this.config.selectionPos2 = this.selection.pos2 != null ?
                    (this.selection.pos2.getX() + "," + this.selection.pos2.getY() + "," + this.selection.pos2.getZ()) : "";
                this.config.save();
            }
        });

        this.engine = new AutoMineEngine(client != null ? client : MinecraftClient.getInstance(), this.config, this.selection);
        this.statusHud = new AutoMineStatusHud();
        CONFIG = this.config;
        AutoMineClient.CONFIG = this.config;
        AutoMineClient.SELECTION = this.selection;
        AutoMineClient.ENGINE = this.engine;
        if (AutoMineClient.SPOTIFY == null) {
            AutoMineClient.SPOTIFY = new AutoMineSpotifyManager();
        }
        SELECTION = this.selection;
        ENGINE = this.engine;
        Bridge.forceBreaking = this::shouldForceBreaking;
        Bridge.targetHitResult = this::getMiningHitResult;
        Bridge.isMiningRunning = () -> this.engine != null && this.engine.state() == AutoMineState.RUNNING;
        Bridge.holdingUseKey = obj -> {
            return AutoMineAutoEat.isHoldingUseKey((MinecraftClient) obj);
        };
        try {
            AutoMineBoxRenderer.register();
            new AutoMineCommands().register();
        } catch (Throwable th2) {
        }
        this.initialized = true;
    }

    @Override // com.automine.core.IAutoMineCore
    public void onTick(MinecraftClient client) {
        if (!this.initialized || this.engine == null) {
            return;
        }
        try {
            AutoMineStaffDetector.tick(client);
            AutoMineUpdateService.tick(client);
            AutoMineWandHandler.tick(client);
        } catch (Throwable th) {
        }
        if (this.engine.state() == AutoMineState.RUNNING) {
            if (AutoMineAutoEat.checkAndEat(client)) {
                this.engine.pauseForEating();
            }
        } else if (this.engine.state() == AutoMineState.PAUSED && this.engine.isAutoPaused() && !AutoMineAutoEat.checkAndEat(client)) {
            this.engine.resumeFromEating();
        }
        this.engine.tick();
    }

    @Override // com.automine.core.IAutoMineCore
    public boolean shouldForceBreaking() {
        BlockPos breakingPos;
        if (!this.initialized || this.engine == null || this.engine.state() != AutoMineState.RUNNING || (breakingPos = this.engine.breakingTarget()) == null) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        double reach = this.config != null ? Math.max(this.config.reachDistance, 4.5d) : 4.5d;
        return AutoMineBlockHelper.isAimingAt(client, breakingPos, reach);
    }

    public HitResult getMiningHitResult() {
        BlockPos breakingPos;
        if (!this.initialized || this.engine == null || this.engine.state() != AutoMineState.RUNNING || (breakingPos = this.engine.breakingTarget()) == null) {
            return null;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return null;
        }
        if (client.crosshairTarget instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult) client.crosshairTarget;
            if (bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(breakingPos)) {
                return bhr;
            }
        }
        net.minecraft.util.math.Vec3d eye = client.player.getEyePos();
        net.minecraft.util.math.Vec3d rot = client.player.getRotationVec(1.0f);
        double reach = this.config != null ? Math.max(this.config.reachDistance, 4.5d) : 4.5d;
        net.minecraft.util.math.Vec3d end = eye.add(rot.multiply(reach));
        BlockHitResult bhr = client.world.raycast(new net.minecraft.world.RaycastContext(eye, end, net.minecraft.world.RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.NONE, client.player));
        if (bhr != null && bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(breakingPos)) {
            return bhr;
        }
        net.minecraft.util.math.Direction bestFace = net.minecraft.util.math.Direction.UP;
        double bestDot = -Double.MAX_VALUE;
        for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
            net.minecraft.util.math.Vec3d faceNormal = net.minecraft.util.math.Vec3d.of(dir.getVector());
            double dot = faceNormal.dotProduct(rot.multiply(-1.0d));
            if (dot > bestDot) {
                bestDot = dot;
                bestFace = dir;
            }
        }
        return new BlockHitResult(net.minecraft.util.math.Vec3d.ofCenter(breakingPos), bestFace, breakingPos, false);
    }

    @Override // com.automine.core.IAutoMineCore
    public boolean isHoldingUseKey(Object obj) {
        return AutoMineAutoEat.isHoldingUseKey((MinecraftClient) obj);
    }

    @Override // com.automine.core.IAutoMineCore
    public void renderStatusHud(DrawContext context) {
        if (this.statusHud != null) {
            this.statusHud.render(context);
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void renderStaffHud(DrawContext context) {
        AutoMineStaffDetector.renderHud(context);
    }

    @Override // com.automine.core.IAutoMineCore
    public void renderBoxes() {
    }

    @Override // com.automine.core.IAutoMineCore
    public void openMenu(MinecraftClient client, Screen parentScreen) {
        if (client != null) {
            client.setScreen(createMainScreen(parentScreen));
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void setPos1(MinecraftClient client) {
        if (client != null && client.player != null && this.selection != null) {
            this.selection.setPos1(client.player.getBlockPos());
            say(client, "§aĐiểm 1 = " + client.player.getBlockPos().toShortString());
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void setPos2(MinecraftClient client) {
        if (client != null && client.player != null && this.selection != null) {
            this.selection.setPos2(client.player.getBlockPos());
            say(client, "§aĐiểm 2 = " + client.player.getBlockPos().toShortString());
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void startMine(MinecraftClient client) {
        if (this.engine != null) {
            String strStart = this.engine.start();
            say(client, strStart != null ? "§c" + strStart : "§aBắt đầu đào!");
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public void stopMine(MinecraftClient client) {
        if (this.engine != null) {
            this.engine.stop();
            say(client, "§6Đã dừng đào!");
        }
    }

    @Override // com.automine.core.IAutoMineCore
    public Screen createMainScreen(Screen parentScreen) {
        return new AutoMineMainScreen(parentScreen);
    }

    @Override // com.automine.core.IAutoMineCore
    public void onDisconnect() {
        if (this.engine != null) {
            this.engine.stop();
        }
        if (this.config != null) {
            this.config.save();
        }
        AutoMineFreecam.disable(null);
        this.initialized = false;
    }
}
