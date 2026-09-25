package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prescription entity.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    @Query("select prescription from Prescription prescription where prescription.doctor.login = ?#{authentication.name}")
    List<Prescription> findByDoctorIsCurrentUser();

    default Optional<Prescription> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Prescription> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Prescription> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select prescription from Prescription prescription left join fetch prescription.doctor",
        countQuery = "select count(prescription) from Prescription prescription"
    )
    Page<Prescription> findAllWithToOneRelationships(Pageable pageable);

    @Query("select prescription from Prescription prescription left join fetch prescription.doctor")
    List<Prescription> findAllWithToOneRelationships();

    @Query("select prescription from Prescription prescription left join fetch prescription.doctor where prescription.id =:id")
    Optional<Prescription> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Everything prescribed during a visit, oldest first.
     *
     * <p>Ordered by the prescribed-at timestamp, falling back to id so the order stays total when two
     * prescriptions share a millisecond.
     */
    @Query(
        "select p from Prescription p left join fetch p.doctor " +
        "where p.visit.id = :visitId order by p.createdAt, p.id"
    )
    List<Prescription> findByVisitId(@Param("visitId") Long visitId);

    /** Prescriptions on a visit in one of the given states, for driving the state machine. */
    List<Prescription> findByVisitIdAndStatusIn(Long visitId, Collection<PrescriptionStatus> statuses);

    /**
     * The pharmacy queue: prescriptions that are safe to hand over.
     *
     * <p>Patient and doctor are fetched because every queue row shows whose medicine this is and who
     * wrote it. Oldest first, so a patient who has been waiting longest is served first and the queue
     * can show how long that has been.
     */
    @Query(
        value = "select p from Prescription p left join fetch p.doctor left join fetch p.visit v left join fetch v.patient " +
            "where p.status in :statuses order by p.createdAt, p.id",
        countQuery = "select count(p) from Prescription p where p.status in :statuses"
    )
    Page<Prescription> findQueue(@Param("statuses") Collection<PrescriptionStatus> statuses, Pageable pageable);
}
