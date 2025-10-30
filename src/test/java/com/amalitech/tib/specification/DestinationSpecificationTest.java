package com.amalitech.tib.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.destination.model.Destination;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DestinationSpecificationTest {

  @Mock private Root<Destination> root;

  @Mock private CriteriaQuery<?> query;

  @Mock private CriteriaBuilder cb;

  @Mock private Path<String> namePath;

  @Mock private Path<String> countryPath;

  @Mock private Path<String> regionPath;

  @Mock private Path<DestinationStatus> statusPath;

  @Mock private Expression<String> lowerNameExpression;

  @Mock private Expression<String> lowerCountryExpression;

  @Mock private Expression<String> lowerRegionExpression;

  @Mock private Predicate namePredicate;

  @Mock private Predicate countryPredicate;

  @Mock private Predicate regionPredicate;

  @Mock private Predicate statusPredicate;

  @Mock private Predicate conjunctionPredicate;

  @Test
  @DisplayName("Should create specification with all filters when all parameters are provided")
  void filterBy_WithAllParameters_ShouldCombineAllSpecifications() {

    String name = "Paris";
    String country = "France";
    String region = "Île-de-France";
    DestinationStatus status = DestinationStatus.PUBLISHED;

    setupCommonMocks();

    Specification<Destination> spec =
        DestinationSpecification.filterBy(name, country, region, status);

    assertNotNull(spec);

    Predicate result = spec.toPredicate(root, query, cb);
    assertNotNull(result);

    verify(root).get("name");
    verify(root).get("country");
    verify(root).get("region");
    verify(root).get("status");

    verify(cb, times(3)).like(any(Expression.class), anyString());
    verify(cb, times(1)).equal(any(Path.class), eq(status));
  }

  @Test
  @DisplayName("Should create name filter with case-insensitive like query")
  void hasName_WithValidName_ShouldCreateCaseInsensitiveLikePredicate() {

    String name = "Paris";
    String expectedPattern = "%paris%";

    doReturn(namePath).when(root).get("name");
    when(cb.lower(namePath)).thenReturn(lowerNameExpression);
    when(cb.like(lowerNameExpression, expectedPattern)).thenReturn(namePredicate);

    Specification<Destination> spec = DestinationSpecification.filterBy(name, null, null, null);
    Predicate result = spec.toPredicate(root, query, cb);

    assertNotNull(result);
    verify(root).get("name");
    verify(cb).lower(namePath);
    verify(cb).like(lowerNameExpression, expectedPattern);
  }

  @Test
  @DisplayName("Should create country filter with case-insensitive like query")
  void hasCountry_WithValidCountry_ShouldCreateCaseInsensitiveLikePredicate() {
    String country = "France";
    String expectedPattern = "%france%";

    doReturn(countryPath).when(root).get("country");
    when(cb.lower(countryPath)).thenReturn(lowerCountryExpression);
    when(cb.like(lowerCountryExpression, expectedPattern)).thenReturn(countryPredicate);

    Specification<Destination> spec = DestinationSpecification.filterBy(null, country, null, null);
    Predicate result = spec.toPredicate(root, query, cb);

    assertNotNull(result);
    verify(root).get("country");
    verify(cb).lower(countryPath);
    verify(cb).like(lowerCountryExpression, expectedPattern);
  }

  @Test
  @DisplayName("Should create region filter with case-insensitive like query")
  void hasRegion_WithValidRegion_ShouldCreateCaseInsensitiveLikePredicate() {
    String region = "Île-de-France";
    String expectedPattern = "%île-de-france%";

    doReturn(regionPath).when(root).get("region");
    when(cb.lower(regionPath)).thenReturn(lowerRegionExpression);
    when(cb.like(lowerRegionExpression, expectedPattern)).thenReturn(regionPredicate);

    Specification<Destination> spec = DestinationSpecification.filterBy(null, null, region, null);
    Predicate result = spec.toPredicate(root, query, cb);

    assertNotNull(result);
    verify(root).get("region");
    verify(cb).lower(regionPath);
    verify(cb).like(lowerRegionExpression, expectedPattern);
  }

  @Test
  @DisplayName("Should create status filter with equal predicate")
  void hasStatus_WithValidStatus_ShouldCreateEqualPredicate() {
    DestinationStatus status = DestinationStatus.PUBLISHED;

    doReturn(statusPath).when(root).get("status");
    when(cb.equal(statusPath, status)).thenReturn(statusPredicate);

    Specification<Destination> spec = DestinationSpecification.filterBy(null, null, null, status);
    Predicate result = spec.toPredicate(root, query, cb);

    assertNotNull(result);
    verify(root).get("status");
    verify(cb).equal(statusPath, status);
  }

  @Test
  @DisplayName("Should handle all status enum values correctly")
  void hasStatus_WithAllStatusValues_ShouldCreateEqualPredicate() {
    for (DestinationStatus status : DestinationStatus.values()) {
      reset(root, cb);
      doReturn(statusPath).when(root).get("status");
      when(cb.equal(statusPath, status)).thenReturn(statusPredicate);

      Specification<Destination> spec = DestinationSpecification.filterBy(null, null, null, status);
      Predicate result = spec.toPredicate(root, query, cb);

      assertNotNull(result);
      verify(cb).equal(statusPath, status);
    }
  }

  private void setupCommonMocks() {

    lenient().doReturn(namePath).when(root).get("name");
    lenient().doReturn(countryPath).when(root).get("country");
    lenient().doReturn(regionPath).when(root).get("region");
    lenient().doReturn(statusPath).when(root).get("status");

    lenient().when(cb.lower(namePath)).thenReturn(lowerNameExpression);
    lenient().when(cb.lower(countryPath)).thenReturn(lowerCountryExpression);
    lenient().when(cb.lower(regionPath)).thenReturn(lowerRegionExpression);

    lenient().when(cb.like(any(Expression.class), anyString())).thenReturn(namePredicate);
    lenient().when(cb.equal(any(Expression.class), any())).thenReturn(statusPredicate);
    lenient().when(cb.conjunction()).thenReturn(conjunctionPredicate);
  }
}
