package com.amalitech.tib.config;

import java.util.Properties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Configuration class for mail-related beans. */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class MailConfig {
  private String frontendUrl;
  private String fromEmail;

  /**
   * Creates and configures a {@link JavaMailSender} bean.
   *
   * @param host The mail server host.
   * @param port The mail server port.
   * @param username The username for authentication.
   * @param password The password for authentication.
   * @param smtpAuth Whether SMTP authentication is enabled.
   * @param starttlsEnable Whether STARTTLS is enabled.
   * @param starttlsRequired Whether STARTTLS is required.
   * @param connectionTimeout The connection timeout in milliseconds.
   * @param timeout The read timeout in milliseconds.
   * @param writeTimeout The write timeout in milliseconds.
   * @param mailDebug Whether to enable debug mode for mail.
   * @return A configured {@link JavaMailSender} instance.
   */
  @Bean
  @ConditionalOnMissingBean(JavaMailSender.class)
  public JavaMailSender javaMailSender(
      @Value("${spring.mail.host:localhost}") String host,
      @Value("${spring.mail.port:1025}") int port,
      @Value("${spring.mail.username:}") String username,
      @Value("${spring.mail.password:}") String password,
      @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean smtpAuth,
      @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean starttlsEnable,
      @Value("${spring.mail.properties.mail.smtp.starttls.required:true}") boolean starttlsRequired,
      @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}") int connectionTimeout,
      @Value("${spring.mail.properties.mail.smtp.timeout:5000}") int timeout,
      @Value("${spring.mail.properties.mail.smtp.writetimeout:5000}") int writeTimeout,
      @Value("${spring.mail.properties.mail.debug:false}") boolean mailDebug) {
    JavaMailSenderImpl impl = new JavaMailSenderImpl();
    impl.setHost(host);
    impl.setPort(port);
    if (username != null && !username.isBlank()) {
      impl.setUsername(username);
    }
    if (password != null && !password.isBlank()) {
      impl.setPassword(password);
    }

    Properties props = impl.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", Boolean.toString(smtpAuth));
    props.put("mail.smtp.starttls.enable", Boolean.toString(starttlsEnable));
    props.put("mail.smtp.starttls.required", Boolean.toString(starttlsRequired));
    props.put("mail.smtp.connectiontimeout", Integer.toString(connectionTimeout));
    props.put("mail.smtp.timeout", Integer.toString(timeout));
    props.put("mail.smtp.writetimeout", Integer.toString(writeTimeout));
    props.put("mail.debug", Boolean.toString(mailDebug));

    return impl;
  }
}
