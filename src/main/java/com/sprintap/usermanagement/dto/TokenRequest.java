package com.sprintap.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for client credentials token request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client secret is required")
    private String clientSecret;

    private String grantType = "client_credentials";

    private String scope;
}
