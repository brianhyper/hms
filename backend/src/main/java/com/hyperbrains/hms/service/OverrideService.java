package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.view.OverrideRequestDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.time.Instant;
import java.util.List;

/**
 * The standard override / emergency-access mechanism: an action that goes ahead without its usual precondition, with a
 * reason, recorded as its own audit event and reviewable afterwards.
 *
 * <p>Single-step on purpose. The failure mode that matters in a hospital is a patient waiting while a second person is
 * found; the control that holds is the mandatory reason plus a trail that can be reviewed in one query, not a second
 * approver before the act. Two-person approval can be added later without rework, because the reason and the audit
 * entry are the entry point.
 *
 * <p>This is the mechanism only, deliberately: it has no route and no caller yet, so it cannot change any user-facing
 * behaviour. Its first caller will be the dispensing gate, which releases medicine ahead of the bill for a patient in
 * the scope the client is confirming.
 */
public interface OverrideService {

    /**
     * Record an override, attributed to the authenticated caller.
     *
     * @param request what was overridden and why
     * @throws BusinessRuleViolationException if no reason was given
     */
    void record(OverrideRequestDTO request);

    /**
     * Every override recorded at or after a moment, newest first.
     *
     * <p>This is the report the retrospective review is done from: "every override in the last month" is what makes
     * break-glass accountable, and it is one query.
     */
    List<RecordHistoryEntryDTO> recordedSince(Instant from);
}
