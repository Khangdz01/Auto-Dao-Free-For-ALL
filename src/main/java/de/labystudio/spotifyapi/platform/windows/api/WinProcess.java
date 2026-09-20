package de.labystudio.spotifyapi.platform.windows.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import de.labystudio.spotifyapi.platform.windows.api.jna.Kernel32;
import de.labystudio.spotifyapi.platform.windows.api.jna.Psapi;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: WinProcess.class */
@Environment(EnvType.CLIENT)
public class WinProcess implements WinApi {
    public static final Gson GSON = new Gson();
    public final int processId;
    public final WinNT.HANDLE handle;
    public final WinDef.HWND window;
    public long scanTimeout = 10000;

    /* JADX INFO: loaded from: WinProcess$SearchCondition.class */
    @Environment(EnvType.CLIENT)
    public interface SearchCondition {
        boolean matches(long j, int i);
    }

    /* JADX INFO: loaded from: WinProcess$SearchRule.class */
    @Environment(EnvType.CLIENT)
    public static class SearchRule {
        public final String text;
        public final SearchCondition condition;

        public SearchRule(String str, SearchCondition searchCondition) {
            this.text = str;
            this.condition = searchCondition;
        }

        public String getText() {
            return this.text;
        }

        public SearchCondition getCondition() {
            return this.condition;
        }
    }

    public WinProcess(String str) {
        this.processId = getProcessIdByName(str);
        if (this.processId == -1) {
            throw new IllegalStateException("Process of executable " + str + " not found");
        }
        this.handle = openProcessHandle(this.processId);
        if (this.handle == null) {
            throw new IllegalStateException("Process handle of " + this.processId + " not found");
        }
        this.window = openWindow(this.processId, hwnd -> {
            String windowTitle = getWindowTitle(hwnd);
            return (windowTitle.equals("Spotify Debug Window") || windowTitle.equals("DevTools")) ? false : true;
        });
        if (getWindowTitle().isEmpty()) {
            throw new IllegalStateException("Window for process " + this.processId + " not found");
        }
    }

    public boolean readBoolean(long j) {
        return readByte(j) == 1;
    }

    public byte readByte(long j) {
        return readBytes(j, 1)[0];
    }

    public int readInteger(long j) {
        byte[] bytes = readBytes(j, 4);
        return (bytes[0] & 255) | ((bytes[1] & 255) << 8) | ((bytes[2] & 255) << 16) | ((bytes[3] & 255) << 24);
    }

    public String readString(long j, int i) {
        return new String(readBytes(j, i));
    }

    public byte[] readBytes(long j, int i) {
        Kernel32 kernel32 = Kernel32.INSTANCE;
        Memory memory = new Memory(i);
        kernel32.ReadProcessMemory(this.handle, new Pointer(j), memory, i, new IntByReference(i));
        return memory.getByteArray(0L, i);
    }

