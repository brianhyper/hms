package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.InvalidPasswordException;
import com.hyperbrains.hms.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.jhipster.security.RandomUtil;

/**
 * What a reset link has to be: usable once, usable only for a while, and not spent by a mistake.
 *
 * <p>Deliberately not transactional. Each call below is a transaction of its own that really commits, which is the
 * only way to see what a second caller would see — inside one test transaction the account is read back as the object
 * the code mutated, so a key that a second request could still spend looks exactly like one it could not. That
 * difference is the whole point of this file, and it is what a test with {@code @Transactional} would hide.
 */
@IntegrationTest
class ResetTokenIT {

    private static final String NEW_PASSWORD = "ward-notes-2026-stairs";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void aKeyStopsWorkingOnceItHasBeenUsed() {
        User account = account("once", Duration.ZERO, true);

        assertThat(userService.completePasswordReset(NEW_PASSWORD, account.getResetKey())).isPresent();

        assertThat(userService.completePasswordReset(NEW_PASSWORD, account.getResetKey()))
            .as("the same link must not set a second password")
            .isEmpty();

        // Read back from the row, in a transaction of its own, so this is the stored state and not the object the
        // service handed back.
        User stored = userRepository.findOneByLogin(account.getLogin()).orElseThrow();
        assertThat(stored.getResetKey()).as("the spent key is cleared, not merely skipped").isNull();
        assertThat(stored.getResetDate()).isNull();
        assertThat(stored.isPasswordChangeRequired()).as("they chose it themselves, so nothing is owed").isFalse();
        assertThat(passwordEncoder.matches(NEW_PASSWORD, stored.getPassword())).isTrue();
    }

    /**
     * The one that justifies the lock. Two requests carrying the same key used to be able to read the account before
     * either wrote to it, so both set a password and the later one won — which hands anybody who kept a copy of a
     * link the power to overrule the person it was sent to.
     */
    @Test
    void aKeyIsSpentByOnlyOneOfTwoRequestsThatArriveTogether() throws Exception {
        User account = account("racing", Duration.ZERO, false);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch bothWaiting = new CountDownLatch(1);
            Callable<Boolean> attempt = () -> {
                bothWaiting.await();
                return userService.completePasswordReset(NEW_PASSWORD, account.getResetKey()).isPresent();
            };
            Future<Boolean> first = pool.submit(attempt);
            Future<Boolean> second = pool.submit(attempt);
            bothWaiting.countDown();

            List<Boolean> outcomes = List.of(first.get(60, TimeUnit.SECONDS), second.get(60, TimeUnit.SECONDS));

            assertThat(outcomes).as("exactly one of the two may set a password").containsExactlyInAnyOrder(true, false);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void anExpiredKeyIsRefused() {
        User account = account("expired", Duration.ofDays(2), false);

        assertThat(userService.completePasswordReset(NEW_PASSWORD, account.getResetKey()))
            .as("a day is the limit, and this one is two days old")
            .isEmpty();
    }

    /**
     * The reason the password is checked before anything is written. A link that the holder loses by typing a weak
     * password would leave them with no way in and no way to tell what happened, so a refusal has to cost them
     * nothing.
     */
    @Test
    void aRefusedPasswordLeavesTheKeyUsable() {
        User account = account("refused", Duration.ZERO, false);
        String key = account.getResetKey();

        assertThatThrownBy(() -> userService.completePasswordReset(account.getLogin(), key))
            .as("the account's own login is not a password this application accepts")
            .isInstanceOf(InvalidPasswordException.class);

        assertThat(userService.completePasswordReset(NEW_PASSWORD, key))
            .as("the link still works after a refused password")
            .isPresent();
    }

    /**
     * A reset key is enough to take an account over, so it must not reach the log, which is read by more people and
     * kept for longer than the database is. Asserting that something <em>was</em> logged is what keeps this honest: a
     * logger that is switched off would otherwise let the test pass without having watched anything.
     */
    @Test
    void noKeyIsWrittenToTheLog() {
        User resetting = account("quiet-reset", Duration.ZERO, false);
        User activating = account("quiet-activation", Duration.ZERO, false);

        Logger logger = (Logger) LoggerFactory.getLogger(UserService.class);
        ListAppender<ILoggingEvent> events = new ListAppender<>();
        Level previousLevel = logger.getLevel();
        events.start();
        logger.addAppender(events);
        logger.setLevel(Level.DEBUG);
        try {
            userService.completePasswordReset(NEW_PASSWORD, resetting.getResetKey());
            userService.activateRegistration(activating.getActivationKey());
        } finally {
            logger.detachAppender(events);
            logger.setLevel(previousLevel);
            events.stop();
        }

        assertThat(events.list).as("the appender saw the calls, so an empty log means something").isNotEmpty();
        assertThat(events.list)
            .as("neither key may appear in the log")
            .noneMatch(event -> event.getFormattedMessage().contains(resetting.getResetKey()));
    }

    private User account(String purpose, Duration keyAge, boolean passwordChangeRequired) {
        String login = "reset-token-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode("a-password-nobody-knows-1"));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.setPasswordChangeRequired(passwordChangeRequired);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now().minus(keyAge));
        user.setActivationKey(RandomUtil.generateActivationKey());
        user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.NURSE).orElseThrow());
        return userRepository.saveAndFlush(user);
    }
}
