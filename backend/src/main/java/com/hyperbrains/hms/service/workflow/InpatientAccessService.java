package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.MyPatientViewDTO;
import java.util.List;

/**
 * Who may see which inpatient.
 *
 * <p>A role check cannot express this rule, which is why it exists as a service: the answer depends on the
 * individual patient — are you their responsible doctor, or are you covering the ward they are in right
 * now. §3 of the specification states it as "a doctor sees admissions where they are primaryDoctor, or any
 * admission whose current ward matches a ward they are covering", and the current ward is read live from
 * the bed rather than cached, so a transfer moves the patient between doctors' lists automatically.
 *
 * <p>Nurses, administrators and the super-admin see every inpatient: the specification puts no row rule on
 * them, and a nurse who could only see the patients they personally had been assigned would be unable to do
 * the job.
 */
public interface InpatientAccessService {

    /**
     * The open stays the caller may see, each carrying why it is on their list.
     *
     * <p>Longest-stay first, because that is the order a doctor works through a ward round.
     */
    List<MyPatientViewDTO> myPatients();

    /**
     * Refuses unless the caller has a claim on this stay.
     *
     * <p>Used by everything on the ward that is reached by an admission id — the chart, the order sheet —
     * because a rule that only filters a list is decorative: the id is guessable, and the row it names is the
     * whole point.
     *
     * @throws org.springframework.security.access.AccessDeniedException when the caller has no claim
     */
    void requireMayView(Long admissionId);
}
