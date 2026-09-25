package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pure rules for how a visit starts and how a queue is ordered. No database involved. */
class VisitLifecycleTest {

    private static final Instant EARLY = Instant.parse("2026-09-24T08:00:00Z");

    private static final Instant LATE = Instant.parse("2026-09-24T09:00:00Z");

    @Test
    void clinicalVisitsStartWaitingForVitals() {
        assertThat(VisitLifecycle.initialStatus(VisitType.OUTPATIENT)).isEqualTo(VisitStatus.WAITING_VITALS);
        assertThat(VisitLifecycle.initialStatus(VisitType.EMERGENCY)).isEqualTo(VisitStatus.WAITING_VITALS);
    }

    /** A pharmacy-only visit has no triage, so a vitals queue would hold it forever. */
    @Test
    void pharmacyOnlyVisitsDoNotEnterTheVitalsQueue() {
        assertThat(VisitLifecycle.initialStatus(VisitType.PHARMACY_ONLY)).isEqualTo(VisitStatus.REGISTERED);
    }

    @Test
    void admissionCannotBeOpenedDirectly() {
        assertThatThrownBy(() -> VisitLifecycle.initialStatus(VisitType.ADMISSION)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emergencyOutranksUrgentOutranksNormal() {
        assertThat(VisitLifecycle.queueWeight(VisitPriority.EMERGENCY)).isLessThan(VisitLifecycle.queueWeight(VisitPriority.URGENT));
        assertThat(VisitLifecycle.queueWeight(VisitPriority.URGENT)).isLessThan(VisitLifecycle.queueWeight(VisitPriority.NORMAL));
    }

    /** The whole point of priority: an emergency overtakes a routine patient who arrived first. */
    @Test
    void priorityOutranksArrivalTime() {
        Visit routineButEarly = visit(1L, VisitPriority.NORMAL, EARLY);
        Visit emergencyButLate = visit(2L, VisitPriority.EMERGENCY, LATE);

        assertThat(VisitLifecycle.sortQueue(List.of(routineButEarly, emergencyButLate))).extracting(Visit::getId).containsExactly(2L, 1L);
    }

    @Test
    void arrivalOrderBreaksTiesWithinAPriorityBand() {
        Visit early = visit(1L, VisitPriority.NORMAL, EARLY);
        Visit late = visit(2L, VisitPriority.NORMAL, LATE);

        assertThat(VisitLifecycle.sortQueue(List.of(late, early))).extracting(Visit::getId).containsExactly(1L, 2L);
    }

    @Test
    void selectingTheHeadOfTheQueueIsNotOutOfOrder() {
        List<Visit> queue = List.of(visit(1L, VisitPriority.NORMAL, EARLY), visit(2L, VisitPriority.NORMAL, LATE));

        assertThat(VisitLifecycle.isOutOfOrder(queue.getFirst(), queue)).isFalse();
    }

    /** Picking the second patient is allowed, but it has to be justified. */
    @Test
    void selectingPastTheHeadIsOutOfOrder() {
        List<Visit> queue = List.of(visit(1L, VisitPriority.NORMAL, EARLY), visit(2L, VisitPriority.NORMAL, LATE));

        assertThat(VisitLifecycle.isOutOfOrder(queue.get(1), queue)).isTrue();
    }

    @Test
    void nothingCanBeOutOfOrderInAnEmptyQueue() {
        Visit visit = visit(1L, VisitPriority.NORMAL, EARLY);

        assertThat(VisitLifecycle.isOutOfOrder(visit, List.of())).isFalse();
        assertThat(VisitLifecycle.isOutOfOrder(visit, null)).isFalse();
        assertThat(VisitLifecycle.isOutOfOrder(null, List.of(visit))).isFalse();
    }

    /** A visit not yet persisted has no identifier to compare, so it cannot be out of order. */
    @Test
    void anUnsavedVisitCannotBeOutOfOrder() {
        Visit unsaved = visit(null, VisitPriority.NORMAL, EARLY);

        assertThat(VisitLifecycle.isOutOfOrder(unsaved, List.of(visit(1L, VisitPriority.NORMAL, EARLY)))).isFalse();
    }

    @Test
    void closedCancelledAndAdmittedVisitsAreNoLongerOpen() {
        assertThat(VisitLifecycle.isOpen(VisitStatus.CLOSED)).isFalse();
        assertThat(VisitLifecycle.isOpen(VisitStatus.CANCELLED)).isFalse();
        assertThat(VisitLifecycle.isOpen(VisitStatus.ADMITTED)).isFalse();
        assertThat(VisitLifecycle.isOpen(VisitStatus.REGISTERED)).isTrue();
        assertThat(VisitLifecycle.isOpen(VisitStatus.WAITING_PAYMENT)).isTrue();
    }

    private static Visit visit(Long id, VisitPriority priority, Instant createdAt) {
        Visit visit = new Visit();
        visit.setId(id);
        visit.setPriority(priority);
        visit.setCreatedAt(createdAt);
        return visit;
    }
}
