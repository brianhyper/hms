package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Pure rules for what a password may be. No database, no request. */
class PasswordsTest {

    @Test
    void theMinimumIsTheBoundaryAndItIsInclusive() {
        assertThat(Passwords.isAcceptable("s".repeat(Passwords.MIN_LENGTH - 1), null)).isFalse();
        assertThat(Passwords.isAcceptable("s".repeat(Passwords.MIN_LENGTH), null)).isTrue();
    }

    @Test
    void theMaximumIsTheBoundaryAndItIsInclusive() {
        assertThat(Passwords.isAcceptable("s".repeat(Passwords.MAX_LENGTH), null)).isTrue();
        assertThat(Passwords.isAcceptable("s".repeat(Passwords.MAX_LENGTH + 1), null)).isFalse();
    }

    @Test
    void aPasswordMayNotBeTheAccountsOwnLogin() {
        assertThat(Passwords.isAcceptable("wardclerk", "wardclerk")).isFalse();
        assertThat(Passwords.isAcceptable("WARDCLERK", "wardclerk")).as("nor in another case").isFalse();
        assertThat(Passwords.isAcceptable("wardclerk1", "wardclerk")).isTrue();
    }

    /** The endpoint checks a length before the account is loaded, and must not be refused for want of a login. */
    @Test
    void withoutALoginToCompareThePasswordIsJudgedOnLengthAlone() {
        assertThat(Passwords.isAcceptable("wardclerk", null)).isTrue();
    }

    @Test
    void nothingIsNotAPassword() {
        assertThat(Passwords.isAcceptable(null, null)).isFalse();
    }
}
