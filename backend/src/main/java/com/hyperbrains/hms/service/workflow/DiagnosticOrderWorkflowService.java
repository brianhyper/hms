package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.OrderSummaryDTO;
import com.hyperbrains.hms.service.dto.view.OrderWorklistItemDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lab and radiology orders, from the doctor ordering one to the result landing back on the visit.
 *
 * <p>Every state change here recomputes the visit's status, because any of them could be the last
 * outstanding item that releases the visit toward payment.
 *
 * <p>Named {@code ...Workflow...} rather than {@code DiagnosticOrderService} because the generated
 * CRUD service already owns that bean name — two classes mapping to {@code diagnosticOrderServiceImpl}
 * stops the whole application context from starting.
 */
public interface DiagnosticOrderWorkflowService {
    /**
     * Order a test. The catalogue entry supplies both the recorded test name and the price, and the
     * name is copied rather than referenced so a later rename cannot rewrite history.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the visit is closed.
     */
    DiagnosticOrderDTO place(Long visitId, PlaceDiagnosticOrderRequestDTO request);

    /**
     * Record what came back, complete the order, and charge for it.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the order is already
     *         resolved or cancelled.
     */
    DiagnosticOrderDTO enterResult(Long orderId, EnterResultRequestDTO request);

    /**
     * Abandon an order that will never be completed.
     *
     * <p>A cancelled order stops holding the visit back and is never charged for.
     */
    DiagnosticOrderDTO cancel(Long orderId);

    /**
     * A worklist scoped to what the caller is allowed to see.
     *
     * <p>A Lab user gets lab orders only. The filtering happens in the query, not on the response.
     */
    Page<OrderWorklistItemDTO> worklist(Pageable pageable);

    /** Everything ordered during a visit, for the doctor reviewing their patient. */
    List<OrderWorklistItemDTO> forVisit(Long visitId);

    /** Billing view of a visit's orders: what it is called and what it costs, and nothing clinical. */
    List<OrderSummaryDTO> billableForVisit(Long visitId);
}
