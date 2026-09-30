package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.dto.AdminUserDTO;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * What may be done to an account, over the routes that do it.
 *
 * <p>Phase 3's account rules are all about the last one: an account is never deleted, the system can never be
 * left with nobody who can manage accounts, and a Super Admin cannot take their own role away. Each is a rule
 * that only matters in the one case it forbids, so every refusal here is paired with the case that is allowed —
 * a rule that refuses everything would pass half of these tests and be useless.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(value = "acclife-actor", authorities = AuthoritiesConstants.SUPER_ADMIN)
class AccountLifecycleIT {

    private static final String ENTITY_API_URL = "/api/admin/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User manager;

    @BeforeEach
    void setUp() {
        manager = account("acclife-manager", "acclife-manager@localhost", AuthoritiesConstants.SUPER_ADMIN);
    }

    // ---------------------------------------------------------------- never deleted

    @Test
    void anAccountIsNeverDeleted() throws Exception {
        mockMvc
            .perform(delete(ENTITY_API_URL + "/{login}", manager.getLogin()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());

        assertThat(userRepository.findOneByLogin(manager.getLogin()))
            .as("the account survives, because orders and payments still point at it")
            .isPresent();
    }

    // ---------------------------------------------------------------- the last way in

    @Test
    void theLastAccountThatCanManageAccountsCannotBeDeactivated() throws Exception {
        AdminUserDTO request = deactivate(manager);

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isConflict());

        assertThat(userRepository.findById(manager.getId()).orElseThrow().isActivated())
            .as("and the refusal left it exactly as it was")
            .isTrue();
    }

    @Test
    void theLastAccountThatCanManageAccountsCannotLoseTheRole() throws Exception {
        AdminUserDTO request = new AdminUserDTO(manager);
        request.setAuthorities(Set.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isConflict());

        User reloaded = userRepository.findOneWithAuthoritiesByLogin(manager.getLogin()).orElseThrow();
        assertThat(reloaded.getAuthorities().stream().map(authority -> authority.getName()))
            .as("the role is still there")
            .contains(AuthoritiesConstants.SUPER_ADMIN);
    }

    @Test
    void oneOfTwoManagersCanBeDeactivated() throws Exception {
        // The positive control for the rule above: with a second way in, the same request is allowed.
        account("acclife-second", "acclife-second@localhost", AuthoritiesConstants.SUPER_ADMIN);
        AdminUserDTO request = deactivate(manager);

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isOk());

        assertThat(userRepository.findById(manager.getId()).orElseThrow().isActivated()).isFalse();
    }

    @Test
    @WithMockUser(value = "acclife-manager", authorities = AuthoritiesConstants.SUPER_ADMIN)
    void aSuperAdminCannotTakeTheirOwnRoleAway() throws Exception {
        account("acclife-second", "acclife-second@localhost", AuthoritiesConstants.SUPER_ADMIN);
        AdminUserDTO request = new AdminUserDTO(manager);
        request.setAuthorities(Set.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isConflict());

        User reloaded = userRepository.findOneWithAuthoritiesByLogin(manager.getLogin()).orElseThrow();
        assertThat(reloaded.getAuthorities().stream().map(authority -> authority.getName()))
            .as("the ability to give the role back is still there")
            .contains(AuthoritiesConstants.SUPER_ADMIN);
    }

    // ---------------------------------------------------------------- the trail

    @Test
    void aRoleChangeIsRecordedWithTheRolesBeforeAndAfter() throws Exception {
        account("acclife-second", "acclife-second@localhost", AuthoritiesConstants.SUPER_ADMIN);
        AdminUserDTO request = new AdminUserDTO(manager);
        request.setAuthorities(Set.of(AuthoritiesConstants.NURSE, AuthoritiesConstants.USER));

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isOk());

        List<AuditLog> trail = auditLogRepository.findByEntityNameAndEntityIdOrderByIdAsc(
            "User",
            String.valueOf(manager.getId())
        );

        assertThat(trail).anyMatch(entry ->
            AuditActions.USER_ROLE_CHANGED.equals(entry.getAction()) &&
            entry.getOldValue().contains(AuthoritiesConstants.SUPER_ADMIN) &&
            entry.getNewValue().contains(AuthoritiesConstants.NURSE)
        );
    }

    // ---------------------------------------------------------------- helpers

    private AdminUserDTO deactivate(User user) {
        AdminUserDTO request = new AdminUserDTO(user);
        request.setActivated(false);
        return request;
    }

    /** An account holding one hospital role, saved directly: the routes under test are the ones being tested. */
    private User account(String login, String email, String authority) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("admin"));
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(authority).orElseThrow());
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
