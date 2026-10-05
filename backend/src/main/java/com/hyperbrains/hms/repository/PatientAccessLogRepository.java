package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.PatientAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for the {@link PatientAccessLog} entity.
 *
 * <p>Nothing in the application writes or removes a row through this repository: an entry is created by opening a
 * chart or correcting one, and security history is written once and read afterwards. The one exception is
 * {@link #deleteByPatientId}, which test teardown needs because the log's foreign key deliberately blocks removing a
 * patient who has history.
 */
@Repository
public interface PatientAccessLogRepository extends JpaRepository<PatientAccessLog, Long> {
    Page<PatientAccessLog> findByPatientIdOrderByAccessedAtDesc(Long patientId, Pageable pageable);

    long countByPatientId(Long patientId);

    /** Teardown only: see the class comment. */
    @Transactional
    void deleteByPatientId(Long patientId);
}
