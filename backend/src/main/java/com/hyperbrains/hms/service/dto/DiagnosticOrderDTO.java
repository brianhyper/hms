package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.DiagnosticOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiagnosticOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private OrderType type;

    @NotNull
    @Size(max = 200)
    private String testName;

    @NotNull
    private OrderStatus status;
    private String notes;

    @NotNull
    private Instant orderedAt;

    private ResultDTO result;

    @NotNull
    private VisitDTO visit;

    @NotNull
    private UserDTO orderedBy;

    private LabTestDTO labTest;

    private RadiologyExamDTO radiologyExam;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public ResultDTO getResult() {
        return result;
    }

    public void setResult(ResultDTO result) {
        this.result = result;
    }

    public VisitDTO getVisit() {
        return visit;
    }

    public void setVisit(VisitDTO visit) {
        this.visit = visit;
    }

    public UserDTO getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(UserDTO orderedBy) {
        this.orderedBy = orderedBy;
    }

    public LabTestDTO getLabTest() {
        return labTest;
    }

    public void setLabTest(LabTestDTO labTest) {
        this.labTest = labTest;
    }

    public RadiologyExamDTO getRadiologyExam() {
        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExamDTO radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiagnosticOrderDTO)) {
            return false;
        }

        DiagnosticOrderDTO diagnosticOrderDTO = (DiagnosticOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, diagnosticOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiagnosticOrderDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", testName='" + getTestName() + "'" +
            ", status='" + getStatus() + "'" +
            ", notes='" + getNotes() + "'" +
            ", orderedAt='" + getOrderedAt() + "'" +
            ", result=" + getResult() +
            ", visit=" + getVisit() +
            ", orderedBy=" + getOrderedBy() +
            ", labTest=" + getLabTest() +
            ", radiologyExam=" + getRadiologyExam() +
            "}";
    }
}