    public long findInMemory(long j, long j2, byte[] bArr) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        long j3 = j;
        while (true) {
            long j4 = j3;
            if (j4 >= j2) {
                return -1L;
            }
            byte[] bytes = readBytes(j4, 65536 + bArr.length);
            for (int i = 0; i < bytes.length - bArr.length; i++) {
                boolean z = true;
                for (int i2 = 0; i2 < bArr.length; i2++) {
                    if (bytes[i + i2] != bArr[i2]) {
                        z = false;
                        break;
                    }
                }
                if (z) {
                    return j4 + ((long) i);
                }
            }
            if (System.currentTimeMillis() - jCurrentTimeMillis > this.scanTimeout) {
                throw new IllegalStateException("Scan timeout of " + this.scanTimeout + "ms reached at address " + j4);
            }
            j3 = j4 + 65536;
        }
    }

    public long findInMemory(long j, long j2, byte[] bArr, SearchCondition searchCondition) {
        long j3 = j;
        int i = 0;
        while (j3 < j2) {
            long jFindInMemory = findInMemory(j3, j2, bArr);
            if (jFindInMemory == -1 || searchCondition.matches(jFindInMemory, i)) {
                return jFindInMemory;
            }
            j3 = jFindInMemory + 1;
            i++;
        }
        return -1L;
    }

    public boolean hasBytes(long j, int... iArr) {
        byte[] bytes = readBytes(j, iArr.length);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != ((byte) iArr[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBytes(long j, byte[] bArr) {
        byte[] bytes = readBytes(j, bArr.length);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != bArr[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBytes(long j, byte[]... bArr) {
        for (byte[] bArr2 : bArr) {
            if (hasBytes(j, bArr2)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasText(long j, String str) {
        return hasBytes(j, str.getBytes());
    }

    public boolean hasText(long j, String... strArr) {
        for (String str : strArr) {
            if (hasText(j, str)) {
                return true;
            }
        }
        return false;
    }

    public long findAddressOfTextInModule(String str, String str2) {
        Psapi.ModuleInfo moduleInfo = getModuleInfo(str);
        if (moduleInfo == null) {
            return -1L;
        }
        return findAddressOfText(moduleInfo.getBaseOfDll(), str2, 0);
    }

    public long findAddressOfText(long j, String str, int i) {
        return findAddressOfText(j, str, (j2, i2) -> {
            return i2 == i;
        });
    }

    public long findAddressOfText(long j, String str, SearchCondition searchCondition) {
        return findAddressOfText(j, Long.MAX_VALUE, str, searchCondition);
    }

    public long findAddressOfText(long j, long j2, String str, SearchCondition searchCondition) {
        return findInMemory(j, j2, str.getBytes(), searchCondition);
    }

    public long findAddressOfTexts(long j, long j2, SearchCondition searchCondition, String... strArr) {
        for (String str : strArr) {
            long jFindAddressOfText = findAddressOfText(j, j2, str, searchCondition);
            if (jFindAddressOfText != -1) {
                return jFindAddressOfText;
            }
        }
        return -1L;
    }

    public long findAddressUsingPath(String... strArr) {
        long jFindAddressOfText = -1;
        for (String str : strArr) {
            jFindAddressOfText = findAddressOfText(jFindAddressOfText + 1, str, 0);
            if (jFindAddressOfText == -1) {
                return -1L;
            }
        }
        return jFindAddressOfText;
    }

    public long findAddressUsingRules(SearchRule... searchRuleArr) {
        long jFindAddressOfText = -1;
        for (SearchRule searchRule : searchRuleArr) {
            jFindAddressOfText = findAddressOfText(jFindAddressOfText + 1, searchRule.getText(), searchRule.getCondition());
            if (jFindAddressOfText == -1) {
                return -1L;
            }
        }
        return jFindAddressOfText;
    }

    public JsonObject readJsonObject(long j) {
        int i = 0;
        boolean z = false;
        long j2 = 0;
        StringBuilder sb = new StringBuilder();
        do {
            String string = readString(j + j2, 1024);
            for (int i2 = 0; i2 < string.length(); i2++) {
                char cCharAt = string.charAt(i2);
                if (cCharAt == '\"') {
                    z = !z;
                }
                if (!z) {
                    if (cCharAt == '{' || cCharAt == '[') {
                        i++;
                    } else if (cCharAt == '}' || cCharAt == ']') {
                        i--;
                    }
                }
                sb.append(cCharAt);
                if (i == 0) {
                    break;
                }
            }
            j2 += 1024;
        } while (i > 0);
        return (JsonObject) GSON.fromJson(sb.toString(), JsonObject.class);
    }

    public Psapi.ModuleInfo getModuleInfo(String str) {
        return getModuleInfo(this.handle, str);
    }

    public Map<String, Psapi.ModuleInfo> getModules() {
        return getModules(this.handle);
    }

    public long getFirstModuleAddress() {
        long jMin = Long.MAX_VALUE;
        Iterator<Map.Entry<String, Psapi.ModuleInfo>> it = getModules().entrySet().iterator();
        while (it.hasNext()) {
            long baseOfDll = it.next().getValue().getBaseOfDll();
            if (baseOfDll > 0) {
                jMin = Math.min(jMin, baseOfDll);
            }
        }
        return jMin;
    }

    public long getMaxProcessAddress() {
        long jMax = 0;
        for (Map.Entry<String, Psapi.ModuleInfo> entry : getModules().entrySet()) {
            jMax = Math.max(jMax, entry.getValue().getBaseOfDll() + ((long) entry.getValue().getSizeOfImage()));
        }
        return jMax;
    }

    public String getWindowTitle() {
        return getWindowTitle(this.window);
    }

    public int getProcessId() {
        return this.processId;
    }

    public WinNT.HANDLE getHandle() {
        return this.handle;
    }

    public void setScanTimeout(long j) {
        this.scanTimeout = j;
    }

    public boolean isOpen() {
        return this.handle != null;
    }

    public void close() {
        Kernel32.INSTANCE.CloseHandle(this.handle);
    }
}
