package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.service.rules.VisitStatusDeriver.PendingWork;
import org.junit.jupiter.api.Test;

/**
 * The rule about where a visit goes after a consultation, over every combination of pending work.
 */
class VisitStatusDeriverTest {

    @Test
    void withNothingOutstandingTheVisitIsReadyToPay() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, PendingWork.none())).isEqualTo(VisitStatus.WAITING_PAYMENT);
    }

    @Test
    void anOpenOrderHoldsTheVisitBack() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, new PendingWork(1, 0))).isEqualTo(
            VisitStatus.WAITING_RESULTS
        );
    }

    @Test
    void anUnresolvedPrescriptionAlsoHoldsTheVisitBack() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, new PendingWork(0, 1))).isEqualTo(
            VisitStatus.WAITING_RESULTS
        );
    }

    /**
     * A blood test and a prescription in the same consultation is routine, which is exactly why the
     * visit cannot be represented as a single linear step. Two outstanding things are still one
     * status, because the status means "something is outstanding", not "a lab is running".
     */
    @Test
    void severalOutstandingThingsCollapseToOneStatus() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, new PendingWork(2, 3))).isEqualTo(
            VisitStatus.WAITING_RESULTS
        );
    }

    /** Only the counts matter, not which kind — the state is a predicate, not a step. */
    @Test
    void theStatusDoesNotDependOnWhichKindIsOutstanding() {
        VisitStatus ordersOnly = VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, new PendingWork(3, 0));
        VisitStatus prescriptionsOnly = VisitStatusDeriver.afterConsultation(VisitType.OUTPATIENT, new PendingWork(0, 3));

        assertThat(ordersOnly).isEqualTo(prescriptionsOnly);
    }

    /**
     * Admission leaves the outpatient path entirely, so an admitted patient must never be pushed
     * into a payment queue however little is outstanding.
     */
    @Test
    void anAdmittedPatientNeverAdvancesToPayment() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.ADMISSION, PendingWork.none())).isEqualTo(VisitStatus.ADMITTED);
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.ADMISSION, new PendingWork(5, 5))).isEqualTo(
            VisitStatus.ADMITTED
        );
    }

    @Test
    void aPharmacyOnlyVisitFollowsTheSameRule() {
        assertThat(VisitStatusDeriver.afterConsultation(VisitType.PHARMACY_ONLY, PendingWork.none())).isEqualTo(
            VisitStatus.WAITING_PAYMENT
        );
    }

    @Test
    void pendingWorkKnowsWhenItIsEmpty() {
        assertThat(PendingWork.none().nothingOutstanding()).isTrue();
        assertThat(new PendingWork(0, 0).nothingOutstanding()).isTrue();
        assertThat(new PendingWork(1, 0).nothingOutstanding()).isFalse();
        assertThat(new PendingWork(0, 1).nothingOutstanding()).isFalse();
    }
}
