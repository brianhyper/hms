package com.hyperbrains.hms.service.workflow;

/**
 * Retirement of appointments nobody attended.
 *
 * <p>Split from the scheduler that calls it so the rule can be tested directly. A {@code @Scheduled}
 * method is awkward to test and, worse, fires during integration tests and would mark the fixtures
 * of unrelated tests as no-shows.
 */
public interface AppointmentNoShowService {

    /**
     * Mark every scheduled appointment whose slot plus the grace period has passed as a no-show.
     *
     * @return how many appointments were marked.
     */
    int markMissedAppointmentsAsNoShow();
}
