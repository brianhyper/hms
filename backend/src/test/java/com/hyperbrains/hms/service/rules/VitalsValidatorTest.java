package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.config.HmsProperties;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * The two-tier vital-signs rule.
 *
 * <p>Uses {@code new HmsProperties()} rather than hand-written numbers, so these tests fail if
 * someone edits the shipped thresholds in {@code application.yml} into something unsafe, rather
 * than silently passing against a copy of the old values.
 */
class VitalsValidatorTest {

    private final HmsProperties.Vitals ranges = new HmsProperties().getVitals();

    @Test
    void aReadingInsideTheHealthyBandIsNeitherWarnedAboutNorRefused() {
        VitalsValidator.Outcome outcome = validate(36.8, 72, 118, 76, 98);

        assertThat(outcome.warnings()).isEmpty();
        assertThat(outcome.rejections()).isEmpty();
        assertThat(outcome.isRejected()).isFalse();
    }

    /**
     * The first tier. A real patient can have a pulse of 130, so refusing it would lose a genuine
     * finding — it is saved, and the nurse is told.
     */
    @Test
    void aPossibleButAbnormalReadingIsWarnedAboutRatherThanRefused() {
        VitalsValidator.Outcome outcome = validate(36.8, 130, null, null, null);

        assertThat(outcome.isRejected()).isFalse();
        assertThat(outcome.warnings())
            .singleElement()
            .satisfies(warning -> {
                assertThat(warning.field()).isEqualTo("pulseRate");
                assertThat(warning.value()).isEqualTo(130.0);
                assertThat(warning.warnMin()).isEqualTo(60.0);
                assertThat(warning.warnMax()).isEqualTo(100.0);
                // The message a nurse reads has to contain the number and the expected band.
                assertThat(warning.message()).contains("130").contains("60-100").contains("bpm");
            });
    }

    /** The second tier. A pulse of 400 is a typing mistake, so the submission is refused. */
    @Test
    void anImpossibleReadingIsRefused() {
        VitalsValidator.Outcome outcome = validate(null, 400, null, null, null);

        assertThat(outcome.isRejected()).isTrue();
        assertThat(outcome.rejections())
            .singleElement()
            .satisfies(rejection -> {
                assertThat(rejection.field()).isEqualTo("pulseRate");
                assertThat(rejection.message()).contains("400").contains("not physiologically possible").contains("20-300");
            });
    }

    /**
     * The two behaviours must not overlap. If a refused field also produced a warning, the client
     * would have to decide which one wins, and the distinction the spec asks for would be gone.
     */
    @Test
    void aRefusedReadingIsNeverAlsoWarnedAbout() {
        assertThat(validate(null, 400, null, null, null).warnings()).isEmpty();
        assertThat(validate(50.0, null, null, null, null).warnings()).isEmpty();
        assertThat(validate(50.0, null, null, null, null).rejections()).hasSize(1);
    }

    @Test
    void theEdgesOfTheHealthyBandAreInsideIt() {
        assertThat(validate(null, 60, null, null, null).warnings()).isEmpty();
        assertThat(validate(null, 100, null, null, null).warnings()).isEmpty();
    }

    @Test
    void theEdgesOfThePossibleBandAreAcceptedAndWarnedAbout() {
        // 20 is the lowest possible pulse, so it is saved — but it is nowhere near healthy.
        VitalsValidator.Outcome lowest = validate(null, 20, null, null, null);
        assertThat(lowest.isRejected()).isFalse();
        assertThat(lowest.warnings()).hasSize(1);

        // 300 is the highest possible, and equally alarming.
        VitalsValidator.Outcome highest = validate(null, 300, null, null, null);
        assertThat(highest.isRejected()).isFalse();
        assertThat(highest.warnings()).hasSize(1);
    }

