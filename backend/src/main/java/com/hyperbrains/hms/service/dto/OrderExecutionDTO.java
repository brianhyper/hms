package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.OrderExecution} entity.
 */
@Schema(description = "One dose or one action actually carried out. The nurse's record of what\nhappened, never a schedule of what should.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrderExecutionDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant executedAt;

    @Size(max = 10000)
    private String notes;

    @NotNull
    private DoctorOrderDTO doctorOrder;

    @NotNull
    private UserDTO executedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DoctorOrderDTO getDoctorOrder() {
        return doctorOrder;
    }

    public void setDoctorOrder(DoctorOrderDTO doctorOrder) {
        this.doctorOrder = doctorOrder;
    }

    public UserDTO getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(UserDTO executedBy) {
        this.executedBy = executedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderExecutionDTO)) {
            return false;
        }

        OrderExecutionDTO orderExecutionDTO = (OrderExecutionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, orderExecutionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OrderExecutionDTO{" +
            "id=" + getId() +
            ", executedAt='" + getExecutedAt() + "'" +
            ", notes='" + getNotes() + "'" +
            ", doctorOrder=" + getDoctorOrder() +
            ", executedBy=" + getExecutedBy() +
            "}";
    }
}
