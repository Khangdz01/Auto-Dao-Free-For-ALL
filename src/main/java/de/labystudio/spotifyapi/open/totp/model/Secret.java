package de.labystudio.spotifyapi.open.totp.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: Secret.class */
@Environment(EnvType.CLIENT)
public class Secret {
    public final int[] secret;
    public final int version;

    public Secret(int[] iArr, int i) {
        this.secret = iArr;
        this.version = i;
    }

    public String getSecretAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i : this.secret) {
            sb.append((char) i);
        }
        return sb.toString();
    }

    public byte[] getSecretAsBytes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.secret.length; i++) {
            sb.append(this.secret[i] ^ ((i % 33) + 9));
        }
        StringBuilder sb2 = new StringBuilder();
        for (int i2 = 0; i2 < sb.length(); i2++) {
            sb2.append(String.format("%02x", Integer.valueOf(sb.charAt(i2))));
        }
        byte[] bArr = new byte[sb2.length() / 2];
        for (int i3 = 0; i3 < sb2.length(); i3 += 2) {
            bArr[i3 / 2] = (byte) Integer.parseInt(sb2.substring(i3, i3 + 2), 16);
        }
        return bArr;
    }

    public int getVersion() {
        return this.version;
    }

    public static Secret fromNumbers(int[] iArr, int i) {
        return new Secret(iArr, i);
    }

    public static Secret fromString(String str, int i) {
        int[] iArr = new int[str.length()];
        for (int i2 = 0; i2 < str.length(); i2++) {
            iArr[i2] = str.charAt(i2);
        }
        return new Secret(iArr, i);
    }
}
