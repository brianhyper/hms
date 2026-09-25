package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.CreateReferralRequestDTO;
import com.hyperbrains.hms.service.dto.view.ReferralViewDTO;
import java.util.List;

/**
 * Referring a patient out.
 *
 * <p>Named {@code ...Workflow...} rather than {@code ReferralService} because the generated CRUD service
 * already owns that bean name.
 *
 * <p>A referral does two separate things, and the specification is explicit that they must not be
 * confused: it ends this hospital's <em>clinical</em> involvement, which moves the visit toward payment
 * based on what was actually done; and it produces a document to send on, which is independent of that
 * billing effect. Either half can fail without the other being undone.
 */
public interface ReferralWorkflowService {

    /**
     * Refer the patient, and move the visit toward payment.
     *
     * <p>The visit moves as though nothing were outstanding, so outstanding tests do not hold the visit
     * back waiting for work that the referral has already decided is not this hospital's to finish.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the visit is not at a point
     *         where the local journey can be ended, or the department does not exist
     */
    ReferralViewDTO create(Long visitId, CreateReferralRequestDTO request);

    /** Everything a visit was referred on, oldest first. */
    List<ReferralViewDTO> forVisit(Long visitId);

    /** The letter, ready to be streamed to a browser. Generating it changes nothing. */
    RenderedDocument renderLetter(Long referralId);

    /**
     * Email the letter to the destination and mark the referral dispatched.
     *
     * <p>Fails loudly if the letter could not be sent: a referral that looks delivered but never
     * arrived means a patient turned away from the receiving facility.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the referral has no
     *         destination address, or the send failed
     */
    ReferralViewDTO emailLetter(Long referralId);

    /** A generated document, ready to be streamed in a response or attached to a message. */
    record RenderedDocument(String filename, String contentType, byte[] content) {}
}
