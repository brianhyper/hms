package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.ShiftType;
import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import com.hyperbrains.hms.repository.ShiftRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * The roster's model: what a shift is, and the rules the table itself enforces.
 *
 * <p>Phase 4's P4.1, and the roster gate. There is no route and no service yet — this covers the table, because the
 * two things that are hard to change later are here: a shift hangs off a {@code StaffRecord} rather than an account,
 * so the people who never sign in can be rostered at all, and the ward is optional, so a porter is not forced onto
 * a ward that does not exist for them. One rule is enforced by the database rather than by code, and is asserted
 * against the database on purpose: one person, one shift a day, which is what makes "who is on Thursday" a single
 * answer.
 */
@IntegrationTest
class ShiftModelIT {

    private static final LocalDate THURSDAY = LocalDate.of(2026, 10, 8);

    @Autowired
    private EntityManager em;

    @Autowired
    private ShiftRepository shiftRepository;

    /** The account that wrote the roster, which the row has to name. */
    private User planner;

    /** A porter: a member of staff with no system account, which is the case the roster exists for. */
    private StaffRecord porter;

    @BeforeEach
    void setUp() {
        planner = UserResourceIT.createEntity();
        em.persist(planner);

        Department department = DepartmentResourceIT.createEntity();
        em.persist(department);

        porter = new StaffRecord()
            .fullName("Amina Yusuf")
            .employmentStartDate(LocalDate.of(2024, 1, 15))
            .status(StaffRecordStatus.ACTIVE)
            .department(department);
        em.persist(porter);
        em.flush();
    }

    @Test
    @Transactional
    void aShiftRecordsTheDayThePersonTheWardAndWhoWroteIt() {
        Ward ward = aWard("Roster ward");
        Shift shift = aShift(THURSDAY).ward(ward);
        shiftRepository.saveAndFlush(shift);
        em.clear();

        Shift stored = shiftRepository.findById(shift.getId()).orElseThrow();
        assertThat(stored.getShiftDate()).isEqualTo(THURSDAY);
        assertThat(stored.getShiftType()).isEqualTo(ShiftType.DAY);
        assertThat(stored.getStartsAt()).isEqualTo(LocalTime.of(7, 0));
        assertThat(stored.getEndsAt()).isEqualTo(LocalTime.of(19, 0));
        assertThat(stored.getStaffRecord().getId()).as("the person on duty").isEqualTo(porter.getId());
        assertThat(stored.getWard().getId()).as("the ward the shift is on").isEqualTo(ward.getId());
        assertThat(stored.getCreatedBy().getId()).as("who wrote the roster").isEqualTo(planner.getId());
    }

    /**
     * A shift on no particular ward is a normal shift: a porter's, or a matron's across the hospital. The column is
     * nullable so that the roster does not have to hold a placeholder ward to say that.
     */
    @Test
    @Transactional
    void aShiftNeedsNoWard() {
        Shift shift = aShift(THURSDAY);
        shiftRepository.saveAndFlush(shift);
        em.clear();

        assertThat(shiftRepository.findById(shift.getId()).orElseThrow().getWard()).isNull();
    }

    /**
     * One person, one shift a day — the phase document's wording, and the reason "who is on Thursday" has one
     * answer. Enforced by a unique index rather than by a service, because the roster will have more than one way in
     * and an index cannot be forgotten by the next caller.
     *
     * <p>The cost of it, written down rather than left to be discovered from a failed insert: a genuine double shift
     * on one date is refused too. If that is real here, the index becomes (staff_record_id, shift_date, shift_type).
     */
    @Test
    @Transactional
    void onePersonHasOneShiftADay() {
        shiftRepository.saveAndFlush(aShift(THURSDAY));

        assertThatThrownBy(() -> shiftRepository.saveAndFlush(aShift(THURSDAY).shiftType(ShiftType.NIGHT)))
            .as("a second shift for the same person on the same day is refused")
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Shift aShift(LocalDate date) {
        return new Shift()
            .shiftDate(date)
            .shiftType(ShiftType.DAY)
            .startsAt(LocalTime.of(7, 0))
            .endsAt(LocalTime.of(19, 0))
            .staffRecord(porter)
            .createdBy(planner);
    }

    /** A ward name is unique, and the fixture builder reuses one name, so each test's ward gets its own. */
    private Ward aWard(String name) {
        Ward ward = WardResourceIT.createEntity(em);
        ward.setName(name + " " + UUID.randomUUID());
        em.persist(ward);
        em.flush();
        return ward;
    }
}
