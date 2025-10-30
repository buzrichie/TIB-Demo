package com.amalitech.tib.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * A utility component that logs a summary of key application configuration properties on startup.
 * This helps in diagnosing configuration issues by providing a quick overview of the settings being
 * used.
 */
@Component
@Slf4j
public class ConfigDiagnostics {

  private final Environment env;

  public ConfigDiagnostics(Environment env) {
    this.env = env;
  }

  /**
   * Logs a summary of essential configuration properties after the bean has been constructed. It
   * retrieves properties like database URL, mail server settings, and base URL, and logs them.
   * Sensitive values like usernames are masked for security.
   */
  @PostConstruct
  public void logConfigSummary() {
    String dbUrl = get("POSTGRES_URL", "n/a");
    String dbUser = get("POSTGRES_USER", "n/a");
    String mailHost = get("MAIL_HOST", "n/a");
    String mailPort = get("MAIL_PORT", "n/a");
    String mailUser = get("MAIL_USERNAME", "");
    String baseUrl = get("BASE_URL", "http://localhost:8080");
    String serverPort = get("SERVER_PORT", get("local.server.port", "8080"));

    String mailAuth = get("MAIL_AUTH", get("spring.mail.properties.mail.smtp.auth", ""));
    String mailTls =
        get("MAIL_STARTTLS_ENABLE", get("spring.mail.properties.mail.smtp.starttls.enable", ""));

    log.info("[CONFIG] ===== Application Configuration Summary =====");
    log.info("[CONFIG] Base URL: {}", baseUrl);
    log.info("[CONFIG] Server Port: {}", serverPort);
    log.info("[CONFIG] DB URL: {}", dbUrl);
    log.info("[CONFIG] DB User: {}", safe(dbUser));
    log.info("[CONFIG] Mail Host: {}", mailHost);
    log.info("[CONFIG] Mail Port: {}", mailPort);
    log.info("[CONFIG] Mail Username: {}", safe(mailUser));
    if (!isBlank(mailAuth)) log.info("[CONFIG] Mail Auth: {}", mailAuth);
    if (!isBlank(mailTls)) log.info("[CONFIG] Mail STARTTLS: {}", mailTls);
    log.info("[CONFIG] ============================================");
  }

  private String get(String key, String def) {
    String v = env.getProperty(key);
    if (v == null) {
      v = env.getProperty("${" + key + "}");
    }
    return v != null ? v : def;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String safe(String value) {
    if (isBlank(value)) return "";
    if (value.length() <= 2) return "*".repeat(value.length());
    return value.charAt(0)
        + "*".repeat(Math.max(1, value.length() - 2))
        + value.charAt(value.length() - 1);
  }
}
