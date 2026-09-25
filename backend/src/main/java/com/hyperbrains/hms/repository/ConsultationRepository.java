package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Consultation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Consultation entity.
 *
 * When extending this class, extend ConsultationRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface ConsultationRepository extends ConsultationRepositoryWithBagRelationships, JpaRepository<Consultation, Long> {
    @Query("select consultation from Consultation consultation where consultation.doctor.login = ?#{authentication.name}")
    List<Consultation> findByDoctorIsCurrentUser();

    default Optional<Consultation> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Consultation> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Consultation> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select consultation from Consultation consultation left join fetch consultation.doctor",
        countQuery = "select count(consultation) from Consultation consultation"
    )
    Page<Consultation> findAllWithToOneRelationships(Pageable pageable);

    @Query("select consultation from Consultation consultation left join fetch consultation.doctor")
    List<Consultation> findAllWithToOneRelationships();

    @Query("select consultation from Consultation consultation left join fetch consultation.doctor where consultation.id =:id")
    Optional<Consultation> findOneWithToOneRelationships(@Param("id") Long id);
}
