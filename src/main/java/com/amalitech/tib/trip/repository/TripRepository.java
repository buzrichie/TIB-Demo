package com.amalitech.tib.trip.repository;

import com.amalitech.tib.trip.model.Trip;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Trip} entities. Provides standard CRUD operations and
 * custom query methods for accessing trip data.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

  Page<Trip> findByUser_Id(UUID userId, Pageable pageable);

  Optional<Trip> findByInviteToken(String inviteToken);
}
