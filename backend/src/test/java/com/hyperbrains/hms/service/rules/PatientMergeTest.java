package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import org.junit.jupiter.api.Test;

/**
 * Who may be merged into whom.
 *
 * <p>The direction is the part worth pinning down: it decides which hospital number survives, and it is
 * the only thing standing between a merge and a real patient's identity being absorbed into a temporary
 * record.
 */
class PatientMergeTest {

    @Test
    void aTemporaryRecordMayAdoptAConfirmedOne() {
        assertThat(PatientMerge.isMergeable(RegistrationStatus.INCOMPLETE_REGISTRATION, RegistrationStatus.COMPLETE)).isTrue();
    }

    /** The temporary record is the one that stops being a patient, so this direction is the wrong way round. */
    @Test
    void aConfirmedRecordMayNotBeMergedIntoATemporaryOne() {
        assertThat(PatientMerge.isMergeable(RegistrationStatus.COMPLETE, RegistrationStatus.INCOMPLETE_REGISTRATION)).isFalse();
    }

    /**
     * Both records are real patients with their own hospital numbers, so which identity survives is a
     * judgement about a person. Losing one of them silently is not a decision this rule will make.
     */
    @Test
    void twoConfirmedRecordsCannotBeMerged() {
        assertThat(PatientMerge.isMergeable(RegistrationStatus.COMPLETE, RegistrationStatus.COMPLETE)).isFalse();
    }

    @Test
    void aRecordThatIsAlreadyAPointerCannotBeAMergeTarget() {
        assertThat(PatientMerge.isMergeable(RegistrationStatus.INCOMPLETE_REGISTRATION, RegistrationStatus.MERGED)).isFalse();
    }

    @Test
    void aRecordThatIsAlreadyAPointerCannotBeMergedAgain() {
        assertThat(PatientMerge.isMergeable(RegistrationStatus.MERGED, RegistrationStatus.COMPLETE)).isFalse();
        assertThat(PatientMerge.isAlreadyMerged(RegistrationStatus.MERGED)).isTrue();
    }

    @Test
    void onlyAnIncompleteRegistrationIsTemporary() {
        assertThat(PatientMerge.isTemporary(RegistrationStatus.INCOMPLETE_REGISTRATION)).isTrue();
        assertThat(PatientMerge.isTemporary(RegistrationStatus.COMPLETE)).isFalse();
        assertThat(PatientMerge.isTemporary(RegistrationStatus.MERGED)).isFalse();
    }

    @Test
    void onlyACompleteRegistrationIsConfirmed() {
        assertThat(PatientMerge.isConfirmed(RegistrationStatus.COMPLETE)).isTrue();
        assertThat(PatientMerge.isConfirmed(RegistrationStatus.INCOMPLETE_REGISTRATION)).isFalse();
        assertThat(PatientMerge.isConfirmed(RegistrationStatus.MERGED)).isFalse();
    }

    /** A merged record is a pointer, not a patient, whichever side of a merge it appears on. */
    @Test
    void everyStatusIsClassifiedExactlyOnce() {
        for (RegistrationStatus status : RegistrationStatus.values()) {
            long classifications =
                (PatientMerge.isTemporary(status) ? 1 : 0) +
                (PatientMerge.isConfirmed(status) ? 1 : 0) +
                (PatientMerge.isAlreadyMerged(status) ? 1 : 0);
            assertThat(classifications).as("%s", status).isEqualTo(1);
        }
    }
}
