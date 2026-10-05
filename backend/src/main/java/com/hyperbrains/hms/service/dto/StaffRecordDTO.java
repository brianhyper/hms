package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.StaffRecord} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StaffRecordDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String fullName;

    /**
     * The identity this file is keyed on, when it is known.
     *
     * <p>Not required: the hospital holds a file on everyone who works in it, including people with no system account
     * and no number to hand. The column and the entity were changed for that reason, and this annotation was the last
     * place still refusing the request — found by the test that was owed, which asserted a file with no number saves
     * and got a 400 back.
     */
    @Size(max = 32)
    private String nationalId;

    @Size(max = 120)
    private String jobTitle;

    @Size(max = 30)
    private String contactPhone;

    @Size(max = 254)
    private String contactEmail;

    @NotNull
    @Schema(description = "Used by Phase 4's leave entitlement, which counts service from here", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate employmentStartDate;

    @NotNull
    private StaffRecordStatus status;

    @NotNull
    private DepartmentDTO department;

    /** The account this person signs in with, or null for the many members of staff who have none. */
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDate getEmploymentStartDate() {
        return employmentStartDate;
    }

    public void setEmploymentStartDate(LocalDate employmentStartDate) {
        this.employmentStartDate = employmentStartDate;
    }

    public StaffRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StaffRecordStatus status) {
        this.status = status;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StaffRecordDTO)) {
            return false;
        }

        StaffRecordDTO staffRecordDTO = (StaffRecordDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, staffRecordDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "StaffRecordDTO{" +
            "id=" + getId() +
            ", fullName='" + getFullName() + "'" +
            ", nationalId='" + getNationalId() + "'" +
            ", jobTitle='" + getJobTitle() + "'" +
            ", contactPhone='" + getContactPhone() + "'" +
            ", contactEmail='" + getContactEmail() + "'" +
            ", employmentStartDate='" + getEmploymentStartDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", department=" + getDepartment() +
            ", user=" + getUser() +
            "}";
    }
}
