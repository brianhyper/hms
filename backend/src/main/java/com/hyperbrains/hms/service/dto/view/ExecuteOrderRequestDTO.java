package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Min;
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
 *
 * <p>{@code quantity} is how much of the drug this dose took off the shelf. It is asked for rather than
 * derived: a recurring drug order is one prescription for the whole course, so taking the course quantity
 * here would empty the shelf on the first dose of a twice-daily drug, and a fixed per-dose amount would be a
 * number nobody prescribed. The ward is the only place that knows what it actually gave.
 */
public class ExecuteOrderRequestDTO implements Serializable {

    @Size(max = 10000)
    private String notes;

    @Min(1)
    private Integer quantity;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
