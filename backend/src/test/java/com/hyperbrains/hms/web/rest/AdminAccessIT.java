package com.hyperbrains.hms.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Who may manage accounts and roles, the price and clinical catalogues, and the audit trail.
 *
 * <p>Phase 3 splits the hospital's administration from the system's: Administration runs wards, beds,
 * transfers and reports, and only Super Admin creates accounts, hands out roles and edits the catalogue. Both
 * gates are asserted here because both exist — the RBAC row for {@code /api/admin/**} and the annotation on each
 * method — and a change to one of them alone would look like a working system while letting the wrong role in
 * through the other.
 */
@IntegrationTest
@AutoConfigureMockMvc
class AdminAccessIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
    void aSuperAdminManagesAccountsAndRoles() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/api/authorities")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void administrationNeitherManagesAnAccountNorHandsOutARole() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/authorities")).andExpect(status().isForbidden());
    }

    /**
     * S3.9. Prices and clinical catalogues decide what the hospital charges and what it can do, so they are Super
     * Admin's; Administration runs the hospital rather than editing the catalogue.
     *
     * <p>Asserted here because the five generated integration tests for these entities were re-annotated to Super
     * Admin when the rows were tightened, and a test that runs as the role that is still allowed would pass either
     * way — it would not notice the row admitting Administration again. The bodies are deliberately empty:
     * authorization happens before dispatch, so what is sent never reaches a validator.
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void administrationCannotEditTheCatalogue() throws Exception {
        List<String> catalogue = List.of(
            "/api/departments",
            "/api/diagnoses",
            "/api/hospital-services",
            "/api/lab-tests",
            "/api/radiology-exams"
        );
        for (String path : catalogue) {
            mockMvc
                .perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        }
    }

    /**
     * S3.3. The audit row used to be method-agnostic, so a write route appearing on that resource later would have
     * been admitted by it rather than refused. The trail is written by the application, never through the API.
     */
    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void theAuditTrailIsReadableByAdministrationAndNotWritableByAnybody() throws Exception {
        mockMvc.perform(get("/api/audit-logs")).andExpect(status().isOk());

        mockMvc
            .perform(post("/api/audit-logs").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/audit-logs/1")).andExpect(status().isForbidden());
    }
}
