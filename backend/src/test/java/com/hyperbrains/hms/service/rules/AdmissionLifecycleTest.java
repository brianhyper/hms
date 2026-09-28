package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pure rules for an admission's status. No database involved. */
class AdmissionLifecycleTest {

    /**
     * Every status and the statuses it may move to, written out rather than derived from the class under
     * test — so a status added to the enum fails here until somebody decides what it may do.
     */
    private static final Map<AdmissionStatus, Set<AdmissionStatus>> EXPECTED = Map.of(
        AdmissionStatus.PENDING_BED,
        Set.of(AdmissionStatus.ADMITTED, AdmissionStatus.DISCHARGED),
        AdmissionStatus.ADMITTED,
        Set.of(AdmissionStatus.DISCHARGED),
        AdmissionStatus.DISCHARGED,
        Set.of()
    );

    @Test
    void aStayBeginsWaitingForABed() {
        assertThat(AdmissionLifecycle.statusOnAdmission()).isEqualTo(AdmissionStatus.PENDING_BED);
        assertThat(AdmissionLifecycle.awaitsABed(AdmissionLifecycle.statusOnAdmission()))
            .as("the doctor's decision and the bed being found are different moments")
            .isTrue();
        assertThat(AdmissionLifecycle.isOpen(AdmissionLifecycle.statusOnAdmission())).isTrue();
    }

    /**
     * The distinction the whole class exists for: a stay that is waiting for a bed is already open and
     * already an inpatient stay, but it does not hold a bed.
     */
    @Test
    void waitingForABedAndHoldingOneAreDifferentStates() {
        for (AdmissionStatus status : AdmissionStatus.values()) {
            assertThat(AdmissionLifecycle.awaitsABed(status) && AdmissionLifecycle.holdsABed(status))
                .as("a stay that is %s cannot be both waiting for a bed and in one", status)
                .isFalse();
        }
        assertThat(AdmissionLifecycle.awaitsABed(AdmissionStatus.PENDING_BED)).isTrue();
        assertThat(AdmissionLifecycle.holdsABed(AdmissionStatus.PENDING_BED)).isFalse();
        assertThat(AdmissionLifecycle.holdsABed(AdmissionStatus.ADMITTED)).isTrue();
    }

    @Test
    void aStayIsOpenUntilItIsDischarged() {
        for (AdmissionStatus status : AdmissionStatus.values()) {
            assertThat(AdmissionLifecycle.isOpen(status))
                .as("a stay that is %s", status)
                .isEqualTo(status != AdmissionStatus.DISCHARGED);
        }
    }

    @Test
    void givingSomebodyABedPutsTheStayIntoAdmitted() {
        assertThat(AdmissionLifecycle.statusAfterBedAssigned()).isEqualTo(AdmissionStatus.ADMITTED);
    }

    @Test
    void everyStatusHasExactlyTheMovesTheLifecycleDescribes() {
        assertThat(EXPECTED.keySet()).containsExactlyInAnyOrder(AdmissionStatus.values());
        for (AdmissionStatus from : AdmissionStatus.values()) {
            assertThat(AdmissionLifecycle.allowedTransitionsFrom(from))
                .as("moves out of %s", from)
                .containsExactlyInAnyOrderElementsOf(EXPECTED.get(from));
        }
    }

    /**
     * The moves worth naming, because two of them are decisions rather than mechanics: a patient can be
     * sent home before a bed was ever found, and a discharged stay is finished for good.
     */
    @Test
    void theMovesThatMatterAreTheOnesTheSpecificationDescribes() {
        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.PENDING_BED, AdmissionStatus.ADMITTED))
            .as("a bed was found")
            .isTrue();
        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.PENDING_BED, AdmissionStatus.DISCHARGED))
            .as("a patient who never got a bed still has to be able to leave")
            .isTrue();
        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.ADMITTED, AdmissionStatus.DISCHARGED))
            .as("the ordinary discharge")
            .isTrue();

        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.ADMITTED, AdmissionStatus.PENDING_BED))
            .as("losing a bed is a transfer, not a step backwards")
            .isFalse();
        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.DISCHARGED, AdmissionStatus.ADMITTED))
            .as("readmitting somebody is a new stay on a new visit")
            .isFalse();
        assertThat(AdmissionLifecycle.canTransition(AdmissionStatus.DISCHARGED, AdmissionStatus.PENDING_BED)).isFalse();
    }

    @Test
    void nothingMovesToWhereItAlreadyIsAndNothingMovesFromUnset() {
        for (AdmissionStatus status : AdmissionStatus.values()) {
            assertThat(AdmissionLifecycle.canTransition(status, status)).as("%s to itself", status).isFalse();
            assertThat(AdmissionLifecycle.canTransition(null, status)).as("from unset to %s", status).isFalse();
            assertThat(AdmissionLifecycle.canTransition(status, null)).as("from %s to unset", status).isFalse();
        }
        assertThat(AdmissionLifecycle.allowedTransitionsFrom(null)).isEmpty();
    }
}
