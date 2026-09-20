package com.automine.core;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: User32Library.class */
@Environment(EnvType.CLIENT)
public interface User32Library extends StdCallLibrary {
    public static final User32Library INSTANCE = Native.load("user32", User32Library.class);

    void keybd_event(byte b, byte b2, int i, long j);
}
