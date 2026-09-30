package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.jwt.JwtAuthenticationTestUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
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
 * Ending a session that has been left alone.
 *
 * <p>Phase 3 fixes the window at thirty minutes. As with revocation, these call a real endpoint with a genuinely
 * signed token rather than testing the arithmetic: a unit test of {@code SessionIdle} would pass with the filter
 * unwired, and the point of the control is that a request is actually refused.
 *
 * <p>An idle session is one whose token was issued long enough ago and which has recorded no activity since, so
 * the tokens here are issued in the past and are still valid. That is the shape of the real thing: the token is
 * unchanged, nobody used it, and time passed.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class SessionIdleTimeoutIT {

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

    @Test
    void aSessionThatKeptBeingUsedIsNotIdle() throws Exception {
        User account = account("used");
        recordActivity(account, Instant.now().minus(Duration.ofMinutes(1)));

        call(tokenIssuedAgo(account)).andExpect(status().isOk());
    }

    @Test
    void aTokenIssuedLongAgoWithNothingSinceIsRefused() throws Exception {
        User account = account("cold");

        call(tokenIssuedAgo(account))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorKey").value("sessionIdle"));
    }

    @Test
    void activityOlderThanTheWindowDoesNotKeepTheSessionAlive() throws Exception {
        User account = account("stale");
        recordActivity(account, Instant.now().minus(Duration.ofMinutes(45)));

        call(tokenIssuedAgo(account)).andExpect(status().isUnauthorized());
    }

    @Test
    void aRefusedSessionIsNotBroughtBackToLifeByTheRefusal() throws Exception {
        User account = account("refused");
        String token = tokenIssuedAgo(account);

        call(token).andExpect(status().isUnauthorized());
        assertThat(userRepository.findLastActivityAtByLogin(account.getLogin()))
            .as("a refused request must not count as activity, or the timeout could only ever fire once")
            .isEmpty();
        call(token).andExpect(status().isUnauthorized());
    }

    /**
     * The case the token's own issue time exists for. Signing in again has to work even though the last activity
     * recorded against the account is from the session that timed out.
     */
    @Test
    void signingInAgainAfterBeingIdleWorks() throws Exception {
        User account = account("returning");
        recordActivity(account, Instant.now().minus(Duration.ofHours(2)));

        call(JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, account.getLogin())).andExpect(status().isOk());
    }

    @Test
    void activityIsRecordedAtMostOncePerInterval() throws Exception {
        User account = account("recorded");
        String token = JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, account.getLogin());

        call(token).andExpect(status().isOk());
        Instant recorded = userRepository.findLastActivityAtByLogin(account.getLogin()).orElseThrow();

        call(token).andExpect(status().isOk());
        assertThat(userRepository.findLastActivityAtByLogin(account.getLogin()))
            .as("a second request inside the interval does not write again")
            .contains(recorded);
    }

    private ResultActions call(String token) throws Exception {
        return mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    /** A token issued long enough ago that a session using it would be idle, and still valid for hours. */
    private String tokenIssuedAgo(User account) {
        return JwtAuthenticationTestUtils.createValidTokenForUserIssuedAt(
            jwtKey,
            account.getLogin(),
            Instant.now().minus(Duration.ofMinutes(45)),
            Duration.ofHours(6)
        );
    }

    private void recordActivity(User account, Instant at) {
        userRepository.recordActivityAt(account.getLogin(), at);
    }

    private User account(String purpose) {
        String login = "idle-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("admin"));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
