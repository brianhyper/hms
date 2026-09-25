package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.VitalsRejectedException;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.StartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionResultDTO;
import com.hyperbrains.hms.service.dto.view.VitalsWarningDTO;
import com.hyperbrains.hms.service.mapper.VisitMapper;
import com.hyperbrains.hms.service.mapper.VitalSignsMapper;
import com.hyperbrains.hms.service.rules.QueueKind;
import com.hyperbrains.hms.service.rules.VitalsValidator;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.TriageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triage workflow. The thresholds live in {@link VitalsValidator}; this class decides what to do
 * with the outcome — save and warn, or refuse — and moves the visit along.
 */
@Service
@Transactional
public class TriageServiceImpl implements TriageService {

    private static final Logger LOG = LoggerFactory.getLogger(TriageServiceImpl.class);

    private final VisitRepository visitRepository;

    private final VitalSignsRepository vitalSignsRepository;

    private final VisitMapper visitMapper;

    private final VitalSignsMapper vitalSignsMapper;

    private final AuditLogService auditLogService;

    private final HmsProperties properties;

    public TriageServiceImpl(
        VisitRepository visitRepository,
        VitalSignsRepository vitalSignsRepository,
        VisitMapper visitMapper,
        VitalSignsMapper vitalSignsMapper,
        AuditLogService auditLogService,
        HmsProperties properties
    ) {
        this.visitRepository = visitRepository;
        this.vitalSignsRepository = vitalSignsRepository;
        this.visitMapper = visitMapper;
        this.vitalSignsMapper = vitalSignsMapper;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public VisitDTO startVitals(Long visitId, StartVitalsRequestDTO request) {
        Visit visit = loadOpenVisit(visitId);
        requireAwaitingVitals(visit);

        if (visit.getStatus() == VisitStatus.WAITING_VITALS) {
            // Only checked on the transition, so calling this twice is harmless rather than
            // demanding a skip reason to re-open a patient already being worked on.
            requireSkipReasonIfOutOfOrder(visit, request.getQueueSkipReason());

            if (isNotBlank(request.getQueueSkipReason())) {
                visit.setQueueSkipReason(request.getQueueSkipReason());
                auditLogService.record(
                    AuditLogService.Entry.of(AuditActions.VISIT_QUEUE_SKIPPED, "Visit", visit.getId()).withReason(
                        request.getQueueSkipReason()
                    )
                );
            }

            visit.setStatus(VisitStatus.IN_VITALS);
            visit.setStartedVitalsAt(Instant.now());
            visit = visitRepository.save(visit);

            auditLogService.record(AuditLogService.Entry.of(AuditActions.VITALS_STARTED, "Visit", visit.getId()));
            LOG.debug("Started vitals for visit {}", visit.getId());
        }

        return visitMapper.toDto(visit);
    }

    @Override
    public VitalsSubmissionResultDTO submitVitals(Long visitId, VitalsSubmissionRequestDTO request) {
        Visit visit = loadOpenVisit(visitId);

        // A correction is a different event from a first reading. By the time anyone corrects vitals
        // the patient has long since left the vitals queue — they are with a doctor, or waiting on
        // results — so demanding WAITING_VITALS here would make the documented "edit in place with a
        // reason" path unreachable. Vitals exist for at most one row per visit, so their presence is
        // what distinguishes the two cases, not the visit's status.
        VitalSigns existing = visit.getVitals();
        boolean correction = existing != null;
        if (!correction) {
            requireAwaitingVitals(visit);
        }

        VitalsValidator.Readings readings = new VitalsValidator.Readings(
            request.getTemperature(),
            request.getPulseRate(),
            request.getSystolicBp(),
            request.getDiastolicBp(),
            request.getOxygenSaturation(),
            request.getWeight(),
            request.getHeight()
        );

        VitalsValidator.Outcome outcome = VitalsValidator.validate(readings, properties.getVitals());
        if (outcome.isRejected()) {
            // Hard block. A value outside the possible range is a typing mistake, and writing it
            // down would make the clinical record state something that cannot be true. The
            // warnings collected alongside are discarded with it — the whole submission is refused,
            // so reporting "also, the pulse looks high" would be noise on a request that failed.
            throw new VitalsRejectedException(outcome.rejections());
        }

        if (correction && isBlank(request.getCorrectionReason())) {
            // A vital sign is the kind of record that gets quietly altered to look tidier. Editing
            // it in place is allowed, but never anonymously.
            throw BusinessRuleViolationException.of(
                "vitalsCorrectionReasonRequired",
                "vitalSigns",
                "Amending vitals already recorded for this visit requires a reason"
            );
        }

        Map<String, String> before = correction ? snapshot(existing) : Map.of();

        VitalSigns vitalSigns = correction ? existing : new VitalSigns();
        apply(readings, vitalSigns, request);
        // Derived, never taken from the request, so it cannot contradict the weight and height
        // recorded beside it.
        vitalSigns.setBmi(VitalsValidator.bmi(request.getWeight(), request.getHeight()));
        vitalSigns = vitalSignsRepository.save(vitalSigns);

        if (!correction) {
            visit.setVitals(vitalSigns);
            if (visit.getStartedVitalsAt() == null) {
                visit.setStartedVitalsAt(Instant.now());
            }
            // The visit moves itself into the doctor's queue the moment vitals exist. A correction
            // deliberately leaves the status alone: the patient has already moved on, and pulling
            // them back into the queue would send them to be seen twice.
            visit.setStatus(VisitStatus.WAITING_DOCTOR);
        }
        visit = visitRepository.save(visit);

        String warnings = describeWarnings(outcome.warnings());
        if (correction) {
            // One row per changed field, so "who altered this patient's blood pressure, when and why" is a
            // query. The previous encoding was a single old/new blob covering only the numeric readings,
            // which meant a corrected triage note or nutritional status left no trace at all.
            auditLogService.recordCorrection(
                AuditActions.VITALS_CORRECTED,
                "Visit",
                visit.getId(),
                request.getCorrectionReason(),
                before,
                snapshot(vitalSigns)
            );
        } else {
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.VITALS_RECORDED, "Visit", visit.getId()).withDetails(warnings)
            );
        }

