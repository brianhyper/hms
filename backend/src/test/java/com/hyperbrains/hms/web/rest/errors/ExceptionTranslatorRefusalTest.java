package com.hyperbrains.hms.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.security.UserNotActivatedException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

/**
 * The cause-chain walk that decides whether an exception is a refused sign-in.
 *
 * <p>Unit level rather than over HTTP, deliberately: two of these cases cannot arrive through any endpoint — an
 * exception that is its own cause, and two that name each other — and the property under test is that the walk
 * always ends. Following a chain like that for ever would hang a request that should simply have been refused.
 */
class ExceptionTranslatorRefusalTest {

    @Test
    void aRefusalIsFoundWhenItIsTheExceptionItself() {
        assertThat(ExceptionTranslator.isSignInRefusal(new BadCredentialsException("Bad credentials"))).isTrue();
        assertThat(ExceptionTranslator.isSignInRefusal(new UserNotActivatedException("not activated"))).isTrue();
    }

    /** The shape Spring actually produces: the user-details service's refusal inside the provider's wrapper. */
    @Test
    void aRefusalIsFoundThroughTheWrapSpringAdds() {
        Throwable wrapped = new InternalAuthenticationServiceException("wrap", new BadCredentialsException("Bad credentials"));

        assertThat(ExceptionTranslator.isSignInRefusal(wrapped)).isTrue();
    }

    /** Depth is not the limit — identity is, which is why this is a visited set and not a counter. */
    @Test
    void aLongChainStillReachesTheRefusalAtTheBottom() {
        Throwable chain = new BadCredentialsException("Bad credentials");
        for (int depth = 0; depth < 200; depth++) {
            chain = new IllegalStateException("wrapper " + depth, chain);
        }

        assertThat(ExceptionTranslator.isSignInRefusal(chain)).isTrue();
    }

    /**
     * A chain that never ends, built the only way the language allows one: {@code initCause} refuses to let a throwable
     * be its own cause ({@code Self-causation not permitted}), so a subclass has to report itself. That is still worth
     * guarding against — the walk cannot know what it is being handed.
     */
    @Test
    void aSelfCausedExceptionTerminates() {
        Throwable selfCaused = new RuntimeException("self") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };

        assertThat(ExceptionTranslator.isSignInRefusal(selfCaused)).isFalse();
    }

    /** Two exceptions naming each other: the chain the previous walk would have followed for ever. */
    @Test
    void aTwoElementCycleTerminates() {
        RuntimeException first = new RuntimeException("first");
        RuntimeException second = new RuntimeException("second", first);
        first.initCause(second);

        assertThat(first.getCause()).isSameAs(second);
        assertThat(second.getCause()).isSameAs(first);
        assertThat(ExceptionTranslator.isSignInRefusal(first)).isFalse();
    }

    /** A cycle is still a refusal if the refusal is in it: the walk finds it before it notices the loop. */
    @Test
    void aCycleContainingARefusalIsStillARefusal() {
        BadCredentialsException refusal = new BadCredentialsException("Bad credentials");
        refusal.initCause(new RuntimeException("and back to the beginning", refusal));

        assertThat(ExceptionTranslator.isSignInRefusal(refusal)).isTrue();
    }

    /** A genuine failure keeps the server-error answer: no mapping means the translator's 500 fallback, not a 401. */
    @Test
    void aFailureThatIsNotARefusalIsNotReportedAsOne() {
        assertThat(ExceptionTranslator.isSignInRefusal(new IllegalStateException("the database is unreachable")))
            .isFalse();
        assertThat(
            ExceptionTranslator.isSignInRefusal(new IllegalStateException("outage", new RuntimeException("connection refused")))
        )
            .as("nor when it is wrapped, however deeply")
            .isFalse();
    }
}
