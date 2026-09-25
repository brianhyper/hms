package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import java.util.Set;

/**
 * The work queues the hospital staff actually stand in front of.
 *
 * <p>Defined once, as sets of visit statuses, so that the screen which shows a queue and the code
 * which decides whether a selection skipped the queue cannot disagree about what "the queue" is.
 */
public enum QueueKind {
    /** Waiting for a nurse to record vitals. */
    VITALS(Set.of(VisitStatus.WAITING_VITALS)),

    /** Vitals done, waiting for a doctor to pick the patient up. */
    CONSULTATION(Set.of(VisitStatus.WAITING_DOCTOR)),

    /** Every visit still in progress, for supervision rather than for calling the next patient. */
    ACTIVE(
        Set.of(
            VisitStatus.REGISTERED,
            VisitStatus.WAITING_VITALS,
            VisitStatus.IN_VITALS,
            VisitStatus.WAITING_DOCTOR,
            VisitStatus.IN_CONSULTATION,
            VisitStatus.WAITING_RESULTS,
            VisitStatus.WAITING_PAYMENT
        )
    );

    private final Set<VisitStatus> statuses;

    QueueKind(Set<VisitStatus> statuses) {
        this.statuses = statuses;
    }

    public Set<VisitStatus> statuses() {
        return statuses;
    }
}
