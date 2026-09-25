package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One row of a work queue.
 *
 * <p>A projection rather than the {@code Visit} entity: the queue screens need the patient's
 * identifying details alongside the visit, and the entity's relations are mapped as identifiers
 * only, which would force the client into a second request per row.
 */
public class VisitQueueItemDTO implements Serializable {

    private Long visitId;

    private String patientHospitalId;

    private String patientName;

    private LocalDate patientDateOfBirth;

    private Integer patientEstimatedAge;

    private Sex patientSex;

    private VisitType type;

    private VisitPriority priority;

    private VisitStatus status;

    private String reasonForVisit;

    private Instant createdAt;

    private Instant startedVitalsAt;

    private Instant startedConsultationAt;

    /**
     * True when selecting this row means skipping past one that ranks ahead of it.
     *
     * <p>Sent per row so the client can demand a reason at the moment of selection rather than
     * discovering the requirement from a rejected request.
     */
    private boolean requiresSkipReason;

    public static VisitQueueItemDTO from(Visit visit, int position) {
        Patient patient = visit.getPatient();
        VisitQueueItemDTO dto = new VisitQueueItemDTO();
        dto.visitId = visit.getId();
        if (patient != null) {
            dto.patientHospitalId = patient.getHospitalId();
            dto.patientName = patient.getFullName();
            dto.patientDateOfBirth = patient.getDateOfBirth();
            dto.patientEstimatedAge = patient.getEstimatedAge();
            dto.patientSex = patient.getSex();
        }
        dto.type = visit.getType();
        dto.priority = visit.getPriority();
        dto.status = visit.getStatus();
        dto.reasonForVisit = visit.getReasonForVisit();
        dto.createdAt = visit.getCreatedAt();
        dto.startedVitalsAt = visit.getStartedVitalsAt();
        dto.startedConsultationAt = visit.getStartedConsultationAt();
        dto.requiresSkipReason = position > 0;
        return dto;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public String getPatientHospitalId() {
        return patientHospitalId;
    }

    public void setPatientHospitalId(String patientHospitalId) {
        this.patientHospitalId = patientHospitalId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getPatientDateOfBirth() {
        return patientDateOfBirth;
    }

    public void setPatientDateOfBirth(LocalDate patientDateOfBirth) {
        this.patientDateOfBirth = patientDateOfBirth;
    }

    public Integer getPatientEstimatedAge() {
        return patientEstimatedAge;
    }

    public void setPatientEstimatedAge(Integer patientEstimatedAge) {
        this.patientEstimatedAge = patientEstimatedAge;
    }

    public Sex getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(Sex patientSex) {
        this.patientSex = patientSex;
    }

    public VisitType getType() {
        return type;
    }

    public void setType(VisitType type) {
        this.type = type;
    }

    public VisitPriority getPriority() {
        return priority;
    }

    public void setPriority(VisitPriority priority) {
        this.priority = priority;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedVitalsAt() {
        return startedVitalsAt;
    }

    public void setStartedVitalsAt(Instant startedVitalsAt) {
        this.startedVitalsAt = startedVitalsAt;
    }

    public Instant getStartedConsultationAt() {
        return startedConsultationAt;
    }

    public void setStartedConsultationAt(Instant startedConsultationAt) {
        this.startedConsultationAt = startedConsultationAt;
    }

    public boolean isRequiresSkipReason() {
        return requiresSkipReason;
    }

    public void setRequiresSkipReason(boolean requiresSkipReason) {
        this.requiresSkipReason = requiresSkipReason;
    }
}
