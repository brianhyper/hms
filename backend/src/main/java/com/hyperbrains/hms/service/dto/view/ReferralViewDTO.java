package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.ReferralType;
import java.time.Instant;

/**
 * A referral as the doctor who wrote it sees it.
 *
 * <p>Deliberately flat rather than nesting the whole {@code VisitDTO} or {@code PatientDTO}: the
 * referral letter is a summary of the encounter for another clinician, and the entity graph would
 * carry the patient's full clinical record to any caller who asked for the referral list.
 */
public record ReferralViewDTO(
    Long referralId,
    Long visitId,
    ReferralType type,
    String destination,
    String destinationEmail,
    Long departmentId,
    String departmentName,
    String reason,
    String notes,
    ReferralStatus status,
    Instant createdAt,
    String referredByLogin
) {
    public static ReferralViewDTO from(Referral referral) {
        return new ReferralViewDTO(
            referral.getId(),
            referral.getVisit() == null ? null : referral.getVisit().getId(),
            referral.getType(),
            referral.getDestination(),
            referral.getDestinationEmail(),
            referral.getDepartment() == null ? null : referral.getDepartment().getId(),
            referral.getDepartment() == null ? null : referral.getDepartment().getName(),
            referral.getReason(),
            referral.getNotes(),
            referral.getStatus(),
            referral.getCreatedAt(),
            referral.getReferredBy() == null ? null : referral.getReferredBy().getLogin()
        );
    }
}
