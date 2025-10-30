package com.amalitech.tib.destination.dto;

import com.amalitech.tib.destination.enums.DestinationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DestinationRequest(
    @NotBlank(message = "Destination name is required")
        @Size(max = 100, message = "Destination name must not exceed 100 characters")
        String name,
    @NotBlank(message = "Country is required") String country,
    @NotBlank(message = "Region is required") String region,
    @NotNull(message = "Latitude is required") Float latitude,
    @NotNull(message = "Longitude is required") Float longitude,
    @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,
    @NotNull(message = "Status is required") DestinationStatus status) {}
