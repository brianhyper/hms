package com.hyperbrains.hms.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Covers the identifier format only. Issuing is a database sequence, so it is exercised by the
 * registration integration tests; the format is the part that is easy to get subtly wrong.
 */
class HospitalIdServiceTest {

    @Test
    void padsTheSequenceToTheConfiguredWidth() {
        assertThat(HospitalIdService.format("HMS", 2026, 7, 4)).isEqualTo("HMS-2026-0007");
    }

    @Test
    void doesNotTruncateValuesWiderThanThePadding() {
        assertThat(HospitalIdService.format("HMS", 2026, 123456, 4)).isEqualTo("HMS-2026-123456");
    }

    @Test
    void usesWhateverPrefixItIsGiven() {
        assertThat(HospitalIdService.format("UNK", 2026, 1, 4)).isEqualTo("UNK-2026-0001");
    }

    /** A width of 0 would build the format string "%00d", which the formatter rejects. */
    @Test
    void clampsAnUnusableWidthRatherThanThrowing() {
        assertThat(HospitalIdService.format("HMS", 2026, 3, 0)).isEqualTo("HMS-2026-3");
    }

    @Test
    void honoursAWiderConfiguredWidth() {
        assertThat(HospitalIdService.format("HMS", 2026, 7, 6)).isEqualTo("HMS-2026-000007");
    }
}
