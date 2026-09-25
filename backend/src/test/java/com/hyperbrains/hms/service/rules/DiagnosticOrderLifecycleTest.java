package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The rule that decides whether a visit is still waiting on a test.
 *
 * <p>Worth testing directly because the two mistakes it guards against are both silent: treating a
 * cancelled order as outstanding strands the visit forever, and treating a completed one as
 * outstanding sends the patient to the payment desk while a result is still missing.
 */
class DiagnosticOrderLifecycleTest {

    @Test
    void anOutstandingOrderNeverCountsAsResolved() {
        assertThat(DiagnosticOrderLifecycle.isResolved(OrderStatus.PENDING)).isFalse();
        assertThat(DiagnosticOrderLifecycle.isResolved(OrderStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    void bothACompletedAndACancelledOrderAreResolved() {
        assertThat(DiagnosticOrderLifecycle.isResolved(OrderStatus.COMPLETED)).isTrue();
        assertThat(DiagnosticOrderLifecycle.isResolved(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void resolutionAndOutstandingnessAreExactOpposites() {
        // If these ever disagree, a visit can be released while work is still pending, or held while
        // nothing is. Asserting the complement on every value keeps the two definitions tied.
        for (OrderStatus status : OrderStatus.values()) {
            assertThat(DiagnosticOrderLifecycle.isResolved(status))
                .as("%s", status)
                .isNotEqualTo(DiagnosticOrderLifecycle.OUTSTANDING.contains(status));
        }
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = { "PENDING", "IN_PROGRESS" })
    void anOutstandingOrderAcceptsAResultAndCanBeCancelled(OrderStatus status) {
        assertThat(DiagnosticOrderLifecycle.acceptsResult(status)).isTrue();
        assertThat(DiagnosticOrderLifecycle.isCancellable(status)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = { "COMPLETED", "CANCELLED" })
    void aResolvedOrderAcceptsNothing(OrderStatus status) {
        assertThat(DiagnosticOrderLifecycle.acceptsResult(status)).isFalse();
        assertThat(DiagnosticOrderLifecycle.isCancellable(status)).isFalse();
    }

    /** A completed order is already on the bill, so removing it is a refund, not a cancellation. */
    @Test
    void aCompletedOrderCannotBeCancelledToAvoidItsCharge() {
        assertThat(DiagnosticOrderLifecycle.isCancellable(OrderStatus.COMPLETED)).isFalse();
    }
}
