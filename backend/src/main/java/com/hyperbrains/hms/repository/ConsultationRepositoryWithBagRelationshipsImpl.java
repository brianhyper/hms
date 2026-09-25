package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Consultation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class ConsultationRepositoryWithBagRelationshipsImpl implements ConsultationRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String CONSULTATIONS_PARAMETER = "consultations";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Consultation> fetchBagRelationships(Optional<Consultation> consultation) {
        return consultation.map(this::fetchDiagnoseses);
    }

    @Override
    public Page<Consultation> fetchBagRelationships(Page<Consultation> consultations) {
        return new PageImpl<>(
            fetchBagRelationships(consultations.getContent()),
            consultations.getPageable(),
            consultations.getTotalElements()
        );
    }

    @Override
    public List<Consultation> fetchBagRelationships(List<Consultation> consultations) {
        return Optional.of(consultations).map(this::fetchDiagnoseses).orElse(List.of());
    }

    Consultation fetchDiagnoseses(Consultation result) {
        return entityManager
            .createQuery(
                "select consultation from Consultation consultation left join fetch consultation.diagnoseses where consultation.id = :id",
                Consultation.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Consultation> fetchDiagnoseses(List<Consultation> consultations) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, consultations.size()).forEach(index -> order.put(consultations.get(index).getId(), index));
        List<Consultation> result = entityManager
            .createQuery(
                "select consultation from Consultation consultation left join fetch consultation.diagnoseses where consultation in :consultations",
                Consultation.class
            )
            .setParameter(CONSULTATIONS_PARAMETER, consultations)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
