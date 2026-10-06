package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.DischargeSignOffRequestDTO;
import com.hyperbrains.hms.service.dto.view.DischargeViewDTO;
import com.hyperbrains.hms.service.workflow.InpatientDischargeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Ending a stay, in two signatures.
 *
 * <p>Two routes and not one, which is Phase 2 §7's own requirement: a single endpoint taking two names would let one
 * caller claim both sign-offs, and the second signature exists precisely to stop that. Each route records its own
 * actor through the security context, so the trail says which two people released this patient.
 *
 * <p>There is no route for death in hospital or for discharge against medical advice. §7 says neither is a discharge
 * and neither should need two signatures, and what they should be instead is §11 question 4 — unanswered, and
 * therefore not invented here.
 */
@RestController
@RequestMapping("/api/admissions")
public class InpatientDischargeResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientDischargeResource.class);

    private final InpatientDischargeService inpatientDischargeService;

    public InpatientDischargeResource(InpatientDischargeService inpatientDischargeService) {
        this.inpatientDischargeService = inpatientDischargeService;
    }

    /**
     * {@code POST  /admissions/:admissionId/discharge/doctor} : the doctor's half of a discharge.
     *
     * @param admissionId the stay being ended.
     * @param request the note, and the acknowledgement required while orders are still running.
     * @return the discharge as it stands after this signature, including any order still running.
     */
    @PostMapping("/{admissionId}/discharge/doctor")
    public DischargeViewDTO doctorSignsOff(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody DischargeSignOffRequestDTO request
    ) {
        LOG.debug("REST request for the doctor's discharge sign-off on admission {}", admissionId);
        return inpatientDischargeService.doctorSignsOff(admissionId, request);
    }

    /**
     * {@code POST  /admissions/:admissionId/discharge/nurse} : the nurse's half of a discharge.
     *
     * @param admissionId the stay being ended.
     * @param request the note, and the acknowledgement required while orders are still running.
     * @return the discharge as it stands after this signature, including any order still running.
     */
    @PostMapping("/{admissionId}/discharge/nurse")
    public DischargeViewDTO nurseSignsOff(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody DischargeSignOffRequestDTO request
    ) {
        LOG.debug("REST request for the nurse's discharge sign-off on admission {}", admissionId);
        return inpatientDischargeService.nurseSignsOff(admissionId, request);
    }
}
