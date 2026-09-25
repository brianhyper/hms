package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.view.AddConsultationAddendumRequestDTO;
import com.hyperbrains.hms.service.dto.view.ConsultationAddendumDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import java.util.List;

/**
 * The consultation: the doctor claims a patient, records findings, and finishes.
 *
 * <p>Two correction rules meet here and they are deliberately different from each other:
 * <ul>
 *   <li>While the consultation is in progress, notes are <strong>edited in place</strong>.</li>
 *   <li>Once it is completed, notes can no longer be changed at all — further comment is appended
 *       as an {@link ConsultationAddendumDTO addendum} that leaves the original untouched. This is
 *       the opposite of how a Patient or a VitalSigns correction works, so the two must not be
 *       handled by the same code path.</li>
 * </ul>
 */
public interface ConsultationWorkflowService {

    /**
     * Take a patient from the consultation queue and open a consultation.
     *
     * <p>Idempotent in a useful way: a doctor re-opening the screen gets the consultation that is
     * already open rather than an error or a second one. {@code Visit.consultation} is one-to-one,
     * so a patient can only ever be claimed once — which is what stops two doctors seeing the same
     * patient and the consultation fee being charged twice.
     */
    ConsultationDTO start(Long visitId, StartConsultationRequestDTO request);

    /**
     * Save notes on an in-progress consultation.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the consultation is
     *         already completed.
     */
    ConsultationDTO updateNotes(Long consultationId, UpdateConsultationRequestDTO request);

    /**
     * Finish the consultation: generate the consultation fee immediately, then decide where the
     * visit goes based on what is still outstanding.
     */
    ConsultationDTO complete(Long consultationId, UpdateConsultationRequestDTO request);

    /** Notes appended after completion, oldest first. */
    List<ConsultationAddendumDTO> listAddenda(Long consultationId);

    /** Append a note to a completed consultation. */
    ConsultationAddendumDTO addAddendum(Long consultationId, AddConsultationAddendumRequestDTO request);
}
