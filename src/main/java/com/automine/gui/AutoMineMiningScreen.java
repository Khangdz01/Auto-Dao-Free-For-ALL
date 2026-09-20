package com.automine.gui;

import com.automine.AutoMineClient;
import com.automine.core.AutoMineConfig;
import com.automine.core.AutoMineStaffDetector;
import com.automine.core.VdmFontRenderer;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;

/* JADX INFO: loaded from: AutoMineMiningScreen.class */
@Environment(EnvType.CLIENT)
public final class AutoMineMiningScreen extends Screen implements StyledScreen {
    public final Screen parentScreen;
    public TextFieldWidget staffInputField;
    public static final int ROW_HEIGHT = 13;
    public static final int LIST_Y = 74;
    public static final int BG_COLOR = -652337630;
    public static final int BORDER_COLOR = 352321535;

    public AutoMineMiningScreen(Screen parentScreen) {
        super(Text.literal("Danh Sách Staff"));
        this.parentScreen = parentScreen;
    }

    public static AutoMineConfig getConfig() {
        return AutoMineClient.CONFIG;
    }

    public int panelWidth() {
        return Math.min(420, this.width - (16));
    }

    public int panelLeft() {
        return (this.width - panelWidth()) / (2);
    }

    public int rowsPerColumn() {
        return Math.max(5, ((this.height - (74)) - (40)) / (13));
    }

    public void init() {
        int panelLeft = panelLeft();
        int panelWidth = panelWidth();
        int i = 70;
        int i2 = 84;
        this.staffInputField = new TextFieldWidget(this.textRenderer, panelLeft + (1), 40, ((panelWidth - i) - i2) - (10), 20, Text.literal("Tên staff..."));
        this.staffInputField.setMaxLength(24);
        this.staffInputField.setPlaceholder(Text.literal("§7Tên staff..."));
        addDrawableChild(this.staffInputField);
        addDrawableChild(AutoMineCustomButton.of((((panelLeft + panelWidth) - i) - i2) - (4), 40, i, 20, "§aThêm", customBtn1 -> {
            addStaff();
        }));
        addDrawableChild(AutoMineCustomButton.of((panelLeft + panelWidth) - i2, 40, i2, 20, "§eMặc định", customBtn2 -> {
            getConfig().resetStaff();
        }));
        addDrawableChild(AutoMineCustomButton.of(panelLeft + ((panelWidth - (100)) / (2)), this.height - (28), 100, 20, "Đóng", customBtn3 -> {
            close();
        }));
    }

    public void addStaff() {
        if (getConfig().addStaff(this.staffInputField.getText().trim())) {
            this.staffInputField.setText("");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void render(DrawContext context, int i, int i2, float f) {
        super.render(context, i, i2, f);
        int panelLeft = panelLeft();
        int panelWidth = panelWidth();
        VdmFontRenderer.roundedRect(context, panelLeft - (8), 12, panelWidth + (16), this.height - (24), 8, -652337630);
        VdmFontRenderer.roundedBorder(context, panelLeft - (8), 12, panelWidth + (16), this.height - (24), 8, 1, -32768);
        context.drawText(this.textRenderer, this.title.getString(), panelLeft, 18, -1, true);
        List<String> listStaffList = getConfig().staffList();
        int screenHeight = rowsPerColumn();
        int i5 = (panelWidth - (8)) / (2);
        context.drawText(this.textRenderer, "§7Tổng: §f" + listStaffList.size() + " §7— chấm §axanh§7 = đang online", panelLeft, 62, -6577749, true);
        for (int i6 = 0; i6 < listStaffList.size() && i6 < screenHeight * (2); i6++) {
            String str = listStaffList.get(i6);
            int i7 = panelLeft + ((i6 / screenHeight) * (i5 + (8)));
            int i8 = (74) + ((i6 % screenHeight) * (13));
            int i9 = 0;
            Iterator<String> it = AutoMineStaffDetector.onlineStaff().iterator();
            while (it.hasNext()) {
                if (it.next().equalsIgnoreCase(str)) {
                    i9 = 1;
                    break;
                }
            }
            context.drawText(this.textRenderer, i9 != 0 ? "§a●" : "§8●", i7, i8, -1, true);
            context.drawText(this.textRenderer, VdmFontRenderer.ellipsize(this.textRenderer, str, i5 - (25)), i7 + (11), i8, i9 != 0 ? -1 : -3552303, true);
            int i10 = (i7 + i5) - (10);
            int i11 = (i < i10 - (2) || i > i10 + (8) || i2 < i8 - (1) || i2 > i8 + (10)) ? 0 : 1;
            TextRenderer textRenderer = this.textRenderer;
            int closeBtnColor = (i11 != 0) ? 0xFFFF3333 : 0xFF666A72;
            context.drawText(textRenderer, "X", i10, i8, closeBtnColor, true);
        }
        if (listStaffList.size() > screenHeight * (2)) {
            context.drawText(this.textRenderer, "§8+ " + (listStaffList.size() - (screenHeight * (2))) + " nữa…", panelLeft, (74) + (screenHeight * (13)) + (2), -7829368, true);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean mouseClicked(Click click, boolean z) {
        List<String> listStaffList = getConfig().staffList();
        int screenHeight = rowsPerColumn();
        int panelWidth = (panelWidth() - (8)) / (2);
        int panelLeft = panelLeft();
        for (int i = 0; i < listStaffList.size() && i < screenHeight * (2); i++) {
            int i2 = ((panelLeft + ((i / screenHeight) * (panelWidth + (8)))) + panelWidth) - (10);
            int i3 = (74) + ((i % screenHeight) * (13));
            if (click.x() >= i2 - (2) && click.x() <= i2 + (8) && click.y() >= i3 - (1) && click.y() <= i3 + (10)) {
                getConfig().removeStaff(listStaffList.get(i));
                return true;
            }
        }
        return super.mouseClicked(click, z);
    }

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parentScreen);
        }
    }
}
