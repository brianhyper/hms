package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import org.junit.jupiter.api.Test;

/** Pure rules for a doctor's order on the ward. No database involved. */
class DoctorOrderLifecycleTest {

    @Test
    void aPlacedOrderIsRunningImmediately() {
        assertThat(DoctorOrderLifecycle.statusOnPlacing()).isEqualTo(DoctorOrderStatus.ACTIVE);
        assertThat(DoctorOrderLifecycle.isExecutable(DoctorOrderLifecycle.statusOnPlacing())).isTrue();
    }

    @Test
    void onlyARunningOrderCanBeCarriedOut() {
        for (DoctorOrderStatus status : DoctorOrderStatus.values()) {
            assertThat(DoctorOrderLifecycle.isExecutable(status))
                .as("an order that is %s", status)
                .isEqualTo(status == DoctorOrderStatus.ACTIVE);
            assertThat(DoctorOrderLifecycle.isFinished(status))
                .as("an order that is %s", status)
                .isEqualTo(status != DoctorOrderStatus.ACTIVE);
        }
        assertThat(DoctorOrderLifecycle.isExecutable(DoctorOrderStatus.ACTIVE))
            .as("not both running and finished")
            .isNotEqualTo(DoctorOrderLifecycle.isFinished(DoctorOrderStatus.ACTIVE));
    }

    /**
     * The judgement this class exists to keep: doing a one-off is finishing it, doing a dose of a recurring
     * course is not.
     */
    @Test
    void oneExecutionFinishesAOneOffOrderButNotARecurringOne() {
        assertThat(DoctorOrderLifecycle.completesOnExecution(DoctorOrderRecurrence.ONE_OFF)).isTrue();
        assertThat(DoctorOrderLifecycle.completesOnExecution(DoctorOrderRecurrence.RECURRING))
            .as("a nurse does not decide that a course of antibiotics is over")
            .isFalse();
    }

    @Test
    void onlyADrugOrderNeedsSomethingBehindIt() {
        for (DoctorOrderType type : DoctorOrderType.values()) {
            assertThat(DoctorOrderLifecycle.requiresAPrescription(type))
                .as("a %s order", type)
                .isEqualTo(type == DoctorOrderType.DRUG);
        }
    }

    @Test
    void anInstructionIsNeverBillable() {
        for (DoctorOrderType type : DoctorOrderType.values()) {
            assertThat(DoctorOrderLifecycle.isBillable(type))
                .as("a %s order", type)
                .isEqualTo(type != DoctorOrderType.INSTRUCTION);
        }
    }
}
