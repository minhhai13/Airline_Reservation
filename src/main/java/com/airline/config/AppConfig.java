package com.airline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application Configuration Defines common beans used across the application
 */
@Configuration
public class AppConfig {

    /**
     * BCrypt Password Encoder for secure password hashing Strength: 10 rounds
     * (default)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
