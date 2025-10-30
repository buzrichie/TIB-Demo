package com.amalitech.tib.trip.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddTripMateRequestDTO(
    @NotEmpty(message = "At least one email is required") List<String> emails) {}
