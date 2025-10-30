package com.amalitech.tib.trip.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amalitech.tib.exception.GlobalExceptionHandler;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.trip.dto.TripInviteAcceptanceDTO;
import com.amalitech.tib.trip.dto.TripInviteDetailsDTO;
import com.amalitech.tib.trip.dto.TripInviteRequestDTO;
import com.amalitech.tib.trip.service.TripInviteService;
import com.amalitech.tib.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TripInviteControllerTest {

  @Mock private TripInviteService tripInviteService;

  @InjectMocks private TripInviteController tripInviteController;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
    mockMvc =
        MockMvcBuilders.standaloneSetup(tripInviteController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Nested
  @DisplayName("GET /api/v1/trips/invite")
  class PreviewInvite {
    @Test
    @DisplayName("Should return details for a valid token")
    void shouldReturnDetailsForValidToken() throws Exception {
      String token = "valid-token";
      TripInviteDetailsDTO details =
          new TripInviteDetailsDTO(null, "Trip", null, null, null, 0, null, null);
      when(tripInviteService.getInviteDetails(eq(token))).thenReturn(details);

      mockMvc.perform(get("/api/v1/trips/invite").param("token", token)).andExpect(status().isOk());
      verify(tripInviteService).getInviteDetails(eq(token));
    }

    @Test
    @DisplayName("Should return 404 Not Found for an invalid token")
    void shouldReturnNotFoundForInvalidToken() throws Exception {
      String token = "invalid-token";
      when(tripInviteService.getInviteDetails(eq(token)))
          .thenThrow(new ResourceNotFoundException("Invalid"));

      mockMvc
          .perform(get("/api/v1/trips/invite").param("token", token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/trips/invite/{token}/accept")
  class AcceptInvite {
    private final String token = "test-token";

    @Test
    @DisplayName("Should succeed for an authenticated user")
    void shouldSucceedForAuthenticatedUser() throws Exception {
      UUID userId = UUID.randomUUID();
      TripInviteRequestDTO request = new TripInviteRequestDTO("test@example.com");
      TripInviteAcceptanceDTO response = new TripInviteAcceptanceDTO(true, "Accepted", null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(userId);
        when(tripInviteService.acceptInvite(eq(token), eq(request.email()), eq(userId)))
            .thenReturn(response);

        mockMvc
            .perform(
                post("/api/v1/trips/invite/{token}/accept", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tripInviteService).acceptInvite(eq(token), eq(request.email()), eq(userId));
      }
    }

    @Test
    @DisplayName("Should succeed for an unauthenticated user")
    void shouldSucceedForUnauthenticatedUser() throws Exception {
      TripInviteRequestDTO request = new TripInviteRequestDTO("test@example.com");
      TripInviteAcceptanceDTO response = new TripInviteAcceptanceDTO(true, "Accepted", null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        when(tripInviteService.acceptInvite(eq(token), eq(request.email()), isNull()))
            .thenReturn(response);

        mockMvc
            .perform(
                post("/api/v1/trips/invite/{token}/accept", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tripInviteService).acceptInvite(eq(token), eq(request.email()), isNull());
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email"})
    @DisplayName("Should return 400 Bad Request for invalid email")
    void shouldReturnBadRequestForInvalidEmail(String email) throws Exception {
      TripInviteRequestDTO request = new TripInviteRequestDTO(email);
      mockMvc
          .perform(
              post("/api/v1/trips/invite/{token}/accept", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
      verify(tripInviteService, never()).acceptInvite(any(), any(), any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/trips/invite/{token}/reject")
  class RejectInvite {
    private final String token = "test-token";

    @Test
    @DisplayName("Should succeed for an authenticated user")
    void shouldSucceedForAuthenticatedUser() throws Exception {
      UUID userId = UUID.randomUUID();
      TripInviteRequestDTO request = new TripInviteRequestDTO("test@example.com");
      TripInviteAcceptanceDTO response = new TripInviteAcceptanceDTO(true, "Rejected", null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(userId);
        when(tripInviteService.rejectInvite(eq(token), eq(request.email()), eq(userId)))
            .thenReturn(response);

        mockMvc
            .perform(
                post("/api/v1/trips/invite/{token}/reject", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tripInviteService).rejectInvite(eq(token), eq(request.email()), eq(userId));
      }
    }

    @Test
    @DisplayName("Should succeed for an unauthenticated user")
    void shouldSucceedForUnauthenticatedUser() throws Exception {
      TripInviteRequestDTO request = new TripInviteRequestDTO("test@example.com");
      TripInviteAcceptanceDTO response = new TripInviteAcceptanceDTO(true, "Rejected", null, null);

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserIdOrNull).thenReturn(null);
        when(tripInviteService.rejectInvite(eq(token), eq(request.email()), isNull()))
            .thenReturn(response);

        mockMvc
            .perform(
                post("/api/v1/trips/invite/{token}/reject", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tripInviteService).rejectInvite(eq(token), eq(request.email()), isNull());
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email"})
    @DisplayName("Should return 400 Bad Request for invalid email")
    void shouldReturnBadRequestForInvalidEmail(String email) throws Exception {
      TripInviteRequestDTO request = new TripInviteRequestDTO(email);
      mockMvc
          .perform(
              post("/api/v1/trips/invite/{token}/reject", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
      verify(tripInviteService, never()).rejectInvite(any(), any(), any());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/trips/{tripId}/invites")
  class RevokeInvite {
    @Test
    @DisplayName("Should succeed for the trip owner")
    void shouldSucceedForOwner() throws Exception {
      UUID ownerId = UUID.randomUUID();
      UUID tripId = UUID.randomUUID();
      String email = "test@example.com";

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(ownerId);
        doNothing().when(tripInviteService).revokeInvite(eq(tripId), eq(email), eq(ownerId));

        mockMvc
            .perform(delete("/api/v1/trips/{tripId}/invites", tripId).param("email", email))
            .andExpect(status().isOk());

        verify(tripInviteService).revokeInvite(eq(tripId), eq(email), eq(ownerId));
      }
    }

    @Test
    @DisplayName("Should return 403 Forbidden for a non-owner")
    void shouldFailForNotOwner() throws Exception {
      UUID tripId = UUID.randomUUID();
      UUID notOwnerId = UUID.randomUUID();
      String email = "test@example.com";

      try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getCurrentUserId).thenReturn(notOwnerId);
        doThrow(new IllegalStateException("Unauthorized"))
            .when(tripInviteService)
            .revokeInvite(eq(tripId), eq(email), eq(notOwnerId));

        mockMvc
            .perform(delete("/api/v1/trips/{tripId}/invites", tripId).param("email", email))
            .andExpect(status().isInternalServerError());
      }
    }
  }
}
