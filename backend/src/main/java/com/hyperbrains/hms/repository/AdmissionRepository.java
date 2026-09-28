package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Admission entity.
 */
@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {
    /** Whether this visit has already produced an admission. There is a unique index on visit_id behind it. */
    boolean existsByVisitId(Long visitId);

    /** The stay opened for this visit, if there is one. Unique, so an Optional is the honest return type. */
    Optional<Admission> findByVisitId(Long visitId);

    /**
     * A stay with the bed it is in and that bed's ward already read.
     *
     * <p>"Where is this patient" is asked far more often than it looks: the ward a doctor is judged against
     * is {@code admission.bed.ward}, read live rather than cached, so that a transfer is correct the moment
     * it happens. Without the fetch that question needs an open Hibernate session, which is exactly the
     * coupling this repository method exists to avoid.
     */
    @Query("select admission from Admission admission join fetch admission.bed bed join fetch bed.ward where admission.id = :id")
    Optional<Admission> findOneWithBedAndWard(@Param("id") Long id);

    /**
     * How many other stays this patient has open.
     *
     * <p>The rule is "one open admission per patient", and it cannot be a database index: the patient is
     * reached through the visit — {@code Admission} deliberately stores no patient reference, so that the
     * identity merge has one fewer thing to carry — and a partial unique index cannot join to visit. So it
     * is checked here, in the service, and the database is not the thing enforcing it.
     *
     * <p>{@code statusNot} takes the discharged status rather than the open ones, so a third open status
     * added later is counted by default rather than being silently allowed.
     */
    long countByVisitPatientIdAndStatusNotAndIdNot(Long patientId, AdmissionStatus status, Long id);

    /**
     * Every stay still waiting for a bed, with everything the worklist shows already loaded.
     *
     * <p>Ordered by how long the patient has been waiting, because that is the order the ward works
     * through it, and {@code admittedAt} is the only timestamp that answers it.
     */
    @Query(
        """
        select admission from Admission admission
        join fetch admission.visit visit
        join fetch visit.patient
        join fetch admission.admittingDoctor
        join fetch admission.primaryDoctor
        where admission.status = :status
        order by admission.admittedAt asc
        """
    )
    List<Admission> findAwaitingBed(@Param("status") AdmissionStatus status);
    @Query("select admission from Admission admission where admission.admittingDoctor.login = ?#{authentication.name}")
    List<Admission> findByAdmittingDoctorIsCurrentUser();

    @Query("select admission from Admission admission where admission.primaryDoctor.login = ?#{authentication.name}")
    List<Admission> findByPrimaryDoctorIsCurrentUser();

    @Query("select admission from Admission admission where admission.dischargedByDoctor.login = ?#{authentication.name}")
    List<Admission> findByDischargedByDoctorIsCurrentUser();

    @Query("select admission from Admission admission where admission.dischargedByNurse.login = ?#{authentication.name}")
    List<Admission> findByDischargedByNurseIsCurrentUser();

    default Optional<Admission> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Admission> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Admission> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select admission from Admission admission left join fetch admission.admittingDoctor left join fetch admission.primaryDoctor left join fetch admission.dischargedByDoctor left join fetch admission.dischargedByNurse",
        countQuery = "select count(admission) from Admission admission"
    )
    Page<Admission> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select admission from Admission admission left join fetch admission.admittingDoctor left join fetch admission.primaryDoctor left join fetch admission.dischargedByDoctor left join fetch admission.dischargedByNurse"
    )
    List<Admission> findAllWithToOneRelationships();

    @Query(
        "select admission from Admission admission left join fetch admission.admittingDoctor left join fetch admission.primaryDoctor left join fetch admission.dischargedByDoctor left join fetch admission.dischargedByNurse where admission.id =:id"
    )
    Optional<Admission> findOneWithToOneRelationships(@Param("id") Long id);
}
