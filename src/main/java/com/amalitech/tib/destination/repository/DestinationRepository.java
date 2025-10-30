package com.amalitech.tib.destination.repository;

import com.amalitech.tib.destination.model.Destination;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Destination entities. Supports pagination and sorting. - Supports dynamic
 * filtering through Specifications.
 */
@Repository
public interface DestinationRepository
    extends JpaRepository<Destination, UUID>, JpaSpecificationExecutor<Destination> {}
