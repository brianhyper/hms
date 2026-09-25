package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.VitalSigns;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VitalSigns entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {}
