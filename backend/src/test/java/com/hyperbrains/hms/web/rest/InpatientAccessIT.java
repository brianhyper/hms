package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Authority;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.view.AssignWardCoverRequestDTO;
import com.hyperbrains.hms.service.dto.view.MyPatientViewDTO;
import com.hyperbrains.hms.service.dto.view.MyPatientViewDTO.SeenBecause;
import com.hyperbrains.hms.service.dto.view.WardCoverViewDTO;
import com.hyperbrains.hms.service.workflow.InpatientAccessService;
import com.hyperbrains.hms.service.workflow.WardCoverWorkflowService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the inpatient row-level rule: which patients a doctor may see.
 *
 * <p>The rule has two halves — "the patients I am responsible for" and "the patients on a ward I am
 * covering" — and the second is the one that needed a roster to exist at all. So these tests are largely
 * about the cover window: in force, not yet started, already ended, and ended early while somebody is
 * looking at the list.
 *
 * <p>The last test in the class is the one that matters most for the rule being real rather than decorative:
 * a doctor must not be able to read the whole hospital's admissions through the generated CRUD, or the
 * filtered list is a suggestion.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = InpatientAccessIT.DOCTOR_LOGIN, authorities = "ROLE_DOCTOR")
class InpatientAccessIT {

    /**
     * The login the tests act as, and the account they create to act as it.
     *
     * <p>The stock test accounts hold no hospital role — the Phase 1 roles are seeded into a development
     * database, which the test context does not run — so a doctor has to be made. The login is a constant
     * because {@code @WithMockUser} is evaluated before any fixture exists, and the fixture is recreated per
     * test so a run that crashed halfway cannot leave it behind and make the next one fail on a duplicate.
     */
    static final String DOCTOR_LOGIN = "wardoctor";

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private InpatientAccessService accessService;

    @Autowired
    private WardCoverWorkflowService wardCoverService;

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
    private WardCoverRepository wardCoverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private MockMvc mockMvc;

    private Department department;

    private Ward wardA;

    private Ward wardB;

    private Ward wardC;

    private BedType bedType;

    private Admission mine;

    private Admission onACoveredWard;

    private Admission onAnUncoveredWard;

    private User me;

    private User anotherDoctor;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Access IT " + SUFFIX);
        department.setCode("ACIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        wardA = ward("Access Ward A " + SUFFIX, true);
        wardB = ward("Access Ward B " + SUFFIX, true);
        wardC = ward("Access Ward C " + SUFFIX, true);

        bedType = new BedType();
        bedType.setName("Access Type " + SUFFIX);
        bedType.setDefaultDailyRate(new BigDecimal("3500.00"));
        bedType.setActive(true);
        bedType = bedTypeRepository.save(bedType);

        me = doctorFixture();
        anotherDoctor = userRepository.findOneByLogin("user").orElseThrow();

        // Distinct admission times so the list order is a fact about the fixture rather than about insertion.
        mine = admission(wardA, me, "A1", Instant.parse("2026-09-27T08:00:00Z"));
        onACoveredWard = admission(wardB, anotherDoctor, "B1", Instant.parse("2026-09-27T10:00:00Z"));
        onAnUncoveredWard = admission(wardC, anotherDoctor, "C1", Instant.parse("2026-09-27T12:00:00Z"));
    }

