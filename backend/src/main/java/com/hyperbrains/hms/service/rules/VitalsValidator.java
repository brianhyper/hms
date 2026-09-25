package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.config.HmsProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks recorded vital signs against two genuinely different thresholds.
 *
 * <p>The distinction is the whole point of this class, and collapsing it would be a clinical bug:
 * <ul>
 *   <li>A reading outside the <em>healthy</em> band but still biologically possible is a
 *       {@link Warning}. It is saved, and the nurse is told, because a real patient can genuinely
 *       have a pulse of 130 and suppressing that would lose the finding.</li>
 *   <li>A reading outside the <em>physiologically possible</em> band is a {@link Rejection}. It is
 *       refused outright, because a pulse of 400 is a typing mistake, and admitting it to the
 *       clinical record makes the record lie.</li>
 * </ul>
 *
 * <p>Pure: the thresholds arrive as configuration, so every rule is testable without a database
 * and the tests exercise the values actually shipped in {@code application.yml}.
 */
public final class VitalsValidator {

    private VitalsValidator() {}

    /** Outside the healthy band. Saved, and surfaced to the nurse. */
    public record Warning(String field, Double value, Double warnMin, Double warnMax, String message) {}

    /** Outside the range a living person can produce. Refused. */
    public record Rejection(String field, Double value, Double hardMin, Double hardMax, String message) {}

    public record Outcome(List<Warning> warnings, List<Rejection> rejections) {
        public boolean isRejected() {
            return !rejections.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    /** Whatever the nurse entered. Any field may be absent — vitals are often partial. */
    public record Readings(
        BigDecimal temperature,
        Integer pulseRate,
        Integer systolicBp,
        Integer diastolicBp,
        Integer oxygenSaturation,
        BigDecimal weight,
        BigDecimal height
    ) {}

    public static Outcome validate(Readings readings, HmsProperties.Vitals ranges) {
        List<Warning> warnings = new ArrayList<>();
        List<Rejection> rejections = new ArrayList<>();

        check("temperature", decimal(readings.temperature()), ranges.getTemperature(), "Temperature", "C", warnings, rejections);
        check("pulseRate", integer(readings.pulseRate()), ranges.getPulseRate(), "Pulse rate", "bpm", warnings, rejections);
        check("systolicBp", integer(readings.systolicBp()), ranges.getSystolicBp(), "Systolic pressure", "mmHg", warnings, rejections);
        check("diastolicBp", integer(readings.diastolicBp()), ranges.getDiastolicBp(), "Diastolic pressure", "mmHg", warnings, rejections);
        check(
            "oxygenSaturation",
            integer(readings.oxygenSaturation()),
            ranges.getOxygenSaturation(),
            "Oxygen saturation",
            "%",
            warnings,
            rejections
        );
        check("weight", decimal(readings.weight()), ranges.getWeight(), "Weight", "kg", warnings, rejections);
        check("height", decimal(readings.height()), ranges.getHeight(), "Height", "cm", warnings, rejections);

        // Cross-field: the two pressures are only meaningful relative to each other, and a
        // transposed pair is a common entry error that no single-field range would catch.
        if (readings.systolicBp() != null && readings.diastolicBp() != null && readings.systolicBp() <= readings.diastolicBp()) {
            rejections.add(
                new Rejection(
                    "systolicBp",
                    readings.systolicBp().doubleValue(),
                    null,
                    null,
                    "Systolic pressure must be higher than diastolic pressure"
                )
            );
        }

        return new Outcome(List.copyOf(warnings), List.copyOf(rejections));
    }

    /**
     * BMI, derived rather than accepted from the client.
     *
     * <p>The entity has a {@code bmi} column, so it would be easy to let the caller fill it in —
     * but then it could disagree with the weight and height recorded beside it, and a derived
     * clinical figure that contradicts its own inputs is worse than no figure. Returns null when
     * either input is missing, leaving the column empty rather than inventing a value.
     */
    public static BigDecimal bmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null || heightCm.signum() <= 0) {
            return null;
        }
        BigDecimal metres = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(metres.multiply(metres), 2, RoundingMode.HALF_UP);
    }

    private static void check(
        String field,
        Double value,
        HmsProperties.Range range,
        String label,
        String unit,
        List<Warning> warnings,
        List<Rejection> rejections
    ) {
        if (value == null || range == null) {
            return;
        }

        if (outside(value, range.getHardMin(), range.getHardMax())) {
            rejections.add(
                new Rejection(
                    field,
                    value,
                    range.getHardMin(),
                    range.getHardMax(),
                    label + " " + num(value) + " is not physiologically possible (expected " + band(range.getHardMin(), range.getHardMax(), unit) + ")"
                )
            );
            return;
        }

        // Only reached when the reading is possible, so a warning and a rejection are mutually
        // exclusive for the same field: a value can never be both saved and refused.
        if (outside(value, range.getWarnMin(), range.getWarnMax())) {
            warnings.add(
                new Warning(
                    field,
                    value,
                    range.getWarnMin(),
                    range.getWarnMax(),
                    label + " " + num(value) + " is outside the normal range of " + band(range.getWarnMin(), range.getWarnMax(), unit) + "; saved, but worth checking"
                )
            );
        }
    }

    private static boolean outside(double value, Double min, Double max) {
        return (min != null && value < min) || (max != null && value > max);
    }

    private static String band(Double min, Double max, String unit) {
        if (min == null && max == null) {
            return "any value";
        }
        if (min == null) {
            return "at most " + num(max) + " " + unit;
        }
        if (max == null) {
            return "at least " + num(min) + " " + unit;
        }
        return num(min) + "-" + num(max) + " " + unit;
    }

    /** Whole numbers read as whole numbers: "60" rather than "60.0", which matters in a message a nurse reads. */
    private static String num(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : "%.1f".formatted(value);
    }

    private static Double decimal(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private static Double integer(Integer value) {
        return value == null ? null : value.doubleValue();
    }
}
