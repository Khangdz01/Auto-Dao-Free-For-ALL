package com.automine.modules;

import com.automine.AutoMineClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AutoMineExtraClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load configuration
        AutoMineExtraConfig.load();

        // Register client tick hook for extra automation modules
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null && client.player != null) {
                AutoSellModule.tick(client);
                AutoDropModule.tick(client);
            }
        });

        // Intercept chat messages starting with '.' (stealth client-side commands)
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message != null && message.startsWith(".")) {
                handleCommand(message.substring(1).trim());
                return false; // Suppress from being sent to server
            }
            return true;
        });

        // Intercept slash commands /autodao or /ad
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
            if (command != null) {
                String trimmed = command.trim();
                if (trimmed.equalsIgnoreCase("autodao") || trimmed.toLowerCase().startsWith("autodao ")) {
                    String args = trimmed.length() > 7 ? trimmed.substring(7).trim() : "";
                    handleCommand(args);
                    return false; // Suppress from server
                }
                if (trimmed.equalsIgnoreCase("ad") || trimmed.toLowerCase().startsWith("ad ")) {
                    String args = trimmed.length() > 2 ? trimmed.substring(2).trim() : "";
                    handleCommand(args);
                    return false; // Suppress from server
                }
            }
            return true;
        });
    }

    public static void handleCommand(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (input == null || input.isEmpty() || input.equalsIgnoreCase("help")) {
            printHelp(client);
            return;
        }

        String[] parts = input.split("\\s+", 2);
        String subCmd = parts[0].toLowerCase();
        String rest = parts.length > 1 ? parts[1].trim() : "";

        switch (subCmd) {
            case "sell":
                if (rest.equalsIgnoreCase("on")) {
                    AutoSellModule.setEnabled(true);
                } else if (rest.equalsIgnoreCase("off")) {
                    AutoSellModule.setEnabled(false);
                } else {
                    AutoSellModule.setEnabled(!AutoSellModule.isEnabled());
                }
                AutoMineExtraConfig.save();
                AutoMineClient.say(client, "§6[AutoSell] §fĐã " + (AutoSellModule.isEnabled() ? "§aBẬT" : "§cTẮT") + "§f! (Lệnh: §e/" + AutoSellModule.getCommand() + "§f, Ngưỡng: §b" + AutoSellModule.getThreshold() + " ô§f)");
                break;

            case "sellcmd":
                if (rest.isEmpty()) {
                    AutoMineClient.say(client, "§6[AutoSell] §fLệnh hiện tại: §e/" + AutoSellModule.getCommand());
                } else {
                    String cmd = rest.startsWith("/") ? rest.substring(1) : rest;
                    AutoSellModule.setCommand(cmd);
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoSell] §fĐã đổi lệnh sell thành: §e/" + cmd);
                }
                break;

            case "threshold":
            case "sellslot":
                if (rest.isEmpty()) {
                    AutoMineClient.say(client, "§6[AutoSell] §fNgưỡng ô trống kích hoạt: §b" + AutoSellModule.getThreshold() + " ô");
                } else {
                    try {
                        int t = Integer.parseInt(rest);
                        AutoSellModule.setThreshold(Math.max(1, Math.min(10, t)));
                        AutoMineExtraConfig.save();
                        AutoMineClient.say(client, "§6[AutoSell] §fĐã đổi ngưỡng ô trống thành: §b" + AutoSellModule.getThreshold() + " ô");
                    } catch (NumberFormatException e) {
                        AutoMineClient.say(client, "§cNgưỡng phải là số nguyên (1 - 10)!");
                    }
                }
                break;

            case "drop":
                if (rest.isEmpty() || rest.equalsIgnoreCase("toggle")) {
                    AutoDropModule.setEnabled(!AutoDropModule.isEnabled());
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoDrop] §fĐã " + (AutoDropModule.isEnabled() ? "§aBẬT" : "§cTẮT") + "§f!");
                } else if (rest.equalsIgnoreCase("on")) {
                    AutoDropModule.setEnabled(true);
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoDrop] §fĐã §aBẬT§f!");
                } else if (rest.equalsIgnoreCase("off")) {
                    AutoDropModule.setEnabled(false);
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoDrop] §fĐã §cTẮT§f!");
                } else if (rest.toLowerCase().startsWith("add ")) {
                    String item = rest.substring(4).trim();
                    AutoDropModule.addJunk(item);
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoDrop] §fĐã thêm vào danh sách rác: §e" + item);
                } else if (rest.toLowerCase().startsWith("remove ")) {
                    String item = rest.substring(7).trim();
                    AutoDropModule.removeJunk(item);
                    AutoMineExtraConfig.save();
                    AutoMineClient.say(client, "§6[AutoDrop] §fĐã xóa khỏi danh sách rác: §e" + item);
                } else if (rest.equalsIgnoreCase("list")) {
                    AutoMineClient.say(client, "§6[AutoDrop] §fDanh sách rác (" + AutoDropModule.getJunkList().size() + "): §7" + AutoDropModule.getJunkListAsString());
                } else {
                    AutoMineClient.say(client, "§cLệnh không hợp lệ! Gõ .help để xem hướng dẫn.");
                }
                break;

            case "status":
                AutoMineClient.say(client, "§6=== [AutoDao Pro Extra] ===");
                AutoMineClient.say(client, "§7- AutoSell: " + (AutoSellModule.isEnabled() ? "§aBẬT" : "§cTẮT") + " §7(Lệnh: §e/" + AutoSellModule.getCommand() + "§7, Ngưỡng: §b" + AutoSellModule.getThreshold() + " ô§7)");
                AutoMineClient.say(client, "§7- AutoDrop: " + (AutoDropModule.isEnabled() ? "§aBẬT" : "§cTẮT") + " §7(Rác: §e" + AutoDropModule.getJunkList().size() + " loại§7)");
                break;

            default:
                printHelp(client);
                break;
        }
    }

    private static void printHelp(MinecraftClient client) {
        AutoMineClient.say(client, "§6=== [AutoDao Pro Extra - Hướng Dẫn] ===");
        AutoMineClient.say(client, "§e.sell §7hoặc §e/autodao sell §7- Bật/Tắt Auto-Sell");
        AutoMineClient.say(client, "§e.sellcmd <cmd> §7- Đổi lệnh sell (vd: §a.sellcmd sell all§7)");
        AutoMineClient.say(client, "§e.sellslot <số> §7- Ngưỡng ô trống tự sell (vd: §a.sellslot 2§7)");
        AutoMineClient.say(client, "§e.drop §7hoặc §e/autodao drop §7- Bật/Tắt Auto-Drop vứt rác");
        AutoMineClient.say(client, "§e.drop add <item> §7/ §e.drop remove <item> §7- Sửa list rác");
        AutoMineClient.say(client, "§e.drop list §7- Xem list rác");
        AutoMineClient.say(client, "§e.status §7- Xem trạng thái tổng thể");
    }
}
