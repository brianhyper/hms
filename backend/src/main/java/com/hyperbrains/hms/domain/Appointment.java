package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Appointment.
 */
@Entity
@Table(name = "appointment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Appointment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @NotNull
    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;
    @Column(name = "reason")
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Visit visit;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "mergedIntoPatient" }, allowSetters = true)
    private Patient patient;

    @ManyToOne(optional = false)
    @NotNull
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    private User doctor;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Appointment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getScheduledDate() {
        return this.scheduledDate;
    }

    public Appointment scheduledDate(LocalDate scheduledDate) {
        this.setScheduledDate(scheduledDate);
        return this;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return this.scheduledTime;
    }

    public Appointment scheduledTime(LocalTime scheduledTime) {
        this.setScheduledTime(scheduledTime);
        return this;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getReason() {
        return this.reason;
    }

    public Appointment reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AppointmentStatus getStatus() {
        return this.status;
    }

    public Appointment status(AppointmentStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Appointment visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Appointment patient(Patient patient) {
        this.setPatient(patient);
        return this;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Appointment department(Department department) {
        this.setDepartment(department);
        return this;
    }

    public User getDoctor() {
        return this.doctor;
    }

    public void setDoctor(User user) {
        this.doctor = user;
    }

    public Appointment doctor(User user) {
        this.setDoctor(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment)) {
            return false;
        }
        return getId() != null && getId().equals(((Appointment) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Appointment{" +
            "id=" + getId() +
            ", scheduledDate='" + getScheduledDate() + "'" +
            ", scheduledTime='" + getScheduledTime() + "'" +
            ", reason='" + getReason() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
