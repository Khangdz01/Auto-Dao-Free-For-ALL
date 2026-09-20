package com.automine.gui;

import com.automine.AutoMineClient;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;

/* JADX INFO: loaded from: AutoMineConfigScreen.class */
@Environment(EnvType.CLIENT)
public final class AutoMineConfigScreen extends Screen implements StyledScreen {
    public final Screen parentScreen;
    public final String promptTitle;
    public final Supplier<String> getter;
    public final Consumer<String> setter;
    public TextFieldWidget textField;

    public AutoMineConfigScreen(Screen parentScreen, String str, Supplier<String> supplier, Consumer<String> consumer) {
        super(Text.literal(str));
        this.parentScreen = parentScreen;
        this.promptTitle = str;
        this.getter = supplier;
        this.setter = consumer;
    }

    public void init() {
        int iMin = Math.min(340, this.width - (40));
        int i = (this.width - iMin) / (2);
        int i2 = (this.height / (2)) - (10);
        this.textField = new TextFieldWidget(this.textRenderer, i, i2, iMin, 18, Text.literal(this.promptTitle));
        this.textField.setMaxLength(4096);
        this.textField.setText(this.getter.get());
        addDrawableChild(this.textField);
        setInitialFocus(this.textField);
        addDrawableChild(AutoMineCustomButton.of(i, i2 + (26), (iMin / (2)) - (3), 18, "Lưu", customBtn1 -> {
            applyChange();
        }));
        addDrawableChild(AutoMineCustomButton.of(i + (iMin / (2)) + (3), i2 + (26), (iMin / (2)) - (3), 18, "Huỷ", customBtn2 -> {
            close();
        }));
    }

    public void applyChange() {
        this.setter.accept(this.textField.getText().trim());
        AutoMineClient.CONFIG.save();
        close();
    }

    public void render(DrawContext context, int i, int i2, float f) {
        super.render(context, i, i2, f);
        int iMin = Math.min(340, this.width - (40));
        context.drawTextWithShadow(this.textRenderer, Text.literal(VdmTheme.ellipsize(this.textRenderer, this.promptTitle, iMin)), (this.width - iMin) / (2), (this.height / (2)) - (24), -1512722);
    }

    public void renderBackground(DrawContext context, int i, int i2, float f) {
        if (this.client == null || this.client.world == null) {
            super.renderBackground(context, i, i2, f);
        } else {
            applyBlur(context);
        }
        int iMin = Math.min(340, this.width - (40)) + (24);
        VdmTheme.group(context, (this.width - iMin) / (2), (this.height / (2)) - (36), iMin, 74);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.key() != (257) && keyInput.key() != (335)) {
            return super.keyPressed(keyInput);
        }
        applyChange();
        return true;
    }

    public void close() {
        if (this.client == null || this.parentScreen == null) {
            super.close();
        } else {
            this.client.setScreen(this.parentScreen);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean shouldPause() {
        return false;
    }
}
