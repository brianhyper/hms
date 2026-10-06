package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.ShiftType;
import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import com.hyperbrains.hms.repository.ShiftRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.dto.ShiftDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.mapper.ShiftMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ShiftResource} REST controller.
 *
 * <p>These are the escape-hatch writes, so this class is what says what the escape hatch does: it stamps the author
 * from the caller rather than the body, it will not let an edit take the shift over, and it has no route that
 * removes a day from the roster. Who may reach any of it is asserted here too, because the row that decides it is
 * three lines away in `SecurityConfiguration` and a change to it would otherwise look like a working roster.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = AuthoritiesConstants.SUPER_ADMIN)
class ShiftResourceIT {

    private static final LocalDate DEFAULT_SHIFT_DATE = LocalDate.of(2026, 10, 8);
    private static final LocalDate UPDATED_SHIFT_DATE = LocalDate.of(2026, 10, 9);

    private static final ShiftType DEFAULT_SHIFT_TYPE = ShiftType.DAY;

    private static final LocalTime DEFAULT_STARTS_AT = LocalTime.of(7, 0);
    private static final LocalTime DEFAULT_ENDS_AT = LocalTime.of(19, 0);

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/shifts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final AtomicLong longCount = new AtomicLong(new Random().nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ShiftMapper shiftMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShiftMockMvc;

    private Shift shift;

    private Shift insertedShift;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Shift createEntity(EntityManager em) {
        Shift shift = new Shift()
            .shiftDate(DEFAULT_SHIFT_DATE)
            .shiftType(DEFAULT_SHIFT_TYPE)
            .startsAt(DEFAULT_STARTS_AT)
            .endsAt(DEFAULT_ENDS_AT)
            .note(DEFAULT_NOTE);
        // Add required entities
        User planner = UserResourceIT.createEntity();
        em.persist(planner);
        shift.setStaffRecord(aStaffRecord(em));
        shift.setCreatedBy(planner);
        return shift;
    }

    /** A member of staff to roster: employment data with no account behind it, which is the case the roster allows. */
    private static StaffRecord aStaffRecord(EntityManager em) {
        Department department = DepartmentResourceIT.createEntity();
        em.persist(department);
        StaffRecord staffRecord = new StaffRecord()
            .fullName("Amina Yusuf")
            .employmentStartDate(LocalDate.of(2024, 1, 15))
            .status(StaffRecordStatus.ACTIVE)
            .department(department);
        em.persist(staffRecord);
        em.flush();
        return staffRecord;
    }

    @BeforeEach
    void initTest() {
        shift = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedShift != null) {
            shiftRepository.deleteById(insertedShift.getId());
            insertedShift = null;
        }
    }

    @Test
    @Transactional
    void createShift() throws Exception {
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        ShiftDTO created = om.readValue(
            restShiftMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShiftDTO.class
        );

        Shift stored = shiftRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getShiftDate()).isEqualTo(DEFAULT_SHIFT_DATE);
        assertThat(stored.getShiftType()).isEqualTo(DEFAULT_SHIFT_TYPE);
        assertThat(stored.getStartsAt()).isEqualTo(DEFAULT_STARTS_AT);
        assertThat(stored.getEndsAt()).isEqualTo(DEFAULT_ENDS_AT);
        assertThat(stored.getNote()).isEqualTo(DEFAULT_NOTE);
        assertThat(stored.getStaffRecord().getId()).as("the person on duty").isEqualTo(shift.getStaffRecord().getId());
        assertThat(stored.getWard()).as("a shift may be on no particular ward").isNull();

