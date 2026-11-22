package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProfessorGradeRulesTest {

    @Test
    void acceptsZeroTwentyAndFrenchDecimalSeparator() {
        assertEquals(BigDecimal.ZERO, ProfessorGradeRules.parseScore("0"));
        assertEquals(BigDecimal.valueOf(20), ProfessorGradeRules.parseScore("20"));
        assertEquals(new BigDecimal("12.5"), ProfessorGradeRules.parseScore("12,5"));
    }

    @Test
    void rejectsBlankInvalidAndOutOfRangeScores() {
        assertThrows(IllegalArgumentException.class, () -> ProfessorGradeRules.parseScore(" "));
        assertThrows(IllegalArgumentException.class, () -> ProfessorGradeRules.parseScore("abc"));
        assertThrows(IllegalArgumentException.class, () -> ProfessorGradeRules.parseScore("-0.1"));
        assertThrows(IllegalArgumentException.class, () -> ProfessorGradeRules.parseScore("20.1"));
    }
}
