package com.automine.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

/* JADX INFO: loaded from: SpotifyMediaPoller.class */
@Environment(EnvType.CLIENT)
public final class SpotifyMediaPoller {
    public Process process;
    public volatile boolean stopped;
    public static final String POLLER_SCRIPT;

    /* JADX WARN: Multi-variable type inference failed */
    public synchronized void start(SpotifyTrackCallback trackCallback) {
        if (this.process != null) {
            return;
        }
        try {
            Path pathResolve = FabricLoader.getInstance().getConfigDir().resolve("automine-media.ps1");
            Files.write(pathResolve, POLLER_SCRIPT.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            String[] strArr = new String[8];
            strArr[0] = "powershell";
            strArr[1] = "-NoProfile";
            strArr[2] = "-ExecutionPolicy";
            strArr[3] = "Bypass";
            strArr[4] = "-WindowStyle";
            strArr[5] = "Hidden";
            strArr[6] = "-File";
            strArr[7] = pathResolve.toString();
            this.process = new ProcessBuilder(strArr).redirectErrorStream(true).start();
            Thread thread = new Thread(() -> {
                readPollerOutput(trackCallback);
            }, "AutoMine-MediaSession");
            thread.setDaemon(true);
            thread.start();
        } catch (Throwable th) {
            this.process = null;
        }
    }

    public void readPollerOutput(SpotifyTrackCallback trackCallback) {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8));
            while (!this.stopped && (line = bufferedReader.readLine()) != null) {
                try {
                    if (line.startsWith("##")) {
                        try {
                            JsonObject asJsonObject = JsonParser.parseString(line.substring(2)).getAsJsonObject();
                            if (!asJsonObject.has("title") || asJsonObject.get("title").isJsonNull()) {
                                trackCallback.clear();
                            } else {
                                trackCallback.update(asJsonObject.get("title").getAsString(), (!asJsonObject.has("artist") || asJsonObject.get("artist").isJsonNull()) ? "" : asJsonObject.get("artist").getAsString(), "Playing".equalsIgnoreCase(asJsonObject.has("status") ? asJsonObject.get("status").getAsString() : ""), asJsonObject.has("pos") ? asJsonObject.get("pos").getAsLong() : 0L, asJsonObject.has("dur") ? asJsonObject.get("dur").getAsLong() : 0L, (!asJsonObject.has("app") || asJsonObject.get("app").isJsonNull()) ? "" : asJsonObject.get("app").getAsString());
                            }
                        } catch (Throwable th) {
                        }
                    }
                } catch (Throwable th2) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                    throw th2;
                }
            }
            bufferedReader.close();
        } catch (Throwable th4) {
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public synchronized void stop() {
        this.stopped = true;
        if (this.process != null) {
            this.process.destroy();
            this.process = null;
        }
    }

    static {
        CharSequence[] charSequenceArr = new CharSequence[30];
        charSequenceArr[0] = "$ErrorActionPreference = 'SilentlyContinue'";
        charSequenceArr[1] = "Add-Type -AssemblyName System.Runtime.WindowsRuntime";
        charSequenceArr[2] = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8";
        charSequenceArr[3] = "[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime] | Out-Null";
        charSequenceArr[4] = "$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]";
        charSequenceArr[5] = "function Await($op, $type) {";
        charSequenceArr[6] = "  $task = $asTask.MakeGenericMethod($type).Invoke($null, @($op))";
        charSequenceArr[7] = "  if ($task.Wait(2000)) { return $task.Result }";
        charSequenceArr[8] = "  return $null";
        charSequenceArr[9] = "}";
        charSequenceArr[10] = "while ($true) {";
        charSequenceArr[11] = "  $line = '##{}'";
        charSequenceArr[12] = "  try {";
        charSequenceArr[13] = "    $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])";
        charSequenceArr[14] = "    if ($mgr) {";
        charSequenceArr[15] = "      $s = $mgr.GetCurrentSession()";
        charSequenceArr[16] = "      if ($s) {";
        charSequenceArr[17] = "        $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])";
        charSequenceArr[18] = "        $tl = $s.GetTimelineProperties()";
        charSequenceArr[19] = "        $info = $s.GetPlaybackInfo()";
        charSequenceArr[20] = "        if ($p) {";
        charSequenceArr[21] = "          $o = @{ title = $p.Title; artist = $p.Artist; status = [string]$info.PlaybackStatus; pos = [int64]$tl.Position.TotalMilliseconds; dur = [int64]$tl.EndTime.TotalMilliseconds; app = $s.SourceAppUserModelId }";
        charSequenceArr[22] = "          $line = '##' + (ConvertTo-Json $o -Compress)";
        charSequenceArr[23] = "        }";
        charSequenceArr[24] = "      }";
        charSequenceArr[25] = "    }";
        charSequenceArr[26] = "  } catch {}";
        charSequenceArr[27] = "  Write-Output $line";
        charSequenceArr[28] = "  Start-Sleep -Milliseconds 1000";
        charSequenceArr[29] = "}";
        POLLER_SCRIPT = String.join("\r\n", charSequenceArr);
    }
}
