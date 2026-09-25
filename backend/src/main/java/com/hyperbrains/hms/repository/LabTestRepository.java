package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.LabTest;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the LabTest entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {}
