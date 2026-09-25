package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.NextOfKinRelationship;
import com.hyperbrains.hms.domain.enumeration.Sex;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A correction to a patient's record.
 *
 * <p>Only the fields supplied are considered, so correcting a phone number is a one-field request and
 * the history shows exactly one changed field. The trade-off is that a field cannot be cleared back to
 * empty through this endpoint — which is the right default, because clearing a patient's name or date of
 * birth is nearly always a mistake rather than an intention.
 */
public class CorrectPatientRequestDTO implements Serializable {

    /** Why the record is being changed. Required: a correction nobody can explain is not auditable. */
    @NotBlank
    @Size(max = 10000)
    private String reason;

    @Size(max = 200)
    private String fullName;

    private LocalDate dateOfBirth;

    @Min(0)
    @Max(150)
    private Integer estimatedAge;

    private Sex sex;

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

    @Size(max = 200)
    private String villageEstate;

    /**
     * Clinical facts. Correcting these needs a clinical role, and the service enforces that — the
     * endpoint cannot, because the registration desk legitimately uses the same route for the fields it
     * does own.
     */
    @Size(max = 10000)
    private String knownAllergies;

    @Size(max = 10000)
    private String knownConditions;

    /**
     * Confirms that an identity document number already on another patient's record is genuinely this
     * patient's, and that the duplicate is understood rather than unnoticed.
     *
     * <p>Registration demands exactly this before it will save a second patient against a known
     * document number, and a correction must not be a way round that.
     */
    private Boolean confirmExistingIdentityDocument;

    /** A correction with no field at all is a mistake, not a correction. */
    @AssertTrue(message = "Supply at least one field to correct")
    public boolean isSomethingToCorrect() {
        return (
            fullName != null ||
            dateOfBirth != null ||
            estimatedAge != null ||
            sex != null ||
            sexEstimated != null ||
            phone != null ||
            email != null ||
            identityDocumentType != null ||
            identityDocumentNumber != null ||
            occupation != null ||
            maritalStatus != null ||
            nextOfKinName != null ||
            nextOfKinPhone != null ||
            nextOfKinRelationship != null ||
            villageEstate != null ||
            knownAllergies != null ||
            knownConditions != null
        );
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getVillageEstate() {
        return villageEstate;
    }

    public void setVillageEstate(String villageEstate) {
        this.villageEstate = villageEstate;
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

    public Boolean getConfirmExistingIdentityDocument() {
        return confirmExistingIdentityDocument;
    }

    public void setConfirmExistingIdentityDocument(Boolean confirmExistingIdentityDocument) {
        this.confirmExistingIdentityDocument = confirmExistingIdentityDocument;
    }
}
