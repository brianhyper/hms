package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.NextOfKinRelationship;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Patient} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PatientDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 32)
    @Schema(
        description = "Permanent business identifier. For unidentified records this temporarily\ncontains the generated UNK-YYYY-#### identifier.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String hospitalId;

    @NotNull
    @Size(max = 200)
    private String fullName;

    private LocalDate dateOfBirth;

    @Min(value = 0)
    @Max(value = 150)
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

    @NotNull
    private RegistrationStatus registrationStatus;

    private PatientDTO mergedIntoPatient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

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

    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public PatientDTO getMergedIntoPatient() {
        return mergedIntoPatient;
    }

    public void setMergedIntoPatient(PatientDTO mergedIntoPatient) {
        this.mergedIntoPatient = mergedIntoPatient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PatientDTO)) {
            return false;
        }

        PatientDTO patientDTO = (PatientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, patientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PatientDTO{" +
            "id=" + getId() +
            ", hospitalId='" + getHospitalId() + "'" +
            ", fullName='" + getFullName() + "'" +
            ", dateOfBirth='" + getDateOfBirth() + "'" +
            ", estimatedAge=" + getEstimatedAge() +
            ", sex='" + getSex() + "'" +
            ", sexEstimated='" + getSexEstimated() + "'" +
            ", phone='" + getPhone() + "'" +
            ", email='" + getEmail() + "'" +
            ", identityDocumentType='" + getIdentityDocumentType() + "'" +
            ", identityDocumentNumber='" + getIdentityDocumentNumber() + "'" +
            ", occupation='" + getOccupation() + "'" +
            ", maritalStatus='" + getMaritalStatus() + "'" +
            ", nextOfKinName='" + getNextOfKinName() + "'" +
            ", nextOfKinPhone='" + getNextOfKinPhone() + "'" +
            ", nextOfKinRelationship='" + getNextOfKinRelationship() + "'" +
            ", knownAllergies='" + getKnownAllergies() + "'" +
            ", knownConditions='" + getKnownConditions() + "'" +
            ", villageEstate='" + getVillageEstate() + "'" +
            ", registrationStatus='" + getRegistrationStatus() + "'" +
            ", mergedIntoPatient=" + getMergedIntoPatient() +
            "}";
    }
}
