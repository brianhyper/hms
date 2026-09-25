package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Referral;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Referral entity.
 */
@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    @Query("select referral from Referral referral where referral.referredBy.login = ?#{authentication.name}")
    List<Referral> findByReferredByIsCurrentUser();

    default Optional<Referral> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Referral> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Referral> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select referral from Referral referral left join fetch referral.referredBy",
        countQuery = "select count(referral) from Referral referral"
    )
    Page<Referral> findAllWithToOneRelationships(Pageable pageable);

    @Query("select referral from Referral referral left join fetch referral.referredBy")
    List<Referral> findAllWithToOneRelationships();

    @Query("select referral from Referral referral left join fetch referral.referredBy where referral.id =:id")
    Optional<Referral> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Everything this visit was referred on to, oldest first.
     *
     * <p>Ordered by the new created-at fallback to id so the order is always total.
     */
    @Query(
        "select r from Referral r left join fetch r.referredBy left join fetch r.department " +
        "where r.visit.id = :visitId order by r.createdAt, r.id"
    )
    List<Referral> findByVisitIdOrderByCreatedAt(@Param("visitId") Long visitId);

    /**
     * Whether a visit has been referred out at all.
     *
     * <p>Used by the visit-status derivation: a referral ends the local journey, so nothing that
     * happens afterwards — a late result, a cancelled order — may pull the visit backwards.
     */
    boolean existsByVisitId(Long visitId);
}
