package com.amalitech.tib.trip.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for displaying the details of a trip invitation, typically for a preview.
 *
 * @param tripId The unique identifier of the trip.
 * @param tripTitle The title of the trip.
 * @param destination The destination of the trip.
 * @param startDate The start date of the trip.
 * @param endDate The end date of the trip.
 * @param travelersCount The number of travelers on the trip.
 * @param createdByEmail The email of the user who created the trip.
 * @param createdAt The timestamp when the invitation was created.
 */
public record TripInviteDetailsDTO(
    String tripId,
    String tripTitle,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    Integer travelersCount,
    String createdByEmail,
    Instant createdAt) {}
