package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.enumeration.AppointmentStatus;
import com.hyperbrains.hms.repository.AppointmentRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.workflow.AppointmentNoShowService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentNoShowServiceImpl implements AppointmentNoShowService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentNoShowServiceImpl.class);

    private final AppointmentRepository appointmentRepository;

    private final AuditLogService auditLogService;

    private final HmsProperties properties;

    public AppointmentNoShowServiceImpl(
        AppointmentRepository appointmentRepository,
        AuditLogService auditLogService,
        HmsProperties properties
    ) {
        this.appointmentRepository = appointmentRepository;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public int markMissedAppointmentsAsNoShow() {
        ZoneId zone = ZoneId.of(properties.getAppointments().getZone());
        // Scheduled date and time are stored as local wall-clock values, so the cutoff has to be
        // computed in the hospital's own zone. Using UTC here would retire an 09:00 appointment at
        // 06:00 local time.
        LocalDateTime cutoff = LocalDateTime.now(zone).minusMinutes(properties.getAppointments().getNoShowGraceMinutes());

        List<Appointment> missed = appointmentRepository.findMissed(cutoff.toLocalDate(), cutoff.toLocalTime());
        for (Appointment appointment : missed) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepository.save(appointment);
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.APPOINTMENT_NO_SHOW, "Appointment", appointment.getId()).withReason(
                    "Scheduled for " + appointment.getScheduledDate() + " " + appointment.getScheduledTime() + " (" + zone + ")"
                )
            );
        }

        if (!missed.isEmpty()) {
            LOG.info("Marked {} missed appointment(s) as no-show", missed.size());
        }
        return missed.size();
    }
}
