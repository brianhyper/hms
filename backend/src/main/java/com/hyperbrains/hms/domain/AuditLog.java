package com.hyperbrains.hms.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AuditLog.
 */
@Entity
@Table(name = "audit_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 80)
    @Column(name = "action", length = 80, nullable = false)
    private String action;

    @NotNull
    @Size(max = 120)
    @Column(name = "entity_name", length = 120, nullable = false)
    private String entityName;

    @NotNull
    @Size(max = 100)
    @Column(name = "entity_id", length = 100, nullable = false)
    private String entityId;
    @Column(name = "reason")
    private String reason;
    @Column(name = "old_value")
    private String oldValue;
    @Column(name = "new_value")
    private String newValue;
    @Column(name = "details")
    private String details;

    /**
     * Which field a correction changed. One audit row per changed field, so the history of a single
     * field is a query rather than a reading exercise.
     */
    @Size(max = 120)
    @Column(name = "field_name", length = 120)
    private String fieldName;

    @NotNull
    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User actor;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuditLog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return this.action;
    }

    public AuditLog action(String action) {
        this.setAction(action);
        return this;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public AuditLog entityName(String entityName) {
        this.setEntityName(entityName);
        return this;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public AuditLog entityId(String entityId) {
        this.setEntityId(entityId);
        return this;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getReason() {
        return this.reason;
    }

    public AuditLog reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public AuditLog oldValue(String oldValue) {
        this.setOldValue(oldValue);
        return this;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public AuditLog newValue(String newValue) {
        this.setNewValue(newValue);
        return this;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDetails() {
        return this.details;
    }

    public AuditLog details(String details) {
        this.setDetails(details);
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public AuditLog fieldName(String fieldName) {
        this.setFieldName(fieldName);
        return this;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Instant getPerformedAt() {
        return this.performedAt;
    }

    public AuditLog performedAt(Instant performedAt) {
        this.setPerformedAt(performedAt);
        return this;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }

    public User getActor() {
        return this.actor;
    }

    public void setActor(User user) {
        this.actor = user;
    }

    public AuditLog actor(User user) {
        this.setActor(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditLog)) {
            return false;
        }
        return getId() != null && getId().equals(((AuditLog) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditLog{" +
            "id=" + getId() +
            ", action='" + getAction() + "'" +
            ", entityName='" + getEntityName() + "'" +
            ", entityId='" + getEntityId() + "'" +
            ", reason='" + getReason() + "'" +
            ", oldValue='" + getOldValue() + "'" +
            ", newValue='" + getNewValue() + "'" +
            ", details='" + getDetails() + "'" +
            ", performedAt='" + getPerformedAt() + "'" +
            "}";
    }
}
