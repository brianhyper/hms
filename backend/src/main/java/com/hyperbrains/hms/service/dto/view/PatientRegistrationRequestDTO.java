package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.NextOfKinRelationship;
import com.hyperbrains.hms.domain.enumeration.Sex;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * The fields registration actually supplies. Deliberately a separate type from {@code PatientDTO}.
 *
 * <p>Two fields on {@code PatientDTO} must never be client-controlled:
 * {@code hospitalId} (the system issues it — accepting it would let anyone claim any identifier,
 * and let two records collide on a unique column) and {@code registrationStatus} (which would let
 * a caller flip a merged record back to active). Omitting them here is safer than validating them
 * away later.
 *
 * <p>Used both for the pre-save duplicate check and for the registration itself.
 */
public class PatientRegistrationRequestDTO implements Serializable {

    @NotNull
    @Size(max = 200)
    private String fullName;

    private LocalDate dateOfBirth;

    @Min(0)
    @Max(150)
    private Integer estimatedAge;

    @NotNull
    private Sex sex;

    @NotNull
    private Boolean sexEstimated;

    @Size(max = 32)
    private String phone;

    @Size(max = 254)
    private String email;

    private IdentityDocumentType identityDocumentType;

    @Size(max = 64)
    private String identityDocumentNumber;

    @Size(max = 120)
    private String occupation;

    @Size(max = 50)
    private String maritalStatus;

    @Size(max = 200)
    private String nextOfKinName;

    @Size(max = 32)
    private String nextOfKinPhone;

    private NextOfKinRelationship nextOfKinRelationship;

    private String knownAllergies;

    private String knownConditions;

    @Size(max = 200)
    private String villageEstate;

    /**
     * Required only when an exact identity-document match already exists and Reception chooses to
     * create a second record anyway. Storing the justification is the point: "we saw the match and
     * decided it was a different person" is exactly what an auditor will ask about.
     */
    @Size(max = 10000)
    private String overrideReason;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getEstimatedAge() {
        return estimatedAge;
    }

    public void setEstimatedAge(Integer estimatedAge) {
        this.estimatedAge = estimatedAge;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public IdentityDocumentType getIdentityDocumentType() {
        return identityDocumentType;
    }

    public void setIdentityDocumentType(IdentityDocumentType identityDocumentType) {
        this.identityDocumentType = identityDocumentType;
    }

    public String getIdentityDocumentNumber() {
        return identityDocumentNumber;
    }

    public void setIdentityDocumentNumber(String identityDocumentNumber) {
        this.identityDocumentNumber = identityDocumentNumber;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getNextOfKinName() {
        return nextOfKinName;
    }

    public void setNextOfKinName(String nextOfKinName) {
        this.nextOfKinName = nextOfKinName;
    }

    public String getNextOfKinPhone() {
        return nextOfKinPhone;
    }

    public void setNextOfKinPhone(String nextOfKinPhone) {
        this.nextOfKinPhone = nextOfKinPhone;
    }

    public NextOfKinRelationship getNextOfKinRelationship() {
        return nextOfKinRelationship;
    }

    public void setNextOfKinRelationship(NextOfKinRelationship nextOfKinRelationship) {
        this.nextOfKinRelationship = nextOfKinRelationship;
    }

    public String getKnownAllergies() {
        return knownAllergies;
    }

    public void setKnownAllergies(String knownAllergies) {
        this.knownAllergies = knownAllergies;
    }

    public String getKnownConditions() {
        return knownConditions;
    }

    public void setKnownConditions(String knownConditions) {
        this.knownConditions = knownConditions;
    }

    public String getVillageEstate() {
        return villageEstate;
    }

    public void setVillageEstate(String villageEstate) {
        this.villageEstate = villageEstate;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }
}
