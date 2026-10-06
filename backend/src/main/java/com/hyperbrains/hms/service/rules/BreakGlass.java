package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitType;
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

    /**
     * Whether a visit is in the scope the client confirmed for break-glass: an emergency-triaged visit, or an admitted
     * patient. Nothing outside it may be released before payment.
     *
     * <p>Emergency is the visit priority, which is what triage sets and what the queues sort on; the emergency visit
     * type is checked as well because a desk can hand one in at the door. An admitted patient is in scope because a
     * prescription written before admission stays in the outpatient payment cycle, and a patient in a bed is not
     * standing at a cash desk.
     */
    public static boolean isInScope(VisitPriority priority, VisitType type) {
        return priority == VisitPriority.EMERGENCY || type == VisitType.EMERGENCY || type == VisitType.ADMISSION;
    }
}
