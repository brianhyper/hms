package com.hyperbrains.hms.security;

import com.hyperbrains.hms.domain.Authority;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.UserRepository;
import java.time.Instant;
import java.util.*;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String login) {
        LOG.debug("Authenticating {}", login);

        if (new EmailValidator().isValid(login, null)) {
            return userRepository
                .findOneWithAuthoritiesByEmailIgnoreCase(login)
                // The account's own login, not the address the caller typed: the state this reads is keyed on login, and
                // passing the email here found nothing and refused a sign-in by email outright. It was harmless while
                // the argument was only used in a message.
                .map(user -> createSpringSecurityUser(user.getLogin(), user))
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in the database"));
        }

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        return userRepository
            .findOneWithAuthoritiesByLogin(lowercaseLogin)
            .map(user -> createSpringSecurityUser(user.getLogin(), user))
            .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {
        // The account's security state is read from the row rather than taken from the user above, which arrived
        // through the usersByLogin cache. That cache is evicted by UserService alone, and the lockout listener writes
        // a lock through the repository: reading the cached copy meant five wrong passwords left this path seeing
        // "not locked", so the lock did nothing until the cache expired — an hour — and a release was equally
        // invisible in the other direction. A projection is not cached, so a lock, a release or a deactivation is
        // seen as soon as it is stored, whoever stored it.
        UserRepository.SignInState state = userRepository
            .findSignInStateByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("User " + login + " was not found in the database"));

        if (!state.isActivated()) {
            // The same refusal as a wrong password, byte for byte, for the same reason the lock uses it: this answer
            // goes to whoever is asking, who is by definition not signed in, and "this account was not activated"
            // confirms that the account exists. It also means the three refusals an anonymous caller can reach —
            // unknown login, wrong password, and an account that cannot sign in yet — read identically from outside.
            //
            // JHipster's UserNotActivatedException used to carry this. Nothing in production throws it now, but the
            // type stays in the translator's walk: a refusal that arrives that way should still be answered as a
            // refusal rather than as a server error.
            throw new BadCredentialsException("Bad credentials");
        }
        if (SignInLockout.isInForce(state.getLockedAt(), Instant.now())) {
            // A lock stops the password being tried at all, so a correct password does not get past it either, and
            // the refusal is identical whichever password was sent.
            //
            // It is a window rather than a bar: fifteen minutes after it went on it stops being in force and the
            // account works again without anybody being asked. The row is tidied the next time the account is
            // touched — by a sign-in that succeeds, or by the next failed attempt — because that is the only moment
            // the answer matters; a timer would exist only to keep a column looking neat.
            //
            // Reported as bad credentials rather than as a locked account, deliberately. This answer goes to
            // whoever is asking, who is by definition not signed in: "this account is locked" would confirm
            // that the account exists and that somebody has been hammering it, which is what credential stuffing
            // is looking for. The account holder knows they have been struggling to sign in, and the audit trail
            // records the lock, so nothing is lost by not saying it here.
            //
            // The message is the one Spring uses for a wrong password, word for word, and the response is built
            // from it, so the answer for a locked account is not merely similar to the answer for a wrong
            // password but the same bytes. Naming the lock here would put it straight into that response. An
            // earlier version of this comment claimed this exception reached the caller intact; it does not, and
            // saying so in the message is what would have leaked.
            throw new BadCredentialsException("Bad credentials");
        }
        return UserWithId.fromUser(user);
    }

    public static class UserWithId extends org.springframework.security.core.userdetails.User {

        private final Long id;

        public UserWithId(String login, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(login, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public static UserWithId fromUser(User user) {
            return new UserWithId(
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities().stream().map(Authority::getName).map(SimpleGrantedAuthority::new).toList(),
                user.getId()
            );
        }
    }
}
