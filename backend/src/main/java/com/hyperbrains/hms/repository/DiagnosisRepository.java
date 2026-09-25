package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Diagnosis;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Diagnosis entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {}
