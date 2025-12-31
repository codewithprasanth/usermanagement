package com.sprintap.usermanagement.service;

import com.sprintap.usermanagement.dto.*;
import com.sprintap.usermanagement.exception.GroupNotFoundException;
import com.sprintap.usermanagement.exception.InvalidOperationException;
import com.sprintap.usermanagement.exception.RoleNotFoundException;
import com.sprintap.usermanagement.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private static final String ROLE_PREFIX = "role_";
    private static final String PRIVILEGE_PREFIX = "priv_";
    private static final int HTTP_CREATED = 201;
    private static final int PRIVILEGE_PREFIX_LENGTH = 5;

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Creates a new group with the specified name and optional users.
     * Roles and privileges must be assigned separately using the updateGroupRolesAndPrivileges endpoint.
     *
     * @param request the group creation request containing group name and optional user IDs
     * @return the created group DTO
     */
    public GroupDTO createGroup(CreateGroupRequest request) {
        log.info("Creating group: {}", request.getGroupName());

        // Create the group in Keycloak
        GroupsResource groupsResource = getGroupsResource();
        String groupId = createGroupInKeycloak(groupsResource, request.getGroupName());

        // Add users to the group if provided
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            log.info("Adding {} users to group '{}'", request.getUserIds().size(), request.getGroupName());
            addUsersToGroup(groupId, request.getUserIds());
            log.info("Added {} users to group '{}'", request.getUserIds().size(), request.getGroupName());
        }

        return mapToGroupDTO(groupsResource.group(groupId).toRepresentation());
    }

    public void deleteGroup(String groupId) {
        log.info("Deleting group with ID: {}", groupId);

        GroupResource groupResource = getGroupResource(groupId);
        groupResource.remove();
        log.info("Group with ID '{}' deleted successfully", groupId);
    }

    public List<GroupDTO> getAllGroups() {
        log.info("Fetching all groups");

        return getGroupsResource().groups().stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toList());
    }

    public GroupRolesPrivilegesDTO getRolesAndPrivilegesForGroup(String groupId) {
        log.info("Fetching roles and privileges for group: {}", groupId);

        RealmResource realmResource = keycloak.realm(realm);
        GroupsResource groupsResource = realmResource.groups();

        GroupResource groupResource;
        try {
            groupResource = groupsResource.group(groupId);
            groupResource.toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
        }

        List<RoleRepresentation> roleMappings = groupResource.roles().realmLevel().listAll();

        List<RoleDTO> roles = new ArrayList<>();
        List<PrivilegeDTO> privileges = new ArrayList<>();

        for (RoleRepresentation role : roleMappings) {
            if (role.getName().startsWith(ROLE_PREFIX)) {
                roles.add(mapToRoleDTO(role));
            } else if (role.getName().startsWith(PRIVILEGE_PREFIX)) {
                privileges.add(mapToPrivilegeDTO(role));
            }
        }

        return GroupRolesPrivilegesDTO.builder()
                .roles(roles)
                .privileges(privileges)
                .build();
    }

    public List<UserDTO> getUsersInGroup(String groupId) {
        log.info("Fetching users for group: {}", groupId);

        RealmResource realmResource = keycloak.realm(realm);
        GroupsResource groupsResource = realmResource.groups();

        GroupResource groupResource;
        try {
            groupResource = groupsResource.group(groupId);
            groupResource.toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
        }

        List<UserRepresentation> members = groupResource.members();

        return members.stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    public void updateGroupUsers(String groupId, UpdateGroupUsersRequest request) {
        log.info("Updating users for group: {}", groupId);

        // Validate group exists
        getGroupResource(groupId);

        if (request.getUserIdsToAdd() != null) {
            addUsersToGroup(groupId, request.getUserIdsToAdd());
        }

        if (request.getUserIdsToRemove() != null) {
            removeUsersFromGroup(groupId, request.getUserIdsToRemove());
        }
    }

    public void updateGroupRolesAndPrivileges(String groupId, UpdateGroupRolesPrivilegesRequest request) {
        log.info("Updating roles and privileges for group: {}", groupId);

        // Validate group exists
        getGroupResource(groupId);

        if (request.getRoleIdsToAdd() != null && !request.getRoleIdsToAdd().isEmpty()) {
            assignRolesToGroupByIds(groupId, request.getRoleIdsToAdd());
        }

        if (request.getRoleIdsToRemove() != null && !request.getRoleIdsToRemove().isEmpty()) {
            removeRolesFromGroupByIds(groupId, request.getRoleIdsToRemove());
        }

        if (request.getPrivilegeIdsToAdd() != null && !request.getPrivilegeIdsToAdd().isEmpty()) {
            assignPrivilegesToGroupByIds(groupId, request.getPrivilegeIdsToAdd());
        }

        if (request.getPrivilegeIdsToRemove() != null && !request.getPrivilegeIdsToRemove().isEmpty()) {
            removePrivilegesFromGroupByIds(groupId, request.getPrivilegeIdsToRemove());
        }

        log.info("Updated roles and privileges for group '{}'", groupId);
    }

    private void assignRolesToGroupByIds(String groupId, List<String> roleIds) {
        GroupResource groupResource = getGroupsResource().group(groupId);
        List<RoleRepresentation> rolesToAssign = fetchRolesByIds(roleIds);

        if (!rolesToAssign.isEmpty()) {
            groupResource.roles().realmLevel().add(rolesToAssign);
            log.info("Assigned {} roles to group '{}'", rolesToAssign.size(), groupId);
        }
    }

    private void assignPrivilegesToGroupByIds(String groupId, List<String> privilegeIds) {
        GroupResource groupResource = getGroupsResource().group(groupId);
        List<RoleRepresentation> privileges = fetchPrivilegesByIds(privilegeIds);

        if (!privileges.isEmpty()) {
            groupResource.roles().realmLevel().add(privileges);
            log.info("Assigned {} privileges to group '{}'", privileges.size(), groupId);
        }
    }

    private void removeRolesFromGroupByIds(String groupId, List<String> roleIds) {
        GroupResource groupResource = getGroupsResource().group(groupId);
        List<RoleRepresentation> rolesToRemove = fetchRolesByIds(roleIds);

        if (!rolesToRemove.isEmpty()) {
            groupResource.roles().realmLevel().remove(rolesToRemove);
            log.info("Removed {} roles from group '{}'", rolesToRemove.size(), groupId);
        }
    }

    private void removePrivilegesFromGroupByIds(String groupId, List<String> privilegeIds) {
        GroupResource groupResource = getGroupsResource().group(groupId);
        List<RoleRepresentation> privileges = fetchRolesByIds(privilegeIds);

        if (!privileges.isEmpty()) {
            groupResource.roles().realmLevel().remove(privileges);
            log.info("Removed {} privileges from group '{}'", privileges.size(), groupId);
        }
    }

    // Helper methods for Keycloak resource access
    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private GroupsResource getGroupsResource() {
        return getRealmResource().groups();
    }

    private RolesResource getRolesResource() {
        return getRealmResource().roles();
    }

    private GroupResource getGroupResource(String groupId) {
        try {
            GroupResource groupResource = getGroupsResource().group(groupId);
            groupResource.toRepresentation(); // Verify it exists
            return groupResource;
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new GroupNotFoundException("Group with ID '" + groupId + "' not found");
        }
    }

    private String createGroupInKeycloak(GroupsResource groupsResource, String groupName) {
        GroupRepresentation groupRep = new GroupRepresentation();
        groupRep.setName(groupName);

        try (Response response = groupsResource.add(groupRep)) {
            if (response.getStatus() != HTTP_CREATED) {
                throw new InvalidOperationException("Failed to create group. Status: " + response.getStatus());
            }

            String locationHeader = response.getHeaderString("Location");
            String groupId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
            log.info("Group '{}' created successfully with ID: {}", groupName, groupId);
            return groupId;
        }
    }

    private void addUsersToGroup(String groupId, List<String> userIds) {
        RealmResource realmResource = getRealmResource();
        for (String userId : userIds) {
            try {
                realmResource.users().get(userId).joinGroup(groupId);
                log.info("Added user '{}' to group '{}'", userId, groupId);
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new UserNotFoundException("User with ID '" + userId + "' not found");
            }
        }
    }

    private void removeUsersFromGroup(String groupId, List<String> userIds) {
        RealmResource realmResource = getRealmResource();
        for (String userId : userIds) {
            try {
                realmResource.users().get(userId).leaveGroup(groupId);
                log.info("Removed user '{}' from group '{}'", userId, groupId);
            } catch (jakarta.ws.rs.NotFoundException e) {
                throw new UserNotFoundException("User with ID '" + userId + "' not found");
            }
        }
    }



    private List<RoleRepresentation> fetchPrivilegesByIds(List<String> privilegeIds) {
        RolesResource rolesResource = getRolesResource();
        List<RoleRepresentation> privileges = new ArrayList<>();

        // Get all roles once to avoid multiple API calls
        List<RoleRepresentation> allRoles = rolesResource.list();

        for (String privilegeId : privilegeIds) {
            // Find role by ID since Keycloak API uses names in URL path
            RoleRepresentation privilege = allRoles.stream()
                    .filter(role -> role.getId().equals(privilegeId))
                    .findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Privilege with ID '" + privilegeId + "' not found"));

            // Verify it's actually a privilege
            if (!privilege.getName().startsWith(PRIVILEGE_PREFIX)) {
                throw new InvalidOperationException(
                        "Invalid privilege ID '" + privilegeId + "'. The role with this ID is not a privilege (must start with '" + PRIVILEGE_PREFIX + "')"
                );
            }

            privileges.add(privilege);
        }

        return privileges;
    }

    private List<RoleRepresentation> fetchRolesByIds(List<String> roleIds) {
        RolesResource rolesResource = getRolesResource();
        List<RoleRepresentation> roles = new ArrayList<>();

        // Get all roles once to avoid multiple API calls
        List<RoleRepresentation> allRoles = rolesResource.list();

        for (String roleId : roleIds) {
            // Find role by ID since Keycloak API uses names in URL path
            RoleRepresentation role = allRoles.stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Role with ID '" + roleId + "' not found"));

            roles.add(role);
        }

        return roles;
    }

    // Mapping methods
    private GroupDTO mapToGroupDTO(GroupRepresentation group) {
        // Get user count for the group
        int userCount = 0;
        try {
            GroupResource groupResource = getGroupsResource().group(group.getId());
            List<UserRepresentation> members = groupResource.members();
            userCount = members != null ? members.size() : 0;
        } catch (Exception e) {
            log.warn("Could not fetch user count for group {}: {}", group.getId(), e.getMessage());
        }

        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .userCount(userCount)
                .build();
    }

    private RoleDTO mapToRoleDTO(RoleRepresentation role) {
        String displayName = role.getName().startsWith(ROLE_PREFIX)
                ? role.getName().substring(ROLE_PREFIX.length())
                : role.getName();

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(displayName)
                .description(role.getDescription())
                .composite(role.isComposite())
                .build();
    }

    private PrivilegeDTO mapToPrivilegeDTO(RoleRepresentation role) {
        String displayName = role.getName().startsWith(PRIVILEGE_PREFIX)
                ? role.getName().substring(PRIVILEGE_PREFIX_LENGTH)
                : role.getName();

        return PrivilegeDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(displayName)
                .description(role.getDescription())
                .build();
    }

    private UserDTO mapToUserDTO(UserRepresentation user) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(user.getId());

        // Fetch roles (only roles starting with role_ prefix)
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

        // Fetch groups
        List<UserGroupInfo> groups = userResource.groups().stream()
                .map(group -> UserGroupInfo.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .build())
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdTimestamp(user.getCreatedTimestamp())
                .attributes(user.getAttributes())
                .roles(roles)
                .groups(groups)
                .build();
    }
}

