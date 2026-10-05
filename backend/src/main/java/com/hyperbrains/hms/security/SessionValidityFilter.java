package com.hyperbrains.hms.security;

import com.hyperbrains.hms.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 *
 * <p>The same place ends a session that has been left alone for longer than Phase 3 allows, by remembering when a
 * request last arrived on the account. That stamp is read and written straight against the row rather than through
 * the account object the revocation check uses, because that object is served from the {@code usersByLogin}
 * cache: a cached copy of a value this filter writes would be a value that never moved, which would mean a
 * timeout that never fires and an active session refused once its token got old enough. The write is deliberately
 * not made on a refused request, or the session would come back to life on the next one.
 */
public class SessionValidityFilter extends OncePerRequestFilter {

    private static final String SESSION_ENDED =
        "{\"errorKey\":\"sessionEnded\",\"message\":\"This session is no longer valid: the account has been deactivated or its sessions were ended\"}";

    private static final String SESSION_IDLE =
        "{\"errorKey\":\"sessionIdle\",\"message\":\"This session has ended: it went unused for longer than the idle time allowed\"}";

    private static final String PASSWORD_CHANGE_REQUIRED =
        "{\"errorKey\":\"passwordChangeRequired\",\"message\":\"This password was chosen for you and has to be replaced before the account can be used\"}";

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
        // The account is read through a projection rather than through the account entity: the entity comes from the
        // usersByLogin cache, which is evicted by UserService alone, and the counting listener writes a lock through
        // the repository. A cached copy would decide this control on state that nobody had stored.
        Optional<UserRepository.SignInState> found = accounts.findSignInStateByLogin(jwt.getSubject());

        if (found.isPresent()) {
            UserRepository.SignInState account = found.orElseThrow();
            if (wasEnded(account, jwt)) {
                refuse(response, HttpStatus.UNAUTHORIZED, SESSION_ENDED);
                return;
            }
            if (account.isPasswordChangeRequired() && !isOnTheWayToChangingThePassword(request)) {
                // A password that was chosen for somebody has to be replaced before the account can do anything. The
                // refusal is a 403 rather than a 401 because the caller is properly signed in — what is missing is
                // not their identity but their own choice of password — and it carries a key a client can act on.
                refuse(response, HttpStatus.FORBIDDEN, PASSWORD_CHANGE_REQUIRED);
                return;
            }

            Instant now = Instant.now();
            Instant lastActivity = accounts.findLastActivityAtByLogin(jwt.getSubject()).orElse(null);
            if (SessionIdle.hasGoneIdle(lastActivity, jwt.getIssuedAt(), now)) {
                refuse(response, HttpStatus.UNAUTHORIZED, SESSION_IDLE);
                return;
            }
            if (SessionIdle.shouldRecordActivity(lastActivity, now)) {
                accounts.recordActivityAt(jwt.getSubject(), now);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * The requests that are allowed while a password has to be changed: reading your own account, so a client can see
     * who it is and what is being asked, and the change itself. Matched on the exact path and method rather than a
     * prefix, so a lookalike route cannot slip through the exemption.
     */
    private static boolean isOnTheWayToChangingThePassword(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("/api/account".equals(path) && "GET".equals(request.getMethod())) {
            return true;
        }
        return "/api/account/change-password".equals(path) && "POST".equals(request.getMethod());
    }

    /** Whether this token is older than the moment the account's sessions stopped being accepted. */
    private static boolean wasEnded(UserRepository.SignInState account, Jwt jwt) {
        if (!account.isActivated()) {
            return true;
        }
        Instant validFrom = account.getSessionsValidFrom();
        if (validFrom == null || jwt.getIssuedAt() == null) {
            return false;
        }
        // Compared at second precision, because the token and the column do not share one. A JWT's `iat` is a whole
        // number of seconds while this stamp carries microseconds, so a token minted in the very second the account
        // was switched off is a fraction *before* the stamp and was refused — which is why switching an account off
        // and on again appeared to lock the person out, and why the test that said so failed only when it ran fast
        // enough.
        //
        // The cost of truncating, stated rather than left to be discovered: a token issued in the same second as the
        // revocation survives it, so the control has a window one second wide that favours the caller. That is the
        // deliberate direction — the alternative refuses a legitimate sign-in that happened to land in that second,
        // which is the failure this code was just taken out of. One second, against a control whose other side is a
        // person unable to sign in at all.
        return jwt.getIssuedAt().isBefore(validFrom.truncatedTo(ChronoUnit.SECONDS));
    }

    private static Optional<Jwt> currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    private static void refuse(HttpServletResponse response, HttpStatus status, String body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(body);
    }
}
