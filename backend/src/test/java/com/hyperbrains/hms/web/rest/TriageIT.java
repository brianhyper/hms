package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.VitalsRejectedException;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.StartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionResultDTO;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for triage.
 *
 * <p>Reads the recorded vitals back through the repository rather than off the returned visit,
 * because the visit's associations are lazy and this test runs outside a session — touching a
 * proxied field would fail for a reason that has nothing to do with triage.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class TriageIT {

    @Autowired
    private TriageService triageService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private final List<Long> createdVitalSignsIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Triage Test Patient");
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);
    }

    @AfterEach
    void cleanup() {
        // Visits first: the visit holds the foreign key to its vitals, so the vitals cannot go
        // until the reference is gone.
        for (Visit visit : visits()) {
            visitRepository.delete(visit);
        }
        createdVitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        createdVitalSignsIds.clear();
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }
    }

    @Test
    void startingVitalsClaimsTheVisitForTheNurse() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);

        VisitDTO claimed = triageService.startVitals(visit.getId(), new StartVitalsRequestDTO());

        assertThat(claimed.getStatus()).isEqualTo(VisitStatus.IN_VITALS);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStartedVitalsAt()).isNotNull();
    }

    @Test
    void submittingVitalsSendsThePatientToTheDoctorsQueue() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        triageService.startVitals(visit.getId(), new StartVitalsRequestDTO());

        VitalsSubmissionResultDTO result = triageService.submitVitals(visit.getId(), normalVitals());
        createdVitalSignsIds.add(result.getVitalSigns().getId());

        assertThat(result.getVisitStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);
        assertThat(result.isCorrection()).isFalse();
        assertThat(result.getWarnings()).isEmpty();
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);
    }

    @Test
    void bmiIsComputedRatherThanTrustedFromTheClient() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionRequestDTO request = normalVitals();
        request.setWeight(BigDecimal.valueOf(70));
        request.setHeight(BigDecimal.valueOf(175));

        VitalsSubmissionResultDTO result = triageService.submitVitals(visit.getId(), request);
        createdVitalSignsIds.add(result.getVitalSigns().getId());

        assertThat(vitalSignsRepository.findById(result.getVitalSigns().getId()).orElseThrow().getBmi()).isEqualByComparingTo("22.86");
    }

    /** The first tier: saved, and flagged. */
    @Test
    void anAbnormalButPossibleReadingIsSavedAndWarnedAbout() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionRequestDTO request = normalVitals();
        request.setPulseRate(130);

        VitalsSubmissionResultDTO result = triageService.submitVitals(visit.getId(), request);
        createdVitalSignsIds.add(result.getVitalSigns().getId());

        assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
            assertThat(warning.getField()).isEqualTo("pulseRate");
            assertThat(warning.getMessage()).contains("130");
        });
        // The reading is on file despite the warning — that is the difference from a refusal.
        assertThat(vitalSignsRepository.findById(result.getVitalSigns().getId()).orElseThrow().getPulseRate()).isEqualTo(130);
        assertThat(result.getVisitStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);
    }

    /** The second tier: refused, and nothing is written. */
    @Test
    void anImpossibleReadingIsRefusedAndLeavesNoTrace() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionRequestDTO request = normalVitals();
        request.setPulseRate(400);

        assertThatThrownBy(() -> triageService.submitVitals(visit.getId(), request))
            .isInstanceOf(VitalsRejectedException.class)
            .satisfies(thrown ->
                assertThat(((VitalsRejectedException) thrown).getRejections()).singleElement().satisfies(rejection ->
                    assertThat(rejection.field()).isEqualTo("pulseRate")
                )
            );

        Visit reloaded = visitRepository.findById(visit.getId()).orElseThrow();
        assertThat(reloaded.getVitals()).isNull();
        // Crucially the patient did not advance: a refused reading must not push them to a doctor.
        assertThat(reloaded.getStatus()).isEqualTo(VisitStatus.WAITING_VITALS);
    }

    @Test
    void vitalsCannotBeRecordedForAVisitThatIsNotAwaitingThem() {
        // A pharmacy-only visit waits for its prescription, not for triage.
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.PHARMACY_ONLY);
        intake.setReasonForVisit("Collecting medication");
        VisitDTO pharmacyOnly = visitIntakeService.createVisit(intake);

        assertThatThrownBy(() -> triageService.submitVitals(pharmacyOnly.getId(), normalVitals()))
            .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void correctingVitalsWithoutAReasonIsRefused() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionResultDTO first = triageService.submitVitals(visit.getId(), normalVitals());
        createdVitalSignsIds.add(first.getVitalSigns().getId());

        VitalsSubmissionRequestDTO correction = normalVitals();
        correction.setPulseRate(80);

        assertThatThrownBy(() -> triageService.submitVitals(visit.getId(), correction))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("requires a reason");
    }

    /**
     * Edit-in-place, not a new row: the current value stays the single source of truth and the
     * previous value survives only in the audit trail.
     */
    @Test
    void aCorrectionEditsTheExistingReadingInPlace() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionResultDTO first = triageService.submitVitals(visit.getId(), normalVitals());
        Long vitalSignsId = first.getVitalSigns().getId();
        createdVitalSignsIds.add(vitalSignsId);

        VitalsSubmissionRequestDTO correction = normalVitals();
        correction.setPulseRate(88);
        correction.setCorrectionReason("Pulse was misread from the monitor");
        VitalsSubmissionResultDTO result = triageService.submitVitals(visit.getId(), correction);

        assertThat(result.isCorrection()).isTrue();
        assertThat(result.getVitalSigns().getId()).isEqualTo(vitalSignsId);
        assertThat(vitalSignsRepository.findById(vitalSignsId).orElseThrow().getPulseRate()).isEqualTo(88);
    }

    /**
     * The patient has already moved on by the time anyone corrects vitals, so a correction must not
     * drag them back into the queue and have them seen twice.
     */
    @Test
    void aCorrectionLeavesTheVisitWhereItWas() {
        VisitDTO visit = openVisit(VisitPriority.NORMAL);
        VitalsSubmissionResultDTO first = triageService.submitVitals(visit.getId(), normalVitals());
        createdVitalSignsIds.add(first.getVitalSigns().getId());
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);

        VitalsSubmissionRequestDTO correction = normalVitals();
        correction.setCorrectionReason("Corrected after re-measuring");
        VitalsSubmissionResultDTO result = triageService.submitVitals(visit.getId(), correction);

        assertThat(result.getVisitStatus()).isEqualTo(VisitStatus.WAITING_DOCTOR);
    }

    /** Selecting out of order is allowed, but it cannot be silent. */
    @Test
    void selectingAPatientOutOfOrderRequiresAReason() {
        // An emergency visit guarantees something ranks ahead of the routine visit below, whatever
        // else other suites have queued.
        openVisit(VisitPriority.EMERGENCY);
        VisitDTO routine = openVisit(VisitPriority.NORMAL);

        assertThatThrownBy(() -> triageService.startVitals(routine.getId(), new StartVitalsRequestDTO()))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("out of order");

        StartVitalsRequestDTO withReason = new StartVitalsRequestDTO();
        withReason.setQueueSkipReason("Patient distressed, seen ahead of turn");
        triageService.startVitals(routine.getId(), withReason);

        assertThat(visitRepository.findById(routine.getId()).orElseThrow().getQueueSkipReason())
            .isEqualTo("Patient distressed, seen ahead of turn");
        assertThat(visitRepository.findById(routine.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.IN_VITALS);
    }

    private List<Visit> visits() {
        return patient == null || patient.getId() == null ? List.of() : visitRepository.findByPatientId(patient.getId());
    }

    private VisitDTO openVisit(VisitPriority priority) {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(priority);
        intake.setReasonForVisit("Triage test");
        return visitIntakeService.createVisit(intake);
    }

    private static VitalsSubmissionRequestDTO normalVitals() {
        VitalsSubmissionRequestDTO request = new VitalsSubmissionRequestDTO();
        request.setTemperature(BigDecimal.valueOf(36.8));
        request.setPulseRate(72);
        request.setSystolicBp(118);
        request.setDiastolicBp(76);
        request.setOxygenSaturation(98);
        request.setTriageNotes("Looks well");
        return request;
    }
}
