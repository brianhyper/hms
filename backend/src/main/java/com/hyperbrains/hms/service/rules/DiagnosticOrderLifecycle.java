package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import java.util.Set;

/**
 * When a diagnostic order is finished with.
 *
 * <p>Kept as its own rule because "resolved" is what releases a visit toward payment, and it is not
 * the same question as "did it come back". A cancelled order is resolved: it is never going to
 * produce a result, so leaving it outstanding would strand the visit forever waiting for something
 * that will not happen.
 */
public final class DiagnosticOrderLifecycle {

    /** Still needs work: nobody has finished with it yet. */
    public static final Set<OrderStatus> OUTSTANDING = Set.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS);

    /** What a lab or radiology worklist shows. */
    public static final Set<OrderStatus> WORKLIST = OUTSTANDING;

    private DiagnosticOrderLifecycle() {}

    /** Nothing more will happen: either it produced a result, or it was cancelled. */
    public static boolean isResolved(OrderStatus status) {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }

    /** A result can only be entered while the order is still outstanding. */
    public static boolean acceptsResult(OrderStatus status) {
        return OUTSTANDING.contains(status);
    }

    /**
     * Only an outstanding order can be cancelled.
     *
     * <p>A completed order has already been charged for, and undoing that is a refund — a Finance
     * action, not a clinical one. Refusing here keeps money out of a clinical endpoint.
     */
    public static boolean isCancellable(OrderStatus status) {
        return OUTSTANDING.contains(status);
    }
}
