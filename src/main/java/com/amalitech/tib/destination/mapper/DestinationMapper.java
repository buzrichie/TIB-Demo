package com.amalitech.tib.destination.mapper;

import com.amalitech.tib.destination.dto.DestinationRequest;
import com.amalitech.tib.destination.dto.DestinationResponse;
import com.amalitech.tib.destination.model.Destination;
import org.mapstruct.Mapper;

/** MapStruct mapper for converting between Destination entity and DTOs. */
@Mapper(componentModel = "spring")
public interface DestinationMapper {
  Destination toEntity(DestinationRequest request);

  DestinationResponse toResponse(Destination destination);
}
