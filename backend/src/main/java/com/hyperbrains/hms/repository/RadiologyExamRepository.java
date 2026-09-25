package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.RadiologyExam;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RadiologyExam entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RadiologyExamRepository extends JpaRepository<RadiologyExam, Long> {}
