package de.labystudio.spotifyapi.open;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.model.AccessTokenResponse;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import de.labystudio.spotifyapi.open.totp.TOTP;
import de.labystudio.spotifyapi.open.totp.gson.SecretDeserializer;
import de.labystudio.spotifyapi.open.totp.gson.SecretSerializer;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import de.labystudio.spotifyapi.open.totp.provider.SecretProvider;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: OpenSpotifyAPI.class */
@Environment(EnvType.CLIENT)
public class OpenSpotifyAPI {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Secret.class, new SecretDeserializer()).registerTypeAdapter(Secret.class, new SecretSerializer()).create();
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537." + ((int) (Math.random() * 90.0d));
    public static final String URL_API_GEN_ACCESS_TOKEN = "https://open.spotify.com/api/token?reason=%s&productType=web-player&totp=%s&totpServer=%s&totpVer=%s";
    public static final String URL_API_TRACKS = "https://api.spotify.com/v1/tracks/%s";
    public static final String URL_API_SERVER_TIME = "https://open.spotify.com/api/server-time";
    public final Executor executor = Executors.newSingleThreadExecutor();
    public final Cache<BufferedImage> imageCache = new Cache<>(10);
    public final Cache<OpenTrack> openTrackCache = new Cache<>(100);
    public final SecretProvider secretProvider;
    public AccessTokenResponse accessTokenResponse;

    public OpenSpotifyAPI(SecretProvider secretProvider) {
        this.secretProvider = secretProvider;
    }

    public void generateAccessTokenAsync(Consumer<AccessTokenResponse> consumer) {
        this.executor.execute(() -> {
            try {
                consumer.accept(generateAccessToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public long requestServerTime() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(URL_API_SERVER_TIME).openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Host", "open.spotify.com");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpURLConnection.setRequestProperty("Accept", "application/json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line = bufferedReader.readLine();
        bufferedReader.close();
        return ((JsonObject) GSON.fromJson(line, JsonObject.class)).get("serverTime").getAsLong();
    }

    public AccessTokenResponse generateAccessToken() throws IOException {
        Secret secret = this.secretProvider.getSecret();
        if (secret == null) {
            throw new IOException("No TOTP secret provided");
        }
        String strGenerateOtp = TOTP.generateOtp(secret.getSecretAsBytes(), requestServerTime(), 30, 6);
        AccessTokenResponse token = getToken("transport", strGenerateOtp, secret.getVersion());
        if (!hasValidAccessToken(token)) {
            token = getToken("init", strGenerateOtp, secret.getVersion());
        }
        if (hasValidAccessToken(token)) {
            return token;
        }
        throw new IOException("Could not generate access token");
    }

    public AccessTokenResponse getToken(String str, String str2, int i) throws IOException {
        InputStream errorStream;
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(String.format(URL_API_GEN_ACCESS_TOKEN, str, str2, str2, Integer.valueOf(i))).openConnection();
        httpsURLConnection.addRequestProperty("User-Agent", USER_AGENT);
        httpsURLConnection.addRequestProperty("referer", "https://open.spotify.com/");
        httpsURLConnection.addRequestProperty("app-platform", "WebPlayer");
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        if (httpsURLConnection.getResponseCode() == 200 || (errorStream = httpsURLConnection.getErrorStream()) == null) {
            return (AccessTokenResponse) GSON.fromJson(new JsonReader(new InputStreamReader(httpsURLConnection.getInputStream())), AccessTokenResponse.class);
        }
        throw new IOException("Could not retrieve access token: " + ((JsonObject) GSON.fromJson(new JsonReader(new InputStreamReader(errorStream)), JsonObject.class)).toString());
    }

    public void requestImageAsync(Track track, Consumer<BufferedImage> consumer) {
        requestImageAsync(track.getId(), consumer);
    }

    public void requestImageAsync(String str, Consumer<BufferedImage> consumer) {
        if (!Track.isTrackIdValid(str)) {
            throw new IllegalArgumentException("Invalid track ID: " + str);
        }
        this.executor.execute(() -> {
            try {
                BufferedImage bufferedImageRequestImage = requestImage(str);
                if (bufferedImageRequestImage != null) {
                    consumer.accept(bufferedImageRequestImage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void requestImageUrlAsync(String str, Consumer<String> consumer) {
        this.executor.execute(() -> {
            try {
                String strRequestImageUrl = requestImageUrl(str);
                if (strRequestImageUrl != null) {
                    consumer.accept(strRequestImageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void requestOpenTrackAsync(Track track, Consumer<OpenTrack> consumer) {
        requestOpenTrackAsync(track.getId(), consumer);
    }

    public void requestOpenTrackAsync(String str, Consumer<OpenTrack> consumer) {
        this.executor.execute(() -> {
            try {
                OpenTrack openTrackRequestOpenTrack = requestOpenTrack(str);
                if (openTrackRequestOpenTrack != null) {
                    consumer.accept(openTrackRequestOpenTrack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public BufferedImage requestImage(Track track) throws IOException {
        return requestImage(track.getId());
    }

    public BufferedImage requestImage(String str) throws IOException {
        if (!Track.isTrackIdValid(str)) {
            throw new IllegalArgumentException("Invalid track ID: " + str);
        }
        BufferedImage bufferedImage = this.imageCache.get(str);
        if (bufferedImage != null) {
            return bufferedImage;
        }
        String strRequestImageUrl = requestImageUrl(str);
        if (strRequestImageUrl == null) {
            return null;
        }
        BufferedImage bufferedImage2 = ImageIO.read(new URL(strRequestImageUrl));
        if (bufferedImage2 == null) {
            throw new IOException("Could not load image: " + strRequestImageUrl);
        }
        this.imageCache.push(str, bufferedImage2);
        return bufferedImage2;
    }

    public String requestImageUrl(String str) throws IOException {
        OpenTrack openTrackRequestOpenTrack = requestOpenTrack(str);
        if (openTrackRequestOpenTrack == null) {
            return null;
        }
        return openTrackRequestOpenTrack.album.images.get(0).url;
    }

    public OpenTrack requestOpenTrack(Track track) throws IOException {
        return requestOpenTrack(track.getId());
    }

    public OpenTrack requestOpenTrack(String str) throws IOException {
        OpenTrack openTrack = this.openTrackCache.get(str);
        if (openTrack != null) {
            return openTrack;
        }
        OpenTrack openTrack2 = (OpenTrack) request(String.format(URL_API_TRACKS, str), OpenTrack.class, true);
        this.openTrackCache.push(str, openTrack2);
        return openTrack2;
    }

    public <T> T request(String str, Class<?> cls, boolean z) throws IOException {
        if (this.accessTokenResponse == null) {
            this.accessTokenResponse = generateAccessToken();
        }
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
        httpsURLConnection.addRequestProperty("User-Agent", USER_AGENT);
        httpsURLConnection.addRequestProperty("referer", "https://open.spotify.com/");
        httpsURLConnection.addRequestProperty("app-platform", "WebPlayer");
        httpsURLConnection.addRequestProperty("origin", "https://open.spotify.com");
        if (this.accessTokenResponse != null) {
            httpsURLConnection.addRequestProperty("authorization", "Bearer " + this.accessTokenResponse.accessToken);
        }
        if (httpsURLConnection.getResponseCode() / 100 == 2) {
            return (T) GSON.fromJson(new JsonReader(new InputStreamReader(httpsURLConnection.getInputStream(), StandardCharsets.UTF_8)), cls);
        }
        if (!z) {
            return null;
        }
        this.accessTokenResponse = generateAccessToken();
        return (T) request(str, cls, false);
    }

    public boolean hasValidAccessToken(AccessTokenResponse accessTokenResponse) {
        return (accessTokenResponse == null || accessTokenResponse.accessToken == null || accessTokenResponse.accessToken.isEmpty()) ? false : true;
    }

    public Cache<BufferedImage> getImageCache() {
        return this.imageCache;
    }

    public Cache<OpenTrack> getOpenTrackCache() {
        return this.openTrackCache;
    }
}
