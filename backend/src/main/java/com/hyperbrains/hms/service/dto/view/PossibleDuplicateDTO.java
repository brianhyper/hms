package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.service.rules.PatientDuplicateMatcher;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * A patient already on file that the one being registered may be the same person as.
 *
 * <p>{@code nameSimilarity} and {@code reasons} are returned on purpose. Reception is being asked
 * to make a judgement call about two human beings, so the reasoning has to be visible — a bare
 * "possible duplicate" with no explanation trains people to click through it.
 */
public class PossibleDuplicateDTO implements Serializable {

    private Long patientId;

    private String hospitalId;

    private String fullName;

    private LocalDate dateOfBirth;

    private Integer estimatedAge;

    private String phone;

    /** 0-100, name edit-distance similarity. */
    private int nameSimilarity;

    /** {@link PatientDuplicateMatcher.Reason} names, so the client can render them without guessing. */
    private List<String> reasons;

    public static PossibleDuplicateDTO from(PatientDuplicateMatcher.Match match) {
        PossibleDuplicateDTO dto = new PossibleDuplicateDTO();
        dto.patientId = match.patient().id();
        dto.hospitalId = match.patient().hospitalId();
        dto.fullName = match.patient().fullName();
        dto.dateOfBirth = match.patient().dateOfBirth();
        dto.estimatedAge = match.patient().estimatedAge();
        dto.phone = match.patient().phone();
        dto.nameSimilarity = match.nameSimilarity();
        dto.reasons = match.reasons().stream().map(Enum::name).sorted().toList();
        return dto;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getNameSimilarity() {
        return nameSimilarity;
    }

    public void setNameSimilarity(int nameSimilarity) {
        this.nameSimilarity = nameSimilarity;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
