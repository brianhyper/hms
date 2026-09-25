package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.RadiologyExamRepository;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.OrderSummaryDTO;
import com.hyperbrains.hms.service.dto.view.OrderWorklistItemDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for ordering tests, recording results, and the visit status that follows.
 *
 * <p>The point of this slice is that a result arriving is an event that can change where the patient
 * is in the hospital. Most of these tests therefore assert on the visit as much as on the order.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class DiagnosticOrderIT {

    private static final BigDecimal LAB_PRICE = new BigDecimal("25.00");

    private static final BigDecimal IMAGING_PRICE = new BigDecimal("40.00");

    @Autowired
    private DiagnosticOrderWorkflowService orderService;

    @Autowired
    private VisitStatusService visitStatusService;

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
    private DiagnosticOrderRepository diagnosticOrderRepository;

    @Autowired
    private ResultRepository resultRepository;

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
    private RadiologyExamRepository radiologyExamRepository;

    @Autowired
    private HospitalServiceRepository hospitalServiceRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private LabTest labTest;

    private RadiologyExam radiologyExam;

    private Long labOrderId;

    private Long radiologyOrderId;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Diagnostics Test Patient");
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        labTest = new LabTest();
        labTest.setName("Full Blood Count");
        labTest.setPrice(LAB_PRICE);
        labTest.setActive(true);
        labTest = labTestRepository.save(labTest);

        radiologyExam = new RadiologyExam();
        radiologyExam.setName("Chest X-Ray");
        radiologyExam.setPrice(IMAGING_PRICE);
        radiologyExam.setActive(true);
        radiologyExam = radiologyExamRepository.save(radiologyExam);
    }

    @AfterEach
    void cleanup() {
        List<Visit> visits = patient == null || patient.getId() == null
            ? List.of()
            : visitRepository.findByPatientId(patient.getId());

        List<Long> visitIds = visits.stream().map(Visit::getId).toList();
        List<Long> orderIds = visitIds
            .stream()
            .flatMap(id -> diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(id).stream())
            .map(DiagnosticOrder::getId)
            .toList();
        List<Long> resultIds = orderIds
            .stream()
            .flatMap(id -> diagnosticOrderRepository.findById(id).stream())
            .map(DiagnosticOrder::getResult)
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
            .map(vitals -> vitals.getVitals())
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // Children before parents: an order points at both its visit and its result, and a visit
        // points at its consultation and its bill. Deleting in the wrong order fails on a foreign
        // key and reads like a product bug rather than a teardown mistake.
        orderIds.forEach(id -> diagnosticOrderRepository.findById(id).ifPresent(diagnosticOrderRepository::delete));
        resultIds.forEach(id -> resultRepository.findById(id).ifPresent(resultRepository::delete));
        visits.forEach(visitRepository::delete);
        consultationIds.forEach(id -> consultationRepository.findById(id).ifPresent(consultationRepository::delete));
        billIds.forEach(id -> {
            billLineItemRepository.findByBillIdOrderByIdAsc(id).forEach(billLineItemRepository::delete);
            billRepository.findById(id).ifPresent(billRepository::delete);
        });
        vitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }
        if (labTest != null && labTest.getId() != null) {
            labTestRepository.findById(labTest.getId()).ifPresent(labTestRepository::delete);
        }
        if (radiologyExam != null && radiologyExam.getId() != null) {
            radiologyExamRepository.findById(radiologyExam.getId()).ifPresent(radiologyExamRepository::delete);
        }
    }

    @Test
    void orderingATestRecordsTheCatalogueNameAndLeavesItPending() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);

        DiagnosticOrderDTO order = orderService.place(visit.getId(), labOrder("Fasting sample required"));

        assertThat(order.getType()).isEqualTo(OrderType.LAB);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        DiagnosticOrder reloaded = diagnosticOrderRepository.findById(order.getId()).orElseThrow();
        // Copied from the catalogue rather than referenced, so a later rename cannot rewrite what was
        // actually ordered.
        assertThat(reloaded.getTestName()).isEqualTo("Full Blood Count");
        assertThat(reloaded.getVisit().getId()).isEqualTo(visit.getId());
        assertThat(reloaded.getOrderedBy().getLogin()).isEqualTo("admin");
        assertThat(consultationId).isNotNull();
    }

    @Test
    void orderingATestDoesNotDisturbAVisitThatIsStillInConsultation() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        orderService.place(visit.getId(), labOrder(null));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.IN_CONSULTATION);
    }

    /**
     * The behaviour this slice exists for: a doctor finishing their notes no longer sends the patient
     * to the payment desk when the lab has not answered yet.
     */
    @Test
    void anOutstandingOrderHoldsTheVisitBackFromPayment() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        orderService.place(visit.getId(), labOrder(null));

        consultationService.complete(consultationId, notes("Sore throat", null));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_RESULTS);
    }

    @Test
    void anOrderThatIsStillOutstandingIsNotYetOnTheBill() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        orderService.place(visit.getId(), labOrder(null));
        consultationService.complete(consultationId, notes("Sore throat", null));

        // Charge only what has actually been delivered: an unperformed test is not a debt.
        assertThat(billLinesOf(visit.getId()))
            .extracting(BillLineItem::getSourceType)
            .containsExactly(BillLineSourceType.CONSULTATION);
    }

    @Test
    void enteringTheLastResultChargesForTheTestAndReleasesTheVisit() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long orderId = orderService.place(visit.getId(), labOrder(null)).getId();
        consultationService.complete(consultationId, notes("Sore throat", null));

        orderService.enterResult(orderId, result("Neutrophils elevated"));

        assertThat(diagnosticOrderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);

        List<BillLineItem> lines = billLinesOf(visit.getId());
        assertThat(lines)
            .extracting(BillLineItem::getSourceRef)
            .containsExactlyInAnyOrder("CONSULTATION:" + consultationId, "LAB:" + orderId);
        assertThat(lines)
            .filteredOn(line -> line.getSourceRef().startsWith("LAB:"))
            .singleElement()
            .satisfies(line -> {
                assertThat(line.getDescription()).isEqualTo("Full Blood Count");
                assertThat(line.getAmount()).isEqualByComparingTo(LAB_PRICE);
            });

        BigDecimal consultationFee = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(consultationFee.add(LAB_PRICE));
    }

    @Test
    void oneResultArrivingIsNotEnoughToReleaseAVisitWithTwoTestsOutstanding() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long labOrderId = orderService.place(visit.getId(), labOrder(null)).getId();
        Long imagingOrderId = orderService.place(visit.getId(), imagingOrder(null)).getId();
        consultationService.complete(consultationId, notes("Cough and fever", null));

        orderService.enterResult(labOrderId, result("Normal"));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_RESULTS);

        orderService.enterResult(imagingOrderId, result("No consolidation"));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
    }

    @Test
    void aResultCannotBeEnteredTwice() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long orderId = orderService.place(visit.getId(), labOrder(null)).getId();
        orderService.enterResult(orderId, result("Normal"));

        assertThatThrownBy(() -> orderService.enterResult(orderId, result("Corrected value")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("can no longer receive a result");

        // And the retry did not charge a second line for the same test.
        assertThat(billLinesOf(visit.getId())).filteredOn(line -> line.getSourceRef().startsWith("LAB:")).hasSize(1);
    }

    @Test
    void cancellingTheOnlyOutstandingOrderReleasesTheVisitWithoutCharging() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long orderId = orderService.place(visit.getId(), labOrder(null)).getId();
        consultationService.complete(consultationId, notes("Sore throat", null));

        orderService.cancel(orderId);

        assertThat(diagnosticOrderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        // A cancelled test is never going to produce a result, so it must not strand the visit.
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(billLinesOf(visit.getId())).filteredOn(line -> line.getSourceRef().startsWith("LAB:")).isEmpty();
    }

    /** A completed order is already charged for, so removing it would be a refund, not a cancellation. */
    @Test
    void aCompletedOrderCannotBeCancelled() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long orderId = orderService.place(visit.getId(), labOrder(null)).getId();
        orderService.enterResult(orderId, result("Normal"));

        assertThatThrownBy(() -> orderService.cancel(orderId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be cancelled");
    }

    /**
     * A patient sitting at the payment desk has not left yet. If the clinician sends them for one
     * more test, the visit has to go back to waiting for it — otherwise the bill is settled while
     * something is still outstanding.
     */
    @Test
    void anOrderRaisedWhileThePatientWaitsToPayPullsTheVisitBack() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        consultationService.complete(consultationId, notes("Nothing outstanding yet", null));
        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);

        Long orderId = orderService.place(visit.getId(), labOrder(null)).getId();

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_RESULTS);

        orderService.enterResult(orderId, result("Normal"));

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
        // And the second pass over the bill picks the new test up.
        BigDecimal consultationFee = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(consultationFee.add(LAB_PRICE));
    }

    /** Once the visit is closed the patient has gone, so nothing more may be ordered against it. */
    @Test
    void anOrderCannotBeRaisedOnAClosedVisit() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        Visit closed = visitRepository.findById(visit.getId()).orElseThrow();
        closed.setStatus(VisitStatus.CLOSED);
        visitRepository.save(closed);

        assertThatThrownBy(() -> orderService.place(visit.getId(), labOrder(null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("no longer accepts clinical work");
    }

    /**
     * The derivation is only allowed to move a visit once the consultation is over. Without that
     * guard, saving vitals on a fresh visit would find nothing outstanding and push the patient
     * straight to the payment desk.
     */
    @Test
    void recomputingBeforeTheConsultationIsFinishedChangesNothing() {
        Visit visit = visitAwaitingDoctor();

        assertThat(visitStatusService.recompute(visit.getId())).isEqualTo(VisitStatus.WAITING_DOCTOR);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_LAB")
    void theLabWorklistShowsLabWorkAndNothingElse() {
        orderBothKindsOfTest();

        List<Long> visible = orderService
            .worklist(PageRequest.of(0, 50))
            .getContent()
            .stream()
            .map(OrderWorklistItemDTO::getOrderId)
            .toList();

        assertThat(visible).contains(labOrderId);
        assertThat(visible).doesNotContain(radiologyOrderId);
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RADIOLOGY")
    void theRadiologyWorklistShowsImagingAndNothingElse() {
        orderBothKindsOfTest();

        List<Long> visible = orderService
            .worklist(PageRequest.of(0, 50))
            .getContent()
            .stream()
            .map(OrderWorklistItemDTO::getOrderId)
            .toList();

        assertThat(visible).contains(radiologyOrderId);
        assertThat(visible).doesNotContain(labOrderId);
    }

    /** A worklist is a list of things still to do, so a completed test must drop off it. */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_LAB")
    void aCompletedTestLeavesTheWorklist() {
        orderBothKindsOfTest();
        assertThat(orderService.worklist(PageRequest.of(0, 50)).getContent())
            .extracting(OrderWorklistItemDTO::getOrderId)
            .contains(labOrderId);

        orderService.enterResult(labOrderId, result("Normal"));

        assertThat(orderService.worklist(PageRequest.of(0, 50)).getContent())
            .extracting(OrderWorklistItemDTO::getOrderId)
            .doesNotContain(labOrderId);
    }

    /**
     * Nobody but the clinicians treating the patient reads the ordering notes, so the finance view
     * must not carry them — or the result, or the imaging reference.
     */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_FINANCE")
    void theFinanceViewCarriesPricesButNoClinicalContent() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        orderService.place(visit.getId(), labOrder("Suspected leukaemia"));

        List<OrderSummaryDTO> billable = orderService.billableForVisit(visit.getId());

        assertThat(billable)
            .singleElement()
            .satisfies(summary -> {
                assertThat(summary.getTestName()).isEqualTo("Full Blood Count");
                assertThat(summary.getPrice()).isEqualByComparingTo(LAB_PRICE);
            });

        // Structural, deliberately: the redaction is a property of the view type, so if a field is
        // added to it the test fails here rather than in production.
        assertThat(fieldNames(OrderSummaryDTO.class)).doesNotContain("notes", "resultValue", "imageReference", "orderedByLogin");
        // And the clinical view does carry the notes, which is what makes the difference above a
        // decision rather than an oversight.
        assertThat(fieldNames(OrderWorklistItemDTO.class)).contains("notes");
    }

    @Test
    void theOrderingDoctorCanSeeWhatWasOrderedForTheirVisit() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long orderId = orderService.place(visit.getId(), labOrder("Fasting sample required")).getId();

        assertThat(orderService.forVisit(visit.getId()))
            .singleElement()
            .satisfies(item -> {
                assertThat(item.getOrderId()).isEqualTo(orderId);
                assertThat(item.getNotes()).isEqualTo("Fasting sample required");
                assertThat(item.getPatient().getHospitalId()).isEqualTo(patient.getHospitalId());
            });
    }

    private void orderBothKindsOfTest() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        labOrderId = orderService.place(visit.getId(), labOrder(null)).getId();
        radiologyOrderId = orderService.place(visit.getId(), imagingOrder(null)).getId();
    }

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Diagnostics test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(37.1));
        vitals.setPulseRate(80);
        vitals.setSystolicBp(120);
        vitals.setDiastolicBp(78);
        vitals.setOxygenSaturation(97);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private Long startConsultation(Visit visit) {
        return consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();
    }

    private PlaceDiagnosticOrderRequestDTO labOrder(String notes) {
        PlaceDiagnosticOrderRequestDTO request = new PlaceDiagnosticOrderRequestDTO();
        request.setType(OrderType.LAB);
        request.setLabTestId(labTest.getId());
        request.setNotes(notes);
        return request;
    }

    private PlaceDiagnosticOrderRequestDTO imagingOrder(String notes) {
        PlaceDiagnosticOrderRequestDTO request = new PlaceDiagnosticOrderRequestDTO();
        request.setType(OrderType.RADIOLOGY);
        request.setRadiologyExamId(radiologyExam.getId());
        request.setNotes(notes);
        return request;
    }

    private static EnterResultRequestDTO result(String value) {
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

    private List<BillLineItem> billLinesOf(Long visitId) {
        return billLineItemRepository.findByBillIdOrderByIdAsc(billOf(visitId).getId());
    }

    /** Always read the visit back: the in-memory copy is not what other transactions saw. */
    private VisitStatus statusOf(Visit visit) {
        return visitRepository.findById(visit.getId()).orElseThrow().getStatus();
    }

    private Bill billOf(Long visitId) {
        // The visit's association is lazy and these tests run outside a session, so the bill is read
        // back by id rather than touched through the visit.
        return billRepository.findById(visitRepository.findById(visitId).orElseThrow().getBill().getId()).orElseThrow();
    }

    private static List<String> fieldNames(Class<?> type) {
        return Stream.concat(Arrays.stream(type.getDeclaredFields()), Arrays.stream(type.getSuperclass().getDeclaredFields()))
            .map(Field::getName)
            .toList();
    }
}
