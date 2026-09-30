package com.hyperbrains.hms.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Who may manage accounts and roles.
 *
 * <p>Phase 3 splits the hospital's administration from the system's: Administration runs wards, beds,
 * transfers and reports, and only Super Admin creates accounts and hands out roles. Both gates are asserted
 * here because both exist — the RBAC row for {@code /api/admin/**} and the annotation on each method — and a
 * change to one of them alone would look like a working system while letting the wrong role in through the
 * other.
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
}
