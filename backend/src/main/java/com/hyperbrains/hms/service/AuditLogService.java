package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.AuditLogDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.AuditLog}.
 */
public interface AuditLogService {
    /**
     * Save a auditLog.
     *
     * @param auditLogDTO the entity to save.
     * @return the persisted entity.
     */
    AuditLogDTO save(AuditLogDTO auditLogDTO);

    /**
     * Updates a auditLog.
     *
     * @param auditLogDTO the entity to update.
     * @return the persisted entity.
     */
    AuditLogDTO update(AuditLogDTO auditLogDTO);

    /**
     * Partially updates a auditLog.
     *
     * @param auditLogDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AuditLogDTO> partialUpdate(AuditLogDTO auditLogDTO);

    /**
     * Get all the auditLogs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuditLogDTO> findAll(Pageable pageable);

    /**
     * Get all the auditLogs with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuditLogDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" auditLog.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuditLogDTO> findOne(Long id);

    /**
     * Delete the "id" auditLog.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * One entry in the audit trail.
     *
     * <p>A record rather than seven positional strings: these calls are scattered through the
     * service layer and transposing two same-typed arguments would corrupt the trail silently.
     *
     * @param action     one of {@link AuditActions}
     * @param entityName the affected entity's simple name, e.g. {@code "Patient"}
     * @param entityId   the affected row's id, as a string so any id type fits
     * @param reason     why, when the operation is a correction, an override or a merge
     * @param oldValue   previous value, for corrections
     * @param newValue   replacement value, for corrections
     * @param details    anything else worth keeping, e.g. JSON of the changed fields
     */
    record Entry(
        String action,
        String entityName,
        String entityId,
        String reason,
        String oldValue,
        String newValue,
        String details,
        String fieldName
    ) {
        public static Entry of(String action, String entityName, Object entityId) {
            return new Entry(action, entityName, entityId == null ? null : entityId.toString(), null, null, null, null, null);
        }

        public Entry withReason(String reason) {
            return new Entry(action, entityName, entityId, reason, oldValue, newValue, details, fieldName);
        }

        public Entry withChange(String oldValue, String newValue) {
            return new Entry(action, entityName, entityId, reason, oldValue, newValue, details, fieldName);
        }

        public Entry withDetails(String details) {
            return new Entry(action, entityName, entityId, reason, oldValue, newValue, details, fieldName);
        }

        /** Names the single field this entry is about. */
        public Entry withField(String fieldName) {
            return new Entry(action, entityName, entityId, reason, oldValue, newValue, details, fieldName);
        }
    }

    /**
     * One field's before and after.
     *
     * <p>Values are strings because the audit log stores text: a correction may be to a date, a phone
     * number or a blood pressure, and the trail is for reading rather than for arithmetic.
     */
    record FieldChange(String field, String previousValue, String currentValue) {
        public static FieldChange of(String field, Object previousValue, Object currentValue) {
            return new FieldChange(field, asText(previousValue), asText(currentValue));
        }

        /**
         * The fields that actually differ between two snapshots of a record.
         *
         * <p>This is what keeps the trail honest in both directions. Logging every supplied field would
         * bury the real change in noise; logging the request instead of the result would let the trail
         * disagree with what was stored. Comparing snapshots makes a correction that changes nothing
         * record nothing, which is also how a caller can tell it had no effect.
         *
         * <p>Blank and absent are treated as the same: to a reader, a field that was empty and is still
         * empty has not changed, and a row saying it did is a false record.
         */
        public static List<FieldChange> diff(Map<String, String> before, Map<String, String> after) {
            List<FieldChange> changes = new ArrayList<>();
            for (Map.Entry<String, String> field : after.entrySet()) {
                String previous = before == null ? null : before.get(field.getKey());
                if (!sameValue(previous, field.getValue())) {
                    changes.add(new FieldChange(field.getKey(), previous, field.getValue()));
                }
            }
            return changes;
        }

        private static boolean sameValue(String left, String right) {
            return normalise(left).equals(normalise(right));
        }

        private static String normalise(String value) {
            return value == null ? "" : value.trim();
        }

        private static String asText(Object value) {
            return value == null ? null : value.toString();
        }
    }

    /**
     * Record an edit-in-place correction: one entry per field that actually changed.
     *
     * <p>One row per field is the whole point. Answering "who changed this patient's phone number, and
     * when, and why" is then a query, rather than a reading exercise over rows whose old and new values
     * are whole-record blobs.
     *
     * <p>The diff is taken here rather than by the caller, so that no caller can get it wrong by logging
     * every field it was sent, and so that a correction which changes nothing records nothing.
     *
     * @return the fields that changed, so the caller can report exactly what it applied
     */
    List<FieldChange> recordCorrection(
        String action,
        String entityName,
        Object entityId,
        String reason,
        Map<String, String> before,
        Map<String, String> after
    );

    /**
     * The audit trail of one record, oldest first.
     *
     * <p>Includes entries that changed no field — a creation, a status move — because the trail is read
     * as a story about the record, and a story with gaps is worse than a longer one.
     */
    List<RecordHistoryEntryDTO> trail(String entityName, String entityId);

    /**
     * Append an audit entry, attributing it to the authenticated caller.
     *
     * <p>Note this does not use the generated {@code save} method: that one takes a client-supplied
     * DTO including the actor, which would let a caller forge the trail.
     */
    void record(Entry entry);
}