    @Test
    void oneStepBeyondThePossibleBandIsRefused() {
        assertThat(validate(null, 19, null, null, null).isRejected()).isTrue();
        assertThat(validate(null, 301, null, null, null).isRejected()).isTrue();
        assertThat(validate(29.9, null, null, null, null).isRejected()).isTrue();
        assertThat(validate(45.1, null, null, null, null).isRejected()).isTrue();
    }

    /** A partial set of vitals is normal and must not be treated as a mistake. */
    @Test
    void absentReadingsAreNeitherWarnedAboutNorRefused() {
        VitalsValidator.Outcome outcome = VitalsValidator.validate(
            new VitalsValidator.Readings(null, null, null, null, null, null, null),
            ranges
        );

        assertThat(outcome.warnings()).isEmpty();
        assertThat(outcome.rejections()).isEmpty();
    }

    /** No single-field range catches a transposed pair, which is why this check exists. */
    @Test
    void aTransposedBloodPressureIsRefused() {
        VitalsValidator.Outcome outcome = validate(null, null, 80, 120, null);

        assertThat(outcome.isRejected()).isTrue();
        assertThat(outcome.rejections()).singleElement().satisfies(rejection -> {
            assertThat(rejection.field()).isEqualTo("systolicBp");
            assertThat(rejection.message()).contains("must be higher than diastolic");
        });
    }

    @Test
    void equalBloodPressuresAreAlsoRefused() {
        assertThat(validate(null, null, 100, 100, null).isRejected()).isTrue();
    }

    /**
     * Weight and height have no healthy band — a healthy adult weighs anywhere from 45kg to 120kg,
     * so a warning would fire constantly and mean nothing. Only impossible values are refused.
     */
    @Test
    void weightAndHeightOnlyHaveAnImpossibleBand() {
        VitalsValidator.Outcome odd = VitalsValidator.validate(
            new VitalsValidator.Readings(null, null, null, null, null, BigDecimal.valueOf(300), BigDecimal.valueOf(210)),
            ranges
        );
        assertThat(odd.warnings()).isEmpty();
        assertThat(odd.rejections()).isEmpty();

        VitalsValidator.Outcome impossible = VitalsValidator.validate(
            new VitalsValidator.Readings(null, null, null, null, null, BigDecimal.valueOf(900), null),
            ranges
        );
        assertThat(impossible.rejections()).hasSize(1);
    }

    @Test
    void everyImpossibleReadingIsReportedNotJustTheFirst() {
        VitalsValidator.Outcome outcome = validate(50.0, 400, null, null, 5);

        assertThat(outcome.rejections()).extracting(VitalsValidator.Rejection::field).containsExactlyInAnyOrder(
            "temperature",
            "pulseRate",
            "oxygenSaturation"
        );
    }

    @Test
    void bmiIsDerivedFromWeightAndHeight() {
        assertThat(VitalsValidator.bmi(BigDecimal.valueOf(70), BigDecimal.valueOf(175))).isEqualByComparingTo("22.86");
        assertThat(VitalsValidator.bmi(BigDecimal.valueOf(100), BigDecimal.valueOf(200))).isEqualByComparingTo("25.00");
    }

    /**
     * A derived clinical figure that contradicts its own inputs is worse than no figure, so a
     * missing input produces nothing rather than a guess.
     */
    @Test
    void bmiIsAbsentWhenItsInputsAre() {
        assertThat(VitalsValidator.bmi(null, BigDecimal.valueOf(175))).isNull();
        assertThat(VitalsValidator.bmi(BigDecimal.valueOf(70), null)).isNull();
        assertThat(VitalsValidator.bmi(BigDecimal.valueOf(70), BigDecimal.ZERO)).isNull();
    }

    private VitalsValidator.Outcome validate(Double temperature, Integer pulseRate, Integer systolicBp, Integer diastolicBp, Integer oxygenSaturation) {
        return VitalsValidator.validate(
            new VitalsValidator.Readings(
                temperature == null ? null : BigDecimal.valueOf(temperature),
                pulseRate,
                systolicBp,
                diastolicBp,
                oxygenSaturation,
                null,
                null
            ),
            ranges
        );
    }
}
