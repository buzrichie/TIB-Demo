package com.amalitech.tib.trip.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.trip.dto.TripInviteAcceptanceDTO;
import com.amalitech.tib.trip.dto.TripInviteDetailsDTO;
import com.amalitech.tib.trip.enums.TravelerRole;
import com.amalitech.tib.trip.enums.TripInviteStatus;
import com.amalitech.tib.trip.model.Traveler;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.trip.repository.TravelerRepository;
import com.amalitech.tib.trip.repository.TripRepository;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripInviteServiceImplTest {

  @Mock private TripRepository tripRepository;

  @Mock private TravelerRepository travelerRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private TripInviteServiceImpl tripInviteService;

  private Trip trip;
  private User user;
  private Traveler traveler;
  private final String collaboratorEmail = "collaborator@example.com";
  private final String newEmail = "new@example.com";
  private final String token = "test-token";

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("owner@example.com");

    trip = new Trip();
    trip.setId(UUID.randomUUID());
    trip.setInviteToken(token);
    trip.setUser(user);

    traveler = new Traveler();
    traveler.setId(UUID.randomUUID());
    traveler.setTrip(trip);
    traveler.setEmail(collaboratorEmail);
    traveler.setRole(TravelerRole.COLLABORATOR);
  }

  @Test
  void testGetInviteDetails() {
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));

    TripInviteDetailsDTO details = tripInviteService.getInviteDetails(token);

    assertNotNull(details);
    assertEquals(trip.getId().toString(), details.tripId());
    verify(tripRepository).findByInviteToken(token);
  }

  @Test
  void testGetInviteDetails_InvalidToken() {
    String invalidToken = "invalid-token";
    when(tripRepository.findByInviteToken(invalidToken)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> tripInviteService.getInviteDetails(invalidToken));
    verify(tripRepository).findByInviteToken(invalidToken);
  }

  @Test
  void testAcceptInvite_NewTraveler() {
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));
    when(travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), newEmail))
        .thenReturn(Optional.empty());

    TripInviteAcceptanceDTO result = tripInviteService.acceptInvite(token, newEmail, null);

    assertTrue(result.success());
    verify(travelerRepository, times(1)).save(any(Traveler.class));
    verify(tripRepository).findByInviteToken(token);
    verify(travelerRepository).findByTripIdAndEmailIgnoreCase(trip.getId(), newEmail);
  }

  @Test
  void testAcceptInvite_ExistingTraveler() {
    traveler.setTripInviteStatus(TripInviteStatus.PENDING);
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));
    when(travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), collaboratorEmail))
        .thenReturn(Optional.of(traveler));

    TripInviteAcceptanceDTO result = tripInviteService.acceptInvite(token, collaboratorEmail, null);

    assertTrue(result.success());
    assertEquals(TripInviteStatus.ACCEPTED, traveler.getTripInviteStatus());
    verify(travelerRepository, times(1)).save(traveler);
  }

  @Test
  void testAcceptInvite_AlreadyAccepted() {
    traveler.setTripInviteStatus(TripInviteStatus.ACCEPTED);
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));
    when(travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), collaboratorEmail))
        .thenReturn(Optional.of(traveler));

    TripInviteAcceptanceDTO result = tripInviteService.acceptInvite(token, collaboratorEmail, null);

    assertTrue(result.success());
    assertEquals("You have already accepted this invitation", result.message());
    verify(travelerRepository, never()).save(traveler);
  }

  @Test
  void testAcceptInvite_Owner() {
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));

    assertThrows(
        IllegalStateException.class,
        () -> tripInviteService.acceptInvite(token, user.getEmail(), user.getId()));
    verify(travelerRepository, never()).save(any(Traveler.class));
  }

  @Test
  void testRejectInvite() {
    when(tripRepository.findByInviteToken(token)).thenReturn(Optional.of(trip));
    when(travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), collaboratorEmail))
        .thenReturn(Optional.of(traveler));

    TripInviteAcceptanceDTO result = tripInviteService.rejectInvite(token, collaboratorEmail, null);

    assertTrue(result.success());
    assertEquals(TripInviteStatus.REJECTED, traveler.getTripInviteStatus());
    verify(travelerRepository, times(1)).save(traveler);
  }

  @Test
  void testRevokeInvite() {
    when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
    when(travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), collaboratorEmail))
        .thenReturn(Optional.of(traveler));

    tripInviteService.revokeInvite(trip.getId(), collaboratorEmail, user.getId());

    verify(travelerRepository, times(1)).delete(traveler);
  }

  @Test
  void testRevokeInvite_NotOwner() {
    when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
    UUID notOwnerId = UUID.randomUUID();

    assertThrows(
        IllegalStateException.class,
        () -> tripInviteService.revokeInvite(trip.getId(), collaboratorEmail, notOwnerId));

    verify(travelerRepository, never()).delete(any(Traveler.class));
  }
}
