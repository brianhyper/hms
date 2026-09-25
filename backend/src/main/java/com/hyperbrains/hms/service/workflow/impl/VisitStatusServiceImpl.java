package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.rules.PrescriptionLifecycle;
import com.hyperbrains.hms.service.rules.VisitStatusDeriver;
import com.hyperbrains.hms.service.workflow.BillingService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitStatusServiceImpl implements VisitStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitStatusServiceImpl.class);

    /**
     * The only statuses this service is allowed to change.
     *
     * <p>Everything before {@code WAITING_RESULTS} is an operational state: a nurse is doing vitals,
     * a doctor is in consultation. Those are set by the people doing the work and must not be
     * derived, or a nurse saving vitals would have the visit jump to payment because nothing is
     * outstanding yet. Beyond {@code WAITING_PAYMENT} the visit is finished and must stay finished.
     */
    private static final Set<VisitStatus> DERIVED_STATUSES = Set.of(VisitStatus.WAITING_RESULTS, VisitStatus.WAITING_PAYMENT);

    private final VisitRepository visitRepository;

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final ReferralRepository referralRepository;

    private final BillingService billingService;

    private final AuditLogService auditLogService;

    public VisitStatusServiceImpl(
        VisitRepository visitRepository,
        DiagnosticOrderRepository diagnosticOrderRepository,
        PrescriptionRepository prescriptionRepository,
        ReferralRepository referralRepository,
        BillingService billingService,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.referralRepository = referralRepository;
        this.billingService = billingService;
        this.auditLogService = auditLogService;
    }

    @Override
    public VisitStatus afterConsultation(Long visitId) {
        return applyDerivation(loadVisit(visitId));
    }

    @Override
    public VisitStatus recompute(Long visitId) {
        Visit visit = loadVisit(visitId);

        if (referralRepository.existsByVisitId(visitId)) {
            // The local journey ended when the referral was written. Work that resolves afterwards — a
            // late lab result, a cancelled order — is still charged for, but it must not drag the
            // encounter backwards out of the payment queue and strand it there.
            if (visit.getStatus() == VisitStatus.WAITING_PAYMENT) {
                refreshPaymentStage(visit);
            }
            return visit.getStatus();
        }

        if (!DERIVED_STATUSES.contains(visit.getStatus())) {
            return visit.getStatus();
        }
        return applyDerivation(visit);
    }

    @Override
    public VisitStatus onReferralCreated(Long visitId) {
        Visit visit = loadVisit(visitId);
        if (!VisitStatusDeriver.participatesInOutpatientPath(visit.getType())) {
            // An admitted patient's stay is not ended by a referral, and their billing accumulates over
            // it under rules that are not part of this phase.
            return visit.getStatus();
        }

        // PendingWork.none() is the whole point: a referral says the local journey is over, so nothing
        // still outstanding is allowed to hold the visit back.
        return moveTo(
            visit,
            VisitStatusDeriver.afterConsultation(visit.getType(), VisitStatusDeriver.PendingWork.none()),
            "referred out"
        );
    }

    @Override
    public VisitStatus onPrescriptionPlaced(Long visitId) {
        Visit visit = loadVisit(visitId);
        if (!VisitStatusDeriver.participatesInOutpatientPath(visit.getType())) {
            // An admitted patient's charges accumulate over the stay under rules that are not part
            // of this phase, so nothing here moves them toward a payment desk.
            return visit.getStatus();
        }

        if (visit.getStatus() == VisitStatus.WAITING_PAYMENT) {
            // Already collecting. The visit does not move, but the new charge and the new
            // prescription both have to join what is being collected right now.
            refreshPaymentStage(visit);
            return visit.getStatus();
        }

        if (visit.getType() == VisitType.PHARMACY_ONLY) {
            // No triage and no consultation to finish, so no derivation would ever fire: the charge
            // itself is the only thing that can make a walk-in payable.
            return moveTo(visit, VisitStatus.WAITING_PAYMENT, "pharmacy-only visit charged");
        }

        return recompute(visitId);
    }

    @Override
    public VisitStatus onPrescriptionWithdrawn(Long visitId) {
        Visit visit = loadVisit(visitId);
        if (visit.getStatus() == VisitStatus.WAITING_PAYMENT) {
            // The status does not change, but the bill does: the charge for the withdrawn medicine has
            // to come out of what the patient is being asked to pay.
            refreshPaymentStage(visit);
            return visit.getStatus();
        }
        return recompute(visitId);
    }

    @Override
    public VisitStatus onBillPaid(Long visitId) {
        Visit visit = loadVisit(visitId);
        if (visit.getStatus() != VisitStatus.WAITING_PAYMENT) {
            // Closing anything other than a visit that was actually at the desk would end an encounter
            // that still has clinical work or an unsettled bill against it. An admitted visit is the
            // case that matters most: its bill is collected against mid-stay, so settlement must not
            // close it — discharge does.
            throw BusinessRuleViolationException.of(
                "visitNotAwaitingPayment",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + " and is not waiting to be paid, so paying cannot close it"
            );
        }
        return moveTo(visit, VisitStatus.CLOSED, "bill settled in full");
    }

    @Override
    public VisitStatus onDischarged(Long visitId) {
        Visit visit = loadVisit(visitId);
        if (visit.getStatus() != VisitStatus.ADMITTED) {
            throw BusinessRuleViolationException.of(
                "visitNotAdmitted",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + ", so it is not an admission waiting to be discharged"
            );
        }
        return moveTo(visit, VisitStatus.CLOSED, "patient discharged");
    }

    private VisitStatus applyDerivation(Visit visit) {
        VisitStatusDeriver.PendingWork pending = currentPendingWork(visit);
        return moveTo(
            visit,
            VisitStatusDeriver.afterConsultation(visit.getType(), pending),
            pending.openOrders() + " outstanding order(s)"
        );
    }

    /**
     * What is still outstanding for this visit.
     *
     * <p>Only diagnostic orders. Prescriptions are deliberately <em>not</em> counted, and that is not
     * an omission — it is forced by the payment-before-dispense guarantee. A prescription's stock is
     * reserved and its charge raised at the moment it is written, so there is nothing left to resolve
     * for the visit's purposes; the only further transitions are payment and the hand-over, and both
     * happen <em>after</em> the visit reaches the payment stage. Counting a prescription as outstanding
     * would therefore deadlock: the visit could not reach payment until the prescription resolved, and
     * the prescription could not resolve until the visit was paid.
     */
    private VisitStatusDeriver.PendingWork currentPendingWork(Visit visit) {
        return new VisitStatusDeriver.PendingWork(
            Math.toIntExact(diagnosticOrderRepository.countOutstandingByVisitId(visit.getId())),
            0
        );
    }

    private VisitStatus moveTo(Visit visit, VisitStatus next, String reason) {
        if (next == visit.getStatus()) {
            return next;
        }

        VisitStatus previous = visit.getStatus();
        visit.setStatus(next);
        if (next == VisitStatus.CLOSED) {
            // This is the single place a visit closes, so it is the single place that has to stamp the
            // time. Nothing re-opens a closed visit, so it never has to be cleared afterwards.
            visit.setClosedAt(Instant.now());
        }
        visit = visitRepository.save(visit);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.VISIT_STATUS_CHANGED, "Visit", visit.getId())
                .withChange(previous.name(), next.name())
                .withDetails(reason)
        );

        if (next == VisitStatus.WAITING_PAYMENT) {
            refreshPaymentStage(visit);
        }

        LOG.debug("Visit {} moved {} -> {}", visit.getId(), previous, next);
        return next;
    }

    /**
     * Everything that becomes true the moment a visit is at the payment stage.
     *
     * <p>Kept together because both halves are about the same thing — the bill is now the figure
     * Finance collects against, and everything prescribed is now part of it. A visit can arrive here
     * more than once (work added, resolved, and added again), so both are idempotent.
     */
    private void refreshPaymentStage(Visit visit) {
        promotePrescriptionsToPayment(visit);
        if (visit.getBill() != null) {
            billingService.recalculateTotal(visit.getBill());
        }
    }

    private void promotePrescriptionsToPayment(Visit visit) {
        List<Prescription> awaiting = prescriptionRepository.findByVisitIdAndStatusIn(
            visit.getId(),
            PrescriptionLifecycle.AWAITING_PAYMENT
        );
        for (Prescription prescription : awaiting) {
            PrescriptionStatus next = PrescriptionLifecycle.promote(prescription.getStatus());
            if (next != prescription.getStatus()) {
                prescription.setStatus(next);
                prescriptionRepository.save(prescription);
            }
        }
    }

    private Visit loadVisit(Long visitId) {
        return visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
    }
}
