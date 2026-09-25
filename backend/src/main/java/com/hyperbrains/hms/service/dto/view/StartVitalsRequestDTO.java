package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A nurse taking a patient from the vitals queue.
 */
public class StartVitalsRequestDTO implements Serializable {

    /**
     * Required only when this patient is not at the head of the queue.
     *
     * <p>Selecting out of order is allowed — staff can see things the system cannot — but it is
     * recorded, so a patient overtaken by someone else always has an explanation attached to the
     * visit rather than showing up only as an unexplained gap in the waiting order.
     */
    @Size(max = 10000)
    private String queueSkipReason;

    public String getQueueSkipReason() {
        return queueSkipReason;
    }

    public void setQueueSkipReason(String queueSkipReason) {
        this.queueSkipReason = queueSkipReason;
    }
}
