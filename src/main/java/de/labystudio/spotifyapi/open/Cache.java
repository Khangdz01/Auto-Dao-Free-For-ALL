package de.labystudio.spotifyapi.open;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: Cache.class */
@Environment(EnvType.CLIENT)
public class Cache<T> {
    public final Map<String, T> cache = new ConcurrentHashMap();
    public final List<String> cacheQueue = new ArrayList();
    public int cacheSize;

    public Cache(int i) {
        this.cacheSize = i;
    }

    public void setCacheSize(int i) {
        this.cacheSize = i;
    }

    public void push(String str, T t) {
        if (str == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (t == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (this.cacheQueue.size() > this.cacheSize) {
            this.cache.remove(this.cacheQueue.remove(0));
        }
        this.cache.put(str, t);
        this.cacheQueue.add(str);
    }

    public boolean has(String str) {
        return this.cache.containsKey(str);
    }

    public T get(String str) {
        return this.cache.get(str);
    }

    public void clear() {
        this.cache.clear();
        this.cacheQueue.clear();
    }
}
