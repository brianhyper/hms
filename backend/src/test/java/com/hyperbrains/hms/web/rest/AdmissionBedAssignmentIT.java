package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.view.AdmissionBedResultDTO;
import com.hyperbrains.hms.service.dto.view.AssignBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.AwaitingBedViewDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import com.hyperbrains.hms.service.workflow.InpatientWorklistService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for putting a patient into a bed, and for the worklist that shows who is still waiting.
 *
 * <p>The refusals carry most of the weight here. An assignment is a two-row change — the stay gains a bed,
 * the bed becomes occupied — and every refusal has to leave <em>both</em> rows exactly as they were, or a
 * bed ends up marked occupied with nobody in it. Each refusal is therefore asserted against the database
 * afterwards, not just against the exception.
 *
 * <p>One rule in here has no database constraint behind it at all: "one open admission per patient", which
 * cannot be an index because the patient is reached through the visit. These tests are the only thing
 * holding it in place, which is why they walk through the service rather than the repository.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class AdmissionBedAssignmentIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private AdmissionWorkflowService admissionWorkflowService;

    @Autowired
    private InpatientWorklistService inpatientWorklistService;

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
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    @Autowired
    private MockMvc mockMvc;

    private Department department;

    private Ward openWard;

    private Ward closedWard;

    private BedType bedType;

    private Bed freeBed;

    private Bed secondFreeBed;

    private Patient patient;

    private Visit visit;

    private User doctor;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Bed Assignment IT " + SUFFIX);
        department.setCode("BAIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        openWard = ward("Bed Assignment Ward " + SUFFIX, true);
        closedWard = ward("Bed Assignment Closed Ward " + SUFFIX, false);

        bedType = new BedType();
        bedType.setName("Bed Assignment Type " + SUFFIX);
        bedType.setDefaultDailyRate(new BigDecimal("3500.00"));
        bedType.setActive(true);
        bedType = bedTypeRepository.save(bedType);

        freeBed = bed(openWard, "F1", BedStatus.AVAILABLE);
        secondFreeBed = bed(openWard, "F2", BedStatus.AVAILABLE);

        patient = patient("Bed Assignment Patient " + SUFFIX, hospitalIdService.nextPermanentId());
        visit = visit(patient);
        doctor = userRepository.findOneByLogin("admin").orElseThrow();
    }

    @AfterEach
    void cleanup() {
        admissionRepository
            .findAllWithToOneRelationships()
            .stream()
            .filter(admission -> admission.getVisit() != null && admission.getVisit().getPatient() != null)
            .filter(admission -> patient.getId().equals(admission.getVisit().getPatient().getId()))
            .forEach(admission -> {
                auditLogRepository
                    .findByEntityNameAndEntityIdOrderByIdAsc("Admission", String.valueOf(admission.getId()))
                    .forEach(auditLogRepository::delete);
                admissionRepository.deleteById(admission.getId());
            });

        List<Long> bedIds = List.of(freeBed.getId(), secondFreeBed.getId());
        bedIds.forEach(id -> {
            auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(id)).forEach(auditLogRepository::delete);
            bedRepository.deleteById(id);
        });

        visitRepository.findById(visit.getId()).ifPresent(found -> visitRepository.deleteById(found.getId()));
        patientRepository.deleteById(patient.getId());
        wardRepository.deleteById(openWard.getId());
        wardRepository.deleteById(closedWard.getId());
        bedTypeRepository.deleteById(bedType.getId());
        departmentRepository.deleteById(department.getId());
    }

    // ---------------------------------------------------------------- the ordinary path

    @Test
    void aPatientWaitingForABedIsGivenOneAndTheStayStarts() {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        AdmissionBedResultDTO result = admissionWorkflowService.assignBed(admission.getId(), assign(freeBed));

        assertThat(result.previousStatus()).isEqualTo(AdmissionStatus.PENDING_BED);
        assertThat(result.status()).isEqualTo(AdmissionStatus.ADMITTED);
        assertThat(result.bedId()).isEqualTo(freeBed.getId());
        assertThat(result.bedNumber()).isEqualTo(freeBed.getBedNumber());
        assertThat(result.wardId()).isEqualTo(openWard.getId());
        assertThat(result.patientId()).isEqualTo(patient.getId());
        assertThat(result.patientHospitalId()).isEqualTo(patient.getHospitalId());
        assertThat(result.assignedAt()).isNotNull();

        Admission reloaded = admissionRepository.findById(admission.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AdmissionStatus.ADMITTED);
        assertThat(reloaded.getBed().getId()).isEqualTo(freeBed.getId());

        // The other half of the change: the bed is not free any more, which is the only reason the next
        // patient cannot be given it.
        assertThat(bedRepository.findById(freeBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void theAssignmentIsRecordedAgainstBothTheBedAndTheStay() {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        admissionWorkflowService.assignBed(admission.getId(), assign(freeBed));

        List<AuditLog> bedTrail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "Bed",
            String.valueOf(freeBed.getId())
        );
        assertThat(bedTrail)
            .as("what the bed has been through")
            .anyMatch(entry ->
                AuditActions.BED_STATUS_CHANGED.equals(entry.getAction()) &&
                BedStatus.AVAILABLE.name().equals(entry.getOldValue()) &&
                BedStatus.OCCUPIED.name().equals(entry.getNewValue())
            );

        List<AuditLog> stayTrail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "Admission",
            String.valueOf(admission.getId())
        );
        assertThat(stayTrail)
            .as("when this patient arrived in a bed")
            .anyMatch(entry ->
                AuditActions.BED_ASSIGNED.equals(entry.getAction()) &&
                freeBed.getBedNumber().equals(entry.getNewValue()) &&
                "bed".equals(entry.getFieldName())
            );
        assertThat(stayTrail)
            .as("how the stay got where it is")
            .anyMatch(entry ->
                AuditActions.ADMISSION_STATUS_CHANGED.equals(entry.getAction()) &&
                AdmissionStatus.PENDING_BED.name().equals(entry.getOldValue()) &&
                AdmissionStatus.ADMITTED.name().equals(entry.getNewValue())
            );
    }

    // ---------------------------------------------------------------- beds that are not free

    @Test
    void aBedThatIsNotFreeIsRefused() {
        for (BedStatus occupied : List.of(BedStatus.OCCUPIED, BedStatus.CLEANING, BedStatus.MAINTENANCE)) {
            Bed unavailable = bed(openWard, "N" + occupied.name().charAt(0) + occupied.name().charAt(1), occupied);
            Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

            assertThatThrownBy(() -> admissionWorkflowService.assignBed(admission.getId(), assign(unavailable)))
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                    assertThat(ex.getErrorKey()).as("a %s bed", occupied).isEqualTo("bedNotAvailable")
                );

            assertThat(bedRepository.findById(unavailable.getId()).orElseThrow().getStatus()).isEqualTo(occupied);
            assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getStatus()).isEqualTo(AdmissionStatus.PENDING_BED);
            assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getBed()).isNull();

            auditLogRepository
                .findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(unavailable.getId()))
                .forEach(auditLogRepository::delete);
            admissionRepository.deleteById(admission.getId());
            bedRepository.deleteById(unavailable.getId());
        }
    }

    @Test
    void aBedInAWardThatIsNotTakingPatientsIsRefused() {
        Bed inClosedWard = bed(closedWard, "C1", BedStatus.AVAILABLE);
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        assertThatThrownBy(() -> admissionWorkflowService.assignBed(admission.getId(), assign(inClosedWard)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("bedInClosedWard")
            );

        assertThat(bedRepository.findById(inClosedWard.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
        bedRepository.deleteById(inClosedWard.getId());
    }

    // ---------------------------------------------------------------- one patient, one bed

    /** Moving a patient between beds is a transfer, which the specification requires to be recorded as one. */
    @Test
    void aPatientAlreadyInABedIsNotMovedByThisAction() {
        Bed occupiedBed = bed(openWard, "O1", BedStatus.OCCUPIED);
        Admission admission = admissionInBed(visit, occupiedBed);

        assertThatThrownBy(() -> admissionWorkflowService.assignBed(admission.getId(), assign(secondFreeBed)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("admissionAlreadyInABed")
            );

        assertThat(bedRepository.findById(secondFreeBed.getId()).orElseThrow().getStatus())
            .as("the bed it was refused was never taken")
            .isEqualTo(BedStatus.AVAILABLE);
        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getBed().getId()).isEqualTo(occupiedBed.getId());

        // The stay goes before the bed it is sitting in: the foreign key runs that way.
        admissionRepository.deleteById(admission.getId());
        bedRepository.deleteById(occupiedBed.getId());
    }

    /**
     * The rule with no database constraint behind it: the patient is reached through the visit, and a
     * partial unique index cannot join to visit.
     */
    @Test
    void onePatientCannotBeInTwoOpenStays() {
        Admission firstStay = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));
        Visit secondVisit = visit(patient);
        Admission secondStay = admissionAwaitingBed(secondVisit, Instant.parse("2026-09-28T09:00:00Z"));

        assertThatThrownBy(() -> admissionWorkflowService.assignBed(secondStay.getId(), assign(freeBed)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("patientAlreadyAdmitted")
            );

        assertThat(bedRepository.findById(freeBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
        assertThat(firstStay.getId()).isNotNull();
        admissionRepository.deleteById(secondStay.getId());
        visitRepository.deleteById(secondVisit.getId());
    }

    // ---------------------------------------------------------------- stays that cannot take a bed

    @Test
    void aDischargedStayCannotBeGivenABed() {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));
        admission.setStatus(AdmissionStatus.DISCHARGED);
        admission = admissionRepository.save(admission);

        Long admissionId = admission.getId();
        assertThatThrownBy(() -> admissionWorkflowService.assignBed(admissionId, assign(freeBed)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("admissionNotAwaitingBed")
            );

        assertThat(bedRepository.findById(freeBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void anUnknownStayOrBedIsRejected() {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));
        Long admissionId = admission.getId();

        assertThatThrownBy(() -> admissionWorkflowService.assignBed(-1L, assign(freeBed)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("admissionNotFound")
            );
        assertThatThrownBy(() -> admissionWorkflowService.assignBed(admissionId, assign(-1L)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedNotFound"));
        assertThatThrownBy(() -> admissionWorkflowService.assignBed(admissionId, new AssignBedRequestDTO()))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedRequired"));

        assertThat(bedRepository.findById(freeBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    // ---------------------------------------------------------------- the worklist

    @Test
    void theWorklistShowsWhoIsWaitingForABedLongestWaitFirst() {
        // One admission per visit, so each stay in this test needs its own visit.
        Visit secondVisit = visit(patient);
        Visit thirdVisit = visit(patient);
        Admission waitingSinceMorning = admissionAwaitingBed(visit, Instant.parse("2026-09-28T07:00:00Z"));
        Admission waitingSinceNoon = admissionAwaitingBed(secondVisit, Instant.parse("2026-09-28T12:00:00Z"));
        Bed occupiedBed = bed(openWard, "W1", BedStatus.OCCUPIED);
        Admission alreadyInABed = admissionInBed(thirdVisit, occupiedBed);

        List<AwaitingBedViewDTO> worklist = inpatientWorklistService
            .awaitingBed()
            .stream()
            .filter(row -> List.of(waitingSinceMorning.getId(), waitingSinceNoon.getId(), alreadyInABed.getId()).contains(row.admissionId()))
            .toList();

        assertThat(worklist)
            .as("only the stays still waiting, in the order the ward works through them")
            .extracting(AwaitingBedViewDTO::admissionId)
            .containsExactly(waitingSinceMorning.getId(), waitingSinceNoon.getId());

        AwaitingBedViewDTO first = worklist.getFirst();
        assertThat(first.patientHospitalId()).isEqualTo(patient.getHospitalId());
        assertThat(first.patientName()).isEqualTo(patient.getFullName());
        assertThat(first.patientSex()).isEqualTo(Sex.MALE);
        assertThat(first.admissionReason()).isEqualTo("Test stay awaiting a bed");
        assertThat(first.admittedAt()).isEqualTo(Instant.parse("2026-09-28T07:00:00Z"));
        assertThat(first.admittingDoctor()).isNotNull();

        admissionRepository.deleteById(alreadyInABed.getId());
        bedRepository.deleteById(occupiedBed.getId());
        admissionRepository.deleteById(waitingSinceNoon.getId());
        visitRepository.deleteById(secondVisit.getId());
        visitRepository.deleteById(thirdVisit.getId());
    }

    @Test
    void aPatientLeavesTheWorklistOnceTheyHaveABed() {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        admissionWorkflowService.assignBed(admission.getId(), assign(freeBed));

        assertThat(inpatientWorklistService.awaitingBed())
            .extracting(AwaitingBedViewDTO::admissionId)
            .doesNotContain(admission.getId());
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    void aNurseMayPutAPatientIntoABed() throws Exception {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        mockMvc
            .perform(
                put("/api/admissions/{admissionId}/bed", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"bedId\":" + freeBed.getId() + "}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.previousStatus").value("PENDING_BED"))
            .andExpect(jsonPath("$.status").value("ADMITTED"))
            .andExpect(jsonPath("$.bedNumber").value(freeBed.getBedNumber()));
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
    void aDoctorMayNotPutAPatientIntoABed() throws Exception {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        mockMvc
            .perform(
                put("/api/admissions/{admissionId}/bed", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"bedId\":" + freeBed.getId() + "}")
            )
            .andExpect(status().isForbidden());

        assertThat(bedRepository.findById(freeBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
    void theDeskMayNotPutAPatientIntoABed() throws Exception {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        mockMvc
            .perform(
                put("/api/admissions/{admissionId}/bed", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"bedId\":" + freeBed.getId() + "}")
            )
            .andExpect(status().isForbidden());
    }

    /**
     * The ward reads stays through the generated CRUD but cannot edit them through it: raw CRUD can set the
     * bed and the status by hand, which would make this whole action and its guards optional.
     */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void theWardMayReadStaysButNotEditThemThroughTheCrud() throws Exception {
        Admission admission = admissionAwaitingBed(visit, Instant.parse("2026-09-28T08:00:00Z"));

        mockMvc.perform(get("/api/admissions")).andExpect(status().isOk());
        mockMvc
            .perform(put("/api/admissions/{id}", admission.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void theWardMayReadTheWorklist() throws Exception {
        mockMvc.perform(get("/api/inpatient-worklist/awaiting-bed")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_PHARMACY")
    void thePharmacyMayNotReadTheWorklist() throws Exception {
        mockMvc.perform(get("/api/inpatient-worklist/awaiting-bed")).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- helpers

    private Ward ward(String name, boolean active) {
        Ward ward = new Ward();
        ward.setName(name);
        ward.setActive(active);
        ward.setDepartment(department);
        return wardRepository.save(ward);
    }

    private Bed bed(Ward ward, String number, BedStatus status) {
        Bed bed = new Bed();
        bed.setBedNumber(SUFFIX + "-" + number);
        bed.setStatus(status);
        bed.setWard(ward);
        bed.setBedType(bedType);
        return bedRepository.save(bed);
    }

    private Patient patient(String fullName, String hospitalId) {
        Patient record = new Patient();
        record.setHospitalId(hospitalId);
        record.setFullName(fullName);
        record.setSex(Sex.MALE);
        record.setSexEstimated(false);
        record.setRegistrationStatus(RegistrationStatus.COMPLETE);
        return patientRepository.save(record);
    }

    private Visit visit(Patient forPatient) {
        Visit visit = new Visit();
        visit.setPatient(forPatient);
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit.setPriority(VisitPriority.NORMAL);
        visit.setReasonForVisit("Bed assignment test");
        visit.setCreatedAt(Instant.now());
        return visitRepository.save(visit);
    }

    private Admission admissionAwaitingBed(Visit visit, Instant admittedAt) {
        return saveAdmission(visit, admittedAt, AdmissionStatus.PENDING_BED, null);
    }

    private Admission admissionInBed(Visit visit, Bed bed) {
        return saveAdmission(visit, Instant.parse("2026-09-28T06:00:00Z"), AdmissionStatus.ADMITTED, bed);
    }

    private Admission saveAdmission(Visit visit, Instant admittedAt, AdmissionStatus status, Bed bed) {
        Admission admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(admittedAt);
        admission.setAdmissionReason("Test stay awaiting a bed");
        admission.setStatus(status);
        admission.setAdmittingDoctor(doctor);
        admission.setPrimaryDoctor(doctor);
        admission.setBed(bed);
        return admissionRepository.save(admission);
    }

    private static AssignBedRequestDTO assign(Bed bed) {
        return assign(bed.getId());
    }

    private static AssignBedRequestDTO assign(Long bedId) {
        AssignBedRequestDTO request = new AssignBedRequestDTO();
        request.setBedId(bedId);
        return request;
    }
}
