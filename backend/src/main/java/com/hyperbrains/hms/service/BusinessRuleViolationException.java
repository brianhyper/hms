package com.hyperbrains.hms.service;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The request is well-formed but the record is not in a state where it makes sense.
 *
 * <p>Used across the clinical workflows: checking in an appointment that was already marked as a
 * no-show, selecting a visit that has already been triaged, dispensing before payment. These are
 * not validation failures — the payload is fine — so they are 409s, not 400s, and the error key
 * lets the client show a specific sentence rather than "something went wrong".
 *
 * <p>Lives in {@code service} for the same ArchUnit reason as {@link ExactPatientMatchException}.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessRuleViolationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorKey;

    private final String entityName;

    public BusinessRuleViolationException(String errorKey, String entityName, String message) {
        super(message);
        this.errorKey = errorKey;
        this.entityName = entityName;
    }

    public static BusinessRuleViolationException of(String errorKey, String entityName, String message) {
        return new BusinessRuleViolationException(errorKey, entityName, message);
    }

    public String getErrorKey() {
        return errorKey;
    }

    public String getEntityName() {
        return entityName;
    }
}
