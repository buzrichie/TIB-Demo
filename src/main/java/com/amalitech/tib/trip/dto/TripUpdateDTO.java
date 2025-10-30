package com.amalitech.tib.trip.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

/**
 * DTO for updating an existing trip.
 *
 * @param title The new title of the trip.
 * @param startDate The new start date of the trip.
 * @param endDate The new end date of the trip.
 * @param destinationId The new ID of the destination.
 * @param destination The new name of the destination.
 * @param travelers The new number of travelers.
 */
public record TripUpdateDTO(
    String title,
    @FutureOrPresent LocalDate startDate,
    @Future LocalDate endDate,
    String destinationId,
    String destination,
    Integer travelers) {}
