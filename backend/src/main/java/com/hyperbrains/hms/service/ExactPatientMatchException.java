package com.hyperbrains.hms.service;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a patient is being registered with an identity document that already belongs to an
 * existing, still-active record.
 *
 * <p>This is the one duplicate condition that <em>does</em> block a save, and it is not
 * unrecoverable: Reception can look at the record we hand back and either use it, or deliberately
 * create a second record by supplying an override reason. Both identifiers travel on the exception
 * so the client can offer those two options without a second round trip.
 *
 * <p>Lives in {@code service} rather than {@code web.rest.errors} to keep the ArchUnit layers
 * intact — a service throwing a web exception would be a Web dependency from the Service layer.
 * {@code ExceptionTranslator} turns it into a 409, the same way it already handles
 * {@link UsernameAlreadyUsedException}.
 *
 * <p>{@code @ResponseStatus} carries the reason text as well as the status: the translator reads
 * the annotation to build the problem title, so without it the title would degrade to the generic
 * "Conflict" and the useful sentence would be lost.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "An existing patient already uses this identity document")
public class ExactPatientMatchException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long existingPatientId;

    private final String existingHospitalId;

    public ExactPatientMatchException(Long existingPatientId, String existingHospitalId) {
        super("Patient identity document already in use by patient " + existingHospitalId);
        this.existingPatientId = existingPatientId;
        this.existingHospitalId = existingHospitalId;
    }

    public Long getExistingPatientId() {
        return existingPatientId;
    }

    public String getExistingHospitalId() {
        return existingHospitalId;
    }
}
