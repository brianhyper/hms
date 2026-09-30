package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.view.CancelDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.DoctorOrderViewDTO;
import com.hyperbrains.hms.service.dto.view.ExecuteOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.workflow.DoctorOrderWorkflowService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for doctor's orders on the ward.
 *
 * <p>The test that carries the weight is the drug one. §6 forbids a drug order from being a second way to
 * prescribe, so a DRUG order must be backed by a real {@code Prescription} — which means the stock is reserved
 * and the charge raised exactly as it is at the outpatient desk — and withdrawing that order has to withdraw
 * the supply too, or the ward stops giving medicine that is still reserved and still on the bill.
 *
 * <p>The other thing asserted here is who finishes an order: a nurse carrying out a one-off finishes it,
 * carrying out a dose of a recurring course does not.
 *
 * <p>Each test runs in a transaction that is rolled back, so nothing has to be deleted afterwards. The
 * hand-written teardown this replaced deleted a visit's prescription lines and then its bill, which Hibernate
 * refused to flush (an unsaved Bill seen through the Visit) — so every test that had placed a drug order
 * failed in teardown, including tests whose assertions had already passed. Rolling back says the same thing
 * without asking Hibernate to agree.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class DoctorOrderIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private DoctorOrderWorkflowService orderService;

    @Autowired
    private DoctorOrderRepository doctorOrderRepository;

    @Autowired
    private OrderExecutionRepository orderExecutionRepository;

    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private BedTypeRepository bedTypeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionLineRepository prescriptionLineRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    @Autowired
    private MockMvc mockMvc;

    private Department department;

    private Ward ward;

    private Bed bed;

    private BedType bedType;

    private Drug drug;

    private Patient patient;

    private Visit visit;

    private Admission admission;

    private User doctor;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Order IT " + SUFFIX);
        department.setCode("ORIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        ward = new Ward();
        ward.setName("Order Ward " + SUFFIX);
        ward.setActive(true);
        ward.setDepartment(department);
        ward = wardRepository.save(ward);

        bedType = new BedType();
        bedType.setName("Order Type " + SUFFIX);
        bedType.setDefaultDailyRate(new BigDecimal("3500.00"));
        bedType.setActive(true);
        bedType = bedTypeRepository.save(bedType);

        bed = new Bed();
        bed.setBedNumber(SUFFIX + "-O1");
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setWard(ward);
        bed.setBedType(bedType);
        bed = bedRepository.save(bed);

        drug = new Drug();
        drug.setName("Ceftriaxone " + SUFFIX);
        drug.setUnit("vial");
        drug.setPrice(new BigDecimal("8.00"));
        drug.setCurrentStock(20);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(5);
        drug.setActive(true);
        drug = drugRepository.save(drug);

        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Order Patient " + SUFFIX);
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        visit = new Visit();
        visit.setPatient(patient);
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit.setPriority(VisitPriority.NORMAL);
        visit.setReasonForVisit("Order test");
        visit.setCreatedAt(Instant.now());
        visit = visitRepository.save(visit);

        // No bill is opened here. Intake does not open one either: it appears when the first charge is raised,
        // which is what a drug order does through the Phase 1 prescription path.
        doctor = userRepository.findOneByLogin("admin").orElseThrow();

        admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(Instant.parse("2026-09-27T22:00:00Z"));
        admission.setAdmissionReason("Pneumonia");
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setBed(bed);
        admission.setAdmittingDoctor(doctor);
        admission.setPrimaryDoctor(doctor);
        admission = admissionRepository.save(admission);
    }

    // ---------------------------------------------------------------- placing

    @Test
    void anInstructionIsPlacedWithNothingToSupply() {
        DoctorOrderViewDTO order = orderService.place(
            admission.getId(),
            order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.ONE_OFF, "Continue IV fluids at 100ml/hr")
        );

        assertThat(order.status()).isEqualTo(DoctorOrderStatus.ACTIVE);
        assertThat(order.prescriptionId()).as("an instruction carries no medicine").isNull();
        assertThat(order.orderedBy()).isNotNull();
    }

    /** The §6 rule: a drug order is backed by a prescription, so the stock is reserved and the charge raised. */
    @Test
    void aDrugOrderIsBackedByAPrescriptionThatReservesTheStock() {
        DoctorOrderViewDTO order = orderService.place(admission.getId(), drugOrder(4));

        assertThat(order.prescriptionId()).as("a drug order without supply behind it must not exist").isNotNull();

        Prescription prescription = prescriptionRepository.findById(order.prescriptionId()).orElseThrow();
        assertThat(prescription.getVisit().getId()).isEqualTo(visit.getId());
        assertThat(prescription.getStatus())
            .as("the ward collects medicine without paying first; the charge sits on the stay's bill instead")
            .isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
        assertThat(prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId())).hasSize(1);

        assertThat(drugRepository.findById(drug.getId()).orElseThrow().getReservedStock())
            .as("the medicine is set aside when it is prescribed, not when somebody remembers")
            .isEqualTo(4);
    }

    @Test
    void aDrugOrderWithoutADrugAndAQuantityIsRefused() {
        PlaceDoctorOrderRequestDTO request = order(DoctorOrderType.DRUG, DoctorOrderRecurrence.ONE_OFF, "Antibiotics");
        request.setDosage("1 vial twice daily");

        assertThatThrownBy(() -> orderService.place(admission.getId(), request))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("drugAndQuantityRequired")
            );

        assertThat(orderService.orderSheet(admission.getId())).isEmpty();
    }

    @Test
    void nothingCanBeOrderedOnAStayThatIsOver() {
        Long admissionId = admission.getId();
        Admission ended = admissionRepository.findById(admissionId).orElseThrow();
        ended.setStatus(AdmissionStatus.DISCHARGED);
        admissionRepository.save(ended);

        assertThatThrownBy(() -> orderService.place(admissionId, order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.ONE_OFF, "More fluids")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("admissionNotOpen"));
    }

    // ---------------------------------------------------------------- carrying out

    @Test
    void carryingOutARecurringOrderDoesNotFinishIt() {
        DoctorOrderViewDTO placed = orderService.place(admission.getId(), drugOrder(4));

        ExecuteOrderRequestDTO execution = new ExecuteOrderRequestDTO();
        execution.setNotes("Dose given, cannula site clean");
        DoctorOrderViewDTO after = orderService.execute(placed.orderId(), execution);

        assertThat(after.status()).as("a nurse does not decide that a course is over").isEqualTo(DoctorOrderStatus.ACTIVE);
        assertThat(after.executions()).hasSize(1);
        assertThat(after.executions().getFirst().notes()).isEqualTo("Dose given, cannula site clean");
        assertThat(after.executions().getFirst().executedBy()).isNotNull();
    }

    @Test
    void carryingOutAOneOffOrderFinishesIt() {
        DoctorOrderViewDTO placed = orderService.place(
            admission.getId(),
            order(DoctorOrderType.LAB, DoctorOrderRecurrence.ONE_OFF, "Blood cultures before antibiotics")
        );

        DoctorOrderViewDTO after = orderService.execute(placed.orderId(), null);

        assertThat(after.status()).isEqualTo(DoctorOrderStatus.COMPLETED);
        assertThat(after.executions()).hasSize(1);
    }

    @Test
    void aFinishedOrderCannotBeCarriedOutAgain() {
        DoctorOrderViewDTO placed = orderService.place(
            admission.getId(),
            order(DoctorOrderType.LAB, DoctorOrderRecurrence.ONE_OFF, "Full blood count")
        );
        orderService.execute(placed.orderId(), null);

        Long orderId = placed.orderId();
        assertThatThrownBy(() -> orderService.execute(orderId, null))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("orderNotRunning"));
    }

    @Test
    void thePrescriberStopsARecurringCourseAndTheHistoryStays() {
        DoctorOrderViewDTO placed = orderService.place(admission.getId(), drugOrder(4));
        orderService.execute(placed.orderId(), null);

        DoctorOrderViewDTO stopped = orderService.complete(placed.orderId());

        assertThat(stopped.status()).isEqualTo(DoctorOrderStatus.COMPLETED);
        assertThat(stopped.executions()).as("what was given stays on the record").hasSize(1);
    }

    // ---------------------------------------------------------------- withdrawing

    @Test
    void withdrawingAnOrderRequiresAReason() {
        DoctorOrderViewDTO placed = orderService.place(
            admission.getId(),
            order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.RECURRING, "Hourly urine output")
        );

        CancelDoctorOrderRequestDTO request = new CancelDoctorOrderRequestDTO();
        request.setReason("   ");

        Long orderId = placed.orderId();
        assertThatThrownBy(() -> orderService.cancel(orderId, request))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("cancellationReasonRequired")
            );

        assertThat(doctorOrderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(DoctorOrderStatus.ACTIVE);
    }

    /** Withdrawing the instruction has to take the medicine with it, or the ward stops giving what it is billed for. */
    @Test
    void withdrawingADrugOrderWithdrawsItsSupplyAndReleasesTheStock() {
        DoctorOrderViewDTO placed = orderService.place(admission.getId(), drugOrder(4));

        CancelDoctorOrderRequestDTO request = new CancelDoctorOrderRequestDTO();
        request.setReason("Cultures grew a resistant organism, switching agent");
        DoctorOrderViewDTO cancelled = orderService.cancel(placed.orderId(), request);

        assertThat(cancelled.status()).isEqualTo(DoctorOrderStatus.CANCELLED);
        assertThat(cancelled.cancelReason()).isEqualTo("Cultures grew a resistant organism, switching agent");
        assertThat(prescriptionRepository.findById(placed.prescriptionId()).orElseThrow().getStatus())
            .as("the medicine is not owed any more")
            .isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(drugRepository.findById(drug.getId()).orElseThrow().getReservedStock())
            .as("and it is back on the shelf")
            .isZero();
    }

    // ---------------------------------------------------------------- the sheet and the trail

    @Test
    void theOrderSheetIsNewestFirstAndCarriesWhatWasDoneAboutEachOrder() {
        DoctorOrderViewDTO first = orderService.place(
            admission.getId(),
            order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.RECURRING, "Hourly urine output")
        );
        DoctorOrderViewDTO second = orderService.place(
            admission.getId(),
            order(DoctorOrderType.LAB, DoctorOrderRecurrence.ONE_OFF, "Repeat potassium in 4 hours")
        );
        orderService.execute(second.orderId(), null);

        List<DoctorOrderViewDTO> sheet = orderService.orderSheet(admission.getId());

        assertThat(sheet).extracting(DoctorOrderViewDTO::orderId).containsExactly(second.orderId(), first.orderId());
        assertThat(sheet.getFirst().executions()).hasSize(1);
        assertThat(sheet.getLast().executions()).isEmpty();
    }

    @Test
    void placingAndCarryingOutAreBothRecorded() {
        DoctorOrderViewDTO placed = orderService.place(admission.getId(), drugOrder(2));
        orderService.execute(placed.orderId(), null);

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "DoctorOrder",
            String.valueOf(placed.orderId())
        );

        assertThat(trail).anyMatch(entry -> AuditActions.ORDER_PLACED.equals(entry.getAction()));
        assertThat(trail).anyMatch(entry -> AuditActions.ORDER_EXECUTED.equals(entry.getAction()));
    }

    @Test
    void anInpatientsDrugIsChargedToTheStayEvenThoughNobodyPaysBeforeItIsCollected() {
        DoctorOrderViewDTO placed = orderService.place(admission.getId(), drugOrder(3));

        assertThat(placed.prescriptionId()).isNotNull();
        Long bill = billId();
        assertThat(bill).as("ordering a drug for an inpatient still opens a charge on the stay's bill").isNotNull();
        assertThat(billLineItemRepository.findByBillIdOrderByIdAsc(bill)).isNotEmpty();
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    void aDoctorPlacesAnOrderOverHttp() throws Exception {
        mockMvc
            .perform(
                post("/api/inpatient-orders/{admissionId}", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"type\":\"INSTRUCTION\",\"recurrence\":\"RECURRING\",\"details\":\"Hourly urine output\",\"frequency\":\"hourly\"}"
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.prescriptionId").doesNotExist());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_NURSE")
    void aNurseMayNotWriteAnOrder() throws Exception {
        mockMvc
            .perform(
                post("/api/inpatient-orders/{admissionId}", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"INSTRUCTION\",\"recurrence\":\"ONE_OFF\",\"details\":\"Sit up\"}")
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_NURSE")
    void aNurseRecordsCarryingAnOrderOutOverHttp() throws Exception {
        DoctorOrderViewDTO placed = orderService.place(
            admission.getId(),
            order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.RECURRING, "Hourly urine output")
        );

        mockMvc
            .perform(
                post("/api/inpatient-orders/{orderId}/executions", placed.orderId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"notes\":\"Patient asleep, not woken\"}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.executions[0].notes").value("Patient asleep, not woken"));
    }

    /** The doctor writes and stops orders; the ward says what was actually given. */
    @Test
    void aDoctorMayNotRecordAnExecution() throws Exception {
        DoctorOrderViewDTO placed = orderService.place(
            admission.getId(),
            order(DoctorOrderType.INSTRUCTION, DoctorOrderRecurrence.RECURRING, "Hourly urine output")
        );

        mockMvc
            .perform(post("/api/inpatient-orders/{orderId}/executions", placed.orderId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void theGeneratedOrderCrudIsClosedToTheWard() throws Exception {
        mockMvc
            .perform(put("/api/doctor-orders/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- helpers

    /** The visit's bill, if a charge ever opened one. Read fresh, because the fixture's copy is stale. */
    private Long billId() {
        return visitRepository.findById(visit.getId()).map(fresh -> fresh.getBill()).map(bill -> bill.getId()).orElse(null);
    }

    private static PlaceDoctorOrderRequestDTO order(DoctorOrderType type, DoctorOrderRecurrence recurrence, String details) {
        PlaceDoctorOrderRequestDTO request = new PlaceDoctorOrderRequestDTO();
        request.setType(type);
        request.setRecurrence(recurrence);
        request.setDetails(details);
        return request;
    }

    private PlaceDoctorOrderRequestDTO drugOrder(int quantity) {
        PlaceDoctorOrderRequestDTO request = order(DoctorOrderType.DRUG, DoctorOrderRecurrence.RECURRING, "Ceftriaxone 1 vial twice daily");
        request.setDrugId(drug.getId());
        request.setQuantity(quantity);
        request.setDosage("1 vial twice daily");
        request.setDuration("5 days");
        return request;
    }
}
