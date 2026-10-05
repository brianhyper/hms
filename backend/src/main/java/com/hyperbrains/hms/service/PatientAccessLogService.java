package com.hyperbrains.hms.service;

import com.hyperbrains.hms.domain.PatientAccessLog;
import com.hyperbrains.hms.repository.PatientAccessLogRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.dto.view.PatientAccessLogViewDTO;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Who opened whose chart.
 *
 * <p>Records one entry when a chart is opened, and reads them back. It has no update and no delete, on purpose and
 * not merely by omission: a log that can be edited answers the question it was kept for only as far as nobody had a
 * reason to change it.
 *
 * <p>The entry is written only for a chart that was actually opened — the caller records after the patient has been
 * found, so a request for a patient who is not there, or one refused before it got that far, leaves nothing behind.
 * A log with entries for requests that failed is worse than no log, because it accuses.
 */
@Service
@Transactional
public class PatientAccessLogService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientAccessLogService.class);

    private final PatientAccessLogRepository patientAccessLogRepository;

    private final PatientRepository patientRepository;

    public PatientAccessLogService(PatientAccessLogRepository patientAccessLogRepository, PatientRepository patientRepository) {
        this.patientAccessLogRepository = patientAccessLogRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Records that the signed-in user opened this patient's chart.
     *
     * <p>Called only after the chart was read successfully. Nothing happens when there is no signed-in user, because
     * there is then no access to record.
     */
    public void recordChartOpen(Long patientId) {
        SecurityUtils.getCurrentUserLogin().ifPresent(login -> {
            PatientAccessLog entry = new PatientAccessLog();
            entry.setPatient(patientRepository.getReferenceById(patientId));
            entry.setActorLogin(login);
            entry.setAction(PatientAccessLog.VIEW);
            entry.setAccessedAt(Instant.now());
            patientAccessLogRepository.save(entry);
            LOG.debug("Chart opened: patient {} by {}", patientId, login);
        });
    }

    @Transactional(readOnly = true)
    public Page<PatientAccessLogViewDTO> findByPatient(Long patientId, Pageable pageable) {
        return patientAccessLogRepository
            .findByPatientIdOrderByAccessedAtDesc(patientId, pageable)
            .map(entry ->
                new PatientAccessLogViewDTO(
                    entry.getId(),
                    entry.getPatient().getId(),
                    entry.getActorLogin(),
                    entry.getAction(),
                    entry.getAccessedAt()
                )
            );
    }
}
