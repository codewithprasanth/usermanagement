package com.sprintap.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.resource}")
    private String keycloakClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http)) // Enable CORS with custom configuration
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/public/**", "/actuator/health").permitAll()
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    /**
     * Converts JWT token to Spring Security Authentication with roles from Keycloak
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * Extracts roles from Keycloak JWT token
     * Keycloak stores roles in: realm_access.roles and resource_access.{client-id}.roles
     * Note: This is not a @Bean to avoid Spring MVC trying to register it as a general converter
     */
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Collection<String> realmRoles = realmAccess != null && realmAccess.containsKey("roles")
                    ? (Collection<String>) realmAccess.get("roles")
                    : List.of();

            // Extract resource/client roles (optional, if you use client-specific roles)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            Collection<String> clientRoles = List.of();

            if (resourceAccess != null) {
                // Get roles for specific client using the configured client ID
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(keycloakClientId);
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    clientRoles = (Collection<String>) clientAccess.get("roles");
                }
            }

            return Stream.concat(
                    realmRoles.stream(),
                    clientRoles.stream()
            )
            .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
            .collect(Collectors.toSet());
        };
    }
}

