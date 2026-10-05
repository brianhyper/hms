package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * One opening of one patient's chart.
 *
 * <p>Deliberately not cached. A security log is written once and read later, and a cached copy of a log is a copy
 * that can be wrong about the thing it exists to establish — the same reasoning that keeps the sign-in state out of
 * the user cache.
 *
 * <p>The actor is stored as a login rather than as a reference to the account, so that this record goes on meaning
 * what it meant when it was written even if the account is later deactivated or renamed.
 */
@Entity
@Table(name = "patient_access_log")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PatientAccessLog implements Serializable {

    /** A chart was opened. */
    public static final String VIEW = "VIEW";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @NotNull
    @Size(max = 50)
    @Column(name = "actor_login", length = 50, nullable = false)
    private String actorLogin;

    @NotNull
    @Size(max = 20)
    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties(value = { "visits" }, allowSetters = true)
    private Patient patient;

    public Long getId() {
        return this.id;
    }

    public PatientAccessLog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAccessedAt() {
        return this.accessedAt;
    }

    public PatientAccessLog accessedAt(Instant accessedAt) {
        this.setAccessedAt(accessedAt);
        return this;
    }

    public void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }

    public String getActorLogin() {
        return this.actorLogin;
    }

    public PatientAccessLog actorLogin(String actorLogin) {
        this.setActorLogin(actorLogin);
        return this;
    }

    public void setActorLogin(String actorLogin) {
        this.actorLogin = actorLogin;
    }

    public String getAction() {
        return this.action;
    }

    public PatientAccessLog action(String action) {
        this.setAction(action);
        return this;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public PatientAccessLog patient(Patient patient) {
        this.patient = patient;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PatientAccessLog)) {
            return false;
        }
        return getId() != null && getId().equals(((PatientAccessLog) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "PatientAccessLog{" +
            "id=" +
            getId() +
            ", accessedAt='" +
            getAccessedAt() +
            "'" +
            ", actorLogin='" +
            getActorLogin() +
            "'" +
            ", action='" +
            getAction() +
            "'" +
            "}"
        );
    }
}
