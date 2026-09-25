package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.PaymentPlanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A PaymentPlan.
 */
@Entity
@Table(name = "payment_plan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PaymentPlan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "total_owed", precision = 21, scale = 2, nullable = false)
    private BigDecimal totalOwed;

    @NotNull
    @Column(name = "agreed_at", nullable = false)
    private Instant agreedAt;

    @NotNull
    @Size(max = 200)
    @Column(name = "guarantor_name", length = 200, nullable = false)
    private String guarantorName;

    @Size(max = 100)
    @Column(name = "guarantor_relationship", length = 100)
    private String guarantorRelationship;

    @NotNull
    @Size(max = 32)
    @Column(name = "guarantor_phone", length = 32, nullable = false)
    private String guarantorPhone;

    @Size(max = 10000)
    @Column(name = "notes", length = 10000)
    private String notes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentPlanStatus status;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "payment", "visit" }, allowSetters = true)
    private Bill bill;

    @ManyToOne(optional = false)
    @NotNull
    private User agreedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PaymentPlan id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalOwed() {
        return this.totalOwed;
    }

    public PaymentPlan totalOwed(BigDecimal totalOwed) {
        this.setTotalOwed(totalOwed);
        return this;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
    }

    public Instant getAgreedAt() {
        return this.agreedAt;
    }

    public PaymentPlan agreedAt(Instant agreedAt) {
        this.setAgreedAt(agreedAt);
        return this;
    }

    public void setAgreedAt(Instant agreedAt) {
        this.agreedAt = agreedAt;
    }

    public String getGuarantorName() {
        return this.guarantorName;
    }

    public PaymentPlan guarantorName(String guarantorName) {
        this.setGuarantorName(guarantorName);
        return this;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGuarantorRelationship() {
        return this.guarantorRelationship;
    }

    public PaymentPlan guarantorRelationship(String guarantorRelationship) {
        this.setGuarantorRelationship(guarantorRelationship);
        return this;
    }

    public void setGuarantorRelationship(String guarantorRelationship) {
        this.guarantorRelationship = guarantorRelationship;
    }

    public String getGuarantorPhone() {
        return this.guarantorPhone;
    }

    public PaymentPlan guarantorPhone(String guarantorPhone) {
        this.setGuarantorPhone(guarantorPhone);
        return this;
    }

    public void setGuarantorPhone(String guarantorPhone) {
        this.guarantorPhone = guarantorPhone;
    }

    public String getNotes() {
        return this.notes;
    }

    public PaymentPlan notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PaymentPlanStatus getStatus() {
        return this.status;
    }

    public PaymentPlan status(PaymentPlanStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(PaymentPlanStatus status) {
        this.status = status;
    }

    public Bill getBill() {
        return this.bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public PaymentPlan bill(Bill bill) {
        this.setBill(bill);
        return this;
    }

    public User getAgreedBy() {
        return this.agreedBy;
    }

    public void setAgreedBy(User user) {
        this.agreedBy = user;
    }

    public PaymentPlan agreedBy(User user) {
        this.setAgreedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentPlan)) {
            return false;
        }
        return getId() != null && getId().equals(((PaymentPlan) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PaymentPlan{" +
            "id=" + getId() +
            ", totalOwed=" + getTotalOwed() +
            ", agreedAt='" + getAgreedAt() + "'" +
            ", guarantorName='" + getGuarantorName() + "'" +
            ", guarantorRelationship='" + getGuarantorRelationship() + "'" +
            ", guarantorPhone='" + getGuarantorPhone() + "'" +
            ", notes='" + getNotes() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
