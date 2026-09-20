package de.labystudio.spotifyapi.platform.windows.api;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import de.labystudio.spotifyapi.platform.windows.api.jna.Kernel32;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import de.labystudio.spotifyapi.platform.windows.api.jna.Tlhelp32;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: WinApi.class */
@Environment(EnvType.CLIENT)
public interface WinApi {
    public static final int PROCESS_VM_READ = 16;
    public static final int PROCESS_VM_WRITE = 32;
    public static final int PROCESS_VM_OPERATION = 8;
    public static final int VK_VOLUME_MUTE = 173;
    public static final int VK_VOLUME_DOWN = 174;
    public static final int VK_VOLUME_UP = 175;
    public static final int VK_MEDIA_NEXT_TRACK = 176;
    public static final int VK_MEDIA_PREV_TRACK = 177;
    public static final int VK_MEDIA_STOP = 178;
    public static final int VK_MEDIA_PLAY_PAUSE = 179;

    /* JADX INFO: loaded from: WinApi$WindowCondition.class */
    @Environment(EnvType.CLIENT)
    public interface WindowCondition {
        boolean test(WinDef.HWND hwnd);
    }

    default int getProcessIdByName(String str) {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        WinNT.HANDLE handleCreateToolhelp32Snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0L));
        Tlhelp32.PROCESSENTRY32.ByReference byReference = new Tlhelp32.PROCESSENTRY32.ByReference();
        while (kernel32.Process32Next(handleCreateToolhelp32Snapshot, byReference)) {
            if (Native.toString(byReference.szExeFile).equals(str)) {
                int iIntValue = byReference.th32ProcessID.intValue();
                kernel32.CloseHandle(handleCreateToolhelp32Snapshot);
                return iIntValue;
            }
        }
        kernel32.CloseHandle(handleCreateToolhelp32Snapshot);
        return -1;
    }

    default List<Integer> getProcessIdsByName(String str) {
        ArrayList arrayList = new ArrayList();
        Kernel32 kernel32 = Kernel32.INSTANCE;
        WinNT.HANDLE handleCreateToolhelp32Snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0L));
        Tlhelp32.PROCESSENTRY32.ByReference byReference = new Tlhelp32.PROCESSENTRY32.ByReference();
        while (kernel32.Process32Next(handleCreateToolhelp32Snapshot, byReference)) {
            if (Native.toString(byReference.szExeFile).equals(str)) {
                arrayList.add(Integer.valueOf(byReference.th32ProcessID.intValue()));
            }
        }
        kernel32.CloseHandle(handleCreateToolhelp32Snapshot);
        return arrayList;
    }

    default WinNT.HANDLE openProcessHandle(int i) {
        return Kernel32.INSTANCE.OpenProcess(56, false, i);
    }

    default WinDef.HWND openWindow(int i) {
        return openWindow(i, hwnd -> {
            return true;
        });
    }

    default WinDef.HWND openWindow(int i, WindowCondition windowCondition) {
        AtomicReference atomicReference = new AtomicReference();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            IntByReference intByReference = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, intByReference);
            if (intByReference.getValue() != i || !isWindowVisible(hwnd) || !windowCondition.test(hwnd)) {
                return true;
            }
            atomicReference.set(hwnd);
            return false;
        }, (Pointer) null);
        return (WinDef.HWND) atomicReference.get();
    }

    default boolean isWindowVisible(WinDef.HWND hwnd) {
        return User32.INSTANCE.IsWindowVisible(hwnd);
    }

    default String getWindowTitle(WinDef.HWND hwnd) {
        char[] cArr = new char[512];
        return Native.toString(Arrays.copyOf(cArr, User32.INSTANCE.GetWindowText(hwnd, cArr, cArr.length)));
    }

    default Map<String, Psapi.ModuleInfo> getModules(WinNT.HANDLE handle) {
        HashMap map = new HashMap();
        for (Pointer pointer : getModuleHandles(handle)) {
            char[] cArr = new char[1024];
            String str = new String(cArr, 0, Psapi.INSTANCE.GetModuleBaseName(handle, pointer, cArr, cArr.length));
            Psapi.ModuleInfo moduleInfo = new Psapi.ModuleInfo();
            Psapi.INSTANCE.GetModuleInformation(handle, pointer, moduleInfo, moduleInfo.size());
            map.put(str, moduleInfo);
        }
        return map;
    }

    default Psapi.ModuleInfo getModuleInfo(WinNT.HANDLE handle, String str) {
        for (Pointer pointer : getModuleHandles(handle)) {
            char[] cArr = new char[1024];
            if (new String(cArr, 0, Psapi.INSTANCE.GetModuleBaseName(handle, pointer, cArr, cArr.length)).equals(str)) {
                Psapi.ModuleInfo moduleInfo = new Psapi.ModuleInfo();
                Psapi.INSTANCE.GetModuleInformation(handle, pointer, moduleInfo, moduleInfo.size());
                return moduleInfo;
            }
        }
        return null;
    }

    default Pointer[] getModuleHandles(WinNT.HANDLE handle) {
        IntByReference intByReference = new IntByReference();
        Pointer[] pointerArr = new Pointer[2048];
        if (!Psapi.INSTANCE.EnumProcessModulesEx(handle, pointerArr, pointerArr.length, intByReference, 3)) {
            throw new RuntimeException("Failed to get module list: ERROR " + Kernel32.INSTANCE.GetLastError());
        }
        int value = intByReference.getValue();
        if (value == 0) {
            throw new RuntimeException("No modules found");
        }
        return (Pointer[]) Arrays.copyOf(pointerArr, value);
    }

    default void pressKey(int i) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(1L);
        input.input.setType("ki");
        input.input.ki.wVk = new WinDef.WORD(i);
        input.input.ki.wScan = new WinDef.WORD(0L);
        input.input.ki.time = new WinDef.DWORD(0L);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0L);
        input.input.ki.dwFlags = new WinDef.DWORD(0L);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1L), new WinUser.INPUT[]{input}, input.size());
        input.input.ki.dwFlags = new WinDef.DWORD(2L);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1L), new WinUser.INPUT[]{input}, input.size());
    }
}
