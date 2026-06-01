package com.example.travelez.backend.config;

import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties(prefix = "google")
@Getter
@Setter
public class GoogleOAuthConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri = "http://localhost:3000/auth/callback";
    private String calendarRedirectUri = "http://localhost:3000/calendar-callback";
}
