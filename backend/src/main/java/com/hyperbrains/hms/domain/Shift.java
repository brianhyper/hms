package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.ShiftType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Who is on duty, when, and where — the roster.
 *
 * <p>Phase 4's P4.1, and by the client's ruling 1a the single source of truth for "who is on duty". The question
 * "who is covering this ward now" is answered by a query over this table rather than by a second table that states
 * its own answer, which is why the ward is a column here and why {@code WardCover} is being reduced to a view of
 * this one. Two writable tables that both claim to say who is on duty always diverge, and the divergence is found
 * during an incident.
 *
 * <p>A shift belongs to a {@link StaffRecord}, not to a {@link User}, because the roster has to hold the people who
 * never sign in — a cleaner, a porter, a driver. A member of staff is a person's employment; an account is their
 * credentials. Rostering the account would leave everyone without one unrostered, and the ward that cannot see the
 * porter on duty is the ward that cannot find them.
 *
 * <p>The ward is optional, deliberately: a shift may be on no particular ward (a porter's, or a matron's across the
 * hospital), and refusing that would force a placeholder ward into the data, which is a lie the roster would then
 * carry forever.
 */
@Entity
@Table(name = "shift")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Shift implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    /**
     * The day the shift belongs to.
     *
     * <p>A date rather than the instant the shift starts, because the roster is read as a calendar: "who is on
     * Thursday" is the question, and it must have one answer whatever the clock says. The times below say when
     * within that day, and a night shift that runs past midnight still belongs to the day it started.
     */
    @NotNull
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", length = 20, nullable = false)
    private ShiftType shiftType;

    @NotNull
    @Column(name = "starts_at", nullable = false)
    private LocalTime startsAt;

    @NotNull
    @Column(name = "ends_at", nullable = false)
    private LocalTime endsAt;

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "department", "user" }, allowSetters = true)
    private StaffRecord staffRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "department" }, allowSetters = true)
    private Ward ward;

    @ManyToOne(optional = false)
    @NotNull
    private User createdBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Shift id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getShiftDate() {
        return this.shiftDate;
    }

    public Shift shiftDate(LocalDate shiftDate) {
        this.setShiftDate(shiftDate);
        return this;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public ShiftType getShiftType() {
        return this.shiftType;
    }

    public Shift shiftType(ShiftType shiftType) {
        this.setShiftType(shiftType);
        return this;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public LocalTime getStartsAt() {
        return this.startsAt;
    }

    public Shift startsAt(LocalTime startsAt) {
        this.setStartsAt(startsAt);
        return this;
    }

    public void setStartsAt(LocalTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalTime getEndsAt() {
        return this.endsAt;
    }

    public Shift endsAt(LocalTime endsAt) {
        this.setEndsAt(endsAt);
        return this;
    }

    public void setEndsAt(LocalTime endsAt) {
        this.endsAt = endsAt;
    }

    public String getNote() {
        return this.note;
    }

    public Shift note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public StaffRecord getStaffRecord() {
        return this.staffRecord;
    }

    public void setStaffRecord(StaffRecord staffRecord) {
        this.staffRecord = staffRecord;
    }

    public Shift staffRecord(StaffRecord staffRecord) {
        this.setStaffRecord(staffRecord);
        return this;
    }

    public Ward getWard() {
        return this.ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Shift ward(Ward ward) {
        this.setWard(ward);
        return this;
    }

    public User getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }

    public Shift createdBy(User user) {
        this.setCreatedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Shift)) {
            return false;
        }
        return getId() != null && getId().equals(((Shift) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Shift{" +
            "id=" + getId() +
            ", shiftDate='" + getShiftDate() + "'" +
            ", shiftType='" + getShiftType() + "'" +
            ", startsAt='" + getStartsAt() + "'" +
            ", endsAt='" + getEndsAt() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
