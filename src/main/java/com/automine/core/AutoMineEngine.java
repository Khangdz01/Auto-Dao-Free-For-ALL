package com.automine.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import java.util.Locale;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;

/* JADX INFO: loaded from: AutoMineEngine.class */
@Environment(EnvType.CLIENT)
public final class AutoMineEngine {
    public static final int MAX_SELECTION_VOLUME = 4096;
    public static final int STUCK_TIMEOUT_TICKS = 160;
    public static final int IDLE_TIMEOUT_TICKS = 60;
    public static final double MAX_DISTANCE_SQ = 36.0d;
    public static final int REPATH_DELAY_TICKS = 200;
    public static final int MAX_TICK_BUDGET = 900;
    public static final int BREAK_RETRY_LIMIT = 4;
    public static final double MAX_LOOK_DISTANCE_SQ = 100.0d;
    public static final double AIM_TOLERANCE_ANGLE = 40.0d;
    public static final double STEP_EPSILON = 0.22d;
    public static final double HEIGHT_TOLERANCE = 1.4d;
    public static final double MOVE_SPEED_MIN = 0.04d;
    public static final double REACH_MARGIN = 0.7d;
    public static final int MAX_RETRY_COUNT = 3;
    public static final int MAX_LAYER_HEIGHT = 6;
    public static final int EXP_BOTTLE_CHECK_DELAY = 600;
    public static final int MIN_PASS_WIDTH = 1;
    public static final int STATUS_REFRESH_TICKS = 1200;
    public static final int BLOCK_CACHE_TIMEOUT = 600;
    public static final double DEFAULT_REACH = 4.0d;
    public static final double HEAD_CLEARANCE = 1.1d;
    public static final double JUMP_HEIGHT = 1.6d;
    public static final double MAX_FALL_DISTANCE = 40.0d;
    public static final double SNAP_TOLERANCE = 0.18d;
    public static final double INTERACT_REACH = 2.2d;
    public static final int MIN_CLEARANCE = 2;
    public static final double BOX_EXPAND = 0.15d;
    public static final int AUTO_EAT_COOLDOWN = 60;
    public static final int DOUBLE_TAP_TICKS = 2;
    public static final int EXP_REPAIR_DELAY = 30;
    public static final int MAX_UNSTUCK_ATTEMPTS = 5;
    public static final int DEFAULT_PASS_WIDTH = 4;
    public static final int DEFAULT_LAYER_HEIGHT = 20;
    public static final int DIG_TIMEOUT_TICKS = 10;
    public static final int ALIGN_CHECK_TICKS = 4;
    public static final int SCAFFOLD_COOLDOWN = 8;
    public static final int TOOL_CHECK_INTERVAL = 20;
    public final MinecraftClient client;
    public final AutoMineConfig config;
    public final AutoMineBlockBreaker blockBreaker;
    public final AutoMineSelection selection;
    public AutoMineMiningStrategy strategy;
    public AutoMinePlan plan;
    public Input playerInput;
    public boolean hasFluidTask;
    public BlockPos pendingFluidPos;
    public int fluidLockTimer;
    public int fluidClearTimer;
    public boolean isFluidLocked;
    public BlockPos lastFluidPos;
    public int scaffoldWaitTicks;
    public int scaffoldState;
    public int scaffoldTimer;
    public boolean isScaffolding;
    public double lastPlayerX;
    public double lastPlayerY;
    public double lastPlayerZ;
    public BlockPos scaffoldPos;
    public boolean scaffoldPlaced;
    public int eatFoodCooldown;
    public BlockPos eatTargetPos;
    public BlockPos lastPlacedBlockPos;
    public int pathingCooldown;
    public boolean isPathStuck;
    public int stuckCount;
    public BlockPos targetBreakPos;
    public int breakTimeout;
    public boolean isBreakingBlock;
    public int miningTicks;
    public BlockPos currentMiningPos;
    public int unreachableTicks;
    public BlockPos lastUnreachablePos;
    public int faceFailCount;
    public int wallObstacleCount;
    public int jumpRetryCount;
    public int alignCooldown;
    public boolean isAligningToBlock;
    public int expRepairCooldown;
    public int eatCooldownTimer;
    public int foodSearchCooldown;
    public int repathCooldown;
    public boolean sweepNeedsRepath;
    public boolean hasActiveTarget;
    public BlockPos bridgeTargetPos;
    public BlockPos layerTargetPos;
    public int bridgeCooldown;
    public int consecutiveObstacles;
    public int unminableTicks;
    public boolean allowAutoEatAction;
    public static final int EAT_THRESHOLD_DEFAULT = 2;
    public static final int SWEEP_SEARCH_RADIUS = 20;
    public static final int MAX_UNMINABLE_CACHE = 200;
    public boolean isToolDamaged;
    public int expThrowTicks;
    public boolean isThrowingExp;
    public int expPreviousSlot;
    public int expThrowCount;
    public boolean isRestoringSlot;
    public int restoredSlotIndex;
    public int slotRestoreDelay;
    public BlockPos lastExpPlayerPos;
    public boolean expTestActive;
    public boolean expTestFinished;
    public final AutoMineMovementHelper movementHelper = new AutoMineMovementHelper();
    public AutoMineState state = AutoMineState.IDLE;
    public final Set<BlockPos> placedBlockCache = new HashSet();
    public final Set<BlockPos> ignoredBlockCache = new HashSet();
    public AutoMineDigTask currentDigTask = AutoMineDigTask.APPROACH;
    public int activeSlotIndex = -1;
    public final ArrayDeque<BlockPos> blockQueue = new ArrayDeque<>();
    public final Map<BlockPos, Integer> failCountMap = new HashMap();
    public final Map<BlockPos, Integer> attemptMap = new HashMap();
    public final Map<BlockPos, Integer> retryMap = new HashMap();
    public final Set<BlockPos> visitedPositions = new HashSet();
    public String statusDetail = "";
    public int previousHotbarSlot = -1;
    public int lastToolDamage = Integer.MAX_VALUE;
    public int preferredToolSlot = -1;
    private boolean oKeyPressedLastTick = false;

    public AutoMineEngine(MinecraftClient client, AutoMineConfig autoMineConfig, AutoMineSelection autoMineSelection) {
        this.client = client;
        this.config = autoMineConfig;
        this.selection = autoMineSelection;
        this.blockBreaker = new AutoMineBlockBreaker(client);
    }

    public AutoMineState state() {
        return this.state;
    }

    public boolean isActive() {
        return this.state == AutoMineState.RUNNING || this.state == AutoMineState.PAUSED;
    }

    public AutoMinePlan plan() {
        return this.plan;
    }

    public BlockPos activeTarget() {
        return this.pendingFluidPos;
    }

    public List<BlockPos> faceCells() {
        if (this.state != AutoMineState.RUNNING || this.plan == null || this.plan.areLayerFacesDone()) {
            return List.of();
        }
        return this.plan.faceCells();
    }

    public BlockPos faceCenter() {
        if (this.state != AutoMineState.RUNNING || this.plan == null || this.plan.areLayerFacesDone()) {
            return null;
        }
        return this.plan.faceCenter();
    }

    public BlockPos breakingTarget() {
        return this.blockBreaker.aiming();
    }

