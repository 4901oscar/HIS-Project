package com.medframe.clinical.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Configuration class to set the default timezone for the application.
 * Sets timezone to America/Guatemala (GMT-6) for all date/time operations.
 */
@Configuration
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        // Set default timezone to Guatemala (GMT-6)
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));
    }
}
