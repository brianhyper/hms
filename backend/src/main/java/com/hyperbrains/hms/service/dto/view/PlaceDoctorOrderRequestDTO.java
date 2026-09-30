package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * A doctor's order on the ward.
 *
 * <p>One request type for four kinds of order, because they are one clinical act with one shape — what to do,
 * how often, for how long — and the differences are in what the order brings with it rather than in what the
 * doctor writes. A DRUG order needs the drug and the quantity as well, since placing it also places the
 * prescription that supplies it; the service refuses a drug order without them rather than storing an
 * instruction nobody can act on.
 *
 * <p>{@code frequency} is free text such as "q4h". A structured scheduling DSL is deliberately not offered:
 * v1.0 has no scheduler, so a structured field would suggest the system knows when the next dose is due when
 * it does not.
 */
public class PlaceDoctorOrderRequestDTO implements Serializable {

    @NotNull
    private DoctorOrderType type;

    @NotNull
    private DoctorOrderRecurrence recurrence;

    /** What is to be done, in the doctor's own words. */
    @NotNull
    @Size(max = 10000)
    private String details;

    @Size(max = 200)
    private String frequency;

    /** Optional: absent means the order runs until the prescriber stops it. */
    private Instant endDate;

    /** The drug, for a DRUG order. */
    private Long drugId;

    /** How much of it, for a DRUG order. */
    private Integer quantity;

    @Size(max = 500)
    private String dosage;

    @Size(max = 200)
    private String duration;

    public DoctorOrderType getType() {
        return type;
    }

    public void setType(DoctorOrderType type) {
        this.type = type;
    }

    public DoctorOrderRecurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(DoctorOrderRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
