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
 * A Result.
 */
@Entity
@Table(name = "result")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Result implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;
    @Column(name = "result_value", nullable = false)
    private String resultValue;
    @Column(name = "notes")
    private String notes;

    @NotNull
    @Column(name = "entered_at", nullable = false)
    private Instant enteredAt;

    @Size(max = 2000)
    @Column(name = "image_reference", length = 2000)
    private String imageReference;

    @ManyToOne(optional = false)
    @NotNull
    private User enteredBy;

    @JsonIgnoreProperties(value = { "result", "visit", "orderedBy", "labTest", "radiologyExam" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "result")
    private DiagnosticOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Result id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResultValue() {
        return this.resultValue;
    }

    public Result resultValue(String resultValue) {
        this.setResultValue(resultValue);
        return this;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getNotes() {
        return this.notes;
    }

    public Result notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getEnteredAt() {
        return this.enteredAt;
    }

    public Result enteredAt(Instant enteredAt) {
        this.setEnteredAt(enteredAt);
        return this;
    }

    public void setEnteredAt(Instant enteredAt) {
        this.enteredAt = enteredAt;
    }

    public String getImageReference() {
        return this.imageReference;
    }

    public Result imageReference(String imageReference) {
        this.setImageReference(imageReference);
        return this;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }

    public User getEnteredBy() {
        return this.enteredBy;
    }

    public void setEnteredBy(User user) {
        this.enteredBy = user;
    }

    public Result enteredBy(User user) {
        this.setEnteredBy(user);
        return this;
    }

    public DiagnosticOrder getOrder() {
        return this.order;
    }

    public void setOrder(DiagnosticOrder diagnosticOrder) {
        if (this.order != null) {
            this.order.setResult(null);
        }
        if (diagnosticOrder != null) {
            diagnosticOrder.setResult(this);
        }
        this.order = diagnosticOrder;
    }

    public Result order(DiagnosticOrder diagnosticOrder) {
        this.setOrder(diagnosticOrder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Result)) {
            return false;
        }
        return getId() != null && getId().equals(((Result) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Result{" +
            "id=" + getId() +
            ", resultValue='" + getResultValue() + "'" +
            ", notes='" + getNotes() + "'" +
            ", enteredAt='" + getEnteredAt() + "'" +
            ", imageReference='" + getImageReference() + "'" +
            "}";
    }
}
