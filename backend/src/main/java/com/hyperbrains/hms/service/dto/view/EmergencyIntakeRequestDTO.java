package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.Sex;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Emergency intake: an unconscious or unidentified patient, or a minor whose guardian details
 * cannot be obtained.
 *
 * <p>There is deliberately no identity-document field and no name requirement. Requiring either
 * would mean refusing to treat the patients this path exists for. The record is created with a
 * generated {@code UNK-YYYY-####} identifier and an incomplete registration status, and clinical
 * work proceeds — registration completeness is not a precondition for treatment here.
 */
public class EmergencyIntakeRequestDTO implements Serializable {

    /** Optional: an unconscious patient cannot give a name. Defaults to {@code Unknown}. */
    @Size(max = 200)
    private String fullName;

    @NotNull
    private Sex sex;

    @NotNull
    private Boolean sexEstimated;

    @Min(0)
    @Max(150)
    private Integer estimatedAge;

    /** Free-text context, recorded on the audit entry explaining why intake was anonymous. */
    @Size(max = 10000)
    private String intakeNotes;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Boolean getSexEstimated() {
        return sexEstimated;
    }

    public void setSexEstimated(Boolean sexEstimated) {
        this.sexEstimated = sexEstimated;
    }

    public Integer getEstimatedAge() {
        return estimatedAge;
    }

    public void setEstimatedAge(Integer estimatedAge) {
        this.estimatedAge = estimatedAge;
    }

    public String getIntakeNotes() {
        return intakeNotes;
    }

    public void setIntakeNotes(String intakeNotes) {
        this.intakeNotes = intakeNotes;
    }
}
