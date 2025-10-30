package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OTPVerificationServiceImplTest {

  @Mock private OTPTokenRepository OTPTokenRepository;
  @Mock private RateLimiterImpl rateLimiter;

  @InjectMocks private OTPVerificationServiceImpl otpVerificationService;

  private final String VALID_OTP = "123456";
  private final String VALID_EMAIL = "test@example.com";
  private OTPToken mockToken;

  @BeforeEach
  void setUp() {
    mockToken = new OTPToken();
    mockToken.setToken(VALID_OTP);
    mockToken.setEmail(VALID_EMAIL);
  }

  @Test
  @DisplayName("Should return token when OTP and email are valid and not expired")
  void verifyOTP_Success_ValidToken() {
    OTPToken spyToken = spy(mockToken);
    doReturn(false).when(spyToken).isExpired();

    when(OTPTokenRepository.findByToken(VALID_OTP)).thenReturn(Optional.of(spyToken));

    OTPToken result = otpVerificationService.verifyOTP(VALID_OTP, VALID_EMAIL);

    assertNotNull(result);
    assertEquals(VALID_EMAIL, result.getEmail());
    verify(OTPTokenRepository).findByToken(VALID_OTP);
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when OTP is null or blank")
  void verifyOTP_Failure_InvalidOTPInput() {
    assertThrows(
        InvalidTokenException.class, () -> otpVerificationService.verifyOTP(null, VALID_EMAIL));
    assertThrows(
        InvalidTokenException.class, () -> otpVerificationService.verifyOTP(" ", VALID_EMAIL));
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when email is null or blank")
  void verifyOTP_Failure_InvalidEmailInput() {
    assertThrows(
        InvalidTokenException.class, () -> otpVerificationService.verifyOTP(VALID_OTP, null));
    assertThrows(
        InvalidTokenException.class, () -> otpVerificationService.verifyOTP(VALID_OTP, " "));
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token not found")
  void verifyOTP_Failure_TokenNotFound() {
    when(OTPTokenRepository.findByToken(VALID_OTP)).thenReturn(Optional.empty());

    InvalidTokenException ex =
        assertThrows(
            InvalidTokenException.class,
            () -> otpVerificationService.verifyOTP(VALID_OTP, VALID_EMAIL));

    assertEquals("Invalid or expired OTP", ex.getMessage());
    verify(OTPTokenRepository).findByToken(VALID_OTP);
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token is expired")
  void verifyOTP_Failure_TokenExpired() {
    OTPToken expiredToken = spy(mockToken);
    doReturn(true).when(expiredToken).isExpired();
    when(OTPTokenRepository.findByToken(VALID_OTP)).thenReturn(Optional.of(expiredToken));

    InvalidTokenException ex =
        assertThrows(
            InvalidTokenException.class,
            () -> otpVerificationService.verifyOTP(VALID_OTP, VALID_EMAIL));

    assertEquals("OTP has expired", ex.getMessage());
    verify(OTPTokenRepository).findByToken(VALID_OTP);
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when email does not match")
  void verifyOTP_Failure_EmailMismatch() {
    OTPToken validToken = spy(mockToken);
    doReturn(false).when(validToken).isExpired();
    when(OTPTokenRepository.findByToken(VALID_OTP)).thenReturn(Optional.of(validToken));

    InvalidTokenException ex =
        assertThrows(
            InvalidTokenException.class,
            () -> otpVerificationService.verifyOTP(VALID_OTP, "wrong@example.com"));

    assertEquals("OTP does not match this email", ex.getMessage());
    verify(OTPTokenRepository).findByToken(VALID_OTP);
  }

  @Test
  @DisplayName("generateOTP: Should throw InvalidTokenException when email is null or blank")
  void generateOTP_Failure_InvalidEmail() {
    assertThrows(InvalidTokenException.class, () -> otpVerificationService.generateOTP(null));
    assertThrows(InvalidTokenException.class, () -> otpVerificationService.generateOTP(" "));
  }

  @Test
  @DisplayName("generateOTP: Should regenerate OTP when existing token found and within rate limit")
  void generateOTP_Regenerate_ExistingTokenFound() {
    OTPToken existingToken = mock(OTPToken.class);
    String newOtp = "789012";

    when(OTPTokenRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingToken));
    when(existingToken.getToken()).thenReturn(newOtp);

    doNothing().when(rateLimiter).checkRateLimit(eq(existingToken), any(Instant.class));

    String otp = otpVerificationService.generateOTP(VALID_EMAIL);

    assertEquals(newOtp, otp);
    verify(rateLimiter).checkRateLimit(eq(existingToken), any(Instant.class));
    verify(existingToken).regenerateOtp(5);
    verify(OTPTokenRepository).save(existingToken);
  }

  @Test
  @DisplayName("generateOTP: Should create new OTP when no existing token found")
  void generateOTP_CreateNew_NoExistingTokenFound() {
    when(OTPTokenRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

    OTPToken newToken = mock(OTPToken.class);
    String freshOtp = "fresh-otp";
    when(newToken.getToken()).thenReturn(freshOtp);

    try (MockedStatic<OTPToken> mockedToken = mockStatic(OTPToken.class)) {
      mockedToken.when(() -> OTPToken.createToken(VALID_EMAIL, 5)).thenReturn(newToken);

      String otp = otpVerificationService.generateOTP(VALID_EMAIL);

      assertEquals(freshOtp, otp);
      verify(OTPTokenRepository).save(newToken);
    }
  }
}
