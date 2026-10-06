package com.hyperbrains.hms.domain;

import com.hyperbrains.hms.domain.enumeration.StaffRecordStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A member of staff, whether or not they sign in.
 *
 * <p>Deliberately separate from {@code User}, which is Phase 3's decision and not an accident of modelling. A
 * {@code User} is an account: credentials, roles, lock state, session validity. This is a person's employment:
 * their name, their identity number, where they work, what they do and when they started. A cleaner, a porter or a
 * records clerk is a member of staff and needs none of the account machinery, so the link between the two is
 * optional and points the other way — a staff record may name the account a person signs in with, or may name
 * nothing.
 *
 * <p>The record is keyed on the national identity number rather than on the name. Names repeat, are spelled several
 * ways and change; an identity number does not, which is what makes a staff file worth keeping.
 */
@Entity
@Table(name = "staff_record")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StaffRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 120)
    @Column(name = "full_name", length = 120, nullable = false)
    private String fullName;

    /**
     * The identity this record is keyed on, and unique when it is there.
     *
     * <p>Optional, deliberately: this table holds a file on everyone who works here, including people who have no
     * system account and no identity number to hand — a cleaner, a porter, a driver. Requiring one would refuse
     * exactly the records the entity exists to allow. Names repeat and change, which is why the number stays unique
     * when present rather than being dropped.
     */
    @Size(max = 32)
    @Column(name = "national_id", length = 32, unique = true)
    private String nationalId;

    @Size(max = 120)
    @Column(name = "job_title", length = 120)
    private String jobTitle;

    @Size(max = 30)
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Size(max = 254)
    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @NotNull
    @Column(name = "employment_start_date", nullable = false)
    private LocalDate employmentStartDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StaffRecordStatus status;

    @ManyToOne(optional = false)
    @NotNull
    private Department department;

    /**
     * The account this person signs in with, or null for the many members of staff who have none.
     *
     * <p>Optional in both directions: a staff record without an account is the normal case for anyone who does not
     * use the system, and an account without a staff record is what every account created before this existed
     * looks like. Unique when present, so one login cannot be claimed by two people — two records sharing an account
     * would mean the rota, the leave balance and the payslip were attributed to whichever row was read first.
     */
    @ManyToOne
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public StaffRecord id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public StaffRecord fullName(String fullName) {
        this.setFullName(fullName);
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return this.nationalId;
    }

    public StaffRecord nationalId(String nationalId) {
        this.setNationalId(nationalId);
        return this;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getJobTitle() {
        return this.jobTitle;
    }

    public StaffRecord jobTitle(String jobTitle) {
        this.setJobTitle(jobTitle);
        return this;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getContactPhone() {
        return this.contactPhone;
    }

    public StaffRecord contactPhone(String contactPhone) {
        this.setContactPhone(contactPhone);
        return this;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public StaffRecord contactEmail(String contactEmail) {
        this.setContactEmail(contactEmail);
        return this;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDate getEmploymentStartDate() {
        return this.employmentStartDate;
    }

    public StaffRecord employmentStartDate(LocalDate employmentStartDate) {
        this.setEmploymentStartDate(employmentStartDate);
        return this;
    }

    public void setEmploymentStartDate(LocalDate employmentStartDate) {
        this.employmentStartDate = employmentStartDate;
    }

    public StaffRecordStatus getStatus() {
        return this.status;
    }

    public StaffRecord status(StaffRecordStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(StaffRecordStatus status) {
        this.status = status;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public StaffRecord department(Department department) {
        this.setDepartment(department);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StaffRecord user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StaffRecord)) {
            return false;
        }
        return id != null && id.equals(((StaffRecord) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "StaffRecord{" +
            "id=" +
            getId() +
            ", fullName='" +
            getFullName() +
            "'" +
            ", nationalId='" +
            getNationalId() +
            "'" +
            ", jobTitle='" +
            getJobTitle() +
            "'" +
            ", contactPhone='" +
            getContactPhone() +
            "'" +
            ", contactEmail='" +
            getContactEmail() +
            "'" +
            ", employmentStartDate='" +
            getEmploymentStartDate() +
            "'" +
            ", status='" +
            getStatus() +
            "'" +
            "}"
        );
    }
}
