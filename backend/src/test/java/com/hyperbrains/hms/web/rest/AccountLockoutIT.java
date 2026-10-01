package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SignInLockout;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.LoginAttemptListener;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Locking an account after repeated failures, and releasing it deliberately.
 *
 * <p>These sign in for real through {@code /api/authenticate}, because the counting hangs off Spring's own
 * authentication events: a test that called the listener directly would pass whether or not anything ever
 * published an event, which is the one failure mode worth excluding.
 *
 * <p>Each test signs in as an account of its own, with a login nothing else in the suite reaches. An earlier
 * version of this class shared the stock account and read back the lock state it left behind: the lock outlived
 * the test that set it, so whichever test ran first decided what the others saw and the class passed or failed
 * by execution order. A private account holds whatever the transaction does with the writes.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(authorities = AuthoritiesConstants.SUPER_ADMIN)
class AccountLockoutIT {

    private static final String PASSWORD = "correct-horse-battery-staple";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void fiveWrongPasswordsLockTheAccountAndTheRightOneStopsWorking() throws Exception {
        User account = newAccount();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD);

        signIn(account, PASSWORD).andExpect(status().isUnauthorized());

        User locked = reload(account);
        assertThat(locked.getLockedAt()).as("the account is locked").isNotNull();
        assertThat(locked.getFailedAttempts())
            .as("and further attempts are not counted against an account that is already locked")
            .isEqualTo(LoginAttemptListener.LOCKOUT_THRESHOLD);
    }

    @Test
    void theRefusalReadsTheSameAsTheRefusalForAWrongPassword() throws Exception {
        // The locked answer is deliberately indistinguishable from the answer to a wrong password. Whoever is
        // asking is by definition not signed in, and "this account is locked" would confirm to them that the
        // account exists and that somebody has been hammering it, which is the signal credential stuffing looks
        // for. Asserted rather than trusted, because the detail that reaches the caller is built from the
        // exception's own message -- which is the piece that was getting this wrong.
        User account = newAccount();

        String wrongPasswordAnswer = signIn(account, "definitely-not-the-password")
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD - 1);
        String lockedAnswer = signIn(account, PASSWORD)
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(lockedAnswer).as("the answer for a locked account does not mention the lock").doesNotContainIgnoringCase("lock");
        assertThat(lockedAnswer)
            .as("and nobody can tell it apart from the answer for a wrong password")
            .isEqualTo(wrongPasswordAnswer);
    }

    @Test
    void anAccountThatWasNeverActivatedIsRefusedRatherThanCrashing() throws Exception {
        // The precedent this lockout was meant to follow. An account that exists but is not usable should be
        // refused, and if this ever answers 500 then the same shape of bug is live in shipped code — so it is
        // asserted rather than assumed, since the whole reason for the assertion is that nobody was sure. It did
        // answer 500: the exception raised for a not-yet-activated account was wrapped by Spring before the error
        // handler saw it, and that wrapper carries no status of its own. This assertion is what found it.
        User neverActivated = newAccount();
        neverActivated.setActivated(false);
        userRepository.saveAndFlush(neverActivated);

        signIn(neverActivated, PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void oneShortOfTheThresholdStillSignsInAndClearsTheCount() throws Exception {
        // The positive control. Without it, a rule that locked everyone out would pass the test above.
        User account = newAccount();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD - 1);

        signIn(account, PASSWORD).andExpect(status().isOk());

        User user = reload(account);
        assertThat(user.getLockedAt()).isNull();
        assertThat(user.getFailedAttempts()).as("a success clears the count, so typos months apart never add up").isZero();
    }

    @Test
    void aSuperAdminReleasesTheLockWithAReasonAndThePasswordWorksAgain() throws Exception {
        User account = newAccount();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD);
        signIn(account, PASSWORD).andExpect(status().isUnauthorized());

        mockMvc
            .perform(
                post("/api/admin/users/{login}/unlock", account.getLogin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"Staff member confirmed it was them, at the desk\"}")
            )
            .andExpect(status().isOk());

        signIn(account, PASSWORD).andExpect(status().isOk());
        assertThat(reload(account).getLockedAt()).isNull();
    }

    @Test
    void releasingALockWithoutAReasonIsRefused() throws Exception {
        User account = newAccount();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD);

        mockMvc
            .perform(
                post("/api/admin/users/{login}/unlock", account.getLogin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
            .andExpect(status().isConflict());

        assertThat(reload(account).getLockedAt()).as("and it stays locked").isNotNull();
    }

    /**
     * The one of the five constraints that is not obvious from the happy path: the window must not slide.
     *
     * <p>If a refused attempt restarted the clock, whoever is guessing could hold an account locked for ever by
     * attacking it — the cheapest denial of service in the system, and the exact thing the auto-release exists to
     * stop. A lock is a fixed quarter of an hour from the moment it went on, and attempts made during it are not
     * counted at all.
     */
    @Test
    void aWrongPasswordWhileLockedDoesNotExtendTheLock() throws Exception {
        User account = newAccount();

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD);
        Instant lockedAt = reload(account).getLockedAt();

        failTimes(account, 3);
        signIn(account, PASSWORD).andExpect(status().isUnauthorized());

        User stillLocked = reload(account);
        assertThat(stillLocked.getLockedAt()).as("the lock keeps the moment it went on").isEqualTo(lockedAt);
        assertThat(stillLocked.getFailedAttempts())
            .as("and attempts made while it is locked are not counted")
            .isEqualTo(LoginAttemptListener.LOCKOUT_THRESHOLD);
    }

    /**
     * The lock releases itself when the window is up, and says so on the trail. Nothing runs on a timer: the release
     * happens at the next sign-in, which is the only moment the answer matters.
     */
    @Test
    void aLockThatHasRunOutReleasesItselfOnASuccessfulSignIn() throws Exception {
        User account = newAccount();
        ageTheLock(account, Instant.now().minus(SignInLockout.LOCK_WINDOW).minusSeconds(1));

        signIn(account, PASSWORD).andExpect(status().isOk());

        User released = reload(account);
        assertThat(released.getLockedAt()).as("the row no longer says locked").isNull();
        assertThat(released.getFailedAttempts()).as("and the count starts again from zero").isZero();
        assertThat(rowsFor(account, AuditActions.USER_LOCK_EXPIRED)).as("the release is its own event").hasSize(1);
    }

    /**
     * The other half of the lazy release: an expired lock must not stay on the row because nobody signed in
     * successfully, or a wrong password would be the first thing to count again while the account still read locked.
     */
    @Test
    void aLockThatHasRunOutReleasesItselfOnTheNextFailedAttemptToo() throws Exception {
        User account = newAccount();
        ageTheLock(account, Instant.now().minus(SignInLockout.LOCK_WINDOW).minusSeconds(1));

        signIn(account, "definitely-not-the-password").andExpect(status().isUnauthorized());

        User afterAttempt = reload(account);
        assertThat(afterAttempt.getLockedAt()).as("the lock is gone").isNull();
        assertThat(afterAttempt.getFailedAttempts()).as("and the attempt is the first of the new window").isEqualTo(1);
        assertThat(rowsFor(account, AuditActions.USER_LOCK_EXPIRED)).hasSize(1);
    }

    /** Three ways a lock can end, and the trail has to tell them apart. */
    @Test
    void theTrailTellsTheLockTheExpiryAndTheManualReleaseApart() throws Exception {
        User account = newAccount();
        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD);
        assertThat(rowsFor(account, AuditActions.USER_LOCKED)).as("the lock is recorded once, not once per attempt").hasSize(1);

        ageTheLock(account, Instant.now().minus(SignInLockout.LOCK_WINDOW).minusSeconds(1));
        signIn(account, "definitely-not-the-password").andExpect(status().isUnauthorized());
        assertThat(rowsFor(account, AuditActions.USER_LOCK_EXPIRED)).hasSize(1);

        failTimes(account, LoginAttemptListener.LOCKOUT_THRESHOLD - 1);
        mockMvc
            .perform(
                post("/api/admin/users/{login}/unlock", account.getLogin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"Confirmed at the desk\"}")
            )
            .andExpect(status().isOk());

        assertThat(rowsFor(account, AuditActions.USER_UNLOCKED)).hasSize(1);
        assertThat(rowsFor(account, AuditActions.USER_LOCK_EXPIRED))
            .as("and an administrator releasing it is not recorded as the window running out")
            .hasSize(1);
    }

    /**
     * A throwaway account for one test. Nothing else signs in as it, so nothing it leaves behind can change
     * another test's answer.
     */
    private User newAccount() {
        String login = "lockout-" + UUID.randomUUID();
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        return userRepository.saveAndFlush(user);
    }

    /**
     * Reads an account back after the requests. The persistence context is flushed and then cleared first, so the
     * answer comes from the database rather than from a cached instance holding the state from before the
     * request. The flush is what makes the clear safe: clearing on its own throws away changes the last request
     * left pending, which reads exactly like the request never happened.
     */
    private User reload(User account) {
        entityManager.flush();
        entityManager.clear();
        return userRepository.findOneByLogin(account.getLogin()).orElseThrow();
    }

    private void failTimes(User account, int times) throws Exception {
        for (int attempt = 0; attempt < times; attempt++) {
            signIn(account, "definitely-not-the-password").andExpect(status().isUnauthorized());
        }
    }

    /**
     * Moves a lock back in time as if the window had gone by.
     *
     * <p>The cache is cleared by hand, and that is not tidiness: the sign-in path reads the account through
     * {@code usersByLogin}, and the only eviction in the application is in {@code UserService}, which this write does
     * not go through. Without the eviction the sign-in path would read the lock state from before the change, and a
     * test written that way cannot fail for the reason it claims to be testing.
     */
    private void ageTheLock(User account, Instant lockedAt) {
        User row = userRepository.findOneByLogin(account.getLogin()).orElseThrow();
        row.setLockedAt(lockedAt);
        row.setFailedAttempts(LoginAttemptListener.LOCKOUT_THRESHOLD);
        userRepository.saveAndFlush(row);
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).clear();
    }

    private List<AuditLog> rowsFor(User account, String action) {
        return auditLogRepository
            .findByEntityNameAndEntityIdOrderByIdAsc("User", String.valueOf(account.getId()))
            .stream()
            .filter(row -> action.equals(row.getAction()))
            .toList();
    }

    private ResultActions signIn(User account, String password) throws Exception {
        return mockMvc.perform(
            post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"" + account.getLogin() + "\",\"password\":\"" + password + "\",\"rememberMe\":false}"
                )
        );
    }
}
