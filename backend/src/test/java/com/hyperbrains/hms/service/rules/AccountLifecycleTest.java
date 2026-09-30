package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The two ways a hospital can lock itself out, and the cases that must not be mistaken for them.
 */
class AccountLifecycleTest {

    @Test
    void managingAccountsMeansHoldingTheSuperAdminRole() {
        assertThat(AccountLifecycle.managesAccounts(Set.of(AuthoritiesConstants.SUPER_ADMIN))).isTrue();
        assertThat(AccountLifecycle.managesAccounts(Set.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER))).isFalse();
        assertThat(AccountLifecycle.managesAccounts(Set.of())).isFalse();
    }

    @Test
    void deactivatingTheLastManagerIsRefused() {
        assertThat(AccountLifecycle.wouldLeaveTheSystemUnmanageable(true, false, 0L)).isTrue();
    }

    @Test
    void takingTheRoleFromTheLastManagerIsRefused() {
        // The same rule as deactivation, because it has the same consequence: nobody is left who could put
        // it back.
        assertThat(AccountLifecycle.wouldLeaveTheSystemUnmanageable(true, false, 0L)).isTrue();
    }

    @Test
    void deactivatingOneOfTwoManagersIsAllowed() {
        assertThat(AccountLifecycle.wouldLeaveTheSystemUnmanageable(true, false, 1L)).isFalse();
    }

    @Test
    void anAccountThatNeverManagedAccountsCanBeDeactivatedFreely() {
        assertThat(AccountLifecycle.wouldLeaveTheSystemUnmanageable(false, false, 0L)).isFalse();
    }

    @Test
    void anAccountThatKeepsManagingAccountsIsNotAProblem() {
        assertThat(AccountLifecycle.wouldLeaveTheSystemUnmanageable(true, true, 0L)).isFalse();
    }

    @Test
    void aSuperAdminCannotTakeTheirOwnRoleAway() {
        assertThat(AccountLifecycle.isTakingTheirOwnRoleAway("me", "me", true, false)).isTrue();
    }

    @Test
    void anotherSuperAdminMayTakeTheRoleAway() {
        assertThat(AccountLifecycle.isTakingTheirOwnRoleAway("someone-else", "me", true, false)).isFalse();
    }

    @Test
    void keepingTheRoleIsNotTakingItAway() {
        assertThat(AccountLifecycle.isTakingTheirOwnRoleAway("me", "me", true, true)).isFalse();
    }

    @Test
    void anAnonymousCallerIsNeverDowngradingThemselves() {
        // The reset and registration flows run with nobody signed in, so a null actor must not be read as a
        // match on a null login.
        assertThat(AccountLifecycle.isTakingTheirOwnRoleAway(null, "me", true, false)).isFalse();
    }

    @Test
    void grantingTheRoleIsNeverARefusal() {
        assertThat(AccountLifecycle.isTakingTheirOwnRoleAway("me", "me", false, true)).isFalse();
    }
}
