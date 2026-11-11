package com.everyconfig.embed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** File round-trips for {@link DemoConfigStore} (bare JUnit JVM, no Minecraft). */
class DemoConfigStoreTest {

    @TempDir
    Path dir;

    private DemoConfigStore.Data sample() {
        DemoConfigStore.Data data = new DemoConfigStore.Data();
        data.enabled = false;
        data.amount = 7;
        data.label = "saved value";
        return data;
    }

    @Test
    void saveThenLoadRoundTrips() {
        DemoConfigStore store = new DemoConfigStore(dir.resolve("everyconfig/demo.json"));
        store.save(sample());

        DemoConfigStore.Data loaded = store.load();
        assertEquals(false, loaded.enabled);
        assertEquals(7, loaded.amount);
        assertEquals("saved value", loaded.label);
    }

    @Test
    void saveCreatesMissingParentDirectories() throws Exception {
        Path deep = dir.resolve("a/b/c/demo.json");
        assertFalse(Files.exists(deep));
        new DemoConfigStore(deep).save(sample());
        assertTrue(Files.exists(deep));
    }

    @Test
    void missingFileFallsBackToDefaults() {
        DemoConfigStore.Data loaded = new DemoConfigStore(dir.resolve("nope.json")).load();
        assertEquals(true, loaded.enabled);
        assertEquals(3, loaded.amount);
        assertEquals("EveryConfig demo", loaded.label);
    }

    @Test
    void corruptFileFallsBackInsteadOfThrowing() throws Exception {
        Path file = dir.resolve("demo.json");
        Files.writeString(file, "{ this is not json ]");
        DemoConfigStore.Data loaded = new DemoConfigStore(file).load();
        // Defaults, not a crash — a broken demo file must not kill the embed.
        assertEquals(true, loaded.enabled);
    }

    @Test
    void nullJsonContentFallsBackToDefaults() throws Exception {
        Path file = dir.resolve("demo.json");
        Files.writeString(file, "null");
        assertEquals(3, new DemoConfigStore(file).load().amount);
    }
}
