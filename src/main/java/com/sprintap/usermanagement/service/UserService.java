package com.sprintap.usermanagement.service;

import com.sprintap.usermanagement.dto.CreateUserRequest;
import com.sprintap.usermanagement.dto.PaginatedResponse;
import com.sprintap.usermanagement.dto.UpdateUserRequest;
import com.sprintap.usermanagement.dto.UserDTO;
import com.sprintap.usermanagement.dto.UserGroupInfo;
import com.sprintap.usermanagement.dto.UserRoleInfo;
import com.sprintap.usermanagement.entity.User;
import com.sprintap.usermanagement.exception.GroupNotFoundException;
import com.sprintap.usermanagement.exception.InvalidOperationException;
import com.sprintap.usermanagement.exception.RoleNotFoundException;
import com.sprintap.usermanagement.exception.UserNotFoundException;
import com.sprintap.usermanagement.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ROLE_PREFIX = "role_";

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    public UserDTO createUser(CreateUserRequest request) {
        // Use email as username
        String username = request.getEmail();
        log.info("Creating user: {}", username);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Validate roleIds and groupIds BEFORE creating the user
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            validateRoleIds(request.getRoleIds(), realmResource);
            log.info("Validated {} role IDs successfully", request.getRoleIds().size());
        }

        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            validateGroupIds(request.getGroupIds(), realmResource);
            log.info("Validated {} group IDs successfully", request.getGroupIds().size());
        }

        // Create user representation
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(username);  // Set username same as email
        userRep.setEmail(request.getEmail());
        userRep.setFirstName(request.getFirstName());
        userRep.setLastName(request.getLastName());
        userRep.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        userRep.setEmailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false);

        Map<String, List<String>> attributes = new HashMap<>();
        if (request.getEntityCode() != null) {
            attributes.put("entity_code", Collections.singletonList(request.getEntityCode()));
        }
        if (request.getCountryCode() != null) {
            attributes.put("country_code", Collections.singletonList(request.getCountryCode()));
        }
        userRep.setAttributes(attributes);

        String userId;
        Response response = usersResource.create(userRep);
        try {
            if (response.getStatus() == 201) {
                String locationHeader = response.getHeaderString("Location");
                userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("User '{}' created successfully with ID: {}", username, userId);
            } else if (response.getStatus() == 409) {
                throw new InvalidOperationException("User with email '" + request.getEmail() + "' already exists");
            } else {
                throw new InvalidOperationException("Failed to create user. Status: " + response.getStatus());
            }
        } finally {
            response.close();
        }

        UserResource userResource = usersResource.get(userId);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        userResource.resetPassword(credential);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRolesToUserByIds(userId, request.getRoleIds());
        }

        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            assignUserToGroups(userId, request.getGroupIds());
        }

        // Sync user to database
        syncUserToDatabase(userId, userRep, true);

        return getUserById(userId);
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        try {
            UserResource userResource = usersResource.get(userId);
            userResource.toRepresentation();
            userResource.remove();
            log.info("User with ID '{}' deleted successfully from Keycloak", userId);

            // Soft delete in database by setting is_active to false
            softDeleteUserInDatabase(userId);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UserNotFoundException("User with ID '" + userId + "' not found");
        }
    }

    /**
     * Soft delete user in database by setting is_active to false
     */
    @Transactional
    private void softDeleteUserInDatabase(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            User user = userRepository.findById(userUuid).orElse(null);

            if (user != null) {
                user.setIsActive(false);
                userRepository.save(user);
                log.info("User soft deleted in database with ID: {}", userId);
            } else {
                log.warn("User {} not found in database during delete", userId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for user ID: {}", userId, e);
        } catch (Exception e) {
            log.error("Error soft deleting user in database: {}", e.getMessage(), e);
        }
    }

    public PaginatedResponse<UserDTO> getAllUsers(String keyword, String role, Integer pageSize, Integer pageNumber, String sortBy, String sortOrder) {
        log.info("Fetching users with keyword: {}, role: {}, pageSize: {}, pageNumber: {}, sortBy: {}, sortOrder: {}",
                 keyword, role, pageSize, pageNumber, sortBy, sortOrder);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Calculate offset for pagination (Keycloak uses 0-based indexing)
        int first = (pageNumber - 1) * pageSize;
        int max = pageSize;

        // Get total count for pagination metadata
        int totalCount;
        List<UserRepresentation> users;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchTerm = keyword.trim();

            // Use Keycloak's server-side search for efficient querying with pagination
            // The search method searches across username, email, firstName, and lastName
            users = usersResource.search(searchTerm, first, max, false);

            // Get total count for search results
            List<UserRepresentation> allSearchResults = usersResource.search(searchTerm, 0, Integer.MAX_VALUE, false);
            totalCount = allSearchResults.size();

            log.info("Found {} users matching keyword '{}' using server-side search (page {} of size {})",
                     users.size(), keyword, pageNumber, pageSize);
        } else {
            // Get paginated list of all users
            users = usersResource.list(first, max);

            // Get total count
            totalCount = usersResource.count();

            log.info("Fetched {} users (page {} of size {})", users.size(), pageNumber, pageSize);
        }

        // Convert to DTOs and apply sorting
        List<UserDTO> userDTOs = users.stream()
                .map(user -> {
                    try {
                        return getUserById(user.getId());
                    } catch (Exception e) {
                        log.warn("Error fetching details for user {}: {}", user.getId(), e.getMessage());
                        return mapToBasicUserDTO(user);
                    }
                })
                .collect(Collectors.toList());

        // Filter by role if specified
        if (role != null && !role.trim().isEmpty()) {
            String roleFilter = role.trim();
            log.info("Filtering users by role: {}", roleFilter);

            userDTOs = userDTOs.stream()
                    .filter(user -> user.getRoles() != null &&
                            user.getRoles().stream()
                                    .anyMatch(userRole -> userRole.getRoleName().equalsIgnoreCase(roleFilter)))
                    .collect(Collectors.toList());

            // Recalculate total count after filtering by role
            totalCount = userDTOs.size();
            log.info("After role filter, {} users remain", totalCount);
        }

        // Sort users
        userDTOs = sortUsers(userDTOs, sortBy, sortOrder);

        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = pageNumber < totalPages;
        boolean hasPrevious = pageNumber > 1;

        PaginatedResponse.Pagination pagination = PaginatedResponse.Pagination.builder()
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .totalItems(totalCount)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();

        // Build and return paginated response
        return PaginatedResponse.<UserDTO>builder()
                .data(userDTOs)
                .pagination(pagination)
                .build();
    }

    private List<UserDTO> sortUsers(List<UserDTO> users, String sortBy, String sortOrder) {
        Comparator<UserDTO> comparator;

        switch (sortBy.toLowerCase()) {
            case "createdat":
            case "createdtimestamp":
                comparator = Comparator.comparing(UserDTO::getCreatedTimestamp, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "username":
                comparator = Comparator.comparing(UserDTO::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "email":
                comparator = Comparator.comparing(UserDTO::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "firstname":
                comparator = Comparator.comparing(UserDTO::getFirstName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "lastname":
                comparator = Comparator.comparing(UserDTO::getLastName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            default:
                // Default to createdTimestamp
                comparator = Comparator.comparing(UserDTO::getCreatedTimestamp, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        // Apply sort order
        if ("asc".equalsIgnoreCase(sortOrder)) {
            return users.stream().sorted(comparator).collect(Collectors.toList());
        } else {
            return users.stream().sorted(comparator.reversed()).collect(Collectors.toList());
        }
    }

    public UserDTO getUserById(String userId) {
        log.info("Fetching user with ID: {}", userId);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserResource userResource;
        UserRepresentation userRep;
        try {
            userResource = usersResource.get(userId);
            userRep = userResource.toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UserNotFoundException("User with ID '" + userId + "' not found");
        }

        List<UserRoleInfo> roles = userResource.roles().realmLevel().listEffective().stream()
                .filter(role -> role.getName().startsWith(ROLE_PREFIX))
                .map(role -> {
                    String displayName = role.getName().startsWith(ROLE_PREFIX)
                            ? role.getName().substring(ROLE_PREFIX.length())
                            : role.getName();
                    return UserRoleInfo.builder()
                            .roleId(role.getId())
                            .roleName(role.getName())
                            .roleDisplayName(displayName)
                            .build();
                })
                .collect(Collectors.toList());

        List<UserGroupInfo> groups = userResource.groups().stream()
                .map(group -> UserGroupInfo.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .build())
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(userRep.getId())
                .username(userRep.getUsername())
                .email(userRep.getEmail())
                .firstName(userRep.getFirstName())
                .lastName(userRep.getLastName())
                .enabled(userRep.isEnabled())
                .emailVerified(userRep.isEmailVerified())
                .createdTimestamp(userRep.getCreatedTimestamp())
                .attributes(userRep.getAttributes())
                .roles(roles)
                .groups(groups)
                .build();
    }

    public UserDTO updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserResource userResource;
        UserRepresentation userRep;
        try {
            userResource = usersResource.get(userId);
            userRep = userResource.toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new UserNotFoundException("User with ID '" + userId + "' not found");
        }

        // Validate roleIds and groupIds BEFORE updating the user
        if (request.getRoleIdsToAdd() != null && !request.getRoleIdsToAdd().isEmpty()) {
            validateRoleIds(request.getRoleIdsToAdd(), realmResource);
            log.info("Validated {} role IDs to add", request.getRoleIdsToAdd().size());
        }

        if (request.getGroupIdsToAdd() != null && !request.getGroupIdsToAdd().isEmpty()) {
            validateGroupIds(request.getGroupIdsToAdd(), realmResource);
            log.info("Validated {} group IDs to add", request.getGroupIdsToAdd().size());
        }

        if (request.getEmail() != null) {
            userRep.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            userRep.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            userRep.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            userRep.setEnabled(request.getEnabled());
        }
        if (request.getEmailVerified() != null) {
            userRep.setEmailVerified(request.getEmailVerified());
        }

        Map<String, List<String>> attributes = userRep.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        if (request.getEntityCode() != null) {
            attributes.put("entity_code", Collections.singletonList(request.getEntityCode()));
        }
        if (request.getCountryCode() != null) {
            attributes.put("country_code", Collections.singletonList(request.getCountryCode()));
        }
        userRep.setAttributes(attributes);

        userResource.update(userRep);

        if (request.getRoleIdsToAdd() != null && !request.getRoleIdsToAdd().isEmpty()) {
            assignRolesToUserByIds(userId, request.getRoleIdsToAdd());
        }
        if (request.getRoleIdsToRemove() != null && !request.getRoleIdsToRemove().isEmpty()) {
            removeRolesFromUserByIds(userId, request.getRoleIdsToRemove());
        }

        if (request.getGroupIdsToAdd() != null && !request.getGroupIdsToAdd().isEmpty()) {
            assignUserToGroups(userId, request.getGroupIdsToAdd());
        }
        if (request.getGroupIdsToRemove() != null && !request.getGroupIdsToRemove().isEmpty()) {
            removeUserFromGroups(userId, request.getGroupIdsToRemove());
        }

        log.info("User '{}' updated successfully", userId);

        // Sync user to database (only update is_active and updated_at for existing users)
        syncUserToDatabase(userId, userRep, false);

        return getUserById(userId);
    }



    private void assignRolesToUserByIds(String userId, List<String> roleIds) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        RolesResource rolesResource = realmResource.roles();
        UserResource userResource = usersResource.get(userId);

        List<RoleRepresentation> allRoles = rolesResource.list();
        List<RoleRepresentation> rolesToAssign = new ArrayList<>();

        for (String roleId : roleIds) {
            RoleRepresentation role = allRoles.stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Role with ID '" + roleId + "' not found"));

            if (!role.getName().startsWith(ROLE_PREFIX)) {
                throw new InvalidOperationException(
                        "Invalid role ID '" + roleId + "'. Only roles (starting with '" + ROLE_PREFIX + "') can be assigned to users, not privileges."
                );
            }

            rolesToAssign.add(role);
        }

        if (!rolesToAssign.isEmpty()) {
            userResource.roles().realmLevel().add(rolesToAssign);
            log.info("Assigned {} roles to user '{}'", rolesToAssign.size(), userId);
        }
    }

    private void removeRolesFromUserByIds(String userId, List<String> roleIds) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        RolesResource rolesResource = realmResource.roles();
        UserResource userResource = usersResource.get(userId);

        List<RoleRepresentation> allRoles = rolesResource.list();
        List<RoleRepresentation> rolesToRemove = new ArrayList<>();

        for (String roleId : roleIds) {
            RoleRepresentation role = allRoles.stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Role with ID '" + roleId + "' not found"));

            rolesToRemove.add(role);
        }

        if (!rolesToRemove.isEmpty()) {
            userResource.roles().realmLevel().remove(rolesToRemove);
            log.info("Removed {} roles from user '{}'", rolesToRemove.size(), userId);
        }
    }

    private void assignUserToGroups(String userId, List<String> groupIds) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        GroupsResource groupsResource = realmResource.groups();
        UserResource userResource = usersResource.get(userId);

        for (String groupId : groupIds) {
            try {
                groupsResource.group(groupId).toRepresentation();
                userResource.joinGroup(groupId);
                log.info("Added user '{}' to group '{}'", userId, groupId);
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
            }
        }
    }

    private void removeUserFromGroups(String userId, List<String> groupIds) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userId);

        for (String groupId : groupIds) {
            try {
                userResource.leaveGroup(groupId);
                log.info("Removed user '{}' from group '{}'", userId, groupId);
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
            }
        }
    }

    private void validateRoleIds(List<String> roleIds, RealmResource realmResource) {
        RolesResource rolesResource = realmResource.roles();
        List<RoleRepresentation> allRoles = rolesResource.list();

        for (String roleId : roleIds) {
            // Check for null, empty, or blank strings
            if (roleId == null || roleId.trim().isEmpty()) {
                throw new RoleNotFoundException("Role with ID '" + (roleId == null ? "null" : roleId) + "' not found");
            }

            RoleRepresentation role = allRoles.stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Role with ID '" + roleId + "' not found"));

            // Verify it's actually a role, not a privilege
            if (!role.getName().startsWith(ROLE_PREFIX)) {
                throw new InvalidOperationException(
                        "Invalid role ID '" + roleId + "'. Only roles (starting with '" + ROLE_PREFIX + "') can be assigned to users, not privileges."
                );
            }
        }
    }

    private void validateGroupIds(List<String> groupIds, RealmResource realmResource) {
        GroupsResource groupsResource = realmResource.groups();

        for (String groupId : groupIds) {
            // Check for null, empty, or blank strings
            if (groupId == null || groupId.trim().isEmpty()) {
                throw new GroupNotFoundException("Group with ID '" + (groupId == null ? "null" : groupId) + "' not found");
            }

            try {
                groupsResource.group(groupId).toRepresentation();
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
            } catch (Exception e) {
                // Catch any other exceptions (like deserialization errors) and convert to GroupNotFoundException
                throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
            }
        }
    }

    private UserDTO mapToBasicUserDTO(UserRepresentation user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdTimestamp(user.getCreatedTimestamp())
                .build();
    }

    /**
     * Sync user data to the database
     * @param userId - Keycloak user ID
     * @param userRep - Keycloak user representation
     * @param isNewUser - true if creating new user, false if updating existing user
     */
    @Transactional
    private void syncUserToDatabase(String userId, UserRepresentation userRep, boolean isNewUser) {
        try {
            UUID userUuid = UUID.fromString(userId);

            if (isNewUser) {
                // Create new user record
                String fullName = buildFullName(userRep.getFirstName(), userRep.getLastName());

                User user = User.builder()
                        .userId(userUuid)
                        .email(userRep.getEmail())
                        .fullName(fullName)
                        .username(userRep.getUsername())
                        .isActive(userRep.isEnabled())
                        // Note: Not setting default_entity_id as per requirement
                        .build();

                userRepository.save(user);
                log.info("User synced to database with ID: {}", userId);
            } else {
                // Update existing user - only update is_active and updated_at
                User existingUser = userRepository.findById(userUuid).orElse(null);

                if (existingUser != null) {
                    // Only update is_active field (updated_at is automatically updated by @UpdateTimestamp)
                    existingUser.setIsActive(userRep.isEnabled());
                    userRepository.save(existingUser);
                    log.info("User database record updated (is_active) for ID: {}", userId);
                } else {
                    // If user doesn't exist in database, create it
                    log.warn("User {} not found in database during update, creating new record", userId);
                    syncUserToDatabase(userId, userRep, true);
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for user ID: {}", userId, e);
        } catch (Exception e) {
            log.error("Error syncing user to database: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking Keycloak operations
        }
    }

    /**
     * Build full name from first name and last name
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return null;
    }
}

