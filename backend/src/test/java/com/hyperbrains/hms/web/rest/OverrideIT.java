package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.OverrideService;
import com.hyperbrains.hms.service.dto.view.OverrideRequestDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * The override / emergency-access mechanism, S3.5.
 *
 * <p>Driven through the service, because there is no route yet: the mechanism has no caller while the scope of its
 * first case is being confirmed. What is asserted here is the part that has to hold before anything is wired to it —
 * an override is its own audited event with a mandatory reason, attributed to the caller, and the whole trail of them
 * is one query.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = AuthoritiesConstants.PHARMACY)
class OverrideIT {

    @Autowired
    private OverrideService overrideService;

    @Test
    void recordsAnOverrideAsItsOwnAuditedEventWithAReasonAndTheActingRole() {
        String subject = "Prescription-" + UUID.randomUUID();

        overrideService.record(override(subject, "Medicine released before the bill was settled"));

        RecordHistoryEntryDTO entry = overridesFor(subject).getFirst();

        assertThat(entry.action()).isEqualTo(AuditActions.OVERRIDE_GRANTED);
        assertThat(entry.entityName()).isEqualTo("Prescription");
        assertThat(entry.entityId()).isEqualTo(subject);
        assertThat(entry.reason()).isEqualTo("Medicine released before the bill was settled");
        assertThat(entry.actorLogin()).isEqualTo("admin");
        assertThat(entry.details()).contains(AuthoritiesConstants.PHARMACY);
    }

    @Test
    void refusesAnOverrideThatDoesNotSayWhyAndRecordsNothing() {
        String subject = "Prescription-" + UUID.randomUUID();

        assertThatThrownBy(() -> overrideService.record(override(subject, "   ")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .satisfies(thrown -> assertThat(((BusinessRuleViolationException) thrown).getErrorKey()).isEqualTo("overrideReasonRequired"));

        assertThat(overridesFor(subject)).isEmpty();
    }

    /** The review is one query: every override, newest first. */
    @Test
    void listsEveryOverrideNewestFirstForTheRetrospectiveReview() {
        String first = "Prescription-" + UUID.randomUUID();
        String second = "Prescription-" + UUID.randomUUID();

        overrideService.record(override(first, "first"));
        overrideService.record(override(second, "second"));

        List<RecordHistoryEntryDTO> overrides = overrideService.recordedSince(Instant.EPOCH);

        assertThat(overrides).allSatisfy(entry -> assertThat(entry.action()).isEqualTo(AuditActions.OVERRIDE_GRANTED));
        assertThat(indexOf(overrides, second)).isLessThan(indexOf(overrides, first));
    }

    private List<RecordHistoryEntryDTO> overridesFor(String entityId) {
        return overrideService.recordedSince(Instant.EPOCH).stream().filter(entry -> entityId.equals(entry.entityId())).toList();
    }

    private static int indexOf(List<RecordHistoryEntryDTO> entries, String entityId) {
        return IntStream.range(0, entries.size()).filter(i -> entityId.equals(entries.get(i).entityId())).findFirst().orElseThrow();
    }

    private static OverrideRequestDTO override(String entityId, String reason) {
        OverrideRequestDTO request = new OverrideRequestDTO();
        request.setOverriddenEntity("Prescription");
        request.setOverriddenEntityId(entityId);
        request.setReason(reason);
        return request;
    }
}
