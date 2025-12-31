package com.sprintap.usermanagement.controller;

import com.sprintap.usermanagement.dto.CreateUserRequest;
import com.sprintap.usermanagement.dto.PaginatedResponse;
import com.sprintap.usermanagement.dto.UpdateUserRequest;
import com.sprintap.usermanagement.dto.UserDTO;
import com.sprintap.usermanagement.service.UserService;
import com.sprintap.usermanagement.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User Management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     * POST /api/v1/users
     */
    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user in Keycloak and database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<Map<String, Object>> createUser(
            @Parameter(description = "User creation request") @Valid @RequestBody CreateUserRequest request) {
        log.info("REST request to create user with email: {}", request.getEmail());
        UserDTO createdUser = userService.createUser(request);
        return ResponseHelper.created("User created successfully", "user", createdUser);
    }

    /**
     * Get all users with filtering, searching, and pagination
     * GET /api/v1/users
     *
     * @param keyword Search keyword for username, firstName, lastName, email (optional)
     * @param role Filter by role name (optional)
     * @param pageSize Number of items per page (default: 10)
     * @param pageNumber Page number starting from 1 (default: 1)
     * @param sortBy Field to sort by - Options: createdAt, username, email, firstName, lastName (default: createdAt)
     * @param sortOrder Sort order - asc or desc (default: desc)
     * @return PaginatedResponse containing users list and pagination metadata
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users with optional search and filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PaginatedResponse<UserDTO>> getAllUsers(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by role name") @RequestParam(required = false) String role,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Sort field") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order") @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        log.info("REST request to get all users with keyword: {}, role: {}, pageSize: {}, pageNumber: {}, sortBy: {}, sortOrder: {}",
                 keyword, role, pageSize, pageNumber, sortBy, sortOrder);

        PaginatedResponse<UserDTO> response = userService.getAllUsers(keyword, role, pageSize, pageNumber, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a user by ID
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("REST request to get user: {}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update a user
     * PUT /api/v1/users/{userId}
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "User update request") @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST request to update user: {}", userId);
        UserDTO updatedUser = userService.updateUser(userId, request);
        return ResponseHelper.ok("User updated successfully", "user", updatedUser);
    }

    /**
     * Delete a user
     * DELETE /api/v1/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("REST request to delete user: {}", userId);
        userService.deleteUser(userId);
        return ResponseHelper.ok("User deleted successfully");
    }
}

