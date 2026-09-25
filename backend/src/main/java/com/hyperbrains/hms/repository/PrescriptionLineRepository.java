package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.PrescriptionLine;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PrescriptionLine entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PrescriptionLineRepository extends JpaRepository<PrescriptionLine, Long> {
    /**
     * The lines of one prescription, oldest first.
     *
     * <p>The drug is joined in because every caller needs its name, unit and price, and an
     * un-joined access to a lazy {@code @ManyToOne} outside a transaction is a failure waiting to
     * happen.
     */
    @Query("select l from PrescriptionLine l join fetch l.drug where l.prescription.id = :prescriptionId order by l.id")
    List<PrescriptionLine> findWithDrugByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    /**
     * The lines of many prescriptions in one round trip.
     *
     * <p>A pharmacy queue of twenty prescriptions would otherwise issue twenty queries; the caller
     * groups the result by prescription id.
     */
    @Query("select l from PrescriptionLine l join fetch l.drug join fetch l.prescription where l.prescription.id in :prescriptionIds order by l.id")
    List<PrescriptionLine> findWithDrugByPrescriptionIdIn(@Param("prescriptionIds") Collection<Long> prescriptionIds);

    /** Everything a visit was ever prescribed, across all of its prescriptions. */
    @Query(
        "select l from PrescriptionLine l join fetch l.drug join fetch l.prescription p " +
        "where p.visit.id = :visitId order by l.id"
    )
    List<PrescriptionLine> findWithDrugByVisitId(@Param("visitId") Long visitId);

    @Query("select count(l) from PrescriptionLine l where l.prescription.id = :prescriptionId")
    long countByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
