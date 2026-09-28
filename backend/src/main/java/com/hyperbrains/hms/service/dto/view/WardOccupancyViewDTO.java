package com.hyperbrains.hms.service.dto.view;

/**
 * How full a ward is.
 *
 * <p>Capacity is counted from the beds on every request rather than stored on the ward. A stored
 * figure is a second copy of the truth that goes wrong the first time a bed is added, moved or
 * deleted without somebody remembering to update it.
 *
 * <p>Wards with no beds at all are included, reporting zero: a ward that has just been created is a
 * ward that needs beds, and omitting it would hide exactly the rows somebody has to act on.
 *
 * <p>{@code active} is reported alongside the counts rather than filtering them out, because a ward
 * being closed is a planning fact the screen has to show — but its beds are not offered for
 * assignment, which the availability query enforces.
 */
public record WardOccupancyViewDTO(
    Long wardId,
    String wardName,
    String departmentName,
    boolean active,
    long totalBeds,
    long availableBeds,
    long occupiedBeds,
    long cleaningBeds,
    long maintenanceBeds
) {}
