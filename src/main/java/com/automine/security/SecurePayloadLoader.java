package com.automine.security;

import com.automine.core.AutoMineCoreImpl;
import com.automine.core.IAutoMineCore;

/* JADX INFO: loaded from: SecurePayloadLoader.class */
public class SecurePayloadLoader {
    private static volatile IAutoMineCore coreInstance;
    private static volatile String lastErrorMessage = "";

    public static synchronized void loadLocalCore() { getCore(); }

    public static synchronized IAutoMineCore getCore() {
        if (coreInstance == null) {
            try {
                AutoMineCoreImpl autoMineCoreImpl = new AutoMineCoreImpl();
                autoMineCoreImpl.init();
                coreInstance = autoMineCoreImpl;
            } catch (Throwable th) {
                lastErrorMessage = th.getMessage();
                th.printStackTrace();
            }
        }
        return coreInstance;
    }

    public static synchronized boolean loadPayloadSecurely(String str, String str2, String str3, String str4, long j, String str5) {
        return getCore() != null;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static synchronized void wipeCore() {
    }
}
