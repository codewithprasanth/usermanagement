package com.sprintap.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * Access Swagger UI at: http://localhost:8070/swagger-ui.html
 * Access API Docs at: http://localhost:8070/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8070}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management & DOA Rules API")
                        .version("1.0.0")
                        .description("""
                                ## User Management & DOA Rules API Documentation
                                
                                This API provides comprehensive user management capabilities integrated with Keycloak,
                                along with Delegation of Authority (DOA) rules management.
                                
                                ### Features:
                                - **User Management**: Create, update, delete, and search users
                                - **Role Management**: Assign and manage user roles
                                - **Group Management**: Create groups and manage user memberships
                                - **DOA Rules**: Manage delegation authority rules with approval levels
                                - **Authentication**: JWT Bearer token authentication via Keycloak
                                
                                ### Authentication:
                                All endpoints require a valid JWT Bearer token. Use the **Authorize** button to add your token.
                                
                                ### Base URLs:
                                - User Management: `/api/v1/users`, `/api/v1/roles`, `/api/v1/groups`
                                - DOA Rules: `/api/v1/doa-rules`
                                - Authentication: `/api/v1/auth`
                                """)
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@sprintap.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")));
    }
}

