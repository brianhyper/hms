package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A BillLineItem.
 */
@Entity
@Table(name = "bill_line_item")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BillLineItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private BillLineSourceType sourceType;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "payment", "visit" }, allowSetters = true)
    private Bill bill;

    /**
     * What produced this line item, in the form {@code "<SOURCE_TYPE>:<entityId>"} —
     * e.g. {@code "CONSULTATION:42"}, {@code "LAB:17"}, {@code "PHARMACY:9"}.
     *
     * <p>This is what makes incremental charging safe. Generation becomes an upsert
     * keyed on this value, so a retried "complete consultation" cannot double-charge,
     * and one charge can be voided without disturbing its siblings. Null for lines
     * entered by hand.
     */
    @Size(max = 64)
    @Column(name = "source_ref", length = 64)
    private String sourceRef;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BillLineItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public BillLineItem description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BillLineItem amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillLineSourceType getSourceType() {
        return this.sourceType;
    }

    public BillLineItem sourceType(BillLineSourceType sourceType) {
        this.setSourceType(sourceType);
        return this;
    }

    public void setSourceType(BillLineSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Bill getBill() {
        return this.bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillLineItem bill(Bill bill) {
        this.setBill(bill);
        return this;
    }

    public String getSourceRef() {
        return this.sourceRef;
    }

    public BillLineItem sourceRef(String sourceRef) {
        this.setSourceRef(sourceRef);
        return this;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BillLineItem)) {
            return false;
        }
        return getId() != null && getId().equals(((BillLineItem) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BillLineItem{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", amount=" + getAmount() +
            ", sourceType='" + getSourceType() + "'" +
            "}";
    }
}
