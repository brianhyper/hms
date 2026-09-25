package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Patient entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findOneByHospitalId(String hospitalId);

    /**
     * Exact identity-document match, using the same normalisation as the functional index
     * {@code ix_patient__identity_document_normalized}.
     *
     * <p>Normalising in SQL rather than in Java is what keeps this a small lookup instead of a full
     * scan: the stored value and the typed value may differ only in spacing, case or punctuation
     * ({@code 12345678} vs {@code 123-456-78}), and a plain equality test would miss the match
     * exactly when it matters most. The {@code MERGED} exclusion matters too — a merged record has
     * been superseded, so pointing Reception at it would send them to a record the system no
     * longer treats as real.
     *
     * <p>Native because {@code regexp_replace} has no JPQL equivalent. Every profile, including
     * test, runs Postgres.
     */
    @Query(
        value = """
            select * from patient
            where identity_document_type = :type
              and identity_document_number is not null
              and upper(regexp_replace(identity_document_number, '[^A-Za-z0-9]', '', 'g')) = :normalizedNumber
              and registration_status <> 'MERGED'
            """,
        nativeQuery = true
    )
    List<Patient> findByIdentityDocumentNormalized(@Param("type") String type, @Param("normalizedNumber") String normalizedNumber);

    /**
     * Bounded pre-filter for the advisory duplicate check.
     *
     * <p>This is deliberately generous and deliberately not the decision — {@code
     * PatientDuplicateMatcher} scores the results in Java. {@code fullName} is matched with a
     * leading wildcard so a reordered name ({@code "Kamau John"} vs {@code "John Kamau"}) still
     * surfaces; that costs the {@code ix_patient__full_name} index, which is the right trade for
     * recall on a table that starts small.
     *
     * <p>The {@code has*} flags exist because the obvious alternative does not work on Postgres.
     * Writing {@code :dateOfBirth is not null} or {@code :phone <> ''} on a bind parameter leaves
     * the database unable to infer the parameter's type, and the whole query fails at runtime with
     * "could not determine data type of parameter". Comparing against a typed column or a literal
     * gives Postgres something to infer from, so each optional clause is guarded by a boolean that
     * the service sets, rather than by the parameter being null.
     */
    @Query(
        """
        select p from Patient p
        where p.registrationStatus <> :merged
          and (
            (:hasName = true and lower(p.fullName) like lower(concat('%', :nameToken, '%')))
            or (:hasPhone = true and p.phone = :phone)
            or (:hasDateOfBirth = true and p.dateOfBirth = :dateOfBirth)
          )
        """
    )
    List<Patient> findDuplicateCandidates(
        @Param("hasName") boolean hasName,
        @Param("nameToken") String nameToken,
        @Param("hasPhone") boolean hasPhone,
        @Param("phone") String phone,
        @Param("hasDateOfBirth") boolean hasDateOfBirth,
        @Param("dateOfBirth") LocalDate dateOfBirth,
        @Param("merged") RegistrationStatus merged
    );
}
