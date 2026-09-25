package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.PatientMergeService;
import com.hyperbrains.hms.service.dto.view.MergePatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientMergeResultDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Merging a temporary patient record into the confirmed one.
 *
 * <p>Not a delete and not an update: it moves clinical history between two identities and leaves the
 * temporary record behind as the explanation of where that history came from. Both ids are in the body so
 * the direction is never implied.
 */
@RestController
@RequestMapping("/api/patient-merges")
public class PatientMergeResource {

    private final PatientMergeService patientMergeService;

    public PatientMergeResource(PatientMergeService patientMergeService) {
        this.patientMergeService = patientMergeService;
    }

    /**
     * Merge the temporary record into the confirmed one.
     *
     * <p>Restricted to the registration desk and the super-admin, which is who the specification assigns to
     * it. It is the one action that destroys an identity, so it is deliberately not something every
     * clinical role can reach.
     */
    @PostMapping
    public ResponseEntity<PatientMergeResultDTO> merge(@Valid @RequestBody MergePatientRequestDTO request) {
        return ResponseEntity.ok(patientMergeService.merge(request));
    }
}
