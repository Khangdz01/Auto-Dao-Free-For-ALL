package com.automine.core;

import com.automine.gui.LicenseScreen;
import com.automine.security.LicenseManager;
import com.automine.security.SecurePayloadLoader;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class AutoMineModMenu
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {
            if (LicenseManager.isAuthorized() && SecurePayloadLoader.getCore() != null) {
                return SecurePayloadLoader.getCore().createMainScreen(parentScreen);
            }
            return new LicenseScreen(parentScreen);
        };
    }
}

