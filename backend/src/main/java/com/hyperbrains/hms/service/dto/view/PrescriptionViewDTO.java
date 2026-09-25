package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * A prescription as the treating clinician and the pharmacy see it.
 *
 * <p>Patient details are {@link PatientSummaryDTO} — identifying information only. A pharmacy needs
 * to know whose medicine this is; it does not need the patient's allergies, conditions or identity
 * document. This is the same boundary the lab worklist draws.
 */
public class PrescriptionViewDTO implements Serializable {

    private Long prescriptionId;

    private Long visitId;

    private PrescriptionSource source;

    private String prescribingSource;

    private PrescriptionStatus status;

    /** When it was written, so the queue can show how long a patient has been waiting. */
    private Instant createdAt;

    private String doctorLogin;

    private PatientSummaryDTO patient;

    private List<PrescriptionLineViewDTO> lines;

    public static PrescriptionViewDTO from(Prescription prescription, List<PrescriptionLine> lines) {
        PrescriptionViewDTO dto = new PrescriptionViewDTO();
        dto.prescriptionId = prescription.getId();
        dto.source = prescription.getSource();
        dto.prescribingSource = prescription.getPrescribingSource();
        dto.status = prescription.getStatus();
        dto.createdAt = prescription.getCreatedAt();
        dto.doctorLogin = prescription.getDoctor() == null ? null : prescription.getDoctor().getLogin();
        if (prescription.getVisit() != null) {
            dto.visitId = prescription.getVisit().getId();
            dto.patient = PatientSummaryDTO.from(prescription.getVisit().getPatient());
        }
        dto.lines = lines == null ? List.of() : lines.stream().map(PrescriptionLineViewDTO::from).toList();
        return dto;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public PrescriptionSource getSource() {
        return source;
    }

    public void setSource(PrescriptionSource source) {
        this.source = source;
    }

    public String getPrescribingSource() {
        return prescribingSource;
    }

    public void setPrescribingSource(String prescribingSource) {
        this.prescribingSource = prescribingSource;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getDoctorLogin() {
        return doctorLogin;
    }

    public void setDoctorLogin(String doctorLogin) {
        this.doctorLogin = doctorLogin;
    }

    public PatientSummaryDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientSummaryDTO patient) {
        this.patient = patient;
    }

    public List<PrescriptionLineViewDTO> getLines() {
        return lines;
    }

    public void setLines(List<PrescriptionLineViewDTO> lines) {
        this.lines = lines;
    }
}
