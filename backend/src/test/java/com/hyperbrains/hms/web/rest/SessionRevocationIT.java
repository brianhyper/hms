package com.hyperbrains.hms.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.jwt.JwtAuthenticationTestUtils;
import com.hyperbrains.hms.service.UserService;
import com.hyperbrains.hms.service.dto.AdminUserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ending a session that is already open.
 *
 * <p>Blocking the next sign-in is not the same thing, and the difference is the case this exists for: an account
 * switched off while somebody is still signed in on a ward machine. These tests call a real endpoint with a
 * genuinely signed token, before and after the account is switched off, because a unit test of the rule would
 * pass whether or not anything ever consulted it.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class SessionRevocationIT {

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Test
    void aDeactivatedAccountsExistingTokenStopsWorking() throws Exception {
        User account = account("sess-actor", "sess-actor@localhost");
        String token = tokenFor(account);

        call(token).andExpect(status().isOk());

        switchOnOrOff(account, false);

        call(token).andExpect(status().isUnauthorized());
    }

    @Test
    void switchingOffOneAccountLeavesEverybodyElseAlone() throws Exception {
        // The positive control: this control has to refuse the deactivated account and nothing else, or it would
        // pass its own test while shutting the hospital out.
        User switchedOff = account("sess-off", "sess-off@localhost");
        User untouched = account("sess-on", "sess-on@localhost");
        String otherToken = tokenFor(untouched);

        switchOnOrOff(switchedOff, false);

        call(otherToken).andExpect(status().isOk());
    }

    /**
     * Reactivation is asserted in {@code SessionRevocationCacheIT}, and it is asserted there rather than here for a
     * reason worth keeping: this class runs inside one test transaction, so the cached account and the deactivation
     * share a persistence context and the failure cannot show itself. It was carried here for a while as a
     * {@code @Disabled} reproduction — "passes alone, fails after AccountLifecycleIT" — and it is now understood:
     * the token's {@code iat} is whole seconds while the stamp is stored with microseconds, so a sign-in in the
     * same second as the revocation compared as older than it and was refused. The control compares at the token's
     * own resolution now, and the case passes in any order.
     */

    private ResultActions call(String token) throws Exception {
        return mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private void switchOnOrOff(User account, boolean activated) {
        AdminUserDTO request = new AdminUserDTO(userRepository.findOneByLogin(account.getLogin()).orElseThrow());
        request.setActivated(activated);
        userService.updateUser(request);
    }

    private String tokenFor(User account) {
        return JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, account.getLogin());
    }

    private User account(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("admin"));
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
