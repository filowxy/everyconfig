package com.everyconfig.embed;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Demo config backing the sample screens the built-in adapters build — and the
 * proof that "Save" inside an embedded screen really persists. Values live here
 * (static fields, the shape the library bindings read/write), the
 * {@link DemoConfigStore} writes them to {@code config/everyconfig/demo.json},
 * and because adapters read the fields again when a page rebuilds the embed, a
 * saved value survives both a refresh and a game restart.
 *
 * <p>(ponytail: still a demo holder — real integrations replace the adapter's
 * {@code createScreen} with the owning mod's own options and persistence.)
 */
public final class DemoConfig {
    private static final String FILE_NAME = "everyconfig/demo.json";

    public static boolean enabled = true;
    public static int amount = 3;
    public static String label = "EveryConfig demo";

    private static boolean loaded;

    private DemoConfig() {}

    /** Sync the static fields from disk; lazy so class init stays side-effect free. */
    public static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        apply(store().load());
    }

    /** Persist the current values (called from the adapters' save hooks). */
    public static synchronized void save() {
        store().save(snapshot());
    }

    /** Copy {@code data} into the static fields; null-safe for corrupt-file fallbacks. */
    public static void apply(DemoConfigStore.Data data) {
        if (data == null) return;
        enabled = data.enabled;
        amount = data.amount;
        label = data.label;
    }

    /** Copy the static fields into a fresh on-disk shape. */
    public static DemoConfigStore.Data snapshot() {
        DemoConfigStore.Data data = new DemoConfigStore.Data();
        data.enabled = enabled;
        data.amount = amount;
        data.label = label;
        return data;
    }

    private static DemoConfigStore store() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        return new DemoConfigStore(configDir.resolve(FILE_NAME));
    }
}
