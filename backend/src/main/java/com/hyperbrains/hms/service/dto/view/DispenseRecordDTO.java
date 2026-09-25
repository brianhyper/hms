package com.hyperbrains.hms.service.dto.view;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * One hand-over that happened, as the dispensing history shows it.
 *
 * <p>Separate from the generated {@code DispenseDTO} because that one carries the whole prescription
 * and the recording user's full account. What a counter or an auditor needs from a hand-over is when
 * it happened, who did it, and exactly which units left the shelf.
 */
public class DispenseRecordDTO implements Serializable {

    private Long dispenseId;

    private Instant dispensedAt;

    private String recordedByLogin;

    private String note;

    private List<DispensedItemDTO> items;

    /** One drug given out in this hand-over. */
    public record DispensedItemDTO(Long prescriptionLineId, String drugName, String unit, Integer quantity, String substitutionReason) {}

    public static DispenseRecordDTO of(
        Long dispenseId,
        Instant dispensedAt,
        String recordedByLogin,
        String note,
        List<DispensedItemDTO> items
    ) {
        DispenseRecordDTO dto = new DispenseRecordDTO();
        dto.dispenseId = dispenseId;
        dto.dispensedAt = dispensedAt;
        dto.recordedByLogin = recordedByLogin;
        dto.note = note;
        dto.items = items;
        return dto;
    }

    public Long getDispenseId() {
        return dispenseId;
    }

    public void setDispenseId(Long dispenseId) {
        this.dispenseId = dispenseId;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public String getRecordedByLogin() {
        return recordedByLogin;
    }

    public void setRecordedByLogin(String recordedByLogin) {
        this.recordedByLogin = recordedByLogin;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<DispensedItemDTO> getItems() {
        return items;
    }

    public void setItems(List<DispensedItemDTO> items) {
        this.items = items;
    }
}
