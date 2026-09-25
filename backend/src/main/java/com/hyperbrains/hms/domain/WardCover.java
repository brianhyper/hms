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
 * Which doctor is responsible for which ward, for a period. Without this the
 * \"a doctor sees admissions on a ward they are covering\" rule has no data to read.
 */
@Entity
@Table(name = "ward_cover")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WardCover implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "covers_from", nullable = false)
    private Instant coversFrom;

    @Column(name = "covers_to")
    private Instant coversTo;

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(optional = false)
    @NotNull
    private User doctor;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "department" }, allowSetters = true)
    private Ward ward;

    @ManyToOne(optional = false)
    @NotNull
    private User assignedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public WardCover id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCoversFrom() {
        return this.coversFrom;
    }

    public WardCover coversFrom(Instant coversFrom) {
        this.setCoversFrom(coversFrom);
        return this;
    }

    public void setCoversFrom(Instant coversFrom) {
        this.coversFrom = coversFrom;
    }

    public Instant getCoversTo() {
        return this.coversTo;
    }

    public WardCover coversTo(Instant coversTo) {
        this.setCoversTo(coversTo);
        return this;
    }

    public void setCoversTo(Instant coversTo) {
        this.coversTo = coversTo;
    }

    public String getNote() {
        return this.note;
    }

    public WardCover note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getDoctor() {
        return this.doctor;
    }

    public void setDoctor(User user) {
        this.doctor = user;
    }

    public WardCover doctor(User user) {
        this.setDoctor(user);
        return this;
    }

    public Ward getWard() {
        return this.ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public WardCover ward(Ward ward) {
        this.setWard(ward);
        return this;
    }

    public User getAssignedBy() {
        return this.assignedBy;
    }

    public void setAssignedBy(User user) {
        this.assignedBy = user;
    }

    public WardCover assignedBy(User user) {
        this.setAssignedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WardCover)) {
            return false;
        }
        return getId() != null && getId().equals(((WardCover) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WardCover{" +
            "id=" + getId() +
            ", coversFrom='" + getCoversFrom() + "'" +
            ", coversTo='" + getCoversTo() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
