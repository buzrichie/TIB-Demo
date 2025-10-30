package com.amalitech.tib.trip.dto;

import java.time.LocalDate;

/**
 * DTO for representing trip details in a response.
 *
 * @param id The unique identifier of the trip.
 * @param title The title of the trip.
 * @param destination The destination of the trip.
 * @param startDate The start date of the trip.
 * @param endDate The end date of the trip.
 * @param travelers The number of travelers on the trip.
 * @param createdBy The user who created the trip.
 * @param inviteLink The link to invite others to the trip.
 */
public record TripResponseDTO(
    String id,
    String title,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    int travelers,
    String createdBy,
    String inviteLink) {}
