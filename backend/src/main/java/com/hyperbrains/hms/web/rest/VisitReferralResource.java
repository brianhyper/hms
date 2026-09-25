package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.CreateReferralRequestDTO;
import com.hyperbrains.hms.service.dto.view.ReferralViewDTO;
import com.hyperbrains.hms.service.workflow.ReferralWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Referring a patient out, and producing the letter that goes with it.
 *
 * <p>Two distinct capabilities live here, and they are deliberately separated by route. Creating a
 * referral ends this hospital's clinical involvement and moves the visit toward payment; fetching the
 * letter is a read that changes nothing. Emailing the letter is the only thing that marks it
 * dispatched, because until it has actually left, this hospital still has to chase it.
 */
@RestController
@RequestMapping("/api/visit-referrals")
public class VisitReferralResource {

    private final ReferralWorkflowService referralService;

    public VisitReferralResource(ReferralWorkflowService referralService) {
        this.referralService = referralService;
    }

    @PostMapping("/{visitId}/create")
    public ResponseEntity<ReferralViewDTO> create(
        @PathVariable Long visitId,
        @Valid @RequestBody CreateReferralRequestDTO request
    ) {
        ReferralViewDTO referral = referralService.create(visitId, request);
        return ResponseEntity.created(URI.create("/api/referrals/" + referral.referralId())).body(referral);
    }

    /** Everything this visit was referred on, for the treating clinician. */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<ReferralViewDTO>> forVisit(@PathVariable Long visitId) {
        return ResponseEntity.ok(referralService.forVisit(visitId));
    }

    /**
     * The letter as a downloadable PDF.
     *
     * <p>A read: generating a letter does not mean it was sent, so this leaves the referral pending.
     * The filename is sent as an attachment so a printed copy keeps the patient's number on it.
     */
    @GetMapping("/{referralId}/letter")
    public ResponseEntity<byte[]> letter(@PathVariable Long referralId) {
        ReferralWorkflowService.RenderedDocument document = referralService.renderLetter(referralId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(document.filename()))
            .body(document.content());
    }

    /**
     * Email the letter to the destination.
     *
     * <p>Only success marks the referral dispatched; a failed send leaves it pending and reports the
     * failure, so nobody assumes the receiving facility has something it does not.
     */
    @PostMapping("/{referralId}/email")
    public ResponseEntity<ReferralViewDTO> email(@PathVariable Long referralId) {
        return ResponseEntity.ok(referralService.emailLetter(referralId));
    }
}
