package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.domain.enumeration.VisitStatus;

/**
 * Owns the visit's status once it is in the part of the flow that is derived rather than driven by
 * hand.
 *
 * <p>Three entry points, because the events are different in kind:
 * <ul>
 *   <li>{@link #afterConsultation} — the consultation has finished, so the visit leaves the
 *       consultation and becomes "waiting on whatever is outstanding".</li>
 *   <li>{@link #recompute} — something linked to the visit changed (a result arrived, an order was
 *       cancelled). Any of these could be the last outstanding item, so each one recomputes.</li>
 *   <li>{@link #onPrescriptionPlaced} — a prescription was written, which is the one event that can
 *       move a visit with no consultation at all.</li>
 * </ul>
 *
 * <p>Centralised deliberately: the specification requires the check to run on every such event, and
 * scattering the same predicate across four services is how one of them ends up not running it.
 */
public interface VisitStatusService {

    /** Called once, when a consultation completes. */
    VisitStatus afterConsultation(Long visitId);

    /**
     * Called whenever a linked order changes state.
     *
     * <p>A no-op unless the visit is in the derived part of the flow — see the implementation for
     * why that guard matters.
     */
    VisitStatus recompute(Long visitId);

    /**
     * Called when a prescription is written.
     *
     * <p>Separate from {@link #recompute} because a pharmacy-only walk-in has no consultation to
     * finish and no doctor to see it: nothing the derivation looks at would ever fire, so the charge
     * itself is what makes the visit payable. This method is also what keeps a visit that is
     * <em>already</em> at the payment stage honest when a late prescription joins the bill.
     */
    VisitStatus onPrescriptionPlaced(Long visitId);

    /**
     * Called when a prescription is withdrawn, which takes its charge off the bill.
     *
     * <p>Separate from {@link #recompute} for a reason worth stating plainly: a visit already waiting
     * to pay re-derives to the same status, so the derivation short-circuits and the bill would keep
     * a charge for medicine that will never be handed over. Withdrawing therefore has to re-total the
     * bill explicitly rather than rely on the status changing.
     */
    VisitStatus onPrescriptionWithdrawn(Long visitId);

    /**
     * Called when the bill is settled, which is the end of the outpatient encounter.
     *
     * <p>Deliberately not part of the derivation. Everything up to the payment stage is computed from
     * what is outstanding; closure is a decision that the encounter is over, and deriving it would
     * mean a visit that happened to have nothing outstanding was closed without anyone being paid.
     */
    VisitStatus onBillPaid(Long visitId);

    /**
     * Called when a doctor refers the patient out.
     *
     * <p>A referral is a decision that the local journey is over, so the visit moves toward payment as
     * though nothing were outstanding — the specification is explicit that it is treated the same way
     * as "nothing further is pending" rather than waiting on work that will never resolve.
     */
    VisitStatus onReferralCreated(Long visitId);
}
