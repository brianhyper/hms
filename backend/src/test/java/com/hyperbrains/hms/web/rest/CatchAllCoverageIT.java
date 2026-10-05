package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Nothing that changes state may be reachable by the catch-all row alone.
 *
 * <p>The RBAC table ends with {@code /api/**} for every hospital role, which is there so that the login flow
 * and the account endpoints keep working. It also means any route nobody wrote a row for is open to every
 * signed-in user — which is how {@code /api/payment-plans} sat unprotected, and how bill lines were writable by
 * Finance. Both were found by reading the table and hoping to notice.
 *
 * <p>This is the test that notices instead. It does not consult the table at all: it asks Spring for every
 * mapping it has, takes every one that is not a plain read, and calls it as a {@code ROLE_USER}-only
 * account — the role the catch-all admits and nothing more. Anything that does not come back forbidden reached
 * the controller without a rule of its own, so a new endpoint added without a row fails here, before anybody
 * has to remember to look.
 *
 * <p>Reads are left out on purpose: a GET that is open to every signed-in user is a deliberate choice in this
 * system, not a hole. The exceptions below are the writes that are intentionally open to whoever is signed in.
 */
@IntegrationTest
@AutoConfigureMockMvc
class CatchAllCoverageIT {

    /**
     * Writes that are deliberately available to any signed-in account, each because it is the account acting on
     * itself. Recovery and signing in are here for a different reason: they are {@code permitAll}
     * on purpose, because somebody who cannot sign in is the whole point of them.
     *
     * <p>{@code /api/register} is deliberately not here: it is closed to everyone now, so it is a route that
     * refuses rather than a route that is open.
     *
     * <p>{@code /api/exception-translator-test} is the controller the exception translator's own tests call. It
     * only exists in test sources and is not part of the application's surface at all.
     */
    private static final Set<String> OPEN_TO_WHOEVER_IS_SIGNED_IN = Set.of(
        "/api/account",
        "/api/account/change-password",
        "/api/activate",
        "/api/account/reset-password/init",
        "/api/account/reset-password/finish",
        "/api/authenticate",
        "/api/exception-translator-test/method-argument"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void nothingThatChangesStateIsReachableByTheCatchAllAlone() {
        List<String> reached = new ArrayList<>();

        for (RequestMappingInfo mapping : handlerMapping.getHandlerMethods().keySet()) {
            for (String pattern : mapping.getPatternValues()) {
                if (!pattern.startsWith("/api/") || OPEN_TO_WHOEVER_IS_SIGNED_IN.contains(pattern)) {
                    continue;
                }
                for (RequestMethod method : mapping.getMethodsCondition().getMethods()) {
                    if (isRead(method)) {
                        continue;
                    }
                    String status = callIt(method, pattern);
                    if (!status.startsWith("403")) {
                        reached.add(method + " " + pattern + " answered " + status);
                    }
                }
            }
        }

        assertThat(reached)
            .as(
                "A state-changing route that answers a ROLE_USER-only account is not protected by an RBAC row " +
                "of its own: it fell through to the /api/** catch-all. Write it a row in SecurityConfiguration."
            )
            .isEmpty();
    }

    private static boolean isRead(RequestMethod method) {
        return method == RequestMethod.GET || method == RequestMethod.HEAD || method == RequestMethod.OPTIONS;
    }

    /**
     * Calls the route with the smallest plausible body and reports the status.
     *
     * <p>An address that fails at the database or throws is reported rather than propagated: the point is to
     * list everything that got past the door, and an exception on the way through is one more thing that got
     * past it.
     */
    private String callIt(RequestMethod method, String pattern) {
        String concrete = pattern.replaceAll("\\{[^/]+}", "1");
        MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method.name()), concrete).contentType(
            MediaType.APPLICATION_JSON
        );
        if (method != RequestMethod.DELETE) {
            request = request.content("{}");
        }
        try {
            return String.valueOf(mockMvc.perform(request).andReturn().getResponse().getStatus());
        } catch (Exception ex) {
            return "an exception (" + ex.getClass().getSimpleName() + ")";
        }
    }
}
