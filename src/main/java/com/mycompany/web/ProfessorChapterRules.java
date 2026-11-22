package com.mycompany.web;

import java.util.List;

final class ProfessorChapterRules {

    static final String TO_COME = "A_VENIR";
    static final String IN_PROGRESS = "EN_COURS";
    static final String COMPLETED = "TERMINE";
    static final List<String> PROGRESS_STATES = List.of(TO_COME, IN_PROGRESS, COMPLETED);

    private ProfessorChapterRules() {
    }

    static String validateProgression(String value) {
        String progression = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!PROGRESS_STATES.contains(progression)) {
            throw new IllegalArgumentException("La progression doit être À VENIR, EN COURS ou TERMINÉ.");
        }
        return progression;
    }
}
