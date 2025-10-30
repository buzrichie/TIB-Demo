package com.amalitech.tib.trip.controller;

import com.amalitech.tib.trip.dto.AddTripMateRequestDTO;
import com.amalitech.tib.trip.dto.TripInviteAcceptanceDTO;
import com.amalitech.tib.trip.dto.TripInviteDetailsDTO;
import com.amalitech.tib.trip.dto.TripInviteRequestDTO;
import com.amalitech.tib.trip.service.TripInviteService;
import com.amalitech.tib.util.ApiResponse;
import com.amalitech.tib.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling trip invitations. This class provides endpoints for adding tripmates,
 * previewing, accepting, rejecting, and revoking trip invites.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Endpoints for handling trip invites.")
@Slf4j
public class TripInviteController {

  private final TripInviteService tripInviteService;

  @Operation(
      summary = "Add a trip mate",
      description = "Add a trip mate to a trip. The trip mate will be added as a collaborator.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/{tripId}/invites")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Object>> addTripMate(
      @PathVariable UUID tripId, @Valid @RequestBody AddTripMateRequestDTO request) {
    UUID ownerId = SecurityUtils.getCurrentUserId();
    log.info("Add trip mate request for trip: {} by owner: {}", tripId, ownerId);

    tripInviteService.addTripMate(tripId, request.emails(), ownerId);
    return ResponseEntity.ok(ApiResponse.success(null, "Trip mate added successfully."));
  }

  @Operation(
      summary = "Get invite link",
      description = "Get the invite link for a trip. Only the trip owner can get the invite link.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/{tripId}/invite-link")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Map<String, String>>> getInviteLink(@PathVariable UUID tripId) {
    UUID ownerId = SecurityUtils.getCurrentUserId();
    log.info("Get invite link request for trip: {} by owner: {}", tripId, ownerId);

    String inviteLink = tripInviteService.getInviteLink(tripId, ownerId);
    return ResponseEntity.ok(
        ApiResponse.success(
            Map.of("inviteLink", inviteLink), "Invite link generated successfully."));
  }

  @Operation(
      summary = "Preview an invite by token",
      description = "Get trip details for an invite without authentication")
  @GetMapping("/invite")
  public ResponseEntity<ApiResponse<TripInviteDetailsDTO>> previewInvite(
      @RequestParam String token) {
    log.info("Preview invite request for token: {}", token);
    TripInviteDetailsDTO details = tripInviteService.getInviteDetails(token);
    return ResponseEntity.ok(ApiResponse.success(details, "Invite is valid."));
  }

  @Operation(
      summary = "Accept an invite by token",
      description =
          "Accept a trip invitation. Authentication is optional - provide email to track acceptance.")
  @PostMapping("/invite/{token}/accept")
  public ResponseEntity<ApiResponse<TripInviteAcceptanceDTO>> acceptInvite(
      @PathVariable String token, @Valid @RequestBody TripInviteRequestDTO request) {
    UUID userId = SecurityUtils.getCurrentUserIdOrNull();
    log.info(
        "Accept invite request for token: {} by email: {} (userId: {})",
        token,
        request.email(),
        userId);

    TripInviteAcceptanceDTO result = tripInviteService.acceptInvite(token, request.email(), userId);
    return ResponseEntity.ok(ApiResponse.success(result, result.message()));
  }

  @Operation(
      summary = "Reject an invite by token",
      description =
          "Reject a trip invitation. Authentication is optional - provide email to track rejection.")
  @PostMapping("/invite/{token}/reject")
  public ResponseEntity<ApiResponse<TripInviteAcceptanceDTO>> rejectInvite(
      @PathVariable String token, @Valid @RequestBody TripInviteRequestDTO request) {
    UUID userId = SecurityUtils.getCurrentUserIdOrNull();
    log.info(
        "Reject invite request for token: {} by email: {} (userId: {})",
        token,
        request.email(),
        userId);

    TripInviteAcceptanceDTO result = tripInviteService.rejectInvite(token, request.email(), userId);
    return ResponseEntity.ok(ApiResponse.success(result, result.message()));
  }

  @Operation(
      summary = "Revoke an invitation",
      description = "Trip owner can revoke an invitation or remove a collaborator")
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/{tripId}/invites")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> revokeInvite(
      @PathVariable UUID tripId, @RequestParam String email) {
    UUID ownerId = SecurityUtils.getCurrentUserId();
    log.info("Revoke invite request for trip: {} email: {} by owner: {}", tripId, email, ownerId);

    tripInviteService.revokeInvite(tripId, email, ownerId);
    ApiResponse<Void> response = ApiResponse.success(null, "Invite revoked successfully.");
    return ResponseEntity.ok(response);
  }
}
