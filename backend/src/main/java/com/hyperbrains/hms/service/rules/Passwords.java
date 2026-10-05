package com.hyperbrains.hms.service.rules;

/**
 * What the system accepts as a password.
 *
 * <p>Lengths and one prohibition, and deliberately nothing else. A composition rule — an upper case letter, a digit, a
 * symbol — is the classic way to produce a password that satisfies a checklist and is easier to guess than a long one,
 * and it makes a keyboard on a ward worse to use. Length is what costs an attacker, so the rule is length.
 *
 * <p>The prohibition is the person's own login: it is the first guess anybody makes, it is written on the account
 * sheet, and "admin" for the account called admin is not a password.
 *
 * <p>Pure, so the boundaries — exactly the minimum, exactly the maximum, and a password that is the login in another
 * case — are decided without a database or a request. It lives here rather than beside the endpoint because the
 * service layer is where the account is known, and the endpoint alone cannot ask the login question.
 */
public final class Passwords {

    /**
     * The shortest password accepted.
     *
     * <p>Eight, which would be thin on its own against an unthrottled attacker and is not thin here: the lockout
     * allows five attempts a quarter of an hour, a few hundred a day against one account, so guessing is bounded by
     * the lock rather than by the length. The number is a judgement rather than a computation, and raising it is one
     * edit — every password set afterwards is held to it, and passwords already in use keep working.
     */
    public static final int MIN_LENGTH = 8;

    /** The longest accepted: the column's width, and past the point where length stops adding anything. */
    public static final int MAX_LENGTH = 100;

    private Passwords() {}

    /**
     * Whether a password may be used.
     *
     * @param login the account's own login, or null when it is not known at the point of the check — the endpoint
     *              checking a length before the account is loaded, for instance
     */
    public static boolean isAcceptable(String password, String login) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }
        return login == null || !password.equalsIgnoreCase(login);
    }
}
