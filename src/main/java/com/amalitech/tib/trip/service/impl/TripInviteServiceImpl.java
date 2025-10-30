package com.amalitech.tib.trip.service.impl;

import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.trip.dto.TripInviteAcceptanceDTO;
import com.amalitech.tib.trip.dto.TripInviteDetailsDTO;
import com.amalitech.tib.trip.enums.TravelerRole;
import com.amalitech.tib.trip.enums.TripInviteStatus;
import com.amalitech.tib.trip.model.Traveler;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.trip.repository.TravelerRepository;
import com.amalitech.tib.trip.repository.TripRepository;
import com.amalitech.tib.trip.service.TripInviteService;
import com.amalitech.tib.trip.service.TripMailService;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.util.TripInviteLinkBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the TripInviteService interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripInviteServiceImpl implements TripInviteService {

  private final TripRepository tripRepository;
  private final TravelerRepository travelerRepository;
  private final UserRepository userRepository;
  private final TripInviteLinkBuilder tripInviteLinkBuilder;
  private final TripMailService tripMailService;

  @PersistenceContext private EntityManager entityManager;

  @Override
  @Transactional
  public void addTripMate(UUID tripId, List<String> emails, UUID ownerId) {
    Trip trip = findTripById(tripId);
    validateTripOwner(trip, ownerId);

    for (String email : emails) {
      Optional<Traveler> existingTraveler =
          travelerRepository.findByTripIdAndEmailIgnoreCase(tripId, email);
      if (existingTraveler.isPresent()) {
        log.warn("Attempted to add existing traveler with email: {}", email);
        continue;
      }

      Traveler newTraveler =
          buildTraveler(trip, email, TravelerRole.COLLABORATOR, TripInviteStatus.PENDING);
      travelerRepository.save(newTraveler);
      log.info("Added new traveler with email: {}", email);
      String inviteLink = getInviteLink(tripId, ownerId);
      tripMailService.sendTripInvite(email, trip.getTitle(), inviteLink);
    }
  }

  @Override
  public String getInviteLink(UUID tripId, UUID ownerId) {
    Trip trip = findTripById(tripId);
    validateTripOwner(trip, ownerId);

    if (trip.getInviteToken() == null || trip.getInviteToken().isEmpty()) {
      trip.setInviteToken(UUID.randomUUID().toString());
      tripRepository.save(trip);
    }

    return tripInviteLinkBuilder.build(trip.getInviteToken());
  }

  @Override
  public TripInviteDetailsDTO getInviteDetails(String token) {
    Trip trip = findTripByToken(token);

    return new TripInviteDetailsDTO(
        trip.getId().toString(),
        trip.getTitle(),
        trip.getDestination() != null ? trip.getDestination().getName() : null,
        trip.getStartDate(),
        trip.getEndDate(),
        trip.getTravelersCount(),
        trip.getUser().getEmail(),
        trip.getCreatedAt());
  }

  @Override
  @Transactional
  public TripInviteAcceptanceDTO acceptInvite(String token, String email, UUID userId) {
    Trip trip = findTripByToken(token);
    validateNotTripOwner(trip, email);

    Optional<Traveler> existingByEmail =
        travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), email);

    if (existingByEmail.isPresent()) {
      return handleExistingTravelerByEmail(existingByEmail.get(), email, userId, trip);
    }

    if (userId != null) {
      Optional<Traveler> existingByUserId =
          travelerRepository.findByTripIdAndUserId(trip.getId(), userId);

      if (existingByUserId.isPresent()) {
        return handleExistingTravelerByUserId(existingByUserId.get(), email, userId, trip);
      }
    }

    return createNewTraveler(trip, email, userId, TripInviteStatus.ACCEPTED);
  }

  @Override
  @Transactional
  public TripInviteAcceptanceDTO rejectInvite(String token, String email, UUID userId) {
    Trip trip = findTripByToken(token);
    validateNotTripOwnerForRejection(trip, email);

    Optional<Traveler> travelerOpt = findTravelerByEmailOrUserId(trip.getId(), email, userId);

    if (travelerOpt.isPresent()) {
      updateTravelerStatus(travelerOpt.get(), email, userId, TripInviteStatus.REJECTED);
    } else {
      createTravelerWithStatus(trip, email, userId, TripInviteStatus.REJECTED);
    }

    log.info("Email {} rejected invite for trip {} (userId: {})", email, trip.getId(), userId);
    return new TripInviteAcceptanceDTO(
        true, "Invitation rejected", trip.getId().toString(), trip.getTitle());
  }

  @Override
  @Transactional
  public void revokeInvite(UUID tripId, String email, UUID ownerId) {
    Trip trip = findTripById(tripId);
    validateTripOwner(trip, ownerId);

    Traveler traveler = findTravelerByEmail(tripId, email);
    validateNotOwnerRole(traveler);

    travelerRepository.delete(traveler);
    log.info("Owner {} revoked invite for {} on trip {}", ownerId, email, tripId);
  }

  /**
   * Finds a trip by its invitation token.
   *
   * @param token The invitation token.
   * @return The trip associated with the token.
   * @throws ResourceNotFoundException if the token is invalid or expired.
   */
  private Trip findTripByToken(String token) {
    return tripRepository
        .findByInviteToken(token)
        .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invite token"));
  }

  /**
   * Finds a trip by its ID.
   *
   * @param tripId The ID of the trip.
   * @return The trip.
   * @throws ResourceNotFoundException if the trip is not found.
   */
  private Trip findTripById(UUID tripId) {
    return tripRepository
        .findById(tripId)
        .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
  }

  /**
   * Finds a traveler by their email for a specific trip.
   *
   * @param tripId The ID of the trip.
   * @param email The email of the traveler.
   * @return The traveler.
   * @throws ResourceNotFoundException if no invitation is found for the email.
   */
  private Traveler findTravelerByEmail(UUID tripId, String email) {
    return travelerRepository
        .findByTripIdAndEmailIgnoreCase(tripId, email)
        .orElseThrow(() -> new ResourceNotFoundException("No invitation found for " + email));
  }

  /**
   * Finds a traveler by email or user ID for a specific trip.
   *
   * @param tripId The ID of the trip.
   * @param email The email of the traveler.
   * @param userId The user ID of the traveler.
   * @return An optional containing the traveler if found.
   */
  private Optional<Traveler> findTravelerByEmailOrUserId(UUID tripId, String email, UUID userId) {
    Optional<Traveler> travelerOpt =
        travelerRepository.findByTripIdAndEmailIgnoreCase(tripId, email);

    if (travelerOpt.isEmpty() && userId != null) {
      travelerOpt = travelerRepository.findByTripIdAndUserId(tripId, userId);
    }

    return travelerOpt;
  }

  /**
   * Validates that the user is not the owner of the trip.
   *
   * @param trip The trip.
   * @param email The email of the user.
   * @throws IllegalStateException if the user is the owner of the trip.
   */
  private void validateNotTripOwner(Trip trip, String email) {
    if (trip.getUser().getEmail().equalsIgnoreCase(email)) {
      throw new IllegalStateException("You are the owner of this trip");
    }
  }

  /**
   * Validates that the user is not the owner of the trip when rejecting an invitation.
   *
   * @param trip The trip.
   * @param email The email of the user.
   * @throws IllegalStateException if the user is the owner of the trip.
   */
  private void validateNotTripOwnerForRejection(Trip trip, String email) {
    if (trip.getUser().getEmail().equalsIgnoreCase(email)) {
      throw new IllegalStateException("Trip owner cannot reject their own trip");
    }
  }

  /**
   * Validates that the user is the owner of the trip.
   *
   * @param trip The trip.
   * @param ownerId The ID of the user.
   * @throws IllegalStateException if the user is not the owner of the trip.
   */
  private void validateTripOwner(Trip trip, UUID ownerId) {
    if (!trip.getUser().getId().equals(ownerId)) {
      throw new IllegalStateException("Only the trip owner can revoke invitations");
    }
  }

  /**
   * Validates that the traveler is not the owner of the trip.
   *
   * @param traveler The traveler.
   * @throws IllegalStateException if the traveler is the owner of the trip.
   */
  private void validateNotOwnerRole(Traveler traveler) {
    if (traveler.getRole() == TravelerRole.OWNER) {
      throw new IllegalStateException("Cannot revoke owner's access");
    }
  }

  /**
   * Validates the traveler's role and throws an exception if they are an owner.
   *
   * @param traveler The traveler to check.
   * @param message The exception message to throw if the traveler is an owner.
   * @throws IllegalStateException if the traveler's role is OWNER.
   */
  private void validateOwnerRoleAndThrow(Traveler traveler, String message) {
    if (traveler.getRole() == TravelerRole.OWNER) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * Validates that the user's email matches the provided email.
   *
   * @param user The user.
   * @param email The email to match.
   * @throws IllegalStateException if the emails do not match.
   */
  private void validateEmailMatch(User user, String email) {
    if (!user.getEmail().equalsIgnoreCase(email)) {
      throw new IllegalStateException(
          "Email mismatch: authenticated user email does not match provided email");
    }
  }

  /**
   * Handles an existing traveler who is accepting an invitation via email.
   *
   * @param traveler The existing traveler.
   * @param email The email of the user.
   * @param userId The ID of the user.
   * @param trip The trip.
   * @return The result of the invitation acceptance.
   */
  private TripInviteAcceptanceDTO handleExistingTravelerByEmail(
      Traveler traveler, String email, UUID userId, Trip trip) {

    validateOwnerRoleAndThrow(traveler, "You are the owner of this trip");

    if (traveler.getTripInviteStatus() == TripInviteStatus.ACCEPTED) {
      log.info("Email {} already accepted invite for trip {}", email, trip.getId());
      return createAlreadyAcceptedResponse(trip);
    }

    traveler.setTripInviteStatus(TripInviteStatus.ACCEPTED);
    linkUserToTraveler(traveler, userId, email);
    travelerRepository.save(traveler);

    log.info("Email {} accepted invite for trip {} (userId: {})", email, trip.getId(), userId);
    return createSuccessResponse(trip);
  }

  /**
   * Handles an existing traveler who is accepting an invitation via user ID.
   *
   * @param traveler The existing traveler.
   * @param email The email of the user.
   * @param userId The ID of the user.
   * @param trip The trip.
   * @return The result of the invitation acceptance.
   */
  private TripInviteAcceptanceDTO handleExistingTravelerByUserId(
      Traveler traveler, String email, UUID userId, Trip trip) {

    validateOwnerRoleAndThrow(traveler, "You are the owner of this trip");

    if (traveler.getTripInviteStatus() == TripInviteStatus.ACCEPTED) {
      log.info("User {} already accepted invite for trip {}", userId, trip.getId());
      return createAlreadyAcceptedResponse(trip);
    }

    traveler.setTripInviteStatus(TripInviteStatus.ACCEPTED);
    traveler.setEmail(email);
    travelerRepository.save(traveler);

    log.info("User {} accepted invite for trip {}", userId, trip.getId());
    return createSuccessResponse(trip);
  }

  /**
   * Creates a new traveler for a trip.
   *
   * @param trip The trip.
   * @param email The email of the new traveler.
   * @param userId The ID of the new traveler.
   * @param status The status of the invitation.
   * @return The result of the invitation acceptance.
   */
  private TripInviteAcceptanceDTO createNewTraveler(
      Trip trip, String email, UUID userId, TripInviteStatus status) {

    Traveler newTraveler = buildTraveler(trip, email, TravelerRole.COLLABORATOR, status);
    linkUserToTraveler(newTraveler, userId, email);
    travelerRepository.save(newTraveler);

    log.info("Email {} joined trip {} via invite link (userId: {})", email, trip.getId(), userId);
    return new TripInviteAcceptanceDTO(
        true, "You have successfully joined the trip!", trip.getId().toString(), trip.getTitle());
  }

  /**
   * Creates a new traveler with a specific status.
   *
   * @param trip The trip.
   * @param email The email of the new traveler.
   * @param userId The ID of the new traveler.
   * @param status The status of the invitation.
   */
  private void createTravelerWithStatus(
      Trip trip, String email, UUID userId, TripInviteStatus status) {

    Traveler newTraveler = buildTraveler(trip, email, TravelerRole.COLLABORATOR, status);
    linkUserToTraveler(newTraveler, userId, email);
    travelerRepository.save(newTraveler);
  }

  /**
   * Updates the status of a traveler.
   *
   * @param traveler The traveler to update.
   * @param email The email of the user.
   * @param userId The ID of the user.
   * @param status The new status.
   */
  private void updateTravelerStatus(
      Traveler traveler, String email, UUID userId, TripInviteStatus status) {

    validateOwnerRoleAndThrow(traveler, "Trip owner cannot reject their own trip");

    traveler.setTripInviteStatus(status);
    traveler.setEmail(email);
    linkUserToTraveler(traveler, userId, email);
    travelerRepository.save(traveler);
  }

  /**
   * Builds a new traveler object.
   *
   * @param trip The trip.
   * @param email The email of the traveler.
   * @param role The role of the traveler.
   * @param status The status of the invitation.
   * @return The new traveler object.
   */
  private Traveler buildTraveler(
      Trip trip, String email, TravelerRole role, TripInviteStatus status) {

    Traveler traveler = new Traveler();
    traveler.setTrip(trip);
    traveler.setEmail(email);
    traveler.setRole(role);
    traveler.setTripInviteStatus(status);
    return traveler;
  }

  /**
   * Links a user to a traveler.
   *
   * @param traveler The traveler.
   * @param userId The ID of the user.
   * @param email The email of the user.
   */
  private void linkUserToTraveler(Traveler traveler, UUID userId, String email) {
    if (userId != null) {
      User user = findUserById(userId);
      validateEmailMatch(user, email);
      traveler.setUser(user);
    }
  }

  /**
   * Finds a user by their ID.
   *
   * @param userId The ID of the user.
   * @return The user.
   * @throws ResourceNotFoundException if the user is not found.
   */
  private User findUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  /**
   * Creates a response for an invitation that has already been accepted.
   *
   * @param trip The trip.
   * @return The response.
   */
  private TripInviteAcceptanceDTO createAlreadyAcceptedResponse(Trip trip) {
    return new TripInviteAcceptanceDTO(
        true,
        "You have already accepted this invitation",
        trip.getId().toString(),
        trip.getTitle());
  }

  /**
   * Creates a success response for an invitation acceptance.
   *
   * @param trip The trip.
   * @return The response.
   */
  private TripInviteAcceptanceDTO createSuccessResponse(Trip trip) {
    return new TripInviteAcceptanceDTO(
        true, "Invitation accepted successfully!", trip.getId().toString(), trip.getTitle());
  }
}
