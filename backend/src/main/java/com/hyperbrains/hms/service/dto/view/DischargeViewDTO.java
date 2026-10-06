package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.service.PersonNames;
import java.time.Instant;
import java.util.List;

/**
 * A discharge in progress: what has been signed, by whom, and what is still running.
 *
 * <p>It carries the loose ends rather than only the signatures, because the answer to "can this patient go" is
 * partly about orders that are still running — and a caller that has to ask a second endpoint to find that out will
 * one day not ask.
 */
public record DischargeViewDTO(
    Long admissionId,
    AdmissionStatus status,
    Instant dischargedAt,
    String dischargeNote,
    String signedOffByDoctor,
    String signedOffByNurse,
    boolean complete,
    Long bedId,
    String bedNumber,
    BedStatus bedStatus,
    Long visitId,
    VisitStatus visitStatus,
    List<Long> outstandingOrderIds
) {
    public static DischargeViewDTO from(Admission admission, List<Long> outstandingOrderIds) {
        Bed bed = admission.getBed();
        Visit visit = admission.getVisit();
        return new DischargeViewDTO(
            admission.getId(),
            admission.getStatus(),
            admission.getDischargedAt(),
            admission.getDischargeNote(),
            PersonNames.displayName(admission.getDischargedByDoctor()),
            PersonNames.displayName(admission.getDischargedByNurse()),
            admission.getDischargedByDoctor() != null && admission.getDischargedByNurse() != null,
            bed == null ? null : bed.getId(),
            bed == null ? null : bed.getBedNumber(),
            bed == null ? null : bed.getStatus(),
            visit == null ? null : visit.getId(),
            visit == null ? null : visit.getStatus(),
            List.copyOf(outstandingOrderIds)
        );
    }
}
