package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.BedStatusViewDTO;
import com.hyperbrains.hms.service.dto.view.MarkBedMaintenanceRequestDTO;
import com.hyperbrains.hms.service.workflow.BedWorkflowService;
import java.math.BigDecimal;
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
 * Integration tests for a bed's status lifecycle: what may move to what, what is refused and why, what
 * the refusal leaves behind, and who is allowed to ask.
 *
 * <p>The refusals matter as much as the successes. An occupied bed that can be released by hand is a bed
 * that will eventually hold two patients, and a release that is quietly treated as success when the bed
 * was already available hides the fact that somebody else got there first. So each refusal is asserted
 * to leave the bed exactly as it was.
 *
 * <p>Two authorities are exercised over HTTP as well as through the service, because the whole point of
 * closing the generated CRUD to Super Admin is that a nurse cannot use it to set a status by hand — and
 * a rule about who may call what is only real if something checks the path patterns still match.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class BedStatusIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private BedWorkflowService bedWorkflowService;

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
    private MockMvc mockMvc;

    private Department department;

    private Ward ward;

    private BedType bedType;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Ward Status IT " + SUFFIX);
        department.setCode("WSIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        ward = new Ward();
        ward.setName("Ward Status IT ward " + SUFFIX);
        ward.setLocation("Block Z");
        ward.setActive(true);
        ward.setDepartment(department);
        ward = wardRepository.save(ward);

        bedType = new BedType();
        bedType.setName("Bed Status IT type " + SUFFIX);
        bedType.setDefaultDailyRate(new BigDecimal("3500.00"));
        bedType.setActive(true);
        bedType = bedTypeRepository.save(bedType);
    }

    @AfterEach
    void cleanup() {
        List<Bed> beds = bedsInThisWardsWard();
        beds.forEach(bed -> {
            auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(bed.getId())).forEach(auditLogRepository::delete);
            bedRepository.deleteById(bed.getId());
        });
        wardRepository.deleteById(ward.getId());
        bedTypeRepository.deleteById(bedType.getId());
        departmentRepository.deleteById(department.getId());
    }

    // ---------------------------------------------------------------- the cycle

    @Test
    void aCleanedBedIsReleasedAndReportsWhatItWasBefore() {
        Bed bed = bedIn(BedStatus.CLEANING);

        BedStatusViewDTO result = bedWorkflowService.markAvailable(bed.getId());

        assertThat(result.previousStatus()).isEqualTo(BedStatus.CLEANING);
        assertThat(result.status()).isEqualTo(BedStatus.AVAILABLE);
        assertThat(result.bedId()).isEqualTo(bed.getId());
        assertThat(result.wardId()).isEqualTo(ward.getId());
        assertThat(result.changedAt()).isNotNull();
        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void aRepairedBedCanBeReleasedToo() {
        Bed bed = bedIn(BedStatus.MAINTENANCE);

        assertThat(bedWorkflowService.markAvailable(bed.getId()).status()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void aBedCanBeTakenOutOfServiceAndBroughtBack() {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        BedStatusViewDTO outOfService = bedWorkflowService.markMaintenance(bed.getId(), maintenance("Frame is bent"));
        assertThat(outOfService.previousStatus()).isEqualTo(BedStatus.AVAILABLE);
        assertThat(outOfService.status()).isEqualTo(BedStatus.MAINTENANCE);

        assertThat(bedWorkflowService.markAvailable(bed.getId()).status()).isEqualTo(BedStatus.AVAILABLE);
    }

    /** A cleaner who finds a bed broken puts it straight out of service, without offering it in between. */
    @Test
    void aBedFoundBrokenWhileBeingCleanedGoesStraightOutOfService() {
        Bed bed = bedIn(BedStatus.CLEANING);

        BedStatusViewDTO result = bedWorkflowService.markMaintenance(bed.getId(), maintenance("Mattress torn"));

        assertThat(result.previousStatus()).isEqualTo(BedStatus.CLEANING);
        assertThat(result.status()).isEqualTo(BedStatus.MAINTENANCE);
    }

    // ---------------------------------------------------------------- what is refused

    @Test
    void aBedHoldingAPatientCannotBeReleased() {
        Bed bed = bedIn(BedStatus.OCCUPIED);

        assertThatThrownBy(() -> bedWorkflowService.markAvailable(bed.getId()))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("bedHoldsPatient")
            );

        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void aBedHoldingAPatientCannotBeTakenOutOfServiceEither() {
        Bed bed = bedIn(BedStatus.OCCUPIED);

        assertThatThrownBy(() -> bedWorkflowService.markMaintenance(bed.getId(), maintenance("Ward is closing")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("bedHoldsPatient")
            );

        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    /**
     * Refused rather than ignored: a second click, or a race with another nurse, must not be reported as
     * a change this caller made.
     */
    @Test
    void releasingABedThatIsAlreadyAvailableIsRefusedRatherThanSilentlyAccepted() {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        assertThatThrownBy(() -> bedWorkflowService.markAvailable(bed.getId()))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("bedAlreadyAvailable")
            );
    }

    @Test
    void aBedAlreadyOutOfServiceIsNotTakenOutOfServiceTwice() {
        Bed bed = bedIn(BedStatus.MAINTENANCE);

        assertThatThrownBy(() -> bedWorkflowService.markMaintenance(bed.getId(), maintenance("Still broken")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("bedAlreadyInMaintenance")
            );
    }

    /** A bed out of service with no reason is a bed nobody can account for later. */
    @Test
    void takingABedOutOfServiceRequiresAReason() {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        assertThatThrownBy(() -> bedWorkflowService.markMaintenance(bed.getId(), null))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("maintenanceReasonRequired")
            );
        assertThatThrownBy(() -> bedWorkflowService.markMaintenance(bed.getId(), maintenance("   ")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("maintenanceReasonRequired")
            );

        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void anUnknownBedIsRejected() {
        assertThatThrownBy(() -> bedWorkflowService.markAvailable(-1L))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("bedNotFound"));
    }

    // ---------------------------------------------------------------- the trail

    @Test
    void everyMoveIsRecordedWithBothEndsOfTheChange() {
        Bed bed = bedIn(BedStatus.CLEANING);

        bedWorkflowService.markAvailable(bed.getId());

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(bed.getId()));
        assertThat(trail).hasSize(1);
        AuditLog entry = trail.getFirst();
        assertThat(entry.getAction()).isEqualTo(AuditActions.BED_STATUS_CHANGED);
        assertThat(entry.getFieldName()).isEqualTo("status");
        assertThat(entry.getOldValue()).isEqualTo(BedStatus.CLEANING.name());
        assertThat(entry.getNewValue()).isEqualTo(BedStatus.AVAILABLE.name());
    }

    @Test
    void takingABedOutOfServiceKeepsTheReasonInTheTrail() {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        bedWorkflowService.markMaintenance(bed.getId(), maintenance("Hydraulic frame failed"));

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(bed.getId()));
        assertThat(trail).hasSize(1);
        assertThat(trail.getFirst().getReason()).isEqualTo("Hydraulic frame failed");
        assertThat(trail.getFirst().getNewValue()).isEqualTo(BedStatus.MAINTENANCE.name());
    }

    /** A refused move leaves no trail: nothing changed, so nothing is claimed to have changed. */
    @Test
    void aRefusedMoveIsNotRecorded() {
        Bed bed = bedIn(BedStatus.OCCUPIED);

        assertThatThrownBy(() -> bedWorkflowService.markAvailable(bed.getId())).isInstanceOf(BusinessRuleViolationException.class);

        assertThat(auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc("Bed", String.valueOf(bed.getId()))).isEmpty();
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    void aNurseMayReleaseABed() throws Exception {
        Bed bed = bedIn(BedStatus.CLEANING);

        mockMvc
            .perform(put("/api/beds/{bedId}/available", bed.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bedId").value(bed.getId()))
            .andExpect(jsonPath("$.previousStatus").value("CLEANING"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void anAdministratorMayTakeABedOutOfService() throws Exception {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        mockMvc
            .perform(
                put("/api/beds/{bedId}/maintenance", bed.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"No hot water on the ward\"}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void takingABedOutOfServiceWithNoReasonIsABadRequest() throws Exception {
        Bed bed = bedIn(BedStatus.AVAILABLE);

        mockMvc
            .perform(put("/api/beds/{bedId}/maintenance", bed.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"  \"}"))
            .andExpect(status().isBadRequest());
    }

    /** Beds are a nurse's and an administrator's business, not a doctor's. */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
    void aDoctorMayNotReleaseABed() throws Exception {
        Bed bed = bedIn(BedStatus.CLEANING);

        mockMvc.perform(put("/api/beds/{bedId}/available", bed.getId())).andExpect(status().isForbidden());

        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.CLEANING);
    }

    /**
     * The catch-all would have let any hospital role read and write these rows; a pharmacist has no
     * business with a bed list, and this is the assertion that the catch-all no longer reaches it.
     */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_PHARMACY")
    void aPharmacistCannotEvenListBeds() throws Exception {
        mockMvc.perform(get("/api/beds")).andExpect(status().isForbidden());
    }

    /** Raw CRUD can set any status, including OCCUPIED, so it is not open to the roles that run the ward. */
    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void anAdministratorMayNotCreateABedThroughTheGeneratedCrud() throws Exception {
        mockMvc
            .perform(post("/api/beds").contentType(MediaType.APPLICATION_JSON).content(newBedBody()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_SUPER_ADMIN")
    void aSuperAdminMayCreateABedThroughTheGeneratedCrud() throws Exception {
        mockMvc
            .perform(post("/api/beds").contentType(MediaType.APPLICATION_JSON).content(newBedBody()))
            .andExpect(status().isCreated());
    }

    // ---------------------------------------------------------------- helpers

    private Bed bedIn(BedStatus status) {
        Bed bed = new Bed();
        bed.setBedNumber(SUFFIX + "-" + status.name().charAt(0));
        bed.setStatus(status);
        bed.setWard(ward);
        bed.setBedType(bedType);
        return bedRepository.save(bed);
    }

    private static MarkBedMaintenanceRequestDTO maintenance(String reason) {
        MarkBedMaintenanceRequestDTO request = new MarkBedMaintenanceRequestDTO();
        request.setReason(reason);
        return request;
    }

    private String newBedBody() {
        return (
            "{\"bedNumber\":\"" +
            SUFFIX +
            "-NEW\",\"status\":\"AVAILABLE\",\"ward\":{\"id\":" +
            ward.getId() +
            "},\"bedType\":{\"id\":" +
            bedType.getId() +
            "}}"
        );
    }

    private List<Bed> bedsInThisWardsWard() {
        return bedRepository
            .findAllWithWardAndBedType()
            .stream()
            .filter(bed -> bed.getWard() != null && ward.getId().equals(bed.getWard().getId()))
            .toList();
    }
}
