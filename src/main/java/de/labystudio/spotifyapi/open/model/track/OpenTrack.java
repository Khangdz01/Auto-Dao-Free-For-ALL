package de.labystudio.spotifyapi.open.model.track;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: OpenTrack.class */
@Environment(EnvType.CLIENT)
public class OpenTrack {
    public Album album;
    public List<Artist> artists = null;

    @SerializedName("available_markets")
    public List<Object> availableMarkets = null;

    @SerializedName("disc_number")
    public Integer discNumber;

    @SerializedName("duration_ms")
    public Integer durationMs;
    public Boolean explicit;

    @SerializedName("external_ids")
    public ExternalIds externalIds;

    @SerializedName("external_urls")
    public ExternalUrls externalUrls;
    public String href;
    public String id;

    @SerializedName("is_local")
    public Boolean isLocal;
    public String name;
    public Integer popularity;

    @SerializedName("preview_url")
    public Object previewUrl;

    @SerializedName("track_number")
    public Integer trackNumber;
    public String type;
    public String uri;
    public transient String joinedArtists;

    public String getArtists() {
        if (this.joinedArtists == null) {
            this.joinedArtists = getArtists(", ");
        }
        return this.joinedArtists;
    }

    public String getArtists(String str) {
        if (this.artists == null || this.artists.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Artist artist : this.artists) {
            sb.append(str);
            sb.append(artist.name);
        }
        return sb.substring(str.length());
    }
}
