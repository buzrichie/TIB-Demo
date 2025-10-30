package com.amalitech.tib.config.initializer;

import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.model.Destination;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DestinationDataInitializer implements CommandLineRunner {

  private final EntityManager entityManager;

  public DestinationDataInitializer(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void run(String... args) {
    log.info("Checking if destinations need to be initialized...");
    // Check if destinations already exist
    Long count =
        (Long) entityManager.createQuery("SELECT COUNT(d) FROM Destination d").getSingleResult();
    log.info("Found {} existing destinations", count);

    if (count == 0) {
      log.info("No destinations found. Creating default destinations...");

      createDestination(
          "Accra",
          "Ghana",
          "Africa",
          "https://unsplash.com/photos/white-concrete-building-with-flag-on-top-during-daytime--CgUhaShACE",
          7.9527706f,
          -1.0307118f,
          "Capital City of The Republic of Ghana",
          DestinationStatus.PUBLISHED);

      createDestination(
          "Paris",
          "France",
          "Europe",
          "https://images.unsplash.com/photo-1502602898657-3e91760cbb34",
          48.8566f,
          2.3522f,
          "Paris, France's capital, is a major European city and a global center for art, fashion, gastronomy and culture.",
          DestinationStatus.PUBLISHED);

      createDestination(
          "Tokyo",
          "Japan",
          "Asia",
          "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf",
          35.6762f,
          139.6503f,
          "Tokyo, Japan's busy capital, mixes the ultramodern and the traditional, from neon-lit skyscrapers to historic temples.",
          DestinationStatus.PUBLISHED);

      createDestination(
          "Cape Coast",
          "Ghana",
          "Africa",
          "https://images.unsplash.com/photo-1580060839134-75a5edca2e99",
          5.1315f,
          -1.2795f,
          "Cape Coast is a city in Ghana known for its beautiful beaches, historic castle, and rich cultural heritage.",
          DestinationStatus.PUBLISHED);

      createDestination(
          "New York City",
          "United States",
          "North America",
          "https://images.unsplash.com/photo-1522083165195-3424ed129620",
          40.7128f,
          -74.0060f,
          "New York City comprises 5 boroughs sitting where the Hudson River meets the Atlantic Ocean, and is known for its iconic landmarks.",
          DestinationStatus.PUBLISHED);

      createDestination(
          "Sydney",
          "Australia",
          "Oceania",
          "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9",
          -33.8688f,
          151.2093f,
          "Sydney, capital of New South Wales and one of Australia's largest cities, is best known for its Opera House and Harbour Bridge.",
          DestinationStatus.PUBLISHED);
    } else {
      log.info("Destinations already exist. Skipping initialization.");
    }
  }

  private void createDestination(
      String name,
      String country,
      String region,
      String imageUrl,
      Float latitude,
      Float longitude,
      String description,
      DestinationStatus status) {
    Destination destination = new Destination();
    destination.setName(name);
    destination.setCountry(country);
    destination.setRegion(region);
    destination.setImageUrl(imageUrl);
    destination.setLatitude(latitude);
    destination.setLongitude(longitude);
    destination.setDescription(description);
    destination.setStatus(status);

    entityManager.persist(destination);
    log.info("Created destination: {} in {}", name, country);
  }
}
