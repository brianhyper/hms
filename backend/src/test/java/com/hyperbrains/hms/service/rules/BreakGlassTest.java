package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Who may break glass. Pure policy, so it is tested without a database.
 */
class BreakGlassTest {

    @Test
    void thePharmacistAndTheDoctorAtThePointOfCareMayInvokeIt() {
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.PHARMACY))).isTrue();
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.DOCTOR))).isTrue();
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.USER, AuthoritiesConstants.DOCTOR))).isTrue();
    }

    /** Not an administrator, and not somebody who has to be found first. */
    @Test
    void administrationAndTheDeskMayNotInvokeIt() {
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.ADMIN))).isFalse();
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.SUPER_ADMIN))).isFalse();
        assertThat(BreakGlass.mayBeInvokedBy(List.of(AuthoritiesConstants.RECEPTION, AuthoritiesConstants.NURSE))).isFalse();
    }

    @Test
    void nobodySignedInMayNotInvokeIt() {
        assertThat(BreakGlass.mayBeInvokedBy(List.of())).isFalse();
    }

    /** The confirmed scope: an emergency-triaged visit, or an admitted patient, and nothing else. */
    @Test
    void onlyEmergencyAndAdmittedVisitsAreInScope() {
        assertThat(BreakGlass.isInScope(VisitPriority.EMERGENCY, VisitType.OUTPATIENT)).isTrue();
        assertThat(BreakGlass.isInScope(VisitPriority.NORMAL, VisitType.EMERGENCY)).isTrue();
        assertThat(BreakGlass.isInScope(VisitPriority.NORMAL, VisitType.ADMISSION)).isTrue();
        assertThat(BreakGlass.isInScope(VisitPriority.NORMAL, VisitType.OUTPATIENT)).isFalse();
        assertThat(BreakGlass.isInScope(VisitPriority.URGENT, VisitType.PHARMACY_ONLY)).isFalse();
    }
}