    public int mined() {
        return this.fluidLockTimer;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public String statusLine() throws MatchException {
        switch (this.state.ordinal()) {
            case 0:
                return "chưa chạy";
            case 1:
                return this.plan.describe() + " · " + Math.round(this.plan.progress() * 100.0f) + "% · đào " + this.fluidLockTimer + (this.fluidClearTimer > 0 ? " · bỏ qua " + this.fluidClearTimer : "") + (this.statusDetail.isEmpty() ? "" : " · " + this.statusDetail);
            case 2:
                return "tạm dừng · " + (this.plan != null ? this.plan.describe() : "");
            case 3:
                return "xong · đã đào " + this.fluidLockTimer + " block";
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    public String start() {
        ClientPlayerEntity player = this.client.player;
        if (player == null || this.client.world == null) {
            return "chưa vào thế giới";
        }
        if (this.state == AutoMineState.RUNNING) {
            return "đang đào rồi — /pause để tạm dừng, /stop để dừng";
        }
        if (this.state == AutoMineState.PAUSED) {
            resume();
            stopWithError("chạy tiếp từ chỗ đang dừng");
            return null;
        }
        if (!this.selection.isComplete()) {
            return "chưa đủ 2 điểm — dùng /sel 1 và /sel 2";
        }
        if ((this.plan == null || this.plan.isDone() || !this.plan.matchesSelection(this.selection)) ? false : true) {
            stopWithError("tiếp tục đào từ chỗ cũ — " + this.plan.describe());
            this.lastFluidPos = null;
            this.foodSearchCooldown = 0;
            executeMiningTick();
            this.blockQueue.clear();
            this.eatTargetPos = null;
            this.lastPlacedBlockPos = null;
            this.pathingCooldown = 0;
            clearBlockQueue();
            clearPriorityTarget();
        } else {
            stop();
            boolean z = this.selection.sizeX() >= this.selection.sizeZ();
            BlockPos playerPos = player.getBlockPos();
            int coordX = z ? playerPos.getX() : playerPos.getZ();
            int coordZ = z ? playerPos.getZ() : playerPos.getX();
            int iMinX = z ? this.selection.minX() : this.selection.minZ();
            int iMaxX = z ? this.selection.maxX() : this.selection.maxZ();
            int iMinZ = z ? this.selection.minZ() : this.selection.minX();
            this.plan = new AutoMinePlan(this.selection, this.config.layerHeight, this.config.passWidth, Math.abs(coordZ - (z ? this.selection.maxZ() : this.selection.maxX())) < Math.abs(coordZ - iMinZ), Math.abs(coordX - iMaxX) < Math.abs(coordX - iMinX));
            AutoMineProgressNotifier.onStart(this.client, this.selection, this.plan);
            this.strategy = new AutoMineMiningStrategy(this.client, this.config, this.movementHelper, this.blockBreaker, this.selection);
            this.fluidLockTimer = 0;
            this.fluidClearTimer = 0;
            this.activeSlotIndex = -1;
            this.isFluidLocked = false;
            this.currentDigTask = AutoMineDigTask.APPROACH;
            this.lastFluidPos = null;
            this.scaffoldWaitTicks = 0;
            this.scaffoldPos = null;
            this.foodSearchCooldown = 0;
            this.isPathStuck = false;
            this.visitedPositions.clear();
            this.repathCooldown = 0;
            this.sweepNeedsRepath = false;
            this.hasActiveTarget = false;
            this.statusDetail = "";
            this.placedBlockCache.clear();
            this.ignoredBlockCache.clear();
            this.blockQueue.clear();
            this.eatTargetPos = null;
            executeMiningTick();
            clearPriorityTarget();
        }
        resetMiningState();
        this.scaffoldTimer = 20;
        this.playerInput = player.input;
        player.input = this.movementHelper;
        this.state = AutoMineState.RUNNING;
        return null;
    }

    public void resetMiningState() {
        this.bridgeTargetPos = null;
        this.layerTargetPos = null;
        this.lastExpPlayerPos = null;
        this.bridgeCooldown = 0;
        this.consecutiveObstacles = 0;
        this.allowAutoEatAction = false;
        this.expTestActive = false;
        this.expTestFinished = false;
        this.isToolDamaged = false;
        this.isThrowingExp = false;
        this.expThrowCount = 0;
        this.isRestoringSlot = false;
        this.restoredSlotIndex = 0;
        this.expThrowTicks = 0;
        this.slotRestoreDelay = 0;
        this.preferredToolSlot = -1;
    }

    public void pause() {
        this.hasFluidTask = false;
        if (this.state == AutoMineState.RUNNING) {
            this.state = AutoMineState.PAUSED;
            this.movementHelper.stop();
            this.blockBreaker.cancel();
            AutoMineProgressNotifier.onStateChange(this.client, AutoMineState.PAUSED, "Người dùng tạm dừng");
        }
    }

    public void pauseForEating() {
        if (this.state == AutoMineState.RUNNING) {
            pause();
            this.hasFluidTask = true;
        }
    }

    public boolean isAutoPaused() {
        return this.hasFluidTask;
    }

    public void resumeFromEating() {
        if (this.state != AutoMineState.PAUSED || !this.hasFluidTask) {
            return;
        }
        this.state = AutoMineState.RUNNING;
        this.hasFluidTask = false;
        setDigTask(AutoMineDigTask.AIM_LOCK);
    }

    public void resume() {
        String strStart;
        if (this.state == AutoMineState.PAUSED) {
            this.state = AutoMineState.RUNNING;
            AutoMineProgressNotifier.onStateChange(this.client, AutoMineState.RUNNING, "Tiếp tục đào");
            this.hasFluidTask = false;
            this.placedBlockCache.clear();
            this.ignoredBlockCache.clear();
            this.blockQueue.clear();
            this.eatTargetPos = null;
            executeMiningTick();
            this.lastFluidPos = null;
            this.foodSearchCooldown = 0;
            clearBlockQueue();
            clearPriorityTarget();
            return;
        }
        if (this.state == AutoMineState.IDLE && this.plan != null && !this.plan.isDone() && (strStart = start()) != null) {
            stopWithError("§c" + strStart);
        }
    }

    public void stop() {
        if (this.state == AutoMineState.RUNNING || this.state == AutoMineState.PAUSED) {
            AutoMineProgressNotifier.onStateChange(this.client, AutoMineState.IDLE, "Đã dừng đào");
        }
        resetExpState();
        this.blockBreaker.cancel();
        this.pendingFluidPos = null;
        this.hasFluidTask = false;
        AutoMineAutoEat.reset();
        if (this.client.player != null) {
            stopEating(this.client.player);
        }
        this.isToolDamaged = false;
        this.slotRestoreDelay = 0;
        this.state = (this.plan == null || !this.plan.isDone()) ? AutoMineState.IDLE : AutoMineState.DONE;
    }

    public void resetExpState() {
        ClientPlayerEntity player = this.client.player;
        if (player != null && this.playerInput != null) {
            player.input = this.playerInput;
        }
        this.playerInput = null;
        this.movementHelper.stop();
    }

    public void resetMovementState() {
        AutoMineProgressNotifier.onComplete(this.client, true, this.fluidLockTimer, this.plan);
        stopWithError("xong! Đã đào " + this.fluidLockTimer + " block" + (this.fluidClearTimer > 0 ? ", bỏ qua " + this.fluidClearTimer + " block không phá được" : "") + ".");
        resetExpState();
        this.blockBreaker.cancel();
        this.pendingFluidPos = null;
        this.state = AutoMineState.DONE;
    }

    public void startExpTest() {
        ClientPlayerEntity player = this.client.player;
        if (player == null) {
            return;
        }
        this.isToolDamaged = true;
        this.previousHotbarSlot = player.getInventory().getSelectedSlot();
        this.slotRestoreDelay = 10;
        this.expThrowTicks = 0;
        this.isThrowingExp = false;
        stopWithError("test ném exp: 10 bình…");
    }

    private void checkKeybindO() {
        if (this.client == null) {
            return;
        }
        if (this.client.currentScreen != null) {
            this.oKeyPressedLastTick = true;
            return;
        }
        boolean isOPressed = false;
        try {
            if (this.client.getWindow() != null) {
                isOPressed = InputUtil.isKeyPressed(this.client.getWindow(), 79);
            }
        } catch (Throwable th) {
        }
        if (isOPressed && !this.oKeyPressedLastTick) {
            if (this.state == AutoMineState.RUNNING) {
                stop();
                stopWithError("§cđã dừng");
            } else {
                String strStart = start();
                if (strStart != null) {
                    stopWithError("§c" + strStart);
                } else {
                    stopWithError("§abắt đầu đào");
                }
            }
        }
        this.oKeyPressedLastTick = isOPressed;
    }

    public void tick() {
        checkKeybindO();
        if (this.state != AutoMineState.RUNNING) {
            if (this.slotRestoreDelay > 0 && this.client.player != null) {
                tickAutoEat(this.client.player);
                return;
            }
            return;
        }
        ClientPlayerEntity player = this.client.player;
        ClientWorld world = this.client.world;
        if (player == null || world == null) {
            stop();
            return;
        }
        if (player.input != this.movementHelper) {
            this.playerInput = player.input;
            player.input = this.movementHelper;
        }
        this.movementHelper.stop();
        if (AutoMineFluidHandler.tick(this)) {
            return;
        }
        AutoMineProgressNotifier.onTick(this.client, this, this.plan, this.fluidLockTimer);
        if (this.pendingFluidPos != null && world.getBlockState(this.pendingFluidPos).isAir()) {
            this.fluidLockTimer++;
            this.retryMap.remove(this.pendingFluidPos);
            markUnminable(this.pendingFluidPos);
            this.pendingFluidPos = null;
            this.blockBreaker.disarm();
        }
        if (!this.failCountMap.isEmpty()) {
            this.failCountMap.replaceAll((pos, num) -> {
                return Integer.valueOf(num.intValue() - 1);
            });
            this.failCountMap.values().removeIf(num2 -> {
                return num2.intValue() <= 0;
            });
        }
        if (!this.attemptMap.isEmpty()) {
            this.attemptMap.replaceAll((pos2, num3) -> {
                return Integer.valueOf(num3.intValue() - 1);
            });
            this.attemptMap.values().removeIf(num4 -> {
                return num4.intValue() <= 0;
            });
        }
        int i = this.eatCooldownTimer + 1;
        this.eatCooldownTimer = i;
        if (i >= 600) {
            this.eatCooldownTimer = 0;
            this.retryMap.clear();
        }
        if (this.targetBreakPos != null || this.isBreakingBlock) {
            this.unreachableTicks++;
        }
        if (!this.blockQueue.isEmpty()) {
            this.stuckCount++;
        }
        this.isScaffolding = (((player.getX() - this.lastPlayerX) * (player.getX() - this.lastPlayerX)) + ((player.getY() - this.lastPlayerY) * (player.getY() - this.lastPlayerY))) + ((player.getZ() - this.lastPlayerZ) * (player.getZ() - this.lastPlayerZ)) > 0.0025d;
        this.lastPlayerX = player.getX();
        this.lastPlayerY = player.getY();
        this.lastPlayerZ = player.getZ();
        if (this.scaffoldTimer > 0) {
            this.scaffoldTimer--;
            this.movementHelper.stop();
            this.blockBreaker.cancel();
            int i2 = 0;
            while (countAirSurrounding(world, 1) == 0) {
                int i3 = i2;
                i2++;
                if (i3 >= this.plan.layerCount()) {
                    break;
                }
                int iLayerIndex = this.plan.layerIndex() + 1;
                if (!this.plan.nextLayer()) {
                    stopWithError("cả vùng đã sạch — không còn gì để đào");
                    resetMovementState();
                    return;
                } else {
                    stopWithError("tầng " + iLayerIndex + " đã sạch sẵn — bỏ qua, xuống tầng " + (this.plan.layerIndex() + 1));
                    clearPriorityTarget();
                    this.blockQueue.clear();
                    executeMiningTick();
                }
            }
            if (this.scaffoldTimer == 0 && this.plan.areLayerFacesDone() && !this.expTestFinished && countAirSurrounding(world, 20) >= 20) {
                this.expTestFinished = true;
                this.plan.restartLayer();
                this.lastFluidPos = null;
                this.foodSearchCooldown = 0;
                clearBlockQueue();
                clearPriorityTarget();
                stopWithError("tầng " + (this.plan.layerIndex() + 1) + " còn nhiều block nguyên — đào lại theo tâm 9 ô");
            }
            if (!this.plan.areLayerFacesDone()) {
                int i4 = 0;
                while (!scanForOres(world) && !this.plan.areLayerFacesDone()) {
                    int i5 = i4;
                    i4++;
                    if (i5 >= 4096) {
                        break;
                    } else {
                        this.plan.advance();
                    }
                }
            }
            if (this.scaffoldTimer == 0 && !this.plan.areLayerFacesDone() && player.getBlockY() > this.plan.faceCenter().getY()) {
                int i6 = 0;
                while (!this.plan.areLayerFacesDone() && !findFallbackCell(world)) {
                    int i7 = i6;
                    i6++;
                    if (i7 >= 4096) {
                        break;
                    } else {
                        this.plan.advance();
                    }
                }
                if (!this.plan.areLayerFacesDone() && !advancePlanCell(world)) {
                    this.breakTimeout = 0;
                    lookTowardsPos(player);
                    stopWithError("đứng trên tầng " + (this.plan.layerIndex() + 1) + " — tới ô 9 đỏ, mở giếng rồi đào tiếp");
                }
            }
            this.statusDetail = "quét vùng điểm 1 → điểm 2…";
            return;
        }
        this.strategy.setLayerFloor(this.plan.layerBottom());
        this.strategy.setDigAimY(this.plan.faceCenter().getY());
        this.strategy.setReach(player.getBlockInteractionRange());
        if (tickAutoEat(player) || selectBestTool(player)) {
            return;
        }
        if (this.isFluidLocked && !this.strategy.isClimbing()) {
            setDigTask(AutoMineDigTask.AIM_LOCK);
        }
        this.isFluidLocked = this.strategy.isClimbing();
        if (this.config.allowPlace && !this.hasActiveTarget && this.strategy.isClimbing() && !AutoMineBlockFilter.hasBuildingBlock(player)) {
            this.hasActiveTarget = true;
            stopWithError("§ehotbar không có block đặc nào để xây trụ leo lên — bỏ đá cuội/deepslate vào hotbar.");
        }
        if (tryDirectMine(world, player) || handleObstacles(world, player)) {
            return;
        }
        tickExpRepair(player, world);
    }

    public void tickExpRepair(ClientPlayerEntity player, World world) {
        if (!this.plan.areLayerFacesDone()) {
            validateTargetBlock(world);
            return;
        }
        if (tryThrowExpBottle(player, world)) {
            return;
        }
        int iLayerIndex = this.plan.layerIndex() + 1;
        clearPriorityTarget();
        this.placedBlockCache.clear();
        this.ignoredBlockCache.clear();
        this.strategy.forgetPlaced();
        this.blockQueue.clear();
        this.expTestActive = false;
        this.expTestFinished = false;
        this.allowAutoEatAction = false;
        if (this.plan.nextLayer()) {
            stopWithError("tầng " + iLayerIndex + " sạch — xuống tầng " + (this.plan.layerIndex() + 1));
            this.statusDetail = "";
            this.breakTimeout = 0;
            lookTowardsPos(player);
            return;
        }
        resetMovementState();
    }

    public boolean tryThrowExpBottle(ClientPlayerEntity player, World world) {
        if (!this.config.sweepLayer) {
            return false;
        }
        double dMax = Math.max(this.config.reachDistance, player.getBlockInteractionRange());
        if (this.bridgeTargetPos != null && (!isBreakableBlock(world, this.bridgeTargetPos) || !this.selection.contains(this.bridgeTargetPos) || !this.blockBreaker.canReach(this.bridgeTargetPos, dMax))) {
            this.bridgeTargetPos = null;
            this.bridgeCooldown = 0;
        }
        if (this.bridgeTargetPos == null && !this.strategy.isClimbing()) {
            this.bridgeTargetPos = findNextMiningTarget(world, player, dMax, false);
            if (this.bridgeTargetPos == null) {
                this.bridgeTargetPos = findNextMiningTarget(world, player, dMax, true);
            }
            if (this.bridgeTargetPos != null) {
                this.bridgeCooldown = 0;
            }
        }
        if (this.bridgeTargetPos != null) {
            int i = this.bridgeCooldown + 1;
            this.bridgeCooldown = i;
            if (i > 70) {
                this.failCountMap.put(this.bridgeTargetPos.toImmutable(), Integer.valueOf(STATUS_REFRESH_TICKS));
                this.bridgeTargetPos = null;
                this.bridgeCooldown = 0;
                this.statusDetail = "bỏ qua ô sót kẹt";
                return true;
            }
            this.movementHelper.stop();
            setMiningTarget(this.bridgeTargetPos, dMax);
            this.statusDetail = "vét sót quanh chỗ đứng";
            return true;
        }
        BlockPos cachedTargetPos = searchReachableBlock(world, player);
        if (cachedTargetPos == null || !this.selection.contains(cachedTargetPos)) {
            this.bridgeTargetPos = null;
            this.layerTargetPos = null;
            this.lastExpPlayerPos = null;
            this.failCountMap.clear();
            return false;
        }
        if (this.layerTargetPos == null || !cachedTargetPos.equals(this.lastExpPlayerPos)) {
            this.lastExpPlayerPos = cachedTargetPos;
            this.layerTargetPos = findAccessibleNeighbor(world, player, cachedTargetPos);
        }
        if (this.layerTargetPos == null || !this.selection.contains(this.layerTargetPos)) {
            this.failCountMap.put(cachedTargetPos.toImmutable(), 2400);
            this.layerTargetPos = null;
            this.lastExpPlayerPos = null;
            return true;
        }
        this.statusDetail = "vét sót — đi tới block sót";
        AutoMinePathNode autoMinePathNodeMoveTo = this.strategy.moveTo(this.layerTargetPos);
        if (autoMinePathNodeMoveTo == AutoMinePathNode.BLOCKED) {
            this.failCountMap.put(cachedTargetPos.toImmutable(), Integer.valueOf(STATUS_REFRESH_TICKS));
            this.visitedPositions.add(this.layerTargetPos);
            this.layerTargetPos = null;
            this.lastExpPlayerPos = null;
            return true;
        }
        if (autoMinePathNodeMoveTo == AutoMinePathNode.ARRIVED) {
            this.movementHelper.stop();
            this.layerTargetPos = null;
            return true;
        }
        return true;
    }

    public BlockPos findNextMiningTarget(World world, ClientPlayerEntity player, double d, boolean z) {
        BlockPos playerPos = player.getBlockPos();
        Vec3d playerPosVec = player.getEyePos();
        int coordY = this.plan.faceCenter().getY();
        int iSqrt = ((int) Math.sqrt(100.0d)) + 1;
        int iMax = Math.max(this.plan.minTravel(), (this.plan.travelAxis == Direction.Axis.X ? playerPos.getX() : playerPos.getZ()) - iSqrt);
        int iMin = Math.min(this.plan.maxTravel(), (this.plan.travelAxis == Direction.Axis.X ? playerPos.getX() : playerPos.getZ()) + iSqrt);
        int iMax2 = Math.max(this.plan.minCross(), (this.plan.travelAxis == Direction.Axis.X ? playerPos.getZ() : playerPos.getX()) - iSqrt);
        int iMin2 = Math.min(this.plan.maxCross(), (this.plan.travelAxis == Direction.Axis.X ? playerPos.getZ() : playerPos.getX()) + iSqrt);
        BlockPos addPos = null;
        int i = -1;
        double d2 = Double.MAX_VALUE;
        int iLayerTop = this.plan.layerTop();
        while (iLayerTop >= this.plan.layerBottom()) {
            for (int i2 = iMax; i2 <= iMin; i2++) {
                for (int i3 = iMax2; i3 <= iMin2; i3++) {
                    BlockPos posAt = this.plan.posAt(i2, i3, iLayerTop);
                    if (this.selection.contains(posAt)) {
                        double valY = posAt.getSquaredDistance(playerPos);
                        if (valY <= 100.0d && isBreakableBlock(world, posAt) && !this.failCountMap.containsKey(posAt) && !this.attemptMap.containsKey(posAt) && this.blockBreaker.canReach(posAt, d)) {
                            BlockHitResult raycastHit = world.raycast(new RaycastContext(playerPosVec, Vec3d.ofCenter(posAt), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
                            if (raycastHit.getType() != HitResult.Type.BLOCK || raycastHit.getBlockPos().equals(posAt)) {
                                int faceScore = getExposedFaceScore(world, posAt) + (iLayerTop == coordY ? 100 : 0);
                                if (faceScore > i || (faceScore == i && valY < d2)) {
                                    i = faceScore;
                                    d2 = valY;
                                    addPos = posAt.toImmutable();
                                }
                            }
                        }
                    }
                }
            }
            iLayerTop--;
        }
        return addPos;
    }

    public void setMiningTarget(BlockPos pos, double d) {
        if (!this.selection.contains(pos)) {
            return;
        }
        ClientPlayerEntity player = this.client.player;
        if (player != null) {
            aimAtTarget(player, pos);
        }
        if (!pos.equals(this.pendingFluidPos)) {
            setPriorityTarget(pos);
        }
        if (!this.blockBreaker.tickArmed(pos, d)) {
            this.blockBreaker.disarm();
            this.pendingFluidPos = null;
        }
    }

    public BlockPos searchReachableBlock(World world, ClientPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        int coordY = this.plan.faceCenter().getY();
        BlockPos addPos = null;
        int i = -1;
        double d = Double.MAX_VALUE;
        int iLayerTop = this.plan.layerTop();
        while (iLayerTop >= this.plan.layerBottom()) {
            for (int iMinTravel = this.plan.minTravel(); iMinTravel <= this.plan.maxTravel(); iMinTravel++) {
                for (int iMinCross = this.plan.minCross(); iMinCross <= this.plan.maxCross(); iMinCross++) {
                    BlockPos posAt = this.plan.posAt(iMinTravel, iMinCross, iLayerTop);
                    if (isBreakableBlock(world, posAt) && !this.failCountMap.containsKey(posAt) && !this.attemptMap.containsKey(posAt)) {
                        int faceScore = getExposedFaceScore(world, posAt) + (iLayerTop == coordY ? 100 : 0);
                        double valY = posAt.getSquaredDistance(playerPos);
                        if (faceScore > i || (faceScore == i && valY < d)) {
                            i = faceScore;
                            d = valY;
                            addPos = posAt.toImmutable();
                        }
                    }
                }
            }
            iLayerTop--;
        }
        return addPos;
    }

    public boolean isBlockReachable(World world, ClientPlayerEntity player, double d) {
        BlockPos playerPos = player.getBlockPos();
        for (BlockPos pos : this.attemptMap.keySet()) {
            if (pos.getSquaredDistance(playerPos) <= d && isBreakableBlock(world, pos) && !this.failCountMap.containsKey(pos)) {
                return true;
            }
        }
        return false;
    }

    public void markUnminable(BlockPos pos) {
        if (pos != null) {
            this.attemptMap.put(pos, 2);
        }
    }

    public int countAirSurrounding(World world, int i) {
        int i2 = 0;
        for (int iLayerTop = this.plan.layerTop(); iLayerTop >= this.plan.layerBottom(); iLayerTop--) {
            for (int iMinTravel = this.plan.minTravel(); iMinTravel <= this.plan.maxTravel(); iMinTravel++) {
                for (int iMinCross = this.plan.minCross(); iMinCross <= this.plan.maxCross(); iMinCross++) {
                    if (isAirOrPassable(world, this.plan.posAt(iMinTravel, iMinCross, iLayerTop))) {
                        i2++;
                        if (i2 >= i) {
                            return i2;
                        }
                    }
                }
            }
        }
        return i2;
    }

    public int getExposedFaceScore(World world, BlockPos pos) {
        int i = 0;
        for (int i2 = -1; i2 <= 1; i2++) {
            for (int i3 = -1; i3 <= 1; i3++) {
                for (int i4 = -1; i4 <= 1; i4++) {
                    if ((i2 | i3 | i4) != 0 && isAirOrPassable(world, pos.add(i2, i3, i4))) {
                        i++;
                    }
                }
            }
        }
        return i;
    }

    public boolean tickAutoEat(ClientPlayerEntity player) {
        boolean z = this.slotRestoreDelay > 0;
        if (!this.config.expRepair && !z) {
            this.isToolDamaged = false;
            return false;
        }
        if (this.isRestoringSlot) {
            if ((this.client.world == null || this.client.world.getEntitiesByClass(ExperienceOrbEntity.class, player.getBoundingBox().expand(4.0d), blockEntity -> {
                return true;
            }).isEmpty()) ? false : true) {
                int i = this.restoredSlotIndex + 1;
                this.restoredSlotIndex = i;
                if (i <= 200) {
                    this.movementHelper.stop();
                    this.blockBreaker.cancel();
                    this.statusDetail = "chờ hút hết exp quanh người…";
                    return true;
                }
            }
            this.isRestoringSlot = false;
            this.restoredSlotIndex = 0;
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        if (!this.isToolDamaged) {
            if (this.strategy.isClimbing() || this.sweepNeedsRepath) {
                return false;
            }
            int windowId = inventory.getSelectedSlot();
            ItemStack heldStack = inventory.getStack(windowId);
            if (!heldStack.isDamageable() || heldStack.getMaxDamage() - heldStack.getDamage() > this.config.expRepairThreshold) {
                return false;
            }
            if (this.isThrowingExp) {
                if (findItemSlot(inventory, Items.EXPERIENCE_BOTTLE) < 0 && findHotbarSlot(inventory, Items.EXPERIENCE_BOTTLE) < 0) {
                    return false;
                }
                this.isThrowingExp = false;
            }
            this.isToolDamaged = true;
            this.previousHotbarSlot = windowId;
            this.expThrowTicks = 0;
            this.expPreviousSlot = 0;
            this.lastToolDamage = Integer.MAX_VALUE;
            stopWithError("cúp còn " + (heldStack.getMaxDamage() - heldStack.getDamage()) + " độ bền — dừng đào, quăng exp hồi cúp");
        }
        ItemStack offHandStack = inventory.getStack(this.previousHotbarSlot);
        if (!z) {
            int maxStackSize = offHandStack.isEmpty() ? -1 : offHandStack.getDamage();
            if (maxStackSize >= 0 && maxStackSize < this.lastToolDamage) {
                this.expPreviousSlot = 0;
            } else {
                int i2 = this.expPreviousSlot + 1;
                this.expPreviousSlot = i2;
                if (i2 > 1200) {
                    stopWithError("00a7esua cup 60s kh00f4ng ti1ebfn tri1ec3n (server ch1eb7n?) 2014 011100e0o ti1ebfp, l00e1t th1eed l1ea1i.");
                    this.isThrowingExp = true;
                    stopEating(player);
                    this.isToolDamaged = false;
                    this.expPreviousSlot = 0;
                    this.lastToolDamage = Integer.MAX_VALUE;
                    return false;
                }
            }
            this.lastToolDamage = maxStackSize < 0 ? Integer.MAX_VALUE : maxStackSize;
        }
        if (!z && (offHandStack.isEmpty() || !offHandStack.isDamageable() || offHandStack.getDamage() == 0)) {
            inventory.setSelectedSlot(this.previousHotbarSlot);
            stopEating(player);
            this.isToolDamaged = false;
            this.expThrowCount = 0;
            if (!offHandStack.isEmpty() && offHandStack.isDamageable() && offHandStack.getDamage() == 0) {
                stopWithError("cúp đầy độ bền — hút nốt exp rồi đào tiếp");
            }
            this.isRestoringSlot = true;
            this.restoredSlotIndex = 0;
            return true;
        }
        this.movementHelper.stop();
        this.blockBreaker.cancel();
        this.pendingFluidPos = null;
        if (inventory.getSelectedSlot() != this.previousHotbarSlot) {
            inventory.setSelectedSlot(this.previousHotbarSlot);
        }
        if (player.getOffHandStack().getItem() != Items.EXPERIENCE_BOTTLE) {
            int invBottleSlot = findItemSlot(inventory, Items.EXPERIENCE_BOTTLE);
            int bottleSlot = invBottleSlot >= 0 ? 36 + invBottleSlot : findHotbarSlot(inventory, Items.EXPERIENCE_BOTTLE);
            if (bottleSlot < 0) {
                if (z) {
                    stopWithError("§etest ném: không có bình exp nào trong người.");
                    this.slotRestoreDelay = 0;
                    stopEating(player);
                    this.isToolDamaged = false;
                    return false;
                }
                int i3 = this.expThrowCount + 1;
                this.expThrowCount = i3;
                if (i3 <= 20) {
                    this.movementHelper.stop();
                    this.statusDetail = "chờ túi đồ đồng bộ để lấy exp tiếp…";
                    return true;
                }
                this.expThrowCount = 0;
                stopWithError("§ehết bình exp — cúp còn " + (offHandStack.getMaxDamage() - offHandStack.getDamage()) + " độ bền.");
                this.isThrowingExp = true;
                stopEating(player);
                this.isToolDamaged = false;
                this.isRestoringSlot = true;
                this.restoredSlotIndex = 0;
                return true;
            }
            this.expThrowCount = 0;
            if (this.preferredToolSlot < 0) {
                this.preferredToolSlot = bottleSlot;
            }
            this.client.interactionManager.clickSlot(player.playerScreenHandler.syncId, bottleSlot, 40, SlotActionType.SWAP, player);
            this.statusDetail = "đưa 1 stack exp sang tay phụ";
            return true;
        }
        player.setPitch(Math.min(90.0f, player.getPitch() + 30.0f));
        int i4 = this.expThrowTicks + 1;
        this.expThrowTicks = i4;
        if (i4 >= 2) {
            this.expThrowTicks = 0;
            if (this.client.interactionManager.interactItem(player, Hand.OFF_HAND).isAccepted()) {
                player.swingHand(Hand.OFF_HAND);
            }
            if (z) {
                int i5 = this.slotRestoreDelay - 1;
                this.slotRestoreDelay = i5;
                if (i5 <= 0) {
                    inventory.setSelectedSlot(this.previousHotbarSlot);
                    stopEating(player);
                    this.isToolDamaged = false;
                    stopWithError("test ném exp xong.");
                    return true;
                }
            }
        }
        this.statusDetail = z ? "test ném exp — còn " + this.slotRestoreDelay + " bình" : "quăng exp hồi cúp — độ bền " + (offHandStack.getMaxDamage() - offHandStack.getDamage()) + "/" + offHandStack.getMaxDamage();
        return true;
    }

    public void stopEating(ClientPlayerEntity player) {
        if (this.preferredToolSlot >= 0 && this.client.interactionManager != null) {
            this.client.interactionManager.clickSlot(player.playerScreenHandler.syncId, this.preferredToolSlot, 40, SlotActionType.SWAP, player);
            this.preferredToolSlot = -1;
        }
    }

    public static int findItemSlot(PlayerInventory inventory, Item item) {
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int findHotbarSlot(PlayerInventory inventory, Item item) {
        for (int i = 9; i < 36; i++) {
            if (inventory.getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public boolean selectBestTool(ClientPlayerEntity player) {
        if (!this.config.allowPlace) {
            this.sweepNeedsRepath = false;
            return false;
        }
        boolean z = false;
        if (!this.plan.areLayerFacesDone()) {
            BlockPos faceCenterPos = this.plan.faceCenter();
            double deltaX = (((double) faceCenterPos.getX()) + 0.5d) - player.getX();
            double d = deltaX * deltaX;
            z = d + (((((double) faceCenterPos.getZ()) + 0.5d) - player.getZ()) * d) <= 6.25d;
        }
        if (this.plan.areLayerFacesDone() || (this.currentDigTask == AutoMineDigTask.APPROACH && !z) || this.currentDigTask == AutoMineDigTask.REPOSITION || this.layerTargetPos != null || this.isPathStuck) {
            this.sweepNeedsRepath = false;
            return false;
        }
        boolean z2 = player.getBlockY() < this.plan.layerBottom();
        if (!this.sweepNeedsRepath && !z2) {
            return false;
        }
        if (this.sweepNeedsRepath && !z2 && player.isOnGround()) {
            this.sweepNeedsRepath = false;
            this.repathCooldown = 0;
            setDigTask(AutoMineDigTask.AIM_LOCK);
            return false;
        }
        this.sweepNeedsRepath = true;
        this.repathCooldown++;
        this.pendingFluidPos = null;
        if (!this.strategy.pillarUp(player)) {
            if (!AutoMineBlockFilter.hasBuildingBlock(player)) {
                this.movementHelper.stop();
                updateStatusLine("§ebị lọt xuống dưới tầng mà hotbar không có block đặc nào để kê — bỏ đá cuội vào hotbar.");
                this.statusDetail = "lọt xuống — chờ block để kê lên";
                return true;
            }
            this.sweepNeedsRepath = false;
            return false;
        }
        this.statusDetail = this.strategy.isBreakingCeiling() ? "lọt xuống — phá trần trên đầu để kê tiếp" : "lọt xuống — kê block leo lên (" + (this.repathCooldown / 20) + "s)";
        return true;
    }

    public void setDigTask(AutoMineDigTask autoMineDigTask) {
        if (this.currentDigTask != autoMineDigTask) {
            this.currentDigTask = autoMineDigTask;
            this.scaffoldWaitTicks = 0;
        }
        if (autoMineDigTask == AutoMineDigTask.APPROACH || autoMineDigTask == AutoMineDigTask.REPOSITION) {
            this.isPathStuck = false;
        }
    }

    public void clearBlockQueue() {
        this.currentDigTask = AutoMineDigTask.APPROACH;
        this.scaffoldWaitTicks = 0;
        this.scaffoldState = 0;
        this.scaffoldPos = null;
        this.scaffoldPlaced = this.foodSearchCooldown > 0;
        this.isPathStuck = false;
        this.visitedPositions.clear();
    }

    /* JADX WARN: Code duplicated, block: B:9:0x002d  */
    public boolean queueBlock(BlockPos pos) {
        double reach = this.client.player != null ? Math.max(this.config.reachDistance, this.client.player.getBlockInteractionRange()) : this.config.reachDistance;
        return AutoMineBlockHelper.isAimingAt(this.client, pos, reach);
    }

    public void validateTargetBlock(World world) {
        int i = 0;
        while (!scanForOres(world) && !this.plan.areLayerFacesDone()) {
            int i2 = i;
            i++;
            if (i2 >= 4096) {
                break;
            } else {
                this.plan.advance();
            }
        }
        if (!scanForOres(world)) {
            this.statusDetail = "kiểm tra tầng";
        }
        BlockPos faceCenterPos = this.plan.faceCenter();
        if (!faceCenterPos.equals(this.lastFluidPos)) {
            if (this.foodSearchCooldown > 0) {
                this.foodSearchCooldown--;
            }
            if (this.lastFluidPos != null) {
                setLayerTarget(this.lastFluidPos);
            }
            clearBlockQueue();
            clearPriorityTarget();
            this.lastFluidPos = faceCenterPos.toImmutable();
            this.eatFoodCooldown = 0;
        }
        if (this.plan.rowIndex() != this.activeSlotIndex) {
            this.activeSlotIndex = this.plan.rowIndex();
            this.foodSearchCooldown = 0;
            this.scaffoldPlaced = false;
            onBlockBroken(faceCenterPos);
            this.strategy.reset();
            setDigTask(AutoMineDigTask.APPROACH);
            this.statusDetail = "sang dãy mới — bổ 3 nhát thẳng hàng";
        }
        if (this.client.player != null && tickFluidManagement(world, this.client.player)) {
            return;
        }
        this.scaffoldWaitTicks++;
        if (this.scaffoldWaitTicks > 30 && this.client.player != null && this.plan != null) {
            double dReach = Math.max(this.config.reachDistance, this.client.player.getBlockInteractionRange());
            for (BlockPos cell : this.plan.faceCells()) {
                if (isAirOrPassable(world, cell) && this.blockBreaker.canReach(cell, dReach)) {
                    this.movementHelper.stop();
                    setMiningTarget(cell, dReach);
                    setDigTask(AutoMineDigTask.AIM_LOCK);
                    this.scaffoldWaitTicks = 0;
                    break;
                }
            }
        }
        this.scaffoldState = this.pendingFluidPos != null || this.blockBreaker.aiming() != null || this.strategy.isClimbing() || this.strategy.isBridging() || this.isScaffolding ? 0 : this.scaffoldState + 1;
        if (this.strategy.isBridging()) {
            this.scaffoldState = 0;
        }
        if (this.scaffoldState > 160 && !this.strategy.isBridging()) {
            this.scaffoldState = 0;
            this.eatFoodCooldown++;
            resetStrategyState();
            switch (this.eatFoodCooldown % 3) {
                case 1:
                    this.foodSearchCooldown = 3;
                    this.scaffoldPlaced = true;
                    this.strategy.reset();
                    setDigTask(AutoMineDigTask.APPROACH);
                    stopWithError("§ekẹt 8s — khoan thẳng vào cột tâm");
                    break;
                case 2:
                    this.visitedPositions.clear();
                    this.strategy.reset();
                    setDigTask(AutoMineDigTask.REPOSITION);
                    stopWithError("§ekẹt — tìm chỗ đứng khác để với tới tâm");
                    break;
                default:
                    this.visitedPositions.clear();
                    this.foodSearchCooldown = 0;
                    this.scaffoldPlaced = false;
                    this.strategy.reset();
                    setDigTask(AutoMineDigTask.APPROACH);
                    stopWithError("§ekẹt — tính lại đường đi tới mặt");
                    break;
            }
        }
        ClientPlayerEntity player = this.client.player;
        if ((player != null && faceCenterPos.getSquaredDistance(player.getBlockPos()) <= 36.0d) && this.scaffoldState > 60 && this.blockQueue.isEmpty() && !this.strategy.isBridging()) {
            onBlockBroken(faceCenterPos);
            if (!this.blockQueue.isEmpty()) {
                this.foodSearchCooldown = 0;
                this.scaffoldPlaced = false;
                this.strategy.reset();
                this.scaffoldState = 0;
                setDigTask(AutoMineDigTask.APPROACH);
                stopWithError("§ekẹt 3s — khoan 3 nhát thẳng rồi đi tiếp");
            }
        }
        BlockPos destPos = findNearestOrePos(world);
        if (destPos == null) {
            return;
        }
        switch (this.currentDigTask.ordinal()) {
            case 0:
                markPositionVisited(world, faceCenterPos);
                break;
            case 1:
                recordRetryAttempt(world, destPos);
                break;
            case 2:
                recordFailAttempt(world, destPos);
                break;
            case 3:
                clearPositionHistory(world, destPos);
                break;
        }
    }

    public boolean canReachFace(ClientPlayerEntity player, BlockPos pos) {
        if (player == null) {
            return false;
        }
        return this.blockBreaker.canReach(pos, Math.max(this.config.reachDistance, player.getBlockInteractionRange()));
    }

    public boolean hasLineOfSightTo(ClientPlayerEntity player, BlockPos pos) {
        if (player == null) {
            return false;
        }
        return this.blockBreaker.canReach(pos, Math.max(this.config.reachDistance, player.getBlockInteractionRange()));
    }

    public double distanceToBlock(ClientPlayerEntity player, BlockPos pos) {
        return this.plan.travelAxis == Direction.Axis.X ? player.getX() - (((double) pos.getX()) + 0.5d) : player.getZ() - (((double) pos.getZ()) + 0.5d);
    }

    public static boolean isWithinMaxAngle(ClientPlayerEntity player, BlockPos pos, double d) {
        Vec3d playerPosVec = player.getEyePos();
        return (Math.abs(Math.toDegrees(Math.atan2((((double) pos.getY()) + 0.5d) - playerPosVec.y, Math.hypot((((double) pos.getX()) + 0.5d) - playerPosVec.x, (((double) pos.getZ()) + 0.5d) - playerPosVec.z)))) > d ? 1 : (Math.abs(Math.toDegrees(Math.atan2((((double) pos.getY()) + 0.5d) - playerPosVec.y, Math.hypot((((double) pos.getX()) + 0.5d) - playerPosVec.x, (((double) pos.getZ()) + 0.5d) - playerPosVec.z)))) == d ? 0 : -1)) <= 0;
    }

    public void lookTowardsPos(ClientPlayerEntity player) {
        executeMiningTick();
        if (player == null) {
            return;
        }
        BlockPos faceCenterPos = this.plan.faceCenter();
        int iCurrentTravel = this.plan.currentTravel();
        if (this.plan.maxTravel() - this.plan.minTravel() >= 2) {
            iCurrentTravel = Math.max(this.plan.minTravel() + 1, Math.min(this.plan.maxTravel() - 1, iCurrentTravel));
        }
        BlockPos posAt = this.plan.posAt(iCurrentTravel, this.plan.aimCross(), faceCenterPos.getY());
        ClientWorld world = this.client.world;
        BlockPos pos = null;
        if (world != null) {
            for (int iLayerTop = this.plan.layerTop() + 3; iLayerTop >= this.plan.layerTop(); iLayerTop--) {
                BlockPos pos2 = new BlockPos(posAt.getX(), iLayerTop, posAt.getZ());
                if (AutoMineBlockHelper.standable(world, pos2)) {
                    pos = pos2;
                    break;
                }
            }
        }
        if (pos == null) {
            pos = new BlockPos(posAt.getX(), this.plan.layerTop() + 1, posAt.getZ());
        }
        BlockPos addPos = pos.toImmutable();
        this.targetBreakPos = addPos;
        this.lastUnreachablePos = addPos;
        this.faceFailCount = pos.getY();
    }

    public boolean handleObstacles(World world, ClientPlayerEntity player) {
        Direction direction;
        if (this.targetBreakPos != null) {
            if (this.unreachableTicks > 600) {
                int i = this.breakTimeout + 1;
                this.breakTimeout = i;
                if (i <= 2) {
                    lookTowardsPos(player);
                    stopWithError("§exuống tầng — lối cũ tắc, dò lối khác tới ô 9 (lần " + this.breakTimeout + "/3)");
                    return true;
                }
                this.targetBreakPos = null;
                stopWithError("§exuống tầng — không tới nổi ô 9, máy chính xử tạm");
                return false;
            }
            AutoMinePathNode autoMinePathNodeMoveTo = this.strategy.moveTo(this.targetBreakPos);
            if (autoMinePathNodeMoveTo == AutoMinePathNode.ARRIVED) {
                this.targetBreakPos = null;
                this.isBreakingBlock = true;
                this.miningTicks = 0;
                this.currentMiningPos = null;
                this.unreachableTicks = 0;
                return true;
            }
            if (autoMinePathNodeMoveTo == AutoMinePathNode.BLOCKED) {
                if (attemptBreakBlock(world, player, this.targetBreakPos, Math.max(this.config.reachDistance, player.getBlockInteractionRange()))) {
                    return true;
                }
                int i2 = this.breakTimeout + 1;
                this.breakTimeout = i2;
                if (i2 <= 2) {
                    lookTowardsPos(player);
                    stopWithError("§exuống tầng — kẹt giữa đường, dò lối khác tới ô 9 (lần " + this.breakTimeout + "/3)");
                    return true;
                }
                this.targetBreakPos = null;
                stopWithError("§exuống tầng — không tới nổi ô 9, máy chính xử tạm");
                return false;
            }
            this.statusDetail = "xuống tầng — tới trên đầu ô 9";
            return true;
        }
        if (!this.isBreakingBlock) {
            return false;
        }
        if (this.currentMiningPos != null && !isAirOrPassable(world, this.currentMiningPos)) {
            this.miningTicks++;
            this.currentMiningPos = null;
            this.unreachableTicks = 0;
        }
        if (this.lastUnreachablePos != null && player.isOnGround() && (player.getBlockX() != this.lastUnreachablePos.getX() || player.getBlockZ() != this.lastUnreachablePos.getZ())) {
            int i3 = this.unreachableTicks;
            lookTowardsPos(player);
            this.unreachableTicks = i3;
            this.statusDetail = "xuống tầng — lệch cột, quay lại đúng ô 9";
            return true;
        }
        if (this.miningTicks >= 3 || this.faceFailCount - player.getBlockY() >= 4 || player.getBlockY() <= this.plan.layerBottom()) {
            if (this.unreachableTicks > 600) {
                return executeMiningTick();
            }
            if (!player.isOnGround()) {
                this.movementHelper.stop();
                this.statusDetail = "xuống tầng — chờ đáp đáy giếng";
                return true;
            }
            if (player.getBlockY() < this.plan.layerBottom()) {
                if (this.strategy.pillarUp(player)) {
                    this.statusDetail = "xuống tầng — kê 1 block lên đúng tầm 9 ô";
                    return true;
                }
                return executeMiningTick();
            }
            int i4 = this.jumpRetryCount + 1;
            this.jumpRetryCount = i4;
            if (i4 <= 5) {
                this.movementHelper.stop();
                this.statusDetail = "xuống tầng — đúng tầm, vào việc";
                return true;
            }
            return executeMiningTick();
        }
        if (this.unreachableTicks > 600) {
            return executeMiningTick();
        }
        if (!player.isOnGround()) {
            this.movementHelper.stop();
            this.statusDetail = "xuống tầng — chờ đáp rồi bổ nhát " + (this.miningTicks + 1) + "/3";
            return true;
        }
        if (this.lastUnreachablePos != null) {
            double playerX = player.getX() - (((double) this.lastUnreachablePos.getX()) + 0.5d);
            double playerZ = player.getZ() - (((double) this.lastUnreachablePos.getZ()) + 0.5d);
            double dMax = Math.max(Math.abs(playerX), Math.abs(playerZ));
            if (this.isAligningToBlock && dMax > 0.45d) {
                this.isAligningToBlock = false;
            }
            if (!this.isAligningToBlock) {
                if (dMax <= 0.15d) {
                    this.isAligningToBlock = true;
                } else {
                    int i5 = this.alignCooldown + 1;
                    this.alignCooldown = i5;
                    if (i5 <= 60) {
                        double radians = Math.toRadians(player.getYaw());
                        double dSin = Math.sin(radians);
                        double dCos = Math.cos(radians);
                        this.movementHelper.set(normalizeAngleYaw(((-playerZ) * dCos) - ((-playerX) * dSin)), normalizeAngleYaw(((-playerZ) * dSin) + ((-playerX) * dCos)), false, false);
                        this.statusDetail = "xuống tầng — căn thẳng hàng với giếng";
                        return true;
                    }
                }
            }
        }
        if (this.miningTicks == 0 && this.currentMiningPos == null) {
            if (this.plan.travelAxis == Direction.Axis.X) {
                direction = this.plan.rowDirection() > 0 ? Direction.EAST : Direction.WEST;
            } else {
                direction = this.plan.rowDirection() > 0 ? Direction.SOUTH : Direction.NORTH;
            }
            Direction direction2 = direction;
            if (AutoMineRotationHelper.stepYawTo(player, AutoMineRotationHelper.yawTo(direction2.getOffsetX(), direction2.getOffsetZ()), 20.0f)) {
                this.movementHelper.stop();
                this.statusDetail = "xuống tầng — quay về hướng đào";
                return true;
            }
        }
        BlockPos upPos = player.getBlockPos().down();
        double dMax2 = Math.max(this.config.reachDistance, player.getBlockInteractionRange());
        if (!this.selection.contains(upPos)) {
            return executeMiningTick();
        }
        if (world.getBlockState(upPos).isAir()) {
            int i6 = this.wallObstacleCount + 1;
            this.wallObstacleCount = i6;
            if (i6 > 30) {
                return executeMiningTick();
            }
            this.movementHelper.stop();
            this.statusDetail = "xuống tầng — chờ tụt xuống rồi bổ nhát " + (this.miningTicks + 1) + "/3";
            return true;
        }
        this.wallObstacleCount = 0;
        if (this.failCountMap.containsKey(upPos)) {
            return executeMiningTick();
        }
        if (this.strategy.placedCells().contains(upPos)) {
            return executeMiningTick();
        }
        if (!isAirOrPassable(world, upPos) || !this.blockBreaker.canReach(upPos, dMax2)) {
            return executeMiningTick();
        }
        this.currentMiningPos = upPos.toImmutable();
        this.movementHelper.stop();
        setMiningTarget(upPos, dMax2);
        this.statusDetail = "xuống tầng — bổ xuống " + (this.miningTicks + 1) + "/3";
        return true;
    }

    public boolean findFallbackCell(World world) {
        int i = 0;
        for (BlockPos pos : this.plan.faceCells()) {
            if (!this.selection.contains(pos) || !isAirOrPassable(world, pos)) {
                return false;
            }
            i++;
        }
        return i == 9;
    }

    public boolean advancePlanCell(World world) {
        BlockPos faceCenterPos = this.plan.faceCenter();
        for (int i = 0; i < 3; i++) {
            BlockPos pos = new BlockPos(faceCenterPos.getX(), this.plan.layerTop() - i, faceCenterPos.getZ());
            if (this.selection.contains(pos) && !world.getBlockState(pos).isAir()) {
                return false;
            }
        }
        return true;
    }

    public boolean executeMiningTick() {
        this.isBreakingBlock = false;
        this.currentMiningPos = null;
        this.targetBreakPos = null;
        this.lastUnreachablePos = null;
        this.unreachableTicks = 0;
        this.wallObstacleCount = 0;
        this.jumpRetryCount = 0;
        this.alignCooldown = 0;
        this.isAligningToBlock = false;
        return false;
    }

    public boolean tryDirectMine(World world, ClientPlayerEntity player) {
        BlockPos aimingPos = this.blockBreaker.aiming();
        if (aimingPos == null || this.pendingFluidPos == null || !aimingPos.equals(this.pendingFluidPos)) {
            this.expRepairCooldown = 0;
            AutoMineMovementHelper.preventJump = false;
            return false;
        }
        double dMax = Math.max(this.config.reachDistance, player.getBlockInteractionRange());
        if (!isAirOrPassable(world, aimingPos) || !this.blockBreaker.canReach(aimingPos, dMax)) {
            this.expRepairCooldown = 0;
            AutoMineMovementHelper.preventJump = false;
            return false;
        }
        AutoMineMovementHelper.preventJump = true;
        int i = this.expRepairCooldown + 1;
        this.expRepairCooldown = i;
        if (i > 1200) {
            this.expRepairCooldown = 0;
            this.failCountMap.put(aimingPos.toImmutable(), 600);
            this.blockBreaker.cancel();
            this.pendingFluidPos = null;
            this.eatFoodCooldown++;
            AutoMineMovementHelper.preventJump = false;
            stopWithError("§eô tại " + aimingPos.toShortString() + " không vỡ sau 60s — tạm để đó, xoay cách khác");
            setDigTask(AutoMineDigTask.REPOSITION);
            return false;
        }
        setMiningTarget(aimingPos, dMax);
        return true;
    }

    public void aimAtTarget(ClientPlayerEntity player, BlockPos pos) {
        int i;
        int i2;
        if (this.plan == null || this.plan.areLayerFacesDone() || !player.isOnGround()) {
            return;
        }
        Vec3d playerPosVec = player.getEyePos();
        double deltaX = (((double) pos.getX()) + 0.5d) - player.getX();
        double deltaZ = (((double) pos.getZ()) + 0.5d) - player.getZ();
        double dHypot = Math.hypot(deltaX, deltaZ);
        if (Math.abs((((double) pos.getY()) + 0.5d) - playerPosVec.y) > 1.6d) {
            return;
        }
        if (!(pos.getY() >= player.getBlockY()) && dHypot < 1.1d) {
            return;
        }
        ClientWorld world = this.client.world;
        if (world != null) {
            if (deltaX > 0.2d) {
                i = 1;
            } else {
                i = deltaX < -0.2d ? -1 : 0;
            }
            int i3 = i;
            if (deltaZ > 0.2d) {
                i2 = 1;
            } else {
                i2 = deltaZ < -0.2d ? -1 : 0;
            }
            int i4 = i2;
            BlockPos playerPos = player.getBlockPos();
            if (i3 != 0 && !AutoMineBlockHelper.walkableOn(world, playerPos.add(i3, -1, 0))) {
                return;
            }
            if (i4 != 0 && !AutoMineBlockHelper.walkableOn(world, playerPos.add(0, -1, i4))) {
                return;
            }
            if (i3 != 0 && i4 != 0 && !AutoMineBlockHelper.walkableOn(world, playerPos.add(i3, -1, i4))) {
                return;
            }
        }
        double radians = Math.toRadians(player.getYaw());
        double dSin = Math.sin(radians);
        double dCos = Math.cos(radians);
        double d = (deltaZ * dCos) - (deltaX * dSin);
        double d2 = (deltaZ * dSin) + (deltaX * dCos);
        this.movementHelper.set(normalizeAnglePitch(d), (Math.abs(d2) > Math.abs(d) ? 1 : (Math.abs(d2) == Math.abs(d) ? 0 : -1)) > 0 ? normalizeAnglePitch(d2) : 0.0f, false, this.config.allowSprint && (dHypot > 2.2d ? 1 : (dHypot == 2.2d ? 0 : -1)) > 0);
    }

    public boolean isCrosshairAligned(ClientPlayerEntity player, BlockPos pos) {
        if (!player.isOnGround() || this.plan == null) {
            return false;
        }
        double playerZ = (this.plan.travelAxis == Direction.Axis.X ? player.getZ() : player.getX()) - (((double) this.plan.aimCross()) + 0.5d);
        double dAbs = Math.abs(playerZ);
        if (dAbs <= 0.15d) {
            return false;
        }
        double d = this.plan.travelAxis == Direction.Axis.X ? 0.0d : -playerZ;
        double d2 = this.plan.travelAxis == Direction.Axis.X ? -playerZ : 0.0d;
        World world = this.client.world;
        if (world != null && !AutoMineBlockHelper.walkableOn(world, new BlockPos((int) Math.floor(player.getX() + (Math.signum(d) * 0.35d)), (int) Math.floor(player.getY() - 0.1d), (int) Math.floor(player.getZ() + (Math.signum(d2) * 0.35d))))) {
            return false;
        }
        double radians = Math.toRadians(player.getYaw());
        double dSin = Math.sin(radians);
        double dCos = Math.cos(radians);
        this.movementHelper.set(normalizeAngleYaw((d2 * dCos) - (d * dSin)), normalizeAngleYaw((d2 * dSin) + (d * dCos)), false, false, false);
        this.statusDetail = "canh đúng tâm 9 ô (lệch " + String.format("%.2f", Double.valueOf(dAbs)) + ")";
        return true;
    }

    public static float normalizeAngleYaw(double d) {
        if (Math.abs(d) < 0.04d) {
            return 0.0f;
        }
        return d > 0.0d ? 1.0f : -1.0f;
    }

    public static float normalizeAnglePitch(double d) {
        if (Math.abs(d) < 0.18d) {
            return 0.0f;
        }
        return d > 0.0d ? 1.0f : -1.0f;
    }

    public boolean attemptBreakBlock(World world, ClientPlayerEntity player, BlockPos pos, double d) {
        BlockHitResult raycastHit = world.raycast(new RaycastContext(player.getEyePos(), Vec3d.ofCenter(pos), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (raycastHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos hitBlockPos = raycastHit.getBlockPos();
        if (hitBlockPos.equals(pos)) {
            if (this.selection.contains(pos) && isAirOrPassable(world, pos) && this.blockBreaker.canReach(pos, d)) {
                this.movementHelper.stop();
                setMiningTarget(pos, d);
                setDigTask(AutoMineDigTask.AIM_LOCK);
                this.statusDetail = "đào thẳng vào ô đích";
                return true;
            }
            return false;
        }
        if (!this.selection.contains(hitBlockPos) || !AutoMineBlockHelper.isBreakable(world, hitBlockPos) || !this.blockBreaker.canReach(hitBlockPos, d) || isBlockSolid(hitBlockPos)) {
            return false;
        }
        this.movementHelper.stop();
        setMiningTarget(hitBlockPos, d);
        this.statusDetail = "dọn chướng ngại để tới ô đào";
        return true;
    }

    public int getKhaiThongTravel() {
        if (this.plan == null) {
            return 0;
        }
        int iMinTravel = this.plan.minTravel();
        int iMaxTravel = this.plan.maxTravel();
        if (iMaxTravel - iMinTravel >= 2) {
            return this.plan.rowDirection() > 0 ? iMinTravel + 1 : iMaxTravel - 1;
        }
        return this.plan.currentTravel();
    }

    public void onBlockBroken(BlockPos pos) {
        this.blockQueue.clear();
        this.stuckCount = 0;
        this.pathingCooldown = 0;
        this.lastPlacedBlockPos = null;
        this.eatTargetPos = null;
        ClientPlayerEntity player = this.client.player;
        int khaiThongTravel = getKhaiThongTravel();
        int iAimCross = this.plan.aimCross();
        int coordY = this.plan.faceCenter().getY();
        if (player != null) {
            int iSignum = Integer.signum(iAimCross - (this.plan.travelAxis == Direction.Axis.X ? player.getBlockZ() : player.getBlockX()));
            if (iSignum == 0) {
                iSignum = (this.plan.layerIndex() % 2 == 0) != this.plan.crossMirrored ? 1 : -1;
            }
            BlockPos posAt = this.plan.posAt(khaiThongTravel, iAimCross - iSignum, coordY);
            BlockPos posAt2 = this.plan.posAt(khaiThongTravel, iAimCross, coordY);
            BlockPos posAt3 = this.plan.posAt(khaiThongTravel, iAimCross + iSignum, coordY);
            if (this.selection.contains(posAt)) {
                this.blockQueue.add(posAt.toImmutable());
            }
            if (this.selection.contains(posAt2)) {
                this.blockQueue.add(posAt2.toImmutable());
            }
            if (this.selection.contains(posAt3)) {
                this.blockQueue.add(posAt3.toImmutable());
                return;
            }
            return;
        }
        Direction direction = this.plan.travelAxis == Direction.Axis.X ? this.plan.rowDirection() > 0 ? Direction.EAST : Direction.WEST : this.plan.rowDirection() > 0 ? Direction.SOUTH : Direction.NORTH;
        for (int i = 0; i < 3; i++) {
            BlockPos eastPos = pos.offset(direction, i);
            if (this.selection.contains(eastPos)) {
                this.blockQueue.add(eastPos.toImmutable());
            }
        }
    }

    public boolean tickFluidManagement(World world, ClientPlayerEntity player) {
        while (!this.blockQueue.isEmpty() && !isAirOrPassable(world, this.blockQueue.peek())) {
            if (this.blockQueue.poll().equals(this.lastPlacedBlockPos)) {
                this.pathingCooldown++;
                this.lastPlacedBlockPos = null;
            }
            this.stuckCount = 0;
        }
        if (this.pathingCooldown >= 3) {
            this.blockQueue.clear();
            this.eatTargetPos = null;
            this.lastPlacedBlockPos = null;
            return false;
        }
        if (this.blockQueue.isEmpty()) {
            return false;
        }
        if (this.stuckCount > 600) {
            this.blockQueue.clear();
            this.eatTargetPos = null;
            stopWithError("§ebổ thẳng đầu dãy quá lâu — trả về lộ trình thường");
            return false;
        }
        BlockPos peekPos = this.blockQueue.peek();
        if (this.failCountMap.containsKey(peekPos)) {
            this.blockQueue.clear();
            this.eatTargetPos = null;
            this.lastPlacedBlockPos = null;
            stopWithError("§eô bổ thẳng không vỡ — trả về lộ trình thường");
            return false;
        }
        double dMax = Math.max(this.config.reachDistance, player.getBlockInteractionRange());
        int i = this.pathingCooldown + 1;
        if (this.blockBreaker.canReach(peekPos, dMax)) {
            this.lastPlacedBlockPos = peekPos;
            setMiningTarget(peekPos, dMax);
            this.statusDetail = "bổ thẳng " + i + "/3";
            return true;
        }
        if (attemptBreakBlock(world, player, peekPos, dMax)) {
            return true;
        }
        BlockPos pathPos = findAccessibleNeighbor(world, player, peekPos);
        if (pathPos == null) {
            pathPos = new BlockPos(peekPos.getX(), this.plan.layerBottom(), peekPos.getZ());
        }
        this.statusDetail = "tới chỗ bổ nhát " + i + "/3";
        if (!pathPos.equals(this.eatTargetPos)) {
            this.eatTargetPos = pathPos.toImmutable();
            this.strategy.reset();
        }
        if (this.strategy.moveTo(pathPos) == AutoMinePathNode.BLOCKED) {
            this.visitedPositions.add(pathPos.toImmutable());
            this.eatTargetPos = null;
            this.strategy.reset();
            return true;
        }
        return true;
    }

    public void markPositionVisited(World world, BlockPos pos) {
        BlockPos standPos;
        String str;
        double playerX;
        ClientPlayerEntity player = this.client.player;
        boolean z = player != null && AutoMineBlockHelper.walkableOn(world, player.getBlockPos().down());
        double d = 0.0d;
        if (player != null && this.plan != null) {
            double dAimCross = ((double) this.plan.aimCross()) + 0.5d;
            if (this.plan.travelAxis == Direction.Axis.X) {
                playerX = player.getZ();
            } else {
                playerX = player.getX();
            }
            d = playerX - dAimCross;
        }
        double reach = player != null ? Math.max(this.config.reachDistance, player.getBlockInteractionRange()) : this.config.reachDistance;

        // 1. Direct reach to target block: stop and mine immediately
        if (player != null && !this.strategy.isClimbing() && this.blockBreaker.canReach(pos, reach)) {
            if (this.strategy.isBridging()) {
                this.strategy.stopBridging();
            }
            this.movementHelper.stop();
            setDigTask(AutoMineDigTask.AIM_LOCK);
            return;
        }

        // 2. Opportunistic mining: check if ANY block in the current 3x3 face is reachable right now
        if (player != null && !this.strategy.isClimbing() && this.plan != null) {
            for (BlockPos cell : this.plan.faceCells()) {
                if (isAirOrPassable(world, cell) && this.blockBreaker.canReach(cell, reach)) {
                    if (this.strategy.isBridging()) {
                        this.strategy.stopBridging();
                    }
                    this.movementHelper.stop();
                    setMiningTarget(cell, reach);
                    setDigTask(AutoMineDigTask.AIM_LOCK);
                    return;
                }
            }
        }

        // 3. Center alignment if already near center
        if (player != null && z && !this.strategy.isClimbing() && this.blockBreaker.canReach(pos, reach) && Math.abs(d) <= 0.35d) {
            if (this.strategy.isBridging()) {
                this.strategy.stopBridging();
            }
            if (isCrosshairAligned(player, pos)) {
                this.pendingFluidPos = null;
                return;
            }
            this.movementHelper.stop();
            this.foodSearchCooldown = 0;
            setDigTask(AutoMineDigTask.AIM_LOCK);
            return;
        }

        if (this.scaffoldPlaced) {
            standPos = this.plan.entryPos();
        } else {
            int iMinTravel = this.plan.minTravel();
            int iMaxTravel = this.plan.maxTravel();
            if (iMaxTravel - iMinTravel >= 2 && (this.plan.currentTravel() == iMinTravel || this.plan.currentTravel() == iMaxTravel)) {
                standPos = this.plan.posAt(getKhaiThongTravel(), this.plan.aimCross(), this.plan.layerBottom());
            } else {
                standPos = this.plan.standPos();
            }
        }
        AutoMinePathNode autoMinePathNodeMoveTo = this.strategy.moveTo(standPos);
        if (autoMinePathNodeMoveTo == AutoMinePathNode.MOVING) {
            if (this.strategy.isClimbing()) {
                str = "đang leo lên";
            } else if (this.strategy.isBridging()) {
                str = "đang bắc cầu";
            } else {
                str = this.scaffoldPlaced ? "đào giếng vào mặt" : "tới chỗ đứng";
            }
            this.statusDetail = str;
            this.pendingFluidPos = null;
            return;
        }
        if (autoMinePathNodeMoveTo == AutoMinePathNode.BLOCKED) {
            if (player != null && attemptBreakBlock(world, player, pos, reach)) {
                return;
            }
            if (player != null && this.plan != null) {
                for (BlockPos cell : this.plan.faceCells()) {
                    if (isAirOrPassable(world, cell) && this.blockBreaker.canReach(cell, reach)) {
                        this.movementHelper.stop();
                        setMiningTarget(cell, reach);
                        setDigTask(AutoMineDigTask.AIM_LOCK);
                        return;
                    }
                }
            }
            if (this.foodSearchCooldown == 0 && !this.scaffoldPlaced) {
                this.foodSearchCooldown = 3;
                this.scaffoldPlaced = true;
                this.strategy.reset();
                this.statusDetail = "đào 9 ô trước mặt";
                return;
            }
            this.statusDetail = "không tới được chỗ đứng — tìm chỗ khác";
            setDigTask(AutoMineDigTask.REPOSITION);
            return;
        }
        this.movementHelper.stop();
        this.foodSearchCooldown = 0;
        setDigTask(AutoMineDigTask.AIM_LOCK);
    }

    public void recordFailAttempt(World world, BlockPos pos) {
        this.movementHelper.stop();
        this.pendingFluidPos = null;
        if (!isAirOrPassable(world, pos)) {
            return;
        }
        ClientPlayerEntity player = this.client.player;
        double reach = player != null ? Math.max(this.config.reachDistance, player.getBlockInteractionRange()) : this.config.reachDistance;
        if (!this.blockBreaker.aimOnly(pos, reach)) {
            setDigTask(AutoMineDigTask.REPOSITION);
            return;
        }
        if (queueBlock(pos)) {
            setDigTask(AutoMineDigTask.DIG_LOCKED);
        } else {
            this.statusDetail = this.scaffoldWaitTicks > 200 ? "ngắm mãi chưa được (" + (this.scaffoldWaitTicks / 20) + "s)" : "ngắm ô đào";
        }
    }

    public void recordRetryAttempt(World world, BlockPos pos) {
        ClientPlayerEntity player = this.client.player;
        if (player == null) {
            return;
        }
        double reach = Math.max(this.config.reachDistance, player.getBlockInteractionRange());
        if (this.blockBreaker.canReach(pos, reach)) {
            if (this.strategy.isBridging()) {
                this.strategy.stopBridging();
            }
            setDigTask(AutoMineDigTask.AIM_LOCK);
            return;
        }
        if (this.plan != null) {
            for (BlockPos cell : this.plan.faceCells()) {
                if (isAirOrPassable(world, cell) && this.blockBreaker.canReach(cell, reach)) {
                    if (this.strategy.isBridging()) {
                        this.strategy.stopBridging();
                    }
                    this.movementHelper.stop();
                    setMiningTarget(cell, reach);
                    setDigTask(AutoMineDigTask.AIM_LOCK);
                    return;
                }
            }
        }
        if (this.scaffoldPos == null) {
            this.scaffoldPos = findAccessibleNeighbor(world, player, pos);
            if (this.scaffoldPos == null) {
                this.statusDetail = "không có chỗ đứng nào thấy tâm — tự khoan vào cột tâm";
                this.visitedPositions.clear();
                this.foodSearchCooldown = 3;
                this.scaffoldPlaced = true;
                this.strategy.reset();
                setDigTask(AutoMineDigTask.APPROACH);
                return;
            }
            this.strategy.reset();
        }
        this.statusDetail = "đi tìm chỗ đứng thấy tâm";
        AutoMinePathNode autoMinePathNodeMoveTo = this.strategy.moveTo(this.scaffoldPos);
        if (autoMinePathNodeMoveTo == AutoMinePathNode.ARRIVED) {
            if (this.strategy.isBridging()) {
                this.strategy.stopBridging();
            }
            this.visitedPositions.add(this.scaffoldPos);
            this.scaffoldPos = null;
            setDigTask(AutoMineDigTask.AIM_LOCK);
            return;
        }
        if (autoMinePathNodeMoveTo == AutoMinePathNode.BLOCKED) {
            if (this.strategy.isBridging()) {
                this.strategy.stopBridging();
            }
            this.visitedPositions.add(this.scaffoldPos);
            this.scaffoldPos = null;
        }
    }

    public void clearPositionHistory(World world, BlockPos pos) {
        this.movementHelper.stop();
        if (!isAirOrPassable(world, pos)) {
            this.blockBreaker.disarm();
            this.pendingFluidPos = null;
            return;
        }
        if (!queueBlock(pos)) {
            this.blockBreaker.disarm();
            this.pendingFluidPos = null;
            setDigTask(AutoMineDigTask.AIM_LOCK);
        } else {
            setMiningTarget(pos, Math.max(this.config.reachDistance, this.client.player != null ? this.client.player.getBlockInteractionRange() : this.config.reachDistance));
            if (this.pendingFluidPos == null) {
                this.statusDetail = "mất đường ngắm — tìm chỗ đứng khác";
                setDigTask(AutoMineDigTask.REPOSITION);
            } else {
                this.statusDetail = this.scaffoldWaitTicks > 200 ? "đào lâu (" + (this.scaffoldWaitTicks / 20) + "s) — vẫn đang đào" : "đào mặt " + this.plan.faceWidth() + "x" + this.plan.faceHeight();
            }
        }
    }

    public void updateStatusLine(String str) {
        if (AutoMineBlockFilter.hasBuildingBlock(this.client.player)) {
            this.hasActiveTarget = false;
        } else if (!this.hasActiveTarget) {
            this.hasActiveTarget = true;
            stopWithError(str);
        }
    }

    public boolean scanForOres(World world) {
        if (this.plan.areLayerFacesDone() || findNearestOrePos(world) == null) {
            return false;
        }
        return true;
    }

    public BlockPos findNearestOrePos(World world) {
        BlockPos faceCenterPos = this.plan.faceCenter();
        if (isAirOrPassable(world, faceCenterPos) && !this.failCountMap.containsKey(faceCenterPos)) {
            return faceCenterPos;
        }
        if (this.plan != null) {
            for (BlockPos cell : this.plan.faceCells()) {
                if (isAirOrPassable(world, cell) && !this.failCountMap.containsKey(cell)) {
                    return cell;
                }
            }
        }
        return null;
    }

    public void setPriorityTarget(BlockPos pos) {
        this.pendingFluidPos = pos.toImmutable();
    }

    public void clearPriorityTarget() {
        resetStrategyState();
        if (this.strategy != null) {
            this.strategy.reset();
        }
    }

    public void resetStrategyState() {
        this.pendingFluidPos = null;
        this.blockBreaker.cancel();
    }

    public void setLayerTarget(BlockPos pos) {
        ClientWorld world = this.client.world;
        if (world == null) {
            return;
        }
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                for (int i3 = -1; i3 <= 1; i3++) {
                    BlockPos subPos = pos.add(i, i2, i3);
                    if (world.getBlockState(subPos).isAir()) {
                        this.ignoredBlockCache.add(subPos);
                    }
                }
            }
        }
    }

    public boolean isBlockTarget(net.minecraft.block.BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (!this.config.filterBlocks) {
            return true;
        }
        List<String> list = this.config.filterBlockList();
        if (list == null || list.isEmpty()) {
            return true;
        }
        String blockId = Registries.BLOCK.getId(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        String fullId = Registries.BLOCK.getId(state.getBlock()).toString().toLowerCase(Locale.ROOT);
        boolean matched = false;
        for (String filter : list) {
            if (filter.equals(blockId) || filter.equals(fullId)) {
                matched = true;
                break;
            }
        }
        return this.config.filterAsBlacklist ? !matched : matched;
    }

    public boolean isAirOrPassable(World world, BlockPos pos) {
        if (!this.selection.contains(pos) || this.placedBlockCache.contains(pos) || world.getBlockState(pos).isAir()) {
            return false;
        }
        if (!isBlockTarget(world.getBlockState(pos))) {
            return false;
        }
        return AutoMineBlockHelper.isBreakable(world, pos);
    }

    public boolean isBlockSolid(BlockPos pos) {
        ClientPlayerEntity player = this.client.player;
        if (player == null) {
            return false;
        }
        BlockPos playerPos = player.getBlockPos();
        if (pos.equals(playerPos.down())) {
            return true;
        }
        return pos.getY() < playerPos.getY() && (pos.getSquaredDistance(playerPos) > 4.0d ? 1 : (pos.getSquaredDistance(playerPos) == 4.0d ? 0 : -1)) <= 0 && this.strategy.placedCells().contains(pos);
    }

    public boolean isBreakableBlock(World world, BlockPos pos) {
        return isAirOrPassable(world, pos) && !isBlockSolid(pos);
    }

    public BlockPos findAccessibleNeighbor(World world, ClientPlayerEntity player, BlockPos pos) {
        BlockPos standPos;
        if (this.plan != null) {
            if (!this.blockQueue.isEmpty()) {
                standPos = this.plan.posAt(getKhaiThongTravel(), this.plan.aimCross(), this.plan.layerBottom());
            } else {
                standPos = this.plan.standPos();
            }
            if (!this.visitedPositions.contains(standPos) && AutoMineBlockHelper.standable(world, standPos) && this.blockBreaker.canReachFrom(standPos, pos, this.config.reachDistance)) {
                return standPos;
            }
        }
        int iAimCross = this.plan != null ? this.plan.aimCross() : -1;
        BlockPos pos2 = null;
        double d = Double.MAX_VALUE;
        for (int i = 0; i <= 2; i++) {
            if (i == 0) {
                for (int i2 = -4; i2 <= -1; i2++) {
                    BlockPos downPos = pos.up(i2);
                    if (this.selection.contains(downPos) && !this.visitedPositions.contains(downPos) && AutoMineBlockHelper.standable(world, downPos) && this.blockBreaker.canReachFrom(downPos, pos, this.config.reachDistance)) {
                        double valY = downPos.getSquaredDistance(player.getBlockPos()) + ((double) (Math.abs(downPos.getY() - player.getBlockY()) * 25));
                        if (valY < d) {
                            d = valY;
                            pos2 = downPos;
                        }
                    }
                }
            } else {
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    for (int i3 = -4; i3 <= 1; i3++) {
                        BlockPos westPos = pos.offset(direction, i).up(i3);
                        if (this.selection.contains(westPos) && !this.visitedPositions.contains(westPos) && AutoMineBlockHelper.standable(world, westPos) && this.blockBreaker.canReachFrom(westPos, pos, this.config.reachDistance)) {
                            int iAbs = iAimCross != -1 ? Math.abs(((this.plan == null || this.plan.travelAxis != Direction.Axis.X) ? westPos.getX() : westPos.getZ()) - iAimCross) : 0;
                            if (iAbs <= 1) {
                                double deltaX = westPos.getSquaredDistance(player.getBlockPos()) + ((double) (Math.abs(westPos.getY() - player.getBlockY()) * 25)) + ((double) (iAbs * 500));
                                if (deltaX < d) {
                                    d = deltaX;
                                    pos2 = westPos;
                                }
                            }
                        }
                    }
                }
            }
            if (pos2 != null) {
                return pos2;
            }
        }
        BlockPos northPos = pos.up();
        if (!this.visitedPositions.contains(northPos) && AutoMineBlockHelper.standable(world, northPos)) {
            return northPos;
        }
        return null;
    }

    public void stopWithError(String str) {
        if (this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§b[AutoMine] §r" + str), false);
        }
    }
}
