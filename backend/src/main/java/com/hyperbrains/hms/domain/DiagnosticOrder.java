package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A DiagnosticOrder.
 */
@Entity
@Table(name = "diagnostic_order")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiagnosticOrder implements Serializable {

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
    private OrderType type;

    @NotNull
    @Size(max = 200)
    @Column(name = "test_name", length = 200, nullable = false)
    private String testName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    @Column(name = "notes")
    private String notes;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @JsonIgnoreProperties(value = { "enteredBy", "order" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Result result;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    private Visit visit;

    @ManyToOne(optional = false)
    @NotNull
    private User orderedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private LabTest labTest;

    @ManyToOne(fetch = FetchType.LAZY)
    private RadiologyExam radiologyExam;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DiagnosticOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderType getType() {
        return this.type;
    }

    public DiagnosticOrder type(OrderType type) {
        this.setType(type);
        return this;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public String getTestName() {
        return this.testName;
    }

    public DiagnosticOrder testName(String testName) {
        this.setTestName(testName);
        return this;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public DiagnosticOrder status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return this.notes;
    }

    public DiagnosticOrder notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getOrderedAt() {
        return this.orderedAt;
    }

    public DiagnosticOrder orderedAt(Instant orderedAt) {
        this.setOrderedAt(orderedAt);
        return this;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public DiagnosticOrder result(Result result) {
        this.setResult(result);
        return this;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public DiagnosticOrder visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public User getOrderedBy() {
        return this.orderedBy;
    }

    public void setOrderedBy(User user) {
        this.orderedBy = user;
    }

    public DiagnosticOrder orderedBy(User user) {
        this.setOrderedBy(user);
        return this;
    }

    public LabTest getLabTest() {
        return this.labTest;
    }

    public void setLabTest(LabTest labTest) {
        this.labTest = labTest;
    }

    public DiagnosticOrder labTest(LabTest labTest) {
        this.setLabTest(labTest);
        return this;
    }

    public RadiologyExam getRadiologyExam() {
        return this.radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    public DiagnosticOrder radiologyExam(RadiologyExam radiologyExam) {
        this.setRadiologyExam(radiologyExam);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiagnosticOrder)) {
            return false;
        }
        return getId() != null && getId().equals(((DiagnosticOrder) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiagnosticOrder{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", testName='" + getTestName() + "'" +
            ", status='" + getStatus() + "'" +
            ", notes='" + getNotes() + "'" +
            ", orderedAt='" + getOrderedAt() + "'" +
            "}";
    }
}
