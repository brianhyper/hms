package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.Set;

/**
 * The rules that keep an account — and the system — reachable.
 *
 * <p>Phase 3 requires that the hospital can never be locked out of its own system: one seed Super Admin
 * account that is never deleted is not enough on its own, because a role can be taken away as easily as an
 * account can be deactivated. So the pair of rules live here, away from the service that applies them, and
 * they are about the <em>change</em> rather than the result: handing somebody the role is always fine, and
 * removing the last one, or removing your own, is not.
 *
 * <p>Pure on purpose: no repository, no security context, nothing to mock. What the rules cannot know is how
 * many other accounts could manage accounts, so that count is passed in by the caller that looked it up.
 */
public final class AccountLifecycle {

    private AccountLifecycle() {}

    /** Whether these authorities make an account one that can manage accounts. */
    public static boolean managesAccounts(Set<String> authorityNames) {
        return authorityNames.contains(AuthoritiesConstants.SUPER_ADMIN);
    }

    /**
     * Whether this change would leave nobody able to put the role back.
     *
     * <p>Asked of the change, not of the account: an account that was never able to manage accounts can be
     * deactivated freely, and so can one of two managers. What is refused is the step from "somebody can" to
     * "nobody can", whether that step is deactivation or the role being taken away.
     *
     * @param wasActiveManager whether the account could manage accounts before the change
     * @param staysActiveManager whether it still could afterwards
     * @param otherActiveManagers how many <em>other</em> active accounts can manage accounts
     */
    public static boolean wouldLeaveTheSystemUnmanageable(
        boolean wasActiveManager,
        boolean staysActiveManager,
        long otherActiveManagers
    ) {
        return wasActiveManager && !staysActiveManager && otherActiveManagers == 0;
    }

    /**
     * Whether an administrator is taking their own role away.
     *
     * <p>Refused because it is the one change that can be made by accident and cannot be undone by the person
     * who made it: the moment the role is gone, so is the ability to give it back. Another Super Admin has to
     * do it, which also means the act is witnessed.
     */
    public static boolean isTakingTheirOwnRoleAway(
        String actorLogin,
        String targetLogin,
        boolean heldTheRole,
        boolean stillHoldsTheRole
    ) {
        return actorLogin != null && actorLogin.equals(targetLogin) && heldTheRole && !stillHoldsTheRole;
    }
}
