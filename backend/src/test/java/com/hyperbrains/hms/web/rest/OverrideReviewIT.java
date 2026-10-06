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
 * Who may read the break-glass review. It is every override in the hospital, so it belongs to Administration and to
 * nobody else — the pharmacist who may grant one does not get to read them all.
 */
@IntegrationTest
@AutoConfigureMockMvc
class OverrideReviewIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.ADMIN)
    void administrationMayReadEveryOverride() throws Exception {
        mockMvc.perform(get("/api/overrides")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.SUPER_ADMIN)
    void theSuperAdminMayReadItToo() throws Exception {
        mockMvc.perform(get("/api/overrides")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.PHARMACY)
    void theCounterMayNotReviewThem() throws Exception {
        mockMvc.perform(get("/api/overrides")).andExpect(status().isForbidden());
    }
}
