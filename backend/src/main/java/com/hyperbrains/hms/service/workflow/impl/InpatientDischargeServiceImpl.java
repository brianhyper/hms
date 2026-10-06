package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentPlanStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.repository.PaymentPlanRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.DischargeSignOffRequestDTO;
import com.hyperbrains.hms.service.dto.view.DischargeViewDTO;
import com.hyperbrains.hms.service.rules.AdmissionLifecycle;
import com.hyperbrains.hms.service.rules.BedLifecycle;
import com.hyperbrains.hms.service.rules.DischargeRequirements;
import com.hyperbrains.hms.service.workflow.InpatientDischargeService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ending a stay, in two signatures.
 *
 * <p>The order of what happens here is the part worth reading. Both halves are checked the same way — the stay is
 * open, this half has not been signed, the signer is not the person who signed the other half, the money allows it,
 * and any order still running has been seen — and the stay is only ended by the half that completes the pair. That
 * last rule is what stops the two actions being one action with two names: whoever signs first leaves a stay a nurse
 * can still refuse to release, which is exactly what a second signature is for.
 *
 * <p>The bed goes to {@code CLEANING} rather than back to {@code AVAILABLE}, and the visit is closed through
 * {@link VisitStatusService#onDischarged(Long)} rather than through the payment path, because a settled bill ends an
 * outpatient encounter and only a discharge ends a stay.
 */
@Service
@Transactional
public class InpatientDischargeServiceImpl implements InpatientDischargeService {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientDischargeServiceImpl.class);

    private final AdmissionRepository admissionRepository;

    private final BedRepository bedRepository;

    private final DoctorOrderRepository doctorOrderRepository;

    private final PaymentPlanRepository paymentPlanRepository;

    private final UserRepository userRepository;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    public InpatientDischargeServiceImpl(
        AdmissionRepository admissionRepository,
        BedRepository bedRepository,
        DoctorOrderRepository doctorOrderRepository,
        PaymentPlanRepository paymentPlanRepository,
        UserRepository userRepository,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService
    ) {
        this.admissionRepository = admissionRepository;
        this.bedRepository = bedRepository;
        this.doctorOrderRepository = doctorOrderRepository;
        this.paymentPlanRepository = paymentPlanRepository;
        this.userRepository = userRepository;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
    }

    @Override
    public DischargeViewDTO doctorSignsOff(Long admissionId, DischargeSignOffRequestDTO request) {
        Admission admission = requireAnOpenStay(admissionId);
        User signer = currentUser();

        if (admission.getDischargedByDoctor() != null) {
            throw BusinessRuleViolationException.of(
                "dischargeAlreadySignedByADoctor",
                "admission",
                "Admission " + admissionId + " is already signed off by " + admission.getDischargedByDoctor().getLogin()
            );
        }
        refuseOnePersonSigningBothHalves(admission, admission.getDischargedByNurse(), signer);

        List<DoctorOrder> outstanding = requireTheMoneyAndTheRunningOrders(admission, request);
        admission.setDischargedByDoctor(signer);
        acceptTheNote(admission, request);
        recordTheSignature(admission, signer, "doctor", "dischargedByDoctor", outstanding);
        LOG.info("Admission {} signed off for discharge by the doctor {}", admissionId, signer.getLogin());

        return finishOrWait(admission, outstanding);
    }

    @Override
    public DischargeViewDTO nurseSignsOff(Long admissionId, DischargeSignOffRequestDTO request) {
        Admission admission = requireAnOpenStay(admissionId);
        User signer = currentUser();

        if (admission.getDischargedByNurse() != null) {
            throw BusinessRuleViolationException.of(
                "dischargeAlreadySignedByANurse",
                "admission",
                "Admission " + admissionId + " is already signed off by " + admission.getDischargedByNurse().getLogin()
            );
        }
        refuseOnePersonSigningBothHalves(admission, admission.getDischargedByDoctor(), signer);

        List<DoctorOrder> outstanding = requireTheMoneyAndTheRunningOrders(admission, request);
        admission.setDischargedByNurse(signer);
        acceptTheNote(admission, request);
        recordTheSignature(admission, signer, "nurse", "dischargedByNurse", outstanding);
        LOG.info("Admission {} signed off for discharge by the nurse {}", admissionId, signer.getLogin());

        return finishOrWait(admission, outstanding);
    }

    private Admission requireAnOpenStay(Long admissionId) {
        Admission admission = admissionRepository
            .findOneWithBedAndWard(admissionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("admissionNotFound", "admission", "No admission with id " + admissionId)
            );
        if (!AdmissionLifecycle.isOpen(admission.getStatus())) {
            throw BusinessRuleViolationException.of(
                "stayAlreadyEnded",
                "admission",
                "Admission " + admissionId + " is " + admission.getStatus() + ", so there is nothing left to discharge"
            );
        }
        return admission;
    }

    /**
     * The two sign-offs have to be two people, which §7 calls a decision to record rather than an accident to allow.
     * It is refused rather than warned about: a rule that lets the same person through "just this once" is a rule
     * nobody can rely on afterwards.
     */
    private void refuseOnePersonSigningBothHalves(Admission admission, User theOtherSignature, User signer) {
        if (theOtherSignature == null || !DischargeRequirements.isOnePersonSigningTwice(signer.getId(), theOtherSignature.getId())) {
            return;
        }
        throw BusinessRuleViolationException.of(
            "dischargeCannotBeSignedByOnePerson",
            "admission",
            "Admission " +
            admission.getId() +
            " was already signed off by " +
            signer.getLogin() +
            ", and a discharge is two people's signatures by definition"
        );
    }

    /**
     * The money, and the orders that are still running.
     *
     * <p>Both are checked on <em>each</em> half rather than only on the one that completes the pair, because each
     * signer is a person agreeing that this patient may leave. The orders are surfaced with their ids: "3 orders are
     * still running" is not something a signer can act on, and naming them is what makes resolving them possible.
     */
    private List<DoctorOrder> requireTheMoneyAndTheRunningOrders(Admission admission, DischargeSignOffRequestDTO request) {
        requireTheMoneyAllowsIt(admission);

        List<DoctorOrder> outstanding = doctorOrderRepository.findByAdmissionIdAndStatus(admission.getId(), DoctorOrderStatus.ACTIVE);
        if (!outstanding.isEmpty() && !request.acknowledgeOutstandingOrders()) {
            throw BusinessRuleViolationException.of(
                "ordersStillRunningAtDischarge",
                "admission",
                (
                    "Admission " +
                    admission.getId() +
                    " still has " +
                    outstanding.size() +
                    " order(s) running (" +
                    outstanding.stream().map(order -> String.valueOf(order.getId())).collect(Collectors.joining(", ")) +
                    "). Resolve or cancel them, or acknowledge that they are running and discharge anyway."
                )
            );
        }
        return outstanding;
    }

    private void requireTheMoneyAllowsIt(Admission admission) {
        Visit visit = admission.getVisit();
        Bill bill = visit == null ? null : visit.getBill();
        boolean coveredByAnAgreedPlan =
            bill != null && paymentPlanRepository.existsByBillIdAndStatus(bill.getId(), PaymentPlanStatus.ACTIVE);

        if (DischargeRequirements.moneyIsSettled(bill == null ? null : bill.getStatus(), coveredByAnAgreedPlan)) {
            return;
        }
        throw BusinessRuleViolationException.of(
            "billUnsettledAtDischarge",
            "admission",
            (
                "Admission " +
                admission.getId() +
                " still owes " +
                bill.getTotalAmount() +
                " on bill " +
                bill.getId() +
                "; settle it or agree a payment plan before discharging"
            )
        );
    }

    /** A sign-off with nothing to say leaves the note as it stands, so the first signer's text is not erased. */
    private void acceptTheNote(Admission admission, DischargeSignOffRequestDTO request) {
        if (request.note() != null && !request.note().isBlank()) {
            admission.setDischargeNote(request.note());
        }
    }

    private void recordTheSignature(
        Admission admission,
        User signer,
        String half,
        String fieldName,
        List<DoctorOrder> outstanding
    ) {
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.DISCHARGE_SIGNED_OFF, "Admission", admission.getId())
                .withField(fieldName)
                .withChange(null, signer.getLogin())
                .withDetails("the " + half + "'s half of the discharge")
        );

        if (!outstanding.isEmpty()) {
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.DISCHARGE_OUTSTANDING_ORDERS_ACKNOWLEDGED, "Admission", admission.getId())
                    .withReason("acknowledged by " + signer.getLogin())
                    .withDetails(
                        "discharged with " +
                        outstanding.size() +
                        " order(s) still running: " +
                        outstanding.stream().map(order -> String.valueOf(order.getId())).collect(Collectors.joining(", "))
                    )
            );
        }
    }

    private DischargeViewDTO finishOrWait(Admission admission, List<DoctorOrder> outstanding) {
        Long doctorId = admission.getDischargedByDoctor() == null ? null : admission.getDischargedByDoctor().getId();
        Long nurseId = admission.getDischargedByNurse() == null ? null : admission.getDischargedByNurse().getId();

        if (!DischargeRequirements.isSignedOff(doctorId, nurseId)) {
            // One signature is recorded and the stay carries on. A patient is not discharged by one person.
            admissionRepository.save(admission);
            return DischargeViewDTO.from(admission, orderIds(outstanding));
        }
        return complete(admission, outstanding);
    }

    private DischargeViewDTO complete(Admission admission, List<DoctorOrder> outstanding) {
        AdmissionStatus previousStatus = admission.getStatus();
        admission.setDischargedAt(Instant.now());
        admission.setStatus(AdmissionStatus.DISCHARGED);
        admissionRepository.save(admission);

        Bed bed = admission.getBed();
        if (bed != null) {
            BedStatus previousBedStatus = bed.getStatus();
            bed.setStatus(BedLifecycle.statusAfterVacating());
            bedRepository.save(bed);
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.BED_STATUS_CHANGED, "Bed", bed.getId())
                    .withField("status")
                    .withChange(previousBedStatus.name(), bed.getStatus().name())
                    .withDetails("bed " + bed.getBedNumber() + " vacated by the discharge of admission " + admission.getId())
            );
        }

        Visit visit = admission.getVisit();
        if (visit != null) {
            // No audit entry here: onDischarged records the visit's own status change, and a second entry saying
            // the same thing would be two rows for one event.
            visitStatusService.onDischarged(visit.getId());
        }

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ADMISSION_STATUS_CHANGED, "Admission", admission.getId())
                .withField("status")
                .withChange(previousStatus.name(), AdmissionStatus.DISCHARGED.name())
                .withDetails("both signatures are in, so the stay is ended")
        );
        LOG.info("Admission {} discharged; bed {} is now {}", admission.getId(), bed == null ? null : bed.getId(), BedStatus.CLEANING);

        return DischargeViewDTO.from(admission, orderIds(outstanding));
    }

    private static List<Long> orderIds(List<DoctorOrder> orders) {
        return orders.stream().map(DoctorOrder::getId).toList();
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "admission", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "admission", "No user account for " + login));
    }
}
