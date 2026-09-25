package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Ward} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WardDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String name;

    @Size(max = 120)
    private String location;

    @NotNull
    @Schema(
        description = "A ward that no longer takes patients is deactivated, never deleted: its beds\nand the admissions that used them have to survive in the history.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean active;

    @NotNull
    private DepartmentDTO department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WardDTO)) {
            return false;
        }

        WardDTO wardDTO = (WardDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, wardDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WardDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", location='" + getLocation() + "'" +
            ", active='" + getActive() + "'" +
            ", department=" + getDepartment() +
            "}";
    }
}
