package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Consultation.
 */
@Entity
@Table(name = "consultation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Consultation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;
    @Column(name = "presenting_complaint")
    private String presentingComplaint;
    @Column(name = "examination_findings")
    private String examinationFindings;
    @Column(name = "diagnosis_other")
    private String diagnosisOther;
    @Column(name = "observations")
    private String observations;
    @Column(name = "follow_up_instructions")
    private String followUpInstructions;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsultationStatus status;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(optional = false)
    @NotNull
    private User doctor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_consultation__diagnoses",
        joinColumns = @JoinColumn(name = "consultation_id"),
        inverseJoinColumns = @JoinColumn(name = "diagnoses_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "consultationses" }, allowSetters = true)
    private Set<Diagnosis> diagnoseses = new HashSet<>();

    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "consultation")
    private Visit visit;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Consultation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPresentingComplaint() {
        return this.presentingComplaint;
    }

    public Consultation presentingComplaint(String presentingComplaint) {
        this.setPresentingComplaint(presentingComplaint);
        return this;
    }

    public void setPresentingComplaint(String presentingComplaint) {
        this.presentingComplaint = presentingComplaint;
    }

    public String getExaminationFindings() {
        return this.examinationFindings;
    }

    public Consultation examinationFindings(String examinationFindings) {
        this.setExaminationFindings(examinationFindings);
        return this;
    }

    public void setExaminationFindings(String examinationFindings) {
        this.examinationFindings = examinationFindings;
    }

    public String getDiagnosisOther() {
        return this.diagnosisOther;
    }

    public Consultation diagnosisOther(String diagnosisOther) {
        this.setDiagnosisOther(diagnosisOther);
        return this;
    }

    public void setDiagnosisOther(String diagnosisOther) {
        this.diagnosisOther = diagnosisOther;
    }

    public String getObservations() {
        return this.observations;
    }

    public Consultation observations(String observations) {
        this.setObservations(observations);
        return this;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getFollowUpInstructions() {
        return this.followUpInstructions;
    }

    public Consultation followUpInstructions(String followUpInstructions) {
        this.setFollowUpInstructions(followUpInstructions);
        return this;
    }

    public void setFollowUpInstructions(String followUpInstructions) {
        this.followUpInstructions = followUpInstructions;
    }

    public ConsultationStatus getStatus() {
        return this.status;
    }

    public Consultation status(ConsultationStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public Consultation startedAt(Instant startedAt) {
        this.setStartedAt(startedAt);
        return this;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public Consultation completedAt(Instant completedAt) {
        this.setCompletedAt(completedAt);
        return this;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public User getDoctor() {
        return this.doctor;
    }

    public void setDoctor(User user) {
        this.doctor = user;
    }

    public Consultation doctor(User user) {
        this.setDoctor(user);
        return this;
    }

    public Set<Diagnosis> getDiagnoseses() {
        return this.diagnoseses;
    }

    public void setDiagnoseses(Set<Diagnosis> diagnoses) {
        this.diagnoseses = diagnoses;
    }

    public Consultation diagnoseses(Set<Diagnosis> diagnoses) {
        this.setDiagnoseses(diagnoses);
        return this;
    }

    public Consultation addDiagnoses(Diagnosis diagnosis) {
        this.diagnoseses.add(diagnosis);
        return this;
    }

    public Consultation removeDiagnoses(Diagnosis diagnosis) {
        this.diagnoseses.remove(diagnosis);
        return this;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        if (this.visit != null) {
            this.visit.setConsultation(null);
        }
        if (visit != null) {
            visit.setConsultation(this);
        }
        this.visit = visit;
    }

    public Consultation visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Consultation)) {
            return false;
        }
        return getId() != null && getId().equals(((Consultation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Consultation{" +
            "id=" + getId() +
            ", presentingComplaint='" + getPresentingComplaint() + "'" +
            ", examinationFindings='" + getExaminationFindings() + "'" +
            ", diagnosisOther='" + getDiagnosisOther() + "'" +
            ", observations='" + getObservations() + "'" +
            ", followUpInstructions='" + getFollowUpInstructions() + "'" +
            ", status='" + getStatus() + "'" +
            ", startedAt='" + getStartedAt() + "'" +
            ", completedAt='" + getCompletedAt() + "'" +
            "}";
    }
}
