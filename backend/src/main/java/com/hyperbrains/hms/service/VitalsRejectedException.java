package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.rules.VitalsValidator;
import java.io.Serial;
import java.util.List;

/**
 * Raised when a vital sign is outside the range a living person can produce.
 *
 * <p>Deliberately a different type from {@link BusinessRuleViolationException}, because it means
 * something different: the payload is wrong, not the record's state. That is why it maps to
 * <strong>400</strong> with per-field errors the client can highlight, rather than a 409 — and it
 * is why it must not be folded into the same "something was rejected" path as a warning. A warning
 * still saves the reading; this refuses it.
 */
public class VitalsRejectedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient List<VitalsValidator.Rejection> rejections;

    public VitalsRejectedException(List<VitalsValidator.Rejection> rejections) {
        super("Vital signs outside the physiologically possible range were rejected: " + rejections.size() + " field(s)");
        this.rejections = List.copyOf(rejections);
    }

    public List<VitalsValidator.Rejection> getRejections() {
        return rejections;
    }
}
