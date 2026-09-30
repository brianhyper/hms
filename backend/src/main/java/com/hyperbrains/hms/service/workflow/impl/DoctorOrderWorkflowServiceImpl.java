package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.OrderExecution;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PersonNames;
import com.hyperbrains.hms.service.dto.view.CancelDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.DispenseLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.DoctorOrderViewDTO;
import com.hyperbrains.hms.service.dto.view.ExecuteOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.OrderExecutionViewDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.rules.AdmissionLifecycle;
import com.hyperbrains.hms.service.rules.DoctorOrderLifecycle;
import com.hyperbrains.hms.service.workflow.DispenseWorkflowService;
import com.hyperbrains.hms.service.workflow.DoctorOrderWorkflowService;
import com.hyperbrains.hms.service.workflow.InpatientAccessService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Doctor's orders on the ward.
 */
@Service
@Transactional
public class DoctorOrderWorkflowServiceImpl implements DoctorOrderWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(DoctorOrderWorkflowServiceImpl.class);

    private final DoctorOrderRepository doctorOrderRepository;

    private final OrderExecutionRepository orderExecutionRepository;

    private final AdmissionRepository admissionRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final UserRepository userRepository;

    private final PrescriptionWorkflowService prescriptionWorkflowService;

    private final PrescriptionLineRepository prescriptionLineRepository;

    private final DispenseWorkflowService dispenseWorkflowService;

    private final InpatientAccessService accessService;

    private final AuditLogService auditLogService;

    public DoctorOrderWorkflowServiceImpl(
        DoctorOrderRepository doctorOrderRepository,
        OrderExecutionRepository orderExecutionRepository,
        AdmissionRepository admissionRepository,
        PrescriptionRepository prescriptionRepository,
        UserRepository userRepository,
        PrescriptionWorkflowService prescriptionWorkflowService,
        PrescriptionLineRepository prescriptionLineRepository,
        DispenseWorkflowService dispenseWorkflowService,
        InpatientAccessService accessService,
        AuditLogService auditLogService
    ) {
        this.doctorOrderRepository = doctorOrderRepository;
        this.orderExecutionRepository = orderExecutionRepository;
        this.admissionRepository = admissionRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.prescriptionWorkflowService = prescriptionWorkflowService;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.dispenseWorkflowService = dispenseWorkflowService;
        this.accessService = accessService;
        this.auditLogService = auditLogService;
    }

    @Override
    public DoctorOrderViewDTO place(Long admissionId, PlaceDoctorOrderRequestDTO request) {
        if (request == null || request.getType() == null || request.getRecurrence() == null) {
            throw BusinessRuleViolationException.of(
                "orderDetailsRequired",
                "doctorOrder",
                "An order needs a type and whether it is one-off or recurring"
            );
        }
        if (request.getDetails() == null || request.getDetails().isBlank()) {
            // Checked here as well as on the DTO: the ward acts on what this says, and "give the usual" is not
            // something anybody can execute.
            throw BusinessRuleViolationException.of("orderDetailsRequired", "doctorOrder", "An order has to say what is to be done");
        }

        Admission admission = requireOpenStay(admissionId);
        User doctor = currentUser();

        // The §6 rule in one line: a drug order is backed by a real prescription, and there is no way to write
        // one without it. Everything about supply — reservation, queue, charge — stays in Phase 1's workflow.
        Prescription prescription = DoctorOrderLifecycle.requiresAPrescription(request.getType()) ? prescribe(admission, request) : null;

        DoctorOrder order = new DoctorOrder();
        order.setAdmission(admission);
        order.setType(request.getType());
        order.setRecurrence(request.getRecurrence());
        order.setDetails(request.getDetails());
        order.setFrequency(request.getFrequency());
        order.setEndDate(request.getEndDate());
        order.setStatus(DoctorOrderLifecycle.statusOnPlacing());
        order.setOrderedAt(Instant.now());
        order.setOrderedBy(doctor);
        order.setPrescription(prescription);
        order = doctorOrderRepository.save(order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_PLACED, "DoctorOrder", order.getId()).withDetails(
                describe(order) + (prescription == null ? "; nothing to supply" : "; supplied by prescription " + prescription.getId())
            )
        );

        LOG.info("Doctor order {} placed on admission {} by {}", order.getId(), admissionId, doctor.getLogin());

        return view(order, List.of());
    }

    @Override
    public DoctorOrderViewDTO execute(Long orderId, ExecuteOrderRequestDTO request) {
        DoctorOrder order = requireOrder(orderId);
        if (!DoctorOrderLifecycle.isExecutable(order.getStatus())) {
            throw BusinessRuleViolationException.of(
                "orderNotRunning",
                "doctorOrder",
                "Order " + orderId + " is " + order.getStatus() + ", so there is nothing left to carry out"
            );
        }

        User nurse = currentUser();
        OrderExecution execution = new OrderExecution();
        execution.setDoctorOrder(order);
        execution.setExecutedBy(nurse);
        execution.setExecutedAt(Instant.now());
        execution.setNotes(request == null ? null : request.getNotes());
        execution = orderExecutionRepository.save(execution);

        // Medicine given is medicine gone, so recording the dose is what moves the stock. This runs in-process
        // against the reservation this order's own prescription took out, through the same logic the pharmacy
        // counter uses: the ward never reaches the counter's route, and it could not, because that route is
        // pharmacy's and stays pharmacy's. If the dose cannot be supplied the whole execution rolls back — a
        // dose charted against stock nobody handed over would be a record of something that did not happen.
        String supplied = takeOffTheShelf(order, execution, request);

        // A one-off order is done by doing it. A recurring one is not: the course ends when the prescriber says
        // so, not when the last charted dose is given.
        if (DoctorOrderLifecycle.completesOnExecution(order.getRecurrence())) {
            order.setStatus(DoctorOrderStatus.COMPLETED);
            order = doctorOrderRepository.save(order);
        }

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_EXECUTED, "DoctorOrder", order.getId()).withDetails(
                describe(order) +
                "; carried out by " +
                nurse.getLogin() +
                notes(execution.getNotes()) +
                supplied +
                "; now " +
                order.getStatus()
            )
        );

        return view(order, executionsOf(order));
    }

    /**
     * Takes this execution's dose off the shelf, and says what the prescription looks like afterwards.
     *
     * <p>An order that supplies no medicine takes nothing, and refuses to be told it did. The single-line
     * check is not decoration: an order is supplied by the prescription its own placement wrote, and with more
     * than one line there is no way to say which line the dose came from — better to refuse than to guess.
     */
    private String takeOffTheShelf(DoctorOrder order, OrderExecution execution, ExecuteOrderRequestDTO request) {
        Integer quantity = request == null ? null : request.getQuantity();

        if (!DoctorOrderLifecycle.requiresAPrescription(order.getType())) {
            if (quantity != null) {
                throw BusinessRuleViolationException.of(
                    "orderSuppliesNoStock",
                    "doctorOrder",
                    "Order " +
                    order.getId() +
                    " is a " +
                    order.getType() +
                    " order and supplies no medicine, so it cannot take " +
                    quantity +
                    " off the shelf"
                );
            }
            return "";
        }

        if (quantity == null || quantity < 1) {
            throw BusinessRuleViolationException.of(
                "orderDoseRequired",
                "doctorOrder",
                "Carrying out a drug order has to say how much was given, because the dose is what leaves the shelf"
            );
        }

        Prescription prescription = order.getPrescription();
        if (prescription == null) {
            throw BusinessRuleViolationException.of(
                "orderHasNoPrescription",
                "doctorOrder",
                "Order " + order.getId() + " is a drug order with nothing prescribed behind it, so there is no stock to hand over"
            );
        }

        List<PrescriptionLine> lines = prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId());
        if (lines.size() != 1) {
            throw BusinessRuleViolationException.of(
                "orderPrescriptionNotOneLine",
                "doctorOrder",
                "Order " +
                order.getId() +
                " is supplied by prescription " +
                prescription.getId() +
                ", which has " +
                lines.size() +
                " lines, so the dose cannot be attributed to one of them"
            );
        }
        PrescriptionLine line = lines.getFirst();

        DispenseLineRequestDTO given = new DispenseLineRequestDTO();
        given.setPrescriptionLineId(line.getId());
        given.setQuantity(quantity);

        DispenseRequestDTO handOver = new DispenseRequestDTO();
        handOver.setLines(List.of(given));
        handOver.setNote("recorded against order " + order.getId() + ", execution " + execution.getId());

        dispenseWorkflowService.dispense(prescription.getId(), handOver);

        // The entity is the instance the dispense just changed, so this is the status after the hand-over.
        PrescriptionStatus status = prescription.getStatus();
        return "; %d %s handed over, prescription %d now %s".formatted(quantity, line.getDrug().getUnit(), prescription.getId(), status);
    }

    @Override
    public DoctorOrderViewDTO complete(Long orderId) {
        DoctorOrder order = requireRunningOrder(orderId);

        order.setStatus(DoctorOrderStatus.COMPLETED);
        order = doctorOrderRepository.save(order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_COMPLETED, "DoctorOrder", order.getId()).withDetails(
                describe(order) + "; stopped by the prescriber"
            )
        );

        return view(order, executionsOf(order));
    }

    @Override
    public DoctorOrderViewDTO cancel(Long orderId, CancelDoctorOrderRequestDTO request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw BusinessRuleViolationException.of(
                "cancellationReasonRequired",
                "doctorOrder",
                "Withdrawing an order requires a reason"
            );
        }

        DoctorOrder order = requireRunningOrder(orderId);

        // A drug order is withdrawn through its prescription, which is where the reserved stock and the charge
        // live. If the medicine is already owed — paid for, or partly given — the prescription refuses and the
        // whole cancellation rolls back, which is the honest answer: the doctor cannot un-order what the patient
        // has already been supplied.
        if (order.getPrescription() != null) {
            prescriptionWorkflowService.cancel(order.getPrescription().getId(), request.getReason());
        }

        User doctor = currentUser();
        order.setStatus(DoctorOrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancelledBy(doctor);
        order.setCancelReason(request.getReason());
        order = doctorOrderRepository.save(order);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ORDER_CANCELLED, "DoctorOrder", order.getId())
                .withReason(request.getReason())
                .withDetails(describe(order) + "; withdrawn by " + doctor.getLogin())
        );

        return view(order, executionsOf(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorOrderViewDTO> orderSheet(Long admissionId) {
        // Reached by an admission id, so the row-level rule applies here too.
        accessService.requireMayView(admissionId);

        List<DoctorOrder> orders = doctorOrderRepository.findSheet(admissionId);
        if (orders.isEmpty()) {
            return List.of();
        }

        Map<Long, List<OrderExecution>> byOrder = orderExecutionRepository
            .findForOrders(orders.stream().map(DoctorOrder::getId).toList())
            .stream()
            .collect(Collectors.groupingBy(execution -> execution.getDoctorOrder().getId()));

        return orders.stream().map(order -> view(order, byOrder.getOrDefault(order.getId(), List.of()))).toList();
    }

    /** The prescription behind a drug order: stock reserved, charge raised, nothing invented here. */
    private Prescription prescribe(Admission admission, PlaceDoctorOrderRequestDTO request) {
        if (request.getDrugId() == null || request.getQuantity() == null) {
            throw BusinessRuleViolationException.of(
                "drugAndQuantityRequired",
                "doctorOrder",
                "A drug order has to name the drug and how much of it"
            );
        }

        PrescriptionLineRequestDTO line = new PrescriptionLineRequestDTO();
        line.setDrugId(request.getDrugId());
        line.setQuantity(request.getQuantity());
        line.setDosage(request.getDosage());
        line.setDuration(request.getDuration());

        PlacePrescriptionRequestDTO prescription = new PlacePrescriptionRequestDTO();
        prescription.setSource(PrescriptionSource.INTERNAL);
        prescription.setLines(List.of(line));

        PrescriptionViewDTO placed = prescriptionWorkflowService.placeForInpatient(admission.getVisit().getId(), prescription);

        return prescriptionRepository
            .findById(placed.getPrescriptionId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of(
                    "prescriptionNotFound",
                    "prescription",
                    "No prescription with id " + placed.getPrescriptionId()
                )
            );
    }

    /** What has been done about this order, oldest first, as the entity the view is built from. */
    private List<OrderExecution> executionsOf(DoctorOrder order) {
        return orderExecutionRepository.findForOrder(order.getId());
    }

    private DoctorOrderViewDTO view(DoctorOrder order, List<OrderExecution> executions) {
        return new DoctorOrderViewDTO(
            order.getId(),
            order.getAdmission() == null ? null : order.getAdmission().getId(),
            order.getType(),
            order.getRecurrence(),
            order.getStatus(),
            order.getDetails(),
            order.getFrequency(),
            order.getEndDate(),
            order.getOrderedAt(),
            PersonNames.displayName(order.getOrderedBy()),
            order.getPrescription() == null ? null : order.getPrescription().getId(),
            order.getCancelledAt(),
            PersonNames.displayName(order.getCancelledBy()),
            order.getCancelReason(),
            executions
                .stream()
                .map(execution ->
                    new OrderExecutionViewDTO(
                        execution.getId(),
                        execution.getExecutedAt(),
                        PersonNames.displayName(execution.getExecutedBy()),
                        execution.getNotes()
                    )
                )
                .toList()
        );
    }

    private Admission requireOpenStay(Long admissionId) {
        Admission admission = admissionRepository
            .findById(admissionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("admissionNotFound", "admission", "No admission with id " + admissionId)
            );
        if (!AdmissionLifecycle.isOpen(admission.getStatus())) {
            throw BusinessRuleViolationException.of(
                "admissionNotOpen",
                "admission",
                "Admission " + admissionId + " is " + admission.getStatus() + ", so nothing more can be ordered on it"
            );
        }
        return admission;
    }

    private DoctorOrder requireOrder(Long orderId) {
        return doctorOrderRepository
            .findById(orderId)
            .orElseThrow(() -> BusinessRuleViolationException.of("orderNotFound", "doctorOrder", "No order with id " + orderId));
    }

    private DoctorOrder requireRunningOrder(Long orderId) {
        DoctorOrder order = requireOrder(orderId);
        if (DoctorOrderLifecycle.isFinished(order.getStatus())) {
            throw BusinessRuleViolationException.of(
                "orderNotRunning",
                "doctorOrder",
                "Order " + orderId + " is " + order.getStatus() + " and cannot be changed"
            );
        }
        return order;
    }

    private static String describe(DoctorOrder order) {
        return order.getType() + " order " + order.getRecurrence() + ": " + order.getDetails();
    }

    private static String notes(String notes) {
        return notes == null || notes.isBlank() ? "" : " (" + notes + ")";
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "doctorOrder", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "doctorOrder", "No user account for " + login));
    }
}
