package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.DuplicateCheckResultDTO;
import com.hyperbrains.hms.service.dto.view.EmergencyIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationResultDTO;

/**
 * Patient registration: the first point at which the system decides whether two people are the
 * same person, and the only place hospital identifiers are issued.
 *
 * <p>Two intake paths exist and they behave differently on purpose:
 * <ul>
 *   <li>{@link #register} — normal desk registration. An exact identity-document match blocks the
 *       save until Reception explicitly overrides it.</li>
 *   <li>{@link #emergencyIntake} — unconscious, unidentified, or a minor without guardian details.
 *       Issues a temporary identifier and marks the registration incomplete. Registration
 *       completeness is deliberately not a precondition for treatment.</li>
 * </ul>
 */
public interface PatientRegistrationService {

    /**
     * Run both duplicate checks without writing anything.
     *
     * <p>Exists so the UI can show matches while Reception is still filling the form, rather than
     * only after a save has already happened.
     */
    DuplicateCheckResultDTO checkForDuplicates(PatientRegistrationRequestDTO request);

    /**
     * Register a patient.
     *
     * @throws com.hyperbrains.hms.service.ExactPatientMatchException if an exact identity-document
     *         match exists and {@code overrideReason} was not supplied.
     */
    PatientRegistrationResultDTO register(PatientRegistrationRequestDTO request);

    /**
     * Create an unidentified-patient record so treatment can begin immediately.
     */
    PatientRegistrationResultDTO emergencyIntake(EmergencyIntakeRequestDTO request);
}
