package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.StaffRecordRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.ArrayList;
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
 * The staff file: Phase 3's S3.8, and the table every Phase 4 slice hangs off.
 *
 * <p>Two things are asserted here rather than assumed, because both are the reason the record exists. The first is
 * that a member of staff need not have an account: a cleaner or a records clerk is a person on the staff file and
 * nothing else, and a model that demanded a login would leave them out of the roster and the payroll. The second is
 * that a person cannot be on file twice, by identity number or by account, which is what the unique constraints and
 * the two refusals in the service are for.
 *
 * <p>The absence of a delete route is asserted too. It is a requirement — a staff record is employment history and
 * the parent of Phase 4's rows, so somebody who has left is {@code TERMINATED} — and a requirement nothing tests is
 * a requirement that comes back.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "staff-hr", authorities = AuthoritiesConstants.HR)
class StaffRecordIT {

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRecordRepository staffRecordRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private Department department;

    private User anAccount;

    private final List<Long> recorded = new ArrayList<>();

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Staff IT " + SUFFIX);
        department.setCode("SIT" + SUFFIX);
        department.setActive(true);
        department = departmentRepository.save(department);

        anAccount = userRepository.findOneByLogin("user").orElseThrow();
    }

    @AfterEach
    void cleanup() {
        // Deleted by id rather than relying on a rollback: the file is a new table and leaving rows behind in a
        // container that other suites reuse would make the next run's duplicate checks pass for the wrong reason.
        recorded.forEach(staffRecordRepository::deleteById);
        recorded.clear();
        departmentRepository.deleteById(department.getId());
    }

    @Test
    void hRRecordsAMemberOfStaffWhoHasNoAccount() throws Exception {
        String nationalId = "ID" + SUFFIX;

        long id = created(aStaffRecord("Grace Wanjiru " + SUFFIX, nationalId, StaffRecordStatus.ACTIVE, null));

        StaffRecord stored = staffRecordRepository.findOneByNationalId(nationalId).orElseThrow();
        assertThat(stored.getUser())
            .as("a cleaner or a records clerk is a member of staff and nothing else")
            .isNull();
        assertThat(stored.getEmploymentStartDate()).as("the leave entitlement will count from here").isNotNull();

        mockMvc
            .perform(get("/api/staff-records/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Grace Wanjiru " + SUFFIX))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void theSamePersonCannotBeOnFileTwice() throws Exception {
        String nationalId = "ID-DUP-" + SUFFIX;
        created(aStaffRecord("John Otieno " + SUFFIX, nationalId, StaffRecordStatus.ACTIVE, null));

        mockMvc
            .perform(
                post("/api/staff-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(aStaffRecord("J. Otieno " + SUFFIX, nationalId, StaffRecordStatus.ACTIVE, null))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.nationalIdAlreadyRecorded"))
            .andExpect(jsonPath("$.params").value("staffRecord"));

        assertThat(staffRecordRepository.findOneByNationalId(nationalId))
            .as("and the first record is still the only one")
            .isPresent();
    }

    @Test
    void anAccountCannotBeTheLoginForTwoPeople() throws Exception {
        created(aStaffRecord("Mary Achieng " + SUFFIX, "ID-A-" + SUFFIX, StaffRecordStatus.ACTIVE, anAccount.getId()));

        mockMvc
            .perform(
                post("/api/staff-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(aStaffRecord("Someone Else " + SUFFIX, "ID-B-" + SUFFIX, StaffRecordStatus.ACTIVE, anAccount.getId()))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.userAlreadyOnStaffRecord"));
    }

    @Test
    void somebodyWhoHasLeftIsTerminatedRatherThanRemoved() throws Exception {
        long id = created(aStaffRecord("Peter Kamau " + SUFFIX, "ID-T-" + SUFFIX, StaffRecordStatus.ACTIVE, null));

        mockMvc
            .perform(
                patch("/api/staff-records/{id}", id)
                    .contentType("application/merge-patch+json")
                    // The id travels in the body as well as the path: a partial update refuses a body whose id is
                    // absent, which is what stops a merge into one record being applied to another.
                    .content("{\"id\":" + id + ",\"status\":\"TERMINATED\"}")
            )
            .andExpect(status().isOk());

        assertThat(staffRecordRepository.findById(id).orElseThrow().getStatus())
            .as("the record survives, and says plainly that they are no longer here")
            .isEqualTo(StaffRecordStatus.TERMINATED);
    }

    /**
     * The route does not exist, and that is the requirement: leaving it out is what stops a staff record being
     * removed, where a route that refused would only invite the caller to keep trying.
     */
    @Test
    void thereIsNoRouteThatDeletesAMemberOfStaff() throws Exception {
        long id = created(aStaffRecord("Anne Njeri " + SUFFIX, "ID-DEL-" + SUFFIX, StaffRecordStatus.ACTIVE, null));

        mockMvc.perform(delete("/api/staff-records/{id}", id)).andExpect(status().isMethodNotAllowed());

        assertThat(staffRecordRepository.existsById(id)).as("and nothing was removed by it").isTrue();
    }

    @Test
    @WithMockUser(value = "anyone", authorities = "ROLE_USER")
    void theStaffFileIsNotOpenToEverySignedInAccount() throws Exception {
        // A non-clinical file with identity numbers and contact details in it: signing in is not a reason to read it.
        mockMvc.perform(get("/api/staff-records")).andExpect(status().isForbidden());
        mockMvc
            .perform(
                post("/api/staff-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(aStaffRecord("Not Allowed " + SUFFIX, "ID-N-" + SUFFIX, StaffRecordStatus.ACTIVE, null))
            )
            .andExpect(status().isForbidden());
    }

    private long created(String body) throws Exception {
        String location = mockMvc
            .perform(post("/api/staff-records").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");
        assertThat(location).as("a created record says where it is").isNotNull();
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
        recorded.add(id);
        return id;
    }

    private String aStaffRecord(String fullName, String nationalId, StaffRecordStatus status, Long accountId) {
        return (
            "{" +
            "\"fullName\":\"" +
            fullName +
            "\"," +
            "\"nationalId\":\"" +
            nationalId +
            "\"," +
            "\"jobTitle\":\"Records clerk\"," +
            "\"contactPhone\":\"+254700000000\"," +
            "\"contactEmail\":\"staff@localhost\"," +
            "\"employmentStartDate\":\"2024-01-15\"," +
            "\"status\":\"" +
            status +
            "\"," +
            "\"department\":{\"id\":" +
            department.getId() +
            "}" +
            (accountId == null ? "" : ",\"user\":{\"id\":" + accountId + "}") +
            "}"
        );
    }
}
