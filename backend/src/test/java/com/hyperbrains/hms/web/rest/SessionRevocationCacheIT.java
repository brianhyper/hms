package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Ending a live session when the account it belongs to was already read.
 *
 * <p><strong>Deliberately not {@code @Transactional}, and that is the entire point of the class.</strong>
 * {@code SessionRevocationIT} does the same thing inside one test transaction, and there the cached account and
 * the deactivation share one persistence context: the cache hands back the very object {@code updateUser} mutated,
 * so a stale cache cannot show itself. Here each step commits, so the switch-off happens in a persistence context
 * of its own — which is what a second request, a second node or a real deployment looks like.
 *
 * <p>Revocation reads the account through {@code findOneWithAuthoritiesByLogin}, which is served from the
 * {@code usersByLogin} cache with a one-hour time to live. If that read is what decides whether somebody switched
 * off is still working, then for up to an hour the answer can be "yes" — in exactly the case the control exists
 * for: an account switched off during an investigation with a session open on a ward machine.
 *
 * <p>The idle timeout's stamp is read straight against the row for this reason, so the two halves of the same
 * filter should not disagree about where they get their facts.
 */
@IntegrationTest
@AutoConfigureMockMvc
class SessionRevocationCacheIT {

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

    private final List<Long> created = new ArrayList<>();

    @AfterEach
    void cleanup() {
        created.forEach(userRepository::deleteById);
        created.clear();
    }

    @Test
    void aDeactivatedAccountIsRefusedEvenThoughItsAccountWasAlreadyRead() throws Exception {
        User account = account("cached-off");
        String token = tokenFor(account);

        // This is what warms the cache, and it is not a contrivance: it is the sign-in that happened before the
        // account was switched off.
        call(token).andExpect(status().isOk());

        switchOnOrOff(account, false);

        call(token).andExpect(status().isUnauthorized());
    }

    /**
     * The half of deactivation that has to let somebody back in, asserted here because it is the same read: if the
     * filter is answering from a cached account, a reactivated one looks switched off and nobody can sign in.
     */
    @Test
    void aTokenIssuedAfterReactivationWorks() throws Exception {
        User account = account("cached-back");
        call(tokenFor(account)).andExpect(status().isOk());

        switchOnOrOff(account, false);
        switchOnOrOff(account, true);

        User reloaded = userRepository.findOneByLogin(account.getLogin()).orElseThrow();
        assertThat(reloaded.isActivated()).as("the account is active again").isTrue();

        call(tokenFor(account)).andExpect(status().isOk());
    }

    private ResultActions call(String token) throws Exception {
        return mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private void switchOnOrOff(User account, boolean activated) {
        // The fetch-join read, because this class is deliberately outside a transaction and an AdminUserDTO is built
        // from the account's authorities: a plain findOneByLogin leaves them as a lazy collection with no session.
        AdminUserDTO request = new AdminUserDTO(userRepository.findOneWithAuthoritiesByLogin(account.getLogin()).orElseThrow());
        request.setActivated(activated);
        userService.updateUser(request);
    }

    private String tokenFor(User account) {
        return JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, account.getLogin());
    }

    private User account(String purpose) {
        String login = purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("admin"));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        User saved = userRepository.saveAndFlush(user);
        created.add(saved.getId());
        return saved;
    }
}
