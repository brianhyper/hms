package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A nurse recording that an order was carried out.
 *
 * <p>{@code notes} is the part that matters clinically, and it is why an execution is a record rather than a
 * tick: "patient refused", "delayed 30 minutes, cannula resited" and "vomited the dose" are the reasons a
 * course fails, and none of them is visible from the fact that a dose was due.
 *
 * <p>There is no {@code executedAt} field. The moment is the server's, not the request's: a client clock is
 * not evidence of when a patient was given something.
 */
public class ExecuteOrderRequestDTO implements Serializable {

    @Size(max = 10000)
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
