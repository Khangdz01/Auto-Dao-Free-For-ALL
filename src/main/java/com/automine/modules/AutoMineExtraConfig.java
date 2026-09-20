package com.automine.modules;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class AutoMineExtraConfig {
    private static final String FILE_NAME = "autodao_extra.properties";

    public static void load() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File file = configDir.resolve(FILE_NAME).toFile();
            if (!file.exists()) {
                save(); // Write defaults
                return;
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            }

            if (props.containsKey("autosell.enabled")) {
                AutoSellModule.setEnabled(Boolean.parseBoolean(props.getProperty("autosell.enabled")));
            }
            if (props.containsKey("autosell.command")) {
                AutoSellModule.setCommand(props.getProperty("autosell.command"));
            }
            if (props.containsKey("autosell.threshold")) {
                try {
                    AutoSellModule.setThreshold(Integer.parseInt(props.getProperty("autosell.threshold")));
                } catch (NumberFormatException ignored) {}
            }

            if (props.containsKey("autodrop.enabled")) {
                AutoDropModule.setEnabled(Boolean.parseBoolean(props.getProperty("autodrop.enabled")));
            }
            if (props.containsKey("autodrop.junk")) {
                String junkStr = props.getProperty("autodrop.junk");
                AutoDropModule.setJunkListFromString(junkStr);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void save() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File file = configDir.resolve(FILE_NAME).toFile();

            Properties props = new Properties();
            props.setProperty("autosell.enabled", String.valueOf(AutoSellModule.isEnabled()));
            props.setProperty("autosell.command", AutoSellModule.getCommand());
            props.setProperty("autosell.threshold", String.valueOf(AutoSellModule.getThreshold()));
            props.setProperty("autodrop.enabled", String.valueOf(AutoDropModule.isEnabled()));
            props.setProperty("autodrop.junk", AutoDropModule.getJunkListAsString());

            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, "AutoDao Extra Features Configuration");
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
