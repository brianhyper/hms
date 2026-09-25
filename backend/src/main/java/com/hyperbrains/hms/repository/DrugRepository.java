package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Drug;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Drug entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {}
