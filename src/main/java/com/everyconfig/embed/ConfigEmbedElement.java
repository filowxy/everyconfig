package com.everyconfig.embed;

import com.everyconfig.EveryConfig;
import com.everyconfig.EveryConfigConstants;
import com.hellow.element.Element;
import com.hellow.element.Lifecycle;
import com.hellow.element.RenderContext;
import com.hellow.element.ScreenEmbedElement;
import com.hellow.embed.EmbedNavigation;
import com.hellow.page.Page;
import com.hellow.page.PageScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Hand-written-page element backing the {@code configEmbed} JSON type.
 *
 * <p>Width/height/mouse display are delegated to a lazily-built inner element: the
 * first time this element renders (or receives a mouse event) it asks
 * {@link EveryConfig} for the chosen config library's {@link Screen} wrapper,
 * parented to an {@link EmbedNavigation} proxy screen — the sentinel that lets
 * hellow intercept the library's Save/Cancel/ESC navigation. When no library is
 * installed the inner element is the "not installed" placeholder instead (never
 * crashes).
 *
 * <p><b>Rebuild rules</b> (the delegate is dropped and re-created when):
 * <ul>
 *   <li>{@code refreshOnSave} and the adapter reported a real save — the next
 *       render rebuilds, so the embed shows the persisted values;</li>
 *   <li>the host screen changed — {@code PageScheduler.show} creates a fresh
 *       {@link PageScreen} per open, so reusing a screen built for an old host
 *       would send the library's "return to parent" to a dead screen;</li>
 *   <li>the element was unmounted and is shown again.</li>
 * </ul>
 * Rebuilding happens outside the library's own save flow (a flag now, a rebuild
 * on the next render/measure), and the old screen is {@code removed()} first.
 *
 * <p>Selection rules, mirroring the page JSON:
 * <ul>
 *   <li>{@code library} — an exact adapter id such as {@code cloth_config}, or
 *       {@code auto}/{@code first}/{@code null} to embed the first installed library.</li>
 *   <li>{@code libraries} — ordered library-id list; the first installed adapter wins
 *       (overrides {@code library}).</li>
 *   <li>{@code height} — embed height hint (default {@link EveryConfigConstants#DEFAULT_EMBED_HEIGHT}).</li>
 *   <li>{@code refreshOnSave} / {@code escClosesPage} / {@code onExit} — see {@link EmbedOptions}.</li>
 * </ul>
 */
public final class ConfigEmbedElement implements Element, Lifecycle, EmbedNavigation.Sink {

    private final String library;
    private final List<String> preference;
    private final int height;
    private final EmbedOptions options;

    // Delegated embed element; rebuilt when stale (see ensureInner).
    private Element inner;
    private Screen builtForHost;
    private boolean rebuildPending;

    // Sentinel parent handed to the library screen so EmbedNavigation recognises
    // its "return to parent" navigation. One per element, reused across rebuilds.
    private Screen proxy;

    // Host bookkeeping for EmbedNavigation (updated on every build).
    private Page page;
    private Screen hostParent;

    // Embedded screen currently registered for promote-interception. Kept across
    // the host's unmount on purpose: Cloth Config's confirm-dialog Cancel re-opens
    // the embedded screen after the page is gone, and that navigation must still
    // be recognised. Swapped whenever the embed rebuilds.
    private Screen trackedEmbedded;

    public ConfigEmbedElement(String library, List<String> preference, int height) {
        this(library, preference, height, EmbedOptions.defaults());
    }

    public ConfigEmbedElement(String library, List<String> preference, int height, EmbedOptions options) {
        this.library = isAuto(library) ? null : library;
        this.preference = preference == null || preference.isEmpty() ? null : List.copyOf(preference);
        this.height = Math.max(1, height);
        this.options = options == null ? EmbedOptions.defaults() : options;
    }

    private static boolean isAuto(String value) {
        return value == null || value.equalsIgnoreCase("auto") || value.equalsIgnoreCase("first");
    }

    // -------- EmbedNavigation.Sink (intercepted library navigation) --------

    @Override
    public void onReturnRequested() {
        // The library tried to leave; the navigation itself was cancelled. A save
        // has already scheduled a rebuild via the adapter's onSaved hook — the only
        // extra thing to decide here is whether "exit" should close the page.
        if (this.options.onExit() == EmbedOptions.ExitPolicy.CLOSE_PAGE) {
            com.hellow.Hellow.closeTop();
        }
    }

    @Override
    public boolean allowExit() {
        return this.options.onExit() == EmbedOptions.ExitPolicy.ALLOW;
    }

    @Override
    public Screen host() {
        return this.builtForHost;
    }

    @Override
    public Page page() {
        return this.page;
    }

    @Override
    public Screen hostParent() {
        return this.hostParent;
    }

    @Override
    public Screen embeddedScreen() {
        // Null before the first render and while a placeholder is showing — the
        // promote-navigation interception only fires once the library screen is
        // live anyway (the player must have clicked inside it).
        return this.inner instanceof ScreenEmbedElement see ? see.embedded() : null;
    }

    // -------- Delegate management --------

    /** Build the delegate when missing or stale; the current screen is the host. */
    private Element ensureInner() {
        Minecraft mc = Minecraft.getInstance();
        Screen current = mc == null ? null : mc.screen;
        if (this.inner != null) {
            boolean stale = this.rebuildPending
                    || (current != null && current != this.builtForHost);
            if (!stale) return this.inner;
            discardInner();
        }
        if (current == null) {
            // No host screen yet (measurement before the page is displayed): fall
            // back to an inert placeholder so layout math keeps working.
            if (this.inner == null) {
                this.inner = EveryConfig.placeholder("configEmbed", this.height);
            }
            return this.inner;
        }
        this.builtForHost = current;
        if (current instanceof PageScreen ps) {
            this.page = ps.page();
            this.hostParent = ps.parentScreen();
        } else {
            this.page = null;
            this.hostParent = null;
        }
        if (this.proxy == null) {
            this.proxy = EmbedNavigation.createProxy(this);
        }
        this.inner = buildInner(this.proxy);
        trackEmbeddedForPromotion(this.inner);
        return this.inner;
    }

    /**
     * Keep the promote-interception session in sync with the live embed: untrack
     * the previous embedded screen and register the new one. Never called from
     * {@link #onUnmount()} — the session must outlive the host page (see
     * {@link EmbedNavigation}).
     */
    private void trackEmbeddedForPromotion(Element built) {
        if (this.trackedEmbedded != null) {
            EmbedNavigation.untrackEmbedded(this.trackedEmbedded);
            this.trackedEmbedded = null;
        }
        if (built instanceof ScreenEmbedElement see) {
            this.trackedEmbedded = see.embedded();
            EmbedNavigation.trackEmbedded(this.trackedEmbedded, this);
        }
    }

    private Element buildInner(Screen parent) {
        Runnable onSaved = this.options.refreshOnSave()
                ? () -> this.rebuildPending = true   // flag now; rebuild outside the save flow
                : () -> { };
        if (this.preference != null) {
            return EveryConfig.embedFirstInstalledIn(this.preference, parent, this.height, onSaved, !this.options.escClosesPage());
        }
        if (this.library != null) {
            return EveryConfig.embed(this.library, parent, this.height, onSaved, !this.options.escClosesPage());
        }
        return EveryConfig.embedFirst(parent, this.height, onSaved, !this.options.escClosesPage());
    }

    /** Drop the delegate, releasing the embedded screen it wrapped. */
    private void discardInner() {
        if (this.inner instanceof ScreenEmbedElement see) {
            see.embedded().removed();
        }
        this.inner = null;
        this.builtForHost = null;
        this.rebuildPending = false;
    }

    // -------- Element --------

    @Override
    public int width(Font font) {
        return ensureInner().width(font);
    }

    @Override
    public int height(Font font, int width) {
        return ensureInner().height(font, width);
    }

    @Override
    public void render(RenderContext ctx) {
        ensureInner().render(ctx);
    }

    @Override
    public boolean mouseClicked(RenderContext ctx, int button) {
        return ensureInner().mouseClicked(ctx, button);
    }

    @Override
    public boolean mouseScrolled(RenderContext ctx, double deltaX, double deltaY) {
        return ensureInner().mouseScrolled(ctx, deltaX, deltaY);
    }

    @Override
    public boolean mouseDragged(RenderContext ctx, int button, double dragX, double dragY) {
        return ensureInner().mouseDragged(ctx, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(RenderContext ctx, int button) {
        return ensureInner().mouseReleased(ctx, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return ensureInner().keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return ensureInner().keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return ensureInner().charTyped(codePoint, modifiers);
    }

    @Override
    public Component tooltipAt(RenderContext ctx) {
        return ensureInner().tooltipAt(ctx);
    }

    // -------- Lifecycle (fan-out reaches this element through the containers) --------

    @Override
    public void tick() {
        if (this.inner instanceof Lifecycle lc) lc.tick();
    }

    @Override
    public void onUnmount() {
        if (this.inner instanceof Lifecycle lc) lc.onUnmount();
        this.inner = null;
        this.builtForHost = null;
        this.rebuildPending = false;
        EmbedNavigation.release(this.proxy);
        this.proxy = null;
    }
}
