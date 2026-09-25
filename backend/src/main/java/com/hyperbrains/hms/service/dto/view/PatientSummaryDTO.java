package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Identifying details for a patient, and nothing else.
 *
 * <p>This is the narrowest useful patient view, and it exists because "patient" does not mean the
 * same thing to every role. It is what a Lab or Finance user is allowed to see: enough to know
 * whose specimen or bill this is, without the clinical record. Returning the full {@code Patient}
 * instead would leak allergies, conditions, next of kin and identity documents to stations that
 * have no clinical need for them.
 */
public class PatientSummaryDTO implements Serializable {

    private Long id;

    private String hospitalId;

    private String fullName;

    private LocalDate dateOfBirth;

    private Integer estimatedAge;

    private Sex sex;

    private String phone;

    private RegistrationStatus registrationStatus;

    public static PatientSummaryDTO from(Patient patient) {
        if (patient == null) {
            return null;
        }
        PatientSummaryDTO dto = new PatientSummaryDTO();
        dto.id = patient.getId();
        dto.hospitalId = patient.getHospitalId();
        dto.fullName = patient.getFullName();
        dto.dateOfBirth = patient.getDateOfBirth();
        dto.estimatedAge = patient.getEstimatedAge();
        dto.sex = patient.getSex();
        dto.phone = patient.getPhone();
        dto.registrationStatus = patient.getRegistrationStatus();
        return dto;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
}
