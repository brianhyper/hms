package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.Patient;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One adult on the worklist of identities still pending.
 *
 * <p>Who registered them, and when, come from the audit trail rather than the patient row: the row records what is
 * known about a person, the trail records who wrote it. A flat login rather than the account, for the same reason
 * the record history is flat — the worklist is read to find out which documents to chase, not to carry roles and
 * account state to whoever reads it.
 */
public record IdentityPendingPatientDTO(
    Long patientId,
    String hospitalId,
    String fullName,
    LocalDate dateOfBirth,
    Integer estimatedAge,
    Instant registeredAt,
    String registeredBy
) {
    public static IdentityPendingPatientDTO from(Patient patient, AuditLog registration) {
        return new IdentityPendingPatientDTO(
            patient.getId(),
            patient.getHospitalId(),
            patient.getFullName(),
            patient.getDateOfBirth(),
            patient.getEstimatedAge(),
            registration == null ? null : registration.getPerformedAt(),
            registration == null || registration.getActor() == null ? null : registration.getActor().getLogin()
        );
    }
}
