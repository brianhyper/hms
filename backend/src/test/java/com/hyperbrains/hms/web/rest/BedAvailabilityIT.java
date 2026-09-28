package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.dto.view.BedAvailabilityViewDTO;
import com.hyperbrains.hms.service.dto.view.WardOccupancyViewDTO;
import com.hyperbrains.hms.service.workflow.BedAvailabilityService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for what the ward screens read: which beds may be given to a patient, and how full
 * each ward is.
 *
 * <p>The distinction the whole class exists for is capacity versus offerable. A closed ward's beds are
 * physically there and still marked {@code AVAILABLE}, so they are counted — a ward does not lose its
 * beds by being closed — but not one of them may be handed to a patient. Counting and offering are
 * therefore asserted separately, because a single "available beds" number that does both is how a
 * patient ends up in a ward that has been shut.
 *
 * <p>The fixtures are deliberately one bed per status, so a status accidentally treated as assignable
 * shows up as an extra row rather than as a number that could be explained away.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = "ROLE_NURSE")
class BedAvailabilityIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private BedAvailabilityService bedAvailabilityService;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private BedTypeRepository bedTypeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private MockMvc mockMvc;

    private Department department;

    private Ward openWard;

    private Ward closedWard;

    private Ward emptyWard;

    private BedType general;

    private BedType highDependency;

    private Bed available;

    private Bed occupied;

    private Bed cleaning;

    private Bed maintenance;

    private Bed availableAtOverride;

    private Bed availableButClosed;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Bed Availability IT " + SUFFIX);
        department.setCode("BAIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        openWard = ward("Open Ward " + SUFFIX, true);
        closedWard = ward("Closed Ward " + SUFFIX, false);
        emptyWard = ward("Empty Ward " + SUFFIX, true);

        general = bedType("GENERAL " + SUFFIX, new BigDecimal("3500.00"));
        highDependency = bedType("HDU " + SUFFIX, new BigDecimal("8000.00"));

        available = bed(openWard, general, "A1", BedStatus.AVAILABLE, null);
        occupied = bed(openWard, general, "A2", BedStatus.OCCUPIED, null);
        cleaning = bed(openWard, highDependency, "A3", BedStatus.CLEANING, null);
        maintenance = bed(openWard, general, "A4", BedStatus.MAINTENANCE, null);
        availableAtOverride = bed(openWard, highDependency, "A5", BedStatus.AVAILABLE, new BigDecimal("9900.00"));
        availableButClosed = bed(closedWard, general, "B1", BedStatus.AVAILABLE, null);
    }

    @AfterEach
    void cleanup() {
        bedsInThisTestsWards().forEach(bed -> bedRepository.deleteById(bed.getId()));
        List.of(openWard, closedWard, emptyWard).forEach(ward -> wardRepository.deleteById(ward.getId()));
        List.of(general, highDependency).forEach(type -> bedTypeRepository.deleteById(type.getId()));
        departmentRepository.deleteById(department.getId());
    }

    // ---------------------------------------------------------------- what may be offered

    @Test
    void onlyBedsThatAreAvailableInAnOpenWardAreOffered() {
        assertThat(offeredBedIds(openWard)).containsExactlyInAnyOrder(available.getId(), availableAtOverride.getId());
    }

    @Test
    void aClosedWardsBedsAreNeverOfferedEvenThoughTheyAreFree() {
        assertThat(offeredBedIds(closedWard)).isEmpty();
    }

    @Test
    void aWardWithNoBedsOffersNothing() {
        assertThat(offeredBedIds(emptyWard)).isEmpty();
    }

    /** A bed a patient has just left is not offered until somebody says it is clean. */
    @Test
    void aBedBeingCleanedIsNotOffered() {
        assertThat(offeredBedIds(openWard)).doesNotContain(cleaning.getId(), maintenance.getId(), occupied.getId());
    }

    // ---------------------------------------------------------------- narrowing the list

    @Test
    void theListCanBeNarrowedToOneBedType() {
        List<BedAvailabilityViewDTO> offered = bedAvailabilityService.findAssignable(null, highDependency.getId());

        assertThat(offered.stream().map(BedAvailabilityViewDTO::bedId)).contains(availableAtOverride.getId());
        assertThat(offered.stream().map(BedAvailabilityViewDTO::bedId))
            .as("the other HDU bed in this ward is being cleaned")
            .doesNotContain(cleaning.getId());
    }

    @Test
    void bothFiltersApplyTogether() {
        List<BedAvailabilityViewDTO> offered = bedAvailabilityService.findAssignable(openWard.getId(), general.getId());

        assertThat(offered.stream().map(BedAvailabilityViewDTO::bedId)).containsExactly(available.getId());
    }

    // ---------------------------------------------------------------- the rate shown

    @Test
    void aBedInheritsItsTypesRateUnlessItHasItsOwn() {
        List<BedAvailabilityViewDTO> offered = bedAvailabilityService.findAssignable(openWard.getId(), null);

        BedAvailabilityViewDTO inherited = offered.stream().filter(row -> row.bedId().equals(available.getId())).findFirst().orElseThrow();
        assertThat(inherited.dailyRate()).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(inherited.rateComesFromTheBed()).isFalse();
        assertThat(inherited.bedTypeName()).isEqualTo(general.getName());

        BedAvailabilityViewDTO overridden = offered
            .stream()
            .filter(row -> row.bedId().equals(availableAtOverride.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(overridden.dailyRate()).isEqualByComparingTo(new BigDecimal("9900.00"));
        assertThat(overridden.rateComesFromTheBed()).isTrue();
    }

    // ---------------------------------------------------------------- capacity

    @Test
    void capacityIsCountedFromTheBedsAndSplitByStatus() {
        WardOccupancyViewDTO open = occupancyOf(openWard);

        assertThat(open.totalBeds()).isEqualTo(5);
        assertThat(open.availableBeds()).isEqualTo(2);
        assertThat(open.occupiedBeds()).isEqualTo(1);
        assertThat(open.cleaningBeds()).isEqualTo(1);
        assertThat(open.maintenanceBeds()).isEqualTo(1);
        assertThat(open.active()).isTrue();
        assertThat(open.departmentName()).isEqualTo(department.getName());
    }

    @Test
    void aWardWithNoBedsIsListedAndReportsZero() {
        WardOccupancyViewDTO empty = occupancyOf(emptyWard);

        assertThat(empty.totalBeds()).isZero();
        assertThat(empty.availableBeds()).isZero();
        assertThat(empty.occupiedBeds()).isZero();
        assertThat(empty.cleaningBeds()).isZero();
        assertThat(empty.maintenanceBeds()).isZero();
    }

    /** A closed ward keeps its beds; what it cannot do is have them given out. */
    @Test
    void aClosedWardStillCountsItsBeds() {
        WardOccupancyViewDTO closed = occupancyOf(closedWard);

        assertThat(closed.active()).isFalse();
        assertThat(closed.totalBeds()).isEqualTo(1);
        assertThat(closed.availableBeds()).isEqualTo(1);
        assertThat(offeredBedIds(closedWard)).as("counted, but not offered").isEmpty();
        assertThat(availableButClosed.getId()).isNotNull();
    }

    @Test
    void everyWardIsListedIncludingTheOnesWithNothingInThem() {
        List<Long> wardIds = bedAvailabilityService.wardOccupancy().stream().map(WardOccupancyViewDTO::wardId).toList();

        assertThat(wardIds).contains(openWard.getId(), closedWard.getId(), emptyWard.getId());
    }

    // ---------------------------------------------------------------- over HTTP, and who may ask

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
    void theDeskMayAskWhichBedsAreFree() throws Exception {
        mockMvc
            .perform(get("/api/bed-availability").param("wardId", String.valueOf(openWard.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].bedId").exists());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_ADMIN")
    void wardOccupancyIsReadableOverHttp() throws Exception {
        mockMvc.perform(get("/api/bed-availability/wards")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_PHARMACY")
    void aPharmacistMayNotAskWhichBedsAreFree() throws Exception {
        mockMvc.perform(get("/api/bed-availability")).andExpect(status().isForbidden());
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

    private Bed bed(Ward ward, BedType type, String number, BedStatus status, BigDecimal override) {
        Bed bed = new Bed();
        bed.setBedNumber(SUFFIX + "-" + number);
        bed.setStatus(status);
        bed.setDailyRateOverride(override);
        bed.setWard(ward);
        bed.setBedType(type);
        return bedRepository.save(bed);
    }

    private List<Long> offeredBedIds(Ward ward) {
        return bedAvailabilityService
            .findAssignable(ward.getId(), null)
            .stream()
            .map(BedAvailabilityViewDTO::bedId)
            .toList();
    }

    private WardOccupancyViewDTO occupancyOf(Ward ward) {
        return bedAvailabilityService
            .wardOccupancy()
            .stream()
            .filter(row -> row.wardId().equals(ward.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("ward " + ward.getName() + " is missing from the occupancy list"));
    }

    private List<Bed> bedsInThisTestsWards() {
        List<Long> wardIds = List.of(openWard.getId(), closedWard.getId(), emptyWard.getId());
        return bedRepository
            .findAllWithWardAndBedType()
            .stream()
            .filter(bed -> bed.getWard() != null && wardIds.contains(bed.getWard().getId()))
            .toList();
    }
}
