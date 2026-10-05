package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.UserService;
import com.hyperbrains.hms.service.dto.AdminUserDTO;
import com.hyperbrains.hms.service.dto.PasswordChangeDTO;
import com.hyperbrains.hms.web.rest.vm.KeyAndPasswordVM;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Forcing a password that was chosen for somebody to be replaced before the account can be used.
 *
 * <p>Signed in for real, because this is a rule about the request that follows a sign-in, and because the state it
 * reads is read per request.
 *
 * <p>The test that matters most is not the refusal — it is {@link #settingYourOwnPasswordThroughTheResetLinkEndsTheRequirementToo}.
 * A reset link is how a new member of staff reaches their account at all, so a control that counted that password as
 * somebody else's would refuse every account a Super Admin creates, for ever, and the failure would look like a
 * policy working rather than a lockout.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class ForcedPasswordChangeIT {

    private static final String CHOSEN_PASSWORD = "correct-horse-battery-staple";

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

    @Autowired
    private ObjectMapper om;

    @Test
    void everythingExceptTheWayOutIsRefusedWhileThePasswordIsSomebodyElses() throws Exception {
        User account = account("forced", true);
        String token = signIn(account);

        mockMvc
            .perform(get("/api/wards").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorKey").value("passwordChangeRequired"));
    }

    @Test
    void readingYourOwnAccountIsStillAllowed() throws Exception {
        User account = account("reading", true);

        mockMvc
            .perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + signIn(account)))
            .andExpect(status().isOk());
    }

    @Test
    void choosingAPasswordOfYourOwnIsTheWayOut() throws Exception {
        User account = account("out", true);
        String token = signIn(account);
        String chosen = "ward-notes-2026-stairs";

        mockMvc
            .perform(
                post("/api/account/change-password")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(CHOSEN_PASSWORD, chosen)))
            )
            .andExpect(status().isOk());

        assertThat(account.isPasswordChangeRequired()).as("the requirement is over").isFalse();
        // and the account works normally from here
        mockMvc
            .perform(get("/api/wards").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
        assertThat(signIn(account, chosen)).as("with the password they chose").isNotBlank();
    }

    /** The reset link is how a new member of staff reaches their account, and the password set there is their own. */
    @Test
    void settingYourOwnPasswordThroughTheResetLinkEndsTheRequirementToo() throws Exception {
        User account = account("reset", true);
        String key = RandomUtil.generateResetKey();
        account.setResetKey(key);
        account.setResetDate(Instant.now());
        userRepository.saveAndFlush(account);

        KeyAndPasswordVM reset = new KeyAndPasswordVM();
        reset.setKey(key);
        reset.setNewPassword("ward-notes-2026-stairs");

        mockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reset))
            )
            .andExpect(status().isOk());

        assertThat(account.isPasswordChangeRequired())
            .as("nothing is owed once the person has chosen it themselves")
            .isFalse();
    }

    @Test
    void anAccountCreatedBySomebodyElseStartsWithTheRequirement() {
        AdminUserDTO created = new AdminUserDTO();
        created.setLogin("forced-created-" + UUID.randomUUID().toString().substring(0, 8));
        created.setEmail(created.getLogin() + "@localhost");
        created.setLangKey("en");

        User user = userService.createUser(created);

        assertThat(user.isPasswordChangeRequired()).as("the password was generated for them").isTrue();
    }

    private String signIn(User account) throws Exception {
        return signIn(account, CHOSEN_PASSWORD);
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

    private User account(String purpose, boolean passwordChangeRequired) {
        String login = "forced-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(CHOSEN_PASSWORD));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.setPasswordChangeRequired(passwordChangeRequired);
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
