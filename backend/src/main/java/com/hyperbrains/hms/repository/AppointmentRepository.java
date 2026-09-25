package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Appointment entity.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("select appointment from Appointment appointment where appointment.doctor.login = ?#{authentication.name}")
    List<Appointment> findByDoctorIsCurrentUser();

    /**
     * Appointments that were scheduled, whose slot plus the grace period has now passed, and which
     * were never checked in.
     *
     * <p>Returned as entities rather than bulk-updated, because each one needs an audit entry
     * recording that the system — not a person — marked it as a no-show. A bulk update would
     * change the rows with no trace of why.
     *
     * <p>The caller supplies the cutoff already adjusted for the grace period and the hospital's
     * zone, because the scheduled date and time are stored as local wall-clock values with no
     * offset and cannot be compared to an instant without knowing the zone.
     */
    @Query(
        """
        select a from Appointment a
        where a.status = com.hyperbrains.hms.domain.enumeration.AppointmentStatus.SCHEDULED
          and (
            a.scheduledDate < :cutoffDate
            or (a.scheduledDate = :cutoffDate and a.scheduledTime < :cutoffTime)
          )
        """
    )
    List<Appointment> findMissed(@Param("cutoffDate") LocalDate cutoffDate, @Param("cutoffTime") LocalTime cutoffTime);

    default Optional<Appointment> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Appointment> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Appointment> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select appointment from Appointment appointment left join fetch appointment.doctor",
        countQuery = "select count(appointment) from Appointment appointment"
    )
    Page<Appointment> findAllWithToOneRelationships(Pageable pageable);

    @Query("select appointment from Appointment appointment left join fetch appointment.doctor")
    List<Appointment> findAllWithToOneRelationships();

    @Query("select appointment from Appointment appointment left join fetch appointment.doctor where appointment.id =:id")
    Optional<Appointment> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Every appointment booked against a patient.
     *
     * <p>Needed when two records turn out to be the same person: an appointment names a patient directly,
     * unlike everything clinical, which hangs off the visit.
     */
    List<Appointment> findByPatientId(Long patientId);
}
