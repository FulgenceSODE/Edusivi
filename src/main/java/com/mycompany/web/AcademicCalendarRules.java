package com.mycompany.web;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class AcademicCalendarRules {

    private AcademicCalendarRules() {
    }

    static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("La date fournie est invalide.");
        }
    }

    static void validateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("La date de début doit précéder la date de fin.");
        }
    }

    static void validateSemesterWithinYear(LocalDate semesterStart, LocalDate semesterEnd,
            LocalDate yearStart, LocalDate yearEnd) {
        validateRange(semesterStart, semesterEnd);
        if (semesterStart.isBefore(yearStart) || semesterEnd.isAfter(yearEnd)) {
            throw new IllegalArgumentException(
                    "Les dates du semestre doivent rester comprises dans celles de son année académique.");
        }
    }
}
