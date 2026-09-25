package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AppointmentStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AppointmentRepository;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
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
import com.hyperbrains.hms.service.PatientMergeService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.MergePatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientMergeResultDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for merging a temporary patient record into the confirmed one.
 *
 * <p>The specification lists what has to follow the patient across: vitals, orders, results,
 * prescriptions. None of those name a patient — they all hang off the visit — so the test proves the list
 * by proving the visit moved, and then checks each item is still reachable through it. That is also why
 * the implementation moves visits rather than maintaining a per-entity list of things to move.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
class PatientMergeIT {

    private static final BigDecimal LAB_PRICE = new BigDecimal("25.00");

    private static final BigDecimal DRUG_PRICE = new BigDecimal("2.50");

    @Autowired
    private PatientMergeService mergeService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private DiagnosticOrderWorkflowService orderService;

    @Autowired
    private PrescriptionWorkflowService prescriptionService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
    private ConsultationRepository consultationRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient temporary;

    private Patient confirmed;

    private LabTest labTest;

    private Drug drug;

    private Department department;

    @BeforeEach
    void setUp() {
        // The unknown patient: no identifying details, so an incomplete registration with a temporary number.
        temporary = patient("Unknown Male Casualty", hospitalIdService.nextTemporaryId(), RegistrationStatus.INCOMPLETE_REGISTRATION);
        confirmed = patient("Wanjiku Kamau", hospitalIdService.nextPermanentId(), RegistrationStatus.COMPLETE);

        labTest = new LabTest();
        labTest.setName("Full Blood Count");
        labTest.setPrice(LAB_PRICE);
        labTest.setActive(true);
        labTest = labTestRepository.save(labTest);

        drug = new Drug();
        drug.setName("Paracetamol");
        drug.setUnit("tablet");
        drug.setPrice(DRUG_PRICE);
        drug.setCurrentStock(100);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(10);
        drug.setActive(true);
        drug = drugRepository.save(drug);

        department = new Department();
        department.setName("Outpatient");
        department.setCode("OPD-MERGE-TEST");
        department.setActive(true);
        department = departmentRepository.save(department);
    }

    @AfterEach
    void cleanup() {
        List<Long> patientIds = List.of(temporary.getId(), confirmed.getId());

        List<Visit> visits = patientIds.stream().flatMap(id -> visitRepository.findByPatientId(id).stream()).toList();
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
        List<Long> billIds = visits.stream().map(Visit::getBill).filter(Objects::nonNull).map(Bill::getId).filter(Objects::nonNull).toList();
        List<Long> vitalSignsIds = visits
            .stream()
            .map(visit -> visit.getVitals())
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // Deepest first: the merge makes some of these hang off the other patient, so they are collected
        // through the visits themselves rather than assumed to be under either record.
        patientIds.forEach(id -> appointmentRepository.findByPatientId(id).forEach(appointmentRepository::delete));
        prescriptions.forEach(prescription -> {
            prescriptionLineRepository.deleteAll(prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId()));
            prescriptionRepository.deleteById(prescription.getId());
        });
        orderIds.forEach(id -> diagnosticOrderRepository.findById(id).ifPresent(diagnosticOrderRepository::delete));
        resultIds.forEach(id -> resultRepository.findById(id).ifPresent(resultRepository::delete));
        visits.forEach(visit -> visitRepository.deleteById(visit.getId()));
        consultationIds.forEach(id -> consultationRepository.findById(id).ifPresent(consultationRepository::delete));
        billIds.forEach(id -> {
            billLineItemRepository.findByBillIdOrderByIdAsc(id).forEach(billLineItemRepository::delete);
            billRepository.findById(id).ifPresent(billRepository::delete);
        });
        vitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        patientIds.forEach(id -> patientRepository.findById(id).ifPresent(patientRepository::delete));

