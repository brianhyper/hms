package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Result;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Result entity.
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    @Query("select result from Result result where result.enteredBy.login = ?#{authentication.name}")
    List<Result> findByEnteredByIsCurrentUser();

    default Optional<Result> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Result> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Result> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select result from Result result left join fetch result.enteredBy",
        countQuery = "select count(result) from Result result"
    )
    Page<Result> findAllWithToOneRelationships(Pageable pageable);

    @Query("select result from Result result left join fetch result.enteredBy")
    List<Result> findAllWithToOneRelationships();

    @Query("select result from Result result left join fetch result.enteredBy where result.id =:id")
    Optional<Result> findOneWithToOneRelationships(@Param("id") Long id);
}
