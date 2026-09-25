package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Bill.
 */
@Entity
@Table(name = "bill")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Bill implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "total_amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BillStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    @JsonIgnoreProperties(value = { "recordedBy", "bill" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Payment payment;

    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "bill")
    private Visit visit;

    /**
     * Optimistic locking guard. The bill total is rewritten every time an incremental
     * line item is appended, and payment confirmation may land concurrently.
     */
    @Version
    @Column(name = "version")
    private int version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Bill id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public Bill totalAmount(BigDecimal totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BillStatus getStatus() {
        return this.status;
    }

    public Bill status(BillStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return this.paidAt;
    }

    public Bill paidAt(Instant paidAt) {
        this.setPaidAt(paidAt);
        return this;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Payment getPayment() {
        return this.payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Bill payment(Payment payment) {
        this.setPayment(payment);
        return this;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        if (this.visit != null) {
            this.visit.setBill(null);
        }
        if (visit != null) {
            visit.setBill(this);
        }
        this.visit = visit;
    }

    public Bill visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public Bill version(int version) {
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
        if (!(o instanceof Bill)) {
            return false;
        }
        return getId() != null && getId().equals(((Bill) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Bill{" +
            "id=" + getId() +
            ", totalAmount=" + getTotalAmount() +
            ", status='" + getStatus() + "'" +
            ", paidAt='" + getPaidAt() + "'" +
            "}";
    }
}
