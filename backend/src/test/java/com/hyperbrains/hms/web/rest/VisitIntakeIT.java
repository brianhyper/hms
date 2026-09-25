package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AppointmentStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AppointmentRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AppointmentCheckInRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitQueueItemDTO;
import com.hyperbrains.hms.service.rules.QueueKind;
import com.hyperbrains.hms.service.workflow.AppointmentNoShowService;
import com.hyperbrains.hms.service.workflow.QueueService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for opening a visit and for the queue ordering the screens rely on.
 *
 * <p>The queue ordering assertions are relative rather than absolute (my emergency must come before
 * my routine visit, not "must be first") because other suites share the database and may legitimately
 * have patients queued at the same time.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
class VisitIntakeIT {

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private AppointmentNoShowService appointmentNoShowService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    /** Unique per run: Department.name and .code are unique columns shared with other suites. */
    private String uniqueSuffix;

    private Department department;

    private Patient patient;

    private final List<Long> createdAppointmentIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        uniqueSuffix = Long.toString(System.nanoTime());

        department = new Department();
        department.setName("Triage " + uniqueSuffix);
        department.setCode("TR" + uniqueSuffix);
        department.setActive(true);
        department = departmentRepository.save(department);

        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Queue Test Patient");
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);
    }

    @AfterEach
    void cleanup() {
        // Appointments first: the appointment holds the foreign key to the visit.
        createdAppointmentIds.forEach(id -> appointmentRepository.findById(id).ifPresent(appointmentRepository::delete));
        createdAppointmentIds.clear();

        if (patient != null && patient.getId() != null) {
            // Sweep by patient rather than trusting the tracked list. A test that forgets to record
            // a visit would otherwise leave a row behind and make the patient undeletable, and the
            // resulting foreign-key error reads as a production bug rather than a test bookkeeping
            // mistake.
            visitRepository.findByPatientId(patient.getId()).forEach(visitRepository::delete);
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }

        if (department != null && department.getId() != null) {
            departmentRepository.findById(department.getId()).ifPresent(departmentRepository::delete);
        }
    }

    @Test
    void checkingInAScheduledAppointmentOpensAVisitAndConsumesTheAppointment() {
        Appointment appointment = appointment(AppointmentStatus.SCHEDULED, LocalDate.now().plusDays(1));

        AppointmentCheckInRequestDTO request = new AppointmentCheckInRequestDTO();
        request.setPriority(VisitPriority.URGENT);
        VisitDTO visit = visitIntakeService.checkIn(appointment.getId(), request);

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.WAITING_VITALS);
        assertThat(visit.getType()).isEqualTo(VisitType.OUTPATIENT);
        assertThat(visit.getPriority()).isEqualTo(VisitPriority.URGENT);

        Appointment reloaded = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
        assertThat(reloaded.getVisit()).isNotNull();
        assertThat(reloaded.getVisit().getId()).isEqualTo(visit.getId());
    }

    @Test
    void anAppointmentCannotBeCheckedInTwice() {
        Appointment appointment = appointment(AppointmentStatus.SCHEDULED, LocalDate.now().plusDays(1));
        VisitDTO visit = visitIntakeService.checkIn(appointment.getId(), new AppointmentCheckInRequestDTO());

        assertThatThrownBy(() -> visitIntakeService.checkIn(appointment.getId(), new AppointmentCheckInRequestDTO()))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Only a scheduled appointment");
    }

    /**
     * The rule that keeps a missed appointment final. A late arrival starts a fresh walk-in visit
     * rather than being retroactively attached to the slot the hospital already wrote off.
     */
    @Test
    void aNoShowAppointmentCannotBeRevived() {
        Appointment missed = appointment(AppointmentStatus.NO_SHOW, LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> visitIntakeService.checkIn(missed.getId(), new AppointmentCheckInRequestDTO()))
            .isInstanceOf(BusinessRuleViolationException.class);

        // And a walk-in is still available to that patient.
        VisitDTO walkIn = visitIntakeService.createVisit(intakeRequest(VisitType.OUTPATIENT, VisitPriority.NORMAL));

        assertThat(walkIn.getId()).isNotNull();
        assertThat(appointmentRepository.findById(missed.getId()).orElseThrow().getVisit()).isNull();
    }

    /** An unidentified emergency patient has an incomplete registration, and must still be treated. */
    @Test
    void aWalkInIsAllowedForAPatientWhoseRegistrationIsIncomplete() {
        patient.setRegistrationStatus(RegistrationStatus.INCOMPLETE_REGISTRATION);
        patient = patientRepository.save(patient);

        VisitDTO visit = visitIntakeService.createVisit(intakeRequest(VisitType.EMERGENCY, VisitPriority.EMERGENCY));

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.WAITING_VITALS);
        assertThat(visit.getType()).isEqualTo(VisitType.EMERGENCY);
    }

    @Test
    void aMergedPatientCannotStartANewVisit() {
        patient.setRegistrationStatus(RegistrationStatus.MERGED);
        patient = patientRepository.save(patient);

        VisitIntakeRequestDTO request = intakeRequest(VisitType.OUTPATIENT, VisitPriority.NORMAL);

        assertThatThrownBy(() -> visitIntakeService.createVisit(request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("merged");
    }

    @Test
    void admissionCannotBeOpenedAsANewVisit() {
        VisitIntakeRequestDTO request = intakeRequest(VisitType.ADMISSION, VisitPriority.NORMAL);

        assertThatThrownBy(() -> visitIntakeService.createVisit(request)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void pharmacyOnlyVisitsWaitForTheirPrescriptionRatherThanForTriage() {
        VisitDTO visit = visitIntakeService.createVisit(intakeRequest(VisitType.PHARMACY_ONLY, VisitPriority.NORMAL));

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.REGISTERED);
    }

    @Test
    void theQueuePutsAnEmergencyAheadOfAnEarlierRoutineVisit() {
        VisitDTO routine = visitIntakeService.createVisit(intakeRequest(VisitType.OUTPATIENT, VisitPriority.NORMAL));
        VisitDTO emergency = visitIntakeService.createVisit(intakeRequest(VisitType.EMERGENCY, VisitPriority.EMERGENCY));

        List<Long> order = queueService
            .page(QueueKind.VITALS, PageRequest.of(0, 200))
            .getContent()
            .stream()
            .map(VisitQueueItemDTO::getVisitId)
            .toList();

        assertThat(order).contains(routine.getId(), emergency.getId());
        assertThat(order.indexOf(emergency.getId())).isLessThan(order.indexOf(routine.getId()));
    }

    @Test
    void onlyTheFirstQueueRowSaysASelectionWouldSkipSomeone() {
        visitIntakeService.createVisit(intakeRequest(VisitType.OUTPATIENT, VisitPriority.NORMAL));
        List<VisitQueueItemDTO> queue = queueService.page(QueueKind.VITALS, PageRequest.of(0, 200)).getContent();

        assertThat(queue).isNotEmpty();
        assertThat(queue.getFirst().isRequiresSkipReason()).isFalse();
        assertThat(queue.stream().skip(1)).allSatisfy(item -> assertThat(item.isRequiresSkipReason()).isTrue());
    }

    @Test
    void theHeadOfTheQueueReadsThroughTheSameOrderingAsThePage() {
        visitIntakeService.createVisit(intakeRequest(VisitType.EMERGENCY, VisitPriority.EMERGENCY));

        assertThat(queueService.head(QueueKind.VITALS))
            .isPresent()
            .get()
            .extracting(VisitQueueItemDTO::getVisitId)
            .isEqualTo(queueService.page(QueueKind.VITALS, PageRequest.of(0, 1)).getContent().getFirst().getVisitId());
    }

    @Test
    void missedAppointmentsBecomeNoShowsButFutureOnesDoNot() {
        Appointment overdue = appointment(AppointmentStatus.SCHEDULED, LocalDate.now().minusDays(3));
        Appointment upcoming = appointment(AppointmentStatus.SCHEDULED, LocalDate.now().plusDays(30));

        appointmentNoShowService.markMissedAppointmentsAsNoShow();

        assertThat(appointmentRepository.findById(overdue.getId()).orElseThrow().getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        assertThat(appointmentRepository.findById(upcoming.getId()).orElseThrow().getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void anAlreadyCheckedInAppointmentIsNotSweptIntoNoShows() {
        Appointment checkedIn = appointment(AppointmentStatus.CHECKED_IN, LocalDate.now().minusDays(3));

        appointmentNoShowService.markMissedAppointmentsAsNoShow();

        assertThat(appointmentRepository.findById(checkedIn.getId()).orElseThrow().getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
    }

    private VisitIntakeRequestDTO intakeRequest(VisitType type, VisitPriority priority) {
        VisitIntakeRequestDTO request = new VisitIntakeRequestDTO();
        request.setPatientId(patient.getId());
        request.setType(type);
        request.setPriority(priority);
        request.setReasonForVisit("Integration test reason");
        return request;
    }

    private Appointment appointment(AppointmentStatus status, LocalDate date) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDepartment(department);
        appointment.setStatus(status);
        appointment.setScheduledDate(date);
        appointment.setScheduledTime(LocalTime.of(9, 30));
        appointment.setReason("Integration test appointment");
        appointment = appointmentRepository.save(appointment);
        createdAppointmentIds.add(appointment.getId());
        return appointment;
    }
}
