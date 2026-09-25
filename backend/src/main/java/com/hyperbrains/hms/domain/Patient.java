package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.NextOfKinRelationship;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Patient.
 */
@Entity
@Table(name = "patient")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Patient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    /**
     * Permanent business identifier. For unidentified records this temporarily
     * contains the generated UNK-YYYY-#### identifier.
     */
    @NotNull
    @Size(max = 32)
    @Column(name = "hospital_id", length = 32, nullable = false, unique = true)
    private String hospitalId;

    @NotNull
    @Size(max = 200)
    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Min(value = 0)
    @Max(value = 150)
    @Column(name = "estimated_age")
    private Integer estimatedAge;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private Sex sex;

    @NotNull
    @Column(name = "sex_estimated", nullable = false)
    private Boolean sexEstimated;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @Size(max = 254)
    @Column(name = "email", length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_document_type")
    private IdentityDocumentType identityDocumentType;

    @Size(max = 64)
    @Column(name = "identity_document_number", length = 64)
    private String identityDocumentNumber;

    @Size(max = 120)
    @Column(name = "occupation", length = 120)
    private String occupation;

    @Size(max = 50)
    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Size(max = 200)
    @Column(name = "next_of_kin_name", length = 200)
    private String nextOfKinName;

    @Size(max = 32)
    @Column(name = "next_of_kin_phone", length = 32)
    private String nextOfKinPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_of_kin_relationship")
    private NextOfKinRelationship nextOfKinRelationship;
    @Column(name = "known_allergies")
    private String knownAllergies;
    @Column(name = "known_conditions")
    private String knownConditions;

    @Size(max = 200)
    @Column(name = "village_estate", length = 200)
    private String villageEstate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "mergedIntoPatient" }, allowSetters = true)
    private Patient mergedIntoPatient;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Patient id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHospitalId() {
        return this.hospitalId;
    }

    public Patient hospitalId(String hospitalId) {
        this.setHospitalId(hospitalId);
        return this;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Patient fullName(String fullName) {
        this.setFullName(fullName);
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    public Patient dateOfBirth(LocalDate dateOfBirth) {
        this.setDateOfBirth(dateOfBirth);
        return this;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getEstimatedAge() {
        return this.estimatedAge;
    }

    public Patient estimatedAge(Integer estimatedAge) {
        this.setEstimatedAge(estimatedAge);
        return this;
    }

    public void setEstimatedAge(Integer estimatedAge) {
        this.estimatedAge = estimatedAge;
    }

    public Sex getSex() {
        return this.sex;
    }

    public Patient sex(Sex sex) {
        this.setSex(sex);
        return this;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Boolean getSexEstimated() {
        return this.sexEstimated;
    }

    public Patient sexEstimated(Boolean sexEstimated) {
        this.setSexEstimated(sexEstimated);
        return this;
    }

    public void setSexEstimated(Boolean sexEstimated) {
        this.sexEstimated = sexEstimated;
    }

    public String getPhone() {
        return this.phone;
    }

    public Patient phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return this.email;
    }

    public Patient email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public IdentityDocumentType getIdentityDocumentType() {
        return this.identityDocumentType;
    }

    public Patient identityDocumentType(IdentityDocumentType identityDocumentType) {
        this.setIdentityDocumentType(identityDocumentType);
        return this;
    }

    public void setIdentityDocumentType(IdentityDocumentType identityDocumentType) {
        this.identityDocumentType = identityDocumentType;
    }

    public String getIdentityDocumentNumber() {
        return this.identityDocumentNumber;
    }

    public Patient identityDocumentNumber(String identityDocumentNumber) {
        this.setIdentityDocumentNumber(identityDocumentNumber);
        return this;
    }

    public void setIdentityDocumentNumber(String identityDocumentNumber) {
        this.identityDocumentNumber = identityDocumentNumber;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public Patient occupation(String occupation) {
        this.setOccupation(occupation);
        return this;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getMaritalStatus() {
        return this.maritalStatus;
    }

    public Patient maritalStatus(String maritalStatus) {
        this.setMaritalStatus(maritalStatus);
        return this;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getNextOfKinName() {
        return this.nextOfKinName;
    }

    public Patient nextOfKinName(String nextOfKinName) {
        this.setNextOfKinName(nextOfKinName);
        return this;
    }

    public void setNextOfKinName(String nextOfKinName) {
        this.nextOfKinName = nextOfKinName;
    }

    public String getNextOfKinPhone() {
        return this.nextOfKinPhone;
    }

    public Patient nextOfKinPhone(String nextOfKinPhone) {
        this.setNextOfKinPhone(nextOfKinPhone);
        return this;
    }

    public void setNextOfKinPhone(String nextOfKinPhone) {
        this.nextOfKinPhone = nextOfKinPhone;
    }

    public NextOfKinRelationship getNextOfKinRelationship() {
        return this.nextOfKinRelationship;
    }

    public Patient nextOfKinRelationship(NextOfKinRelationship nextOfKinRelationship) {
        this.setNextOfKinRelationship(nextOfKinRelationship);
        return this;
    }

    public void setNextOfKinRelationship(NextOfKinRelationship nextOfKinRelationship) {
        this.nextOfKinRelationship = nextOfKinRelationship;
    }

    public String getKnownAllergies() {
        return this.knownAllergies;
    }

    public Patient knownAllergies(String knownAllergies) {
        this.setKnownAllergies(knownAllergies);
        return this;
    }

    public void setKnownAllergies(String knownAllergies) {
        this.knownAllergies = knownAllergies;
    }

    public String getKnownConditions() {
        return this.knownConditions;
    }

    public Patient knownConditions(String knownConditions) {
        this.setKnownConditions(knownConditions);
        return this;
    }

    public void setKnownConditions(String knownConditions) {
        this.knownConditions = knownConditions;
    }

    public String getVillageEstate() {
        return this.villageEstate;
    }

    public Patient villageEstate(String villageEstate) {
        this.setVillageEstate(villageEstate);
        return this;
    }

    public void setVillageEstate(String villageEstate) {
        this.villageEstate = villageEstate;
    }

    public RegistrationStatus getRegistrationStatus() {
        return this.registrationStatus;
    }

    public Patient registrationStatus(RegistrationStatus registrationStatus) {
        this.setRegistrationStatus(registrationStatus);
        return this;
    }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public Patient getMergedIntoPatient() {
        return this.mergedIntoPatient;
    }

    public void setMergedIntoPatient(Patient patient) {
        this.mergedIntoPatient = patient;
    }

    public Patient mergedIntoPatient(Patient patient) {
        this.setMergedIntoPatient(patient);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patient)) {
            return false;
        }
        return getId() != null && getId().equals(((Patient) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Patient{" +
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
            "}";
    }
}
