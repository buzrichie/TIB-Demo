package com.amalitech.tib.destination.service.impl;

import com.amalitech.tib.destination.dto.DestinationRequest;
import com.amalitech.tib.destination.dto.DestinationResponse;
import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.mapper.DestinationMapper;
import com.amalitech.tib.destination.model.Destination;
import com.amalitech.tib.destination.repository.DestinationRepository;
import com.amalitech.tib.destination.service.DestinationService;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.specification.DestinationSpecification;
import com.amalitech.tib.util.S3FileStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of {@link DestinationService} providing CRUD operations for managing travel
 * destinations, integrated with AWS S3 for image storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationServiceImpl implements DestinationService {

  private static final String DESTINATION_IMAGE_PATH = "destinations/";

  private final DestinationRepository destinationRepository;
  private final DestinationMapper destinationMapper;
  private final S3FileStorage s3FileStorage;

  @Override
  @Transactional
  public DestinationResponse createDestination(DestinationRequest request, MultipartFile image) {
    String imageUrl = null;

    if (image != null && !image.isEmpty()) {
      String s3Key = DESTINATION_IMAGE_PATH + UUID.randomUUID();
      imageUrl = s3FileStorage.uploadFile(image, s3Key);
    }

    Destination destination = destinationMapper.toEntity(request);
    destination.setImageUrl(imageUrl);

    Destination saved = destinationRepository.save(destination);

    return destinationMapper.toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DestinationResponse> getAllDestinations(Pageable pageable) {
    return destinationRepository.findAll(pageable).map(destinationMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DestinationResponse> searchDestinations(
      String name, String country, String region, DestinationStatus status, Pageable pageable) {
    Specification<Destination> spec =
        DestinationSpecification.filterBy(name, country, region, status);
    return destinationRepository.findAll(spec, pageable).map(destinationMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public DestinationResponse getDestinationById(UUID id) {
    Destination destination =
        destinationRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Destination not found with id: " + id));
    return destinationMapper.toResponse(destination);
  }

  @Override
  @Transactional
  public DestinationResponse updateDestination(
      UUID id, DestinationRequest request, MultipartFile image) {
    Destination existing =
        destinationRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Destination not found with id: " + id));

    if (image != null && !image.isEmpty()) {
      String s3Key = DESTINATION_IMAGE_PATH + id;
      String newImageUrl = s3FileStorage.uploadFile(image, s3Key);
      existing.setImageUrl(newImageUrl);
    }

    existing.setName(request.name());
    existing.setCountry(request.country());
    existing.setRegion(request.region());
    existing.setLatitude(request.latitude());
    existing.setLongitude(request.longitude());
    existing.setDescription(request.description());
    existing.setStatus(request.status());

    Destination updated = destinationRepository.save(existing);
    return destinationMapper.toResponse(updated);
  }

  @Override
  @Transactional
  public void deleteDestination(UUID id) {
    Destination destination =
        destinationRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Destination not found with id: " + id));

    if (destination.getImageUrl() != null) {
      try {
        String fileKey =
            s3FileStorage.extractS3Key(destination.getImageUrl(), DESTINATION_IMAGE_PATH);
        s3FileStorage.deleteFile(fileKey);
      } catch (Exception e) {
        throw new ResourceNotFoundException("Could not delete file: " + e.getMessage());
      }
    }

    destinationRepository.delete(destination);
  }
}
