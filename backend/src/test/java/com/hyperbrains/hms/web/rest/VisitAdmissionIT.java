package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationAddendumRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
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
 * Integration tests for the outpatient-to-inpatient conversion.
 *
 * <p>Two things are being proved here. The first is the specification's central claim: the visit is not
 * closed and replaced — its type changes in place and everything already recorded on it survives. The
 * second is what "bypasses the path toward payment" actually means in behaviour: after admission, a
 * finished consultation and a landing lab result must both fail to move the visit to the payment stage,
 * because an admitted patient's charges accumulate over the stay instead.
 *
 * <p>The consultation fee is deliberately not asserted on, so a change to the fee cannot break these
 * tests. The accumulated total is read from the bill instead.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class VisitAdmissionIT {

    @Autowired
    private AdmissionWorkflowService admissionService;

    @Autowired
    private ConsultationWorkflowService consultationService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private DiagnosticOrderWorkflowService orderService;

    @Autowired
    private PrescriptionWorkflowService prescriptionService;

    @Autowired
    private PaymentWorkflowService paymentService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private ConsultationAddendumRepository addendumRepository;

    @Autowired
    private DiagnosticOrderRepository diagnosticOrderRepository;

    @Autowired
    private ResultRepository resultRepository;

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
    private LabTestRepository labTestRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private LabTest labTest;

    private Drug drug;

    @BeforeEach
    void setUp() {
        patient = patient("Admission Test Patient", hospitalIdService.nextPermanentId(), RegistrationStatus.COMPLETE);

        labTest = new LabTest();
        labTest.setName("Malaria RDT");
        labTest.setPrice(new BigDecimal("12.00"));
        labTest.setActive(true);
        labTest = labTestRepository.save(labTest);

        drug = new Drug();
        drug.setName("Ceftriaxone");
        drug.setUnit("vial");
        drug.setPrice(new BigDecimal("8.00"));
        drug.setCurrentStock(50);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(5);
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
        List<Long> orderIds = visits
            .stream()
            .flatMap(visit -> diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visit.getId()).stream())
            .map(order -> order.getId())
            .toList();
        List<Long> resultIds = orderIds
            .stream()
            .flatMap(id -> diagnosticOrderRepository.findById(id).stream())
            .map(order -> order.getResult())
            .filter(Objects::nonNull)
            .map(result -> result.getId())
            .filter(Objects::nonNull)
            .toList();
        List<Long> consultationIds = visits
            .stream()
            .map(Visit::getConsultation)
            .filter(Objects::nonNull)
            .map(Consultation::getId)
            .filter(Objects::nonNull)
            .toList();
        List<Long> billIds = visits
            .stream()
            .map(Visit::getBill)
            .filter(Objects::nonNull)
            .map(Bill::getId)
            .filter(Objects::nonNull)
            .toList();
        List<Long> vitalSignsIds = visits
            .stream()
            .map(Visit::getVitals)
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // Deepest first: an order carries a reference to its result, and the visit carries references to
        // the consultation, the bill and the vitals.
        prescriptions.forEach(prescription -> {
            prescriptionLineRepository.deleteAll(prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId()));
            prescriptionRepository.deleteById(prescription.getId());
        });
        orderIds.forEach(id -> diagnosticOrderRepository.findById(id).ifPresent(diagnosticOrderRepository::delete));
        resultIds.forEach(id -> resultRepository.findById(id).ifPresent(resultRepository::delete));
        consultationIds.forEach(id ->
            addendumRepository.findByConsultationIdOrderByCreatedAtAsc(id).forEach(addendumRepository::delete)
        );
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
        labTestRepository.findById(labTest.getId()).ifPresent(labTestRepository::delete);
        drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete);
    }

    // ---------------------------------------------------------------- the visit is changed, not replaced

    @Test
    void admittingDuringAConsultationKeepsEverythingAlreadyRecordedOnTheSameVisit() {
        Visit visit = visitInConsultation();
        Long visitId = visit.getId();
        Long consultationId = visit.getConsultation().getId();
        Long vitalSignsId = visit.getVitals().getId();

        VisitAdmissionResultDTO result = admissionService.admit(visitId, admit("Sepsis, needs IV antibiotics"));

        assertThat(result.visitType()).isEqualTo(VisitType.ADMISSION);
        assertThat(result.visitStatus()).isEqualTo(VisitStatus.ADMITTED);
        assertThat(result.consultationId()).isEqualTo(consultationId);

        Visit admitted = visitRepository.findById(visitId).orElseThrow();
        assertThat(admitted.getType()).isEqualTo(VisitType.ADMISSION);
        assertThat(admitted.getStatus()).isEqualTo(VisitStatus.ADMITTED);
        // Nothing was detached and no new visit appeared.
        assertThat(visitRepository.findByPatientId(patient.getId())).singleElement().satisfies(only -> assertThat(only.getId()).isEqualTo(visitId));
        assertThat(admitted.getPatient().getId()).isEqualTo(patient.getId());
        assertThat(admitted.getVitals().getId()).isEqualTo(vitalSignsId);
        assertThat(admitted.getConsultation().getId()).isEqualTo(consultationId);
        assertThat(consultationRepository.findById(consultationId).orElseThrow().getVisit().getId()).isEqualTo(visitId);
    }

    @Test
    void everyItemTheEncounterAlreadyHeldIsStillReachableThroughTheAdmittedVisit() {
        Visit visit = visitInConsultation();
        Long visitId = visit.getId();
        placeOrder(visitId);
        placePrescription(visitId);

        admissionService.admit(visitId, admit("Pneumonia requiring inpatient care"));

        assertThat(diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visitId)).hasSize(1);
        assertThat(prescriptionRepository.findByVisitId(visitId)).hasSize(1);
        assertThat(vitalSignsRepository.findById(visit.getVitals().getId())).isPresent();
        assertThat(visitRepository.findById(visitId).orElseThrow().getConsultation()).isNotNull();
    }

    // ---------------------------------------------------------------- the outpatient path is left

    @Test
    void completingTheConsultationAfterAdmissionDoesNotSendTheVisitToPayment() {
        Visit visit = visitInConsultation();
        admissionService.admit(visit.getId(), admit("Admitted before the notes were finished"));

        consultationService.complete(visit.getConsultation().getId(), notes("Fever", "Chest crackles"));

        // The doctor still has to finish the note, and doing so must not undo the admission or push the
        // patient into a cash queue they are not in.
        Visit afterCompletion = visitRepository.findById(visit.getId()).orElseThrow();
        assertThat(afterCompletion.getStatus()).isEqualTo(VisitStatus.ADMITTED);
        assertThat(afterCompletion.getType()).isEqualTo(VisitType.ADMISSION);
        assertThat(consultationRepository.findById(visit.getConsultation().getId()).orElseThrow().getStatus()).isEqualTo(
            ConsultationStatus.COMPLETED
        );
    }

    @Test
    void aResultLandingAfterAdmissionDoesNotMoveTheVisitTowardsPayment() {
        Visit visit = visitInConsultation();
        Long visitId = visit.getId();
        Long orderId = placeOrder(visitId);

        VisitAdmissionResultDTO result = admissionService.admit(visitId, admit("Admitted while awaiting the result"));
        assertThat(result.ordersStillOpen()).isEqualTo(1);

        orderService.enterResult(orderId, enteredResult("Positive"));

        // This is the behaviour the "bypasses the path toward payment" clause is about: the outstanding
        // work resolves, but there is no payment stage for an admitted patient to land on.
        assertThat(visitRepository.findById(visitId).orElseThrow().getStatus()).isEqualTo(VisitStatus.ADMITTED);
        assertThat(visitRepository.findById(visitId).orElseThrow().getType()).isEqualTo(VisitType.ADMISSION);
    }

    @Test
    void chargesAlreadyRaisedAreLeftOnTheBillAndTheReservedStockStaysHeld() {
        Visit visit = visitInConsultation();
        Long visitId = visit.getId();
        placePrescription(visitId);

        VisitAdmissionResultDTO result = admissionService.admit(visitId, admit("Admitted after the first dose"));

        // Nothing is silently voided or refunded: the specification says the charges accumulate over the
        // stay, so an admission that quietly cleared the bill would contradict it. The consequences are
        // reported instead — the prescription can no longer be paid for, so it can no longer be dispensed
        // on this visit, and its stock stays reserved until a clinician withdraws it.
        assertThat(result.prescriptionsAwaitingDispense()).isEqualTo(1);
        assertThat(result.chargesAccumulated()).isEqualByComparingTo(totalOf(visitId));
        assertThat(prescriptionRepository.findByVisitId(visitId).getFirst().getStatus()).isEqualTo(PrescriptionStatus.PENDING);
        assertThat(drugRepository.findById(drug.getId()).orElseThrow().getReservedStock()).isEqualTo(4);
    }

    @Test
    void anUnidentifiedPatientCanBeAdmittedAndTheResultSaysTheRecordIsNotConfirmed() {
        // Deliberate: admitting a patient whose identity is not yet known is normal practice, so this is
        // reported rather than refused. What must not happen is the caller being left unaware of it.
        Patient unidentified = patient("Unknown Male Casualty", hospitalIdService.nextTemporaryId(), RegistrationStatus.INCOMPLETE_REGISTRATION);
        patientRepository.delete(patient);
        patient = unidentified;

        Visit visit = visitInConsultation();

        VisitAdmissionResultDTO result = admissionService.admit(visit.getId(), admit("Head injury, identity unknown"));

        assertThat(result.registrationStatus()).isEqualTo(RegistrationStatus.INCOMPLETE_REGISTRATION);
        assertThat(result.patientHospitalId()).isEqualTo(unidentified.getHospitalId());
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.ADMITTED);
    }

    // ---------------------------------------------------------------- what the action refuses

    @Test
    void aClosedEncounterCannotBeAdmitted() {
        Visit visit = visitInConsultation();
        consultationService.complete(visit.getConsultation().getId(), notes("Headache", "No focal signs"));
        paymentService.recordPayment(visit.getId(), payment(totalOf(visit.getId())));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.CLOSED);

        assertThatThrownBy(() -> admissionService.admit(visit.getId(), admit("Changed our mind")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("CLOSED and cannot be converted to an admission");
    }

    @Test
    void aPatientNobodyHasAssessedCannotBeAdmitted() {
        Visit visit = visitAwaitingDoctor();

        assertThatThrownBy(() -> admissionService.admit(visit.getId(), admit("Keep them in")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("nobody has assessed this patient");

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getType()).isEqualTo(VisitType.OUTPATIENT);
    }

    @Test
    void admittingTheSameVisitTwiceIsRefused() {
        Visit visit = visitInConsultation();
        admissionService.admit(visit.getId(), admit("First decision"));

        assertThatThrownBy(() -> admissionService.admit(visit.getId(), admit("Second attempt")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("already an admission");
    }

    @Test
    void anAdmissionWithoutAReasonIsRefused() {
        Visit visit = visitInConsultation();

        assertThatThrownBy(() -> admissionService.admit(visit.getId(), admit("   ")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("requires a reason");

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.IN_CONSULTATION);
    }

    @Test
    void anUnknownVisitCannotBeAdmitted() {
        assertThatThrownBy(() -> admissionService.admit(-1L, admit("No such visit")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("No visit with id -1");
    }

    // ---------------------------------------------------------------- the trail

    @Test
    void theConversionIsRecordedAsAnAdmissionAndAsAStatusMove() {
        Visit visit = visitInConsultation();

        admissionService.admit(visit.getId(), admit("Diabetic ketoacidosis"));

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Visit", String.valueOf(visit.getId()));

        assertThat(trail)
            .as("the action itself, with the reason and the type change")
            .anyMatch(entry ->
                AuditActions.PATIENT_ADMITTED.equals(entry.getAction()) &&
                "Diabetic ketoacidosis".equals(entry.getReason()) &&
                VisitType.OUTPATIENT.name().equals(entry.getOldValue()) &&
                VisitType.ADMISSION.name().equals(entry.getNewValue())
            );
        assertThat(trail)
            .as("the status move, recorded the same way as every other one")
            .anyMatch(entry ->
                AuditActions.VISIT_STATUS_CHANGED.equals(entry.getAction()) &&
                VisitStatus.IN_CONSULTATION.name().equals(entry.getOldValue()) &&
                VisitStatus.ADMITTED.name().equals(entry.getNewValue())
            );
    }

    // ---------------------------------------------------------------- helpers

    /** A visit with vitals done and a doctor at the patient's side: the state the action is aimed at. */
    private Visit visitInConsultation() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());
        assertThat(consultation.getId()).isNotNull();
        return visitRepository.findById(visit.getId()).orElseThrow();
    }

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Admission conversion test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(38.4));
        vitals.setPulseRate(104);
        vitals.setSystolicBp(106);
        vitals.setDiastolicBp(64);
        vitals.setOxygenSaturation(94);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private Long placeOrder(Long visitId) {
        PlaceDiagnosticOrderRequestDTO order = new PlaceDiagnosticOrderRequestDTO();
        order.setType(OrderType.LAB);
        order.setLabTestId(labTest.getId());
        return orderService.place(visitId, order).getId();
    }

    private void placePrescription(Long visitId) {
        PrescriptionLineRequestDTO line = new PrescriptionLineRequestDTO();
        line.setDrugId(drug.getId());
        line.setDosage("1 vial twice daily");
        line.setDuration("5 days");
        line.setQuantity(4);

        PlacePrescriptionRequestDTO request = new PlacePrescriptionRequestDTO();
        request.setSource(PrescriptionSource.INTERNAL);
        request.setLines(List.of(line));
        prescriptionService.place(visitId, request);
    }

    private BigDecimal totalOf(Long visitId) {
        Bill bill = billRepository.findById(visitRepository.findById(visitId).orElseThrow().getBill().getId()).orElseThrow();
        return bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
    }

    private static AdmitPatientRequestDTO admit(String reason) {
        AdmitPatientRequestDTO request = new AdmitPatientRequestDTO();
        request.setAdmissionReason(reason);
        return request;
    }

    private static EnterResultRequestDTO enteredResult(String value) {
        EnterResultRequestDTO request = new EnterResultRequestDTO();
        request.setResultValue(value);
        return request;
    }

    private static UpdateConsultationRequestDTO notes(String complaint, String findings) {
        UpdateConsultationRequestDTO request = new UpdateConsultationRequestDTO();
        request.setPresentingComplaint(complaint);
        request.setExaminationFindings(findings);
        return request;
    }

    private static RecordPaymentRequestDTO payment(BigDecimal amount) {
        RecordPaymentRequestDTO request = new RecordPaymentRequestDTO();
        request.setMethod(PaymentMethod.CASH);
        request.setAmount(amount);
        // The receipt is only required when money actually moves, which is the case here: the
        // consultation fee is a real figure, so settling this bill collects something. The number is
        // distinctive because a receipt is the hospital's record of a payment and must not be reused.
        request.setReceiptNumber("RCPT-ADMISSION-IT");
        return request;
    }

    private Patient patient(String fullName, String hospitalId, RegistrationStatus status) {
        Patient record = new Patient();
        record.setHospitalId(hospitalId);
        record.setFullName(fullName);
        record.setSex(Sex.MALE);
        record.setSexEstimated(false);
        record.setRegistrationStatus(status);
        return patientRepository.save(record);
    }
}
