package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.domain.Result;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.repository.RadiologyExamRepository;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.OrderSummaryDTO;
import com.hyperbrains.hms.service.dto.view.OrderWorklistItemDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.mapper.DiagnosticOrderMapper;
import com.hyperbrains.hms.service.rules.DiagnosticOrderLifecycle;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.BillingService;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagnosticOrderWorkflowServiceImpl implements DiagnosticOrderWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderWorkflowServiceImpl.class);

    private final VisitRepository visitRepository;

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    private final LabTestRepository labTestRepository;

    private final RadiologyExamRepository radiologyExamRepository;

    private final ResultRepository resultRepository;

    private final UserRepository userRepository;

    private final DiagnosticOrderMapper diagnosticOrderMapper;

    private final BillingService billingService;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    public DiagnosticOrderWorkflowServiceImpl(
        VisitRepository visitRepository,
        DiagnosticOrderRepository diagnosticOrderRepository,
        LabTestRepository labTestRepository,
        RadiologyExamRepository radiologyExamRepository,
        ResultRepository resultRepository,
        UserRepository userRepository,
        DiagnosticOrderMapper diagnosticOrderMapper,
        BillingService billingService,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.labTestRepository = labTestRepository;
        this.radiologyExamRepository = radiologyExamRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.diagnosticOrderMapper = diagnosticOrderMapper;
        this.billingService = billingService;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
    }

    @Override
    public DiagnosticOrderDTO place(Long visitId, PlaceDiagnosticOrderRequestDTO request) {
        Visit visit = loadOpenVisit(visitId);

        DiagnosticOrder order = new DiagnosticOrder();
        order.setType(request.getType());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderedAt(Instant.now());
        order.setOrderedBy(currentUser());
        order.setVisit(visit);
        order.setNotes(request.getNotes());

        // The catalogue entry is resolved here, not trusted from the request, so the recorded name
        // and the price always come from the same row.
        switch (request.getType()) {
            case LAB -> {
                LabTest test = labTestRepository
                    .findById(request.getLabTestId())
                    .orElseThrow(() ->
                        BusinessRuleViolationException.of("labTestNotFound", "labTest", "No lab test with id " + request.getLabTestId())
                    );
                order.setLabTest(test);
                order.setTestName(test.getName());
            }
            case RADIOLOGY -> {
                RadiologyExam exam = radiologyExamRepository
                    .findById(request.getRadiologyExamId())
                    .orElseThrow(() ->
                        BusinessRuleViolationException.of(
                            "radiologyExamNotFound",
                            "radiologyExam",
                            "No radiology exam with id " + request.getRadiologyExamId()
                        )
                    );
                order.setRadiologyExam(exam);
                order.setTestName(exam.getName());
            }
        }

        order = diagnosticOrderRepository.save(order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_PLACED, "DiagnosticOrder", order.getId()).withDetails(
                order.getType() + " " + order.getTestName() + " for visit " + visit.getId()
            )
        );

        // Normally a no-op: the visit is in consultation, which is not a derived status. It matters
        // if an order is added to a visit that is already waiting on results, which must then keep
        // waiting rather than settle.
        visitStatusService.recompute(visit.getId());

        LOG.debug("Placed {} order {} for visit {}", order.getType(), order.getId(), visit.getId());
        return diagnosticOrderMapper.toDto(order);
    }

    @Override
    public DiagnosticOrderDTO enterResult(Long orderId, EnterResultRequestDTO request) {
        DiagnosticOrder order = loadOrder(orderId);
        if (!DiagnosticOrderLifecycle.acceptsResult(order.getStatus())) {
            throw BusinessRuleViolationException.of(
                "orderAlreadyResolved",
                "diagnosticOrder",
                "Order " + orderId + " is " + order.getStatus() + " and can no longer receive a result"
            );
        }

        Result result = new Result();
        result.setResultValue(request.getResultValue());
        result.setNotes(request.getNotes());
        result.setImageReference(request.getImageReference());
        result.setEnteredAt(Instant.now());
        result.setEnteredBy(currentUser());
        result = resultRepository.save(result);

        order.setResult(result);
        order.setStatus(OrderStatus.COMPLETED);
        order = diagnosticOrderRepository.save(order);

        Visit visit = order.getVisit();
        chargeForOrder(visit, order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_RESULT_ENTERED, "DiagnosticOrder", order.getId()).withDetails(
                "Result " + result.getId() + " recorded by " + result.getEnteredBy().getLogin()
            )
        );

        // The last outstanding order releasing the visit is exactly the case this covers.
        visitStatusService.recompute(visit.getId());

        LOG.debug("Result {} recorded for order {}; visit {} is now {}", result.getId(), order.getId(), visit.getId(), visit.getStatus());
        return diagnosticOrderMapper.toDto(order);
    }

    @Override
    public DiagnosticOrderDTO cancel(Long orderId) {
        DiagnosticOrder order = loadOrder(orderId);
        if (!DiagnosticOrderLifecycle.isCancellable(order.getStatus())) {
            throw BusinessRuleViolationException.of(
                "orderNotCancellable",
                "diagnosticOrder",
                "Order " + orderId + " is " + order.getStatus() + " and cannot be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = diagnosticOrderRepository.save(order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_CANCELLED, "DiagnosticOrder", order.getId()).withReason(
                "Order abandoned before completion"
            )
        );

        // A cancelled order stops holding the visit back. It was never charged for, so there is
        // nothing to reverse.
        visitStatusService.recompute(order.getVisit().getId());

        return diagnosticOrderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWorklistItemDTO> worklist(Pageable pageable) {
        Set<OrderType> visibleTypes = visibleOrderTypes();
        if (visibleTypes.isEmpty()) {
            // Refuse locally rather than sending an empty IN list to the database.
            return Page.empty(pageable);
        }
        return diagnosticOrderRepository
            .findWorklist(visibleTypes, DiagnosticOrderLifecycle.WORKLIST, pageable)
            .map(OrderWorklistItemDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderWorklistItemDTO> forVisit(Long visitId) {
        return diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visitId).stream().map(OrderWorklistItemDTO::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> billableForVisit(Long visitId) {
        return diagnosticOrderRepository
            .findByVisitIdOrderByOrderedAtAsc(visitId)
            .stream()
            .map(order -> OrderSummaryDTO.from(order, cataloguePriceOrNull(order)))
            .toList();
    }

    /**
     * The order types the caller is entitled to see.
     *
     * <p>A Lab user sees lab work and a Radiology user sees imaging, and neither sees the other.
     * Doctors, administrators and the super-admin see everything, because the ordering doctor has to
     * be able to follow up on what they ordered.
     */
    private Set<OrderType> visibleOrderTypes() {
        if (
            SecurityUtils.hasCurrentUserAnyOfAuthorities(
                AuthoritiesConstants.ADMIN,
                AuthoritiesConstants.SUPER_ADMIN,
                AuthoritiesConstants.DOCTOR
            )
        ) {
            return EnumSet.allOf(OrderType.class);
        }

        Set<OrderType> types = EnumSet.noneOf(OrderType.class);
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.LAB)) {
            types.add(OrderType.LAB);
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.RADIOLOGY)) {
            types.add(OrderType.RADIOLOGY);
        }
        return types;
    }

    private void chargeForOrder(Visit visit, DiagnosticOrder order) {
        BillLineSourceType sourceType = switch (order.getType()) {
            case LAB -> BillLineSourceType.LAB;
            case RADIOLOGY -> BillLineSourceType.RADIOLOGY;
        };

        BigDecimal price = cataloguePriceOrNull(order);
        if (price == null) {
            // Charging nothing would quietly produce a bill the hospital cannot reconcile, so this
            // refuses instead of inventing a zero.
            throw BusinessRuleViolationException.of(
                "orderNotPriced",
                "diagnosticOrder",
                "Order " + order.getId() + " has no catalogue entry behind it, so it cannot be priced"
            );
        }

        Bill bill = billingService.ensureBill(visit);
        billingService.addOrUpdateLine(bill, sourceType, BillingService.sourceRef(sourceType, order.getId()), order.getTestName(), price);
    }

    private BigDecimal cataloguePriceOrNull(DiagnosticOrder order) {
        return switch (order.getType()) {
            case LAB -> order.getLabTest() == null ? null : order.getLabTest().getPrice();
            case RADIOLOGY -> order.getRadiologyExam() == null ? null : order.getRadiologyExam().getPrice();
        };
    }

    private DiagnosticOrder loadOrder(Long orderId) {
        return diagnosticOrderRepository
            .findById(orderId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("orderNotFound", "diagnosticOrder", "No diagnostic order with id " + orderId)
            );
    }

    private Visit loadOpenVisit(Long visitId) {
        Visit visit = visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
        if (!VisitLifecycle.isOpen(visit.getStatus())) {
            throw BusinessRuleViolationException.of(
                "visitNotOpen",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + " and no longer accepts clinical work"
            );
        }
        return visit;
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "diagnosticOrder", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "diagnosticOrder", "No user account for " + login));
    }
}
