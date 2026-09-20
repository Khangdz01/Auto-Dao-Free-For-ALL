package com.automine.security;

public class ClientEnvironment {
    private static final String WALL_PROTECTED_FINGERPRINT = "WALL-PROTECTED-OFFLINE-ENVIRONMENT";

    public static String getClientFingerprint() {
        // Wall Protect: Prevent OS/hardware identification and user tracking
        return WALL_PROTECTED_FINGERPRINT;
    }
}
