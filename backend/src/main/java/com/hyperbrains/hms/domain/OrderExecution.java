package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * One dose or one action actually carried out. The nurse's record of what
 * happened, never a schedule of what should.
 */
@Entity
@Table(name = "order_execution")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrderExecution implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Size(max = 10000)
    @Column(name = "notes", length = 10000)
    private String notes;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "admission", "orderedBy", "cancelledBy" }, allowSetters = true)
    private DoctorOrder doctorOrder;

    @ManyToOne(optional = false)
    @NotNull
    private User executedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public OrderExecution id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getExecutedAt() {
        return this.executedAt;
    }

    public OrderExecution executedAt(Instant executedAt) {
        this.setExecutedAt(executedAt);
        return this;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public String getNotes() {
        return this.notes;
    }

    public OrderExecution notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DoctorOrder getDoctorOrder() {
        return this.doctorOrder;
    }

    public void setDoctorOrder(DoctorOrder doctorOrder) {
        this.doctorOrder = doctorOrder;
    }

    public OrderExecution doctorOrder(DoctorOrder doctorOrder) {
        this.setDoctorOrder(doctorOrder);
        return this;
    }

    public User getExecutedBy() {
        return this.executedBy;
    }

    public void setExecutedBy(User user) {
        this.executedBy = user;
    }

    public OrderExecution executedBy(User user) {
        this.setExecutedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderExecution)) {
            return false;
        }
        return getId() != null && getId().equals(((OrderExecution) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OrderExecution{" +
            "id=" + getId() +
            ", executedAt='" + getExecutedAt() + "'" +
            ", notes='" + getNotes() + "'" +
            "}";
    }
}