    @AfterEach
    void cleanup() {
        wardCoverRepository
            .findAllWithWard()
            .stream()
            .filter(cover -> cover.getDoctor() != null && me.getId().equals(cover.getDoctor().getId()))
            .forEach(cover -> {
                // The roster's own trail first: an audit row names the user as its actor, so it keeps the doctor
                // fixture undeletable until it is gone.
                auditLogRepository
                    .findByEntityNameAndEntityIdOrderByIdAsc("WardCover", String.valueOf(cover.getId()))
                    .forEach(auditLogRepository::delete);
                wardCoverRepository.deleteById(cover.getId());
            });
        List.of(mine, onACoveredWard, onAnUncoveredWard).forEach(admission -> {
            // The stay goes before the bed it is sitting in, and the visit before the patient it belongs to:
            // the foreign keys run that way round.
            admissionRepository.deleteById(admission.getId());
            bedRepository.deleteById(admission.getBed().getId());
            visitRepository.deleteById(admission.getVisit().getId());
            patientRepository.deleteById(admission.getVisit().getPatient().getId());
        });
        List.of(wardA, wardB, wardC).forEach(ward -> wardRepository.deleteById(ward.getId()));
        bedTypeRepository.deleteById(bedType.getId());
        departmentRepository.deleteById(department.getId());
        if (me != null && me.getId() != null) {
            userRepository.findById(me.getId()).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    // ---------------------------------------------------------------- what the doctor sees

    @Test
    void aDoctorSeesTheirOwnPatientAndAnyPatientOnAWardTheyCover() {
        cover(wardB, now(), now().plus(8, ChronoUnit.HOURS), null);

        List<MyPatientViewDTO> list = mineIn(accessService.myPatients());

        assertThat(list)
            .as("their own patient, and the one on the ward they are covering")
            .extracting(MyPatientViewDTO::admissionId)
            .containsExactly(mine.getId(), onACoveredWard.getId());
        assertThat(list)
            .filteredOn(row -> row.admissionId().equals(mine.getId()))
            .singleElement()
            .satisfies(row -> assertThat(row.seenBecause()).isEqualTo(SeenBecause.PRIMARY_DOCTOR));
        assertThat(list)
            .filteredOn(row -> row.admissionId().equals(onACoveredWard.getId()))
            .singleElement()
            .satisfies(row -> {
                assertThat(row.seenBecause()).isEqualTo(SeenBecause.COVERING_WARD);
                assertThat(row.wardName()).isEqualTo(wardB.getName());
                assertThat(row.bedNumber()).isNotNull();
                assertThat(row.patientHospitalId()).isEqualTo(onACoveredWard.getVisit().getPatient().getHospitalId());
            });
    }

    @Test
    void aPatientOnAWardNobodyIsCoveringIsNotOnTheList() {
        cover(wardB, now(), now().plus(8, ChronoUnit.HOURS), null);

        assertThat(mineIn(accessService.myPatients()))
            .extracting(MyPatientViewDTO::admissionId)
            .doesNotContain(onAnUncoveredWard.getId());
    }

    @Test
    void coverThatHasAlreadyEndedGrantsNothing() {
        cover(
            wardC,
            now().minus(12, ChronoUnit.HOURS),
            now().minus(1, ChronoUnit.HOURS),
            "The night shift that has finished"
        );

        assertThat(mineIn(accessService.myPatients()))
            .as("a finished shift is not cover, however recently it finished")
            .extracting(MyPatientViewDTO::admissionId)
            .doesNotContain(onAnUncoveredWard.getId());
    }

    @Test
    void coverThatHasNotStartedYetGrantsNothing() {
        cover(wardC, now().plus(1, ChronoUnit.HOURS), now().plus(9, ChronoUnit.HOURS), "Tomorrow's shift");

        assertThat(mineIn(accessService.myPatients()))
            .extracting(MyPatientViewDTO::admissionId)
            .doesNotContain(onAnUncoveredWard.getId());
    }

    @Test
    void endingCoverTakesItsPatientsOffTheListStraightAway() {
        WardCoverViewDTO assigned = cover(wardB, now(), now().plus(8, ChronoUnit.HOURS), null);
        assertThat(mineIn(accessService.myPatients())).extracting(MyPatientViewDTO::admissionId).contains(onACoveredWard.getId());

        wardCoverService.end(assigned.coverId());

        assertThat(mineIn(accessService.myPatients()))
            .as("covering stopped the moment it was ended")
            .extracting(MyPatientViewDTO::admissionId)
            .doesNotContain(onACoveredWard.getId());
        assertThat(wardCoverService.current()).extracting(WardCoverViewDTO::coverId).doesNotContain(assigned.coverId());
    }

    @Test
    void closingAWardTakesItsPatientsBackOffTheCoveringDoctorsList() {
        cover(wardB, now(), now().plus(8, ChronoUnit.HOURS), null);
        assertThat(mineIn(accessService.myPatients()))
            .as("cover on a ward that is open does show its patients")
            .extracting(MyPatientViewDTO::admissionId)
            .contains(onACoveredWard.getId());

        close(wardB);

        assertThat(mineIn(accessService.myPatients()))
            .as("a ward taken out of service is nobody's to cover, whatever the roster still says")
            .extracting(MyPatientViewDTO::admissionId)
            .doesNotContain(onACoveredWard.getId());
        assertThat(mineIn(accessService.myPatients()))
            .as("and their own patient is still theirs, because the cover was never what put them there")
            .extracting(MyPatientViewDTO::admissionId)
            .contains(mine.getId());
    }

    /**
     * The sharper half of the same rule. Cover is also the claim that lets a doctor write on a patient — orders
     * and charting both go through this check — so a roster entry for a closed ward would otherwise leave a
     * doctor able to chart on patients the hospital has stopped using.
     */
    @Test
    void closingAWardStopsTheCoveringDoctorWritingOnItsPatients() {
        cover(wardB, now(), now().plus(8, ChronoUnit.HOURS), null);
        accessService.requireMayView(onACoveredWard.getId());

        close(wardB);

        assertThatThrownBy(() -> accessService.requireMayView(onACoveredWard.getId())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void aDischargedPatientIsOnNobodyListEvenTheirOwnDoctors() {
        Admission ended = admissionRepository.findById(mine.getId()).orElseThrow();
        ended.setStatus(AdmissionStatus.DISCHARGED);
        admissionRepository.save(ended);

        assertThat(mineIn(accessService.myPatients())).extracting(MyPatientViewDTO::admissionId).doesNotContain(mine.getId());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_NURSE")
    void aNurseSeesEveryInpatient() {
        List<MyPatientViewDTO> list = mineIn(accessService.myPatients());

        assertThat(list)
            .as("the specification puts no row rule on nursing")
            .extracting(MyPatientViewDTO::admissionId)
            .containsExactlyInAnyOrder(mine.getId(), onACoveredWard.getId(), onAnUncoveredWard.getId());
        assertThat(list).allSatisfy(row -> assertThat(row.seenBecause()).isEqualTo(SeenBecause.FULL_ACCESS));
    }

    // ---------------------------------------------------------------- the roster

    @Test
    void aCoverPeriodThatEndsBeforeItStartsIsRefused() {
        assertThatThrownBy(() ->
            wardCoverService.assign(request(wardA, me, now(), now().minus(1, ChronoUnit.HOURS), null))
        )
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex ->
                assertThat(ex.getErrorKey()).isEqualTo("coverPeriodNotWellFormed")
            );
    }

    @Test
    void theRosterRefusesSomebodyWhoIsNotADoctor() {
        assertThatThrownBy(() -> wardCoverService.assign(request(wardA, anotherDoctor, now(), null, null)))
            .as("the roster's only job is to decide which doctors see which patients")
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("notADoctor"));
    }

    @Test
    void theRosterRefusesCoverForAWardThatIsNotTakingPatients() {
        Ward closed = ward("Access Closed Ward " + SUFFIX, false);

        assertThatThrownBy(() -> wardCoverService.assign(request(closed, me, now(), null, null)))
            .isInstanceOfSatisfying(BusinessRuleViolationException.class, ex -> assertThat(ex.getErrorKey()).isEqualTo("wardNotActive"));

        wardRepository.deleteById(closed.getId());
    }

    @Test
    void openEndedCoverStaysInForceUntilItIsEnded() {
        WardCoverViewDTO assigned = cover(wardB, now().minus(1, ChronoUnit.HOURS), null, "Until the rotation changes");

        assertThat(assigned.coversTo()).isNull();
        assertThat(assigned.inForce()).as("open-ended is in force now").isTrue();
        assertThat(wardCoverService.current()).extracting(WardCoverViewDTO::coverId).contains(assigned.coverId());
    }

    // ---------------------------------------------------------------- over HTTP

    @Test
    void aDoctorMayReadTheirOwnList() throws Exception {
        cover(wardB, now(), null, null);

        mockMvc
            .perform(get("/api/inpatient-worklist/my-patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.admissionId == " + mine.getId() + ")]").isNotEmpty());
    }

    /**
     * The rule is only real if the unrestricted list is closed to doctors: otherwise a filtered screen and an
     * unfiltered API are both one request away from each other.
     */
    @Test
    void aDoctorCannotReadTheWholeHospitalsAdmissions() throws Exception {
        mockMvc.perform(get("/api/admissions")).andExpect(status().isForbidden());
    }

    @Test
    void aDoctorMayReadWhoIsCoveringWhat() throws Exception {
        cover(wardB, now(), null, null);

        mockMvc
            .perform(get("/api/ward-cover-roster/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.wardId == " + wardB.getId() + ")]").isNotEmpty());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
    void theDeskMayNotReadTheDoctorsList() throws Exception {
        mockMvc.perform(get("/api/inpatient-worklist/my-patients")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void anAdministratorMayReadEveryInpatient() throws Exception {
        mockMvc
            .perform(get("/api/inpatient-worklist/my-patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.admissionId == " + onAnUncoveredWard.getId() + ")]").isNotEmpty());
    }

    // ---------------------------------------------------------------- helpers

    /** The doctor the tests act as: a real account holding the role the roster insists on. */
    private User doctorFixture() {
        userRepository.findOneByLogin(DOCTOR_LOGIN).ifPresent(existing -> userRepository.deleteById(existing.getId()));
        Authority doctorRole = authorityRepository.findById(AuthoritiesConstants.DOCTOR).orElseThrow();
        User doctor = UserResourceIT.createEntity();
        doctor.setLogin(DOCTOR_LOGIN);
        doctor.setAuthorities(Set.of(doctorRole));
        return userRepository.save(doctor);
    }

    private List<MyPatientViewDTO> mineIn(List<MyPatientViewDTO> list) {
        List<Long> ours = List.of(mine.getId(), onACoveredWard.getId(), onAnUncoveredWard.getId());
        return list.stream().filter(row -> ours.contains(row.admissionId())).toList();
    }

    private Ward ward(String name, boolean active) {
        Ward ward = new Ward();
        ward.setName(name);
        ward.setActive(active);
        ward.setDepartment(department);
        return wardRepository.save(ward);
    }

    private Patient patient(String fullName) {
        Patient record = new Patient();
        record.setHospitalId(hospitalIdService.nextPermanentId());
        record.setFullName(fullName);
        record.setSex(Sex.FEMALE);
        record.setSexEstimated(false);
        record.setRegistrationStatus(RegistrationStatus.COMPLETE);
        return patientRepository.save(record);
    }

    /** A stay in a bed on this ward, looked after by this doctor, for a patient of their own. */
    private Admission admission(Ward ward, User responsible, String bedNumber, Instant admittedAt) {
        Visit visit = new Visit();
        visit.setPatient(patient("Access Patient " + SUFFIX + " " + bedNumber));
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit.setPriority(VisitPriority.NORMAL);
        visit.setReasonForVisit("Access test");
        visit.setCreatedAt(Instant.now());
        visit = visitRepository.save(visit);

        Bed bed = new Bed();
        bed.setBedNumber(SUFFIX + "-" + bedNumber);
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setWard(ward);
        bed.setBedType(bedType);
        bed = bedRepository.save(bed);

        Admission admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(admittedAt);
        admission.setAdmissionReason("Access test");
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setBed(bed);
        admission.setAdmittingDoctor(responsible);
        admission.setPrimaryDoctor(responsible);
        return admissionRepository.save(admission);
    }

    private WardCoverViewDTO cover(Ward ward, Instant from, Instant to, String note) {
        return wardCoverService.assign(request(ward, me, from, to, note));
    }

    /** Takes a ward out of service the way the ward screens do, without going through the endpoint. */
    private void close(Ward ward) {
        Ward closing = wardRepository.findById(ward.getId()).orElseThrow();
        closing.setActive(false);
        wardRepository.saveAndFlush(closing);
    }

    private static AssignWardCoverRequestDTO request(Ward ward, User doctor, Instant from, Instant to, String note) {
        AssignWardCoverRequestDTO request = new AssignWardCoverRequestDTO();
        request.setDoctorId(doctor.getId());
        request.setWardId(ward.getId());
        request.setCoversFrom(from);
        request.setCoversTo(to);
        request.setNote(note);
        return request;
    }

    private static Instant now() {
        return Instant.now();
    }
}
