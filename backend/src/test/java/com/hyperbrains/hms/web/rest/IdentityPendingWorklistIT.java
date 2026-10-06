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
 * Who may read the pending-identity worklist.
 *
 * <p>The list carries patient names and hospital identifiers, so signing in is not a reason to read it. The desk
 * that chases the missing documents and Administration that oversees the backlog may; nobody else.
 */
@IntegrationTest
@AutoConfigureMockMvc
class IdentityPendingWorklistIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(value = "reception", authorities = AuthoritiesConstants.RECEPTION)
    void receptionMayReadTheWorklist() throws Exception {
        mockMvc.perform(get("/api/patient-registration/identity-pending")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.ADMIN)
    void administrationMayReadTheWorklist() throws Exception {
        mockMvc.perform(get("/api/patient-registration/identity-pending")).andExpect(status().isOk());
    }

    /** A clinical role is signed in, but the worklist is not a clinical record. */
    @Test
    @WithMockUser(value = "nurse", authorities = AuthoritiesConstants.NURSE)
    void theWorklistIsNotOpenToEverySignedInRole() throws Exception {
        mockMvc.perform(get("/api/patient-registration/identity-pending")).andExpect(status().isForbidden());
    }
}
