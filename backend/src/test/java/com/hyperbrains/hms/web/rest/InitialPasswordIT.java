package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.UserService;
import com.hyperbrains.hms.service.dto.AdminUserDTO;
import com.hyperbrains.hms.service.dto.PasswordChangeDTO;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.jhipster.security.RandomUtil;

/**
 * Handing somebody a password, and everything that has to stop being true when you do.
 *
 * <p>This is the route that makes the forced change reachable. Until it existed, an account created by an
 * administrator was reached through a reset link and the requirement to replace the password was never exercised in
 * the flow it was written for — the mechanism closed a door on a case nothing could reach. Here the whole loop is
 * walked: hand over, sign in, be refused everything except changing it, change it, be let in.
 *
 * <p>Not transactional, because these are claims about what survives a sign-in: an audit row that has to be there
 * afterwards, and a session that has to be dead for the next request. Inside one test transaction both look the same
 * whether they were committed or not.
 */
@IntegrationTest
@AutoConfigureMockMvc
class InitialPasswordIT {

    private static final String REASON = "new member of staff, no password of their own yet";
    private static final String KNOWN_PASSWORD = "known-password-123456";
    private static final String THEIR_OWN_PASSWORD = "ward-notes-2026-stairs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper om;

    @Test
    void theHandedOverPasswordSignsInAndMustBeReplacedBeforeItDoesAnythingElse() throws Exception {
        User target = createdByAnAdministrator();
        String adminToken = signIn(admin());

        String handed = handOver(target, adminToken);

        String token = signIn(target.getLogin(), handed);
        mockMvc
            .perform(get("/api/wards").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorKey").value("passwordChangeRequired"));
        mockMvc
            .perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());

        mockMvc
            .perform(
                post("/api/account/change-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(handed, THEIR_OWN_PASSWORD)))
            )
            .andExpect(status().isOk());

        mockMvc
            .perform(get("/api/wards").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
        signIn(target.getLogin(), THEIR_OWN_PASSWORD);
        assertSignInRefused(target.getLogin(), handed);
    }

    /**
     * Handing somebody a password is how an account is taken from whoever was using it, so a session already open
     * with the old one has to be over — otherwise the change reaches the person it was aimed at and not the person
     * who had it open.
     */
    @Test
    void handingOverAPasswordEndsTheSessionsOpenWithTheOldOne() throws Exception {
        User target = account("sessions", KNOWN_PASSWORD, AuthoritiesConstants.NURSE);
        String stolen = signIn(target);
        String adminToken = signIn(admin());

        mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + stolen)).andExpect(status().isOk());

        // A token records its issue time in whole seconds and a stamp set inside that same second cannot be told
        // apart from it, so the comparison deliberately lets a token of that second through. Waiting past the second
        // is what makes this assertion exact rather than a race: the claim is that a session opened before the
        // handover is over afterwards, and this is the smallest gap at which "before" is knowable at all.
        Thread.sleep(1100);

        handOver(target, adminToken);

        mockMvc
            .perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + stolen))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorKey").value("sessionEnded"));
        assertSignInRefused(target.getLogin(), KNOWN_PASSWORD);
        // and the administrator who did it is unaffected
        mockMvc.perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)).andExpect(status().isOk());
    }

    /** The older way in has to stop being one, or the link sets the password and the handover never happened. */
    @Test
    void aResetLinkThatWasAlreadySentStopsWorking() {
        User target = account("pending-link", KNOWN_PASSWORD, AuthoritiesConstants.NURSE);
        String key = RandomUtil.generateResetKey();
        User row = userRepository.findOneByLogin(target.getLogin()).orElseThrow();
        row.setResetKey(key);
        row.setResetDate(Instant.now());
        userRepository.saveAndFlush(row);

        userService.setInitialPassword(target.getLogin(), REASON);

        assertThat(userService.completePasswordReset(THEIR_OWN_PASSWORD, key))
            .as("a link sent before the handover must not still be a way in")
            .isEmpty();
    }

    @Test
    void theAuditTrailSaysItHappenedAndWhyButNotWhatThePasswordWas() throws Exception {
        User target = createdByAnAdministrator();
        String adminToken = signIn(admin());

        String handed = handOver(target, adminToken);

        List<AuditLog> rows = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "User",
            String.valueOf(target.getId())
        );
        AuditLog row = rows
            .stream()
            .filter(it -> AuditActions.PASSWORD_CHANGED.equals(it.getAction()))
            .reduce((earlier, later) -> later)
            .orElseThrow();
        assertThat(row.getReason()).as("the decision is reviewable").isEqualTo(REASON);
        assertThat(rows)
            .as("the password itself must not reach the trail, in any field")
            .noneMatch(it ->
                String.valueOf(it.getDetails()).contains(handed) ||
                String.valueOf(it.getNewValue()).contains(handed) ||
                String.valueOf(it.getOldValue()).contains(handed)
            );
    }

    @Test
    void aReasonIsRequiredForTheSamePurposeAsReleasingALock() throws Exception {
        User target = createdByAnAdministrator();
        String adminToken = signIn(admin());

        mockMvc
            .perform(
                post("/api/admin/users/" + target.getLogin() + "/initial-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"  \"}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.initialPasswordReasonRequired"));
    }

    @Test
    void nobodyBelowSuperAdminCanHandOutPasswords() throws Exception {
        User target = createdByAnAdministrator();
        String nurseToken = signIn(account("nurse", KNOWN_PASSWORD, AuthoritiesConstants.NURSE));

        mockMvc
            .perform(
                post("/api/admin/users/" + target.getLogin() + "/initial-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + nurseToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"" + REASON + "\"}")
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void anUnknownLoginIsRefusedRatherThanSilentlyIgnored() throws Exception {
        String adminToken = signIn(admin());

        mockMvc
            .perform(
                post("/api/admin/users/nobody-here-at-all/initial-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"" + REASON + "\"}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.userNotFound"));
    }

    private String handOver(User target, String adminToken) throws Exception {
        MvcResult result = mockMvc
            .perform(
                post("/api/admin/users/" + target.getLogin() + "/initial-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"" + REASON + "\"}")
            )
            .andExpect(status().isOk())
            .andReturn();
        JsonNode body = om.readTree(result.getResponse().getContentAsString());
        String password = body.get("password").asText();
        assertThat(password).as("a usable password comes back, once").isNotBlank();
        return password;
    }

    /** An account as an administrator creates it: nobody has chosen this password, least of all its owner. */
    private User createdByAnAdministrator() {
        String login = "handed-over-" + UUID.randomUUID().toString().substring(0, 8);
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin(login);
        dto.setEmail(login + "@localhost");
        dto.setLangKey("en");
        dto.setAuthorities(java.util.Set.of(AuthoritiesConstants.NURSE));
        return userService.createUser(dto);
    }

    private User admin() {
        return account("admin", KNOWN_PASSWORD, AuthoritiesConstants.SUPER_ADMIN);
    }

    private User account(String purpose, String password, String authority) {
        String login = "initial-password-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(authority).orElseThrow());
        return userRepository.saveAndFlush(user);
    }

    private String signIn(User account) throws Exception {
        return signIn(account.getLogin(), KNOWN_PASSWORD);
    }

    /** Signs in for real and returns the token, failing loudly with the body if the sign-in is refused. */
    private String signIn(String login, String password) throws Exception {
        MvcResult result = authenticate(login, password);
        assertThat(result.getResponse().getStatus())
            .as("sign-in refused: " + result.getResponse().getContentAsString())
            .isEqualTo(200);
        return om.readTree(result.getResponse().getContentAsString()).get("id_token").asText();
    }

    private void assertSignInRefused(String login, String password) throws Exception {
        assertThat(authenticate(login, password).getResponse().getStatus()).as("this password must not sign in").isEqualTo(401);
    }

    private MvcResult authenticate(String login, String password) throws Exception {
        return mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + login + "\",\"password\":\"" + password + "\",\"rememberMe\":false}")
            )
            .andReturn();
    }
}
