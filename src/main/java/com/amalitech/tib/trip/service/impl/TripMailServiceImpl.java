package com.amalitech.tib.trip.service.impl;

import com.amalitech.tib.trip.service.TripMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Implementation of the TripMailService interface. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripMailServiceImpl implements TripMailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.from:no-reply@tib.local}")
  private String from;

  @Value("${spring.mail.host:localhost}")
  private String configuredHost;

  @Value("${spring.mail.port:1025}")
  private int configuredPort;

  @Value("${app.mail.fallback-to-localhost-when-mailhog-unreachable:true}")
  private boolean fallbackEnabled;

  @Override
  public void sendTripInvite(String to, String tripTitle, String link) {
    log.info("[MAIL] Preparing to send invite to {} for trip '{}'", to, tripTitle);

    JavaMailSender senderToUse = mailSender;
    String effectiveHost = configuredHost;
    int effectivePort = configuredPort;

    if (fallbackEnabled
        && isMailhogHost(configuredHost)
        && !isReachable(configuredHost, configuredPort, Duration.ofMillis(800))) {
      log.warn(
          "[MAIL] Host '{}' is not reachable on port {}. Falling back to 'localhost'.",
          configuredHost,
          configuredPort);
      senderToUse = buildFallbackSender("localhost", configuredPort);
      effectiveHost = "localhost";
    } else {
      log.info("[MAIL] Using configured SMTP {}:{}", effectiveHost, effectivePort);
    }

    try {
      sendHtmlEmail(senderToUse, to, tripTitle, link);
      log.info(
          "[MAIL] ✓ Invite sent successfully via {}:{} to {} for trip '{}'",
          effectiveHost,
          effectivePort,
          to,
          tripTitle);
    } catch (MessagingException me) {
      log.warn("[MAIL] Failed to send HTML email, falling back to plain text: {}", me.getMessage());
      try {
        sendSimpleEmail(senderToUse, to, tripTitle, link);
        log.info("[MAIL] ✓ Plain text invite sent successfully to {}", to);
      } catch (MailException ex) {
        log.error(
            "[MAIL] ✗ Failed to send invite email to {} via {}:{} - {}",
            to,
            effectiveHost,
            effectivePort,
            ex.getMessage());
        throw new RuntimeException("Failed to send invitation email to " + to, ex);
      }
    } catch (MailException | UnsupportedEncodingException ex) {
      log.error(
          "[MAIL] ✗ Failed to send invite email to {} via {}:{} - {}",
          to,
          effectiveHost,
          effectivePort,
          ex.getMessage());
      throw new RuntimeException("Failed to send invitation email to " + to, ex);
    }
  }

  /**
   * Sends an email with HTML content using Thymeleaf template.
   *
   * @param sender The {@link JavaMailSender} to use for sending the email.
   * @param to The recipient's email address.
   * @param tripTitle The title of the trip, used in the email subject and body.
   * @param link The invitation link to be included in the email.
   * @throws MessagingException If there is an error creating or sending the MimeMessage.
   */
  private void sendHtmlEmail(JavaMailSender sender, String to, String tripTitle, String link)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = sender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(to);
    helper.setFrom(from, "Travel Itinerary Builder");
    helper.setSubject("You're invited to join: " + (tripTitle != null ? tripTitle : "Trip"));

    Context context = new Context();
    context.setVariable("tripTitle", tripTitle != null ? tripTitle : "Trip");
    context.setVariable("link", link);

    String htmlContent = templateEngine.process("email/trip-invite", context);
    helper.setText(htmlContent, true);

    sender.send(message);
  }

  /**
   * Sends a plain text email, typically as a fallback when HTML email fails.
   *
   * @param sender The {@link JavaMailSender} to use.
   * @param to The recipient's email address.
   * @param tripTitle The title of the trip.
   * @param link The invitation link.
   */
  private void sendSimpleEmail(JavaMailSender sender, String to, String tripTitle, String link) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setFrom(from);
    message.setSubject(
        "You're invited to join a trip: " + (tripTitle != null ? tripTitle : "Trip"));
    message.setText(buildPlainTextContent(tripTitle, link));

    sender.send(message);
  }

  /**
   * Constructs the plain text body for the trip invitation email.
   *
   * @param tripTitle The title of the trip.
   * @param link The invitation link.
   * @return A string containing the plain text content for the email body.
   */
  private String buildPlainTextContent(String tripTitle, String link) {
    return String.format(
        """
                    Hello,

                    You have been invited to collaborate on a trip.

                    Trip: %s

                    To view the invitation and trip details, click the link below:
                    %s

                    If you don't have an account yet, please sign up to accept the invite.

                    Regards,
                    Travel Itinerary Builder
                    """,
        tripTitle != null ? tripTitle : "Trip", link);
  }

  /**
   * Checks if the configured mail host is specifically "mailhog", ignoring case. This is used to
   * determine if the fallback logic should be considered.
   *
   * @param host The hostname to check.
   * @return {@code true} if the host is "mailhog", {@code false} otherwise.
   */
  private boolean isMailhogHost(String host) {
    return host != null && host.equalsIgnoreCase("mailhog");
  }

  /**
   * Checks if a network host is reachable on a specific port within a given timeout. This is used
   * to detect if a mail server like MailHog is running before attempting to connect.
   *
   * @param host The hostname or IP address to check.
   * @param port The port number to check.
   * @param timeout The maximum time to wait for a connection.
   * @return {@code true} if a connection can be established, {@code false} otherwise.
   */
  private boolean isReachable(String host, int port, Duration timeout) {
    try {
      InetAddress.getByName(host);
      try (Socket socket = new Socket()) {
        socket.connect(new java.net.InetSocketAddress(host, port), (int) timeout.toMillis());
        return true;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Creates a simple {@link JavaMailSender} instance for fallback purposes. This is used when the
   * primary mail server is unreachable.
   *
   * @param host The fallback host (e.g., "localhost").
   * @param port The fallback port.
   * @return A new {@link JavaMailSender} configured with the fallback host and port.
   */
  private JavaMailSender buildFallbackSender(String host, int port) {
    JavaMailSenderImpl impl = new JavaMailSenderImpl();
    impl.setHost(host);
    impl.setPort(port);
    return impl;
  }
}
