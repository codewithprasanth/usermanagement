package com.sprintap.usermanagement.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprintap.usermanagement.dto.TokenRequest;
import com.sprintap.usermanagement.dto.TokenResponse;
import com.sprintap.usermanagement.service.TokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for authentication and authorization examples.
 * Demonstrates JWT token handling and role-based access control.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;

    /**
     * Public endpoint accessible without authentication.
     *
     * @return welcome message
     */
    @GetMapping("/public/hello")
    public Map<String, String> publicEndpoint() {
        log.debug("Public endpoint accessed");
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        return response;
    }

    /**
     * Retrieves an access token using client credentials flow.
     * This is a public endpoint that allows clients to obtain tokens using their client ID and secret.
     *
     * @param tokenRequest the token request containing client credentials
     * @return ResponseEntity containing the token response
     */
    @PostMapping("/public/token")
    public ResponseEntity<TokenResponse> getClientToken(@Valid @RequestBody TokenRequest tokenRequest) {
        log.info("Token request received for client: {}", tokenRequest.getClientId());
        
        try {
            TokenResponse tokenResponse = tokenService.getToken(tokenRequest);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Error retrieving token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Protected endpoint that returns user profile information from JWT token.
     *
     * @param authentication the authenticated user's authentication object
     * @return user profile details
     */
    @GetMapping("/user/profile")
    public Map<String, Object> getUserProfile(Authentication authentication) {
        log.info("Fetching user profile for: {}", authentication.getName());

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("username", jwt.getClaim("preferred_username"));
        response.put("email", jwt.getClaim("email"));
        response.put("name", jwt.getClaim("name"));
        response.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Endpoint that requires USER role.
     *
     * @return success message
     */
    @GetMapping("/user/data")
    @PreAuthorize("hasRole('PRIV_USER_MANAGEMENT')")
    public Map<String, String> getUserData() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint requires USER role");
        response.put("data", "Some user data");
        return response;
    }

    /**
     * Requires ADMIN role
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Map<String, String> getAdminDashboard() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint requires ADMIN role");
        response.put("data", "Admin dashboard data");
        return response;
    }


    /**
     * Requires either ADMIN or MANAGER role
     */
    @GetMapping("/manager/reports")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public Map<String, String> getManagerReports() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint requires ADMIN or MANAGER role");
        response.put("data", "Reports data");
        return response;
    }

    /**
     * Advanced: Requires multiple roles
     */
    @GetMapping("/secure/combined")
    @PreAuthorize("hasRole('ROLE_USER') and hasRole('ROLE_PREMIUM')")
    public Map<String, String> getCombinedAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint requires both USER and PREMIUM roles");
        response.put("data", "Premium content");
        return response;
    }
}

