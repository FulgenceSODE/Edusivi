package com.mycompany.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ResourceBundle;

public final class UiMessages {

    private UiMessages() {
    }

    public static String text(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        String language = session == null ? "fr" : (String) session.getAttribute("userLanguage");
        Locale locale = "en".equals(language) ? Locale.ENGLISH : Locale.FRENCH;
        return ResourceBundle.getBundle("i18n.messages", locale).getString(key);
    }
}
