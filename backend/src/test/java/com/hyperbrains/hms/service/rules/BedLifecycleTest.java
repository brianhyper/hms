package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.BedStatus;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pure rules for a bed's status. No database involved. */
class BedLifecycleTest {

    /**
     * Every status and the statuses it may move to, written out rather than derived from the class under
     * test. Adding a status to the enum therefore fails here until somebody decides what it may do, which
     * is the point: a new status that quietly inherits permission to be assigned is how two patients end
     * up in one bed.
     */
    private static final Map<BedStatus, Set<BedStatus>> EXPECTED = Map.of(
        BedStatus.AVAILABLE,
        Set.of(BedStatus.MAINTENANCE),
        BedStatus.OCCUPIED,
        Set.of(BedStatus.CLEANING),
        BedStatus.CLEANING,
        Set.of(BedStatus.AVAILABLE, BedStatus.MAINTENANCE),
        BedStatus.MAINTENANCE,
        Set.of(BedStatus.AVAILABLE)
    );

    @Test
    void onlyAnAvailableBedMayBeGivenToAPatient() {
        for (BedStatus status : BedStatus.values()) {
            assertThat(BedLifecycle.isAssignable(status))
                .as("a bed that is %s", status)
                .isEqualTo(status == BedStatus.AVAILABLE);
        }
    }

    /** The one combination that loses data if it is ever wrong: a second patient in a bed cannot be undone. */
    @Test
    void noBedIsEverBothAssignableAndHoldingAPatient() {
        for (BedStatus status : BedStatus.values()) {
            assertThat(BedLifecycle.isAssignable(status) && BedLifecycle.isHoldingPatient(status))
                .as("a bed that is %s cannot be free and occupied at once", status)
                .isFalse();
        }
    }

    @Test
    void aPatientLeavingLeavesTheBedNeedingCleaning() {
        assertThat(BedLifecycle.statusAfterVacating()).isEqualTo(BedStatus.CLEANING);
        assertThat(BedLifecycle.isAssignable(BedLifecycle.statusAfterVacating()))
            .as("a bed that has just been vacated must not be offered to the next patient")
            .isFalse();
    }

    @Test
    void everyStatusHasExactlyTheMovesTheLifecycleDescribes() {
        assertThat(EXPECTED.keySet()).containsExactlyInAnyOrder(BedStatus.values());
        for (BedStatus from : BedStatus.values()) {
            assertThat(BedLifecycle.allowedTransitionsFrom(from))
                .as("moves out of %s", from)
                .containsExactlyInAnyOrderElementsOf(EXPECTED.get(from));
        }
    }

    /**
     * The moves worth naming individually, because each is a rule somebody would otherwise assume the
     * other way round.
     */
    @Test
    void theMovesThatMatterAreTheOnesTheSpecificationDescribes() {
        assertThat(BedLifecycle.canTransition(BedStatus.OCCUPIED, BedStatus.CLEANING))
            .as("discharge starts the cleaning cycle")
            .isTrue();
        assertThat(BedLifecycle.canTransition(BedStatus.CLEANING, BedStatus.AVAILABLE))
            .as("cleaning is closed by hand, since housekeeping is not a role")
            .isTrue();
        assertThat(BedLifecycle.canTransition(BedStatus.CLEANING, BedStatus.MAINTENANCE))
            .as("a bed found broken while being cleaned must not have to pass through available")
            .isTrue();
        assertThat(BedLifecycle.canTransition(BedStatus.MAINTENANCE, BedStatus.AVAILABLE))
            .as("repair is closed by hand")
            .isTrue();

        assertThat(BedLifecycle.canTransition(BedStatus.AVAILABLE, BedStatus.OCCUPIED))
            .as("assignment is its own action with its own guards, not a status edit")
            .isFalse();
        assertThat(BedLifecycle.canTransition(BedStatus.OCCUPIED, BedStatus.AVAILABLE))
            .as("the cleaning step cannot be skipped")
            .isFalse();
        assertThat(BedLifecycle.canTransition(BedStatus.OCCUPIED, BedStatus.MAINTENANCE))
            .as("the patient has to leave before the bed can go out of service")
            .isFalse();
        assertThat(BedLifecycle.canTransition(BedStatus.AVAILABLE, BedStatus.CLEANING))
            .as("a clean bed does not become dirty by decree")
            .isFalse();
        assertThat(BedLifecycle.canTransition(BedStatus.MAINTENANCE, BedStatus.CLEANING))
            .as("a repaired bed is declared ready by the person who checked it")
            .isFalse();
    }

    @Test
    void nothingMovesToWhereItAlreadyIsAndNothingMovesFromUnset() {
        for (BedStatus status : BedStatus.values()) {
            assertThat(BedLifecycle.canTransition(status, status)).as("%s to itself", status).isFalse();
            assertThat(BedLifecycle.canTransition(null, status)).as("from unset to %s", status).isFalse();
            assertThat(BedLifecycle.canTransition(status, null)).as("from %s to unset", status).isFalse();
        }
        assertThat(BedLifecycle.allowedTransitionsFrom(null)).isEmpty();
    }
}
