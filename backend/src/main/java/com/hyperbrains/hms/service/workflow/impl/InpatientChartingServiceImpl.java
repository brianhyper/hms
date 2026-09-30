package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.InpatientVitals;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.InpatientVitalsRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PersonNames;
import com.hyperbrains.hms.service.VitalsRejectedException;
import com.hyperbrains.hms.service.dto.view.ChartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.InpatientVitalsViewDTO;
import com.hyperbrains.hms.service.dto.view.VitalsChartingResultDTO;
import com.hyperbrains.hms.service.dto.view.VitalsWarningDTO;
import com.hyperbrains.hms.service.rules.AdmissionLifecycle;
import com.hyperbrains.hms.service.rules.VitalsValidator;
import com.hyperbrains.hms.service.workflow.InpatientAccessService;
import com.hyperbrains.hms.service.workflow.InpatientChartingService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charting on the ward, and correcting what was charted.
 */
@Service
@Transactional
public class InpatientChartingServiceImpl implements InpatientChartingService {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientChartingServiceImpl.class);

    private final AdmissionRepository admissionRepository;

    private final InpatientVitalsRepository inpatientVitalsRepository;

    private final UserRepository userRepository;

    private final InpatientAccessService accessService;

    private final HmsProperties properties;

    private final AuditLogService auditLogService;

    public InpatientChartingServiceImpl(
        AdmissionRepository admissionRepository,
        InpatientVitalsRepository inpatientVitalsRepository,
        UserRepository userRepository,
        InpatientAccessService accessService,
        HmsProperties properties,
        AuditLogService auditLogService
    ) {
        this.admissionRepository = admissionRepository;
        this.inpatientVitalsRepository = inpatientVitalsRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.properties = properties;
        this.auditLogService = auditLogService;
    }

    @Override
    public VitalsChartingResultDTO chart(Long admissionId, ChartVitalsRequestDTO request) {
        if (request == null) {
            throw BusinessRuleViolationException.of("observationRequired", "inpatientVitals", "An observation is required");
        }
        if (isPresent(request.getCorrectionReason())) {
            // Refused rather than ignored. A caller who supplied a reason believes they are correcting
            // something; accepting it as an ordinary row would leave them with a chart that records something
            // they did not intend, and no way to tell afterwards that they meant something else.
            throw BusinessRuleViolationException.of(
                "correctionReasonNotExpected",
                "inpatientVitals",
                "To replace an observation that was charted wrongly, use the corrections endpoint: charting always adds a row"
            );
        }

        Admission admission = requireOpenStay(admissionId);
        VitalsValidator.Outcome outcome = validate(request);
        if (outcome.isRejected()) {
            throw new VitalsRejectedException(outcome.rejections());
        }

        InpatientVitals vitals = new InpatientVitals();
        vitals.setAdmission(admission);
        vitals.setRecordedBy(currentUser());
        vitals.setRecordedAt(Instant.now());
        apply(vitals, request);
        vitals = inpatientVitalsRepository.save(vitals);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.VITALS_RECORDED, "Admission", admission.getId()).withDetails(
                describeWarnings(outcome.warnings())
            )
        );

        if (outcome.hasWarnings()) {
            LOG.info("Observation {} charted for admission {} with {} warning(s)", vitals.getId(), admission.getId(), outcome.warnings().size());
        }

        return new VitalsChartingResultDTO(view(vitals, null), false, warnings(outcome.warnings()));
    }

    @Override
    public VitalsChartingResultDTO correct(Long admissionId, Long vitalsId, ChartVitalsRequestDTO request) {
        Admission admission = requireOpenStay(admissionId);

        InpatientVitals original = inpatientVitalsRepository
            .findById(vitalsId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("observationNotFound", "inpatientVitals", "No observation with id " + vitalsId)
            );
        if (original.getAdmission() == null || !admissionId.equals(original.getAdmission().getId())) {
            throw BusinessRuleViolationException.of(
                "observationNotOnThisStay",
                "inpatientVitals",
                "Observation " + vitalsId + " was not charted for admission " + admissionId
            );
        }
        if (inpatientVitalsRepository.existsByCorrectsId(vitalsId)) {
            // A second correction of the same row would leave two rows each claiming to replace it, and no way
            // to tell which one is in force. Correct the correction instead, and the chain stays a chain.
            throw BusinessRuleViolationException.of(
                "observationAlreadyCorrected",
                "inpatientVitals",
                "Observation " + vitalsId + " has already been replaced; correct the replacement instead"
            );
        }
        if (!isPresent(request == null ? null : request.getCorrectionReason())) {
            throw BusinessRuleViolationException.of(
                "correctionReasonRequired",
                "inpatientVitals",
                "Replacing a charted observation requires a reason"
            );
        }

        VitalsValidator.Outcome outcome = validate(request);
        if (outcome.isRejected()) {
            throw new VitalsRejectedException(outcome.rejections());
        }

        InpatientVitals corrected = new InpatientVitals();
        corrected.setAdmission(admission);
        corrected.setRecordedBy(currentUser());
        corrected.setRecordedAt(Instant.now());
        corrected.setCorrects(original);
        corrected.setCorrectionReason(request.getCorrectionReason());
        apply(corrected, request);
        corrected = inpatientVitalsRepository.save(corrected);

        // One audit row per field that changed, so "who altered this patient's blood pressure, when and why"
        // is a query rather than a diff of two rows somebody has to notice.
        auditLogService.recordCorrection(
            AuditActions.VITALS_CORRECTED,
            "Admission",
            admission.getId(),
            request.getCorrectionReason(),
            snapshot(original),
            snapshot(corrected)
        );

        LOG.info("Observation {} superseded by {} for admission {}", vitalsId, corrected.getId(), admission.getId());

        return new VitalsChartingResultDTO(view(corrected, null), true, warnings(outcome.warnings()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InpatientVitalsViewDTO> chartOf(Long admissionId) {
        // The row-level rule, enforced rather than merely filtered: this is reached by an admission id, and the
        // patient behind that id is the whole point of the rule.
        accessService.requireMayView(admissionId);

        List<InpatientVitals> chart = inpatientVitalsRepository.findChart(admissionId);

        // Which row was replaced by which, resolved in one pass rather than a query per row. Two corrections of
        // the same observation cannot happen, so the first wins and no merge function is needed.
        Map<Long, Long> supersededBy = chart
            .stream()
            .filter(vitals -> vitals.getCorrects() != null)
            .collect(Collectors.toMap(vitals -> vitals.getCorrects().getId(), InpatientVitals::getId));

        return chart.stream().map(vitals -> view(vitals, supersededBy.get(vitals.getId()))).toList();
    }

    private VitalsValidator.Outcome validate(ChartVitalsRequestDTO request) {
        VitalsValidator.Readings readings = new VitalsValidator.Readings(
            request.getTemperature(),
            request.getPulseRate(),
            request.getSystolicBp(),
            request.getDiastolicBp(),
            request.getOxygenSaturation(),
            request.getWeight(),
            request.getHeight()
        );
        return VitalsValidator.validate(readings, properties.getVitals());
    }

    private static void apply(InpatientVitals vitals, ChartVitalsRequestDTO request) {
        vitals.setTemperature(request.getTemperature());
        vitals.setPulseRate(request.getPulseRate());
        vitals.setSystolicBp(request.getSystolicBp());
        vitals.setDiastolicBp(request.getDiastolicBp());
        vitals.setOxygenSaturation(request.getOxygenSaturation());
        vitals.setWeight(request.getWeight());
        vitals.setHeight(request.getHeight());
        vitals.setNotes(request.getNotes());
        // Derived from the two figures recorded beside it, never taken from the request, so it cannot
        // contradict them.
        vitals.setBmi(VitalsValidator.bmi(request.getWeight(), request.getHeight()));
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
                "Admission " + admissionId + " is " + admission.getStatus() + ", so nothing more can be charted on it"
            );
        }
        return admission;
    }

    private InpatientVitalsViewDTO view(InpatientVitals vitals, Long supersededById) {
        return new InpatientVitalsViewDTO(
            vitals.getId(),
            vitals.getAdmission() == null ? null : vitals.getAdmission().getId(),
            vitals.getRecordedAt(),
            vitals.getTemperature(),
            vitals.getPulseRate(),
            vitals.getSystolicBp(),
            vitals.getDiastolicBp(),
            vitals.getOxygenSaturation(),
            vitals.getWeight(),
            vitals.getHeight(),
            vitals.getBmi(),
            vitals.getNotes(),
            PersonNames.displayName(vitals.getRecordedBy()),
            vitals.getCorrects() == null ? null : vitals.getCorrects().getId(),
            vitals.getCorrectionReason(),
            supersededById
        );
    }

    private static List<VitalsWarningDTO> warnings(List<VitalsValidator.Warning> warnings) {
        return warnings
            .stream()
            .map(warning -> {
                VitalsWarningDTO dto = new VitalsWarningDTO();
                dto.setField(warning.field());
                dto.setValue(warning.value());
                dto.setWarnMin(warning.warnMin());
                dto.setWarnMax(warning.warnMax());
                dto.setMessage(warning.message());
                return dto;
            })
            .toList();
    }

    private static String describeWarnings(List<VitalsValidator.Warning> warnings) {
        if (warnings.isEmpty()) {
            return "charted with no abnormal readings";
        }
        return warnings.stream().map(VitalsValidator.Warning::message).collect(Collectors.joining("; "));
    }

    private static Map<String, String> snapshot(InpatientVitals vitals) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("temperature", text(vitals.getTemperature()));
        values.put("pulseRate", text(vitals.getPulseRate()));
        values.put("systolicBp", text(vitals.getSystolicBp()));
        values.put("diastolicBp", text(vitals.getDiastolicBp()));
        values.put("oxygenSaturation", text(vitals.getOxygenSaturation()));
        values.put("weight", text(vitals.getWeight()));
        values.put("height", text(vitals.getHeight()));
        values.put("bmi", text(vitals.getBmi()));
        values.put("notes", vitals.getNotes());
        return values;
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "inpatientVitals", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("unknownUser", "inpatientVitals", "No user account for " + login)
            );
    }
}
