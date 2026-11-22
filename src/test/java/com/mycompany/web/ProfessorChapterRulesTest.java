package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ProfessorChapterRulesTest {

    @Test
    void acceptsSupportedProgressAndNormalizesCase() {
        assertEquals(ProfessorChapterRules.IN_PROGRESS,
                ProfessorChapterRules.validateProgression("en_cours"));
        assertEquals(ProfessorChapterRules.TO_COME,
                ProfessorChapterRules.validateProgression("A_VENIR"));
        assertEquals(ProfessorChapterRules.COMPLETED,
                ProfessorChapterRules.validateProgression("TERMINE"));
    }

    @Test
    void rejectsUnknownProgress() {
        assertThrows(IllegalArgumentException.class,
                () -> ProfessorChapterRules.validateProgression("PUBLIE"));
    }
}
