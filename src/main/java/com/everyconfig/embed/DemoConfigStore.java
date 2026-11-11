package com.everyconfig.embed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON persistence for the demo config — the "really saved" half of the embedded
 * Save button. Kept free of Minecraft/FML types (a plain {@link Path} in, data
 * out) so it runs — and is unit-tested — in a bare JUnit JVM; {@link DemoConfig}
 * wires it to the real {@code config/} directory.
 *
 * <p>Failure policy: a missing file falls back to defaults (first launch), a
 * corrupt one logs a warning and also falls back — this is a demo holder, not
 * production data, and a loud error would block the embed from rendering at all.
 */
public final class DemoConfigStore {
    private static final Logger LOG = LoggerFactory.getLogger("everyconfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** The on-disk shape; field initializers double as the first-launch defaults. */
    public static final class Data {
        public boolean enabled = true;
        public int amount = 3;
        public String label = "EveryConfig demo";
    }

    private final Path file;

    public DemoConfigStore(Path file) {
        this.file = file;
    }

    /** Read the file, or the defaults when it does not exist / cannot be parsed. */
    public Data load() {
        try {
            if (!Files.exists(this.file)) return new Data();
            String json = Files.readString(this.file, StandardCharsets.UTF_8);
            Data data = GSON.fromJson(json, Data.class);
            return data == null ? new Data() : data;
        } catch (Exception e) {
            LOG.warn("[everyconfig] could not read {} ({}); using defaults", this.file, e.toString());
            return new Data();
        }
    }

    /** Write {@code data}; failures are logged, never thrown. */
    public void save(Data data) {
        try {
            if (this.file.getParent() != null) Files.createDirectories(this.file.getParent());
            Files.writeString(this.file, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("[everyconfig] could not write {}: {}", this.file, e.toString());
        }
    }
}
