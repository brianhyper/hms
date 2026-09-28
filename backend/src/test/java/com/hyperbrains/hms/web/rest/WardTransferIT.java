package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AdmissionTransfer;
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
import com.hyperbrains.hms.repository.AdmissionTransferRepository;
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
import com.hyperbrains.hms.service.dto.view.TransferBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.WardTransferResultDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
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
 * Integration tests for moving a patient from one bed to another, with a reason.
 *
 * <p>The case this exists for is the ordinary one: an ICU patient stepped down to an ordinary ward. That is
 * a change of ward, which changes which staff are responsible for the patient, which is why the reason is
 * mandatory and why the move is recorded as an event rather than as an edit.
 *
 * <p>Two properties are asserted here that nothing else in the codebase asserts, and both would be silent if
 * they broke: the bed being left goes to {@code CLEANING} rather than straight back to {@code AVAILABLE},
 * and the ward a doctor is judged against follows the bed rather than being cached anywhere.
 *
 * <p>Every refusal is asserted against the database afterwards. A move changes three rows — two beds and the
 * admission — and a refusal that changed any of them would leave a bed occupied with nobody in it.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class WardTransferIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private AdmissionWorkflowService admissionWorkflowService;

    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private AdmissionTransferRepository admissionTransferRepository;

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

    private Ward intensiveCare;

    private Ward ordinaryWard;

    private Ward closedWard;

    private BedType intensiveCareType;

    private BedType generalType;

    private Bed icuBed;

    private Bed ordinaryBed;

    private Bed spareOrdinaryBed;

    private Bed closedWardBed;

    private Patient patient;

    private Visit visit;

    private Admission admission;

    private User nurse;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Ward Transfer IT " + SUFFIX);
        department.setCode("WTIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        intensiveCare = ward("Intensive Care " + SUFFIX, true);
        ordinaryWard = ward("Ordinary Ward " + SUFFIX, true);
        closedWard = ward("Closed Ward " + SUFFIX, false);

        intensiveCareType = bedType("ICU " + SUFFIX, new BigDecimal("15000.00"));
        generalType = bedType("GENERAL " + SUFFIX, new BigDecimal("3500.00"));

        icuBed = bed(intensiveCare, intensiveCareType, "ICU1", BedStatus.OCCUPIED);
        ordinaryBed = bed(ordinaryWard, generalType, "G1", BedStatus.AVAILABLE);
        spareOrdinaryBed = bed(ordinaryWard, generalType, "G2", BedStatus.AVAILABLE);
        closedWardBed = bed(closedWard, generalType, "C1", BedStatus.AVAILABLE);

        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Ward Transfer Patient " + SUFFIX);
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        visit = visit(patient);

        admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(Instant.parse("2026-09-27T22:00:00Z"));
        admission.setAdmissionReason("Ventilated overnight");
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setBed(icuBed);

        nurse = userRepository.findOneByLogin("admin").orElseThrow();
        admission.setAdmittingDoctor(nurse);
        admission.setPrimaryDoctor(nurse);
        admission = admissionRepository.save(admission);
    }

    @AfterEach
    void cleanup() {
        admissionTransferRepository
            .findByAdmissionIdOrderByTransferredAtAscIdAsc(admission.getId())
            .forEach(transfer -> admissionTransferRepository.deleteById(transfer.getId()));
        admissionRepository.deleteById(admission.getId());
        auditLogRepository
            .findByEntityNameAndEntityIdOrderByIdAsc("Admission", String.valueOf(admission.getId()))
            .forEach(auditLogRepository::delete);

        List.of(icuBed, ordinaryBed, spareOrdinaryBed, closedWardBed).forEach(bed -> {
            auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(bed.getId())).forEach(auditLogRepository::delete);
            bedRepository.deleteById(bed.getId());
        });

        visitRepository.deleteById(visit.getId());
        patientRepository.deleteById(patient.getId());
        List.of(intensiveCare, ordinaryWard, closedWard).forEach(ward -> wardRepository.deleteById(ward.getId()));
        List.of(intensiveCareType, generalType).forEach(type -> bedTypeRepository.deleteById(type.getId()));
        departmentRepository.deleteById(department.getId());
    }

    // ---------------------------------------------------------------- the ordinary path

    @Test
    void aPatientIsSteppedDownFromIntensiveCareToAnOrdinaryWard() {
        WardTransferResultDTO result = admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "Stepped down, stable on room air"));

        assertThat(result.admissionId()).isEqualTo(admission.getId());
        assertThat(result.fromBedId()).isEqualTo(icuBed.getId());
        assertThat(result.fromBedNumber()).isEqualTo(icuBed.getBedNumber());
        assertThat(result.fromWardId()).isEqualTo(intensiveCare.getId());
        assertThat(result.fromWardName()).isEqualTo(intensiveCare.getName());
        assertThat(result.toBedId()).isEqualTo(ordinaryBed.getId());
        assertThat(result.toWardId()).isEqualTo(ordinaryWard.getId());
        assertThat(result.toWardName()).isEqualTo(ordinaryWard.getName());
        assertThat(result.reason()).isEqualTo("Stepped down, stable on room air");
        assertThat(result.transferId()).isNotNull();
        assertThat(result.patientHospitalId()).isEqualTo(patient.getHospitalId());

        // The bed left behind is not free: it has just been occupied, so somebody has to say it is clean.
        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.CLEANING);
        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);

        Admission reloaded = admissionRepository.findById(admission.getId()).orElseThrow();
        assertThat(reloaded.getBed().getId()).isEqualTo(ordinaryBed.getId());
        assertThat(reloaded.getStatus()).as("a move does not end the stay").isEqualTo(AdmissionStatus.ADMITTED);
    }

    /**
     * The current ward is the bed's ward, read live. Nothing caches it, so it is already correct after the
     * move — which is what the inpatient access rule ("a doctor sees admissions on a ward they cover") reads.
     */
    @Test
    void theWardThePatientIsJudgedAgainstFollowsTheBed() {
        assertThat(admissionRepository.findOneWithBedAndWard(admission.getId()).orElseThrow().getBed().getWard().getId())
            .isEqualTo(intensiveCare.getId());

        admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "Stepped down"));

        assertThat(admissionRepository.findOneWithBedAndWard(admission.getId()).orElseThrow().getBed().getWard().getId())
            .isEqualTo(ordinaryWard.getId());
    }

    @Test
    void theMoveIsRecordedOnceInThePatientsLocationHistory() {
        WardTransferResultDTO result = admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "Stepped down"));

        List<AdmissionTransfer> history = admissionTransferRepository.findByAdmissionIdOrderByTransferredAtAscIdAsc(admission.getId());
        assertThat(history).hasSize(1);
        AdmissionTransfer move = history.getFirst();
        assertThat(move.getId()).isEqualTo(result.transferId());
        assertThat(move.getFromBed().getId()).isEqualTo(icuBed.getId());
        assertThat(move.getToBed().getId()).isEqualTo(ordinaryBed.getId());
        assertThat(move.getReason()).isEqualTo("Stepped down");
        assertThat(move.getTransferredBy().getId()).isEqualTo(nurse.getId());
        assertThat(move.getTransferredAt())
            .as("the recorded moment is the one the caller was told, to the precision the column stores")
            .isBetween(result.transferredAt().minusSeconds(1), result.transferredAt().plusSeconds(1));
    }

    @Test
    void theMoveIsRecordedInTheAuditTrailWithItsReason() {
        admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "Stepped down, stable on room air"));

        List<AuditLog> stayTrail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "Admission",
            String.valueOf(admission.getId())
        );
        assertThat(stayTrail)
            .as("the move, with the reason it was made for")
            .anyMatch(entry ->
                AuditActions.WARD_TRANSFERRED.equals(entry.getAction()) &&
                "Stepped down, stable on room air".equals(entry.getReason()) &&
                "bed".equals(entry.getFieldName())
            );

        assertThat(auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(icuBed.getId())))
            .as("the bed that was left")
            .anyMatch(entry ->
                AuditActions.BED_STATUS_CHANGED.equals(entry.getAction()) &&
                BedStatus.OCCUPIED.name().equals(entry.getOldValue()) &&
                BedStatus.CLEANING.name().equals(entry.getNewValue())
            );
        assertThat(auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(ordinaryBed.getId())))
            .as("the bed that was taken")
            .anyMatch(entry ->
                AuditActions.BED_STATUS_CHANGED.equals(entry.getAction()) &&
                BedStatus.AVAILABLE.name().equals(entry.getOldValue()) &&
                BedStatus.OCCUPIED.name().equals(entry.getNewValue())
            );
    }

    @Test
    void aSecondMoveIsRecordedTooAndTheHistoryKeepsItsOrder() {
        admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "Stepped down"));
        admissionWorkflowService.transfer(admission.getId(), transfer(spareOrdinaryBed, "Closer to the nurses' station"));

        List<AdmissionTransfer> history = admissionTransferRepository.findByAdmissionIdOrderByTransferredAtAscIdAsc(admission.getId());
        assertThat(history)
            .as("where the patient has been, in the order it happened")
            .extracting(move -> move.getToBed().getBedNumber())
            .containsExactly(ordinaryBed.getBedNumber(), spareOrdinaryBed.getBedNumber());
        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.CLEANING);
    }

    // ---------------------------------------------------------------- what the action refuses

    @Test
    void aMoveWithoutAReasonIsRefused() {
        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, null)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("transferReasonRequired")
            );
        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(ordinaryBed, "   ")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("transferReasonRequired")
            );

        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getBed().getId()).isEqualTo(icuBed.getId());
        assertThat(admissionTransferRepository.findByAdmissionIdOrderByTransferredAtAscIdAsc(admission.getId())).isEmpty();
    }

    @Test
    void aBedThatIsNotFreeCannotBeMovedInto() {
        Bed beingCleaned = bed(ordinaryWard, generalType, "G3", BedStatus.CLEANING);

        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(beingCleaned, "Needs a quieter bed")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedNotAvailable"));

        assertThat(bedRepository.findById(beingCleaned.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.CLEANING);
        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getBed().getId()).isEqualTo(icuBed.getId());

        bedRepository.deleteById(beingCleaned.getId());
    }

    @Test
    void aBedInAClosedWardCannotBeMovedInto() {
        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(closedWardBed, "Ward is quieter")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedInClosedWard"));

        assertThat(bedRepository.findById(closedWardBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void movingAPatientIntoTheBedTheyAreAlreadyInIsRefused() {
        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(icuBed, "No particular reason")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedUnchanged"));

        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus())
            .as("a refused no-op move must not send the bed for cleaning either")
            .isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void aStayThatIsStillWaitingForABedIsNotMoved() {
        Visit secondVisit = visit(patient);
        Admission waiting = admissionWithoutABed(secondVisit);
        Long waitingId = waiting.getId();

        assertThatThrownBy(() -> admissionWorkflowService.transfer(waitingId, transfer(ordinaryBed, "Up to the ward")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("admissionNotInABed"));

        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);

        admissionRepository.deleteById(waitingId);
        visitRepository.deleteById(secondVisit.getId());
    }

    @Test
    void aDischargedStayCannotBeMoved() {
        Admission ended = admissionRepository.findById(admission.getId()).orElseThrow();
        ended.setStatus(AdmissionStatus.DISCHARGED);
        ended = admissionRepository.save(ended);

        Long endedId = ended.getId();
        assertThatThrownBy(() -> admissionWorkflowService.transfer(endedId, transfer(ordinaryBed, "Still unwell")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("admissionNotOpen"));

        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void anUnknownStayOrBedIsRejected() {
        assertThatThrownBy(() -> admissionWorkflowService.transfer(-1L, transfer(ordinaryBed, "Nowhere")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("admissionNotFound"));
        assertThatThrownBy(() -> admissionWorkflowService.transfer(admission.getId(), transfer(-1L, "Nowhere")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedNotFound"));

        assertThat(bedRepository.findById(ordinaryBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    void aNurseMayMoveAPatient() throws Exception {
        mockMvc
            .perform(
                post("/api/admissions/{admissionId}/transfers", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferBody(ordinaryBed, "Stepped down, stable on room air"))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fromBedNumber").value(icuBed.getBedNumber()))
            .andExpect(jsonPath("$.toBedNumber").value(ordinaryBed.getBedNumber()))
            .andExpect(jsonPath("$.toWardName").value(ordinaryWard.getName()))
            .andExpect(jsonPath("$.reason").value("Stepped down, stable on room air"));
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void anAdministratorMayMoveAPatient() throws Exception {
        mockMvc
            .perform(
                post("/api/admissions/{admissionId}/transfers", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferBody(ordinaryBed, "Ward requires the ICU bed"))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
    void aDoctorMayNotMoveAPatient() throws Exception {
        mockMvc
            .perform(
                post("/api/admissions/{admissionId}/transfers", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferBody(ordinaryBed, "I would prefer this"))
            )
            .andExpect(status().isForbidden());

        assertThat(bedRepository.findById(icuBed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
    void theDeskMayNotMoveAPatient() throws Exception {
        mockMvc
            .perform(
                post("/api/admissions/{admissionId}/transfers", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferBody(ordinaryBed, "Front desk says so"))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void aMoveWithNoReasonInTheBodyIsABadRequest() throws Exception {
        mockMvc
            .perform(
                post("/api/admissions/{admissionId}/transfers", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"toBedId\":" + ordinaryBed.getId() + ",\"reason\":\"  \"}")
            )
            .andExpect(status().isBadRequest());
    }

    /** The history is append-only, so raw CRUD cannot rewrite a move that already happened. */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void theWardMayReadTheMoveHistoryButNotRewriteIt() throws Exception {
        mockMvc.perform(get("/api/admission-transfers")).andExpect(status().isOk());
        mockMvc
            .perform(put("/api/admission-transfers/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- helpers

    private Ward ward(String name, boolean active) {
        Ward ward = new Ward();
        ward.setName(name);
        ward.setActive(active);
        ward.setDepartment(department);
        return wardRepository.save(ward);
    }

    private BedType bedType(String name, BigDecimal rate) {
        BedType type = new BedType();
        type.setName(name);
        type.setDefaultDailyRate(rate);
        type.setActive(true);
        return bedTypeRepository.save(type);
    }

    private Bed bed(Ward ward, BedType type, String number, BedStatus status) {
        Bed bed = new Bed();
        bed.setBedNumber(SUFFIX + "-" + number);
        bed.setStatus(status);
        bed.setWard(ward);
        bed.setBedType(type);
        return bedRepository.save(bed);
    }

    private Visit visit(Patient forPatient) {
        Visit visit = new Visit();
        visit.setPatient(forPatient);
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit.setPriority(VisitPriority.NORMAL);
        visit.setReasonForVisit("Ward transfer test");
        visit.setCreatedAt(Instant.now());
        return visitRepository.save(visit);
    }

    /** A second stay still waiting for a bed, for the "nothing to move from" refusal. */
    private Admission admissionWithoutABed(Visit forVisit) {
        Admission waiting = new Admission();
        waiting.setVisit(forVisit);
        waiting.setAdmittedAt(Instant.parse("2026-09-28T06:00:00Z"));
        waiting.setAdmissionReason("Awaiting a bed");
        waiting.setStatus(AdmissionStatus.PENDING_BED);
        waiting.setAdmittingDoctor(nurse);
        waiting.setPrimaryDoctor(nurse);
        return admissionRepository.save(waiting);
    }

    private static TransferBedRequestDTO transfer(Bed toBed, String reason) {
        return transfer(toBed.getId(), reason);
    }

    private static TransferBedRequestDTO transfer(Long toBedId, String reason) {
        TransferBedRequestDTO request = new TransferBedRequestDTO();
        request.setToBedId(toBedId);
        request.setReason(reason);
        return request;
    }

    private static String transferBody(Bed toBed, String reason) {
        return "{\"toBedId\":" + toBed.getId() + ",\"reason\":\"" + reason + "\"}";
    }
}
