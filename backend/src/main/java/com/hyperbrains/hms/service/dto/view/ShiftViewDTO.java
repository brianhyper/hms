package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.enumeration.ShiftType;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One shift on the roster, as a ward reads it.
 *
 * <p>This exists because of what it leaves out. A shift row names its person by staff record id, and the staff file
 * behind that id — identity number, contact details, employment history — is HR's and Super Admin's alone. A ward
 * needs to be told who is on duty, and the cheapest way to give it that is to widen the staff-records read until it
 * covers the roster too, which hands over the whole file to answer a question that needs a name.
 *
 * <p>So the person appears here as a name and a department, and nothing else of theirs is in this shape at all: no
 * staff record id, no identity number, no contact details. There is no field to leak, which is a stronger guarantee
 * than a mapping that is careful with one.
 */
public record ShiftViewDTO(
    Long id,
    LocalDate shiftDate,
    ShiftType shiftType,
    LocalTime startsAt,
    LocalTime endsAt,
    String staffName,
    String departmentName,
    Long wardId,
    String wardName,
    String note
) {
    /**
     * Read from a shift whose person, department and ward are already loaded — these are all touched here, and a
     * view that lazily loaded its own way out of a stream would be a view that failed on a large roster.
     */
    public static ShiftViewDTO from(Shift shift) {
        return new ShiftViewDTO(
            shift.getId(),
            shift.getShiftDate(),
            shift.getShiftType(),
            shift.getStartsAt(),
            shift.getEndsAt(),
            shift.getStaffRecord() == null ? null : shift.getStaffRecord().getFullName(),
            shift.getStaffRecord() == null || shift.getStaffRecord().getDepartment() == null
                ? null
                : shift.getStaffRecord().getDepartment().getName(),
            shift.getWard() == null ? null : shift.getWard().getId(),
            shift.getWard() == null ? null : shift.getWard().getName(),
            shift.getNote()
        );
    }
}
