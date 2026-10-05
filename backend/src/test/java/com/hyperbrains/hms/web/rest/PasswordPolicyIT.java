package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.dto.PasswordChangeDTO;
import com.hyperbrains.hms.service.rules.Passwords;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * What the system accepts as a password, over the route that actually sets one.
 *
 * <p>These sign in for real rather than running as a mock user. The login prohibition can only be applied where the
 * account is known, and the account is known because somebody signed in — a test that skipped that would not be
 * exercising the path the rule lives on, which is the pattern that let three defects hide on this side of the
 * application already.
 *
 * <p>Transactional so the accounts it creates and the audit rows the route writes roll back with it: the trail names
 * the actor, so an account with one cannot be deleted afterwards.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class PasswordPolicyIT {

    private static final String CURRENT_PASSWORD = "correct-horse-battery-staple";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper om;

    @Test
    void aPasswordShorterThanTheMinimumIsRefused() throws Exception {
        User account = account("short");
        String token = signIn(account, CURRENT_PASSWORD);

        changePassword(token, "s".repeat(Passwords.MIN_LENGTH - 1)).andExpect(status().isBadRequest());

        assertThat(passwordEncoder.matches(CURRENT_PASSWORD, account.getPassword()))
            .as("and the password they already had is untouched")
            .isTrue();
    }

    @Test
    void aPasswordThatIsTheAccountsOwnLoginIsRefused() throws Exception {
        User account = account("aslogin");
        String token = signIn(account, CURRENT_PASSWORD);

        changePassword(token, account.getLogin()).andExpect(status().isBadRequest());

        assertThat(passwordEncoder.matches(CURRENT_PASSWORD, account.getPassword())).isTrue();
    }

    @Test
    void aPasswordThatMeetsTheRuleIsAcceptedAndBecomesTheOneThatSignsIn() throws Exception {
        User account = account("accepted");
        String token = signIn(account, CURRENT_PASSWORD);
        String chosen = "ward-notes-2026-stairs";

        changePassword(token, chosen).andExpect(status().isOk());

        assertThat(signIn(account, chosen)).as("the new password is the one that works").isNotBlank();
        assertThat(passwordEncoder.matches(CURRENT_PASSWORD, account.getPassword())).as("and the old one is gone").isFalse();
    }

    private ResultActions changePassword(String token, String newPassword) throws Exception {
        return mockMvc.perform(
            post("/api/account/change-password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(new PasswordChangeDTO(CURRENT_PASSWORD, newPassword)))
        );
    }

    private String signIn(User account, String password) throws Exception {
        MvcResult result = mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + account.getLogin() + "\",\"password\":\"" + password + "\",\"rememberMe\":false}"
                    )
            )
            .andExpect(status().isOk())
            .andReturn();
        return om.readTree(result.getResponse().getContentAsString()).get("id_token").asText();
    }

    private User account(String purpose) {
        String login = "policy-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(CURRENT_PASSWORD));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
