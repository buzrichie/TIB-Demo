package com.amalitech.tib.trip.repository;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class TripRepositoryTest {

  @Autowired private TripRepository tripRepository;

  @Autowired private EntityManager entityManager;

  private User user;
  private Trip trip;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setEmail("test@example.com");
    user.setPassword("password");
    entityManager.persist(user);

    trip = new Trip();
    trip.setTitle("Test Trip");
    trip.setUser(user);
    trip.setInviteToken("test-token");
    tripRepository.save(trip);
  }

  @Test
  @DisplayName("Should find trips by user ID")
  void findByUserId_shouldReturnPagedTrips() {
    Page<Trip> found = tripRepository.findByUser_Id(user.getId(), PageRequest.of(0, 10));
    assertFalse(found.isEmpty());
    assertEquals(1, found.getTotalElements());
    assertEquals(trip.getId(), found.getContent().get(0).getId());
  }

  @Test
  @DisplayName("Should not find trips for a different user ID")
  void findByUserId_shouldNotReturnTrips_forWrongUserId() {
    Page<Trip> found = tripRepository.findByUser_Id(UUID.randomUUID(), PageRequest.of(0, 10));
    assertTrue(found.isEmpty());
  }

  @Test
  @DisplayName("Should find a trip by its invite token")
  void findByInviteToken_shouldReturnTrip() {
    Optional<Trip> found = tripRepository.findByInviteToken("test-token");
    assertTrue(found.isPresent());
    assertEquals(trip.getId(), found.get().getId());
  }

  @Test
  @DisplayName("Should not find a trip for a wrong invite token")
  void findByInviteToken_shouldNotReturnTrip_forWrongToken() {
    Optional<Trip> found = tripRepository.findByInviteToken("wrong-token");
    assertFalse(found.isPresent());
  }
}
