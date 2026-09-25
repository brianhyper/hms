package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.DuplicateCheckResultDTO;
import com.hyperbrains.hms.service.dto.view.EmergencyIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationResultDTO;
import com.hyperbrains.hms.service.workflow.PatientRegistrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Action endpoints for patient registration.
 *
 * <p>These are separate from the generated {@code /api/patients} CRUD on purpose. Registration is
 * not a create: it issues an identifier, runs two duplicate checks, and may write audit entries.
 * A plain {@code POST /api/patients} cannot express any of that, and letting clients set
 * {@code hospitalId} or {@code registrationStatus} directly would bypass all of it.
 *
 * <p>Authorization is declared in the {@code PHASE 1 RBAC TABLE} of
 * {@link com.hyperbrains.hms.config.SecurityConfiguration}, which is the single place role-to-path
 * mappings live.
 */
@RestController
@RequestMapping("/api/patient-registration")
public class PatientRegistrationResource {

    private static final Logger LOG = LoggerFactory.getLogger(PatientRegistrationResource.class);

    private final PatientRegistrationService patientRegistrationService;

    public PatientRegistrationResource(PatientRegistrationService patientRegistrationService) {
        this.patientRegistrationService = patientRegistrationService;
    }

    /**
     * {@code POST /patient-registration/duplicate-check} : run both duplicate checks without saving.
     */
    @PostMapping("/duplicate-check")
    public ResponseEntity<DuplicateCheckResultDTO> checkForDuplicates(@Valid @RequestBody PatientRegistrationRequestDTO request) {
        LOG.debug("REST request to check for duplicate patients");
        return ResponseEntity.ok(patientRegistrationService.checkForDuplicates(request));
    }

    /**
     * {@code POST /patient-registration/register} : register a patient.
     *
     * <p>Returns {@code 409 Conflict} when an exact identity-document match exists and no override
     * reason was supplied; the problem body carries the matched patient's id and hospital id.
     */
    @PostMapping("/register")
    public ResponseEntity<PatientRegistrationResultDTO> register(@Valid @RequestBody PatientRegistrationRequestDTO request) {
        LOG.debug("REST request to register a patient");
        return ResponseEntity.status(HttpStatus.CREATED).body(patientRegistrationService.register(request));
    }

    /**
     * {@code POST /patient-registration/emergency-intake} : unidentified or unconscious patient.
     *
     * <p>Issues a temporary {@code UNK-YYYY-NNNN} identifier and marks the registration incomplete,
     * so clinical work can start immediately.
     */
    @PostMapping("/emergency-intake")
    public ResponseEntity<PatientRegistrationResultDTO> emergencyIntake(@Valid @RequestBody EmergencyIntakeRequestDTO request) {
        LOG.debug("REST request for emergency patient intake");
        return ResponseEntity.status(HttpStatus.CREATED).body(patientRegistrationService.emergencyIntake(request));
    }
}
