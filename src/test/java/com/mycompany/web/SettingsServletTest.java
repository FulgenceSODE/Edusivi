package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettingsServletTest {

    @Test
    void normalizesRequiredProfileNamesWithinDatabaseLimit() {
        assertEquals("Aminata", SettingsServlet.normalizeProfileName("  Aminata  "));
        assertNull(SettingsServlet.normalizeProfileName("  "));
        assertNull(SettingsServlet.normalizeProfileName("a".repeat(101)));
        assertEquals("a".repeat(100), SettingsServlet.normalizeProfileName("a".repeat(100)));
    }

    @Test
    void validatesCurrentAndMatchingNewPassword() {
        String validPassword = "a".repeat(12);

        assertTrue(SettingsServlet.isValidPasswordChange("current", validPassword, validPassword));
        assertFalse(SettingsServlet.isValidPasswordChange("", validPassword, validPassword));
        assertFalse(SettingsServlet.isValidPasswordChange("c".repeat(129), validPassword, validPassword));
        assertFalse(SettingsServlet.isValidPasswordChange("current", "short", "short"));
        assertFalse(SettingsServlet.isValidPasswordChange("current", validPassword, "different"));
        assertFalse(SettingsServlet.isValidPasswordChange("current", "a".repeat(129), "a".repeat(129)));
    }
}
