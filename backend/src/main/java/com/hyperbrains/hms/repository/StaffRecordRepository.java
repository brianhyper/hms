package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.StaffRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link StaffRecord} entity.
 */
@Repository
public interface StaffRecordRepository extends JpaRepository<StaffRecord, Long> {
    /**
     * The staff record holding this identity number, if any.
     *
     * <p>Asked before accepting a new record, so the refusal is a stated rule with a message rather than a unique
     * index violation the caller has to interpret.
     */
    Optional<StaffRecord> findOneByNationalId(String nationalId);

    /**
     * The staff record attached to this login, if any.
     *
     * <p>There is one record per person, so an account already claimed by one cannot be claimed by another.
     */
    Optional<StaffRecord> findOneByUserId(Long userId);
}
