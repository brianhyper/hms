package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.DispenseLine;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DispenseLine entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DispenseLineRepository extends JpaRepository<DispenseLine, Long> {
    /**
     * How much of one prescribed line has already been handed over.
     *
     * <p>Summed in the database: the remaining quantity decides whether a dispense is allowed and
     * where the prescription sits in its state machine, so it must not be derived from a partly
     * loaded collection.
     */
    @Query("select coalesce(sum(dl.quantity), 0) from DispenseLine dl where dl.prescriptionLine.id = :prescriptionLineId")
    long sumQuantityByPrescriptionLineId(@Param("prescriptionLineId") Long prescriptionLineId);

    /**
     * Every hand-over recorded for a prescription, with the drug and the prescribed line joined in.
     *
     * <p>This is the dispensing history: a prescription can be filled over several visits to the
     * counter, and the history is the only record of what actually left the shelf.
     */
    @Query(
        "select dl from DispenseLine dl " +
        "join fetch dl.drug join fetch dl.prescriptionLine pl join fetch dl.dispense d " +
        "where pl.prescription.id = :prescriptionId order by d.dispensedAt, dl.id"
    )
    List<DispenseLine> findWithDrugByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
