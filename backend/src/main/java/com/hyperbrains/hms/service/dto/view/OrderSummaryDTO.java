package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The narrow view Finance needs to bill for a test: what it is called, what it costs, and whether
 * it is done.
 *
 * <p>Deliberately excludes the order's clinical notes and anything about the consultation. Finance
 * generating a charge does not need to read why the test was ordered or what the patient's
 * examination findings were, and giving it the full order would hand the billing desk a clinical
 * record it has no reason to hold. The distinction the specification draws is only enforceable by
 * having a separate type for it.
 */
public class OrderSummaryDTO implements Serializable {

    private Long orderId;

    private Long visitId;

    private OrderType type;

    private String testName;

    private BigDecimal price;

    private OrderStatus status;

    /**
     * @param price the catalogue price, which is what the charge is based on. Null when the order has
     *              no catalogue entry, which the placement endpoint prevents.
     */
    public static OrderSummaryDTO from(DiagnosticOrder order, BigDecimal price) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.orderId = order.getId();
        dto.type = order.getType();
        dto.testName = order.getTestName();
        dto.status = order.getStatus();
        dto.price = price;
        if (order.getVisit() != null) {
            dto.visitId = order.getVisit().getId();
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
