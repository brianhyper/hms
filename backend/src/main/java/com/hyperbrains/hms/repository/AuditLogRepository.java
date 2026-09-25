package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.AuditLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("select auditLog from AuditLog auditLog where auditLog.actor.login = ?#{authentication.name}")
    List<AuditLog> findByActorIsCurrentUser();

    default Optional<AuditLog> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AuditLog> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AuditLog> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select auditLog from AuditLog auditLog left join fetch auditLog.actor",
        countQuery = "select count(auditLog) from AuditLog auditLog"
    )
    Page<AuditLog> findAllWithToOneRelationships(Pageable pageable);

    @Query("select auditLog from AuditLog auditLog left join fetch auditLog.actor")
    List<AuditLog> findAllWithToOneRelationships();

    @Query("select auditLog from AuditLog auditLog left join fetch auditLog.actor where auditLog.id =:id")
    Optional<AuditLog> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * The audit trail for one record, oldest first, with the actor joined in.
     *
     * <p>This is what {@code ix_audit_log__entity} exists for: answering "what happened to this
     * patient / prescription / bill" without scanning the whole table. The actor is fetched eagerly
     * because naming who did it is the point of reading a trail.
     */
    @Query(
        "select a from AuditLog a left join fetch a.actor " +
        "where a.entityName = :entityName and a.entityId = :entityId order by a.id"
    )
    List<AuditLog> findByEntityNameAndEntityIdOrderByIdAsc(
        @Param("entityName") String entityName,
        @Param("entityId") String entityId
    );
}
