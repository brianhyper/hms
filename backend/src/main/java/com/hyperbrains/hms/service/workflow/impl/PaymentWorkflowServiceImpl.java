package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Payment;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.PaymentRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.BillViewDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;
import com.hyperbrains.hms.service.rules.BillSettlement;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.PaymentWorkflowService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentWorkflowServiceImpl implements PaymentWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentWorkflowServiceImpl.class);

    private final VisitRepository visitRepository;

    private final BillRepository billRepository;

    private final BillLineItemRepository billLineItemRepository;

    private final PaymentRepository paymentRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final UserRepository userRepository;

    private final PrescriptionWorkflowService prescriptionService;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    public PaymentWorkflowServiceImpl(
        VisitRepository visitRepository,
        BillRepository billRepository,
        BillLineItemRepository billLineItemRepository,
        PaymentRepository paymentRepository,
        PrescriptionRepository prescriptionRepository,
        UserRepository userRepository,
        PrescriptionWorkflowService prescriptionService,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.billRepository = billRepository;
        this.billLineItemRepository = billLineItemRepository;
        this.paymentRepository = paymentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.prescriptionService = prescriptionService;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public BillViewDTO billForVisit(Long visitId) {
        Visit visit = loadVisit(visitId);
        Bill bill = visit.getBill();
        if (bill == null) {
            // Nothing has been charged, so there is nothing to collect. Not an error.
            return BillViewDTO.empty(visitId);
        }
        return view(bill, visitId);
    }

    @Override
    public BillViewDTO recordPayment(Long visitId, RecordPaymentRequestDTO request) {
        Visit visit = loadVisit(visitId);
        Bill bill = visit.getBill();

        // Checked before the visit's status so that a second attempt on a settled bill is reported as
        // exactly that. After settlement the visit is CLOSED, and blaming the status would send the desk
        // looking for a problem with the visit rather than telling them the money is already in.
        if (bill != null && bill.getStatus() == BillStatus.PAID) {
            throw BusinessRuleViolationException.of("billAlreadyPaid", "bill", "Bill " + bill.getId() + " has already been settled");
        }

        if (!VisitLifecycle.acceptsPayment(visit.getStatus())) {
            // The bill is only finalised once the visit reaches the payment stage: before that the total
            // is still moving as results come back and orders resolve, so collecting against it would
            // collect against a figure that is about to change. An admitted visit is the exception —
            // its bill runs for the length of the stay and is collected against while the patient is
            // still in the building.
            throw BusinessRuleViolationException.of(
                "visitNotAwaitingPayment",
                "visit",
                "Visit " +
                visitId +
                " is " +
                visit.getStatus() +
                ": its bill is not finalised until the visit is waiting to be paid or the patient is admitted"
            );
        }

        if (bill == null) {
            throw BusinessRuleViolationException.of(
                "visitHasNoBill",
                "bill",
                "Visit " + visitId + " is at the payment stage but has no bill, so there is nothing to collect"
            );
        }

        PaymentConfirmationStatus confirmation = confirmationStatusOrDefault(request);
        requireMethodDetail(request);

        if (confirmation == PaymentConfirmationStatus.REJECTED) {
            // A rejected payment is not money the hospital is holding, so recording it as though it were
            // would overstate what has been collected against this bill.
            throw BusinessRuleViolationException.of(
                "paymentRejected",
                "payment",
                "A rejected payment cannot be recorded against the bill"
            );
        }

        Payment existing = bill.getPayment();
        BigDecimal alreadyRecorded = existing == null ? BigDecimal.ZERO : existing.getAmount();
        BigDecimal outstanding = BillSettlement.outstanding(bill.getTotalAmount(), alreadyRecorded);

        if (outstanding.signum() == 0 && request.getAmount().signum() == 0) {
            // Nothing is owed, but the visit still has to be settled: closure and the release of medicine
            // both hang off settlement, so a patient with nothing to pay must not be stuck at the desk.
            // No payment row is written, because recording money that was never collected would be a lie
            // in the ledger.
            bill.setStatus(BillStatus.PAID);
            bill.setPaidAt(Instant.now());
            bill = billRepository.save(bill);

            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.BILL_PAID, "Bill", bill.getId()).withDetails(
                    "Settled with nothing owed"
                )
            );
            settleEncounter(visit, bill);
            return view(bill, visitId);
        }

        if (request.getAmount().signum() <= 0) {
            throw BusinessRuleViolationException.of(
                "paymentAmountRequired",
                "bill",
                "%s is still outstanding on this bill, so a payment amount is required".formatted(outstanding)
            );
        }
        if (isBlank(request.getReceiptNumber())) {
            throw BusinessRuleViolationException.of(
                "receiptNumberRequired",
                "payment",
                "Money being collected must be recorded against a receipt number"
            );
        }

        BigDecimal overshoot = BillSettlement.overshoot(bill.getTotalAmount(), alreadyRecorded, request.getAmount());
        if (overshoot.signum() > 0) {
            throw BusinessRuleViolationException.of(
                "paymentExceedsBalance",
                "bill",
                "This payment overshoots the balance by %s; the outstanding amount is %s".formatted(overshoot, outstanding)
            );
        }

        // Receipt numbers are unique, so a retry has to be recognised here rather than allowed to reach
        // the database and return a constraint violation the desk cannot act on.
        Payment byReceipt = paymentRepository.findOneByReceiptNumber(request.getReceiptNumber()).orElse(null);
        if (byReceipt != null && (existing == null || !byReceipt.getId().equals(existing.getId()))) {
            throw BusinessRuleViolationException.of(
                "receiptAlreadyUsed",
                "payment",
                "Receipt " + request.getReceiptNumber() + " has already been recorded against another bill"
            );
        }

        Payment payment = existing == null ? new Payment() : existing;
        // Accumulated, not replaced: this call is one payment event, and the bill's record of what has
        // been collected is the sum of them. The receipt on the row is the latest; earlier ones remain
        // in the audit trail.
        payment.setAmount(alreadyRecorded.add(request.getAmount()));
        payment.setMethod(request.getMethod());
        payment.setReceiptNumber(request.getReceiptNumber());
        payment.setMpesaReference(request.getMpesaReference());
        payment.setInsurerName(request.getInsurerName());
        payment.setConfirmationStatus(confirmation);
        payment.setRecordedAt(Instant.now());
        payment.setRecordedBy(currentUser());
        payment = paymentRepository.save(payment);

        bill.setPayment(payment);

        boolean settles = confirmation == PaymentConfirmationStatus.CONFIRMED &&
            BillSettlement.settles(bill.getTotalAmount(), alreadyRecorded, request.getAmount());
        if (settles) {
            bill.setStatus(BillStatus.PAID);
            bill.setPaidAt(Instant.now());
        }
        bill = billRepository.save(bill);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PAYMENT_RECORDED, "Payment", payment.getId()).withDetails(
                "%s by %s against bill %d; recorded %s of %s outstanding%s".formatted(
                    request.getAmount(),
                    request.getMethod(),
                    bill.getId(),
                    payment.getAmount(),
                    bill.getTotalAmount(),
                    settles ? "; bill settled" : " (confirmation " + confirmation + ")"
                )
            )
        );

        if (settles) {
            settleEncounter(visit, bill);
        }

        LOG.debug("Recorded payment {} on bill {}; settled={}", payment.getId(), bill.getId(), settles);
        return view(bill, visitId);
    }

    /**
     * Everything that becomes true once the bill is settled.
     *
     * <p>The order matters: medicine is released to the pharmacy first, then the encounter is closed if
     * settling is what ends it. Closing first would mean a failure in between left a closed visit whose
     * prescription could never be dispensed, since the pharmacy queue only accepts released
     * prescriptions.
     *
     * <p><strong>Being settled is not the same as being over.</strong> For an outpatient the two
     * coincide — the desk takes the money and the encounter is done. An admitted patient's bill is
     * collected against for the length of the stay, so a payment (a deposit, an instalment, or the
     * balance at the end) must leave the visit where it is; the stay ends at discharge, which is a
     * clinical decision. Closing here would end a running admission and strand the bed and the ward's
     * outstanding work.
     */
    private void settleEncounter(Visit visit, Bill bill) {
        List<Prescription> prescriptions = prescriptionRepository.findByVisitId(visit.getId());
        for (Prescription prescription : prescriptions) {
            // Idempotent per prescription, and a no-op for any that were withdrawn.
            prescriptionService.markPaid(prescription.getId());
        }

        if (VisitLifecycle.closesOnBillSettlement(visit.getType())) {
            visitStatusService.onBillPaid(visit.getId());
        } else {
            LOG.debug(
                "Bill {} settled; visit {} stays {} because settling is not what ends an admission",
                bill.getId(),
                visit.getId(),
                visit.getStatus()
            );
        }

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BILL_PAID, "Bill", bill.getId()).withDetails(
                "%d prescription(s) released to the pharmacy dispensing queue".formatted(prescriptions.size())
            )
        );
    }

    private static PaymentConfirmationStatus confirmationStatusOrDefault(RecordPaymentRequestDTO request) {
        // A cashier who has taken money has confirmed it; only an insurance claim needs to be told apart.
        return request.getConfirmationStatus() == null ? PaymentConfirmationStatus.CONFIRMED : request.getConfirmationStatus();
    }

    /**
     * The reference each method needs to be reconcilable later.
     *
     * <p>Money that cannot be traced back to a statement or a policy is money that will be queried
     * months later with nothing to answer from.
     */
    private static void requireMethodDetail(RecordPaymentRequestDTO request) {
        if (request.getMethod() == PaymentMethod.MPESA && isBlank(request.getMpesaReference())) {
            throw BusinessRuleViolationException.of(
                "mpesaReferenceRequired",
                "payment",
                "An M-Pesa payment must quote the transaction reference it can be reconciled against"
            );
        }
        if (request.getMethod() == PaymentMethod.INSURANCE && isBlank(request.getInsurerName())) {
            throw BusinessRuleViolationException.of(
                "insurerNameRequired",
                "payment",
                "An insurance payment must name the insurer it is claimed from"
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BillViewDTO view(Bill bill, Long visitId) {
        List<BillLineItem> lines = billLineItemRepository.findByBillIdOrderByIdAsc(bill.getId());
        return BillViewDTO.from(bill, visitId, lines, bill.getPayment());
    }

    private Visit loadVisit(Long visitId) {
        return visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> BusinessRuleViolationException.of("authenticationRequired", "payment", "No authenticated user in scope"));
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "payment", "No user account for " + login));
    }
}
