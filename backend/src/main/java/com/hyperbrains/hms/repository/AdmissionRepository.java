package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Admission;
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