        labTestRepository.findById(labTest.getId()).ifPresent(labTestRepository::delete);
        drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete);
        departmentRepository.findById(department.getId()).ifPresent(departmentRepository::delete);
    }

    // ---------------------------------------------------------------- the clinical history follows the patient

    @Test
    void mergingMovesTheWholeEncounterToTheConfirmedPatient() {
        Visit visit = encounterFor(temporary);
        Long visitId = visit.getId();

        PatientMergeResultDTO result = mergeService.merge(merge("Patient was identified after treatment"));

        assertThat(result.sourceStatus()).isEqualTo(RegistrationStatus.MERGED);
        assertThat(result.visitsMoved()).isEqualTo(1);

        // The temporary record has no clinical history left, and is a pointer rather than a patient.
        assertThat(visitRepository.findByPatientId(temporary.getId())).isEmpty();
        Patient mergedAway = patientRepository.findById(temporary.getId()).orElseThrow();
        assertThat(mergedAway.getRegistrationStatus()).isEqualTo(RegistrationStatus.MERGED);
        assertThat(mergedAway.getMergedIntoPatient()).isNotNull();
        assertThat(mergedAway.getMergedIntoPatient().getId()).isEqualTo(confirmed.getId());
        // The real patient keeps their own number; the temporary one is not promoted onto them.
        assertThat(mergedAway.getHospitalId()).startsWith("UNK");
        assertThat(patientRepository.findById(confirmed.getId()).orElseThrow().getHospitalId()).startsWith("HMS");

        // And every item the specification names is now reachable through the confirmed patient's visit.
        assertThat(visitRepository.findByPatientId(confirmed.getId())).singleElement().satisfies(moved -> assertThat(moved.getId()).isEqualTo(visitId));

        Visit moved = visitRepository.findById(visitId).orElseThrow();
        assertThat(moved.getPatient().getId()).isEqualTo(confirmed.getId());
        assertThat(moved.getVitals()).isNotNull();
        assertThat(moved.getBill()).isNotNull();
        assertThat(diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visitId))
            .singleElement()
            .satisfies(order -> assertThat(order.getResult()).isNotNull());
        assertThat(prescriptionRepository.findByVisitId(visitId)).hasSize(1);
    }

    @Test
    void appointmentsAlsoFollowThePatient() {
        Appointment booked = appointmentFor(temporary);

        PatientMergeResultDTO result = mergeService.merge(merge("Patient was identified after booking"));

        assertThat(result.appointmentsMoved()).isEqualTo(1);
        assertThat(appointmentRepository.findByPatientId(temporary.getId())).isEmpty();
        assertThat(appointmentRepository.findByPatientId(confirmed.getId()))
            .singleElement()
            .satisfies(appointment -> assertThat(appointment.getId()).isEqualTo(booked.getId()));
    }

    @Test
    void mergingWithNothingAttachedStillRetiresTheTemporaryRecord() {
        PatientMergeResultDTO result = mergeService.merge(merge("Duplicate created by mistake at reception"));

        assertThat(result.visitsMoved()).isZero();
        assertThat(result.appointmentsMoved()).isZero();
        assertThat(patientRepository.findById(temporary.getId()).orElseThrow().getRegistrationStatus()).isEqualTo(
            RegistrationStatus.MERGED
        );
    }

    /** Being mid-encounter under both identities builds two open visits, and that is worth reporting. */
    @Test
    void openVisitsAreCountedInTheResult() {
        encounterFor(temporary);

        PatientMergeResultDTO result = mergeService.merge(merge("Identified while still in the department"));

        assertThat(result.openVisitsMoved()).isEqualTo(1);
    }

    // ---------------------------------------------------------------- what a merge refuses

    @Test
    void aConfirmedRecordCannotBeMergedIntoAnotherConfirmedRecord() {
        Patient otherConfirmed = patient("Another Confirmed", hospitalIdService.nextPermanentId(), RegistrationStatus.COMPLETE);

        MergePatientRequestDTO request = new MergePatientRequestDTO();
        request.setSourcePatientId(otherConfirmed.getId());
        request.setTargetPatientId(confirmed.getId());
        request.setReason("Possibly the same person");

        assertThatThrownBy(() -> mergeService.merge(request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("temporary record adopting a confirmed one");

        // Nothing moved, and neither record was retired.
        assertThat(patientRepository.findById(otherConfirmed.getId()).orElseThrow().getRegistrationStatus()).isEqualTo(
            RegistrationStatus.COMPLETE
        );
        patientRepository.findById(otherConfirmed.getId()).ifPresent(patientRepository::delete);
    }

    @Test
    void aRecordCannotBeMergedIntoItself() {
        MergePatientRequestDTO request = new MergePatientRequestDTO();
        request.setSourcePatientId(temporary.getId());
        request.setTargetPatientId(temporary.getId());
        request.setReason("Mistyped the target");

        assertThatThrownBy(() -> mergeService.merge(request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be merged into itself");
    }

    @Test
    void anAlreadyMergedRecordCannotBeMergedAgain() {
        mergeService.merge(merge("First merge"));

        assertThatThrownBy(() -> mergeService.merge(merge("Second attempt")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("has already been merged into");
    }

    /** Merging onto a pointer would put clinical history on a record that is not a patient. */
    @Test
    void aRecordThatIsItselfAPointerCannotAbsorbAnything() {
        mergeService.merge(merge("First merge"));

        Patient anotherTemporary = patient("Unknown Female Casualty", hospitalIdService.nextTemporaryId(), RegistrationStatus.INCOMPLETE_REGISTRATION);
        MergePatientRequestDTO request = new MergePatientRequestDTO();
        request.setSourcePatientId(anotherTemporary.getId());
        request.setTargetPatientId(temporary.getId());
        request.setReason("Same person as the earlier one?");

        assertThatThrownBy(() -> mergeService.merge(request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("has itself been merged");

        patientRepository.findById(anotherTemporary.getId()).ifPresent(patientRepository::delete);
    }

    @Test
    void aMergeWithoutAReasonIsRefused() {
        MergePatientRequestDTO request = merge("   ");

        assertThatThrownBy(() -> mergeService.merge(request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("requires a reason");

        assertThat(patientRepository.findById(temporary.getId()).orElseThrow().getRegistrationStatus()).isEqualTo(
            RegistrationStatus.INCOMPLETE_REGISTRATION
        );
    }

    // ---------------------------------------------------------------- the trail

    /** Both records have to show it: one has to explain where its patient went, the other what it absorbed. */
    @Test
    void theMergeIsRecordedOnBothRecords() {
        encounterFor(temporary);

        mergeService.merge(merge("Patient produced their national ID at the desk"));

        assertThat(
            auditLogRepository
                .findByEntityNameAndEntityIdOrderByIdAsc("Patient", String.valueOf(temporary.getId()))
                .stream()
                .anyMatch(entry -> AuditActions.PATIENT_MERGED.equals(entry.getAction()) && "Patient produced their national ID at the desk".equals(entry.getReason()))
        )
            .as("the temporary record explains where its patient went")
            .isTrue();

        assertThat(
            auditLogRepository
                .findByEntityNameAndEntityIdOrderByIdAsc("Patient", String.valueOf(confirmed.getId()))
                .stream()
                .anyMatch(entry -> AuditActions.PATIENT_MERGED.equals(entry.getAction()))
        )
            .as("the confirmed record shows what it absorbed")
            .isTrue();
    }

    // ---------------------------------------------------------------- helpers

    /** A visit with everything clinical hanging off it: vitals, an order and its result, a prescription. */
    private Visit encounterFor(Patient patient) {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.EMERGENCY);
        intake.setPriority(VisitPriority.EMERGENCY);
        intake.setReasonForVisit("Collapsed in the street");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(37.6));
        vitals.setPulseRate(96);
        vitals.setSystolicBp(110);
        vitals.setDiastolicBp(70);
        vitals.setOxygenSaturation(95);
        triageService.submitVitals(created.getId(), vitals);

        PlaceDiagnosticOrderRequestDTO order = new PlaceDiagnosticOrderRequestDTO();
        order.setType(OrderType.LAB);
        order.setLabTestId(labTest.getId());
        Long orderId = orderService.place(created.getId(), order).getId();

        EnterResultRequestDTO result = new EnterResultRequestDTO();
        result.setResultValue("Normal");
        orderService.enterResult(orderId, result);

        PrescriptionLineRequestDTO line = new PrescriptionLineRequestDTO();
        line.setDrugId(drug.getId());
        line.setDosage("1 tablet twice daily");
        line.setDuration("3 days");
        line.setQuantity(6);

        PlacePrescriptionRequestDTO prescription = new PlacePrescriptionRequestDTO();
        prescription.setSource(PrescriptionSource.INTERNAL);
        prescription.setLines(List.of(line));
        prescriptionService.place(created.getId(), prescription);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private Appointment appointmentFor(Patient patient) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDepartment(department);
        appointment.setScheduledDate(LocalDate.now().plusDays(2));
        appointment.setScheduledTime(LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason("Follow-up");
        return appointmentRepository.save(appointment);
    }

    /** The standard direction under test: the temporary record adopting the confirmed one. */
    private MergePatientRequestDTO merge(String reason) {
        MergePatientRequestDTO request = new MergePatientRequestDTO();
        request.setSourcePatientId(temporary.getId());
        request.setTargetPatientId(confirmed.getId());
        request.setReason(reason);
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
