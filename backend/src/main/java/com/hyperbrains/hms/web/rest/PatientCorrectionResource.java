package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.PatientCorrectionService;
import com.hyperbrains.hms.service.dto.view.CorrectPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientCorrectionResultDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Correcting a patient record.
 *
 * <p>Separate from the generated {@code /api/patients} update because a correction is not an update.
 * This route demands a reason and records which fields changed, so the record keeps a history of what was
 * altered and why. The generated endpoint's writes are closed to every role but the super-admin for
 * exactly that reason: it can change a patient's name or allergies with no explanation attached.
 */
@RestController
@RequestMapping("/api/patient-corrections")
public class PatientCorrectionResource {

    private final PatientCorrectionService patientCorrectionService;

    public PatientCorrectionResource(PatientCorrectionService patientCorrectionService) {
        this.patientCorrectionService = patientCorrectionService;
    }

    /**
     * Correct the supplied fields, keeping the previous values in the record's history.
     *
     * <p>Reachable by the registration desk and by clinicians, but not for the same fields: a clinical
     * fact such as a recorded allergy is refused for a non-clinical caller with a 403 rather than being
     * silently accepted.
     *
     * <p>A correction never rewrites history. It moves the single stored value, and the history keeps
     * what was there before — the opposite of a consultation, whose notes become addenda once the
     * encounter has moved on.
     */
    @PutMapping("/{patientId}")
    public ResponseEntity<PatientCorrectionResultDTO> correct(
        @PathVariable Long patientId,
        @Valid @RequestBody CorrectPatientRequestDTO request
    ) {
        return ResponseEntity.ok(patientCorrectionService.correct(patientId, request));
    }
}
