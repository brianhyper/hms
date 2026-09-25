package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.AdmissionTransfer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AdmissionTransfer entity.
 */
@Repository
public interface AdmissionTransferRepository extends JpaRepository<AdmissionTransfer, Long> {
    @Query(
        "select admissionTransfer from AdmissionTransfer admissionTransfer where admissionTransfer.transferredBy.login = ?#{authentication.name}"
    )
    List<AdmissionTransfer> findByTransferredByIsCurrentUser();

    default Optional<AdmissionTransfer> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AdmissionTransfer> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AdmissionTransfer> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select admissionTransfer from AdmissionTransfer admissionTransfer left join fetch admissionTransfer.transferredBy",
        countQuery = "select count(admissionTransfer) from AdmissionTransfer admissionTransfer"
    )
    Page<AdmissionTransfer> findAllWithToOneRelationships(Pageable pageable);

    @Query("select admissionTransfer from AdmissionTransfer admissionTransfer left join fetch admissionTransfer.transferredBy")
    List<AdmissionTransfer> findAllWithToOneRelationships();

    @Query(
        "select admissionTransfer from AdmissionTransfer admissionTransfer left join fetch admissionTransfer.transferredBy where admissionTransfer.id =:id"
    )
    Optional<AdmissionTransfer> findOneWithToOneRelationships(@Param("id") Long id);
}
