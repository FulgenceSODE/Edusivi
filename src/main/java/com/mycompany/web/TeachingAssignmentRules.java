package com.mycompany.web;

final class TeachingAssignmentRules {

    private TeachingAssignmentRules() {
    }

    static void validateYearPair(Long groupYearId, Long semesterYearId) {
        if (groupYearId == null || semesterYearId == null || !groupYearId.equals(semesterYearId)) {
            throw new IllegalArgumentException(
                    "Le semestre et le groupe doivent appartenir à la même année académique.");
        }
    }
}
