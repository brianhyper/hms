package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Diagnosis} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiagnosisDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 64)
    private String code;

    @NotNull
    @Size(max = 200)
    private String name;

    @NotNull
    private Boolean active;

    private Set<ConsultationDTO> consultationses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<ConsultationDTO> getConsultationses() {
        return consultationses;
    }

    public void setConsultationses(Set<ConsultationDTO> consultationses) {
        this.consultationses = consultationses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiagnosisDTO)) {
            return false;
        }

        DiagnosisDTO diagnosisDTO = (DiagnosisDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, diagnosisDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiagnosisDTO{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", active='" + getActive() + "'" +
            ", consultationses=" + getConsultationses() +
            "}";
    }
}
