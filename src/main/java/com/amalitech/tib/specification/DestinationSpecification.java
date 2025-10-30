package com.amalitech.tib.specification;

import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.model.Destination;
import org.springframework.data.jpa.domain.Specification;

public class DestinationSpecification {

  public static Specification<Destination> filterBy(
      String name, String country, String region, DestinationStatus status) {
    return Specification.<Destination>unrestricted()
        .and(hasName(name))
        .and(hasCountry(country))
        .and(hasRegion(region))
        .and(hasStatus(status));
  }

  private static Specification<Destination> hasName(String name) {
    return (root, query, cb) ->
        (name == null || name.isBlank())
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
  }

  private static Specification<Destination> hasCountry(String country) {
    return (root, query, cb) ->
        (country == null || country.isBlank())
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%");
  }

  private static Specification<Destination> hasRegion(String region) {
    return (root, query, cb) ->
        (region == null || region.isBlank())
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("region")), "%" + region.toLowerCase() + "%");
  }

  private static Specification<Destination> hasStatus(DestinationStatus status) {
    return (root, query, cb) ->
        (status == null) ? cb.conjunction() : cb.equal(root.get("status"), status);
  }
}
