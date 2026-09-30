package com.hyperbrains.hms.service;

import com.hyperbrains.hms.domain.User;

/**
 * How a person's name is shown on a screen.
 *
 * <p>One place, because the alternative is each read service deciding for itself, and then the same
 * doctor is "Attending Doctor" on the ward roster and "doctor" on the worklist next to it.
 *
 * <p>Falls back to the login: a User created through the administration screens always has names, but one
 * created by the generator or by a test may not, and a roster row showing nothing at all is worse than one
 * showing the account it refers to.
 */
public final class PersonNames {

    private PersonNames() {}

    /** The name to show for this account, or null when there is no account. */
    public static String displayName(User user) {
        if (user == null) {
            return null;
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getLogin() : full;
    }
}
