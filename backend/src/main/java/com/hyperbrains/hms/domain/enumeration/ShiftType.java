package com.hyperbrains.hms.domain.enumeration;

/**
 * The ShiftType enumeration.
 *
 * <p>Stored as a string rather than as an ordinal, so a value added later is a one-line change with no migration and
 * no renumbering of the rows already written. That matters here: the client has not fixed the final set of shift
 * types (see `phase4.md`, "Questions for the client"), and the two below are the ones the phase document names.
 */
public enum ShiftType {
    DAY,
    NIGHT,
}
