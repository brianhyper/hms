package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.DispenseRecordDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import java.util.List;

/**
 * Handing medicine over at the counter.
 *
 * <p>Named {@code ...Workflow...} rather than {@code DispenseService} because the generated CRUD
 * service already owns that bean name.
 *
 * <p>This is the last step of the pharmacy guarantee. A prescription only reaches this service once its
 * bill has been settled, and reaching it is the only thing that takes medicine off the shelf — so the
 * order of payment and hand-over is enforced by the state machine rather than by anyone remembering to
 * check.
 */
public interface DispenseWorkflowService {

    /**
     * Hand over part or all of a prescription.
     *
     * <p>Partial by design: a counter gives out what it has and the rest later. The prescription's own
     * status follows from what is left rather than being set by hand.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the prescription has not
     *         been paid for, has been withdrawn, is already fully handed over, or if any line asks for
     *         more than is still outstanding on it
     */
    PrescriptionViewDTO dispense(Long prescriptionId, DispenseRequestDTO request);

    /**
     * Everything handed over for a prescription, oldest hand-over first.
     *
     * <p>This is the only record of what physically left the shelf, and it is what answers a query
     * about a patient who says they never received their medicine.
     */
    List<DispenseRecordDTO> history(Long prescriptionId);
}
