package com.amalitech.tib.destination.service;

import com.amalitech.tib.destination.dto.DestinationRequest;
import com.amalitech.tib.destination.dto.DestinationResponse;
import com.amalitech.tib.destination.enums.DestinationStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/** Service interface defining CRUD operations for managing destinations. */
public interface DestinationService {

  DestinationResponse createDestination(DestinationRequest request, MultipartFile image);

  Page<DestinationResponse> getAllDestinations(Pageable pageable);

  DestinationResponse getDestinationById(UUID id);

  Page<DestinationResponse> searchDestinations(
      String name, String country, String region, DestinationStatus status, Pageable pageable);

  DestinationResponse updateDestination(UUID id, DestinationRequest request, MultipartFile image);

  void deleteDestination(UUID id);
}
