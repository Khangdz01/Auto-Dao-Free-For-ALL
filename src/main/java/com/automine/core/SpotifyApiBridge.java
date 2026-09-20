package com.automine.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;

/* JADX INFO: loaded from: SpotifyApiBridge.class */
@Environment(EnvType.CLIENT)
public final class SpotifyApiBridge {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126 Safari/537.36";
    public static final String YT_MUSIC_PARAMS = "EgWKAQIIAWoKEAkQBRAKEAMQBA==";
    public static final Pattern NEXT_DATA_PATTERN = Pattern.compile("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>", 32);
    public static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("playlist[/:]([A-Za-z0-9]{16,34})");
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(HttpClient.Redirect.ALWAYS).build();
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "AutoMine-MusicSearch");
        thread.setDaemon(true);
        return thread;
    });

    public static String extractPlaylistId(String str) {
        Matcher matcher = PLAYLIST_ID_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void search(String str, Consumer<List<SpotifyTrackInfo>> consumer, Consumer<String> consumer2) {
        EXECUTOR.submit(() -> {
            try {
                List<SpotifyTrackInfo> list = searchYouTube(str);
                if (list.isEmpty()) {
                    consumer2.accept("Không tìm thấy bài nào cho \"" + str + "\"");
                } else {
                    consumer.accept(list);
                }
            } catch (Throwable th) {
                consumer2.accept("Lỗi mạng khi tìm: " + th.getMessage());
            }
        });
    }

    public static void loadPlaylist(String str, BiConsumer<String, List<SpotifyTrackInfo>> biConsumer, Consumer<String> consumer) {
        EXECUTOR.submit(() -> {
            try {
                Matcher matcher = NEXT_DATA_PATTERN.matcher((String) HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create("https://open.spotify.com/embed/playlist/" + str)).header("User-Agent", USER_AGENT).timeout(Duration.ofSeconds(10L)).GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
                if (!matcher.find()) {
                    consumer.accept("Không đọc được playlist (private? hoặc Spotify đổi trang)");
                    return;
                }
                JsonObject asJsonObject = JsonParser.parseString(matcher.group(1)).getAsJsonObject().getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonObject("state").getAsJsonObject("data").getAsJsonObject("entity");
                String asString = asJsonObject.has("title") ? asJsonObject.get("title").getAsString() : "Playlist";
                ArrayList arrayList = new ArrayList();
                Iterator it = asJsonObject.getAsJsonArray("trackList").iterator();
                while (it.hasNext()) {
                    JsonObject asJsonObject2 = ((JsonElement) it.next()).getAsJsonObject();
                    String asString2 = asJsonObject2.get("title").getAsString();
                    String asString3 = (!asJsonObject2.has("subtitle") || asJsonObject2.get("subtitle").isJsonNull()) ? "" : asJsonObject2.get("subtitle").getAsString();
                    long asLong = (!asJsonObject2.has("duration") || asJsonObject2.get("duration").isJsonNull()) ? 0L : asJsonObject2.get("duration").getAsLong();
                    arrayList.add(new SpotifyTrackInfo(asString2, asLong > (0L) ? asString3 + " • " + formatDuration(asLong) : asString3, null, asString2 + " " + asString3.split(",")[0].trim()));
                }
                if (arrayList.isEmpty()) {
                    consumer.accept("Playlist trống hoặc không công khai");
                } else {
                    biConsumer.accept(asString, arrayList);
                }
            } catch (Throwable th) {
                consumer.accept("Lỗi tải playlist: " + th.getMessage());
            }
        });
    }

    public static void play(SpotifyTrackInfo spotifyTrackInfo, AutoMineSpotifyManager autoMineSpotifyManager, Consumer<String> consumer) {
        EXECUTOR.submit(() -> {
            try {
                String strVideoId = spotifyTrackInfo.videoId();
                if (strVideoId == null) {
                    consumer.accept("Đang tra YouTube: " + spotifyTrackInfo.title());
                    List<SpotifyTrackInfo> list = searchYouTube(spotifyTrackInfo.query());
                    if (list.isEmpty()) {
                        consumer.accept("Không thấy \"" + spotifyTrackInfo.title() + "\" trên YouTube");
                        return;
                    }
                    strVideoId = list.get(0).videoId();
                }
                if (autoMineSpotifyManager != null && autoMineSpotifyManager.isPlaying()) {
                    autoMineSpotifyManager.playPause();
                    Thread.sleep(450L);
                }
                Util.getOperatingSystem().open("https://www.youtube.com/watch?v=" + strVideoId);
                consumer.accept("Đang phát: " + spotifyTrackInfo.title());
            } catch (Throwable th) {
                consumer.accept("Lỗi khi mở bài: " + th.getMessage());
            }
        });
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static List<SpotifyTrackInfo> searchYouTube(String str) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("clientName", "WEB_REMIX");
        jsonObject.addProperty("clientVersion", "1.20250101.01.00");
        jsonObject.addProperty("hl", "vi");
        jsonObject.addProperty("gl", "VN");
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("client", jsonObject);
        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.add("context", jsonObject2);
        jsonObject3.addProperty("query", str);
        jsonObject3.addProperty("params", YT_MUSIC_PARAMS);
        String str2 = (String) HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create("https://music.youtube.com/youtubei/v1/search?prettyPrint=false")).header("User-Agent", USER_AGENT).header("Content-Type", "application/json; charset=utf-8").header("Origin", "https://music.youtube.com").header("Referer", "https://music.youtube.com/").timeout(Duration.ofSeconds(10L)).POST(HttpRequest.BodyPublishers.ofString(jsonObject3.toString(), StandardCharsets.UTF_8)).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
        ArrayList<JsonObject> arrayList = new ArrayList();
        collectRenderers(JsonParser.parseString(str2), arrayList);
        ArrayList arrayList2 = new ArrayList();
        for (JsonObject jsonObject4 : arrayList) {
            try {
                String asString = jsonObject4.has("playlistItemData") ? jsonObject4.getAsJsonObject("playlistItemData").get("videoId").getAsString() : null;
                if (asString == null) {
                    asString = findString(jsonObject4, "videoId");
                }
                JsonArray asJsonArray = jsonObject4.getAsJsonArray("flexColumns");
                String title = extractRunText(asJsonArray.get(0), true);
                String artist = asJsonArray.size() > 1 ? extractRunText(asJsonArray.get(1), false) : "";
                if (asString != null && title != null && !title.isEmpty()) {
                    arrayList2.add(new SpotifyTrackInfo(title, artist, asString, null));
                }
            } catch (Throwable th) {
            }
            if (arrayList2.size() >= 15) {
                break;
            }
        }
        return arrayList2;
    }

    public static void collectRenderers(JsonElement jsonElement, List<JsonObject> list) {
        if (!jsonElement.isJsonObject()) {
            if (jsonElement.isJsonArray()) {
                Iterator it = jsonElement.getAsJsonArray().iterator();
                while (it.hasNext()) {
                    collectRenderers((JsonElement) it.next(), list);
                }
                return;
            }
            return;
        }
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        if (asJsonObject.has("musicResponsiveListItemRenderer")) {
            list.add(asJsonObject.getAsJsonObject("musicResponsiveListItemRenderer"));
        }
        Iterator it2 = asJsonObject.entrySet().iterator();
        while (it2.hasNext()) {
            collectRenderers((JsonElement) ((Map.Entry) it2.next()).getValue(), list);
        }
    }

    public static String findString(JsonElement jsonElement, String str) {
        if (!jsonElement.isJsonObject()) {
            if (!jsonElement.isJsonArray()) {
                return null;
            }
            Iterator it = jsonElement.getAsJsonArray().iterator();
            while (it.hasNext()) {
                String strFound = findString((JsonElement) it.next(), str);
                if (strFound != null) {
                    return strFound;
                }
            }
            return null;
        }
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        if (asJsonObject.has(str) && asJsonObject.get(str).isJsonPrimitive()) {
            return asJsonObject.get(str).getAsString();
        }
        Iterator it2 = asJsonObject.entrySet().iterator();
        while (it2.hasNext()) {
            String strFound2 = findString((JsonElement) ((Map.Entry) it2.next()).getValue(), str);
            if (strFound2 != null) {
                return strFound2;
            }
        }
        return null;
    }

    public static String extractRunText(JsonElement jsonElement, boolean firstOnly) {
        JsonArray asJsonArray = jsonElement.getAsJsonObject().getAsJsonObject("musicResponsiveListItemFlexColumnRenderer").getAsJsonObject("text").getAsJsonArray("runs");
        if (asJsonArray == null || asJsonArray.isEmpty()) {
            return "";
        }
        if (firstOnly) {
            return asJsonArray.get(0).getAsJsonObject().get("text").getAsString();
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = asJsonArray.iterator();
        while (it.hasNext()) {
            sb.append(((JsonElement) it.next()).getAsJsonObject().get("text").getAsString());
        }
        return sb.toString();
    }

    public static String formatDuration(long j) {
        long j2 = j / 1000L;
        Object[] objArr = new Object[2];
        objArr[0] = Long.valueOf(j2 / 60L);
        objArr[1] = Long.valueOf(j2 % 60L);
        return String.format("%d:%02d", objArr);
    }
}
