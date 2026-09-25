package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A DoctorOrder.
 */
@Entity
@Table(name = "doctor_order")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DoctorOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DoctorOrderType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = false)
    private DoctorOrderRecurrence recurrence;

    /**
     * Free text such as \"q4h\" — deliberately not a scheduling DSL.
     */
    @Size(max = 200)
    @Column(name = "frequency", length = 200)
    private String frequency;

    @Column(name = "end_date")
    private Instant endDate;

    /**
     * What is to be done: the instruction itself, or the drug and dose.
     */
    @NotNull
    @Size(max = 10000)
    @Column(name = "details", length = 10000, nullable = false)
    private String details;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DoctorOrderStatus status;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Size(max = 10000)
    @Column(name = "cancel_reason", length = 10000)
    private String cancelReason;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "visit", "bed", "admittingDoctor", "primaryDoctor", "dischargedByDoctor", "dischargedByNurse" },
        allowSetters = true
    )
    private Admission admission;

    @ManyToOne(optional = false)
    @NotNull
    private User orderedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private User cancelledBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DoctorOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getOrderedAt() {
        return this.orderedAt;
    }

    public DoctorOrder orderedAt(Instant orderedAt) {
        this.setOrderedAt(orderedAt);
        return this;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public DoctorOrderType getType() {
        return this.type;
    }

    public DoctorOrder type(DoctorOrderType type) {
        this.setType(type);
        return this;
    }

    public void setType(DoctorOrderType type) {
        this.type = type;
    }

    public DoctorOrderRecurrence getRecurrence() {
        return this.recurrence;
    }

    public DoctorOrder recurrence(DoctorOrderRecurrence recurrence) {
        this.setRecurrence(recurrence);
        return this;
    }

    public void setRecurrence(DoctorOrderRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getFrequency() {
        return this.frequency;
    }

    public DoctorOrder frequency(String frequency) {
        this.setFrequency(frequency);
        return this;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public DoctorOrder endDate(Instant endDate) {
        this.setEndDate(endDate);
        return this;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getDetails() {
        return this.details;
    }

    public DoctorOrder details(String details) {
        this.setDetails(details);
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public DoctorOrderStatus getStatus() {
        return this.status;
    }

    public DoctorOrder status(DoctorOrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(DoctorOrderStatus status) {
        this.status = status;
    }

    public Instant getCancelledAt() {
        return this.cancelledAt;
    }

    public DoctorOrder cancelledAt(Instant cancelledAt) {
        this.setCancelledAt(cancelledAt);
        return this;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return this.cancelReason;
    }

    public DoctorOrder cancelReason(String cancelReason) {
        this.setCancelReason(cancelReason);
        return this;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Admission getAdmission() {
        return this.admission;
    }

    public void setAdmission(Admission admission) {
        this.admission = admission;
    }

    public DoctorOrder admission(Admission admission) {
        this.setAdmission(admission);
        return this;
    }

    public User getOrderedBy() {
        return this.orderedBy;
    }

    public void setOrderedBy(User user) {
        this.orderedBy = user;
    }

    public DoctorOrder orderedBy(User user) {
        this.setOrderedBy(user);
        return this;
    }

    public User getCancelledBy() {
        return this.cancelledBy;
    }

    public void setCancelledBy(User user) {
        this.cancelledBy = user;
    }

    public DoctorOrder cancelledBy(User user) {
        this.setCancelledBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoctorOrder)) {
            return false;
        }
        return getId() != null && getId().equals(((DoctorOrder) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DoctorOrder{" +
            "id=" + getId() +
            ", orderedAt='" + getOrderedAt() + "'" +
            ", type='" + getType() + "'" +
            ", recurrence='" + getRecurrence() + "'" +
            ", frequency='" + getFrequency() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", details='" + getDetails() + "'" +
            ", status='" + getStatus() + "'" +
            ", cancelledAt='" + getCancelledAt() + "'" +
            ", cancelReason='" + getCancelReason() + "'" +
            "}";
    }
}
