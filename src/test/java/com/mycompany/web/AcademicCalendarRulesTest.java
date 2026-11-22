package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AcademicCalendarRulesTest {

    @Test
    void acceptsAnAcademicYearWithStartBeforeEnd() {
        assertDoesNotThrow(() -> AcademicCalendarRules.validateRange(
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31)));
    }

    @Test
    void rejectsAnAcademicYearWithEqualOrReversedDates() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        assertThrows(IllegalArgumentException.class,
                () -> AcademicCalendarRules.validateRange(start, start));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicCalendarRules.validateRange(start, start.minusDays(1)));
    }

    @Test
    void rejectsSemesterDatesOutsideItsAcademicYear() {
        assertThrows(IllegalArgumentException.class,
                () -> AcademicCalendarRules.validateSemesterWithinYear(
                        LocalDate.of(2026, 8, 31), LocalDate.of(2027, 1, 31),
                        LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31)));
        assertThrows(IllegalArgumentException.class,
                () -> AcademicCalendarRules.validateSemesterWithinYear(
                        LocalDate.of(2027, 2, 1), LocalDate.of(2027, 8, 1),
                        LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31)));
    }
}
