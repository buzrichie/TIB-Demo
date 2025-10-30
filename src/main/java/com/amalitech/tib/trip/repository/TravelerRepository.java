package com.amalitech.tib.trip.repository;

import com.amalitech.tib.trip.model.Traveler;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Traveler} entities. Provides standard CRUD operations
 * and custom query methods for accessing traveler data.
 */
@Repository
public interface TravelerRepository extends JpaRepository<Traveler, UUID> {

  Optional<Traveler> findByTripIdAndUserId(UUID tripId, UUID userId);

  Optional<Traveler> findByTripIdAndEmailIgnoreCase(UUID tripId, String email);
}