        if (outcome.hasWarnings()) {
            LOG.info("Vitals for visit {} recorded with {} warning(s)", visit.getId(), outcome.warnings().size());
        }

        return result(vitalSigns, visit, correction, outcome.warnings());
    }

    private VitalsSubmissionResultDTO result(
        VitalSigns vitalSigns,
        Visit visit,
        boolean correction,
        List<VitalsValidator.Warning> warnings
    ) {
        VitalsSubmissionResultDTO dto = new VitalsSubmissionResultDTO();
        dto.setVitalSigns(vitalSignsMapper.toDto(vitalSigns));
        dto.setVisitStatus(visit.getStatus());
        dto.setCorrection(correction);
        dto.setWarnings(
            warnings
                .stream()
                .map(warning -> {
                    VitalsWarningDTO dtoWarning = new VitalsWarningDTO();
                    dtoWarning.setField(warning.field());
                    dtoWarning.setValue(warning.value());
                    dtoWarning.setWarnMin(warning.warnMin());
                    dtoWarning.setWarnMax(warning.warnMax());
                    dtoWarning.setMessage(warning.message());
                    return dtoWarning;
                })
                .toList()
        );
        return dto;
    }

    private void apply(VitalsValidator.Readings readings, VitalSigns vitalSigns, VitalsSubmissionRequestDTO request) {
        // Full replacement, so leaving a field out clears it. That is what makes it possible to
        // remove a value entered against the wrong patient or the wrong unit.
        vitalSigns.setTemperature(readings.temperature());
        vitalSigns.setPulseRate(readings.pulseRate());
        vitalSigns.setSystolicBp(readings.systolicBp());
        vitalSigns.setDiastolicBp(readings.diastolicBp());
        vitalSigns.setOxygenSaturation(readings.oxygenSaturation());
        vitalSigns.setWeight(readings.weight());
        vitalSigns.setHeight(readings.height());
        vitalSigns.setNutritionalStatus(request.getNutritionalStatus());
        vitalSigns.setPregnancyScreening(request.getPregnancyScreening());
        vitalSigns.setTriageNotes(request.getTriageNotes());
        vitalSigns.setOtherMeasurements(request.getOtherMeasurements());
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

    private void requireAwaitingVitals(Visit visit) {
        if (visit.getStatus() != VisitStatus.WAITING_VITALS && visit.getStatus() != VisitStatus.IN_VITALS) {
            throw BusinessRuleViolationException.of(
                "visitNotAwaitingVitals",
                "visit",
                "Visit " + visit.getId() + " is " + visit.getStatus() + " and is not awaiting vitals"
            );
        }
    }

    private void requireSkipReasonIfOutOfOrder(Visit visit, String reason) {
        List<Visit> head = visitRepository.findQueue(QueueKind.VITALS.statuses(), PageRequest.of(0, 1)).getContent();
        if (VisitLifecycle.isOutOfOrder(visit, head) && isBlank(reason)) {
            throw BusinessRuleViolationException.of(
                "queueSkipReasonRequired",
                "visit",
                "This patient is not next in the vitals queue; a reason is required to select them out of order"
            );
        }
    }

    /**
     * Every recorded reading, as text, for the correction history.
     *
     * <p>Includes the qualitative fields as well as the numeric ones. Correcting a triage note is still a
     * change to the clinical record, and a trail that only watches the numbers would not show it.
     */
    static Map<String, String> snapshot(VitalSigns vitalSigns) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("temperature", text(vitalSigns.getTemperature()));
        values.put("pulseRate", text(vitalSigns.getPulseRate()));
        values.put("systolicBp", text(vitalSigns.getSystolicBp()));
        values.put("diastolicBp", text(vitalSigns.getDiastolicBp()));
        values.put("oxygenSaturation", text(vitalSigns.getOxygenSaturation()));
        values.put("weight", text(vitalSigns.getWeight()));
        values.put("height", text(vitalSigns.getHeight()));
        values.put("bmi", text(vitalSigns.getBmi()));
        values.put("nutritionalStatus", vitalSigns.getNutritionalStatus());
        values.put("pregnancyScreening", text(vitalSigns.getPregnancyScreening()));
        values.put("triageNotes", vitalSigns.getTriageNotes());
        values.put("otherMeasurements", vitalSigns.getOtherMeasurements());
        return values;
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static String describeWarnings(List<VitalsValidator.Warning> warnings) {
        if (warnings.isEmpty()) {
            return null;
        }
        return warnings.stream().map(VitalsValidator.Warning::message).collect(Collectors.joining("; "));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}
