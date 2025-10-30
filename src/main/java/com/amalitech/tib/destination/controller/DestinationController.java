package com.amalitech.tib.destination.controller;

import com.amalitech.tib.destination.dto.DestinationRequest;
import com.amalitech.tib.destination.dto.DestinationResponse;
import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.service.DestinationService;
import com.amalitech.tib.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing travel destinations. Supports CRUD operations and integrates with
 * AWS S3 for image handling.
 */
@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
@Tag(name = "Destinations", description = "APIs for managing travel destinations")
public class DestinationController {

  private final DestinationService destinationService;

  @Operation(
      summary = "Create a new destination",
      description =
          "Creates a new travel destination with optional image upload and returns the created record.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<DestinationResponse>> createDestination(
      @Valid @ModelAttribute("destination") DestinationRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {
    DestinationResponse created = destinationService.createDestination(request, image);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Destination created successfully"));
  }

  @Operation(
      summary = "Get all destinations (paginated)",
      description = "Fetches a paginated list of destinations with their key details.")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<DestinationResponse>>> getAllDestinations(
      Pageable pageable) {
    Page<DestinationResponse> page = destinationService.getAllDestinations(pageable);
    return ResponseEntity.ok(ApiResponse.success(page, "Destinations retrieved successfully"));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<DestinationResponse>>> searchDestinations(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) DestinationStatus status,
      Pageable pageable) {
    Page<DestinationResponse> page =
        destinationService.searchDestinations(name, country, region, status, pageable);
    return ResponseEntity.ok(
        ApiResponse.success(page, "Filtered destinations retrieved successfully"));
  }

  @Operation(
      summary = "Get a destination by ID",
      description = "Retrieves a single destination by its unique identifier.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<DestinationResponse>> getDestinationById(
      @PathVariable UUID id) {
    DestinationResponse response = destinationService.getDestinationById(id);
    return ResponseEntity.ok(ApiResponse.success(response, "Destination retrieved successfully"));
  }

  @Operation(
      summary = "Update an existing destination",
      description = "Updates a destination record and uploads a new image if provided.")
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<DestinationResponse>> updateDestination(
      @PathVariable UUID id,
      @Valid @ModelAttribute("destination") DestinationRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {
    DestinationResponse updated = destinationService.updateDestination(id, request, image);
    return ResponseEntity.ok(ApiResponse.success(updated, "Destination updated successfully"));
  }

  @Operation(
      summary = "Delete a destination",
      description = "Deletes a destination record and its image from S3 if available.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteDestination(@PathVariable UUID id) {
    destinationService.deleteDestination(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Destination deleted successfully"));
  }
}
