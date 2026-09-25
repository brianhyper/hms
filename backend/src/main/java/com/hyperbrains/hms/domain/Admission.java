package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Admission.
 */
@Entity
@Table(name = "admission")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Admission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "admitted_at", nullable = false)
    private Instant admittedAt;

    @NotNull
    @Size(max = 10000)
    @Column(name = "admission_reason", length = 10000, nullable = false)
    private String admissionReason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdmissionStatus status;

    @Column(name = "discharged_at")
    private Instant dischargedAt;

    @Size(max = 10000)
    @Column(name = "discharge_note", length = 10000)
    private String dischargeNote;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "ward", "bedType" }, allowSetters = true)
    private Bed bed;

    @ManyToOne(optional = false)
    @NotNull
    private User admittingDoctor;

    @ManyToOne(optional = false)
    @NotNull
    private User primaryDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private User dischargedByDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    private User dischargedByNurse;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Admission id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAdmittedAt() {
        return this.admittedAt;
    }

    public Admission admittedAt(Instant admittedAt) {
        this.setAdmittedAt(admittedAt);
        return this;
    }

    public void setAdmittedAt(Instant admittedAt) {
        this.admittedAt = admittedAt;
    }

    public String getAdmissionReason() {
        return this.admissionReason;
    }

    public Admission admissionReason(String admissionReason) {
        this.setAdmissionReason(admissionReason);
        return this;
    }

    public void setAdmissionReason(String admissionReason) {
        this.admissionReason = admissionReason;
    }

    public AdmissionStatus getStatus() {
        return this.status;
    }

    public Admission status(AdmissionStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AdmissionStatus status) {
        this.status = status;
    }

    public Instant getDischargedAt() {
        return this.dischargedAt;
    }

    public Admission dischargedAt(Instant dischargedAt) {
        this.setDischargedAt(dischargedAt);
        return this;
    }

    public void setDischargedAt(Instant dischargedAt) {
        this.dischargedAt = dischargedAt;
    }

    public String getDischargeNote() {
        return this.dischargeNote;
    }

    public Admission dischargeNote(String dischargeNote) {
        this.setDischargeNote(dischargeNote);
        return this;
    }

    public void setDischargeNote(String dischargeNote) {
        this.dischargeNote = dischargeNote;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Admission visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public Bed getBed() {
        return this.bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    public Admission bed(Bed bed) {
        this.setBed(bed);
        return this;
    }

    public User getAdmittingDoctor() {
        return this.admittingDoctor;
    }

    public void setAdmittingDoctor(User user) {
        this.admittingDoctor = user;
    }

    public Admission admittingDoctor(User user) {
        this.setAdmittingDoctor(user);
        return this;
    }

    public User getPrimaryDoctor() {
        return this.primaryDoctor;
    }

    public void setPrimaryDoctor(User user) {
        this.primaryDoctor = user;
    }

    public Admission primaryDoctor(User user) {
        this.setPrimaryDoctor(user);
        return this;
    }

    public User getDischargedByDoctor() {
        return this.dischargedByDoctor;
    }

    public void setDischargedByDoctor(User user) {
        this.dischargedByDoctor = user;
    }

    public Admission dischargedByDoctor(User user) {
        this.setDischargedByDoctor(user);
        return this;
    }

    public User getDischargedByNurse() {
        return this.dischargedByNurse;
    }

    public void setDischargedByNurse(User user) {
        this.dischargedByNurse = user;
    }

    public Admission dischargedByNurse(User user) {
        this.setDischargedByNurse(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Admission)) {
            return false;
        }
        return getId() != null && getId().equals(((Admission) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Admission{" +
            "id=" + getId() +
            ", admittedAt='" + getAdmittedAt() + "'" +
            ", admissionReason='" + getAdmissionReason() + "'" +
            ", status='" + getStatus() + "'" +
            ", dischargedAt='" + getDischargedAt() + "'" +
            ", dischargeNote='" + getDischargeNote() + "'" +
            "}";
    }
}
