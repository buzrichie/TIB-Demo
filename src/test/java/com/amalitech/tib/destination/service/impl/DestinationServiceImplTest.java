package com.amalitech.tib.destination.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.destination.dto.DestinationRequest;
import com.amalitech.tib.destination.dto.DestinationResponse;
import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.mapper.DestinationMapper;
import com.amalitech.tib.destination.model.Destination;
import com.amalitech.tib.destination.repository.DestinationRepository;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.util.S3FileStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class DestinationServiceImplTest {

  @Mock private DestinationRepository destinationRepository;

  @Mock private DestinationMapper destinationMapper;

  @Mock private S3FileStorage s3FileStorage;

  @InjectMocks private DestinationServiceImpl destinationService;

  private UUID destinationId;
  private Destination destination;
  private DestinationRequest destinationRequest;
  private DestinationResponse destinationResponse;
  private MultipartFile mockImage;

  @BeforeEach
  void setUp() {
    destinationId = UUID.randomUUID();

    destinationRequest =
        new DestinationRequest(
            "Paris",
            "France",
            "Île-de-France",
            48.8566f,
            2.3522f,
            "City of Light",
            DestinationStatus.PUBLISHED);

    destination = new Destination();
    destination.setId(destinationId);
    destination.setName("Paris");
    destination.setCountry("France");
    destination.setRegion("Île-de-France");
    destination.setLatitude(48.8566f);
    destination.setLongitude(2.3522f);
    destination.setDescription("City of Light");
    destination.setStatus(DestinationStatus.PUBLISHED);
    destination.setImageUrl("https://s3.amazonaws.com/bucket/destinations/" + destinationId);

    destinationResponse =
        new DestinationResponse(
            destinationId,
            "Paris",
            "France",
            "Île-de-France",
            "https://s3.amazonaws.com/bucket/destinations/" + destinationId,
            48.8566f,
            2.3522f,
            "City of Light",
            DestinationStatus.PUBLISHED);

    mockImage = mock(MultipartFile.class);
  }

  @Test
  @DisplayName("Should create destination successfully with image")
  void createDestination_WithImage_ShouldSaveDestinationWithImageUrl() {

    String expectedImageUrl = "https://s3.amazonaws.com/bucket/destinations/" + UUID.randomUUID();
    when(mockImage.isEmpty()).thenReturn(false);
    when(s3FileStorage.uploadFile(any(MultipartFile.class), anyString()))
        .thenReturn(expectedImageUrl);
    when(destinationMapper.toEntity(destinationRequest)).thenReturn(destination);
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result =
        destinationService.createDestination(destinationRequest, mockImage);

    assertNotNull(result);
    assertEquals(destinationResponse, result);
    verify(s3FileStorage, times(1)).uploadFile(any(MultipartFile.class), contains("destinations/"));
    verify(destinationMapper, times(1)).toEntity(destinationRequest);
    verify(destinationRepository, times(1)).save(destination);
    verify(destinationMapper, times(1)).toResponse(destination);
  }

  @Test
  @DisplayName("Should create destination successfully without image")
  void createDestination_WithoutImage_ShouldSaveDestinationWithNullImageUrl() {

    destination.setImageUrl(null);
    when(destinationMapper.toEntity(destinationRequest)).thenReturn(destination);
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result = destinationService.createDestination(destinationRequest, null);

    assertNotNull(result);
    assertEquals(destinationResponse, result);
    verify(s3FileStorage, never()).uploadFile(any(MultipartFile.class), anyString());
    verify(destinationRepository, times(1)).save(destination);
  }

  @Test
  @DisplayName("Should create destination with empty image file without uploading")
  void createDestination_WithEmptyImage_ShouldNotUploadToS3() {

    when(mockImage.isEmpty()).thenReturn(true);
    when(destinationMapper.toEntity(destinationRequest)).thenReturn(destination);
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result =
        destinationService.createDestination(destinationRequest, mockImage);

    assertNotNull(result);
    verify(s3FileStorage, never()).uploadFile(any(MultipartFile.class), anyString());
    verify(destinationRepository, times(1)).save(destination);
  }

  @Test
  @DisplayName("Should return paginated destinations")
  void getAllDestinations_WithPageable_ShouldReturnDestinationPage() {

    Pageable pageable = mock(Pageable.class);
    Page<Destination> destinationPage = new PageImpl<>(List.of(destination));
    Page<DestinationResponse> expectedResponsePage = new PageImpl<>(List.of(destinationResponse));

    when(destinationRepository.findAll(pageable)).thenReturn(destinationPage);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    Page<DestinationResponse> result = destinationService.getAllDestinations(pageable);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(destinationResponse, result.getContent().get(0));
    verify(destinationRepository, times(1)).findAll(pageable);
    verify(destinationMapper, times(1)).toResponse(destination);
  }

  @Test
  @DisplayName("Should search destinations with all filters")
  void searchDestinations_WithAllFilters_ShouldReturnFilteredResults() {

    Pageable pageable = mock(Pageable.class);
    String name = "Paris";
    String country = "France";
    String region = "Île-de-France";
    DestinationStatus status = DestinationStatus.PUBLISHED;

    Page<Destination> destinationPage = new PageImpl<>(List.of(destination));
    Page<DestinationResponse> expectedResponsePage = new PageImpl<>(List.of(destinationResponse));

    when(destinationRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(destinationPage);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    Page<DestinationResponse> result =
        destinationService.searchDestinations(name, country, region, status, pageable);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(destinationRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    verify(destinationMapper, times(1)).toResponse(destination);
  }

  @Test
  @DisplayName("Should search destinations with partial filters")
  void searchDestinations_WithPartialFilters_ShouldReturnFilteredResults() {

    Pageable pageable = mock(Pageable.class);
    String name = "Paris";

    Page<Destination> destinationPage = new PageImpl<>(List.of(destination));
    when(destinationRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(destinationPage);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    Page<DestinationResponse> result =
        destinationService.searchDestinations(name, null, null, null, pageable);

    assertNotNull(result);
    verify(destinationRepository, times(1)).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Should return destination by valid ID")
  void getDestinationById_WithValidId_ShouldReturnDestination() {

    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result = destinationService.getDestinationById(destinationId);

    assertNotNull(result);
    assertEquals(destinationResponse, result);
    verify(destinationRepository, times(1)).findById(destinationId);
    verify(destinationMapper, times(1)).toResponse(destination);
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when destination not found by ID")
  void getDestinationById_WithInvalidId_ShouldThrowException() {

    UUID invalidId = UUID.randomUUID();
    when(destinationRepository.findById(invalidId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> destinationService.getDestinationById(invalidId));

    assertEquals("Destination not found with id: " + invalidId, exception.getMessage());
    verify(destinationRepository, times(1)).findById(invalidId);
    verify(destinationMapper, never()).toResponse(any());
  }

  @Test
  @DisplayName("Should update destination with new image")
  void updateDestination_WithNewImage_ShouldUpdateAndUploadImage() {

    DestinationRequest updateRequest =
        new DestinationRequest(
            "Updated Paris",
            "France",
            "Updated Region",
            49.8566f,
            3.3522f,
            "Updated Description",
            DestinationStatus.ARCHIVED);

    String newImageUrl = "https://s3.amazonaws.com/bucket/destinations/new-image";
    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    when(mockImage.isEmpty()).thenReturn(false);
    when(s3FileStorage.uploadFile(any(MultipartFile.class), anyString())).thenReturn(newImageUrl);
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result =
        destinationService.updateDestination(destinationId, updateRequest, mockImage);

    assertNotNull(result);
    assertEquals("Updated Paris", destination.getName());
    assertEquals("Updated Region", destination.getRegion());
    assertEquals("Updated Description", destination.getDescription());
    assertEquals(DestinationStatus.ARCHIVED, destination.getStatus());
    assertEquals(newImageUrl, destination.getImageUrl());
    verify(s3FileStorage, times(1))
        .uploadFile(any(MultipartFile.class), eq("destinations/" + destinationId));
    verify(destinationRepository, times(1)).save(destination);
  }

  @Test
  @DisplayName("Should update destination without changing image when no new image provided")
  void updateDestination_WithoutNewImage_ShouldUpdateWithoutImageChange() {

    String originalImageUrl = destination.getImageUrl();
    DestinationRequest updateRequest =
        new DestinationRequest(
            "Updated Paris",
            "France",
            "Updated Region",
            49.8566f,
            3.3522f,
            "Updated Description",
            DestinationStatus.ARCHIVED);

    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result =
        destinationService.updateDestination(destinationId, updateRequest, null);

    assertNotNull(result);
    assertEquals(originalImageUrl, destination.getImageUrl());
    verify(s3FileStorage, never()).uploadFile(any(MultipartFile.class), anyString());
    verify(destinationRepository, times(1)).save(destination);
  }

  @Test
  @DisplayName("Should update destination with empty image file without uploading")
  void updateDestination_WithEmptyImage_ShouldNotUploadNewImage() {

    String originalImageUrl = destination.getImageUrl();
    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    when(mockImage.isEmpty()).thenReturn(true);
    when(destinationRepository.save(destination)).thenReturn(destination);
    when(destinationMapper.toResponse(destination)).thenReturn(destinationResponse);

    DestinationResponse result =
        destinationService.updateDestination(destinationId, destinationRequest, mockImage);

    assertNotNull(result);
    assertEquals(originalImageUrl, destination.getImageUrl());
    verify(s3FileStorage, never()).uploadFile(any(MultipartFile.class), anyString());
    verify(destinationRepository, times(1)).save(destination);
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when updating non-existent destination")
  void updateDestination_WithInvalidId_ShouldThrowException() {

    UUID invalidId = UUID.randomUUID();
    when(destinationRepository.findById(invalidId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> destinationService.updateDestination(invalidId, destinationRequest, mockImage));

    assertEquals("Destination not found with id: " + invalidId, exception.getMessage());
    verify(destinationRepository, never()).save(any());
    verify(s3FileStorage, never()).uploadFile(any(MultipartFile.class), anyString());
  }

  @Test
  @DisplayName("Should delete destination successfully with image cleanup")
  void deleteDestination_WithImage_ShouldDeleteAndCleanupS3() {

    final String expectedFileKey = "destinations/" + destinationId;

    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    when(s3FileStorage.extractS3Key(eq(destination.getImageUrl()), eq("destinations/")))
        .thenReturn(expectedFileKey);

    doNothing().when(s3FileStorage).deleteFile(anyString());
    doNothing().when(destinationRepository).delete(destination);

    destinationService.deleteDestination(destinationId);

    verify(destinationRepository, times(1)).findById(destinationId);

    verify(s3FileStorage, times(1)).deleteFile(eq(expectedFileKey));

    verify(destinationRepository, times(1)).delete(destination);
  }

  @Test
  @DisplayName("Should delete destination without image without S3 cleanup")
  void deleteDestination_WithoutImage_ShouldDeleteWithoutS3Cleanup() {

    destination.setImageUrl(null);
    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    doNothing().when(destinationRepository).delete(destination);

    destinationService.deleteDestination(destinationId);

    verify(destinationRepository, times(1)).findById(destinationId);
    verify(s3FileStorage, never()).deleteFile(anyString());
    verify(destinationRepository, times(1)).delete(destination);
  }

  @Test
  @DisplayName("Should handle S3 deletion failure gracefully during destination deletion")
  void deleteDestination_WhenS3DeletionFails_ShouldThrowResourceNotFoundException() {

    when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
    doThrow(new RuntimeException("S3 error")).when(s3FileStorage).deleteFile(anyString());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> destinationService.deleteDestination(destinationId));

    assertTrue(exception.getMessage().contains("Could not delete file:"));
    verify(destinationRepository, never()).delete((Destination) any());
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when deleting non-existent destination")
  void deleteDestination_WithInvalidId_ShouldThrowException() {

    UUID invalidId = UUID.randomUUID();
    when(destinationRepository.findById(invalidId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> destinationService.deleteDestination(invalidId));

    assertEquals("Destination not found with id: " + invalidId, exception.getMessage());
    verify(destinationRepository, never()).delete((Destination) any());
    verify(s3FileStorage, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("Should handle empty page results for destination queries")
  void getAllDestinations_WithEmptyResult_ShouldReturnEmptyPage() {

    Pageable pageable = mock(Pageable.class);
    Page<Destination> emptyPage = Page.empty();
    when(destinationRepository.findAll(pageable)).thenReturn(emptyPage);

    Page<DestinationResponse> result = destinationService.getAllDestinations(pageable);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(destinationRepository, times(1)).findAll(pageable);
    verify(destinationMapper, never()).toResponse(any());
  }
}
