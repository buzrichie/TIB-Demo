package com.amalitech.tib.shared;

import com.amalitech.tib.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository providing common functionality for all repositories.
 *
 * @param <T>  Entity type
 * @param <ID> Entity primary key type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {


    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    Page<T> findAllActive(Pageable pageable);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<T> findByIdActive(UUID id);

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    long countActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt >= :date AND e.deletedAt IS NULL")
    List<T> findCreatedAfter(LocalDateTime date);

    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt >= :date AND e.deletedAt IS NULL")
    List<T> findUpdatedAfter(LocalDateTime date);

    default T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Entity not found with ID: " + id
                )
        );
    }


    default void assertExists(ID id) {
        if (!existsById(id)) {
            throw new ResourceNotFoundException(
                    "Entity not found with ID: " + id
            );
        }
    }

    default Optional<T> safeFindById(ID id) {
        return findById(id);
    }


}
