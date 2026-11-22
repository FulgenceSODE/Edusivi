package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TeachingAssignmentRulesTest {

    @Test
    void acceptsSemesterAndGroupFromTheSameYear() {
        assertDoesNotThrow(() -> TeachingAssignmentRules.validateYearPair(12L, 12L));
    }

    @Test
    void rejectsSemesterFromAnotherOrMissingYear() {
        assertThrows(IllegalArgumentException.class,
                () -> TeachingAssignmentRules.validateYearPair(12L, 13L));
        assertThrows(IllegalArgumentException.class,
                () -> TeachingAssignmentRules.validateYearPair(12L, null));
    }
}
