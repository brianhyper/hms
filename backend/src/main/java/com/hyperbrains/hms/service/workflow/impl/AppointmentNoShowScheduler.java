package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.service.workflow.AppointmentNoShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodic sweep for appointments nobody attended. Holds no logic — see
 * {@link AppointmentNoShowService} for the rule itself.
 *
 * <p>Excluded from the {@code test} profile. A {@code @Scheduled} method with no initial delay runs
 * as soon as the context starts, which during an integration test would mark other suites' fixture
 * appointments as no-shows and fail those tests for an unrelated reason.
 *
 * <p>The initial delay is tied to the scan interval so the first sweep happens one interval after
 * startup, not during it.
 */
@Component
@Profile("!test")
public class AppointmentNoShowScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentNoShowScheduler.class);

    private final AppointmentNoShowService appointmentNoShowService;

    public AppointmentNoShowScheduler(AppointmentNoShowService appointmentNoShowService) {
        this.appointmentNoShowService = appointmentNoShowService;
    }

    @Scheduled(
        fixedDelayString = "${hms.appointments.no-show-scan-interval:PT15M}",
        initialDelayString = "${hms.appointments.no-show-scan-interval:PT15M}"
    )
    public void sweep() {
        try {
            appointmentNoShowService.markMissedAppointmentsAsNoShow();
        } catch (RuntimeException ex) {
            // A failed sweep must not kill the scheduler thread and silently stop all future sweeps.
            LOG.error("No-show sweep failed", ex);
        }
    }
}
