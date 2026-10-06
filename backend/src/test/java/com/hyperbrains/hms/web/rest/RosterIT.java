package com.hyperbrains.hms.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.ShiftType;
import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import com.hyperbrains.hms.repository.ShiftRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Who is on duty, as a ward reads it.
 *
 * <p>Two things are asserted here that are the reason this read exists at all. It answers the question — a shift that
 * covers this moment is on the list and one that has ended is not — and it carries no field of the staff file, which
 * is asserted against the serialised JSON rather than against a mapping, because the guarantee is supposed to be
 * that there is nothing to leak rather than that a mapper is careful.
 */
@IntegrationTest
@AutoConfigureMockMvc
class RosterIT {

    /**
     * The window used for "on duty now" is the whole of today, so the assertion does not depend on the hour the
     * suite happens to run at. It ends a second before midnight rather than at {@code LocalTime.MAX}, because
     * PostgreSQL rounds fractional seconds to the column's precision and would carry that up to midnight, making the
     * window empty.
     */
    private static final LocalTime ALL_DAY_FROM = LocalTime.MIDNIGHT;
    private static final LocalTime ALL_DAY_TO = LocalTime.of(23, 59, 59);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private EntityManager em;

    private User planner;
    private Department emergency;
    private Ward ward;

    @BeforeEach
    void setUp() {
        planner = UserResourceIT.createEntity();
        em.persist(planner);

        emergency = DepartmentResourceIT.createEntity();
        emergency.setName("Emergency " + UUID.randomUUID());
        em.persist(emergency);

        ward = WardResourceIT.createEntity(em);
        ward.setName("Roster Ward " + UUID.randomUUID());
        em.persist(ward);
        em.flush();
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.NURSE)
    void theShiftCoveringThisMomentIsOnDutyAndTheOneThatEndedIsNot() throws Exception {
        aShiftOnDutyToday("Amina Yusuf");
        // The same person's shift for yesterday's daytime: it ended, and nothing about it is in force now.
        aShiftFor("John Kamau", LocalDate.now(hospitalZone()).minusDays(1), LocalTime.of(7, 0), LocalTime.of(19, 0));

        mockMvc
            .perform(get("/api/roster/on-duty"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].staffName").value(hasItem("Amina Yusuf")))
            .andExpect(jsonPath("$.[*].departmentName").value(hasItem(emergency.getName())))
            .andExpect(jsonPath("$.[*].shiftType").value(hasItem(ShiftType.DAY.toString())))
            .andExpect(jsonPath("$.[*].staffName").value(not(hasItem("John Kamau"))));
    }

    /**
     * The reason this read exists. A shift row names the person by staff record id, and the file behind it is HR's;
     * what a ward gets instead has no field for any of it.
     */
    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.NURSE)
    void theWardIsGivenANameAndADepartmentAndNothingElseOfTheirs() throws Exception {
        aShiftOnDutyToday("Amina Yusuf");

        mockMvc
            .perform(get("/api/roster/on-duty"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].staffName").exists())
            .andExpect(jsonPath("$.[0].nationalId").doesNotExist())
            .andExpect(jsonPath("$.[0].staffRecord").doesNotExist())
            .andExpect(jsonPath("$.[0].staffRecordId").doesNotExist())
            .andExpect(jsonPath("$.[0].contactPhone").doesNotExist())
            .andExpect(jsonPath("$.[0].createdBy").doesNotExist());
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.DOCTOR)
    void theWardReadReturnsThatWardsShiftsAndNoOthers() throws Exception {
        onDutyOnWard("Amina Yusuf", ward);
        Ward otherWard = aWard("Other Ward");
        onDutyOnWard("John Kamau", otherWard);

        mockMvc
            .perform(get("/api/roster/wards/{wardId}/on-duty", ward.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].staffName").value(hasItem("Amina Yusuf")))
            .andExpect(jsonPath("$.[*].staffName").value(not(hasItem("John Kamau"))));

        mockMvc
            .perform(get("/api/roster/wards/{wardId}/on-duty", otherWard.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].staffName").value(hasItem("John Kamau")))
            .andExpect(jsonPath("$.[*].staffName").value(not(hasItem("Amina Yusuf"))));
    }

    /**
     * The roster tells a ward who is on duty, and that is not every role. Pharmacy, the lab and the desk have no
     * question this answers, and a row that admitted them would be a row nobody re-reads.
     */
    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.PHARMACY)
    void aRoleTheRosterIsNotForIsRefused() throws Exception {
        mockMvc.perform(get("/api/roster/on-duty")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/roster/wards/{wardId}/on-duty", 1L)).andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.RECEPTION)
    void theDeskIsRefusedToo() throws Exception {
        mockMvc.perform(get("/api/roster/on-duty")).andExpect(status().isForbidden());
    }

    /** Somebody on duty on a ward for the whole of today, which is every moment the suite could be running at. */
    private void onDutyOnWard(String staffName, Ward onThisWard) {
        shiftRepository.saveAndFlush(
            aShiftFor(staffName, LocalDate.now(hospitalZone()), ALL_DAY_FROM, ALL_DAY_TO).ward(onThisWard)
        );
    }

    private Shift aShiftOnDutyToday(String staffName) {
        return shiftRepository.saveAndFlush(
            aShiftFor(staffName, LocalDate.now(hospitalZone()), ALL_DAY_FROM, ALL_DAY_TO).ward(ward)
        );
    }

    private Shift aShiftFor(String staffName, LocalDate date, LocalTime from, LocalTime to) {
        StaffRecord staffRecord = new StaffRecord()
            .fullName(staffName)
            .employmentStartDate(LocalDate.of(2024, 1, 15))
            .status(StaffRecordStatus.ACTIVE)
            .department(emergency);
        em.persist(staffRecord);
        em.flush();
        return new Shift()
            .shiftDate(date)
            .shiftType(ShiftType.DAY)
            .startsAt(from)
            .endsAt(to)
            .staffRecord(staffRecord)
            .createdBy(planner);
    }

    /** A ward name is unique and the fixture builder reuses one, so the second ward gets its own. */
    private Ward aWard(String name) {
        Ward another = WardResourceIT.createEntity(em);
        another.setName(name + " " + UUID.randomUUID());
        em.persist(another);
        em.flush();
        return another;
    }

    private static ZoneId hospitalZone() {
        return ZoneId.of("Africa/Nairobi");
    }
}
