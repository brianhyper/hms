package com.hyperbrains.hms.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");

    /**
     * A patient is being registered with an identity document that already belongs to an active
     * record. 409 rather than 400: the request is well-formed and could succeed unchanged for a
     * different patient, and the client can offer "use the existing record" as an alternative.
     */
    public static final URI EXACT_PATIENT_MATCH_TYPE = URI.create(PROBLEM_BASE_URL + "/exact-patient-match");

    private ErrorConstants() {}
}
