package de.labystudio.spotifyapi.platform.osx.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: AppleScript.class */
@Environment(EnvType.CLIENT)
public class AppleScript {
    public static final String GRAMMAR_FORMAT = "tell application \"%s\" to %s";
    public final String application;
    public final String[] runtimeParameters = {"osascript", "-e", null};
    public final Runtime runtime = Runtime.getRuntime();

    public AppleScript(String str) {
        this.application = str;
    }

    public String getOf(Action action, Action action2) throws Exception {
        return execute(Action.GET, action, Action.OF, action2);
    }

    public String get(Action action) throws Exception {
        return execute(Action.GET, action);
    }

    public String execute(Action... actionArr) throws Exception {
        String string = Action.toString(actionArr);
        this.runtimeParameters[2] = String.format(GRAMMAR_FORMAT, this.application, string);
        Process processExec = this.runtime.exec(this.runtimeParameters);
        int iWaitFor = processExec.waitFor();
        if (iWaitFor == 0) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(processExec.getInputStream()));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    return sb.toString();
                }
                sb.append(line);
            }
        } else {
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(processExec.getErrorStream()));
            StringBuilder sb2 = new StringBuilder();
            while (true) {
                String line2 = bufferedReader2.readLine();
                if (line2 == null) {
                    throw new Exception("AppleScript execution \"" + string + "\" failed with exit code " + iWaitFor + ": " + String.valueOf(sb2));
                }
                sb2.append(line2);
            }
        }
    }
}
