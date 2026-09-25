package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import java.io.Serializable;
import java.util.List;

/**
 * What happened when vitals were submitted.
 *
 * <p>Carries {@code warnings} on a <strong>successful</strong> response. That is the whole shape of
 * the two-tier rule: a reading that is possible but abnormal is saved and reported here, while a
 * reading that is impossible never gets this far because the request is refused with a 400.
 */
public class VitalsSubmissionResultDTO implements Serializable {

    private VitalSignsDTO vitalSigns;

    /** Where the visit moved to. Submitting vitals puts the patient in the doctor's queue. */
    private VisitStatus visitStatus;

    /** True when this replaced vitals already on file rather than recording them for the first time. */
    private boolean correction;

    private List<VitalsWarningDTO> warnings;

    public VitalSignsDTO getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(VitalSignsDTO vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public VisitStatus getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(VisitStatus visitStatus) {
        this.visitStatus = visitStatus;
    }

    public boolean isCorrection() {
        return correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    public List<VitalsWarningDTO> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<VitalsWarningDTO> warnings) {
        this.warnings = warnings;
    }
}
