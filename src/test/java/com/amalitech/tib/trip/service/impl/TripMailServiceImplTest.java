package com.amalitech.tib.trip.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class TripMailServiceImplTest {

  @Mock private JavaMailSender mailSender;
  @Mock private TemplateEngine templateEngine;
  @Mock private MimeMessage mimeMessage;

  @InjectMocks private TripMailServiceImpl tripMailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(tripMailService, "from", "test@example.com");
    ReflectionTestUtils.setField(tripMailService, "configuredHost", "mailhog");
    ReflectionTestUtils.setField(tripMailService, "configuredPort", 1025);
    ReflectionTestUtils.setField(tripMailService, "fallbackEnabled", false);
  }

  @Nested
  @DisplayName("Send Trip Invite")
  class SendTripInvite {

    @Test
    @DisplayName("Should send HTML email successfully")
    void shouldSendHtmlEmailSuccessfully() {
      when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
      when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

      assertDoesNotThrow(
          () -> tripMailService.sendTripInvite("to@example.com", "Test Trip", "link"));

      verify(mailSender, times(1)).send(any(MimeMessage.class));
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when HTML email sending fails")
    void shouldThrowRuntimeExceptionWhenHtmlSendFails() {
      when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
      when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");
      doThrow(new MailSendException("HTML email failed"))
          .when(mailSender)
          .send(any(MimeMessage.class));

      assertThrows(
          RuntimeException.class,
          () -> tripMailService.sendTripInvite("to@example.com", "Test Trip", "link"));

      verify(mailSender, times(1)).send(any(MimeMessage.class));
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
  }
}
