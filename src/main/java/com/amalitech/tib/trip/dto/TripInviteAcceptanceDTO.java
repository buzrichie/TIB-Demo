package com.amalitech.tib.trip.dto;

/**
 * DTO for representing the response after a user accepts or rejects a trip invitation.
 *
 * @param success True if the operation was successful, false otherwise.
 * @param message A message describing the outcome of the operation.
 * @param tripId The unique identifier of the trip.
 * @param tripTitle The title of the trip.
 */
public record TripInviteAcceptanceDTO(
    boolean success, String message, String tripId, String tripTitle) {}
