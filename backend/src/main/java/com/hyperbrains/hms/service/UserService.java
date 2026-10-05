package com.hyperbrains.hms.service;

import com.hyperbrains.hms.config.Constants;
import com.hyperbrains.hms.domain.Authority;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.dto.AdminUserDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.rules.AccountLifecycle;
import com.hyperbrains.hms.service.rules.Passwords;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    private final AuditLogService auditLogService;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager,
        AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.auditLogService = auditLogService;
    }

    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key).map(user -> {
            // activate given user for the registration key.
            user.setActivated(true);
            user.setActivationKey(null);
            this.clearUserCaches(user);
            LOG.debug("Activated user: {}", user);
            return user;
        });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                if (!Passwords.isAcceptable(newPassword, user.getLogin())) {
                    // The same 400 the too-short case gets: one answer for "that password may not be used", whether it
                    // is too short or is the account's own login.
                    throw new InvalidPasswordException();
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                auditLogService.record(
                    AuditLogService.Entry.of(AuditActions.PASSWORD_RESET_COMPLETED, "User", user.getId()).withDetails(
                        "Password reset completed with a reset link for " + user.getLogin()
                    )
                );
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                // Recorded without an actor when nobody is signed in, which is the normal case here: knowing
                // that a reset was asked for, and when, is what makes a suspicious one visible.
                auditLogService.record(
                    AuditLogService.Entry.of(AuditActions.PASSWORD_RESET_REQUESTED, "User", user.getId()).withDetails(
                        "Password reset requested for " + user.getLogin()
                    )
                );
                return user;
            });
    }

    public User registerUser(AdminUserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.USER_CREATED, "User", user.getId())
                .withDetails("Account " + user.getLogin() + " created with roles " + String.join(",", sorted(authorityNames(user.getAuthorities()))))
        );
        LOG.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);

                // Read what the account is before writing anything, because both checks below are about the
                // change rather than the result, and both have to fail without leaving a half-applied edit
                // behind for the rollback to clean up.
                Set<String> authoritiesBefore = authorityNames(user.getAuthorities());
                boolean heldTheRole = AccountLifecycle.managesAccounts(authoritiesBefore);
                boolean wasActiveManager = user.isActivated() && heldTheRole;
                boolean wasActivated = user.isActivated();
                String targetLogin = user.getLogin();

                Set<Authority> requestedAuthorities = requestedAuthorities(userDTO);
                Set<String> authoritiesAfter = authorityNames(requestedAuthorities);
                boolean stillHoldsTheRole = AccountLifecycle.managesAccounts(authoritiesAfter);
                boolean staysActiveManager = userDTO.isActivated() && stillHoldsTheRole;

                // The hospital must never be able to lock itself out: deactivating the last account that can
                // manage accounts, or taking that role from it, has no way back because nobody would be left
                // who could put it right.
                if (
                    AccountLifecycle.wouldLeaveTheSystemUnmanageable(
                        wasActiveManager,
                        staysActiveManager,
                        userRepository.countOtherActiveUsersWithAuthority(AuthoritiesConstants.SUPER_ADMIN, user.getId())
                    )
                ) {
                    throw BusinessRuleViolationException.of(
                        "lastAccountManager",
                        "user",
                        "This is the only active account that can manage accounts, so this change would leave nobody able to undo it"
                    );
                }

                // And an administrator cannot take their own role away: the moment it is gone, so is the
                // ability to give it back, so it has to be another Super Admin who does it.
                if (
                    AccountLifecycle.isTakingTheirOwnRoleAway(
                        SecurityUtils.getCurrentUserLogin().orElse(null),
                        targetLogin,
                        heldTheRole,
                        stillHoldsTheRole
                    )
                ) {
                    throw BusinessRuleViolationException.of(
                        "ownSuperAdminRole",
                        "user",
                        "A Super Admin cannot take their own Super Admin role away; another Super Admin has to do it"
                    );
                }

                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                // Switching an account off has to end the sessions that are already open, not just block the
                // next sign-in: otherwise somebody who is still signed in keeps acting for as long as their
                // token lives, which is the whole point of switching the account off.
                if (wasActivated && !user.isActivated()) {
                    user.setSessionsValidFrom(Instant.now());
                }
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                managedAuthorities.addAll(requestedAuthorities);
                userRepository.save(user);
                this.clearUserCaches(user);
                auditAccountChange(user, wasActivated, authoritiesBefore, authoritiesAfter);
                LOG.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    /**
     * Records what changed about an account, because the row only ever shows where it ended up.
     *
     * <p>Two entries at most, and only for things that actually changed: an entry saying "roles unchanged"
     * every time somebody corrects a surname would bury the ones that matter.
     */
    private void auditAccountChange(User user, boolean wasActivated, Set<String> authoritiesBefore, Set<String> authoritiesAfter) {
        if (!authoritiesBefore.equals(authoritiesAfter)) {
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.USER_ROLE_CHANGED, "User", user.getId())
                    .withChange(String.join(",", sorted(authoritiesBefore)), String.join(",", sorted(authoritiesAfter)))
                    .withDetails("Roles for " + user.getLogin() + " changed")
            );
        }
        if (wasActivated != user.isActivated()) {
            auditLogService.record(
                AuditLogService.Entry.of(
                    user.isActivated() ? AuditActions.USER_ACTIVATED : AuditActions.USER_DEACTIVATED,
                    "User",
                    user.getId()
                )
                    .withChange(String.valueOf(wasActivated), String.valueOf(user.isActivated()))
                    .withDetails(user.getLogin() + (user.isActivated() ? " activated" : " deactivated"))
            );
        }
    }

    private Set<Authority> requestedAuthorities(AdminUserDTO userDTO) {
        if (userDTO.getAuthorities() == null) {
            return new HashSet<>();
        }
        return userDTO
            .getAuthorities()
            .stream()
            .map(authorityRepository::findById)
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }

    private static Set<String> authorityNames(Collection<Authority> authorities) {
        return authorities.stream().map(Authority::getName).collect(Collectors.toSet());
    }

    /** Sorted so that an audit entry compares two role sets rather than two orderings of one. */
    private static List<String> sorted(Set<String> names) {
        return names.stream().sorted().toList();
    }

    /**
     * Releases a sign-in lock, and says why.
     *
     * <p>Phase 3 has no automatic release. A locked account stays locked until somebody with the authority to
     * release it does so deliberately, with a reason that goes into the audit trail — which is the point of a
     * lockout, and the reason a timer would be the wrong shape for it.
     */
    public void unlock(String login, String reason) {
        if (reason == null || reason.isBlank()) {
            throw BusinessRuleViolationException.of(
                "unlockReasonRequired",
                "user",
                "Releasing a sign-in lock requires a reason"
            );
        }

        User user = userRepository
            .findOneByLogin(login.toLowerCase(Locale.ENGLISH))
            .orElseThrow(() -> BusinessRuleViolationException.of("userNotFound", "user", "No account with login " + login));

        if (user.getLockedAt() == null) {
            throw BusinessRuleViolationException.of(
                "userNotLocked",
                "user",
                login + " is not locked, so there is nothing to release"
            );
        }

        user.setFailedAttempts(0);
        user.setLockedAt(null);
        userRepository.save(user);
        this.clearUserCaches(user);
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.USER_UNLOCKED, "User", user.getId())
                .withReason(reason)
                .withDetails("Sign-in lock released for " + user.getLogin())
        );
    }

    /**
     * Deletes an account outright.
     *
     * <p><strong>Not reachable from any route, and that is the point.</strong> Phase 3 requires that accounts
     * are deactivated rather than deleted, so that the audit trail and every historical owner still point at
     * a person. {@code UserResource} refuses the delete route and points at deactivation instead; this method
     * remains only because test teardown uses it, and wiring it back to a route would reopen exactly the hole
     * the phase closed.
     */
    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            this.clearUserCaches(user);
            LOG.debug("Deleted User: {}", user);
        });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                this.clearUserCaches(user);
                LOG.debug("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                // The route already refused a password that is too short or too long; this is the prohibition that
                // needs the account, which is what this layer has and the endpoint does not.
                if (!Passwords.isAcceptable(newPassword, user.getLogin())) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                auditLogService.record(
                    AuditLogService.Entry.of(AuditActions.PASSWORD_CHANGED, "User", user.getId()).withDetails(
                        "Password changed by " + user.getLogin()
                    )
                );
                LOG.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evictIfPresent(user.getEmail());
        }
    }
}
