package de.labystudio.spotifyapi.platform;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.config.SpotifyConfiguration;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/* JADX INFO: loaded from: AbstractTickSpotifyAPI.class */
@Environment(EnvType.CLIENT)
public abstract class AbstractTickSpotifyAPI implements SpotifyAPI {
    public static final long TICK_INTERVAL = 1000;
    public OpenSpotifyAPI openAPI;
    public SpotifyConfiguration configuration;
    public ScheduledFuture<?> task;
    public final List<SpotifyListener> listeners = new ArrayList();
    public final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public long timeLastException = -1;

    public abstract void onTick() throws Exception;

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public SpotifyAPI initialize(SpotifyConfiguration spotifyConfiguration) {
        synchronized (this) {
            this.configuration = spotifyConfiguration;
            if (this.executor.isShutdown()) {
                throw new IllegalStateException("This SpotifyAPI has been shutdown and cannot be reused");
            }
            if (isInitialized()) {
                throw new IllegalStateException("This SpotifyAPI is already initialized");
            }
            onInitialized();
            this.task = this.executor.scheduleWithFixedDelay(this::onInternalTick, 0L, 1000L, TimeUnit.MILLISECONDS);
            onInternalTick();
        }
        return this;
    }

    public void onInitialized() {
    }

    public synchronized void onInternalTick() {
        try {
            if (System.currentTimeMillis() - this.timeLastException < this.configuration.getExceptionReconnectDelay()) {
                return;
            }
            onTick();
        } catch (Exception e) {
            this.timeLastException = System.currentTimeMillis();
            stop();
            this.listeners.forEach(spotifyListener -> {
                spotifyListener.onDisconnect(e);
            });
            if (this.configuration.isAutoReconnect()) {
                initialize(this.configuration);
            }
        }
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void registerListener(SpotifyListener spotifyListener) {
        this.listeners.add(spotifyListener);
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void unregisterListener(SpotifyListener spotifyListener) {
        this.listeners.remove(spotifyListener);
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public boolean isInitialized() {
        return this.task != null;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public SpotifyConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void stop() {
        synchronized (this) {
            if (this.task != null) {
                this.task.cancel(true);
                this.task = null;
            }
        }
    }

    @Override // de.labystudio.spotifyapi.SpotifyAPI
    public void shutdown() {
        stop();
        this.executor.shutdownNow();
    }
}
