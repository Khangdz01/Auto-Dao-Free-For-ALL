package com.automine.gui;

import com.automine.AutoMineClient;
import com.automine.core.AutoMineConfig;
import com.automine.core.VdmFontRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class AutoMineBlockFilterScreen extends Screen implements StyledScreen {
    public final Screen parentScreen;

    public static class BlockEntry {
        public final Block block;
        public final String id;
        public final String displayName;
        public final ItemStack iconStack;
        public final boolean isOre;
        public final boolean isStone;
        public final boolean isWood;

        public BlockEntry(Block block, String id, String displayName) {
            this.block = block;
            this.id = id;
            this.displayName = displayName;
            this.iconStack = new ItemStack(block);
            String lowerId = id.toLowerCase(Locale.ROOT);
            this.isOre = lowerId.contains("ore") || lowerId.contains("debris") || lowerId.contains("raw_");
            this.isStone = lowerId.contains("stone") || lowerId.contains("deepslate") || lowerId.contains("granite")
                    || lowerId.contains("diorite") || lowerId.contains("andesite") || lowerId.contains("tuff")
                    || lowerId.contains("calcite") || lowerId.contains("blackstone") || lowerId.contains("basalt")
                    || lowerId.contains("sandstone") || lowerId.contains("obsidian") || lowerId.contains("netherrack");
            this.isWood = lowerId.contains("log") || lowerId.contains("wood") || lowerId.contains("stem") || lowerId.contains("hyphae");
        }
    }

    private static List<BlockEntry> ALL_BLOCKS = null;

    private TextFieldWidget searchField;
    private final List<BlockEntry> filteredRegistryBlocks = new ArrayList<>();
    private int leftScroll = 0;
    private int rightScroll = 0;
    private int selectedCategory = 0; // 0 = Tat ca, 1 = Quang (Ores), 2 = Da (Stones), 3 = Go (Wood)

    public AutoMineBlockFilterScreen(Screen parentScreen) {
        super(Text.literal("Quản Lý Lọc Block"));
        this.parentScreen = parentScreen;
        ensureBlockRegistryLoaded();
    }

    private static void ensureBlockRegistryLoaded() {
        if (ALL_BLOCKS != null) {
            return;
        }
        List<BlockEntry> list = new ArrayList<>();
        for (Block block : Registries.BLOCK) {
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR
                    || block == Blocks.BARRIER || block == Blocks.LIGHT || block == Blocks.STRUCTURE_VOID
                    || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK || block == Blocks.REPEATING_COMMAND_BLOCK
                    || block == Blocks.JIGSAW || block == Blocks.STRUCTURE_BLOCK) {
                continue;
            }
            Identifier identifier = Registries.BLOCK.getId(block);
            String id = identifier.getPath();
            String displayName = block.getName().getString();
            list.add(new BlockEntry(block, id, displayName));
        }

        // Sort: ores first, then alphabetical by name
        Collections.sort(list, (a, b) -> {
            if (a.isOre && !b.isOre) return -1;
            if (!a.isOre && b.isOre) return 1;
            return a.displayName.compareToIgnoreCase(b.displayName);
        });

        ALL_BLOCKS = Collections.unmodifiableList(list);
    }

    public static AutoMineConfig getConfig() {
        return AutoMineClient.CONFIG;
    }

    @Override
    public void init() {
        int panelW = Math.min(520, this.width - 24);
        int left = (this.width - panelW) / 2;
        int rightHalfX = left + (panelW / 2) + 6;
        int rightHalfW = (panelW / 2) - 6;

        // Top toggle buttons
        int topBtnY = 32;
        int tabW = 135;

        // Whitelist tab
        addDrawableChild(new AutoMineCustomButton(left, topBtnY, tabW, 20,
                Text.literal("★ Whitelist (Chỉ đào)"),
                btn -> {
                    getConfig().filterAsBlacklist = false;
                    getConfig().syncActiveFilter();
                    getConfig().save();
                },
                () -> !getConfig().filterAsBlacklist));

        // Blacklist tab
        addDrawableChild(new AutoMineCustomButton(left + tabW + 4, topBtnY, tabW, 20,
                Text.literal("⛔ Blacklist (Bỏ qua)"),
                btn -> {
                    getConfig().filterAsBlacklist = true;
                    getConfig().syncActiveFilter();
                    getConfig().save();
                },
                () -> getConfig().filterAsBlacklist));

        // Filter Enabled toggle
        addDrawableChild(new AutoMineCustomButton(left + (tabW * 2) + 8, topBtnY, 100, 20,
                Text.literal(getConfig().filterBlocks ? "§a● Lọc: BẬT" : "§c○ Lọc: TẮT"),
                btn -> {
                    getConfig().filterBlocks = !getConfig().filterBlocks;
                    getConfig().save();
                    btn.setMessage(Text.literal(getConfig().filterBlocks ? "§a● Lọc: BẬT" : "§c○ Lọc: TẮT"));
                },
                () -> getConfig().filterBlocks));

        // Close button top right
        addDrawableChild(AutoMineCustomButton.of(left + panelW - 70, topBtnY, 70, 20, "Đóng", btn -> close()));

        // Right side search box
        this.searchField = new TextFieldWidget(this.textRenderer, rightHalfX, 60, rightHalfW, 18, Text.literal("Tìm kiếm"));
        this.searchField.setMaxLength(64);
        this.searchField.setPlaceholder(Text.literal("§7Tìm block (vd: ore, diamond, stone)..."));
        this.searchField.setChangedListener(text -> updateFilter());
        addDrawableChild(this.searchField);

        // Category filter chips in right half
        int chipY = 82;
        int chipW = (rightHalfW - 9) / 4;
        addDrawableChild(new AutoMineCustomButton(rightHalfX, chipY, chipW, 16, Text.literal("Tất cả"),
                btn -> { selectedCategory = 0; updateFilter(); }, () -> selectedCategory == 0));
        addDrawableChild(new AutoMineCustomButton(rightHalfX + chipW + 3, chipY, chipW, 16, Text.literal("Quặng"),
                btn -> { selectedCategory = 1; updateFilter(); }, () -> selectedCategory == 1));
        addDrawableChild(new AutoMineCustomButton(rightHalfX + (chipW + 3) * 2, chipY, chipW, 16, Text.literal("Đá"),
                btn -> { selectedCategory = 2; updateFilter(); }, () -> selectedCategory == 2));
        addDrawableChild(new AutoMineCustomButton(rightHalfX + (chipW + 3) * 3, chipY, chipW, 16, Text.literal("Gỗ"),
                btn -> { selectedCategory = 3; updateFilter(); }, () -> selectedCategory == 3));

        // Left side action buttons
        int leftHalfX = left;
        int leftHalfW = (panelW / 2) - 6;
        int leftBtnY = 60;
        int lBtnW = (leftHalfW - 4) / 2;
        addDrawableChild(AutoMineCustomButton.of(leftHalfX, leftBtnY, lBtnW, 18, "+ Cầm trên tay", btn -> {
            if (this.client != null && this.client.player != null) {
                ItemStack stack = this.client.player.getMainHandStack();
                if (!stack.isEmpty()) {
                    Block block = Block.getBlockFromItem(stack.getItem());
                    if (block != null && block != Blocks.AIR) {
                        String id = Registries.BLOCK.getId(block).getPath();
                        getConfig().addFilterBlock(id);
                    }
                }
            }
        }));
        addDrawableChild(AutoMineCustomButton.of(leftHalfX + lBtnW + 4, leftBtnY, lBtnW, 18, "🗑 Xoá danh sách", btn -> {
            getConfig().clearFilterBlocks();
        }));

        updateFilter();
    }

    private void updateFilter() {
        this.filteredRegistryBlocks.clear();
        String query = (this.searchField != null) ? this.searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        if (ALL_BLOCKS == null) {
            ensureBlockRegistryLoaded();
        }

        for (BlockEntry entry : ALL_BLOCKS) {
            if (this.selectedCategory == 1 && !entry.isOre) continue;
            if (this.selectedCategory == 2 && !entry.isStone) continue;
            if (this.selectedCategory == 3 && !entry.isWood) continue;

            if (!query.isEmpty()) {
                boolean matchName = entry.displayName.toLowerCase(Locale.ROOT).contains(query);
                boolean matchId = entry.id.toLowerCase(Locale.ROOT).contains(query);
                if (!matchName && !matchId) {
                    continue;
                }
            }
            this.filteredRegistryBlocks.add(entry);
        }
        this.rightScroll = 0;
    }

    private int getListTop() {
        return 102;
    }

    private int getListBottom() {
        return this.height - 20;
    }

    private int getRowHeight() {
        return 22;
    }

    private int visibleRows() {
        return Math.max(1, (getListBottom() - getListTop()) / getRowHeight());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.world == null) {
            super.renderBackground(context, mouseX, mouseY, delta);
        } else {
            applyBlur(context);
        }

        int panelW = Math.min(520, this.width - 24);
        int left = (this.width - panelW) / 2;
        int listTop = getListTop();
        int listH = getListBottom() - listTop;
        int halfW = (panelW / 2) - 6;

        // Outer container backdrop
        VdmFontRenderer.roundedRect(context, left - 6, 8, panelW + 12, this.height - 16, 8, 0xCC111318);
        VdmFontRenderer.roundedBorder(context, left - 6, 8, panelW + 12, this.height - 16, 8, 1, 0x553C404B);

        // Left panel frame
        VdmFontRenderer.roundedRect(context, left, listTop - 18, halfW, listH + 20, 6, 0x88181A20);
        VdmFontRenderer.roundedBorder(context, left, listTop - 18, halfW, listH + 20, 6, 1, 0x44303440);

        // Right panel frame
        int rightX = left + halfW + 12;
        VdmFontRenderer.roundedRect(context, rightX, listTop - 4, halfW, listH + 6, 6, 0x88181A20);
        VdmFontRenderer.roundedBorder(context, rightX, listTop - 4, halfW, listH + 6, 6, 1, 0x44303440);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int panelW = Math.min(520, this.width - 24);
        int left = (this.width - panelW) / 2;
        int halfW = (panelW / 2) - 6;
        int rightX = left + halfW + 12;
        int listTop = getListTop();
        int rowH = getRowHeight();
        int visibleCount = visibleRows();

        // Screen title
        context.drawTextWithShadow(this.textRenderer, Text.literal("§l✦ BỘ LỌC BLOCK §7— Chọn block bằng nút [+] và [-]"), left, 14, 0xFFE0E0E0);

        // Left panel header: Whitelist or Blacklist
        boolean isBlacklist = getConfig().filterAsBlacklist;
        List<String> activeList = isBlacklist ? getConfig().blacklistBlockList() : getConfig().whitelistBlockList();

        String leftHeader = isBlacklist
                ? "§c⛔ Danh sách Blacklist §f(" + activeList.size() + ")"
                : "§a★ Danh sách Whitelist §f(" + activeList.size() + ")";
        context.drawTextWithShadow(this.textRenderer, Text.literal(leftHeader), left + 6, listTop - 14, 0xFFFFFFFF);

        String modeSub = isBlacklist ? "§8(Bỏ qua không đào các block này)" : "§8(Chỉ đào đúng các block này)";
        context.drawText(this.textRenderer, Text.literal(modeSub), left + 6, 84, 0xFF888888, false);

        // Render Left Panel Items (Configured blocks)
        if (activeList.isEmpty()) {
            context.drawText(this.textRenderer, Text.literal("§7Chưa có block nào."), left + 10, listTop + 10, 0xFFAAAAAA, false);
            context.drawText(this.textRenderer, Text.literal("§8Ấn [+] ở danh sách bên phải"), left + 10, listTop + 24, 0xFF777777, false);
            context.drawText(this.textRenderer, Text.literal("§8hoặc bấm [+ Cầm trên tay]"), left + 10, listTop + 38, 0xFF777777, false);
        } else {
            int maxLeftScroll = Math.max(0, activeList.size() - visibleCount);
            this.leftScroll = clamp(this.leftScroll, 0, maxLeftScroll);

            int endIdx = Math.min(activeList.size(), this.leftScroll + visibleCount);
            for (int i = this.leftScroll; i < endIdx; i++) {
                String blockId = activeList.get(i);
                int rowY = listTop + ((i - this.leftScroll) * rowH);

                boolean hovered = (mouseX >= left + 2 && mouseX <= left + halfW - 4 && mouseY >= rowY && mouseY < rowY + rowH - 2);

                // Row background
                int bg = hovered ? 0x44FFFFFF : 0x22FFFFFF;
                VdmFontRenderer.roundedRect(context, left + 2, rowY, halfW - 4, rowH - 2, 4, bg);

                // Block lookup
                Block block = Registries.BLOCK.get(Identifier.of("minecraft", blockId));
                ItemStack stack = (block != null && block != Blocks.AIR) ? new ItemStack(block) : ItemStack.EMPTY;

                // Render item icon
                if (!stack.isEmpty()) {
                    context.drawItem(stack, left + 5, rowY + 2);
                } else {
                    context.fill(left + 5, rowY + 2, left + 21, rowY + 18, 0x44666666);
                }

                // Name & ID
                String name = (block != null && block != Blocks.AIR) ? block.getName().getString() : blockId;
                String truncatedName = VdmFontRenderer.ellipsize(this.textRenderer, name, halfW - 60);
                context.drawText(this.textRenderer, truncatedName, left + 25, rowY + 3, hovered ? 0xFFFFFFFF : 0xFFDDDDDD, false);
                context.drawText(this.textRenderer, "§8" + blockId, left + 25, rowY + 12, 0xFF777777, false);

                // Red [-] remove button on right
                int btnX = left + halfW - 24;
                int btnY = rowY + 2;
                boolean btnHovered = (mouseX >= btnX && mouseX <= btnX + 18 && mouseY >= btnY && mouseY <= btnY + 16);

                int btnBg = btnHovered ? 0xFFFF3333 : 0x88CC3333;
                VdmFontRenderer.roundedRect(context, btnX, btnY, 18, 15, 3, btnBg);
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§f-"), btnX + 9, btnY + 3, 0xFFFFFFFF);
            }

            // Left scrollbar
            if (activeList.size() > visibleCount) {
                drawScrollbar(context, left + halfW - 3, listTop, getListBottom() - listTop, activeList.size(), visibleCount, this.leftScroll);
            }
        }

        // Render Right Panel Items (Available blocks)
        int rightListCount = this.filteredRegistryBlocks.size();
        int maxRightScroll = Math.max(0, rightListCount - visibleCount);
        this.rightScroll = clamp(this.rightScroll, 0, maxRightScroll);

        int rightEndIdx = Math.min(rightListCount, this.rightScroll + visibleCount);
        for (int i = this.rightScroll; i < rightEndIdx; i++) {
            BlockEntry entry = this.filteredRegistryBlocks.get(i);
            int rowY = listTop + ((i - this.rightScroll) * rowH);

            boolean hovered = (mouseX >= rightX + 2 && mouseX <= rightX + halfW - 4 && mouseY >= rowY && mouseY < rowY + rowH - 2);

            int bg = hovered ? 0x44FFFFFF : 0x22FFFFFF;
            VdmFontRenderer.roundedRect(context, rightX + 2, rowY, halfW - 4, rowH - 2, 4, bg);

            // Item icon
            if (!entry.iconStack.isEmpty()) {
                context.drawItem(entry.iconStack, rightX + 5, rowY + 2);
            }

            // Display name & ID
            String truncatedName = VdmFontRenderer.ellipsize(this.textRenderer, entry.displayName, halfW - 65);
            context.drawText(this.textRenderer, truncatedName, rightX + 25, rowY + 3, hovered ? 0xFFFFFFFF : 0xFFDDDDDD, false);
            context.drawText(this.textRenderer, "§8" + entry.id, rightX + 25, rowY + 12, 0xFF777777, false);

            boolean alreadyInList = activeList.contains(entry.id);
            int btnX = rightX + halfW - 24;
            int btnY = rowY + 2;

            if (alreadyInList) {
                // Dimmed checkmark
                context.drawText(this.textRenderer, Text.literal("§a✔"), btnX + 4, btnY + 3, 0xFF55FF55, true);
            } else {
                // Green [+] add button
                boolean btnHovered = (mouseX >= btnX && mouseX <= btnX + 18 && mouseY >= btnY && mouseY <= btnY + 16);
                int btnBg = btnHovered ? 0xFF22C55E : 0x8816A34A;
                VdmFontRenderer.roundedRect(context, btnX, btnY, 18, 15, 3, btnBg);
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§f+"), btnX + 9, btnY + 3, 0xFFFFFFFF);
            }
        }

        // Right scrollbar
        if (rightListCount > visibleCount) {
            drawScrollbar(context, rightX + halfW - 3, listTop, getListBottom() - listTop, rightListCount, visibleCount, this.rightScroll);
        }
    }

    private void drawScrollbar(DrawContext context, int barX, int top, int height, int total, int visible, int currentScroll) {
        int barH = Math.max(16, (height * visible) / total);
        int barY = top + (((height - barH) * currentScroll) / Math.max(1, total - visible));
        context.fill(barX, top, barX + 3, top + height, 0x33FFFFFF);
        context.fill(barX, barY, barX + 3, barY + barH, 0xAAFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean z) {
        if (super.mouseClicked(click, z)) {
            return true;
        }

        if (click.button() != 0) {
            return false;
        }

        int panelW = Math.min(520, this.width - 24);
        int left = (this.width - panelW) / 2;
        int halfW = (panelW / 2) - 6;
        int rightX = left + halfW + 12;
        int listTop = getListTop();
        int rowH = getRowHeight();
        int visibleCount = visibleRows();

        double mx = click.x();
        double my = click.y();

        // 1. Check click in Left Panel (Active list removal)
        boolean isBlacklist = getConfig().filterAsBlacklist;
        List<String> activeList = isBlacklist ? getConfig().blacklistBlockList() : getConfig().whitelistBlockList();

        if (mx >= left && mx <= left + halfW && my >= listTop && my <= getListBottom()) {
            int clickedRow = this.leftScroll + (int) ((my - listTop) / rowH);
            if (clickedRow >= 0 && clickedRow < activeList.size()) {
                String blockId = activeList.get(clickedRow);
                // Check if clicked the [-] button or anywhere on the row
                int rowY = listTop + ((clickedRow - this.leftScroll) * rowH);
                int btnX = left + halfW - 24;
                if (mx >= btnX - 2 && mx <= btnX + 22 && my >= rowY && my <= rowY + rowH) {
                    getConfig().removeFilterBlock(blockId);
                    return true;
                }
            }
        }

        // 2. Check click in Right Panel (Registry list addition)
        if (mx >= rightX && mx <= rightX + halfW && my >= listTop && my <= getListBottom()) {
            int clickedRow = this.rightScroll + (int) ((my - listTop) / rowH);
            if (clickedRow >= 0 && clickedRow < this.filteredRegistryBlocks.size()) {
                BlockEntry entry = this.filteredRegistryBlocks.get(clickedRow);
                int rowY = listTop + ((clickedRow - this.rightScroll) * rowH);
                int btnX = rightX + halfW - 24;
                if (mx >= btnX - 2 && mx <= btnX + 22 && my >= rowY && my <= rowY + rowH) {
                    if (!activeList.contains(entry.id)) {
                        getConfig().addFilterBlock(entry.id);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = Math.min(520, this.width - 24);
        int left = (this.width - panelW) / 2;
        int halfW = (panelW / 2) - 6;
        int rightX = left + halfW + 12;
        int visibleCount = visibleRows();

        if (mouseX >= left && mouseX <= left + halfW) {
            boolean isBlacklist = getConfig().filterAsBlacklist;
            List<String> activeList = isBlacklist ? getConfig().blacklistBlockList() : getConfig().whitelistBlockList();
            int maxLeft = Math.max(0, activeList.size() - visibleCount);
            this.leftScroll = clamp(this.leftScroll - (int) Math.signum(verticalAmount), 0, maxLeft);
            return true;
        }

        if (mouseX >= rightX && mouseX <= rightX + halfW) {
            int maxRight = Math.max(0, this.filteredRegistryBlocks.size() - visibleCount);
            this.rightScroll = clamp(this.rightScroll - (int) Math.signum(verticalAmount), 0, maxRight);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (this.searchField != null && this.searchField.isFocused()) {
            if (keyInput.key() == 256) { // ESC key un-focuses search
                this.searchField.setFocused(false);
                return true;
            }
        }
        return super.keyPressed(keyInput);
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
