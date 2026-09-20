package com.automine.core;

import com.automine.AutoMineClient;
import com.automine.gui.AutoMineMainScreen;
import com.automine.gui.AutoMineSpotifyScreen;
import com.automine.gui.AutoMineStaffScreen;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public final class AutoMineCommands {

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, screenTexts) -> {
            // Standalone shortcut commands
            commandDispatcher.register(ClientCommandManager.literal("sel").executes(ctx -> {
                say(getSelection().describe());
                return 1;
            }).then(ClientCommandManager.literal("1").executes(ctx -> setPosCommand(1)))
              .then(ClientCommandManager.literal("2").executes(ctx -> setPosCommand(2)))
              .then(ClientCommandManager.literal("pos1").executes(ctx -> setPosCommand(1)))
              .then(ClientCommandManager.literal("pos2").executes(ctx -> setPosCommand(2)))
              .then(ClientCommandManager.literal("clear").executes(ctx -> {
                  getSelection().clear();
                  say("đã xoá vùng chọn");
                  return 1;
              })).then(ClientCommandManager.literal("info").executes(ctx -> {
                  printInfo();
                  return 1;
              })));

            commandDispatcher.register(ClientCommandManager.literal("start").executes(ctx -> startMining()));
            commandDispatcher.register(ClientCommandManager.literal("stop").executes(ctx -> {
                getEngine().stop();
                say("đã dừng");
                return 1;
            }));
            commandDispatcher.register(ClientCommandManager.literal("pause").executes(ctx -> {
                getEngine().pause();
                say("tạm dừng — gõ /resume để chạy tiếp");
                return 1;
            }));
            commandDispatcher.register(ClientCommandManager.literal("resume").executes(ctx -> {
                getEngine().resume();
                if (getEngine().state() == AutoMineState.RUNNING) {
                    say("chạy tiếp");
                } else {
                    sayError("không có gì để chạy tiếp — dùng /start");
                }
                return 1;
            }));
            commandDispatcher.register(ClientCommandManager.literal("menu").executes(ctx -> {
                openMenu();
                return 1;
            }));
            commandDispatcher.register(ClientCommandManager.literal("freecam").executes(ctx -> {
                AutoMineFreecam.toggle(MinecraftClient.getInstance());
                return 1;
            }));
            commandDispatcher.register(ClientCommandManager.literal("tiktok").executes(ctx -> {
                new AutoMineSpotifyScreen(null).openUrlInBrowser("https://www.tiktok.com");
                say("§aĐã mở TikTok overlay!");
                return 1;
            }));

            // Main commands with all subcommands & aliases:
            // /automine, /autodao, /dao, /khangdzlaanh
            String[] aliases = new String[] {
                AutoMineStaffDetector.MOD_ID, // "automine"
                "autodao",
                "dao",
                "khangdzlaanh"
            };

            for (String alias : aliases) {
                commandDispatcher.register(buildBaseCommand(alias));
            }
        });
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildBaseCommand(String name) {
        return ClientCommandManager.literal(name)
            .executes(ctx -> {
                openMenu();
                return 1;
            })
            .then(ClientCommandManager.literal("menu").executes(ctx -> {
                openMenu();
                return 1;
            }))
            .then(ClientCommandManager.literal("start").executes(ctx -> startMining()))
            .then(ClientCommandManager.literal("stop").executes(ctx -> {
                getEngine().stop();
                say("đã dừng");
                return 1;
            }))
            .then(ClientCommandManager.literal("pause").executes(ctx -> {
                getEngine().pause();
                say("tạm dừng — gõ /resume để chạy tiếp");
                return 1;
            }))
            .then(ClientCommandManager.literal("resume").executes(ctx -> {
                getEngine().resume();
                if (getEngine().state() == AutoMineState.RUNNING) {
                    say("chạy tiếp");
                } else {
                    sayError("không có gì để chạy tiếp — dùng /start");
                }
                return 1;
            }))
            .then(ClientCommandManager.literal("1").executes(ctx -> setPosCommand(1)))
            .then(ClientCommandManager.literal("2").executes(ctx -> setPosCommand(2)))
            .then(ClientCommandManager.literal("pos1").executes(ctx -> setPosCommand(1)))
            .then(ClientCommandManager.literal("pos2").executes(ctx -> setPosCommand(2)))
            .then(ClientCommandManager.literal("clear").executes(ctx -> {
                getSelection().clear();
                say("đã xoá vùng chọn");
                return 1;
            }))
            .then(ClientCommandManager.literal("info").executes(ctx -> {
                printInfo();
                return 1;
            }))
            .then(ClientCommandManager.literal("sel")
                .executes(ctx -> {
                    say(getSelection().describe());
                    return 1;
                })
                .then(ClientCommandManager.literal("1").executes(ctx -> setPosCommand(1)))
                .then(ClientCommandManager.literal("2").executes(ctx -> setPosCommand(2)))
                .then(ClientCommandManager.literal("pos1").executes(ctx -> setPosCommand(1)))
                .then(ClientCommandManager.literal("pos2").executes(ctx -> setPosCommand(2)))
                .then(ClientCommandManager.literal("clear").executes(ctx -> {
                    getSelection().clear();
                    say("đã xoá vùng chọn");
                    return 1;
                }))
                .then(ClientCommandManager.literal("info").executes(ctx -> {
                    printInfo();
                    return 1;
                })))
            .then(ClientCommandManager.literal("bg").executes(ctx -> {
                AutoMineClient.CONFIG.backgroundMining = !AutoMineClient.CONFIG.backgroundMining;
                AutoMineClient.CONFIG.save();
                say("Đào xuyên GUI / Unfocus: " + (AutoMineClient.CONFIG.backgroundMining ? "§aBẬT" : "§cTẮT"));
                return 1;
            }))
            .then(ClientCommandManager.literal("freecam").executes(ctx -> {
                AutoMineFreecam.toggle(MinecraftClient.getInstance());
                return 1;
            }))
            .then(ClientCommandManager.literal("tiktok").executes(ctx -> {
                new AutoMineSpotifyScreen(null).openUrlInBrowser("https://www.tiktok.com");
                say("§aĐã mở TikTok overlay!");
                return 1;
            }))
            .then(ClientCommandManager.literal("web").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> mc.setScreen(new AutoMineSpotifyScreen(null)));
                return 1;
            }))
            .then(ClientCommandManager.literal("youtube").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> mc.setScreen(new AutoMineSpotifyScreen(null)));
                return 1;
            }))
            .then(ClientCommandManager.literal("spotify").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> mc.setScreen(new AutoMineStaffScreen(null)));
                return 1;
            }))
            .then(ClientCommandManager.literal("music").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> mc.setScreen(new AutoMineStaffScreen(null)));
                return 1;
            }))
            .then(ClientCommandManager.literal("friend")
                .then(ClientCommandManager.literal("list").executes(ctx -> {
                    say("Danh sách bạn bè (" + AutoMineClient.CONFIG.friendList().size() + "): §f" + String.join(", ", AutoMineClient.CONFIG.friendList()));
                    return 1;
                }))
                .then(ClientCommandManager.literal("clear").executes(ctx -> {
                    AutoMineClient.CONFIG.friendNames = "";
                    AutoMineClient.CONFIG.save();
                    say("§eĐã xoá toàn bộ danh sách bạn bè");
                    return 1;
                }))
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String fName = StringArgumentType.getString(ctx, "name");
                            if (AutoMineClient.CONFIG.addFriend(fName)) {
                                say("§aĐã thêm bạn bè: §f" + fName);
                            } else {
                                sayError("Bạn bè đã tồn tại: " + fName);
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String fName = StringArgumentType.getString(ctx, "name");
                            if (AutoMineClient.CONFIG.removeFriend(fName)) {
                                say("§eĐã xoá bạn bè: §f" + fName);
                            } else {
                                sayError("Không tìm thấy bạn bè: " + fName);
                            }
                            return 1;
                        }))))
            .then(ClientCommandManager.literal("filter")
                .then(ClientCommandManager.literal("toggle").executes(ctx -> {
                    AutoMineClient.CONFIG.filterBlocks = !AutoMineClient.CONFIG.filterBlocks;
                    AutoMineClient.CONFIG.save();
                    say("Lọc block: " + (AutoMineClient.CONFIG.filterBlocks ? "§aBẬT" : "§cTẮT"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("mode").executes(ctx -> {
                    AutoMineClient.CONFIG.filterAsBlacklist = !AutoMineClient.CONFIG.filterAsBlacklist;
                    AutoMineClient.CONFIG.save();
                    say("Chế độ lọc: " + (AutoMineClient.CONFIG.filterAsBlacklist ? "§eBlacklist (bỏ qua các block trong danh sách)" : "§aWhitelist (chỉ đào các block trong danh sách)"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("list").executes(ctx -> {
                    say("Danh sách lọc (" + AutoMineClient.CONFIG.filterBlockList().size() + " block, " + (AutoMineClient.CONFIG.filterAsBlacklist ? "Blacklist" : "Whitelist") + "): §f" + String.join(", ", AutoMineClient.CONFIG.filterBlockList()));
                    return 1;
                }))
                .then(ClientCommandManager.literal("clear").executes(ctx -> {
                    AutoMineClient.CONFIG.clearFilterBlocks();
                    say("§eĐã xoá sạch danh sách block lọc");
                    return 1;
                }))
                .then(ClientCommandManager.literal("hand").executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        ItemStack stack = client.player.getMainHandStack();
                        if (!stack.isEmpty()) {
                            Block block = Block.getBlockFromItem(stack.getItem());
                            if (block != null && block != Blocks.AIR) {
                                String id = Registries.BLOCK.getId(block).getPath();
                                if (AutoMineClient.CONFIG.addFilterBlock(id)) {
                                    say("§aĐã thêm block đang cầm vào bộ lọc: §f" + id);
                                } else {
                                    sayError("Block này đã có trong bộ lọc: " + id);
                                }
                                return 1;
                            }
                        }
                    }
                    sayError("Bạn không cầm block nào trên tay");
                    return 0;
                }))
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("block", StringArgumentType.string())
                        .executes(ctx -> {
                            String block = StringArgumentType.getString(ctx, "block");
                            if (AutoMineClient.CONFIG.addFilterBlock(block)) {
                                say("§aĐã thêm block vào bộ lọc: §f" + block);
                            } else {
                                sayError("Block này đã có trong bộ lọc: " + block);
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("block", StringArgumentType.string())
                        .executes(ctx -> {
                            String block = StringArgumentType.getString(ctx, "block");
                            if (AutoMineClient.CONFIG.removeFilterBlock(block)) {
                                say("§eĐã xoá block khỏi bộ lọc: §f" + block);
                            } else {
                                sayError("Không tìm thấy block trong bộ lọc: " + block);
                            }
                            return 1;
                        }))))
            .then(ClientCommandManager.literal("webhook")
                .then(ClientCommandManager.literal("toggle").executes(ctx -> {
                    AutoMineClient.CONFIG.webhookEnabled = !AutoMineClient.CONFIG.webhookEnabled;
                    AutoMineClient.CONFIG.save();
                    say("Báo cáo Webhook tiến độ: " + (AutoMineClient.CONFIG.webhookEnabled ? "§aBẬT" : "§cTẮT"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("test").executes(ctx -> {
                    say("Đang gửi tin nhắn test tới Discord webhook...");
                    AutoMineProgressNotifier.sendTest(MinecraftClient.getInstance());
                    return 1;
                })));
    }

    public static int setPosCommand(int i) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }
        BlockPos playerPos = player.getBlockPos();
        if (i == 1) {
            getSelection().setPos1(playerPos);
        } else {
            getSelection().setPos2(playerPos);
        }
        say("điểm " + i + " = " + playerPos.getX() + " " + playerPos.getY() + " " + playerPos.getZ() + (getSelection().isComplete() ? " · vùng " + getSelection().describe() + " — gõ /start" : ""));
        return 1;
    }

    public static int startMining() {
        String strStart = getEngine().start();
        if (strStart != null) {
            sayError(strStart);
            return 0;
        }
        AutoMinePlan autoMinePlanPlan = getEngine().plan();
        say("bắt đầu đào — " + getSelection().describe() + " · " + autoMinePlanPlan.layerCount() + " tầng cao " + AutoMineClient.CONFIG.layerHeight + " · mặt " + AutoMineClient.CONFIG.passWidth + "x" + AutoMineClient.CONFIG.layerHeight + " · trục " + (autoMinePlanPlan.travelAxis == Direction.Axis.X ? "X" : "Z") + " · §7" + AutoMineClient.VERSION);
        return 1;
    }

    public static void printInfo() {
        AutoMineSelection selection = getSelection();
        if (!selection.isComplete()) {
            say("vùng chọn: " + selection.describe());
        } else {
            say("vùng " + selection.describe());
            say("từ " + selection.minX() + " " + selection.minY() + " " + selection.minZ() + " đến " + selection.maxX() + " " + selection.maxY() + " " + selection.maxZ());
        }
    }

    public static void openMenu() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            client.setScreen(new AutoMineMainScreen(null));
        });
    }

    public static AutoMineSelection getSelection() {
        return AutoMineClient.SELECTION;
    }

    public static AutoMineEngine getEngine() {
        return AutoMineClient.ENGINE;
    }

    public static void say(String str) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§b[AutoMine] §r" + str), false);
        }
    }

    public static void sayError(String str) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c[AutoMine] " + str), false);
        }
    }
}
