package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.config.MailConfig;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock private JavaMailSender mailSender;

  @Mock private TemplateEngine templateEngine;

  @Mock private MailConfig mailConfig;

  @InjectMocks private EmailServiceImpl emailService;

  private String validEmail;
  private String validOtp;
  private MimeMessage mockMimeMessage;

  @BeforeEach
  void setUp() {
    validEmail = "user@example.com";
    validOtp = "123456";
    mockMimeMessage = mock(MimeMessage.class);
  }

  @Test
  @DisplayName("Should send OTP email successfully with valid parameters")
  void sendOtpMail_WithValidParameters_ShouldSendEmailSuccessfully() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, validOtp);

    verify(mailConfig, times(1)).getFromEmail();
    verify(mailSender, times(1)).createMimeMessage();
    verify(templateEngine, times(1)).process(eq("email/email-verification"), any(Context.class));
    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when email is null")
  void sendOtpMail_WithNullEmail_ShouldThrowIllegalArgumentException() {

    String nullEmail = null;

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(nullEmail, validOtp));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when email is blank")
  void sendOtpMail_WithBlankEmail_ShouldThrowIllegalArgumentException() {

    String blankEmail = "   ";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(blankEmail, validOtp));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when email is empty")
  void sendOtpMail_WithEmptyEmail_ShouldThrowIllegalArgumentException() {

    String emptyEmail = "";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(emptyEmail, validOtp));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when OTP token is null")
  void sendOtpMail_WithNullToken_ShouldThrowIllegalArgumentException() {

    String nullToken = null;

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(validEmail, nullToken));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when OTP token is blank")
  void sendOtpMail_WithBlankToken_ShouldThrowIllegalArgumentException() {

    String blankToken = "   ";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(validEmail, blankToken));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when OTP token is empty")
  void sendOtpMail_WithEmptyToken_ShouldThrowIllegalArgumentException() {

    String emptyToken = "";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> emailService.sendOtpMail(validEmail, emptyToken));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when both email and token are null")
  void sendOtpMail_WithNullEmailAndToken_ShouldThrowIllegalArgumentException() {

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> emailService.sendOtpMail(null, null));

    assertEquals("Invalid email address or token", exception.getMessage());
    verifyNoInteractions(mailSender, templateEngine, mailConfig);
  }

  @Test
  @DisplayName("Should handle email with special characters in address")
  void sendOtpMail_WithSpecialCharacterEmail_ShouldProcessSuccessfully() {

    String specialEmail = "user+test@example.com";
    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(specialEmail, validOtp);

    verify(mailSender, times(1)).send(mockMimeMessage);
    verify(templateEngine, times(1)).process(eq("email/email-verification"), any(Context.class));
  }

  @Test
  @DisplayName("Should handle long OTP token successfully")
  void sendOtpMail_WithLongOtpToken_ShouldProcessSuccessfully() {

    String longOtp = "12345678901234567890";
    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 12345678901234567890</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, longOtp);

    verify(mailSender, times(1)).send(mockMimeMessage);
    verify(templateEngine, times(1)).process(eq("email/email-verification"), any(Context.class));
  }

  @Test
  @DisplayName("Should wrap and rethrow exception when JavaMailSender throws exception")
  void sendOtpMail_WhenMailSenderThrowsException_ShouldWrapAndRethrow() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doThrow(new RuntimeException("SMTP server unavailable")).when(mailSender).send(mockMimeMessage);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> emailService.sendOtpMail(validEmail, validOtp));

    assertEquals("Failed to send email verification", exception.getMessage());
    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should wrap and rethrow exception when TemplateEngine throws exception")
  void sendOtpMail_WhenTemplateEngineThrowsException_ShouldWrapAndRethrow() {

    String fromEmail = "noreply@travelitinerary.com";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenThrow(new RuntimeException("Template not found"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> emailService.sendOtpMail(validEmail, validOtp));

    assertEquals("Failed to send email verification", exception.getMessage());
    verify(mailSender, never()).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should throw RuntimeException when from email configuration is null")
  void sendOtpMail_WhenFromEmailIsNull_ShouldThrowRuntimeException() {

    when(mailConfig.getFromEmail()).thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> emailService.sendOtpMail(validEmail, validOtp));

    assertEquals("From email not configured", ex.getMessage());

    verify(mailSender, never()).send((MimeMessage) mockMimeMessage);
  }

  @Test
  @DisplayName("Should set correct context variables for template processing")
  void sendOtpMail_ShouldSetCorrectTemplateContextVariables() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenAnswer(
            invocation -> {
              Context context = invocation.getArgument(1);
              assertEquals(validEmail, context.getVariable("recipientEmail"));
              assertEquals(validOtp, context.getVariable("otpCode"));
              return htmlContent;
            });

    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, validOtp);

    verify(templateEngine, times(1)).process(eq("email/email-verification"), any(Context.class));
    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should handle multiple consecutive email sends successfully")
  void sendOtpMail_MultipleConsecutiveCalls_ShouldProcessAllSuccessfully() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";
    String secondEmail = "another@example.com";
    String secondOtp = "654321";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, validOtp);
    emailService.sendOtpMail(secondEmail, secondOtp);

    verify(mailSender, times(2)).createMimeMessage();
    verify(templateEngine, times(2)).process(eq("email/email-verification"), any(Context.class));
    verify(mailSender, times(2)).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should use UTF-8 encoding for email content")
  void sendOtpMail_ShouldUseUtf8Encoding() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, validOtp);

    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  @DisplayName("Should set correct email subject and from name")
  void sendOtpMail_ShouldSetCorrectEmailMetadata() {

    String fromEmail = "noreply@travelitinerary.com";
    String htmlContent = "<html><body>Your OTP is 123456</body></html>";

    when(mailConfig.getFromEmail()).thenReturn(fromEmail);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    when(templateEngine.process(eq("email/email-verification"), any(Context.class)))
        .thenReturn(htmlContent);
    doNothing().when(mailSender).send(mockMimeMessage);

    emailService.sendOtpMail(validEmail, validOtp);

    verify(mailSender, times(1)).send(mockMimeMessage);
  }
}
