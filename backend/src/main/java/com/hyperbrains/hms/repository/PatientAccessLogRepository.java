package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.PatientAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link PatientAccessLog} entity.
 *
 * <p>There is no way to change or remove a row through this repository beyond what {@code JpaRepository} offers by
 * default, and nothing in the application calls those methods: security history is written once and read afterwards.
 */
@Repository
public interface PatientAccessLogRepository extends JpaRepository<PatientAccessLog, Long> {
    Page<PatientAccessLog> findByPatientIdOrderByAccessedAtDesc(Long patientId, Pageable pageable);

    long countByPatientId(Long patientId);
}
