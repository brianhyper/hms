package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.Collection;
import java.util.Set;

/**
 * The break-glass case: releasing emergency medicine before the bill is settled.
 *
 * <p>Pure policy, so it can be reasoned about and tested without Spring. The recording lives in the override
 * service, and the release itself belongs to the caller that owns the gate.
 *
 * <p>It follows the no-detention decision: withholding emergency treatment over money has the same legal and
 * clinical problem. The legal basis is believed to be Article 43(2) of the Constitution, to be confirmed with the
 * client or a lawyer before the design leans on it.
 */
public final class BreakGlass {

    private BreakGlass() {}

    /**
     * Who may invoke it: the pharmacist or the doctor at the point of care.
     *
     * <p>Not an administrator, and not somebody who has to be located first. That is the whole point of acting before
     * two people are found, and it is why the mechanism is single-step.
     */
    public static final Set<String> INVOKERS = Set.of(AuthoritiesConstants.PHARMACY, AuthoritiesConstants.DOCTOR);

    /** Whether these authorities may invoke it. */
    public static boolean mayBeInvokedBy(Collection<String> authorities) {
        return authorities.stream().anyMatch(INVOKERS::contains);
    }
}
