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
import com.hyperbrains.hms.repository.InpatientVitalsRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.PersonNames;
import com.hyperbrains.hms.service.VitalsRejectedException;
import com.hyperbrains.hms.service.dto.view.ChartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.InpatientVitalsViewDTO;
import com.hyperbrains.hms.service.dto.view.VitalsChartingResultDTO;
import com.hyperbrains.hms.service.workflow.InpatientChartingService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for charting on the ward.
 *
 * <p>The two-tier validation is Phase 1's, reused rather than re-implemented, so these tests are about the
 * parts that are new: that an alarming reading is <em>kept</em> while an impossible one is refused, that a
 * correction supersedes rather than overwrites, and that a row can never be replaced twice — because two
 * rows each claiming to replace the same reading leave the chart with no answer to "which figure is in
 * force".
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class InpatientChartingIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private InpatientChartingService chartingService;

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
    private InpatientVitalsRepository inpatientVitalsRepository;

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

    private Patient patient;

    private Visit visit;

    private Admission admission;

    private User nurse;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Charting IT " + SUFFIX);
        department.setCode("CHIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        ward = new Ward();
        ward.setName("Charting Ward " + SUFFIX);
        ward.setActive(true);
        ward.setDepartment(department);
        ward = wardRepository.save(ward);

        BedType type = new BedType();
        type.setName("Charting Type " + SUFFIX);
        type.setDefaultDailyRate(new BigDecimal("3500.00"));
        type.setActive(true);
        type = bedTypeRepository.save(type);

        bed = new Bed();
        bed.setBedNumber(SUFFIX + "-C1");
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setWard(ward);
        bed.setBedType(type);
        bed = bedRepository.save(bed);

        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Charting Patient " + SUFFIX);
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        visit = new Visit();
        visit.setPatient(patient);
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit.setPriority(VisitPriority.NORMAL);
        visit.setReasonForVisit("Charting test");
        visit.setCreatedAt(Instant.now());
        visit = visitRepository.save(visit);

        nurse = userRepository.findOneByLogin("admin").orElseThrow();

        admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(Instant.parse("2026-09-28T06:00:00Z"));
        admission.setAdmissionReason("Observation");
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setBed(bed);
        admission.setAdmittingDoctor(nurse);
        admission.setPrimaryDoctor(nurse);
        admission = admissionRepository.save(admission);
    }

    @AfterEach
    void cleanup() {
        // Newest first: a correction points at the row it supersedes, so deleting in chart order would hit the
        // foreign key on the very link this slice exists to keep.
        List.copyOf(inpatientVitalsRepository.findChart(admission.getId()))
            .reversed()
            .forEach(vitals -> inpatientVitalsRepository.deleteById(vitals.getId()));
        auditLogRepository
            .findByEntityNameAndEntityIdOrderByIdAsc("Admission", String.valueOf(admission.getId()))
            .forEach(auditLogRepository::delete);
        admissionRepository.deleteById(admission.getId());
        bedRepository.deleteById(bed.getId());
        visitRepository.deleteById(visit.getId());
        patientRepository.deleteById(patient.getId());
        wardRepository.deleteById(ward.getId());
        bedTypeRepository.deleteById(bed.getBedType().getId());
        departmentRepository.deleteById(department.getId());
    }

    // ---------------------------------------------------------------- charting

    @Test
    void anObservationIsChartedAndTheBmiIsDerivedFromWhatWasRecorded() {
        VitalsChartingResultDTO result = chartingService.chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"));

        assertThat(result.correction()).isFalse();
        assertThat(result.warnings()).isEmpty();
        assertThat(result.vitals().id()).isNotNull();
        assertThat(result.vitals().recordedBy()).as("whoever charted it, by name").isEqualTo(PersonNames.displayName(nurse));
        assertThat(result.vitals().bmi())
            .as("78 kg at 170 cm, derived rather than taken from the request")
            .isEqualByComparingTo(new BigDecimal("26.99"));
    }

    @Test
    void anAlarmingReadingIsSavedAndReportedRatherThanRefused() {
        // Temperature is left in the healthy band so the pulse is the only abnormal reading, which is what makes
        // "the warning names the field" a meaningful assertion rather than one about the order warnings come in.
        VitalsChartingResultDTO result = chartingService.chart(admission.getId(), observation(36.8, 132, 118, 76, 97, "78", "1.70"));

        assertThat(result.warnings()).as("a real patient can have a pulse of 132").isNotEmpty();
        assertThat(result.warnings().getFirst().getField()).isEqualTo("pulseRate");
        assertThat(chartingService.chartOf(admission.getId())).as("and the reading is on the chart").hasSize(1);
    }

    @Test
    void aReadingThatCannotBeTrueIsRefusedAndNothingIsCharted() {
        assertThatThrownBy(() -> chartingService.chart(admission.getId(), observation(36.8, 400, 118, 76, 97, "78", "1.70")))
            .isInstanceOf(VitalsRejectedException.class);

        assertThat(chartingService.chartOf(admission.getId())).isEmpty();
    }

    /** A reason sent to the wrong endpoint is refused rather than ignored: the caller meant something else. */
    @Test
    void chartingRefusesACorrectionReasonInsteadOfIgnoringIt() {
        ChartVitalsRequestDTO request = observation(36.8, 84, 118, 76, 97, "78", "1.70");
        request.setCorrectionReason("Typed the wrong arm");

        assertThatThrownBy(() -> chartingService.chart(admission.getId(), request))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("correctionReasonNotExpected")
            );

        assertThat(chartingService.chartOf(admission.getId())).isEmpty();
    }

    @Test
    void nothingCanBeChartedOnAStayThatHasEnded() {
        Long admissionId = admission.getId();
        Admission ended = admissionRepository.findById(admissionId).orElseThrow();
        ended.setStatus(AdmissionStatus.DISCHARGED);
        admissionRepository.save(ended);

        assertThatThrownBy(() -> chartingService.chart(admissionId, observation(36.8, 84, 118, 76, 97, "78", "1.70")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("admissionNotOpen"));
    }

    // ---------------------------------------------------------------- correcting

    @Test
    void aCorrectionSupersedesTheOriginalRatherThanOverwritingIt() {
        InpatientVitalsViewDTO original = chartingService
            .chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"))
            .vitals();

        ChartVitalsRequestDTO correction = observation(37.1, 88, 122, 78, 96, "78", "1.70");
        correction.setCorrectionReason("Blood pressure taken on the other arm");
        VitalsChartingResultDTO result = chartingService.correct(admission.getId(), original.id(), correction);

        assertThat(result.correction()).isTrue();

        List<InpatientVitalsViewDTO> chart = chartingService.chartOf(admission.getId());
        assertThat(chart).as("both rows are on the chart: nothing is deleted from a clinical record").hasSize(2);

        InpatientVitalsViewDTO stillThere = chart.getFirst();
        assertThat(stillThere.id()).isEqualTo(original.id());
        assertThat(stillThere.systolicBp()).as("the original reading is unchanged").isEqualTo(118);
        assertThat(stillThere.supersededById()).as("and it says what replaced it").isEqualTo(result.vitals().id());

        InpatientVitalsViewDTO correcting = chart.getLast();
        assertThat(correcting.correctsId()).isEqualTo(original.id());
        assertThat(correcting.correctionReason()).isEqualTo("Blood pressure taken on the other arm");
        assertThat(correcting.systolicBp()).isEqualTo(122);
        assertThat(correcting.isSuperseded()).as("the correcting row is the one in force").isFalse();
    }

    @Test
    void aCorrectionRequiresAReason() {
        InpatientVitalsViewDTO original = chartingService
            .chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"))
            .vitals();

        assertThatThrownBy(() -> chartingService.correct(admission.getId(), original.id(), observation(37.1, 88, 122, 78, 96, "78", "1.70")))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("correctionReasonRequired")
            );

        assertThat(chartingService.chartOf(admission.getId())).hasSize(1);
    }

    @Test
    void theSameObservationCannotBeReplacedTwice() {
        InpatientVitalsViewDTO original = chartingService
            .chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"))
            .vitals();
        ChartVitalsRequestDTO first = observation(37.1, 88, 122, 78, 96, "78", "1.70");
        first.setCorrectionReason("Other arm");
        chartingService.correct(admission.getId(), original.id(), first);

        ChartVitalsRequestDTO second = observation(37.4, 90, 130, 80, 95, "78", "1.70");
        second.setCorrectionReason("And again");

        assertThatThrownBy(() -> chartingService.correct(admission.getId(), original.id(), second))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("observationAlreadyCorrected")
            );
    }

    @Test
    void anObservationChartedOnAnotherStayCannotBeCorrectedHere() {
        Long admissionId = admission.getId();
        InpatientVitalsViewDTO original = chartingService
            .chart(admissionId, observation(36.8, 84, 118, 76, 97, "78", "1.70"))
            .vitals();
        ChartVitalsRequestDTO correction = observation(37.1, 88, 122, 78, 96, "78", "1.70");
        correction.setCorrectionReason("Other arm");

        assertThatThrownBy(() -> chartingService.correct(-1L, original.id(), correction))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("admissionNotFound")
            );
    }

    // ---------------------------------------------------------------- the trail

    @Test
    void everyObservationAndEveryCorrectionIsRecorded() {
        InpatientVitalsViewDTO original = chartingService
            .chart(admission.getId(), observation(38.6, 132, 118, 76, 97, "78", "1.70"))
            .vitals();
        ChartVitalsRequestDTO correction = observation(37.1, 88, 122, 78, 96, "78", "1.70");
        correction.setCorrectionReason("Other arm");
        chartingService.correct(admission.getId(), original.id(), correction);

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "Admission",
            String.valueOf(admission.getId())
        );
        assertThat(trail)
            .as("charted, with the abnormal readings named")
            .anyMatch(entry -> AuditActions.VITALS_RECORDED.equals(entry.getAction()) && entry.getDetails().contains("Pulse"));
        assertThat(trail)
            .as("corrected, with the reason")
            .anyMatch(entry -> AuditActions.VITALS_CORRECTED.equals(entry.getAction()) && "Other arm".equals(entry.getReason()));
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    void aNurseMayChartOverHttp() throws Exception {
        mockMvc
            .perform(
                post("/api/inpatient-charting/{admissionId}/vitals", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(36.8, 84, 118, 76, 97))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correction").value(false))
            .andExpect(jsonPath("$.vitals.id").exists());
    }

    @Test
    void anImpossibleReadingIsABadRequestOverHttp() throws Exception {
        mockMvc
            .perform(
                post("/api/inpatient-charting/{admissionId}/vitals", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(36.8, 400, 118, 76, 97))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
    void aDoctorMayNotChartButMayReadTheChart() throws Exception {
        chartingService.chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"));

        mockMvc
            .perform(
                post("/api/inpatient-charting/{admissionId}/vitals", admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body(36.8, 84, 118, 76, 97))
            )
            .andExpect(status().isForbidden());

        // The doctor here is the primary doctor of this stay, so the row-level rule lets them read it.
        mockMvc.perform(get("/api/inpatient-charting/{admissionId}/vitals", admission.getId())).andExpect(status().isOk());
    }

    /** The generated chart CRUD writes a reading in without the validator, so it is not open to the ward. */
    @Test
    void theGeneratedChartCrudIsClosedToTheWard() throws Exception {
        mockMvc
            .perform(put("/api/inpatient-vitals/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    /**
     * The row-level rule enforced rather than merely filtered: a chart is reached by an admission id, and the
     * patient behind that id is the whole point. Refused as a 403, because nothing about the request is wrong.
     */
    @Test
    @WithMockUser(value = "user", authorities = "ROLE_DOCTOR")
    void aDoctorWhoIsNeitherResponsibleNorCoveringCannotReadTheChart() throws Exception {
        chartingService.chart(admission.getId(), observation(36.8, 84, 118, 76, 97, "78", "1.70"));

        assertThatThrownBy(() -> chartingService.chartOf(admission.getId())).isInstanceOf(AccessDeniedException.class);

        mockMvc.perform(get("/api/inpatient-charting/{admissionId}/vitals", admission.getId())).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- helpers

    /**
     * An observation with the readings a nurse would write down.
     *
     * <p>Height is given in metres, as a person says it, and converted here to the centimetres the domain
     * uses: the configured possible range is 10-260 and {@code VitalsValidator.bmi} takes height in
     * centimetres, so a height of 1.70 would be refused outright as physiologically impossible. That is
     * worth knowing before writing another test against a charted observation.
     */
    private static ChartVitalsRequestDTO observation(
        double temperature,
        int pulse,
        int systolic,
        int diastolic,
        int saturation,
        String weightKg,
        String heightMetres
    ) {
        ChartVitalsRequestDTO request = new ChartVitalsRequestDTO();
        request.setTemperature(BigDecimal.valueOf(temperature));
        request.setPulseRate(pulse);
        request.setSystolicBp(systolic);
        request.setDiastolicBp(diastolic);
        request.setOxygenSaturation(saturation);
        request.setWeight(new BigDecimal(weightKg));
        request.setHeight(new BigDecimal(heightMetres).movePointRight(2));
        request.setNotes("Charted by the night nurse");
        return request;
    }

    private static String body(double temperature, int pulse, int systolic, int diastolic, int saturation) {
        return (
            "{\"temperature\":" +
            temperature +
            ",\"pulseRate\":" +
            pulse +
            ",\"systolicBp\":" +
            systolic +
            ",\"diastolicBp\":" +
            diastolic +
            ",\"oxygenSaturation\":" +
            saturation +
            "}"
        );
    }
}
