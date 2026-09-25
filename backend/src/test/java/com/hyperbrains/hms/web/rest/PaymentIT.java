package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.BillViewDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.PaymentWorkflowService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for collecting money: finalisation, settlement, and the medicine it releases.
 *
 * <p>These are the tests that hold the end of the outpatient encounter together. A payment is not just
 * a row — it is the event that closes the visit and lets the pharmacy hand medicine over, and getting
 * it wrong in either direction is either a patient stuck at a desk or medicine given away unpaid.
 *
 * <p>The consultation fee is read from the catalogue rather than hard-coded, because it is seeded at
 * zero until someone sets the real figure.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_FINANCE")
class PaymentIT {

    private static final BigDecimal DRUG_PRICE = new BigDecimal("2.50");

    @Autowired
    private PaymentWorkflowService paymentService;

    @Autowired
    private PrescriptionWorkflowService prescriptionService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private ConsultationWorkflowService consultationService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionLineRepository prescriptionLineRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private Drug drug;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Payment Test Patient");
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        drug = new Drug();
        drug.setName("Paracetamol");
        drug.setUnit("tablet");
        drug.setPrice(DRUG_PRICE);
        drug.setCurrentStock(100);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(10);
        drug.setActive(true);
        drug = drugRepository.save(drug);
    }

    @AfterEach
    void cleanup() {
        List<Visit> visits = patient == null || patient.getId() == null
            ? List.of()
            : visitRepository.findByPatientId(patient.getId());

        List<Prescription> prescriptions = visits
            .stream()
            .flatMap(visit -> prescriptionRepository.findByVisitId(visit.getId()).stream())
            .toList();
        List<Long> consultationIds = visits
            .stream()
            .map(Visit::getConsultation)
            .filter(Objects::nonNull)
            .map(Consultation::getId)
            .filter(Objects::nonNull)
            .toList();
        List<Long> billIds = visits.stream().map(Visit::getBill).filter(Objects::nonNull).map(Bill::getId).filter(Objects::nonNull).toList();
        List<Long> vitalSignsIds = visits
            .stream()
            .map(visit -> visit.getVitals())
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // Children before parents: a prescription points at its visit, a bill line at its bill, and a
        // visit points at both the consultation and the bill.
        prescriptions.forEach(prescription -> {
            prescriptionLineRepository.deleteAll(prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId()));
            prescriptionRepository.deleteById(prescription.getId());
        });
        visits.forEach(visit -> visitRepository.deleteById(visit.getId()));
        consultationIds.forEach(id -> consultationRepository.findById(id).ifPresent(consultationRepository::delete));
        billIds.forEach(id -> {
            billLineItemRepository.findByBillIdOrderByIdAsc(id).forEach(billLineItemRepository::delete);
            billRepository.findById(id).ifPresent(billRepository::delete);
        });
        vitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }
        if (drug != null && drug.getId() != null) {
            drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete);
        }
    }

    @Test
    void aBillCannotBeCollectedBeforeTheVisitReachesThePaymentStage() {
        Visit visit = visitAwaitingDoctor();
        assertThat(visit.getStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);

        // Nothing has been charged, so there is no bill to read a total from; the amount is irrelevant
        // because the visit's status is what refuses this.
        assertThatThrownBy(() -> paymentService.recordPayment(visit.getId(), payment(new BigDecimal("10.00"), "RCPT-1", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("not finalised");
    }

    @Test
    void theBillShowsEveryChargeAndWhatIsOutstanding() {
        Visit visit = visitAwaitingPayment(4);

        BillViewDTO bill = paymentService.billForVisit(visit.getId());

        assertThat(bill.getStatus()).isEqualTo(BillStatus.UNPAID);
        assertThat(bill.getRecordedAmount()).isEqualByComparingTo("0");
        assertThat(bill.getOutstanding()).isEqualByComparingTo(bill.getTotalAmount());
        // The consultation fee plus the medicine, each raised by the thing that produced it.
        assertThat(bill.getLines())
            .extracting(BillViewDTO.BillLineItemViewDTO::description)
            .contains("Paracetamol");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo(totalOf(visit));
    }

    @Test
    void aVisitWithNoChargesOwesNothingRatherThanFailing() {
        Visit visit = visitAwaitingDoctor();

        BillViewDTO bill = paymentService.billForVisit(visit.getId());

        assertThat(bill.getTotalAmount()).isEqualByComparingTo("0");
        assertThat(bill.getOutstanding()).isEqualByComparingTo("0");
        assertThat(bill.getLines()).isEmpty();
        // No bill has been created yet, because a bill is only created by the first charge.
        assertThat(bill.getBillId()).isNull();
    }

    @Test
    void payingInFullSettlesTheBillClosesTheVisitAndReleasesTheMedicine() {
        Visit visit = visitAwaitingPayment(4);
        Long prescriptionId = prescriptionIdOf(visit);
        BigDecimal total = totalOf(visit);

        BillViewDTO bill = paymentService.recordPayment(visit.getId(), payment(total, "RCPT-1001", null));

        assertThat(bill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(bill.getPaidAt()).isNotNull();
        assertThat(bill.getOutstanding()).isEqualByComparingTo("0");
        assertThat(bill.getPayment()).isNotNull();
        assertThat(bill.getPayment().recordedByLogin()).isEqualTo("admin");

        // The encounter is over.
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.CLOSED);
        // And the medicine is now the pharmacy's to hand over, without it having to check the bill.
        assertThat(prescriptionRepository.findById(prescriptionId).orElseThrow().getStatus()).isEqualTo(
            PrescriptionStatus.READY_FOR_DISPENSE
        );
    }

    @Test
    void anUnderpaymentIsRecordedButLeavesTheBillUnsettled() {
        Visit visit = visitAwaitingPayment(4);
        Long prescriptionId = prescriptionIdOf(visit);
        BigDecimal half = totalOf(visit).divide(new BigDecimal("2"));

        BillViewDTO bill = paymentService.recordPayment(visit.getId(), payment(half, "RCPT-1002", null));

        // Money the hospital is holding must always be visible.
        assertThat(bill.getRecordedAmount()).isEqualByComparingTo(half);
        assertThat(bill.getOutstanding()).isEqualByComparingTo(totalOf(visit).subtract(half));
        assertThat(bill.getStatus()).isEqualTo(BillStatus.UNPAID);

        // Nothing is released against an unpaid bill, and the patient stays at the desk.
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(prescriptionRepository.findById(prescriptionId).orElseThrow().getStatus()).isEqualTo(
            PrescriptionStatus.PENDING_PAYMENT
        );
    }

    @Test
    void aDepositFollowedByTheBalanceSettlesTheBill() {
        Visit visit = visitAwaitingPayment(4);
        BigDecimal total = totalOf(visit);
        BigDecimal deposit = total.divide(new BigDecimal("4"));

        paymentService.recordPayment(visit.getId(), payment(deposit, "RCPT-1003", null));
        BillViewDTO settled = paymentService.recordPayment(visit.getId(), payment(total.subtract(deposit), "RCPT-1004", null));

        assertThat(settled.getStatus()).isEqualTo(BillStatus.PAID);
        // Accumulated, not replaced: the bill records everything that was collected against it.
        assertThat(settled.getPayment().amount()).isEqualByComparingTo(total);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.CLOSED);
    }

    @Test
    void anOverpaymentIsRefused() {
        Visit visit = visitAwaitingPayment(4);
        BigDecimal tooMuch = totalOf(visit).add(new BigDecimal("100.00"));

        assertThatThrownBy(() -> paymentService.recordPayment(visit.getId(), payment(tooMuch, "RCPT-1005", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("overshoots");

        // Refused outright: nothing was recorded and the visit did not move.
        assertThat(billOf(visit.getId()).getStatus()).isEqualTo(BillStatus.UNPAID);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
    }

    /**
     * An insurance claim is money promised, not money held. Releasing medicine against a claim that
     * may be rejected is the expensive mistake this guards.
     */
    @Test
    void aPendingInsuranceClaimIsRecordedButReleasesNothing() {
        Visit visit = visitAwaitingPayment(4);
        Long prescriptionId = prescriptionIdOf(visit);
        RecordPaymentRequestDTO request = payment(totalOf(visit), "CLAIM-77", PaymentConfirmationStatus.PENDING);
        request.setMethod(PaymentMethod.INSURANCE);
        request.setInsurerName("Jubilee Health");

        BillViewDTO bill = paymentService.recordPayment(visit.getId(), request);

        assertThat(bill.getPayment().confirmationStatus()).isEqualTo(PaymentConfirmationStatus.PENDING);
        assertThat(bill.getStatus()).isEqualTo(BillStatus.UNPAID);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(prescriptionRepository.findById(prescriptionId).orElseThrow().getStatus()).isEqualTo(
            PrescriptionStatus.PENDING_PAYMENT
        );
    }

    @Test
    void aRejectedPaymentIsRefused() {
        Visit visit = visitAwaitingPayment(4);

        assertThatThrownBy(() ->
            paymentService.recordPayment(visit.getId(), payment(totalOf(visit), "RCPT-1006", PaymentConfirmationStatus.REJECTED))
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("rejected payment");

        assertThat(billOf(visit.getId()).getPayment()).isNull();
    }

    @Test
    void anMpesaPaymentMustQuoteTheReferenceItCanBeReconciledAgainst() {
        Visit visit = visitAwaitingPayment(4);
        RecordPaymentRequestDTO request = payment(totalOf(visit), "RCPT-1007", null);
        request.setMethod(PaymentMethod.MPESA);

        assertThatThrownBy(() -> paymentService.recordPayment(visit.getId(), request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("M-Pesa payment must quote");

        request.setMpesaReference("SJK7H2LQ9P");
        assertThat(paymentService.recordPayment(visit.getId(), request).getStatus()).isEqualTo(BillStatus.PAID);
    }

    @Test
    void aReceiptAlreadyUsedOnAnotherBillIsRefused() {
        Visit settledVisit = visitAwaitingPayment(2);
        paymentService.recordPayment(settledVisit.getId(), payment(totalOf(settledVisit), "RCPT-DUPLICATE", null));

        Visit otherVisit = visitAwaitingPayment(2);

        assertThatThrownBy(() -> paymentService.recordPayment(otherVisit.getId(), payment(totalOf(otherVisit), "RCPT-DUPLICATE", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("already been recorded");

        assertThat(billOf(otherVisit.getId()).getPayment()).isNull();
    }

    @Test
    void anAlreadySettledBillCannotBePaidAgain() {
        Visit visit = visitAwaitingPayment(2);
        paymentService.recordPayment(visit.getId(), payment(totalOf(visit), "RCPT-1008", null));

        assertThatThrownBy(() -> paymentService.recordPayment(visit.getId(), payment(new BigDecimal("1.00"), "RCPT-1009", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("already been settled");
    }

    /**
     * A bill that ends up owing nothing still has to be settled: closure and the release of medicine
     * both hang off settlement, so without this the patient cannot leave and their prescription can
     * never be dispensed. Setting the total to zero isolates the rule from the seeded catalogue price.
     */
    @Test
    void aBillWithNothingOwedCanStillBeSettled() {
        Visit visit = visitAwaitingPayment(1);
        Long prescriptionId = prescriptionIdOf(visit);

        Bill bill = billOf(visit.getId());
        bill.setTotalAmount(BigDecimal.ZERO);
        billRepository.save(bill);

        BillViewDTO settled = paymentService.recordPayment(visit.getId(), payment(BigDecimal.ZERO, null, null));

        assertThat(settled.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(settled.getOutstanding()).isEqualByComparingTo("0");
        // Nothing was collected, so nothing is claimed to have been.
        assertThat(settled.getPayment()).isNull();
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.CLOSED);
        assertThat(prescriptionRepository.findById(prescriptionId).orElseThrow().getStatus()).isEqualTo(
            PrescriptionStatus.READY_FOR_DISPENSE
        );
    }

    @Test
    void moneyCannotBeCollectedWithoutAReceiptNumber() {
        Visit visit = visitAwaitingPayment(4);

        assertThatThrownBy(() -> paymentService.recordPayment(visit.getId(), payment(new BigDecimal("1.00"), null, null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("receipt number");
    }

    // ---------------------------------------------------------------- helpers

    /** A visit taken all the way to the payment stage, with medicine on the bill. */
    private Visit visitAwaitingPayment(int drugQuantity) {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();

        if (drugQuantity > 0) {
            PrescriptionLineRequestDTO line = new PrescriptionLineRequestDTO();
            line.setDrugId(drug.getId());
            line.setDosage("1 tablet twice daily");
            line.setDuration("5 days");
            line.setQuantity(drugQuantity);

            PlacePrescriptionRequestDTO prescription = new PlacePrescriptionRequestDTO();
            prescription.setSource(PrescriptionSource.INTERNAL);
            prescription.setLines(List.of(line));
            prescriptionService.place(visit.getId(), prescription);
        }

        consultationService.complete(consultationId, notes("Sore throat", null));
        return visitRepository.findById(visit.getId()).orElseThrow();
    }

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Payment test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(36.9));
        vitals.setPulseRate(76);
        vitals.setSystolicBp(118);
        vitals.setDiastolicBp(76);
        vitals.setOxygenSaturation(98);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private static RecordPaymentRequestDTO payment(BigDecimal amount, String receiptNumber, PaymentConfirmationStatus confirmation) {
        RecordPaymentRequestDTO request = new RecordPaymentRequestDTO();
        request.setMethod(PaymentMethod.CASH);
        request.setAmount(amount);
        request.setReceiptNumber(receiptNumber);
        request.setConfirmationStatus(confirmation);
        return request;
    }

    private static UpdateConsultationRequestDTO notes(String complaint, String findings) {
        UpdateConsultationRequestDTO request = new UpdateConsultationRequestDTO();
        request.setPresentingComplaint(complaint);
        request.setExaminationFindings(findings);
        return request;
    }

    private Long prescriptionIdOf(Visit visit) {
        return prescriptionRepository.findByVisitId(visit.getId()).getFirst().getId();
    }

    private BigDecimal totalOf(Visit visit) {
        return billOf(visit.getId()).getTotalAmount();
    }

    private Bill billOf(Long visitId) {
        // The visit's association is lazy and these tests run outside a session, so the bill is read
        // back by id rather than touched through the visit.
        return billRepository.findById(visitRepository.findById(visitId).orElseThrow().getBill().getId()).orElseThrow();
    }
}
