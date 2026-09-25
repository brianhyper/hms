package com.hyperbrains.hms.service;

import com.hyperbrains.hms.config.HmsProperties;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Issues hospital identifiers.
 *
 * <p>The format and prefix are configuration, not constants — see {@code hms.ids} in
 * {@code config/application.yml} and {@link HmsProperties.Ids}.
 *
 * <p>Uniqueness is guaranteed by a database sequence rather than by counting existing rows. A
 * {@code select max(...) + 1} approach would hand the same identifier to two concurrent
 * registrations, and {@code hospital_id} is a unique column — so the second save would fail.
 */
@Service
public class HospitalIdService {

    /**
     * Kept as a single literal, deliberately with no string concatenation: the sequence name is
     * not user input, but building SQL by concatenation is a habit worth not having.
     */
    private static final String NEXT_VALUE_SQL = "select nextval('hms_hospital_id_seq')";

    private final JdbcTemplate jdbcTemplate;

    private final HmsProperties properties;

    public HospitalIdService(JdbcTemplate jdbcTemplate, HmsProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    /** Permanent identifier for a normally-registered patient, e.g. {@code HMS-2026-0007}. */
    public String nextPermanentId() {
        return next(properties.getIds().getPermanentPrefix());
    }

    /**
     * Temporary identifier for an unidentified or unconscious emergency patient, e.g.
     * {@code UNK-2026-0007}. It is replaced only indirectly, by merging the record into a
     * confirmed one.
     */
    public String nextTemporaryId() {
        return next(properties.getIds().getTemporaryPrefix());
    }

    private String next(String prefix) {
        Long sequence = jdbcTemplate.queryForObject(NEXT_VALUE_SQL, Long.class);
        return format(prefix, LocalDate.now(ZoneOffset.UTC).getYear(), sequence == null ? 0L : sequence, properties.getIds().getDigits());
    }

    /**
     * Pure formatting, exposed for testing. A width below 1 would produce a format string the
     * formatter rejects, so it is clamped rather than trusted.
     */
    static String format(String prefix, int year, long sequence, int digits) {
        int width = Math.max(1, digits);
        String padding = "%0" + width + "d";
        return prefix + "-" + year + "-" + padding.formatted(sequence);
    }
}
