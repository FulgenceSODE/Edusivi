package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StudentEnrollmentRulesTest {

    @Test
    void transitionsPendingValidatedAndSuspendedEnrollments() {
        assertEquals("VALIDE", StudentEnrollmentRules.nextStatus("EN_ATTENTE"));
        assertEquals("SUSPENDUE", StudentEnrollmentRules.nextStatus("VALIDE"));
        assertEquals("VALIDE", StudentEnrollmentRules.nextStatus("SUSPENDUE"));
    }

    @Test
    void rejectsUnknownEnrollmentStatuses() {
        assertThrows(IllegalArgumentException.class, () -> StudentEnrollmentRules.nextStatus("CLOTUREE"));
    }
}
