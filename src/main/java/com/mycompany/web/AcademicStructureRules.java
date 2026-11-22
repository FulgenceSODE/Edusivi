package com.mycompany.web;

import java.math.BigDecimal;
import java.util.regex.Pattern;

final class AcademicStructureRules {

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)$");

    private AcademicStructureRules() {
    }

    static BigDecimal parsePositiveCoefficient(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > 32
                || (text.indexOf(',') >= 0 && text.indexOf('.') >= 0)) {
            throw new IllegalArgumentException("Le coefficient doit être un nombre décimal valide.");
        }
        String normalized = text.replace(',', '.');
        if (!DECIMAL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Le coefficient doit être un nombre décimal valide.");
        }
        BigDecimal coefficient = new BigDecimal(normalized);
        if (coefficient.signum() <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être strictement positif.");
        }
        return coefficient;
    }
}
