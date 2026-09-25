package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import java.io.Serializable;
import java.time.Instant;

/**
 * One row of a lab or radiology worklist.
 *
 * <p>Patient details are carried as {@link PatientSummaryDTO} — identifying information only. A
 * laboratory needs to know whose specimen it is holding; it has no business reading that patient's
 * allergies, conditions, identity document or next of kin. Using the full {@code PatientDTO} here
 * would hand a lab the whole clinical record for every test it runs.
 *
 * <p>The ordering doctor's {@code notes} are included, because they are instructions for performing
 * the test ("fasting sample") rather than unrelated clinical history.
 */
public class OrderWorklistItemDTO implements Serializable {

    private Long orderId;

    private Long visitId;

    private OrderType type;

    private String testName;

    private OrderStatus status;

    private Instant orderedAt;

    private String orderedByLogin;

    private String notes;

    private PatientSummaryDTO patient;

    public static OrderWorklistItemDTO from(DiagnosticOrder order) {
        OrderWorklistItemDTO dto = new OrderWorklistItemDTO();
        dto.orderId = order.getId();
        dto.type = order.getType();
        dto.testName = order.getTestName();
        dto.status = order.getStatus();
        dto.orderedAt = order.getOrderedAt();
        dto.notes = order.getNotes();
        dto.orderedByLogin = order.getOrderedBy() == null ? null : order.getOrderedBy().getLogin();
        if (order.getVisit() != null) {
            dto.visitId = order.getVisit().getId();
            dto.patient = PatientSummaryDTO.from(order.getVisit().getPatient());
        }
        return dto;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
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

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public String getOrderedByLogin() {
        return orderedByLogin;
    }

    public void setOrderedByLogin(String orderedByLogin) {
        this.orderedByLogin = orderedByLogin;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PatientSummaryDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientSummaryDTO patient) {
        this.patient = patient;
    }
}
