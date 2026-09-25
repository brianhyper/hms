package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.view.MergePatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientMergeResultDTO;

/**
 * Connecting a temporary patient record to the confirmed one.
 *
 * <p>This is the only action in the system that combines two identities. Everything clinical recorded
 * under the temporary record — vitals, orders and their results, consultations, prescriptions, referrals,
 * bills — ends up belonging to the real patient, and the temporary record stops being a patient and
 * becomes a pointer to them.
 *
 * <p>It is not a deletion, and it is not reversible through this API. The temporary record survives as the
 * explanation of where that history came from.
 */
public interface PatientMergeService {

    /**
     * Merge the temporary record into the confirmed one.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the reason is missing, the two
     *         ids are the same, either record has already been merged, or the pair is not a temporary
     *         record adopting a confirmed one
     */
    PatientMergeResultDTO merge(MergePatientRequestDTO request);
}
