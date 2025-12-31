package com.sprintap.usermanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * Allows frontend applications to access the backend APIs from different origins.
 * All CORS settings are externalized in application.properties for easy configuration.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private Boolean allowCredentials;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.max-age}")
    private Long maxAge;

    /**
     * Configures CORS settings for the application.
     * All values are read from application.properties.
     *
     * @return CorsConfigurationSource with configured CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse comma-separated values from properties
        configuration.setAllowedOrigins(parseCommaSeparated(allowedOrigins));
        configuration.setAllowedMethods(parseCommaSeparated(allowedMethods));
        configuration.setAllowedHeaders(parseCommaSeparated(allowedHeaders));
        configuration.setExposedHeaders(parseCommaSeparated(exposedHeaders));

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Helper method to parse comma-separated string into a list.
     *
     * @param value comma-separated string
     * @return list of trimmed strings
     */
    private List<String> parseCommaSeparated(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .toList();
    }
}

