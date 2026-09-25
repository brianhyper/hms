package com.hyperbrains.hms.service.dto.view;

import java.io.Serializable;
import java.util.List;

/**
 * The result of the pre-save duplicate check, returned before anything is written.
 *
 * <p>Lets Reception see both checks at once and decide: use the existing record, correct the
 * details, or consciously create a second record.
 */
public class DuplicateCheckResultDTO implements Serializable {

    /**
     * An existing patient with the same identity document. Non-null means a save will be refused
     * unless {@code overrideReason} is supplied.
     */
    private PatientSummaryDTO exactMatch;

    /** Advisory only. Never a reason to refuse a save. */
    private List<PossibleDuplicateDTO> possibleDuplicates;

    /** Mirrors {@code exactMatch != null}, so the client does not have to infer the rule. */
    private boolean overrideRequired;

    public PatientSummaryDTO getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(PatientSummaryDTO exactMatch) {
        this.exactMatch = exactMatch;
    }

    public List<PossibleDuplicateDTO> getPossibleDuplicates() {
        return possibleDuplicates;
    }

    public void setPossibleDuplicates(List<PossibleDuplicateDTO> possibleDuplicates) {
        this.possibleDuplicates = possibleDuplicates;
    }

    public boolean isOverrideRequired() {
        return overrideRequired;
    }

    public void setOverrideRequired(boolean overrideRequired) {
        this.overrideRequired = overrideRequired;
    }
}
