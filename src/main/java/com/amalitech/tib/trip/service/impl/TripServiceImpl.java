package com.amalitech.tib.trip.service.impl;

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
import com.amalitech.tib.trip.service.TripService;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.util.TripInviteLinkBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the TripService interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {
  private final TripRepository tripRepository;
  private final TravelerRepository travelerRepository;
  private final UserRepository userRepository;
  private final TripMailService tripMailService;
  private final TripInviteLinkBuilder tripInviteLinkBuilder;

  @PersistenceContext private EntityManager entityManager;

  @Override
  @Transactional
  public TripResponseDTO createTrip(TripCreationDTO tripCreationDTO, UUID userId) {
    log.info("Creating trip for user: {}", userId);

    Trip trip = new Trip();
    trip.setTitle(tripCreationDTO.title());
    trip.setStartDate(tripCreationDTO.startDate());
    trip.setEndDate(tripCreationDTO.endDate());

    User userRef = entityManager.getReference(User.class, userId);
    trip.setUser(userRef);

    Destination destination =
        resolveDestination(tripCreationDTO.destinationId(), tripCreationDTO.destination());
    trip.setDestination(destination);

    String inviteToken = UUID.randomUUID().toString();
    trip.setInviteToken(inviteToken);

    Set<String> uniqueInvitees = new HashSet<>();
    if (tripCreationDTO.inviteeEmails() != null) {
      for (String email : tripCreationDTO.inviteeEmails()) {
        if (email != null && !email.isBlank()) {
          uniqueInvitees.add(email.toLowerCase().trim());
        }
      }
    }

    Integer travelersProvided = tripCreationDTO.travelers();
    int travelersCount = travelersProvided != null ? travelersProvided : 1 + uniqueInvitees.size();
    if (travelersCount < 1) travelersCount = 1;
    trip.setTravelersCount(travelersCount);

    Trip savedTrip = tripRepository.save(trip);
    log.info("Trip created with ID: {}", savedTrip.getId());

    List<Traveler> toPersist = new ArrayList<>();

    Traveler owner = new Traveler();
    owner.setTrip(savedTrip);
    owner.setUser(userRef);
    owner.setEmail(userRef.getEmail());
    owner.setRole(TravelerRole.OWNER);
    owner.setTripInviteStatus(TripInviteStatus.ACCEPTED);
    toPersist.add(owner);

    String link = tripInviteLinkBuilder.build(inviteToken);
    int successfulInvites = 0;
    int failedInvites = 0;

    for (String email : uniqueInvitees) {
      Traveler t = new Traveler();
      t.setTrip(savedTrip);
      t.setRole(TravelerRole.COLLABORATOR);
      t.setEmail(email);
      t.setTripInviteStatus(TripInviteStatus.PENDING);

      userRepository.findByEmail(email).ifPresent(t::setUser);
      toPersist.add(t);

      try {
        tripMailService.sendTripInvite(email, savedTrip.getTitle(), link);
        successfulInvites++;
        log.info("Invite sent successfully to: {}", email);
      } catch (Exception e) {
        failedInvites++;
        log.error("Failed to send invite to {}: {}", email, e.getMessage());
      }
    }

    if (!toPersist.isEmpty()) {
      travelerRepository.saveAll(toPersist);
      log.info("Created {} traveler records", toPersist.size());
    }

    log.info(
        "Trip creation complete. Invites sent: {}, failed: {}", successfulInvites, failedInvites);

    return toTripResponseDTO(savedTrip);
  }

  @Override
  public Page<TripResponseDTO> getAllTrips(UUID userId, Pageable pageable) {
    Page<Trip> trips = tripRepository.findByUser_Id(userId, pageable);
    return trips.map(this::toTripResponseDTO);
  }

  @Override
  public TripResponseDTO getTripById(UUID tripId, UUID userId) {
    Trip trip =
        tripRepository
            .findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

    travelerRepository
        .findByTripIdAndUserId(tripId, userId)
        .orElseThrow(() -> new BadException("You are not authorized to view this trip."));

    return toTripResponseDTO(trip);
  }

  @Override
  @Transactional
  public TripResponseDTO updateTrip(UUID tripId, TripUpdateDTO tripUpdateDTO, UUID userId) {
    Trip trip =
        tripRepository
            .findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

    if (!trip.getUser().getId().equals(userId)) {
      throw new BadException("You are not authorized to update this trip.");
    }

    if (tripUpdateDTO.startDate() != null || tripUpdateDTO.endDate() != null) {
      var newStart =
          tripUpdateDTO.startDate() != null ? tripUpdateDTO.startDate() : trip.getStartDate();
      var newEnd = tripUpdateDTO.endDate() != null ? tripUpdateDTO.endDate() : trip.getEndDate();
      if (newStart != null && newEnd != null && newEnd.isBefore(newStart)) {
        throw new BadException("End date must not be before start date.");
      }
    }

    if (tripUpdateDTO.title() != null) {
      trip.setTitle(tripUpdateDTO.title());
    }
    if (tripUpdateDTO.startDate() != null) {
      trip.setStartDate(tripUpdateDTO.startDate());
    }
    if (tripUpdateDTO.endDate() != null) {
      trip.setEndDate(tripUpdateDTO.endDate());
    }

    if (tripUpdateDTO.destinationId() != null || tripUpdateDTO.destination() != null) {
      Destination destination =
          resolveDestination(tripUpdateDTO.destinationId(), tripUpdateDTO.destination());
      trip.setDestination(destination);
    }

    if (tripUpdateDTO.travelers() != null) {
      int count = Math.max(1, tripUpdateDTO.travelers());
      trip.setTravelersCount(count);
    }

    Trip updatedTrip = tripRepository.save(trip);
    log.info("Trip updated: {}", tripId);
    return toTripResponseDTO(updatedTrip);
  }

  @Override
  @Transactional
  public void deleteTrip(UUID tripId, UUID userId) {
    log.info("Attempting to delete trip: {} by user: {}", tripId, userId);

    Trip trip =
        tripRepository
            .findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

    if (!trip.getUser().getId().equals(userId)) {
      log.warn("User {} attempted to delete trip {} without authorization", userId, tripId);
      throw new BadException("You are not authorized to delete this trip.");
    }

    try {
      tripRepository.delete(trip);
      log.info("Trip {} successfully deleted by user {}", tripId, userId);
    } catch (Exception e) {
      log.error("Error deleting trip {}: {}", tripId, e.getMessage(), e);
      throw new BadException("Failed to delete trip. Please try again.");
    }
  }

  private TripResponseDTO toTripResponseDTO(Trip trip) {
    return new TripResponseDTO(
        trip.getId().toString(),
        trip.getTitle(),
        trip.getDestination() != null ? trip.getDestination().getName() : null,
        trip.getStartDate(),
        trip.getEndDate(),
        trip.getTravelersCount() != null ? trip.getTravelersCount() : 0,
        trip.getUser().getId().toString(),
        tripInviteLinkBuilder.build(trip.getInviteToken()));
  }

  private Destination resolveDestination(String destinationId, String destinationName) {
    if (destinationId != null && !destinationId.isBlank()) {
      try {
        UUID destId = UUID.fromString(destinationId);
        return entityManager.getReference(Destination.class, destId);
      } catch (IllegalArgumentException ex) {
        throw new ResourceNotFoundException("Invalid destinationId format: " + destinationId);
      }
    } else if (destinationName != null && !destinationName.isBlank()) {
      List<UUID> ids =
          entityManager
              .createQuery(
                  "SELECT d.id FROM Destination d WHERE LOWER(d.name) = LOWER(:name)", UUID.class)
              .setParameter("name", destinationName.trim())
              .setMaxResults(1)
              .getResultList();
      if (ids.isEmpty()) {
        throw new ResourceNotFoundException("Destination not found with name: " + destinationName);
      }
      return entityManager.getReference(Destination.class, ids.get(0));
    } else {
      throw new ResourceNotFoundException("Destination must be provided by id or name.");
    }
  }
}
