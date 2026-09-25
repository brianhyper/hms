package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A note added to a consultation that has already been completed.
 *
 * <p>Exists because of a specific rule: once a visit has moved on, a consultation's clinical notes
 * must never be overwritten. A correction at that point has to be recorded as a new entry that is
 * clearly linked to the original and leaves it completely untouched. That is the opposite of how
 * {@code Patient} and {@code VitalSigns} corrections work, which are edited in place with a reason
 * and their previous value kept in the audit trail.
 *
 * <p>Deliberately not a self-referencing {@code Consultation}: {@code Visit.consultation} is
 * one-to-one, so a second {@code Consultation} row for the same visit is impossible by construction.
 */
@Entity
@Table(name = "consultation_addendum")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConsultationAddendum implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JsonIgnoreProperties(value = { "doctor", "diagnoseses", "visit" }, allowSetters = true)
    private Consultation consultation;

    @NotNull
    @ManyToOne(optional = false)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User author;

    /** The note. Named {@code body} because {@code text} is a Postgres type name and reads badly as a column. */
    @NotNull
    @Column(name = "body", nullable = false)
    private String body;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
