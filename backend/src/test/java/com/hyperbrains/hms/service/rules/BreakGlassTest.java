package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

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
}
