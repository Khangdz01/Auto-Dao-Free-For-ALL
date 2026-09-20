package com.automine.security;

import com.automine.security.SecurePayloadLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;

public class LicenseManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File LICENSE_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "automine_license.json");
    private static LicenseData currentData = new LicenseData();
    private static boolean authorized = true;
    private static String statusMessage = "Bản quyền vĩnh viễn (Wall Protected / Offline)";
    private static long expireTimeHolder = 0L;
    private static boolean destroyed = false;

    public static LicenseData getCurrentData() {
        return currentData;
    }

    public static String getSavedDiscordId() {
        return currentData != null ? LicenseManager.currentData.discordId : "";
    }

    public static String getServerUrl() {
        return "http://127.0.0.1";
    }

    public static void init() {
        LicenseManager.loadConfig();
        authorized = true;
        destroyed = false;
        expireTimeHolder = 0L;
        statusMessage = "Bản quyền vĩnh viễn (Wall Protected / Offline)";
        SecurePayloadLoader.loadLocalCore();
    }

    public static void loadConfig() {
        if (LICENSE_FILE.exists()) {
            try (FileReader fileReader = new FileReader(LICENSE_FILE);){
                LicenseData licenseData = (LicenseData)GSON.fromJson((Reader)fileReader, LicenseData.class);
                if (licenseData != null) {
                    currentData = licenseData;
                }
            }
            catch (Exception exception) {
                currentData = new LicenseData();
            }
        }
    }

    public static void saveConfig(String string, String string2) {
        LicenseManager.currentData.discordId = string != null ? string.trim() : "";
        LicenseManager.currentData.licenseKey = string2 != null ? string2.trim() : "";
        try (FileWriter fileWriter = new FileWriter(LICENSE_FILE);){
            GSON.toJson((Object)currentData, (Appendable)fileWriter);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static CompletableFuture<Boolean> validateAsync() {
        authorized = true;
        destroyed = false;
        expireTimeHolder = 0L;
        statusMessage = "Bản quyền vĩnh viễn (Wall Protected / Offline)";
        SecurePayloadLoader.loadLocalCore();
        return CompletableFuture.completedFuture(true);
    }

    public static void triggerDestruction(String string) {
        // Wall Protect: Defused remote kill-switch and owner destruction trigger.
    }

    public static boolean isAuthorized() {
        return true;
    }

    public static String getStatusMessage() {
        return statusMessage;
    }

    public static class LicenseData {
        public String discordId = "WallProtected";
        public String licenseKey = "PERMANENT-OFFLINE-KEY";
    }
}
