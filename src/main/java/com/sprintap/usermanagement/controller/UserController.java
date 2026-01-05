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

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        log.info("REST request to delete user: {}", userId);
        userService.deleteUser(userId);
        return ResponseHelper.ok("User deleted successfully");
    }

    /**
     * GET /api/users : Get all users with optional search filter and pagination
     *
     * @param keyword Optional search keyword to filter users.
     *                Searches across username, email, firstName, and lastName with partial matching.
     *                If not provided, returns all users.
     * @param role Optional role name to filter users by role (e.g., "role_admin", "role_user").
     *             Filters users who have the specified role assigned.
     *             If not provided, returns users with any role.
     * @param pageSize Number of items per page (default: 10)
     * @param pageNumber Page number starting from 1 (default: 1)
     * @param sortBy Field to sort by - Options: createdAt, username, email, firstName, lastName (default: createdAt)
     * @param sortOrder Sort order - asc or desc (default: desc)
     * @return PaginatedResponse containing users list and pagination metadata
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        log.info("REST request to get all users with keyword: {}, role: {}, pageSize: {}, pageNumber: {}, sortBy: {}, sortOrder: {}",
                 keyword, role, pageSize, pageNumber, sortBy, sortOrder);

        PaginatedResponse<UserDTO> response = userService.getAllUsers(keyword, role, pageSize, pageNumber, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        log.info("REST request to get user: {}", userId);

        UserDTO user = userService.getUserById(userId);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST request to update user: {}", userId);
        UserDTO updatedUser = userService.updateUser(userId, request);
        return ResponseHelper.ok("User updated successfully", "user", updatedUser);
    }
}

