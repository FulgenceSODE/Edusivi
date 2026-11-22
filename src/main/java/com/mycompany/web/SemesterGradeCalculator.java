package com.mycompany.web;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SemesterGradeCalculator {

    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;
    private static final Set<String> CATEGORIES = Set.of("INTERROGATION", "DEVOIR", "EXAMEN");

    private SemesterGradeCalculator() {
    }

    public record Enrollment(Long id, String matricule, String nom, String prenom) {
    }

    public record Composition(
            Long ueId,
            String ueCode,
            String ueLabel,
            Long ecId,
            String ecCode,
            String ecLabel,
            BigDecimal coefficient,
            Integer order) {
    }

    public record Grade(Long enrollmentId, Long ecId, String category, BigDecimal value, boolean published) {
    }

    public record StudentResult(Enrollment enrollment, List<UeResult> ueResults, BigDecimal semesterAverage) {
        public StudentResult {
            ueResults = List.copyOf(ueResults);
        }
    }

    public record UeResult(String code, String label, List<EcResult> ecResults, BigDecimal average) {
        public UeResult {
            ecResults = List.copyOf(ecResults);
        }
    }

    public record EcResult(
            String code,
            String label,
            BigDecimal coefficient,
            Map<String, BigDecimal> categoryAverages,
            BigDecimal average) {
        public EcResult {
            categoryAverages = Map.copyOf(categoryAverages);
        }
    }

    public static List<StudentResult> calculate(
            List<Enrollment> enrollments,
            List<Composition> compositions,
            List<Grade> grades) {
        List<Composition> orderedCompositions = compositions.stream()
                .sorted(Comparator.comparing(Composition::ueCode)
                        .thenComparing(Composition::order, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        if (orderedCompositions.stream().anyMatch(
                composition -> composition.coefficient() == null || composition.coefficient().signum() <= 0)) {
            throw new IllegalStateException("Un coefficient d’EC doit être strictement positif.");
        }
        Map<Long, Map<Long, Map<String, List<BigDecimal>>>> scores = collectValidPublishedScores(grades);
        List<StudentResult> results = new ArrayList<>(enrollments.size());

        for (Enrollment enrollment : enrollments) {
            Map<Long, EcResult> ecResults = calculateEcResults(
                    scores.getOrDefault(enrollment.id(), Map.of()), orderedCompositions);
            Map<Long, UeAccumulator> ueAccumulators = new LinkedHashMap<>();
            for (Composition composition : orderedCompositions) {
                UeAccumulator ue = ueAccumulators.computeIfAbsent(
                        composition.ueId(),
                        ignored -> new UeAccumulator(composition.ueCode(), composition.ueLabel()));
                EcResult ec = ecResults.get(composition.ecId());
                ue.ecResults.add(new EcResult(
                        composition.ecCode(),
                        composition.ecLabel(),
                        composition.coefficient(),
                        ec.categoryAverages(),
                        ec.average()));
                if (ec.average() != null) {
                    ue.weightedTotal = ue.weightedTotal.add(
                            ec.average().multiply(composition.coefficient(), CALCULATION_CONTEXT),
                            CALCULATION_CONTEXT);
                    ue.coefficientTotal = ue.coefficientTotal.add(composition.coefficient());
                }
            }

            List<UeResult> ueResults = new ArrayList<>(ueAccumulators.size());
            List<BigDecimal> availableUeMeans = new ArrayList<>();
            for (UeAccumulator ue : ueAccumulators.values()) {
                BigDecimal ueAverage = ue.coefficientTotal.signum() == 0
                        ? null
                        : ue.weightedTotal.divide(ue.coefficientTotal, CALCULATION_CONTEXT);
                ueResults.add(new UeResult(ue.code, ue.label, ue.ecResults, ueAverage));
                if (ueAverage != null) {
                    availableUeMeans.add(ueAverage);
                }
            }
            results.add(new StudentResult(enrollment, ueResults, arithmeticMean(availableUeMeans)));
        }
        return List.copyOf(results);
    }

    public static BigDecimal roundForDisplay(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatForDisplay(BigDecimal value) {
        BigDecimal rounded = roundForDisplay(value);
        return rounded == null
                ? "Non calculée"
                : new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(java.util.Locale.FRANCE))
                        .format(rounded);
    }

    private static Map<Long, Map<Long, Map<String, List<BigDecimal>>>> collectValidPublishedScores(
            List<Grade> grades) {
        Map<Long, Map<Long, Map<String, List<BigDecimal>>>> scores = new HashMap<>();
        for (Grade grade : grades) {
            if (!grade.published() || grade.value() == null
                    || grade.value().compareTo(BigDecimal.ZERO) < 0
                    || grade.value().compareTo(BigDecimal.valueOf(20)) > 0) {
                continue;
            }
            String category = grade.category() == null
                    ? ""
                    : grade.category().trim().toUpperCase(java.util.Locale.ROOT);
            if (!CATEGORIES.contains(category)) {
                throw new IllegalStateException("Catégorie d’évaluation invalide : " + category);
            }
            scores.computeIfAbsent(grade.enrollmentId(), ignored -> new HashMap<>())
                    .computeIfAbsent(grade.ecId(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(category, ignored -> new ArrayList<>())
                    .add(grade.value());
        }
        return scores;
    }

    private static Map<Long, EcResult> calculateEcResults(
            Map<Long, Map<String, List<BigDecimal>>> enrollmentScores,
            List<Composition> compositions) {
        Map<Long, EcResult> results = new HashMap<>();
        for (Composition composition : compositions) {
            results.computeIfAbsent(composition.ecId(), ecId -> {
                Map<String, BigDecimal> categoryAverages = new LinkedHashMap<>();
                Map<String, List<BigDecimal>> categoryScores =
                        enrollmentScores.getOrDefault(ecId, Map.of());
                for (String category : List.of("INTERROGATION", "DEVOIR", "EXAMEN")) {
                    List<BigDecimal> values = categoryScores.get(category);
                    if (values != null && !values.isEmpty()) {
                        categoryAverages.put(category, arithmeticMean(values));
                    }
                }
                return new EcResult(
                        composition.ecCode(),
                        composition.ecLabel(),
                        composition.coefficient(),
                        categoryAverages,
                        arithmeticMean(new ArrayList<>(categoryAverages.values())));
            });
        }
        return results;
    }

    private static BigDecimal arithmeticMean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), CALCULATION_CONTEXT);
    }

    private static final class UeAccumulator {

        private final String code;
        private final String label;
        private final List<EcResult> ecResults = new ArrayList<>();
        private BigDecimal weightedTotal = BigDecimal.ZERO;
        private BigDecimal coefficientTotal = BigDecimal.ZERO;

        private UeAccumulator(String code, String label) {
            this.code = code;
            this.label = label;
        }
    }
}
