package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.OverrideService;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The break-glass review: every override, for Administration to read after the fact.
 *
 * <p>Read-only, and the reason the mechanism is accountable at all — "every override in the last month" is one
 * request, which is what makes a single-step release safe to allow. Defaults to the last thirty days.
 *
 * <p>Authorization is declared in the {@code PHASE 1 RBAC TABLE} of
 * {@link com.hyperbrains.hms.config.SecurityConfiguration}.
 */
@RestController
@RequestMapping("/api/overrides")
public class OverrideResource {

    private static final Duration DEFAULT_WINDOW = Duration.ofDays(30);

    private final OverrideService overrideService;

    public OverrideResource(OverrideService overrideService) {
        this.overrideService = overrideService;
    }

    /**
     * {@code GET /overrides} : every override at or after {@code since}, newest first.
     *
     * @param since the start of the window, ISO-8601; the last thirty days when omitted
     */
    @GetMapping("")
    public List<RecordHistoryEntryDTO> reviewedSince(
        @RequestParam(name = "since", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        return overrideService.recordedSince(since == null ? Instant.now().minus(DEFAULT_WINDOW) : since);
    }
}
