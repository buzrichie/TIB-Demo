package com.amalitech.tib.trip.service;

import com.amalitech.tib.trip.dto.TripInviteAcceptanceDTO;
import com.amalitech.tib.trip.dto.TripInviteDetailsDTO;
import java.util.List;
import java.util.UUID;

public interface TripInviteService {

  void addTripMate(UUID tripId, List<String> emails, UUID ownerId);

  String getInviteLink(UUID tripId, UUID ownerId);

  TripInviteDetailsDTO getInviteDetails(String token);

  TripInviteAcceptanceDTO acceptInvite(String token, String email, UUID userId);

  TripInviteAcceptanceDTO rejectInvite(String token, String email, UUID userId);

  void revokeInvite(UUID tripId, String email, UUID ownerId);
}
