package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Visit.
 */
@Entity
@Table(name = "visit")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Visit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VisitType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private VisitPriority priority;
    @Column(name = "reason_for_visit", nullable = false)
    private String reasonForVisit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VisitStatus status;
    @Column(name = "queue_skip_reason")
    private String queueSkipReason;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_vitals_at")
    private Instant startedVitalsAt;

    @Column(name = "started_consultation_at")
    private Instant startedConsultationAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @JsonIgnoreProperties(value = { "visit" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private VitalSigns vitals;

    @JsonIgnoreProperties(value = { "doctor", "diagnoseses", "visit" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Consultation consultation;

    @JsonIgnoreProperties(value = { "payment", "visit" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Bill bill;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "mergedIntoPatient" }, allowSetters = true)
    private Patient patient;

    @JsonIgnoreProperties(value = { "visit", "patient", "department", "doctor" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "visit")
    private Appointment appointment;

    /**
     * Optimistic locking guard. The derived visit status is recomputed from several
     * concurrent events (a lab result landing while a prescription is being dispensed),
     * so two overlapping recomputes must not silently overwrite each other.
     */
    @Version
    @Column(name = "version")
    private int version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Visit id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VisitType getType() {
        return this.type;
    }

    public Visit type(VisitType type) {
        this.setType(type);
        return this;
    }

    public void setType(VisitType type) {
        this.type = type;
    }

    public VisitPriority getPriority() {
        return this.priority;
    }

    public Visit priority(VisitPriority priority) {
        this.setPriority(priority);
        return this;
    }

    public void setPriority(VisitPriority priority) {
        this.priority = priority;
    }

    public String getReasonForVisit() {
        return this.reasonForVisit;
    }

    public Visit reasonForVisit(String reasonForVisit) {
        this.setReasonForVisit(reasonForVisit);
        return this;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public VisitStatus getStatus() {
        return this.status;
    }

    public Visit status(VisitStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getQueueSkipReason() {
        return this.queueSkipReason;
    }

    public Visit queueSkipReason(String queueSkipReason) {
        this.setQueueSkipReason(queueSkipReason);
        return this;
    }

    public void setQueueSkipReason(String queueSkipReason) {
        this.queueSkipReason = queueSkipReason;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Visit createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedVitalsAt() {
        return this.startedVitalsAt;
    }

    public Visit startedVitalsAt(Instant startedVitalsAt) {
        this.setStartedVitalsAt(startedVitalsAt);
        return this;
    }

    public void setStartedVitalsAt(Instant startedVitalsAt) {
        this.startedVitalsAt = startedVitalsAt;
    }

    public Instant getStartedConsultationAt() {
        return this.startedConsultationAt;
    }

    public Visit startedConsultationAt(Instant startedConsultationAt) {
        this.setStartedConsultationAt(startedConsultationAt);
        return this;
    }

    public void setStartedConsultationAt(Instant startedConsultationAt) {
        this.startedConsultationAt = startedConsultationAt;
    }

    public Instant getClosedAt() {
        return this.closedAt;
    }

    public Visit closedAt(Instant closedAt) {
        this.setClosedAt(closedAt);
        return this;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public VitalSigns getVitals() {
        return this.vitals;
    }

    public void setVitals(VitalSigns vitalSigns) {
        this.vitals = vitalSigns;
    }

    public Visit vitals(VitalSigns vitalSigns) {
        this.setVitals(vitalSigns);
        return this;
    }

    public Consultation getConsultation() {
        return this.consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public Visit consultation(Consultation consultation) {
        this.setConsultation(consultation);
        return this;
    }

    public Bill getBill() {
        return this.bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Visit bill(Bill bill) {
        this.setBill(bill);
        return this;
    }

    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Visit patient(Patient patient) {
        this.setPatient(patient);
        return this;
    }

    public Appointment getAppointment() {
        return this.appointment;
    }

    public void setAppointment(Appointment appointment) {
        if (this.appointment != null) {
            this.appointment.setVisit(null);
        }
        if (appointment != null) {
            appointment.setVisit(this);
        }
        this.appointment = appointment;
    }

    public Visit appointment(Appointment appointment) {
        this.setAppointment(appointment);
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public Visit version(int version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Visit)) {
            return false;
        }
        return getId() != null && getId().equals(((Visit) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Visit{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", priority='" + getPriority() + "'" +
            ", reasonForVisit='" + getReasonForVisit() + "'" +
            ", status='" + getStatus() + "'" +
            ", queueSkipReason='" + getQueueSkipReason() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", startedVitalsAt='" + getStartedVitalsAt() + "'" +
            ", startedConsultationAt='" + getStartedConsultationAt() + "'" +
            ", closedAt='" + getClosedAt() + "'" +
            "}";
    }
}
