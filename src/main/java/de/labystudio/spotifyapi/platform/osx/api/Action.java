package de.labystudio.spotifyapi.platform.osx.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: Action.class */
@Environment(EnvType.CLIENT)
public class Action {
    public static final Action GET = new Action("get", "the");
    public static final Action OF = new Action("of");
    public final String action;

    public Action(String... strArr) {
        this.action = String.join(" ", strArr);
    }

    public Action(String str) {
        this.action = str;
    }

    public Action(Action... actionArr) {
        this.action = toString(actionArr);
    }

    public String toString() {
        return this.action;
    }

    public static String toString(Action... actionArr) {
        String[] strArr = new String[actionArr.length];
        for (int i = 0; i < actionArr.length; i++) {
            strArr[i] = actionArr[i].toString();
        }
        return String.join(" ", strArr);
    }
}
