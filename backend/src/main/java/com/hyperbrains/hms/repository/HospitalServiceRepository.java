package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.HospitalService;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the HospitalService entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HospitalServiceRepository extends JpaRepository<HospitalService, Long> {
    /**
     * Look a catalogue entry up by its stable business code.
     *
     * <p>By code and not by name: the consultation fee is looked up on every completed
     * consultation, and matching on a display name means someone renaming the service silently
     * changes what patients are charged — or stops the charge working at all.
     */
    Optional<HospitalService> findOneByCode(String code);
}
