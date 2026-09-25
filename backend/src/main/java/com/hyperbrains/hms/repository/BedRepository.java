package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Bed;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Bed entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {}
