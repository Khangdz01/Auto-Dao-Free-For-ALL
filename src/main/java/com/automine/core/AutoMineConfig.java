package com.automine.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AutoMineConfig {
    public int layerHeight = 3;
    public int passWidth = 3;
    public boolean allowSprint = true;
    public boolean allowPlace = true;
    public boolean sweepLayer = true;
    public double reachDistance = 4.5d;
    public boolean renderSelection = true;
    public boolean autoEat = true;
    public int autoEatThreshold = 2;
    public boolean expRepair = true;
    public int expRepairThreshold = 50;
    public boolean alertFluid = true;
    public String alertWebhook = "";
    public String alertDiscordId = "";
    public boolean spotifyHud = false;
    public int spotifyX = 5;
    public int spotifyY = 45;
    public String musicPlaylist = "37i9dQZEVXbLdGSmz6xilI";
    public String guiPanels = "";
    public boolean staffHud = true;
    public int staffHudX = -1;
    public int staffHudY = -1;
    public boolean autoSign = false;
    public int staffRadius = 10;
    public String signText = "Minh AFK dao da|Khong dung hack|Cam on staff <3";
    public String staffNames = "DrDonutt,Dough4,Fallerfly,Evxn,Ryuui,Shyalyy,OGsummer,ItsDefRealMe,LzouZMp5,Munkerlich,Chaon,Showered,PastaGamer,Bautiegar,bloodspulse,GsMusie,Frwost,FluffyMaster07,W1zoX_,Itszdeath,archivePedro,evify,NoahvdAa,Zababi,Nathan,Owen1212055";

    // --- New Features ---
    // 1. Target Block Filter
    public boolean filterBlocks = false;
    public String filterBlockList = "";
    public boolean filterAsBlacklist = false;
    public String whitelistBlockList = "";
    public String blacklistBlockList = "";

    // 2. Discord Webhook Progress Reporter
    public boolean webhookEnabled = false;
    public String webhookUrl = "";
    public boolean webhookMilestones = true;
    public boolean webhookStateChange = true;
    public int webhookIntervalMinutes = 5;
    public boolean webhookIncludeCoords = true;

    // 3. Friend Whitelist
    public String friendNames = "";

    // 4. Background & Unfocused Mining
    public boolean backgroundMining = true;

    // 5. Saved Box Area Selection
    public String selectionPos1 = "";
    public String selectionPos2 = "";

    // 6. Freecam Configuration
    public int freecamKey = 85;
    public int freecamSpeed = 1;

    public transient Path configPath;

    public static AutoMineConfig loadOrCreate(Path path) {
        AutoMineConfig autoMineConfig = new AutoMineConfig();
        autoMineConfig.configPath = path.resolve("automine.properties");
        if (Files.exists(autoMineConfig.configPath, new LinkOption[0])) {
            try {
                Iterator<String> it = Files.readAllLines(autoMineConfig.configPath).iterator();
                while (it.hasNext()) {
                    String strTrim = it.next().trim();
                    if (!strTrim.isEmpty() && !strTrim.startsWith("#")) {
                        int iIndexOf = strTrim.indexOf('=');
                        if (iIndexOf > 0) {
                            autoMineConfig.parseProperty(strTrim.substring(0, iIndexOf).trim(), strTrim.substring(iIndexOf + 1).trim());
                        }
                    }
                }
            } catch (IOException e) {
            }
            if (autoMineConfig.whitelistBlockList.isEmpty() && autoMineConfig.blacklistBlockList.isEmpty() && !autoMineConfig.filterBlockList.isEmpty()) {
                if (autoMineConfig.filterAsBlacklist) {
                    autoMineConfig.blacklistBlockList = autoMineConfig.filterBlockList;
                } else {
                    autoMineConfig.whitelistBlockList = autoMineConfig.filterBlockList;
                }
            }
            autoMineConfig.syncActiveFilter();
        } else {
            autoMineConfig.save();
        }
        return autoMineConfig;
    }

    public void save() {
        if (this.configPath == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("# AutoMine settings\n");
        for (String str : keys()) {
            sb.append(str).append('=').append(get(str)).append('\n');
        }
        try {
            Files.writeString(this.configPath, sb.toString(), new OpenOption[0]);
        } catch (IOException e) {
        }
    }

    public List<String> keys() {
        return List.of(
                "layerHeight", "passWidth", "allowSprint", "allowPlace", "sweepLayer",
                "reachDistance", "renderSelection", "autoEat", "autoEatThreshold",
                "expRepair", "expRepairThreshold", "alertFluid", "alertWebhook", "alertDiscordId",
                "spotifyHud", "spotifyX", "spotifyY", "musicPlaylist", "guiPanels",
                "staffHud", "staffHudX", "staffHudY", "autoSign", "staffRadius",
                "signText", "staffNames",
                "filterBlocks", "filterBlockList", "filterAsBlacklist", "whitelistBlockList", "blacklistBlockList",
                "webhookEnabled", "webhookUrl", "webhookMilestones", "webhookStateChange",
                "webhookIntervalMinutes", "webhookIncludeCoords",
                "friendNames", "backgroundMining",
                "selectionPos1", "selectionPos2",
                "freecamKey", "freecamSpeed"
        );
    }

    public String get(String key) {
        switch (key) {
            case "layerHeight": return String.valueOf(this.layerHeight);
            case "passWidth": return String.valueOf(this.passWidth);
            case "allowSprint": return String.valueOf(this.allowSprint);
            case "allowPlace": return String.valueOf(this.allowPlace);
            case "sweepLayer": return String.valueOf(this.sweepLayer);
            case "reachDistance": return String.valueOf(this.reachDistance);
            case "renderSelection": return String.valueOf(this.renderSelection);
            case "autoEat": return String.valueOf(this.autoEat);
            case "autoEatThreshold": return String.valueOf(this.autoEatThreshold);
            case "expRepair": return String.valueOf(this.expRepair);
            case "expRepairThreshold": return String.valueOf(this.expRepairThreshold);
            case "alertFluid": return String.valueOf(this.alertFluid);
            case "alertWebhook": return this.alertWebhook;
            case "alertDiscordId": return this.alertDiscordId;
            case "spotifyHud": return String.valueOf(this.spotifyHud);
            case "spotifyX": return String.valueOf(this.spotifyX);
            case "spotifyY": return String.valueOf(this.spotifyY);
            case "musicPlaylist": return this.musicPlaylist;
            case "guiPanels": return this.guiPanels;
            case "staffHud": return String.valueOf(this.staffHud);
            case "staffHudX": return String.valueOf(this.staffHudX);
            case "staffHudY": return String.valueOf(this.staffHudY);
            case "autoSign": return String.valueOf(this.autoSign);
            case "staffRadius": return String.valueOf(this.staffRadius);
            case "signText": return this.signText;
            case "staffNames": return this.staffNames;
            case "filterBlocks": return String.valueOf(this.filterBlocks);
            case "filterBlockList": return this.filterBlockList;
            case "filterAsBlacklist": return String.valueOf(this.filterAsBlacklist);
            case "whitelistBlockList": return this.whitelistBlockList;
            case "blacklistBlockList": return this.blacklistBlockList;
            case "webhookEnabled": return String.valueOf(this.webhookEnabled);
            case "webhookUrl": return this.webhookUrl;
            case "webhookMilestones": return String.valueOf(this.webhookMilestones);
            case "webhookStateChange": return String.valueOf(this.webhookStateChange);
            case "webhookIntervalMinutes": return String.valueOf(this.webhookIntervalMinutes);
            case "webhookIncludeCoords": return String.valueOf(this.webhookIncludeCoords);
            case "friendNames": return this.friendNames;
            case "backgroundMining": return String.valueOf(this.backgroundMining);
            case "selectionPos1": return this.selectionPos1;
            case "selectionPos2": return this.selectionPos2;
            case "freecamKey": return String.valueOf(this.freecamKey);
            case "freecamSpeed": return String.valueOf(this.freecamSpeed);
            default: return null;
        }
    }

    public boolean set(String str, String str2) {
        boolean parsed = parseProperty(str, str2);
        if (parsed) {
            save();
        }
        return parsed;
    }

    public boolean parseProperty(String str, String str2) {
        try {
            switch (str) {
                case "layerHeight":
                    this.layerHeight = clampInt(Integer.parseInt(str2), 1, 6);
                    break;
                case "passWidth":
                    this.passWidth = clampInt(Integer.parseInt(str2), 1, 5);
                    break;
                case "allowSprint":
                    this.allowSprint = parseBoolean(str2);
                    break;
                case "allowPlace":
                    this.allowPlace = parseBoolean(str2);
                    break;
                case "sweepLayer":
                    this.sweepLayer = parseBoolean(str2);
                    break;
                case "reachDistance":
                    this.reachDistance = Double.parseDouble(str2);
                    break;
                case "renderSelection":
                    this.renderSelection = parseBoolean(str2);
                    break;
                case "autoEat":
                    this.autoEat = parseBoolean(str2);
                    break;
                case "autoEatThreshold":
                    this.autoEatThreshold = clampInt(Integer.parseInt(str2), 1, 10);
                    break;
                case "expRepair":
                    this.expRepair = parseBoolean(str2);
                    break;
                case "expRepairThreshold":
                    this.expRepairThreshold = clampInt(Integer.parseInt(str2), 1, 1000);
                    break;
                case "alertFluid":
                    this.alertFluid = parseBoolean(str2);
                    break;
                case "alertWebhook":
                    this.alertWebhook = str2;
                    break;
                case "alertDiscordId":
                    this.alertDiscordId = str2;
                    break;
                case "spotifyHud":
                    this.spotifyHud = parseBoolean(str2);
                    break;
                case "spotifyX":
                    this.spotifyX = clampInt(Integer.parseInt(str2), 0, 4000);
                    break;
                case "spotifyY":
                    this.spotifyY = clampInt(Integer.parseInt(str2), 0, 4000);
                    break;
                case "musicPlaylist":
                    this.musicPlaylist = str2.isEmpty() ? this.musicPlaylist : str2;
                    break;
                case "guiPanels":
                    this.guiPanels = str2;
                    break;
                case "staffHud":
                    this.staffHud = parseBoolean(str2);
                    break;
                case "staffHudX":
                    this.staffHudX = Integer.parseInt(str2);
                    break;
                case "staffHudY":
                    this.staffHudY = Integer.parseInt(str2);
                    break;
                case "autoSign":
                    this.autoSign = parseBoolean(str2);
                    break;
                case "staffRadius":
                    this.staffRadius = clampInt(Integer.parseInt(str2), 1, 128);
                    break;
                case "signText":
                    this.signText = str2.isEmpty() ? this.signText : str2;
                    break;
                case "staffNames":
                    this.staffNames = str2.isEmpty() ? this.staffNames : str2;
                    break;
                case "filterBlocks":
                    this.filterBlocks = parseBoolean(str2);
                    break;
                case "filterBlockList":
                    this.filterBlockList = str2;
                    break;
                case "filterAsBlacklist":
                    this.filterAsBlacklist = parseBoolean(str2);
                    break;
                case "whitelistBlockList":
                    this.whitelistBlockList = str2;
                    break;
                case "blacklistBlockList":
                    this.blacklistBlockList = str2;
                    break;
                case "webhookEnabled":
                    this.webhookEnabled = parseBoolean(str2);
                    break;
                case "webhookUrl":
                    this.webhookUrl = str2;
                    break;
                case "webhookMilestones":
                    this.webhookMilestones = parseBoolean(str2);
                    break;
                case "webhookStateChange":
                    this.webhookStateChange = parseBoolean(str2);
                    break;
                case "webhookIntervalMinutes":
                    this.webhookIntervalMinutes = clampInt(Integer.parseInt(str2), 1, 60);
                    break;
                case "webhookIncludeCoords":
                    this.webhookIncludeCoords = parseBoolean(str2);
                    break;
                case "friendNames":
                    this.friendNames = str2;
                    break;
                case "backgroundMining":
                    this.backgroundMining = parseBoolean(str2);
                    break;
                case "selectionPos1":
                    this.selectionPos1 = str2;
                    break;
                case "selectionPos2":
                    this.selectionPos2 = str2;
                    break;
                case "freecamKey":
                    this.freecamKey = Integer.parseInt(str2);
                    break;
                case "freecamSpeed":
                    this.freecamSpeed = Integer.parseInt(str2);
                    break;
                default:
                    return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<String> staffList() {
        ArrayList<String> arrayList = new ArrayList<>();
        String[] strArrSplit = this.staffNames.split(",");
        for (String s : strArrSplit) {
            String strTrim = s.trim();
            if (!strTrim.isEmpty()) {
                arrayList.add(strTrim);
            }
        }
        return arrayList;
    }

    public boolean addStaff(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        List<String> list = staffList();
        for (String s : list) {
            if (s.equalsIgnoreCase(str.trim())) {
                return false;
            }
        }
        list.add(str.trim());
        this.staffNames = String.join(",", list);
        save();
        return true;
    }

    public void removeStaff(String str) {
        List<String> list = staffList();
        list.removeIf(s -> s.equalsIgnoreCase(str.trim()));
        this.staffNames = String.join(",", list);
        save();
    }

    public void resetStaff() {
        this.staffNames = new AutoMineConfig().staffNames;
        save();
    }

    // Friend list helpers
    public List<String> friendList() {
        ArrayList<String> list = new ArrayList<>();
        String[] split = this.friendNames.split(",");
        for (String s : split) {
            String trim = s.trim();
            if (!trim.isEmpty()) {
                list.add(trim);
            }
        }
        return list;
    }

    public boolean addFriend(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        List<String> list = friendList();
        for (String s : list) {
            if (s.equalsIgnoreCase(str.trim())) {
                return false;
            }
        }
        list.add(str.trim());
        this.friendNames = String.join(",", list);
        save();
        return true;
    }

    public boolean removeFriend(String str) {
        List<String> list = friendList();
        boolean removed = list.removeIf(s -> s.equalsIgnoreCase(str.trim()));
        if (removed) {
            this.friendNames = String.join(",", list);
            save();
        }
        return removed;
    }

    public boolean isFriend(String name) {
        if (name == null || name.isBlank() || this.friendNames.isBlank()) {
            return false;
        }
        for (String f : friendList()) {
            if (f.equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    // Block filter helpers
    public static String cleanBlockId(String blockId) {
        if (blockId == null) return "";
        String cleaned = blockId.trim().toLowerCase(Locale.ROOT);
        if (cleaned.startsWith("minecraft:")) {
            cleaned = cleaned.substring(10);
        }
        return cleaned;
    }

    public static List<String> parseCsvList(String csv) {
        ArrayList<String> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return list;
        }
        String[] split = csv.split(",");
        for (String s : split) {
            String trim = cleanBlockId(s);
            if (!trim.isEmpty() && !list.contains(trim)) {
                list.add(trim);
            }
        }
        return list;
    }

    public List<String> whitelistBlockList() {
        return parseCsvList(this.whitelistBlockList);
    }

    public List<String> blacklistBlockList() {
        return parseCsvList(this.blacklistBlockList);
    }

    public void syncActiveFilter() {
        if (this.filterAsBlacklist) {
            this.filterBlockList = this.blacklistBlockList;
        } else {
            this.filterBlockList = this.whitelistBlockList;
        }
    }

    public List<String> filterBlockList() {
        syncActiveFilter();
        return this.filterAsBlacklist ? blacklistBlockList() : whitelistBlockList();
    }

    public boolean addWhitelistBlock(String blockId) {
        String cleaned = cleanBlockId(blockId);
        if (cleaned.isEmpty()) {
            return false;
        }
        List<String> list = whitelistBlockList();
        if (list.contains(cleaned)) {
            return false;
        }
        list.add(cleaned);
        this.whitelistBlockList = String.join(",", list);
        syncActiveFilter();
        save();
        return true;
    }

    public boolean removeWhitelistBlock(String blockId) {
        String cleaned = cleanBlockId(blockId);
        List<String> list = whitelistBlockList();
        boolean removed = list.removeIf(s -> s.equals(cleaned));
        if (removed) {
            this.whitelistBlockList = String.join(",", list);
            syncActiveFilter();
            save();
        }
        return removed;
    }

    public void clearWhitelistBlocks() {
        this.whitelistBlockList = "";
        syncActiveFilter();
        save();
    }

    public boolean addBlacklistBlock(String blockId) {
        String cleaned = cleanBlockId(blockId);
        if (cleaned.isEmpty()) {
            return false;
        }
        List<String> list = blacklistBlockList();
        if (list.contains(cleaned)) {
            return false;
        }
        list.add(cleaned);
        this.blacklistBlockList = String.join(",", list);
        syncActiveFilter();
        save();
        return true;
    }

    public boolean removeBlacklistBlock(String blockId) {
        String cleaned = cleanBlockId(blockId);
        List<String> list = blacklistBlockList();
        boolean removed = list.removeIf(s -> s.equals(cleaned));
        if (removed) {
            this.blacklistBlockList = String.join(",", list);
            syncActiveFilter();
            save();
        }
        return removed;
    }

    public void clearBlacklistBlocks() {
        this.blacklistBlockList = "";
        syncActiveFilter();
        save();
    }

    public boolean addFilterBlock(String blockId) {
        return this.filterAsBlacklist ? addBlacklistBlock(blockId) : addWhitelistBlock(blockId);
    }

    public boolean removeFilterBlock(String blockId) {
        return this.filterAsBlacklist ? removeBlacklistBlock(blockId) : removeWhitelistBlock(blockId);
    }

    public void clearFilterBlocks() {
        if (this.filterAsBlacklist) {
            clearBlacklistBlocks();
        } else {
            clearWhitelistBlocks();
        }
    }

    public static int clampInt(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    public static boolean parseBoolean(String str) {
        String lowerCase = str.toLowerCase(Locale.ROOT);
        return (lowerCase.equals("true") || lowerCase.equals("1") || lowerCase.equals("yes") || lowerCase.equals("on"));
    }
}
