package com.hyperbrains.hms.service.dto.view;

import java.io.Serializable;
import java.time.Instant;

/**
 * One entry of the patient access log, as it is read back.
 *
 * <p>A view rather than an entity: the log has no editable shape at all, so there is nothing to map into that could
 * be sent back to the server.
 */
public record PatientAccessLogViewDTO(Long id, Long patientId, String actorLogin, String action, Instant accessedAt)
    implements Serializable {}
