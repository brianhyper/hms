package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Diagnosis.
 */
@Entity
@Table(name = "diagnosis")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Diagnosis implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 64)
    @Column(name = "code", length = 64, nullable = false, unique = true)
    private String code;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "diagnoseses")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "doctor", "diagnoseses", "visit" }, allowSetters = true)
    private Set<Consultation> consultationses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Diagnosis id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public Diagnosis code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public Diagnosis name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Diagnosis active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Consultation> getConsultationses() {
        return this.consultationses;
    }

    public void setConsultationses(Set<Consultation> consultations) {
        if (this.consultationses != null) {
            this.consultationses.forEach(i -> i.removeDiagnoses(this));
        }
        if (consultations != null) {
            consultations.forEach(i -> i.addDiagnoses(this));
        }
        this.consultationses = consultations;
    }

    public Diagnosis consultationses(Set<Consultation> consultations) {
        this.setConsultationses(consultations);
        return this;
    }

    public Diagnosis addConsultations(Consultation consultation) {
        this.consultationses.add(consultation);
        consultation.getDiagnoseses().add(this);
        return this;
    }

    public Diagnosis removeConsultations(Consultation consultation) {
        this.consultationses.remove(consultation);
        consultation.getDiagnoseses().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Diagnosis)) {
            return false;
        }
        return getId() != null && getId().equals(((Diagnosis) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Diagnosis{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
