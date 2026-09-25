package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.dto.AuditLogDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import com.hyperbrains.hms.service.mapper.AuditLogMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.AuditLog}.
 */
@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    private final AuditLogMapper auditLogMapper;

    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
        this.userRepository = userRepository;
    }

    @Override
    public void record(Entry entry) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(entry.action());
        auditLog.setEntityName(entry.entityName());
        auditLog.setEntityId(entry.entityId());
        auditLog.setReason(entry.reason());
        auditLog.setOldValue(entry.oldValue());
        auditLog.setNewValue(entry.newValue());
        auditLog.setDetails(entry.details());
        auditLog.setFieldName(entry.fieldName());
        auditLog.setPerformedAt(Instant.now());
        currentActor().ifPresent(auditLog::setActor);
        auditLogRepository.save(auditLog);
        LOG.debug("Audit {} on {} {}", entry.action(), entry.entityName(), entry.entityId());
    }

    @Override
    public List<FieldChange> recordCorrection(
        String action,
        String entityName,
        Object entityId,
        String reason,
        Map<String, String> before,
        Map<String, String> after
    ) {
        List<FieldChange> changes = FieldChange.diff(before, after);
        for (FieldChange change : changes) {
            record(
                Entry.of(action, entityName, entityId)
                    .withReason(reason)
                    .withField(change.field())
                    .withChange(change.previousValue(), change.currentValue())
            );
        }
        if (changes.isEmpty()) {
            // Nothing was altered, so nothing is claimed to have been. A correction that changes no
            // field is usually a caller sending the value it was already holding.
            LOG.debug("Correction of {} {} changed no field", entityName, entityId);
        }
        return changes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordHistoryEntryDTO> trail(String entityName, String entityId) {
        return auditLogRepository
            .findByEntityNameAndEntityIdOrderByIdAsc(entityName, entityId)
            .stream()
            .map(RecordHistoryEntryDTO::from)
            .toList();
    }

    private Optional<User> currentActor() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }

    @Override
    public AuditLogDTO save(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to save AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    @Override
    public AuditLogDTO update(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to update AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    @Override
    public Optional<AuditLogDTO> partialUpdate(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to partially update AuditLog : {}", auditLogDTO);

        return auditLogRepository
            .findById(auditLogDTO.getId())
            .map(existingAuditLog -> {
                auditLogMapper.partialUpdate(existingAuditLog, auditLogDTO);

                return existingAuditLog;
            })
            .map(auditLogRepository::save)
            .map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AuditLogs");
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toDto);
    }

    public Page<AuditLogDTO> findAllWithEagerRelationships(Pageable pageable) {
        return auditLogRepository.findAllWithEagerRelationships(pageable).map(auditLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLogDTO> findOne(Long id) {
        LOG.debug("Request to get AuditLog : {}", id);
        return auditLogRepository.findOneWithEagerRelationships(id).map(auditLogMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AuditLog : {}", id);
        auditLogRepository.deleteById(id);
    }
}
