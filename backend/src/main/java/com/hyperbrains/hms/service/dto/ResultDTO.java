package com.hyperbrains.hms.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Result} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResultDTO implements Serializable {

    private Long id;
    private String resultValue;
    private String notes;

    @NotNull
    private Instant enteredAt;

    @Size(max = 2000)
    private String imageReference;

    @NotNull
    private UserDTO enteredBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(Instant enteredAt) {
        this.enteredAt = enteredAt;
    }

    public String getImageReference() {
        return imageReference;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }

    public UserDTO getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(UserDTO enteredBy) {
        this.enteredBy = enteredBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultDTO)) {
            return false;
        }

        ResultDTO resultDTO = (ResultDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, resultDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResultDTO{" +
            "id=" + getId() +
            ", resultValue='" + getResultValue() + "'" +
            ", notes='" + getNotes() + "'" +
            ", enteredAt='" + getEnteredAt() + "'" +
            ", imageReference='" + getImageReference() + "'" +
            ", enteredBy=" + getEnteredBy() +
            "}";
    }
}
