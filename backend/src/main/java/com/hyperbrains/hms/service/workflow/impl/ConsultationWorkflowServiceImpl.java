package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.ConsultationAddendum;
import com.hyperbrains.hms.domain.Diagnosis;
import com.hyperbrains.hms.domain.HospitalService;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.repository.ConsultationAddendumRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DiagnosisRepository;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.view.AddConsultationAddendumRequestDTO;
import com.hyperbrains.hms.service.dto.view.ConsultationAddendumDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.mapper.ConsultationMapper;
import com.hyperbrains.hms.service.rules.QueueKind;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.BillingService;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsultationWorkflowServiceImpl implements ConsultationWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultationWorkflowServiceImpl.class);

    private final VisitRepository visitRepository;

    private final ConsultationRepository consultationRepository;

    private final ConsultationAddendumRepository addendumRepository;

    private final DiagnosisRepository diagnosisRepository;

    private final HospitalServiceRepository hospitalServiceRepository;

    private final UserRepository userRepository;

    private final ConsultationMapper consultationMapper;

    private final BillingService billingService;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    private final HmsProperties properties;

    public ConsultationWorkflowServiceImpl(
        VisitRepository visitRepository,
        ConsultationRepository consultationRepository,
        ConsultationAddendumRepository addendumRepository,
        DiagnosisRepository diagnosisRepository,
        HospitalServiceRepository hospitalServiceRepository,
        UserRepository userRepository,
        ConsultationMapper consultationMapper,
        BillingService billingService,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService,
        HmsProperties properties
    ) {
        this.visitRepository = visitRepository;
        this.consultationRepository = consultationRepository;
        this.addendumRepository = addendumRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.hospitalServiceRepository = hospitalServiceRepository;
        this.userRepository = userRepository;
        this.consultationMapper = consultationMapper;
        this.billingService = billingService;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public ConsultationDTO start(Long visitId, StartConsultationRequestDTO request) {
        Visit visit = loadOpenVisit(visitId);

        Consultation alreadyOpen = visit.getConsultation();
        if (alreadyOpen != null) {
            // Re-opening the screen is not an error and must never create a second consultation:
            // Visit.consultation is one-to-one, and a duplicate would bill the fee twice.
            return consultationMapper.toDto(alreadyOpen);
        }

        if (visit.getStatus() != VisitStatus.WAITING_DOCTOR) {
            throw BusinessRuleViolationException.of(
                "visitNotAwaitingDoctor",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + " and is not waiting for a doctor"
            );
        }

        requireSkipReasonIfOutOfOrder(visit, request.getQueueSkipReason());
        if (isNotBlank(request.getQueueSkipReason())) {
            visit.setQueueSkipReason(request.getQueueSkipReason());
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.VISIT_QUEUE_SKIPPED, "Visit", visit.getId()).withReason(
                    request.getQueueSkipReason()
                )
            );
        }

        Consultation consultation = new Consultation();
        consultation.setDoctor(currentUser());
        consultation.setStatus(ConsultationStatus.IN_PROGRESS);
        consultation.setStartedAt(Instant.now());
        consultation = consultationRepository.save(consultation);

        visit.setConsultation(consultation);
        visit.setStatus(VisitStatus.IN_CONSULTATION);
        visit.setStartedConsultationAt(Instant.now());
        visitRepository.save(visit);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.CONSULTATION_STARTED, "Consultation", consultation.getId()).withDetails(
                "Visit " + visit.getId()
            )
        );
        LOG.debug("Opened consultation {} for visit {}", consultation.getId(), visit.getId());

        return consultationMapper.toDto(consultation);
    }

    @Override
    public ConsultationDTO updateNotes(Long consultationId, UpdateConsultationRequestDTO request) {
        Consultation consultation = loadConsultation(consultationId);
        requireEditable(consultation);

        Map<String, String> before = snapshot(consultation);
        apply(request, consultation);
        consultation = consultationRepository.save(consultation);

        // Field by field rather than a bare "updated": an in-progress consultation is edited in place, and
        // the trail has to show which part of the record moved. Deliberately no reason — this is ordinary
        // editing, not a correction of a record that others have already read. Once the consultation is
        // complete it stops being editable at all and changes become addenda instead.
        auditLogService.recordCorrection(
            AuditActions.CONSULTATION_UPDATED,
            "Consultation",
            consultation.getId(),
            null,
            before,
            snapshot(consultation)
        );
        return consultationMapper.toDto(consultation);
    }

    @Override
    public ConsultationDTO complete(Long consultationId, UpdateConsultationRequestDTO request) {
        Consultation consultation = loadConsultation(consultationId);
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw BusinessRuleViolationException.of(
                "consultationAlreadyCompleted",
                "consultation",
                "Consultation " + consultationId + " is already completed"
            );
        }

        apply(request, consultation);
        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.setCompletedAt(Instant.now());
        consultation = consultationRepository.save(consultation);

        Visit visit = visitRepository
            .findOneByConsultationId(consultation.getId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of(
                    "consultationWithoutVisit",
                    "consultation",
                    "Consultation " + consultationId + " is not attached to a visit"
                )
            );

        chargeConsultationFee(visit, consultation);
        advanceVisit(visit);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.CONSULTATION_COMPLETED, "Consultation", consultation.getId()).withDetails(
                "Visit " + visit.getId() + " moved to " + visit.getStatus()
            )
        );
        LOG.debug("Completed consultation {}; visit {} is now {}", consultation.getId(), visit.getId(), visit.getStatus());

        return consultationMapper.toDto(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationAddendumDTO> listAddenda(Long consultationId) {
        loadConsultation(consultationId);
        return addendumRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId).stream().map(this::toDto).toList();
    }

    @Override
    public ConsultationAddendumDTO addAddendum(Long consultationId, AddConsultationAddendumRequestDTO request) {
        Consultation consultation = loadConsultation(consultationId);
        if (consultation.getStatus() != ConsultationStatus.COMPLETED) {
            // While the consultation is in progress the notes are simply edited. An addendum exists
            // because the original can no longer be touched, so allowing one early would split the
            // record in two for no reason.
            throw BusinessRuleViolationException.of(
                "consultationNotCompleted",
                "consultation",
                "An addendum can only be added to a completed consultation; edit the notes while it is in progress"
            );
        }

        ConsultationAddendum addendum = new ConsultationAddendum();
        addendum.setConsultation(consultation);
        addendum.setAuthor(currentUser());
        addendum.setBody(request.getBody());
        addendum.setCreatedAt(Instant.now());
        addendum = addendumRepository.save(addendum);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.CONSULTATION_ADDENDUM, "Consultation", consultationId).withDetails(
                "Addendum " + addendum.getId() + " appended; original left unchanged"
            )
        );

        return toDto(addendum);
    }

    /**
     * Charge for the consultation the moment it is completed rather than at the end of the visit.
     * The price comes from the catalogue entry identified by its stable code, never its name.
     */
    private void chargeConsultationFee(Visit visit, Consultation consultation) {
        String code = properties.getBilling().getConsultationServiceCode();
        HospitalService fee = hospitalServiceRepository
            .findOneByCode(code)
            .orElseThrow(() ->
                BusinessRuleViolationException.of(
                    "consultationServiceNotConfigured",
                    "hospitalService",
                    "No HospitalService with code '" + code + "' exists, so a consultation fee cannot be determined"
                )
            );

        Bill bill = billingService.ensureBill(visit);
        billingService.addOrUpdateLine(
            bill,
            BillLineSourceType.CONSULTATION,
            BillingService.sourceRef(BillLineSourceType.CONSULTATION, consultation.getId()),
            fee.getName(),
            fee.getPrice()
        );
    }

    /**
     * Decide where the visit goes now that the consultation is finished.
     *
     * <p>Delegated rather than computed here: the same question is asked again every time an order or
     * a prescription changes state, and the answer has to be identical from every caller. The
     * outstanding counts are what make the difference between "waiting for the lab" and "ready to
     * pay", and they are read in one place.
     */
    private void advanceVisit(Visit visit) {
        visitStatusService.afterConsultation(visit.getId());
    }

    private void apply(UpdateConsultationRequestDTO request, Consultation consultation) {
        consultation.setPresentingComplaint(request.getPresentingComplaint());
        consultation.setExaminationFindings(request.getExaminationFindings());
        consultation.setObservations(request.getObservations());
        consultation.setDiagnosisOther(request.getDiagnosisOther());
        consultation.setFollowUpInstructions(request.getFollowUpInstructions());

        if (request.getDiagnosisIds() != null) {
            Set<Long> requested = new HashSet<>(request.getDiagnosisIds());
            Set<Diagnosis> diagnoses = new HashSet<>(diagnosisRepository.findAllById(requested));
            if (diagnoses.size() != requested.size()) {
                // Silently dropping an unmatched id would let a doctor believe a diagnosis was
                // recorded when it was not.
                throw BusinessRuleViolationException.of(
                    "unknownDiagnosis",
                    "consultation",
                    "One or more diagnosis ids do not exist in the catalogue"
                );
            }
            consultation.setDiagnoseses(diagnoses);
        }
    }

    /**
     * The editable parts of a consultation, as text, for the correction history.
     *
     * <p>The diagnoses are included as a sorted list of names: swapping a diagnosis is a change to the
     * clinical record, and it is the kind of change a reviewer most wants to see.
     */
    static Map<String, String> snapshot(Consultation consultation) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("presentingComplaint", consultation.getPresentingComplaint());
        values.put("examinationFindings", consultation.getExaminationFindings());
        values.put("observations", consultation.getObservations());
        values.put("diagnosisOther", consultation.getDiagnosisOther());
        values.put("followUpInstructions", consultation.getFollowUpInstructions());
        values.put(
            "diagnoses",
            consultation.getDiagnoseses() == null
                ? null
                : consultation.getDiagnoseses().stream().map(Diagnosis::getName).sorted().collect(Collectors.joining(", "))
        );
        return values;
    }

    private void requireEditable(Consultation consultation) {
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            // The original has been acted on by now — the fee is billed and results may be coming
            // back — so overwriting it would rewrite history other people have already read.
            throw BusinessRuleViolationException.of(
                "consultationCompleted",
                "consultation",
                "A completed consultation cannot be edited; add an addendum instead"
            );
        }
    }

    private Consultation loadConsultation(Long consultationId) {
        return consultationRepository
            .findById(consultationId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("consultationNotFound", "consultation", "No consultation with id " + consultationId)
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

    private void requireSkipReasonIfOutOfOrder(Visit visit, String reason) {
        List<Visit> head = visitRepository.findQueue(QueueKind.CONSULTATION.statuses(), PageRequest.of(0, 1)).getContent();
        if (VisitLifecycle.isOutOfOrder(visit, head) && isBlank(reason)) {
            throw BusinessRuleViolationException.of(
                "queueSkipReasonRequired",
                "visit",
                "This patient is not next in the consultation queue; a reason is required to select them out of order"
            );
        }
    }

    /**
     * The doctor the consultation belongs to. Taken from the authenticated principal, never from the
     * request — a client-supplied author would make the clinical record unverifiable.
     */
    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "consultation", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "consultation", "No user account for " + login));
    }

    private ConsultationAddendumDTO toDto(ConsultationAddendum addendum) {
        ConsultationAddendumDTO dto = new ConsultationAddendumDTO();
        dto.setId(addendum.getId());
        dto.setConsultationId(addendum.getConsultation().getId());
        dto.setBody(addendum.getBody());
        dto.setCreatedAt(addendum.getCreatedAt());

        User author = addendum.getAuthor();
        if (author != null) {
            dto.setAuthorLogin(author.getLogin());
            String firstName = author.getFirstName() == null ? "" : author.getFirstName();
            String lastName = author.getLastName() == null ? "" : author.getLastName();
            dto.setAuthorName((firstName + " " + lastName).trim());
        }
        return dto;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}
