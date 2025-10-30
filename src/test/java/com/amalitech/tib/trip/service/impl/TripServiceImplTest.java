package com.amalitech.tib.trip.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amalitech.tib.destination.model.Destination;
import com.amalitech.tib.exception.BadException;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.trip.dto.TripCreationDTO;
import com.amalitech.tib.trip.dto.TripResponseDTO;
import com.amalitech.tib.trip.dto.TripUpdateDTO;
import com.amalitech.tib.trip.enums.TravelerRole;
import com.amalitech.tib.trip.enums.TripInviteStatus;
import com.amalitech.tib.trip.model.Traveler;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.trip.repository.TravelerRepository;
import com.amalitech.tib.trip.repository.TripRepository;
import com.amalitech.tib.trip.service.TripMailService;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.util.TripInviteLinkBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

  @Mock private TripRepository tripRepository;
  @Mock private TravelerRepository travelerRepository;
  @Mock private UserRepository userRepository;
  @Mock private TripMailService tripMailService;
  @Mock private TripInviteLinkBuilder tripInviteLinkBuilder;
  @Mock private EntityManager entityManager;
  @Mock private TypedQuery<UUID> typedQuery;

  @InjectMocks private TripServiceImpl tripService;

  @Captor private ArgumentCaptor<List<Traveler>> travelersCaptor;

  private User user;
  private Trip trip;
  private UUID userId;
  private UUID tripId;
  private UUID destinationId;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    userId = UUID.randomUUID();
    tripId = UUID.randomUUID();
    destinationId = UUID.randomUUID();

    user = new User();
    user.setId(userId);
    user.setEmail("user@example.com");

    trip = new Trip();
    trip.setId(tripId);
    trip.setUser(user);
    trip.setTitle("Test Trip");
    trip.setInviteToken("test-token");

    // Manually inject the mocked EntityManager
    Field entityManagerField = TripServiceImpl.class.getDeclaredField("entityManager");
    entityManagerField.setAccessible(true);
    entityManagerField.set(tripService, entityManager);
  }

  private void mockDestinationLookup() {
    when(entityManager.createQuery(
            "SELECT d.id FROM Destination d WHERE LOWER(d.name) = LOWER(:name)", UUID.class))
        .thenReturn(typedQuery);
    when(typedQuery.setParameter(eq("name"), anyString())).thenReturn(typedQuery);
    when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
    when(typedQuery.getResultList()).thenReturn(List.of(destinationId));

    Destination destination = new Destination();
    destination.setId(destinationId);
    when(entityManager.getReference(eq(Destination.class), eq(destinationId)))
        .thenReturn(destination);
  }

  @Nested
  @DisplayName("Create Trip")
  class CreateTripTests {
    @Test
    @DisplayName("Should create trip, add owner as traveler, and send invites")
    void shouldCreateTripAndSendInvites() {
      TripCreationDTO creationDTO =
          new TripCreationDTO(
              "New Trip",
              LocalDate.now(),
              LocalDate.now().plusDays(5),
              null,
              "Test Destination",
              3,
              List.of("invitee1@example.com", "invitee2@example.com"));

      mockDestinationLookup();
      when(entityManager.getReference(eq(User.class), eq(userId))).thenReturn(user);
      when(tripRepository.save(any(Trip.class))).thenReturn(trip);
      when(tripInviteLinkBuilder.build(anyString())).thenReturn("http://invite.link");

      TripResponseDTO result = tripService.createTrip(creationDTO, userId);

      assertNotNull(result);
      verify(travelerRepository).saveAll(travelersCaptor.capture());
      List<Traveler> savedTravelers = travelersCaptor.getValue();

      assertEquals(3, savedTravelers.size());
      assertTrue(
          savedTravelers.stream()
              .anyMatch(
                  t ->
                      t.getRole() == TravelerRole.OWNER
                          && t.getTripInviteStatus() == TripInviteStatus.ACCEPTED));
      assertEquals(
          2, savedTravelers.stream().filter(t -> t.getRole() == TravelerRole.COLLABORATOR).count());

      verify(tripMailService, times(2)).sendTripInvite(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle email sending failures gracefully")
    void shouldHandleEmailFailures() {
      TripCreationDTO creationDTO =
          new TripCreationDTO(
              "New Trip",
              LocalDate.now(),
              LocalDate.now().plusDays(5),
              null,
              "Test Destination",
              2,
              List.of("invitee@example.com"));

      mockDestinationLookup();
      when(entityManager.getReference(eq(User.class), eq(userId))).thenReturn(user);
      when(tripRepository.save(any(Trip.class))).thenReturn(trip);
      when(tripInviteLinkBuilder.build(anyString())).thenReturn("http://invite.link");
      doThrow(new RuntimeException("Email failed"))
          .when(tripMailService)
          .sendTripInvite(anyString(), anyString(), anyString());

      assertDoesNotThrow(() -> tripService.createTrip(creationDTO, userId));
      verify(travelerRepository).saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("Get Trip(s)")
  class GetTripTests {
    @Test
    @DisplayName("Should return a paginated list of trips for a user")
    void shouldReturnPagedTrips() {
      PageRequest pageable = PageRequest.of(0, 10);
      Page<Trip> pagedTrips = new PageImpl<>(List.of(trip), pageable, 1);

      when(tripRepository.findByUser_Id(userId, pageable)).thenReturn(pagedTrips);

      Page<TripResponseDTO> result = tripService.getAllTrips(userId, pageable);

      assertEquals(1, result.getTotalElements());
      verify(tripInviteLinkBuilder).build(trip.getInviteToken());
    }

    @Test
    @DisplayName("Should return a trip by ID when user is a traveler")
    void shouldReturnTripByIdWhenAuthorized() {
      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
      when(travelerRepository.findByTripIdAndUserId(tripId, userId))
          .thenReturn(Optional.of(new Traveler()));

      TripResponseDTO result = tripService.getTripById(tripId, userId);

      assertNotNull(result);
      verify(tripInviteLinkBuilder).build(trip.getInviteToken());
    }

    @Test
    @DisplayName("Should throw BadException when user is not a traveler")
    void shouldThrowExceptionWhenNotAuthorized() {
      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
      when(travelerRepository.findByTripIdAndUserId(tripId, userId)).thenReturn(Optional.empty());

      assertThrows(BadException.class, () -> tripService.getTripById(tripId, userId));
    }
  }

  @Nested
  @DisplayName("Update Trip")
  class UpdateTripTests {
    @Test
    @DisplayName("Should update trip when user is the owner")
    void shouldUpdateTripWhenOwner() {
      TripUpdateDTO updateDTO = new TripUpdateDTO("Updated Title", null, null, null, null, null);

      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
      when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

      TripResponseDTO result = tripService.updateTrip(tripId, updateDTO, userId);

      assertEquals("Updated Title", result.title());
    }

    @Test
    @DisplayName("Should throw BadException when user is not the owner")
    void shouldThrowExceptionWhenNotOwner() {
      TripUpdateDTO updateDTO = new TripUpdateDTO("Updated Title", null, null, null, null, null);
      UUID notOwnerId = UUID.randomUUID();

      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

      assertThrows(BadException.class, () -> tripService.updateTrip(tripId, updateDTO, notOwnerId));
    }

    @Test
    @DisplayName("Should throw BadException when end date is before start date")
    void shouldThrowExceptionForInvalidDates() {
      TripUpdateDTO updateDTO =
          new TripUpdateDTO(null, LocalDate.now(), LocalDate.now().minusDays(1), null, null, null);

      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

      assertThrows(BadException.class, () -> tripService.updateTrip(tripId, updateDTO, userId));
    }
  }

  @Nested
  @DisplayName("Delete Trip")
  class DeleteTripTests {
    @Test
    @DisplayName("Should delete trip when user is the owner")
    void shouldDeleteTripWhenOwner() {
      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

      assertDoesNotThrow(() -> tripService.deleteTrip(tripId, userId));
      verify(tripRepository, times(1)).delete(trip);
    }

    @Test
    @DisplayName("Should throw BadException when user is not the owner")
    void shouldThrowExceptionWhenNotOwner() {
      UUID notOwnerId = UUID.randomUUID();
      when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

      assertThrows(BadException.class, () -> tripService.deleteTrip(tripId, notOwnerId));
      verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trip does not exist")
    void shouldThrowExceptionWhenTripNotFound() {
      when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> tripService.deleteTrip(tripId, userId));
    }
  }
}
