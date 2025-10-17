package com.amalitech.tib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
@EnableJpaAuditing
public class TravelItineraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelItineraryApplication.class, args);
    }

}
