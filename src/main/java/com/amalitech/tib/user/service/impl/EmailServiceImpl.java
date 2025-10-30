package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.config.MailConfig;
import com.amalitech.tib.user.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Service implementation for sending emails. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final MailConfig mailConfig;

  /**
   * Sends a password-reset email with a reset token link
   *
   * @param to recipient email address
   * @param token token for password reset
   */
  @Override
  public void sendPasswordResetEmail(String to, String token) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      Context context = new Context();
      context.setVariable("token", token);

      String htmlContent = templateEngine.process("email/password-reset-email", context);

      helper.setFrom(mailConfig.getFromEmail(), "Atlasia");
      helper.setTo(to);
      helper.setSubject("Password Reset Request");
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Password reset email sent to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send password reset email to: {}", to, e);
    }
  }

  @Override
  public void sendOtpMail(String to, String token) {

    if (to == null || to.isBlank() || token == null || token.isBlank()) {
      throw new IllegalArgumentException("Invalid email address or token");
    }

    String fromEmail = mailConfig.getFromEmail();

    if (fromEmail == null || fromEmail.isBlank()) {
      throw new RuntimeException("From email not configured");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      Context context = new Context();
      context.setVariable("recipientEmail", to);
      context.setVariable("otpCode", token);

      String htmlContent = templateEngine.process("email/email-verification", context);

      helper.setFrom(fromEmail, "Travel Itinerary");
      helper.setTo(to);
      helper.setSubject("Verify Your Email Address");
      helper.setText(htmlContent, true);

      mailSender.send(message);
    } catch (Exception e) {
      log.error("Failed to send verification email to {}", to, e);
      throw new RuntimeException("Failed to send email verification");
    }
  }
}
