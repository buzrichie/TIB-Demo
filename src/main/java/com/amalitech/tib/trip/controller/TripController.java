package com.amalitech.tib.trip.controller;

import com.amalitech.tib.trip.dto.TripCreationDTO;
import com.amalitech.tib.trip.dto.TripResponseDTO;
import com.amalitech.tib.trip.dto.TripUpdateDTO;
import com.amalitech.tib.trip.service.TripService;
import com.amalitech.tib.util.ApiResponse;
import com.amalitech.tib.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing travel trips. Provides endpoints for creating, retrieving, updating,
 * and deleting trips.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Endpoints for managing travel trips.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TripController {
  private final TripService tripService;

  /**
   * Creates a new trip.
   *
   * @param tripCreationDTO The DTO containing the details for the new trip.
   * @return A {@link ResponseEntity} with the created trip's details.
   */
  @Operation(
      summary = "Create a new trip",
      description = "Create a new trip and optionally invite collaborators")
  @PostMapping
  public ResponseEntity<ApiResponse<TripResponseDTO>> createTrip(
      @Valid @RequestBody TripCreationDTO tripCreationDTO) {
    UUID userId = SecurityUtils.getCurrentUserId();
    log.info("User {} creating a new trip: {}", userId, tripCreationDTO.title());

    TripResponseDTO createdTrip = tripService.createTrip(tripCreationDTO, userId);
    ApiResponse<TripResponseDTO> response =
        ApiResponse.success(createdTrip, "Trip created successfully.");
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Retrieves a paginated list of trips for the authenticated user.
   *
   * @param pageable Pagination parameters (page, size, sort)
   * @return A {@link ResponseEntity} containing a paginated list of trips.
   */
  @Operation(
      summary = "Get all trips",
      description = "Get all trips created by the authenticated user with pagination support")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<TripResponseDTO>>> getAllTrips(Pageable pageable) {
    UUID userId = SecurityUtils.getCurrentUserId();
    log.info(
        "User {} fetching trips - page: {}, size: {}",
        userId,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<TripResponseDTO> trips = tripService.getAllTrips(userId, pageable);
    ApiResponse<Page<TripResponseDTO>> response =
        ApiResponse.success(trips, "Trips retrieved successfully.");
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves a single trip by its ID.
   *
   * @param tripId The UUID of the trip to retrieve.
   * @return A {@link ResponseEntity} with the trip's details.
   */
  @Operation(
      summary = "Get a trip by its ID",
      description = "Get detailed information about a specific trip")
  @GetMapping("/{tripId}")
  public ResponseEntity<ApiResponse<TripResponseDTO>> getTripById(@PathVariable UUID tripId) {
    UUID userId = SecurityUtils.getCurrentUserId();
    log.info("User {} fetching trip {}", userId, tripId);

    TripResponseDTO trip = tripService.getTripById(tripId, userId);
    ApiResponse<TripResponseDTO> response =
        ApiResponse.success(trip, "Trip details retrieved successfully.");
    return ResponseEntity.ok(response);
  }

  /**
   * Updates an existing trip.
   *
   * @param tripId The UUID of the trip to update.
   * @param tripUpdateDTO The DTO containing the updated trip data.
   * @return A {@link ResponseEntity} with the updated trip's details.
   */
  @Operation(
      summary = "Update an existing trip",
      description = "Update trip details. Only the trip owner can update.")
  @PutMapping("/{tripId}")
  public ResponseEntity<ApiResponse<TripResponseDTO>> updateTrip(
      @PathVariable UUID tripId, @Valid @RequestBody TripUpdateDTO tripUpdateDTO) {
    UUID userId = SecurityUtils.getCurrentUserId();
    log.info("User {} updating trip {}", userId, tripId);

    TripResponseDTO updatedTrip = tripService.updateTrip(tripId, tripUpdateDTO, userId);
    ApiResponse<TripResponseDTO> response =
        ApiResponse.success(updatedTrip, "Trip updated successfully.");
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a trip.
   *
   * @param tripId The UUID of the trip to delete.
   * @return A {@link ResponseEntity} confirming the deletion.
   */
  @Operation(
      summary = "Delete a trip",
      description = "Delete a trip. Only the trip owner can delete.")
  @DeleteMapping("/{tripId}")
  public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable UUID tripId) {
    UUID userId = SecurityUtils.getCurrentUserId();
    log.info("User {} deleting trip {}", userId, tripId);

    tripService.deleteTrip(tripId, userId);
    ApiResponse<Void> response = ApiResponse.success(null, "Trip deleted successfully.");
    return ResponseEntity.ok(response);
  }
}
