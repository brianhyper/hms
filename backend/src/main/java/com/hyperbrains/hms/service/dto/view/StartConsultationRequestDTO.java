package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** A doctor taking a patient from the consultation queue. */
public class StartConsultationRequestDTO implements Serializable {

    /** Required only when this patient is not at the head of the consultation queue. */
    @Size(max = 10000)
    private String queueSkipReason;

    public String getQueueSkipReason() {
        return queueSkipReason;
    }

    public void setQueueSkipReason(String queueSkipReason) {
        this.queueSkipReason = queueSkipReason;
    }
}
