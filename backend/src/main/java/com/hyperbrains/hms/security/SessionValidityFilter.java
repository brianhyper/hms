package com.hyperbrains.hms.security;

import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Refuses a request whose token belongs to an account that has been switched off, or whose token predates the
 * moment that account's sessions were revoked.
 *
 * <p>Why this has to exist: the tokens are signed and stateless, so nothing can be taken back once issued.
 * Blocking the next sign-in is not the same as ending the session that is already open, and the difference
 * matters exactly when it matters most — an account switched off during an investigation, with a session still
 * open on a ward machine, would otherwise keep working for as long as the token lived.
 *
 * <p>It deliberately does <strong>not</strong> refuse a token whose login has no account. That would be the
 * stricter reading, but a signed token for an account that never existed is not reachable in this system
 * (accounts are never deleted, and the tokens are signed with the server's own key), while the tests and the
 * anonymous flows do mint tokens against logins that have no row. The rule this enforces is revocation, and it
 * enforces exactly that.
 *
 * <p>Runs after the bearer token has been read, so it sees a decoded {@link Jwt}; before that point there is no
 * principal and it does nothing, which is also why the registration Boot may add alongside the security chain
 * costs nothing.
 */
public class SessionValidityFilter extends OncePerRequestFilter {

    private static final String SESSION_ENDED =
        "{\"errorKey\":\"sessionEnded\",\"message\":\"This session is no longer valid: the account has been deactivated or its sessions were ended\"}";

    private final ObjectProvider<UserRepository> userRepository;

    public SessionValidityFilter(ObjectProvider<UserRepository> userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Optional<Jwt> token = currentToken();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // The repository is asked for lazily, and its absence is not an error. There are security-only slices of
        // this application - the tests that exercise token cryptography, which load this configuration without
        // any repositories - and a control that cannot be constructed there would take the whole slice down with
        // it. Anywhere the application is whole the repository is there, and that is where accounts exist to
        // consult in the first place.
        UserRepository accounts = userRepository.getIfAvailable();
        if (accounts == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Jwt jwt = token.orElseThrow();
        Optional<User> account = accounts.findOneWithAuthoritiesByLogin(jwt.getSubject());

        if (account.isPresent() && wasEnded(account.orElseThrow(), jwt)) {
            refuse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Whether this token is older than the moment the account's sessions stopped being accepted. */
    private static boolean wasEnded(User account, Jwt jwt) {
        if (!account.isActivated()) {
            return true;
        }
        Instant validFrom = account.getSessionsValidFrom();
        // Strictly before, so a token issued in the same second as the revocation is not thrown away by a clock
        // that has not ticked yet. The window is a second wide and it is on the permissive side of a control
        // whose alternative is refusing a legitimate new sign-in.
        return validFrom != null && jwt.getIssuedAt() != null && jwt.getIssuedAt().isBefore(validFrom);
    }

    private static Optional<Jwt> currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    private static void refuse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(SESSION_ENDED);
    }
}
