package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.service.dto.PatientDTO;
import java.io.Serializable;
import java.util.List;

/**
 * The created patient, plus any duplicates found on the way in.
 *
 * <p>Possible duplicates are returned even though the record was saved. The soft check is
 * explicitly not allowed to block a save, so surfacing them here is what gives Reception the
 * chance to notice and undo a mistake — rejecting the request would strand the patient.
 */
public class PatientRegistrationResultDTO implements Serializable {

    private PatientDTO patient;

    private List<PossibleDuplicateDTO> possibleDuplicates;

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    public List<PossibleDuplicateDTO> getPossibleDuplicates() {
        return possibleDuplicates;
    }

    public void setPossibleDuplicates(List<PossibleDuplicateDTO> possibleDuplicates) {
        this.possibleDuplicates = possibleDuplicates;
    }
}
