package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the admission conversion rules.
 *
 * <p>The exhaustive cases are the point of this class: the interesting part of the rule is not that a
 * closed visit is refused, but that nothing <em>else</em> is, so the tests walk every status and every
 * type rather than naming a few examples.
 */
class AdmissionConversionTest {

    @Test
    void aVisitThatIsAlreadyAnAdmissionIsNotConvertedAgain() {
        assertThat(AdmissionConversion.isAlreadyAdmitted(VisitType.ADMISSION)).isTrue();
        assertThat(AdmissionConversion.canConvert(VisitType.ADMISSION, VisitStatus.ADMITTED)).isFalse();
    }

    @Test
    void onlyTheFinishedEncounterAndAnExistingAdmissionAreRefused() {
        for (VisitStatus status : VisitStatus.values()) {
            boolean expected =
                status != VisitStatus.CLOSED && status != VisitStatus.CANCELLED && status != VisitStatus.ADMITTED;
            assertThat(AdmissionConversion.canConvert(VisitType.OUTPATIENT, status))
                .as("a %s visit", status)
                .isEqualTo(expected);
        }
    }

    @Test
    void everyStatusIsClassifiedEitherAsStillRunningOrAsPastConversion() {
        // Listed by name rather than derived from the refusal set, so that a status added later fails
        // this test and has to be classified deliberately instead of becoming admissible by accident.
        List<VisitStatus> stillRunning = List.of(
            VisitStatus.REGISTERED,
            VisitStatus.WAITING_VITALS,
            VisitStatus.IN_VITALS,
            VisitStatus.WAITING_DOCTOR,
            VisitStatus.IN_CONSULTATION,
            VisitStatus.WAITING_RESULTS,
            VisitStatus.WAITING_PAYMENT
        );

        assertThat(stillRunning).allMatch(status -> AdmissionConversion.canConvert(VisitType.OUTPATIENT, status));
        assertThat(List.of(VisitStatus.CLOSED, VisitStatus.CANCELLED, VisitStatus.ADMITTED)).noneMatch(status ->
            AdmissionConversion.canConvert(VisitType.OUTPATIENT, status)
        );
        assertThat(stillRunning).hasSize(VisitStatus.values().length - 3);
    }

    @Test
    void anAdmittedVisitOfAnyOtherTypeIsStillRefused() {
        // Defensive: the type is what marks the admission, so a record whose status says ADMITTED while
        // its type says otherwise is corrupt data and must not be converted a second time either.
        for (VisitStatus status : VisitStatus.values()) {
            assertThat(AdmissionConversion.canConvert(VisitType.ADMISSION, status))
                .as("type ADMISSION with status %s", status)
                .isFalse();
        }
    }

    @Test
    void anOutpatientOrEmergencyVisitCanBeConverted() {
        assertThat(AdmissionConversion.canConvert(VisitType.OUTPATIENT, VisitStatus.IN_CONSULTATION)).isTrue();
        assertThat(AdmissionConversion.canConvert(VisitType.EMERGENCY, VisitStatus.IN_CONSULTATION)).isTrue();
        assertThat(AdmissionConversion.canConvert(VisitType.OUTPATIENT, VisitStatus.WAITING_RESULTS)).isTrue();
        assertThat(AdmissionConversion.canConvert(VisitType.OUTPATIENT, VisitStatus.WAITING_PAYMENT)).isTrue();
    }

    @Test
    void aPatientNobodyHasSeenHasNotBeenAssessed() {
        assertThat(AdmissionConversion.hasBeenAssessed(null)).isFalse();
    }

    @Test
    void beingAssessedMeansTheConsultationIsRunningOrFinished() {
        assertThat(AdmissionConversion.hasBeenAssessed(ConsultationStatus.IN_PROGRESS)).isTrue();
        assertThat(AdmissionConversion.hasBeenAssessed(ConsultationStatus.COMPLETED)).isTrue();
    }
}
