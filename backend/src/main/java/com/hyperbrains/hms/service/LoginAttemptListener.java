package com.hyperbrains.hms.service;

import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Counts failed sign-ins, and locks an account that keeps failing.
 *
 * <p>It lives here rather than beside the other security classes because it writes an audit entry when a lock
 * goes on, and the architecture test is right that a security primitive should not depend on the service layer.
 * It listens to Spring's own authentication events rather than sitting in the authentication path, so the
 * counting cannot be skipped by a caller who authenticates some other way, and nothing about how a password is
 * verified had to change.
 *
 * <p>Only bad credentials count. A locked account's later attempts raise a different event
 * ({@code AuthenticationFailureLockedEvent}), so somebody hammering a locked account does not extend the lock or
 * fill the trail: the failure stops being about the password the moment the lock is on.
 *
 * <p>A successful sign-in clears the count, which is the whole difference between "five typos over a year" and
 * "five guesses in a minute" — only consecutive failures are counted.
 */
@Component
public class LoginAttemptListener {

    /** Phase 3's threshold, fixed by the client: five failed attempts and the account is locked. */
    public static final int LOCKOUT_THRESHOLD = 5;

    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptListener.class);

    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    public LoginAttemptListener(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        countFailure(event.getAuthentication().getName());
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        clearFailures(event.getAuthentication().getName());
    }

    private void countFailure(String login) {
        Optional<User> maybeUser = userRepository.findOneByLogin(login.toLowerCase(Locale.ENGLISH));
        if (maybeUser.isEmpty()) {
            // An unknown login has no account to lock. Guessing at a login that does not exist is a different
            // problem, and there is nothing here to count it against.
            return;
        }

        User user = maybeUser.orElseThrow();
        if (user.getLockedAt() != null) {
            // Already locked. The attempt is refused before the password is even looked at, so counting it would
            // only make the number meaningless for whoever reads the account afterwards.
            return;
        }
        user.setFailedAttempts(user.getFailedAttempts() + 1);

        boolean becameLocked = user.getFailedAttempts() >= LOCKOUT_THRESHOLD && user.getLockedAt() == null;
        if (becameLocked) {
            user.setLockedAt(Instant.now());
        }
        userRepository.save(user);

        if (becameLocked) {
            LOG.warn("Account {} locked after {} failed sign-in attempts", user.getLogin(), user.getFailedAttempts());
            // Recorded once, when the lock goes on, rather than once per attempt: the lock is the event, and a
            // hundred rows saying the same thing would bury it.
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.USER_LOCKED, "User", user.getId()).withDetails(
                    "Sign-in locked for " +
                    user.getLogin() +
                    " after " +
                    user.getFailedAttempts() +
                    " failed attempts; an administrator has to release it"
                )
            );
        }
    }

    private void clearFailures(String login) {
        userRepository
            .findOneByLogin(login.toLowerCase(Locale.ENGLISH))
            .filter(user -> user.getFailedAttempts() > 0)
            .ifPresent(user -> {
                user.setFailedAttempts(0);
                userRepository.save(user);
            });
    }
}
