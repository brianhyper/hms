package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.OverrideService;
import com.hyperbrains.hms.service.dto.view.OverrideRequestDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for recording and reviewing overrides.
 */
@Service
@Transactional
public class OverrideServiceImpl implements OverrideService {

    private static final Logger LOG = LoggerFactory.getLogger(OverrideServiceImpl.class);

    private final AuditLogService auditLogService;

    private final AuditLogRepository auditLogRepository;

    public OverrideServiceImpl(AuditLogService auditLogService, AuditLogRepository auditLogRepository) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(OverrideRequestDTO request) {
        if (request.getReason() == null || request.getReason().isBlank()) {
            // Re-checked here, not only on the DTO: bean validation runs only when the request comes in over HTTP,
            // and the reason is the control that replaces the missing precondition.
            throw BusinessRuleViolationException.of(
                "overrideReasonRequired",
                "override",
                "An override goes ahead without its usual precondition, so it must say why."
            );
        }
        // The actor comes from the trail's own attribution; the role is captured here because it is the role held at
        // the time, which the account's current roles do not answer later.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.OVERRIDE_GRANTED, request.getOverriddenEntity(), request.getOverriddenEntityId())
                .withReason(request.getReason())
                .withDetails("Roles at the time: " + String.join(", ", SecurityUtils.getCurrentUserAuthorities()))
        );
        LOG.info(
            "Override recorded on {} {} by {}",
            request.getOverriddenEntity(),
            request.getOverriddenEntityId(),
            SecurityUtils.getCurrentUserLogin().orElse("an unidentified caller")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordHistoryEntryDTO> recordedSince(Instant from) {
        return auditLogRepository
            .findByActionSince(AuditActions.OVERRIDE_GRANTED, from)
            .stream()
            .map(RecordHistoryEntryDTO::from)
            .toList();
    }
}
