package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * The clinical content of a consultation.
 *
 * <p>Used both to save notes mid-consultation and to finish it. Every field is optional because a
 * doctor may record findings before having anything else to write, and requiring a full record up
 * front would push them to fill it in with noise.
 */
public class UpdateConsultationRequestDTO implements Serializable {

    @Size(max = 10000)
    private String presentingComplaint;

    @Size(max = 10000)
    private String examinationFindings;

    @Size(max = 10000)
    private String observations;

    @Size(max = 10000)
    private String diagnosisOther;

    @Size(max = 10000)
    private String followUpInstructions;

    /** Ids of catalogue diagnoses to attach. Replaces the existing set when supplied. */
    private List<Long> diagnosisIds;

    public String getPresentingComplaint() {
        return presentingComplaint;
    }

    public void setPresentingComplaint(String presentingComplaint) {
        this.presentingComplaint = presentingComplaint;
    }

    public String getExaminationFindings() {
        return examinationFindings;
    }

    public void setExaminationFindings(String examinationFindings) {
        this.examinationFindings = examinationFindings;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getDiagnosisOther() {
        return diagnosisOther;
    }

    public void setDiagnosisOther(String diagnosisOther) {
        this.diagnosisOther = diagnosisOther;
    }

    public String getFollowUpInstructions() {
        return followUpInstructions;
    }

    public void setFollowUpInstructions(String followUpInstructions) {
        this.followUpInstructions = followUpInstructions;
    }

    public List<Long> getDiagnosisIds() {
        return diagnosisIds;
    }

    public void setDiagnosisIds(List<Long> diagnosisIds) {
        this.diagnosisIds = diagnosisIds;
    }
}
