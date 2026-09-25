package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * What happened to one record, in order.
 *
 * <p>One route for every record rather than a history endpoint per entity, because the question is always
 * the same — "who changed this, when, and why" — and the audit table already answers it for all of them.
 * Entries that changed no field (a creation, a status move) are included, since a trail read as a story
 * is worse with gaps than long.
 */
@RestController
@RequestMapping("/api/record-history")
public class RecordHistoryResource {

    private final AuditLogService auditLogService;

    public RecordHistoryResource(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * @param entityName the entity's simple name, e.g. {@code Patient} or {@code Visit}
     * @param entityId   the record's id, as text
     */
    @GetMapping("/{entityName}/{entityId}")
    public ResponseEntity<List<RecordHistoryEntryDTO>> trail(
        @PathVariable String entityName,
        @PathVariable String entityId
    ) {
        return ResponseEntity.ok(auditLogService.trail(entityName, entityId));
    }
}
