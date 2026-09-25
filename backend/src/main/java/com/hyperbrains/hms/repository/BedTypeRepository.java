package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.BedType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BedType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BedTypeRepository extends JpaRepository<BedType, Long> {}
