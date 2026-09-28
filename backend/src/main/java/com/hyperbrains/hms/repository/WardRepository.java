package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Ward;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Ward entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    /**
     * Every ward, in the order the screens list them.
     *
     * <p>Unpaged on purpose: a ward is a physical place, a hospital has tens of them rather than thousands,
     * and occupancy has to include the wards with no beds at all — which a page of beds could never produce.
     */
    List<Ward> findAllByOrderByNameAsc();
}
