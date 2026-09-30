package com.hyperbrains.hms.domain.enumeration;

/**
 * Where a member of staff stands.
 *
 * <p>There is no "deleted" value and no delete route: a staff record is a person's employment history, and it is
 * the parent of the rostering, leave and payroll rows that Phase 4 adds. Somebody who has left is
 * {@code TERMINATED}, which keeps the record and its history intact while saying plainly that they are no longer
 * here — the same choice the account side makes, where accounts are deactivated rather than removed.
 */
public enum StaffRecordStatus {
    /** On the payroll and expected at work. */
    ACTIVE,

    /** Still employed, and away for an approved reason. */
    ON_LEAVE,

    /** No longer employed here. The end state of a record, not its removal. */
    TERMINATED,
}
