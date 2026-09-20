package com.automine.gui;

import com.automine.core.IAutoMineCore;
import com.automine.gui.StyledScreen;
import com.automine.security.ClientEnvironment;
import com.automine.security.LicenseManager;
import com.automine.security.SecurePayloadLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;

@Environment(value=EnvType.CLIENT)
public class LicenseScreen
extends Screen
implements StyledScreen {
    private final Screen parent;
    private TextFieldWidget discordIdField;
    private TextFieldWidget licenseKeyField;
    private String feedbackMessage = "";
    private int feedbackColor = -1;

    public LicenseScreen(Screen parentScreen) {
        super(Text.literal((String)"AutoMine - Qu\u1ea3n L\u00fd B\u1ea3n Quy\u1ec1n"));
        this.parent = parentScreen;
    }

    protected void init() {
        super.init();
        int n = this.width / 2;
        int n2 = 45;
        this.discordIdField = new TextFieldWidget(this.textRenderer, n - 130, n2 + 30, 260, 20, Text.literal((String)"Discord ID"));
        this.discordIdField.setMaxLength(32);
        this.discordIdField.setText(LicenseManager.getCurrentData().discordId);
        this.discordIdField.setPlaceholder(Text.literal((String)"Nh\u1eadp Discord ID ho\u1eb7c Key..."));
        this.addDrawableChild(this.discordIdField);
        this.licenseKeyField = new TextFieldWidget(this.textRenderer, n - 130, n2 + 75, 260, 20, Text.literal((String)"License Key"));
        this.licenseKeyField.setMaxLength(64);
        this.licenseKeyField.setText(LicenseManager.getCurrentData().licenseKey);
        this.licenseKeyField.setPlaceholder(Text.literal((String)"Nh\u1eadp m\u00e3 Key b\u1ea3n quy\u1ec1n..."));
        this.addDrawableChild(this.licenseKeyField);
        this.addDrawableChild(ButtonWidget.builder(Text.literal((String)"\ud83d\udccb Sao ch\u00e9p HWID"), button -> {
            String string = ClientEnvironment.getClientFingerprint();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.keyboard != null) {
                client.keyboard.setClipboard(string);
                this.feedbackMessage = "\u00a7a\u0110\u00e3 sao ch\u00e9p m\u00e3 HWID v\u00e0o Clipboard!";
                this.feedbackColor = -11141291;
            }
        }).dimensions(n - 130, n2 + 110, 125, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal((String)"\u26a1 K\u00edch ho\u1ea1t Key"), button -> {
            String string = this.discordIdField.getText().trim();
            String string2 = this.licenseKeyField.getText().trim();
            if (string.isEmpty() && string2.isEmpty()) {
                this.feedbackMessage = "\u00a7cVui l\u00f2ng nh\u1eadp m\u00e3 Key ho\u1eb7c Discord ID!";
                this.feedbackColor = -43691;
                return;
            }
            if (string.isEmpty()) {
                string = string2;
            }
            if (string2.isEmpty()) {
                string2 = string;
            }
            LicenseManager.saveConfig(string, string2);
            this.feedbackMessage = "\u00a7e\u0110ang x\u00e1c th\u1ef1c v\u1edbi m\u00e1y ch\u1ee7...";
            this.feedbackColor = -171;
            LicenseManager.validateAsync().thenAccept(bl -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (bl.booleanValue()) {
                    IAutoMineCore iAutoMineCore = SecurePayloadLoader.getCore();
                    if (client != null) {
                        client.execute(() -> {
                            if (iAutoMineCore != null) {
                                client.setScreen(iAutoMineCore.createMainScreen(this.parent));
                            } else {
                                this.feedbackMessage = "\u00a7aK\u00edch ho\u1ea1t th\u00e0nh c\u00f4ng! Nh\u1ea5n V \u0111\u1ec3 m\u1edf Menu.";
                                this.feedbackColor = -11141291;
                            }
                        });
                    }
                } else if (client != null) {
                    client.execute(() -> {
                        this.feedbackMessage = "\u00a7c" + LicenseManager.getStatusMessage();
                        this.feedbackColor = -43691;
                    });
                }
            });
        }).dimensions(n + 5, n2 + 110, 125, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal((String)"Quay l\u1ea1i"), button -> this.close()).dimensions(n - 70, this.height - 35, 140, 20).build());
    }

    public void render(DrawContext context, int n, int n2, float f) {
        context.fill(0, 0, this.width, this.height, -300937200);
        int n3 = this.width / 2;
        int n4 = 45;
        context.fill(n3 - 145, n4 - 15, n3 + 145, n4 + 145, -300016098);
        context.drawTextWithShadow(this.textRenderer, "\u26a1 AUTOMINE - B\u1ea2N QUY\u1ec0N H\u1ec6 TH\u1ed0NG", n3 - this.textRenderer.getWidth("\u26a1 AUTOMINE - B\u1ea2N QUY\u1ec0N H\u1ec6 TH\u1ed0NG") / 2, n4 - 5, Short.MIN_VALUE);
        context.drawTextWithShadow(this.textRenderer, "Discord ID / User:", n3 - 130, n4 + 18, -5592406);
        context.drawTextWithShadow(this.textRenderer, "M\u00e3 License Key:", n3 - 130, n4 + 63, -5592406);
        if (!this.feedbackMessage.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, this.feedbackMessage, n3 - this.textRenderer.getWidth(this.feedbackMessage) / 2, n4 + 155, this.feedbackColor);
        }
        String string = "Tr\u1ea1ng th\u00e1i: " + (LicenseManager.isAuthorized() ? "\u00a7a\u0110\u00e3 k\u00edch ho\u1ea1t" : "\u00a7cCh\u01b0a k\u00edch ho\u1ea1t");
        context.drawTextWithShadow(this.textRenderer, string, n3 - this.textRenderer.getWidth(string) / 2, n4 + 172, -3355444);
        String string2 = "HWID: " + ClientEnvironment.getClientFingerprint().substring(0, Math.min(16, ClientEnvironment.getClientFingerprint().length())) + "...";
        context.drawTextWithShadow(this.textRenderer, string2, n3 - this.textRenderer.getWidth(string2) / 2, n4 + 187, -7829368);
        super.render(context, n, n2, f);
    }
}

