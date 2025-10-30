package com.amalitech.tib.trip.service;

import com.amalitech.tib.trip.dto.TripCreationDTO;
import com.amalitech.tib.trip.dto.TripResponseDTO;
import com.amalitech.tib.trip.dto.TripUpdateDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service interface for managing the core business logic related to trips. */
public interface TripService {

  /**
   * Creates a new trip.
   *
   * @param tripCreationDTO DTO containing the details for the new trip.
   * @param userId The ID of the user creating the trip.
   * @return A DTO representing the newly created trip.
   */
  TripResponseDTO createTrip(TripCreationDTO tripCreationDTO, UUID userId);

  /**
   * Retrieves a paginated list of trips for a specific user.
   *
   * @param userId The ID of the user whose trips are to be retrieved.
   * @param pageable Pagination information.
   * @return A page of trip response DTOs.
   */
  Page<TripResponseDTO> getAllTrips(UUID userId, Pageable pageable);

  /**
   * Retrieves a single trip by its unique ID.
   *
   * @param tripId The ID of the trip to retrieve.
   * @param userId The ID of the user requesting the trip.
   * @return A DTO representing the trip.
   */
  TripResponseDTO getTripById(UUID tripId, UUID userId);

  /**
   * Updates an existing trip with new information.
   *
   * @param tripId The ID of the trip to update.
   * @param tripUpdateDTO DTO containing the updated information.
   * @param userId The ID of the user updating the trip.
   * @return A DTO representing the updated trip.
   */
  TripResponseDTO updateTrip(UUID tripId, TripUpdateDTO tripUpdateDTO, UUID userId);

  /**
   * Deletes a trip by its ID.
   *
   * @param tripId The ID of the trip to delete.
   * @param userId The ID of the user deleting the trip.
   */
  void deleteTrip(UUID tripId, UUID userId);
}
