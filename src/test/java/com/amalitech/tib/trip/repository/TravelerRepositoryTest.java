package com.amalitech.tib.trip.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.amalitech.tib.trip.model.Traveler;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.user.model.User;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TravelerRepositoryTest {

  @Autowired private TravelerRepository travelerRepository;

  @Autowired private EntityManager entityManager;

  private Trip trip;
  private User user;
  private Traveler traveler;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    entityManager.persist(user);

    trip = new Trip();
    trip.setTitle("Test Trip");
    entityManager.persist(trip);

    traveler = new Traveler();
    traveler.setTrip(trip);
    traveler.setUser(user);
    traveler.setEmail("test@example.com");
    travelerRepository.save(traveler);
  }

  @Test
  @DisplayName("Should find traveler by trip ID and user ID")
  void findByTripIdAndUserId_shouldReturnTraveler() {
    Optional<Traveler> found = travelerRepository.findByTripIdAndUserId(trip.getId(), user.getId());
    assertTrue(found.isPresent());
    assertEquals(traveler.getId(), found.get().getId());
  }

  @Test
  @DisplayName("Should not find traveler for wrong trip ID")
  void findByTripIdAndUserId_shouldNotReturnTraveler_forWrongTripId() {
    Optional<Traveler> found =
        travelerRepository.findByTripIdAndUserId(UUID.randomUUID(), user.getId());
    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should find traveler by trip ID and email, ignoring case")
  void findByTripIdAndEmailIgnoreCase_shouldReturnTraveler() {
    Optional<Traveler> found =
        travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), "TEST@EXAMPLE.COM");
    assertTrue(found.isPresent());
    assertEquals(traveler.getId(), found.get().getId());
  }

  @Test
  @DisplayName("Should not find traveler for wrong email")
  void findByTripIdAndEmailIgnoreCase_shouldNotReturnTraveler_forWrongEmail() {
    Optional<Traveler> found =
        travelerRepository.findByTripIdAndEmailIgnoreCase(trip.getId(), "wrong@example.com");
    assertFalse(found.isPresent());
  }
}