        insertedShift = stored;
    }

    @Test
    @Transactional
    void createShiftWithExistingId() throws Exception {
        shift.setId(longCount.incrementAndGet());
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        restShiftMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Every required field is checked, one request each, because a validator that silently stopped applying would
     * otherwise be found by a caller rather than by this.
     */
    @Test
    @Transactional
    void createShiftRefusesARequestMissingSomethingRequired() throws Exception {
        ShiftDTO noDate = shiftMapper.toDto(shift);
        noDate.setShiftDate(null);
        ShiftDTO noType = shiftMapper.toDto(shift);
        noType.setShiftType(null);
        ShiftDTO noStart = shiftMapper.toDto(shift);
        noStart.setStartsAt(null);
        ShiftDTO noEnd = shiftMapper.toDto(shift);
        noEnd.setEndsAt(null);
        ShiftDTO noPerson = shiftMapper.toDto(shift);
        noPerson.setStaffRecord(null);

        for (ShiftDTO incomplete : List.of(noDate, noType, noStart, noEnd, noPerson)) {
            restShiftMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomplete)))
                .andExpect(status().isBadRequest());
        }
    }

    /**
     * A shift that ends when it starts is refused rather than stored: it could mean nothing or the whole day, and the
     * roster rule answers "not on duty" for it, so the row would look like cover and grant nothing. A 24-hour duty is
     * two shifts, day and night.
     */
    @Test
    @Transactional
    void createShiftRefusesAShiftThatIsNotAWindow() throws Exception {
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);
        shiftDTO.setEndsAt(DEFAULT_STARTS_AT);

        restShiftMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void getAllShifts() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);

        restShiftMockMvc
            .perform(get(ENTITY_API_URL + "?sort=shiftDate,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shift.getId().intValue())))
            .andExpect(jsonPath("$.[*].shiftDate").value(hasItem(DEFAULT_SHIFT_DATE.toString())))
            .andExpect(jsonPath("$.[*].shiftType").value(hasItem(DEFAULT_SHIFT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)));
    }

    @Test
    @Transactional
    void getShift() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);

        restShiftMockMvc
            .perform(get(ENTITY_API_URL_ID, shift.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(shift.getId().intValue()))
            .andExpect(jsonPath("$.shiftDate").value(DEFAULT_SHIFT_DATE.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE));
    }

    @Test
    @Transactional
    void getNonExistingShift() throws Exception {
        restShiftMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putShift() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);

        Shift updated = shiftRepository.findById(shift.getId()).orElseThrow();
        em.detach(updated);
        updated.shiftDate(UPDATED_SHIFT_DATE).note(UPDATED_NOTE);
        ShiftDTO shiftDTO = shiftMapper.toDto(updated);

        restShiftMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shiftDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO))
            )
            .andExpect(status().isOk());

        Shift stored = shiftRepository.findById(shift.getId()).orElseThrow();
        assertThat(stored.getShiftDate()).isEqualTo(UPDATED_SHIFT_DATE);
        assertThat(stored.getNote()).isEqualTo(UPDATED_NOTE);
    }

    @Test
    @Transactional
    void putNonExistingShift() throws Exception {
        shift.setId(longCount.incrementAndGet());
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        restShiftMockMvc
            .perform(put(ENTITY_API_URL_ID, shiftDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void putWithIdMismatchShift() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);
        shiftDTO.setId(longCount.incrementAndGet());

        restShiftMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shiftDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(shiftDTO))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShift() throws Exception {
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        restShiftMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Transactional
    void partialUpdateShiftWithPatch() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);

        Shift partialUpdatedShift = new Shift();
        partialUpdatedShift.setId(shift.getId());
        partialUpdatedShift.setNote(UPDATED_NOTE);

        restShiftMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShift.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedShift))
            )
            .andExpect(status().isOk());

        Shift stored = shiftRepository.findById(shift.getId()).orElseThrow();
        assertThat(stored.getNote()).isEqualTo(UPDATED_NOTE);
        assertThat(stored.getShiftDate()).as("a field the patch did not mention is left alone").isEqualTo(DEFAULT_SHIFT_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingShift() throws Exception {
        shift.setId(longCount.incrementAndGet());
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        restShiftMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shiftDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shiftDTO))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void patchWithIdMismatchShift() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);
        shiftDTO.setId(longCount.incrementAndGet());

        restShiftMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shiftDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(shiftDTO))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShift() throws Exception {
        ShiftDTO shiftDTO = shiftMapper.toDto(shift);

        restShiftMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isMethodNotAllowed());
    }

    /**
     * The roster records who wrote it. A request that names somebody else's account is not refused: it is ignored,
     * because the only trustworthy source for that field is the authenticated caller.
     */
    @Test
    @Transactional
    void theAuthorIsTheCallerAndNotTheRequestBody() throws Exception {
        User somebodyElse = UserResourceIT.createEntity();
        em.persist(somebodyElse);
        em.flush();

        ShiftDTO requested = shiftMapper.toDto(shift);
        UserDTO impostor = new UserDTO();
        impostor.setId(somebodyElse.getId());
        impostor.setLogin(somebodyElse.getLogin());
        requested.setCreatedBy(impostor);

        ShiftDTO created = om.readValue(
            restShiftMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(requested)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ShiftDTO.class
        );

        Shift stored = shiftRepository.findById(created.getId()).orElseThrow();
        insertedShift = stored;
        assertThat(stored.getCreatedBy().getLogin())
            .as("the caller wrote it, whatever the body said")
            .isEqualTo("admin")
            .isNotEqualTo(somebodyElse.getLogin());
    }

    /**
     * An edit re-writes the shift, not who wrote it. Asserted by editing as a different account and checking the
     * stored author has not moved: the alternative is a roster whose history can be taken over by whoever touches the
     * row last.
     */
    @Test
    @Transactional
    @WithMockUser(value = "staff-hr", authorities = AuthoritiesConstants.SUPER_ADMIN)
    void anEditDoesNotTakeOverTheAuthor() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);
        Long authorBefore = shift.getCreatedBy().getId();

        Shift updated = shiftRepository.findById(shift.getId()).orElseThrow();
        em.detach(updated);
        updated.note("Corrected by a different planner");
        ShiftDTO shiftDTO = shiftMapper.toDto(updated);
        UserDTO theCaller = new UserDTO();
        theCaller.setLogin("staff-hr");
        shiftDTO.setCreatedBy(theCaller);

        restShiftMockMvc
            .perform(put(ENTITY_API_URL_ID, shiftDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(shiftDTO)))
            .andExpect(status().isOk());

        assertThat(shiftRepository.findById(shift.getId()).orElseThrow().getCreatedBy().getId())
            .as("the author is unchanged")
            .isEqualTo(authorBefore);
    }

    /**
     * There is no delete route, deliberately: the roster is the record of who was on duty, and a day removed from it
     * is a day edited out of what happened. A shift that is wrong is corrected.
     */
    @Test
    @Transactional
    void thereIsNoRouteThatRemovesADayFromTheRoster() throws Exception {
        insertedShift = shiftRepository.saveAndFlush(shift);

        restShiftMockMvc.perform(delete(ENTITY_API_URL_ID, shift.getId())).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.NURSE)
    void aNurseNeitherReadsTheRosterNorWritesToIt() throws Exception {
        restShiftMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isForbidden());
        restShiftMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    /**
     * Administration runs the rota, so it reads it. It does not write it: the raw writes are the escape hatch, and
     * the roster is about to become the source of truth for who may see a ward's patients.
     */
    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.ADMIN)
    void administrationReadsTheRosterButDoesNotWriteIt() throws Exception {
        restShiftMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        restShiftMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.HR)
    void humanResourcesReadsTheRosterButDoesNotWriteIt() throws Exception {
        restShiftMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        restShiftMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }
}
