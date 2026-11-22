package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AcademicStructureRulesTest {

    @Test
    void acceptsPositiveCoefficientsWithDotOrFrenchDecimalComma() {
        assertEquals(new BigDecimal("1.25"), AcademicStructureRules.parsePositiveCoefficient("1.25"));
        assertEquals(new BigDecimal("0.5"), AcademicStructureRules.parsePositiveCoefficient("0,5"));
    }

    @Test
    void rejectsBlankInvalidAndNonPositiveCoefficients() {
        assertThrows(IllegalArgumentException.class,
                () -> AcademicStructureRules.parsePositiveCoefficient(""));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicStructureRules.parsePositiveCoefficient("1,2.3"));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicStructureRules.parsePositiveCoefficient("0"));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicStructureRules.parsePositiveCoefficient("-1"));
    }
}
