package de.labystudio.spotifyapi.open.totp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: TOTP.class */
@Environment(EnvType.CLIENT)
public class TOTP {
    public static final String DEFAULT_ALGORITHM = "HmacSHA1";

    public static String generateOtp(byte[] bArr, long j, int i, int i2) {
        ByteBuffer byteBufferOrder = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        byteBufferOrder.putLong(j / ((long) i));
        byte[] bArrArray = byteBufferOrder.array();
        try {
            Mac mac = Mac.getInstance(DEFAULT_ALGORITHM);
            mac.init(new SecretKeySpec(bArr, DEFAULT_ALGORITHM));
            byte[] bArrDoFinal = mac.doFinal(bArrArray);
            int i3 = bArrDoFinal[bArrDoFinal.length - 1] & 15;
            return String.format("%0" + i2 + "d", Integer.valueOf((((((bArrDoFinal[i3] & 127) << 24) | ((bArrDoFinal[i3 + 1] & 255) << 16)) | ((bArrDoFinal[i3 + 2] & 255) << 8)) | (bArrDoFinal[i3 + 3] & 255)) % ((int) Math.pow(10.0d, i2))));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate TOTP", e);
        }
    }
}
