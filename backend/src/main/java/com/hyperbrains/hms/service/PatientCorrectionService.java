package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.view.CorrectPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientCorrectionResultDTO;

/**
 * Correcting a patient record in place, with a reason and a history.
 *
 * <p>This is one of the two correction patterns the specification insists must not be mixed up. A
 * patient's recorded phone number <em>is</em> corrected: the stored value is the single source of truth,
 * and what happened to it is preserved separately so the change can be explained later.
 *
 * <p>The consultation is the other pattern and lives elsewhere: past a certain point its notes are never
 * overwritten, and a change becomes an addendum. Applying edit-in-place logic here, or addendum logic to a
 * patient's next of kin, are both implementation mistakes against what was decided.
 */
public interface PatientCorrectionService {

    /**
     * Apply a correction and record exactly which fields it changed.
     *
     * @throws com.hyperbrains.hms.service.ExactPatientMatchException if the new identity document number
     *         already belongs to another patient and the caller has not confirmed the duplicate
     * @throws org.springframework.security.access.AccessDeniedException if the caller is trying to change
     *         a clinical fact without a clinical role
     */
    PatientCorrectionResultDTO correct(Long patientId, CorrectPatientRequestDTO request);
}
