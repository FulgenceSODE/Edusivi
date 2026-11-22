package com.mycompany.web;

import java.util.Locale;

final class StudentEnrollmentRules {

    private StudentEnrollmentRules() {
    }

    static String nextStatus(String currentStatus) {
        String normalized = currentStatus == null ? "" : currentStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "EN_ATTENTE" -> "VALIDE";
            case "VALIDE" -> "SUSPENDUE";
            case "SUSPENDUE" -> "VALIDE";
            default -> throw new IllegalArgumentException(
                    "Le statut actuel de cette inscription ne peut pas être modifié ici.");
        };
    }
}
