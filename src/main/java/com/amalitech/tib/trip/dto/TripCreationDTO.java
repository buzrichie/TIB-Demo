package com.amalitech.tib.trip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new trip.
 *
 * @param title The title of the trip.
 * @param startDate The start date of the trip.
 * @param endDate The end date of the trip.
 * @param destinationId The ID of the destination.
 * @param destination The name of the destination.
 * @param travelers The number of travelers.
 * @param inviteeEmails A list of email addresses to invite to the trip.
 */
public record TripCreationDTO(
    @NotBlank String title,
    @NotNull @FutureOrPresent LocalDate startDate,
    @NotNull @Future LocalDate endDate,
    String destinationId,
    String destination,
    Integer travelers,
    @Size(max = 50) List<@Email String> inviteeEmails) {}
