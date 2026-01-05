package com.sprintap.doarules.controller;

import com.sprintap.doarules.dto.DoaRuleRequest;
import com.sprintap.doarules.dto.DoaRuleResponse;
import com.sprintap.doarules.dto.ToggleStatusRequest;
import com.sprintap.doarules.dto.ToggleStatusResponse;
import com.sprintap.doarules.service.DoaRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for DOA rules management
 * Base path: /api/v1/doa-rules
 */
@RestController
@RequestMapping("/api/v1/doa-rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DOA Rules", description = "Delegation of Authority Rules Management API")
@SecurityRequirement(name = "Bearer Authentication")
public class DoaRuleController {

    private final DoaRuleService doaRuleService;

    /**
     * Get all DOA rules with pagination and filtering
     * GET /api/v1/doa-rules
     */
    @GetMapping
    @Operation(
        summary = "Get all DOA rules",
        description = "Retrieve a paginated list of DOA rules with optional filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved DOA rules"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<DoaRuleResponse>> getAllDoaRules(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field and order") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by entity") @RequestParam(required = false) String entity,
            @Parameter(description = "Filter by currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Filter by classification") @RequestParam(required = false) String classification,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by enabled status") @RequestParam(required = false) Boolean enabled) {

        log.info("GET /api/v1/doa-rules - page: {}, size: {}, sort: {}", page, size, sort);

        // Parse sort parameter (e.g., "userName,asc" or "createdAt,desc")
        String[] sortParams = sort.split(",");
        String sortBy = sortParams.length > 0 ? sortParams[0] : "createdAt";
        String sortOrder = sortParams.length > 1 ? sortParams[1] : "desc";

        Page<DoaRuleResponse> response = doaRuleService.getAllDoaRules(
                page, size, sortBy, sortOrder, userId, entity, currency, classification, isActive, enabled);

        return ResponseEntity.ok(response);
    }

    /**
     * Get DOA rule by ID
     * GET /api/v1/doa-rules/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get DOA rule by ID", description = "Retrieve a specific DOA rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved DOA rule"),
        @ApiResponse(responseCode = "404", description = "DOA rule not found")
    })
    public ResponseEntity<DoaRuleResponse> getDoaRuleById(
            @Parameter(description = "DOA Rule ID") @PathVariable UUID id) {
        log.info("GET /api/v1/doa-rules/{}", id);

        DoaRuleResponse response = doaRuleService.getDoaRuleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new DOA rule
     * POST /api/v1/doa-rules
     */
    @PostMapping
    @Operation(summary = "Create new DOA rule", description = "Create a new DOA rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "DOA rule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DoaRuleResponse> createDoaRule(
            @Parameter(description = "DOA Rule request") @Valid @RequestBody DoaRuleRequest request,
            Authentication authentication) {

        log.info("POST /api/v1/doa-rules - Creating DOA rule for user: {}", request.getUserId());

        // Extract user ID from JWT token
        UUID createdByUserId = extractUserIdFromToken(authentication);

        DoaRuleResponse response = doaRuleService.createDoaRule(request, createdByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing DOA rule
     * PUT /api/v1/doa-rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoaRuleResponse> updateDoaRule(
            @PathVariable UUID id,
            @Valid @RequestBody DoaRuleRequest request) {

        log.info("PUT /api/v1/doa-rules/{} - Updating DOA rule", id);

        DoaRuleResponse response = doaRuleService.updateDoaRule(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a DOA rule (soft delete)
     * DELETE /api/v1/doa-rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoaRule(@PathVariable UUID id) {
        log.info("DELETE /api/v1/doa-rules/{} - Deleting DOA rule", id);

        doaRuleService.deleteDoaRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle DOA rule status (enable/disable)
     * PATCH /api/v1/doa-rules/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ToggleStatusResponse> toggleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ToggleStatusRequest request) {

        log.info("PATCH /api/v1/doa-rules/{}/toggle-status - Toggling status to: {}", id, request.getEnabled());

        ToggleStatusResponse response = doaRuleService.toggleDoaRuleStatus(id, request.getEnabled());
        return ResponseEntity.ok(response);
    }

    /**
     * Get DOA rules by user ID
     * GET /api/v1/doa-rules/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<DoaRuleResponse>> getDoaRulesByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("GET /api/v1/doa-rules/user/{} - page: {}, size: {}", userId, page, size);

        Page<DoaRuleResponse> response = doaRuleService.getDoaRulesByUserId(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get DOA rules by entity
     * GET /api/v1/doa-rules/entity/{entity}
     */
    @GetMapping("/entity/{entity}")
    public ResponseEntity<Page<DoaRuleResponse>> getDoaRulesByEntity(
            @PathVariable String entity,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("GET /api/v1/doa-rules/entity/{} - page: {}, size: {}", entity, page, size);

        Page<DoaRuleResponse> response = doaRuleService.getDoaRulesByEntity(entity, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to extract user ID from JWT token
     */
    private UUID extractUserIdFromToken(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getClaimAsString("sub");
            if (userId != null) {
                try {
                    return UUID.fromString(userId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format in token sub claim: {}", userId);
                }
            }
        }
        // Return a default UUID or throw an exception based on your requirements
        return UUID.randomUUID(); // TODO: Handle this case properly
    }
}

