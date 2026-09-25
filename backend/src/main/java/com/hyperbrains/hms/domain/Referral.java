package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.ReferralType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Referral.
 */
@Entity
@Table(name = "referral")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Referral implements Serializable {

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
    private ReferralType type;

    @NotNull
    @Size(max = 255)
    @Column(name = "destination", length = 255, nullable = false)
    private String destination;

    @Size(max = 254)
    @Column(name = "destination_email", length = 254)
    private String destinationEmail;
    @Column(name = "reason", nullable = false)
    private String reason;
    @Column(name = "notes")
    private String notes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReferralStatus status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    private Visit visit;

    @ManyToOne(optional = false)
    @NotNull
    private User referredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Referral id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReferralType getType() {
        return this.type;
    }

    public Referral type(ReferralType type) {
        this.setType(type);
        return this;
    }

    public void setType(ReferralType type) {
        this.type = type;
    }

    public String getDestination() {
        return this.destination;
    }

    public Referral destination(String destination) {
        this.setDestination(destination);
        return this;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationEmail() {
        return this.destinationEmail;
    }

    public Referral destinationEmail(String destinationEmail) {
        this.setDestinationEmail(destinationEmail);
        return this;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }

    public String getReason() {
        return this.reason;
    }

    public Referral reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return this.notes;
    }

    public Referral notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReferralStatus getStatus() {
        return this.status;
    }

    public Referral status(ReferralStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Referral createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Referral visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public User getReferredBy() {
        return this.referredBy;
    }

    public void setReferredBy(User user) {
        this.referredBy = user;
    }

    public Referral referredBy(User user) {
        this.setReferredBy(user);
        return this;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Referral department(Department department) {
        this.setDepartment(department);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Referral)) {
            return false;
        }
        return getId() != null && getId().equals(((Referral) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Referral{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", destination='" + getDestination() + "'" +
            ", destinationEmail='" + getDestinationEmail() + "'" +
            ", reason='" + getReason() + "'" +
            ", notes='" + getNotes() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
