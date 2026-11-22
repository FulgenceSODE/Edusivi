package com.mycompany.web;

import java.math.BigDecimal;

final class ProfessorGradeRules {

    private ProfessorGradeRules() {
    }

    static BigDecimal parseScore(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La note est obligatoire.");
        }

        BigDecimal score;
        try {
            score = new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La note doit être un nombre valide.");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20.");
        }
        return score;
    }
}
