package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SemesterGradeCalculatorTest {

    @Test
    void averagesCategoriesEquallyAndUsesPublishedGradesOnly() {
        var enrollment = new SemesterGradeCalculator.Enrollment(1L, "MAT-1", "Nom", "Prenom");
        var composition = new SemesterGradeCalculator.Composition(
                10L, "UE-1", "UE", 20L, "EC-1", "EC", BigDecimal.ONE, 1);
        var grades = List.of(
                new SemesterGradeCalculator.Grade(1L, 20L, "DEVOIR", BigDecimal.TEN, true),
                new SemesterGradeCalculator.Grade(1L, 20L, "DEVOIR", BigDecimal.valueOf(20), true),
                new SemesterGradeCalculator.Grade(1L, 20L, "EXAMEN", BigDecimal.valueOf(5), true),
                new SemesterGradeCalculator.Grade(1L, 20L, "INTERROGATION", BigDecimal.valueOf(20), false));

        var result = SemesterGradeCalculator.calculate(
                List.of(enrollment), List.of(composition), grades).get(0);

        assertEquals(0, result.ueResults().get(0).ecResults().get(0).average()
                .compareTo(new BigDecimal("10")));
        assertEquals(0, SemesterGradeCalculator.roundForDisplay(result.semesterAverage())
                .compareTo(new BigDecimal("10.00")));
    }

    @Test
    void excludesUnavailableEcFromUeWeightAndSemesterAverage() {
        var enrollment = new SemesterGradeCalculator.Enrollment(1L, "MAT-1", "Nom", "Prenom");
        var compositions = List.of(
                new SemesterGradeCalculator.Composition(
                        10L, "UE-1", "UE 1", 20L, "EC-1", "EC 1", BigDecimal.valueOf(3), 1),
                new SemesterGradeCalculator.Composition(
                        10L, "UE-1", "UE 1", 21L, "EC-2", "EC 2", BigDecimal.ONE, 2),
                new SemesterGradeCalculator.Composition(
                        11L, "UE-2", "UE 2", 22L, "EC-3", "EC 3", BigDecimal.ONE, 1));
        var grades = List.of(
                new SemesterGradeCalculator.Grade(1L, 20L, "DEVOIR", BigDecimal.valueOf(12), true),
                new SemesterGradeCalculator.Grade(1L, 22L, "EXAMEN", BigDecimal.valueOf(0), true));

        var result = SemesterGradeCalculator.calculate(
                List.of(enrollment), compositions, grades).get(0);

        assertEquals(0, result.ueResults().get(0).average().compareTo(new BigDecimal("12")));
        assertEquals(0, result.ueResults().get(1).average().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.semesterAverage().compareTo(new BigDecimal("6")));
    }

    @Test
    void keepsMissingMeansNullAndRejectsNonPositiveCoefficients() {
        var enrollment = new SemesterGradeCalculator.Enrollment(1L, "MAT-1", "Nom", "Prenom");
        var composition = new SemesterGradeCalculator.Composition(
                10L, "UE-1", "UE", 20L, "EC-1", "EC", BigDecimal.ONE, 1);
        var result = SemesterGradeCalculator.calculate(
                List.of(enrollment), List.of(composition), List.of()).get(0);

        assertNull(result.ueResults().get(0).average());
        assertNull(result.semesterAverage());
        assertThrows(IllegalStateException.class, () -> SemesterGradeCalculator.calculate(
                List.of(enrollment),
                List.of(new SemesterGradeCalculator.Composition(
                        10L, "UE-1", "UE", 20L, "EC-1", "EC", BigDecimal.ZERO, 1)),
                List.of()));
    }
}
