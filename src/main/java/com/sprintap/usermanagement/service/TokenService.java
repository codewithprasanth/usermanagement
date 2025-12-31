package com.sprintap.usermanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.sprintap.usermanagement.dto.TokenRequest;
import com.sprintap.usermanagement.dto.TokenResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling token operations with Keycloak.
 */
@Slf4j
@Service
public class TokenService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public TokenService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Retrieves an access token from Keycloak using client credentials.
     *
     * @param tokenRequest the token request containing client credentials
     * @return TokenResponse containing the access token and related information
     * @throws RuntimeException if token retrieval fails
     */
    public TokenResponse getToken(TokenRequest tokenRequest) {
        log.info("Requesting token for client: {}", tokenRequest.getClientId());

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", tokenRequest.getGrantType());
        body.add("client_id", tokenRequest.getClientId());
        body.add("client_secret", tokenRequest.getClientSecret());
        
        if (tokenRequest.getScope() != null && !tokenRequest.getScope().isEmpty()) {
            body.add("scope", tokenRequest.getScope());
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    requestEntity,
                    TokenResponse.class
            );

            log.info("Token successfully retrieved for client: {}", tokenRequest.getClientId());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve token for client: {}. Status: {}, Error: {}",
                    tokenRequest.getClientId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to retrieve token: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while retrieving token for client: {}",
                    tokenRequest.getClientId(), e);
            throw new RuntimeException("Unexpected error while retrieving token: " + e.getMessage(), e);
        }
    }
}
