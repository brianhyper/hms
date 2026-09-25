package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Consultation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ConsultationRepositoryWithBagRelationships {
    Optional<Consultation> fetchBagRelationships(Optional<Consultation> consultation);

    List<Consultation> fetchBagRelationships(List<Consultation> consultations);

    Page<Consultation> fetchBagRelationships(Page<Consultation> consultations);
}
