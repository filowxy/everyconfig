package com.everyconfig.embed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Parsing of the {@code configEmbed} navigation rules (pure, no Minecraft). */
class EmbedOptionsTest {

    @Test
    void defaultsStayClosePageAndRefresh() {
        EmbedOptions options = EmbedOptions.defaults();
        assertEquals(EmbedOptions.ExitPolicy.STAY, options.onExit());
        assertEquals(true, options.escClosesPage());
        assertEquals(true, options.refreshOnSave());
    }

    @Test
    void exitPolicyParsesKnownValues() {
        assertEquals(EmbedOptions.ExitPolicy.CLOSE_PAGE, EmbedOptions.parseExitPolicy("closePage"));
        assertEquals(EmbedOptions.ExitPolicy.CLOSE_PAGE, EmbedOptions.parseExitPolicy("close"));
        assertEquals(EmbedOptions.ExitPolicy.ALLOW, EmbedOptions.parseExitPolicy("allow"));
        assertEquals(EmbedOptions.ExitPolicy.STAY, EmbedOptions.parseExitPolicy("stay"));
    }

    @Test
    void exitPolicyIsCaseAndDashInsensitive() {
        assertEquals(EmbedOptions.ExitPolicy.CLOSE_PAGE, EmbedOptions.parseExitPolicy("CLOSE-PAGE"));
    }

    @Test
    void unknownOrNullExitPolicyFallsBackToStay() {
        assertEquals(EmbedOptions.ExitPolicy.STAY, EmbedOptions.parseExitPolicy(null));
        assertEquals(EmbedOptions.ExitPolicy.STAY, EmbedOptions.parseExitPolicy("explode"));
        assertEquals(EmbedOptions.ExitPolicy.STAY, EmbedOptions.parseExitPolicy(""));
    }
}
