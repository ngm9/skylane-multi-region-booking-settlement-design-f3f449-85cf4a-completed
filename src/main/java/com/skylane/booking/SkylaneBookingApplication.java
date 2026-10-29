package com.skylane.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

/**
 * Entrypoint for Skylane's single-region booking edge-to-settlement path.
 * Load balancer (nginx) terminates in front of this gateway; JWT auth is
 * terminated here (see SecurityConfig) before requests reach the Search/Quote
 * and Booking services.
 */
@SpringBootApplication
@EnableScheduling
public class SkylaneBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkylaneBookingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
