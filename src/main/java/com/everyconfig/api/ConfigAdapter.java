package com.everyconfig.api;

import net.minecraft.client.gui.screens.Screen;

/**
 * A single config-library adapter: knows whether the library mod is installed at
 * runtime and builds a {@link Screen} for it.
 *
 * <p>Soft-dependency contract: implementations may reference the library's classes
 * directly, but must never touch them unless {@link #isInstalled()} has returned
 * {@code true}. The JVM verifies/loads each method lazily, so keeping the library
 * classes inside {@link #createScreen} and calling it only after the guard keeps a
 * missing-library install from crashing.
 */
public interface ConfigAdapter {
    /** NeoForge mod id of the underlying library (e.g. {@code "cloth_config"}). */
    String libraryId();

    /** Human-readable library name for the placeholder / demo. */
    String displayName();

    /** True when the library mod is present at runtime. */
    boolean isInstalled();

    /**
     * Build a fresh config screen parented to {@code parent}; only call when
     * installed. {@code onSaved} runs after the library has actually applied and
     * persisted the user's changes (Cloth's Save / save-and-quit, YACL's save
     * button) — embeds rely on it to refresh, so a no-op here breaks that.
     */
    Screen createScreen(Screen parent, Runnable onSaved);

    /**
     * Legacy shape: a screen that never reports saves back to the caller. Adapters
     * still persist their own config, but an embed built this way will not refresh
     * after a save.
     */
    default Screen createScreen(Screen parent) {
        return createScreen(parent, () -> { });
    }
}
