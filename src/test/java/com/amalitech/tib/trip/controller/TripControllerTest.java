package com.amalitech.tib.trip.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amalitech.tib.exception.BadException;
import com.amalitech.tib.exception.GlobalExceptionHandler;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.trip.dto.TripCreationDTO;
import com.amalitech.tib.trip.dto.TripResponseDTO;
import com.amalitech.tib.trip.dto.TripUpdateDTO;
import com.amalitech.tib.trip.service.TripService;
import com.amalitech.tib.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

  @Mock private TripService tripService;

  @InjectMocks private TripController tripController;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private UUID userId;
  private TripResponseDTO tripResponseDTO;

  @BeforeEach
  void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
    mockMvc =
        MockMvcBuilders.standaloneSetup(tripController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    userId = UUID.randomUUID();
    tripResponseDTO =
        new TripResponseDTO(
            UUID.randomUUID().toString(),
            "Test Trip",
            "Destination",
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            2,
            userId.toString(),
            "http://invite.link");
  }

  @Nested
  @DisplayName("POST /api/v1/trips")
  class CreateTripTests {
    @Test
    @DisplayName("Should create trip and return 201 Created")
    void shouldCreateTrip() throws Exception {
      TripCreationDTO creationDTO =
          new TripCreationDTO(
              "Test Trip",
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              null,
              null,
              null,
              Collections.emptyList());

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.createTrip(any(TripCreationDTO.class), eq(userId)))
            .thenReturn(tripResponseDTO);

        mockMvc
            .perform(
                post("/api/v1/trips")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(creationDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("Test Trip"));

        verify(tripService).createTrip(any(TripCreationDTO.class), eq(userId));
      }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Should return 400 Bad Request when title is invalid")
    void shouldReturnBadRequestWhenTitleIsInvalid(String title) throws Exception {
      TripCreationDTO creationDTO =
          new TripCreationDTO(
              title, LocalDate.now(), LocalDate.now().plusDays(1), null, null, null, null);

      mockMvc
          .perform(
              post("/api/v1/trips")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(creationDTO)))
          .andExpect(status().isBadRequest());

      verify(tripService, never()).createTrip(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when startDate is in the past")
    void shouldReturnBadRequestWhenStartDateIsInPast() throws Exception {
      TripCreationDTO creationDTO =
          new TripCreationDTO(
              "Test Trip",
              LocalDate.now().minusDays(1),
              LocalDate.now().plusDays(1),
              null,
              null,
              null,
              null);

      mockMvc
          .perform(
              post("/api/v1/trips")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(creationDTO)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/trips")
  class GetAllTripsTests {
    @Test
    @DisplayName("Should return a list of trips")
    void shouldReturnTripList() throws Exception {
      Page<TripResponseDTO> pagedResponse =
          new PageImpl<>(List.of(tripResponseDTO), PageRequest.of(0, 10), 1);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.getAllTrips(eq(userId), any())).thenReturn(pagedResponse);

        mockMvc
            .perform(get("/api/v1/trips").param("page", "0").param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].title").value("Test Trip"));

        verify(tripService).getAllTrips(eq(userId), any());
      }
    }
  }

  @Nested
  @DisplayName("GET /api/v1/trips/{tripId}")
  class GetTripByIdTests {
    @Test
    @DisplayName("Should return a trip when found and authorized")
    void shouldReturnTrip() throws Exception {
      UUID tripId = UUID.fromString(tripResponseDTO.id());

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.getTripById(tripId, userId)).thenReturn(tripResponseDTO);

        mockMvc
            .perform(get("/api/v1/trips/{tripId}", tripId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(tripId.toString()));

        verify(tripService).getTripById(tripId, userId);
      }
    }

    @Test
    @DisplayName("Should return 404 Not Found when trip does not exist")
    void shouldReturnNotFound() throws Exception {
      UUID tripId = UUID.randomUUID();
      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.getTripById(tripId, userId))
            .thenThrow(new ResourceNotFoundException("Trip not found"));

        mockMvc.perform(get("/api/v1/trips/{tripId}", tripId)).andExpect(status().isNotFound());
      }
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/trips/{tripId}")
  class UpdateTripTests {
    @Test
    @DisplayName("Should return updated trip when successful")
    void shouldReturnUpdatedTrip() throws Exception {
      UUID tripId = UUID.fromString(tripResponseDTO.id());
      TripUpdateDTO updateDTO = new TripUpdateDTO("Updated Trip", null, null, null, null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.updateTrip(eq(tripId), any(TripUpdateDTO.class), eq(userId)))
            .thenReturn(tripResponseDTO);

        mockMvc
            .perform(
                put("/api/v1/trips/{tripId}", tripId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk());

        verify(tripService).updateTrip(eq(tripId), any(TripUpdateDTO.class), eq(userId));
      }
    }

    @Test
    @DisplayName("Should return 400 Bad Request for service-level validation failure")
    void shouldReturnBadRequestForServiceValidation() throws Exception {
      UUID tripId = UUID.randomUUID();
      TripUpdateDTO updateDTO = new TripUpdateDTO(null, null, null, null, null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        when(tripService.updateTrip(eq(tripId), any(TripUpdateDTO.class), eq(userId)))
            .thenThrow(new BadException("Service validation failed"));

        mockMvc
            .perform(
                put("/api/v1/trips/{tripId}", tripId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/trips/{tripId}")
  class DeleteTripTests {
    @Test
    @DisplayName("Should return 200 OK when successful")
    void shouldReturnOk() throws Exception {
      UUID tripId = UUID.randomUUID();

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        doNothing().when(tripService).deleteTrip(tripId, userId);

        mockMvc.perform(delete("/api/v1/trips/{tripId}", tripId)).andExpect(status().isOk());

        verify(tripService).deleteTrip(tripId, userId);
      }
    }

    @Test
    @DisplayName("Should return 404 Not Found when trip does not exist")
    void shouldReturnNotFound() throws Exception {
      UUID tripId = UUID.randomUUID();

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
        doThrow(new ResourceNotFoundException("Trip not found"))
            .when(tripService)
            .deleteTrip(tripId, userId);

        mockMvc.perform(delete("/api/v1/trips/{tripId}", tripId)).andExpect(status().isNotFound());
      }
    }
  }
}
