package com.everyconfig.embed;

import java.util.Locale;

/**
 * Parsed {@code configEmbed} rules that steer what happens around the embedded
 * screen's own Save/Cancel navigation. Pure data + parsing, so the rules are
 * unit-testable without Minecraft.
 *
 * <ul>
 *   <li>{@code refreshOnSave} — rebuild the embedded screen after a real save so
 *       it shows the persisted values (default true).</li>
 *   <li>{@code escClosesPage} — ESC closes the hellow page instead of reaching
 *       the embedded screen (default true; the page, not the library, owns ESC).</li>
 *   <li>{@code onExit} — what happens when the library navigates back to its
 *       parent (Save-and-exit / Cancel / its own ESC): {@code stay} keeps the
 *       page open (default), {@code closePage} closes it, {@code allow} lets the
 *       navigation through and re-opens the page afterwards.</li>
 * </ul>
 */
public record EmbedOptions(boolean refreshOnSave, boolean escClosesPage, ExitPolicy onExit) {

    public enum ExitPolicy { STAY, CLOSE_PAGE, ALLOW }

    public static EmbedOptions defaults() {
        return new EmbedOptions(true, true, ExitPolicy.STAY);
    }

    /** Lenient parse: unknown or missing values fall back to {@link ExitPolicy#STAY}. */
    public static ExitPolicy parseExitPolicy(String raw) {
        if (raw == null) return ExitPolicy.STAY;
        return switch (raw.toLowerCase(Locale.ROOT).replace("-", "")) {
            case "closepage", "close" -> ExitPolicy.CLOSE_PAGE;
            case "allow" -> ExitPolicy.ALLOW;
            default -> ExitPolicy.STAY;
        };
    }
}
