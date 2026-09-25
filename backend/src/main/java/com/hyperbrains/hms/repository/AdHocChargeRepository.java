package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.AdHocCharge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AdHocCharge entity.
 */
@Repository
public interface AdHocChargeRepository extends JpaRepository<AdHocCharge, Long> {
    @Query("select adHocCharge from AdHocCharge adHocCharge where adHocCharge.addedBy.login = ?#{authentication.name}")
    List<AdHocCharge> findByAddedByIsCurrentUser();

    @Query("select adHocCharge from AdHocCharge adHocCharge where adHocCharge.voidedBy.login = ?#{authentication.name}")
    List<AdHocCharge> findByVoidedByIsCurrentUser();

    default Optional<AdHocCharge> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AdHocCharge> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AdHocCharge> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select adHocCharge from AdHocCharge adHocCharge left join fetch adHocCharge.addedBy left join fetch adHocCharge.voidedBy",
        countQuery = "select count(adHocCharge) from AdHocCharge adHocCharge"
    )
    Page<AdHocCharge> findAllWithToOneRelationships(Pageable pageable);

    @Query("select adHocCharge from AdHocCharge adHocCharge left join fetch adHocCharge.addedBy left join fetch adHocCharge.voidedBy")
    List<AdHocCharge> findAllWithToOneRelationships();

    @Query(
        "select adHocCharge from AdHocCharge adHocCharge left join fetch adHocCharge.addedBy left join fetch adHocCharge.voidedBy where adHocCharge.id =:id"
    )
    Optional<AdHocCharge> findOneWithToOneRelationships(@Param("id") Long id);
}
