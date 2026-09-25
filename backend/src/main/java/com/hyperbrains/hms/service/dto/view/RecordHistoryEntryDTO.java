package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.AuditLog;
import java.time.Instant;

/**
 * One entry in a record's history.
 *
 * <p>Flat rather than nesting the actor's account: this is read to find out who changed what, and the
 * login is the answer to "who". Sending the full user record would carry roles and account state to
 * anybody allowed to read a correction history.
 */
public record RecordHistoryEntryDTO(
    Long id,
    String action,
    String entityName,
    String entityId,
    String fieldName,
    String previousValue,
    String currentValue,
    String reason,
    String details,
    String actorLogin,
    Instant performedAt
) {
    public static RecordHistoryEntryDTO from(AuditLog entry) {
        return new RecordHistoryEntryDTO(
            entry.getId(),
            entry.getAction(),
            entry.getEntityName(),
            entry.getEntityId(),
            entry.getFieldName(),
            entry.getOldValue(),
            entry.getNewValue(),
            entry.getReason(),
            entry.getDetails(),
            entry.getActor() == null ? null : entry.getActor().getLogin(),
            entry.getPerformedAt()
        );
    }
}
