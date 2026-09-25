package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Payment.
 */
@Entity
@Table(name = "payment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Payment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Size(max = 100)
    @Column(name = "mpesa_reference", length = 100)
    private String mpesaReference;

    @Size(max = 200)
    @Column(name = "insurer_name", length = 200)
    private String insurerName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false)
    private PaymentConfirmationStatus confirmationStatus;

    @NotNull
    @Size(max = 64)
    @Column(name = "receipt_number", length = 64, nullable = false, unique = true)
    private String receiptNumber;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @ManyToOne(optional = false)
    @NotNull
    private User recordedBy;

    @JsonIgnoreProperties(value = { "payment", "visit" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "payment")
    private Bill bill;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Payment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentMethod getMethod() {
        return this.method;
    }

    public Payment method(PaymentMethod method) {
        this.setMethod(method);
        return this;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getMpesaReference() {
        return this.mpesaReference;
    }

    public Payment mpesaReference(String mpesaReference) {
        this.setMpesaReference(mpesaReference);
        return this;
    }

    public void setMpesaReference(String mpesaReference) {
        this.mpesaReference = mpesaReference;
    }

    public String getInsurerName() {
        return this.insurerName;
    }

    public Payment insurerName(String insurerName) {
        this.setInsurerName(insurerName);
        return this;
    }

    public void setInsurerName(String insurerName) {
        this.insurerName = insurerName;
    }

    public PaymentConfirmationStatus getConfirmationStatus() {
        return this.confirmationStatus;
    }

    public Payment confirmationStatus(PaymentConfirmationStatus confirmationStatus) {
        this.setConfirmationStatus(confirmationStatus);
        return this;
    }

    public void setConfirmationStatus(PaymentConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public String getReceiptNumber() {
        return this.receiptNumber;
    }

    public Payment receiptNumber(String receiptNumber) {
        this.setReceiptNumber(receiptNumber);
        return this;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Payment amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getRecordedAt() {
        return this.recordedAt;
    }

    public Payment recordedAt(Instant recordedAt) {
        this.setRecordedAt(recordedAt);
        return this;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public User getRecordedBy() {
        return this.recordedBy;
    }

    public void setRecordedBy(User user) {
        this.recordedBy = user;
    }

    public Payment recordedBy(User user) {
        this.setRecordedBy(user);
        return this;
    }

    public Bill getBill() {
        return this.bill;
    }

    public void setBill(Bill bill) {
        if (this.bill != null) {
            this.bill.setPayment(null);
        }
        if (bill != null) {
            bill.setPayment(this);
        }
        this.bill = bill;
    }

    public Payment bill(Bill bill) {
        this.setBill(bill);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Payment)) {
            return false;
        }
        return getId() != null && getId().equals(((Payment) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Payment{" +
            "id=" + getId() +
            ", method='" + getMethod() + "'" +
            ", mpesaReference='" + getMpesaReference() + "'" +
            ", insurerName='" + getInsurerName() + "'" +
            ", confirmationStatus='" + getConfirmationStatus() + "'" +
            ", receiptNumber='" + getReceiptNumber() + "'" +
            ", amount=" + getAmount() +
            ", recordedAt='" + getRecordedAt() + "'" +
            "}";
    }
}
