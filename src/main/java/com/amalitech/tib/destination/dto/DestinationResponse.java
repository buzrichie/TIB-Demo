package com.amalitech.tib.destination.dto;

import com.amalitech.tib.destination.enums.DestinationStatus;
import java.util.UUID;

public record DestinationResponse(
    UUID id,
    String name,
    String country,
    String region,
    String imageUrl,
    Float latitude,
    Float longitude,
    String description,
    DestinationStatus status) {}
