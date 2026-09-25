package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.ConsultationAddendum;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ConsultationAddendum}.
 *
 * <p>Hand-written rather than generated: the entity is application-specific, not part of the JDL
 * model, so there is no DTO/mapper/resource scaffolding to match.
 */
@Repository
public interface ConsultationAddendumRepository extends JpaRepository<ConsultationAddendum, Long> {
    /** Oldest first, so the notes read in the order they were written. */
    List<ConsultationAddendum> findByConsultationIdOrderByCreatedAtAsc(Long consultationId);
}
