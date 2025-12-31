package com.sprintap.usermanagement.service;

import com.sprintap.usermanagement.dto.CreateRoleRequest;
import com.sprintap.usermanagement.dto.PrivilegeDTO;
import com.sprintap.usermanagement.dto.RoleDTO;
import com.sprintap.usermanagement.dto.UpdateRoleRequest;
import com.sprintap.usermanagement.exception.InvalidOperationException;
import com.sprintap.usermanagement.exception.PrivilegeNotFoundException;
import com.sprintap.usermanagement.exception.RoleInUseException;
import com.sprintap.usermanagement.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private static final String ROLE_PREFIX = "role_";
    private static final String PRIVILEGE_PREFIX = "priv_";
    private static final int ROLE_PREFIX_LENGTH = 5;
    private static final int PRIVILEGE_PREFIX_LENGTH = 5;

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public RoleDTO createRole(CreateRoleRequest request) {
        String roleName = ensureRolePrefix(request.getRoleName());
        log.info("Creating role: {}", roleName);

        RolesResource rolesResource = getRolesResource();
        validateRoleDoesNotExist(rolesResource, roleName);

        // Validate privilege IDs BEFORE creating the role
        List<RoleRepresentation> privileges = Collections.emptyList();
        if (request.getPrivilegeIds() != null && !request.getPrivilegeIds().isEmpty()) {
            // This will throw exception if any privilege ID is invalid
            privileges = fetchPrivilegesByIds(request.getPrivilegeIds());
            log.info("Validated {} privilege IDs successfully", privileges.size());
        }

        // Only create the role after all validations pass
        RoleRepresentation roleRep = buildRoleRepresentation(roleName, request.getDescription(),
                !privileges.isEmpty());
        rolesResource.create(roleRep);
        log.info("Role '{}' created successfully", roleName);

        // Add privileges if any (we already validated them above)
        if (!privileges.isEmpty()) {
            RoleResource roleResource = rolesResource.get(roleName);
            roleResource.addComposites(privileges);
            log.info("Added {} privileges to role '{}'", privileges.size(), roleName);
        }

        return mapToRoleDTO(rolesResource.get(roleName).toRepresentation());
    }

    public void deleteRole(String roleId) {
        log.info("Attempting to delete role with ID: {}", roleId);

        // Get role by ID first
        RoleRepresentation role = getRoleById(roleId);
        String roleName = role.getName();

        log.info("Resolved role ID '{}' to role name '{}'", roleId, roleName);

        validateNotPrivilege(roleName, "delete");

        RoleResource roleResource = getRoleResource(roleName);
        validateRoleNotInUse(roleResource, roleName);


        roleResource.remove();
        log.info("Role '{}' deleted successfully", roleName);
    }

    public RoleDTO updateRole(String roleId, UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        RoleRepresentation role = getRoleById(roleId);
        String roleName = role.getName();

        validateNotPrivilege(roleName, "modify");

        RoleResource roleResource = getRoleResource(roleName);

        if (request.getDescription() != null) {
            updateRoleDescription(roleResource, request.getDescription());
        }

        if (request.getPrivilegeIdsToAdd() != null && !request.getPrivilegeIdsToAdd().isEmpty()) {
            addPrivilegesToRoleByIds(roleName, request.getPrivilegeIdsToAdd());
        }

        if (request.getPrivilegeIdsToRemove() != null && !request.getPrivilegeIdsToRemove().isEmpty()) {
            removePrivilegesFromRoleByIds(roleName, request.getPrivilegeIdsToRemove());
        }

        return mapToRoleDTO(roleResource.toRepresentation());
    }

    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");

        return getRolesResource().list().stream()
                .filter(role -> role.getName().startsWith(ROLE_PREFIX))
                .map(this::mapToRoleDTO)
                .collect(Collectors.toList());
    }

    public List<PrivilegeDTO> getPrivilegesForRole(String roleId) {
        log.info("Fetching privileges for role with ID: {}", roleId);

        RoleRepresentation role = getRoleById(roleId);
        RoleResource roleResource = getRoleResource(role.getName());
        Set<RoleRepresentation> composites = roleResource.getRoleComposites();

        if (composites == null || composites.isEmpty()) {
            return Collections.emptyList();
        }

        return composites.stream()
                .filter(r -> r.getName().startsWith(PRIVILEGE_PREFIX))
                .map(this::mapToPrivilegeDTO)
                .collect(Collectors.toList());
    }

    public List<PrivilegeDTO> getAllPrivileges() {
        log.info("Fetching all privileges");

        return getRolesResource().list().stream()
                .filter(role -> role.getName().startsWith(PRIVILEGE_PREFIX))
                .map(this::mapToPrivilegeDTO)
                .collect(Collectors.toList());
    }

    private void addPrivilegesToRoleByIds(String roleName, List<String> privilegeIds) {
        RoleResource roleResource = getRolesResource().get(roleName);
        List<RoleRepresentation> privileges = fetchPrivilegesByIds(privilegeIds);

        if (!privileges.isEmpty()) {
            roleResource.addComposites(privileges);
            log.info("Added {} privileges to role '{}'", privileges.size(), roleName);
        }
    }

    private void removePrivilegesFromRoleByIds(String roleName, List<String> privilegeIds) {
        RoleResource roleResource = getRolesResource().get(roleName);
        List<RoleRepresentation> privileges = fetchRolesByIds(privilegeIds);

        if (!privileges.isEmpty()) {
            roleResource.deleteComposites(privileges);
            log.info("Removed {} privileges from role '{}'", privileges.size(), roleName);
        }
    }

    // Helper methods for Keycloak resource access
    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private RolesResource getRolesResource() {
        return getRealmResource().roles();
    }

    private RoleResource getRoleResource(String roleName) {
        try {
            RoleResource roleResource = getRolesResource().get(roleName);
            roleResource.toRepresentation(); // Verify it exists
            return roleResource;
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new RoleNotFoundException("Role '" + roleName + "' not found");
        }
    }

    private RoleRepresentation getRoleById(String roleId) {
        List<RoleRepresentation> allRoles = getRolesResource().list();
        return allRoles.stream()
                .filter(role -> role.getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException("Role with ID '" + roleId + "' not found"));
    }

    // Validation helper methods
    private String ensureRolePrefix(String roleName) {
        if (!roleName.startsWith(ROLE_PREFIX)) {
            String prefixedName = ROLE_PREFIX + roleName;
            log.info("Auto-prepended '{}' prefix. Final role name: {}", ROLE_PREFIX, prefixedName);
            return prefixedName;
        }
        return roleName;
    }

    private void validateRoleDoesNotExist(RolesResource rolesResource, String roleName) {
        try {
            rolesResource.get(roleName).toRepresentation();
            throw new InvalidOperationException("Role '" + roleName + "' already exists");
        } catch (jakarta.ws.rs.NotFoundException e) {
            // Role doesn't exist, which is what we want
        }
    }

    private void validateNotPrivilege(String roleName, String operation) {
        if (roleName.startsWith(PRIVILEGE_PREFIX)) {
            throw new InvalidOperationException(
                    "Cannot " + operation + " privilege '" + roleName + "'. Privileges are pre-defined and cannot be " + operation + "d."
            );
        }
    }

    private void validateRoleNotInUse(RoleResource roleResource, String roleName) {
        List<UserRepresentation> usersWithRole = roleResource.getUserMembers();
        if (usersWithRole != null && !usersWithRole.isEmpty()) {
            throw new RoleInUseException(
                    "Cannot delete role '" + roleName + "'. It is currently assigned to " +
                            usersWithRole.size() + " user(s). Please remove the role from all users first."
            );
        }
    }

    private RoleRepresentation buildRoleRepresentation(String name, String description, boolean isComposite) {
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName(name);
        roleRep.setDescription(description);
        roleRep.setComposite(isComposite);
        return roleRep;
    }

    private void updateRoleDescription(RoleResource roleResource, String description) {
        RoleRepresentation roleRep = roleResource.toRepresentation();
        roleRep.setDescription(description);
        roleResource.update(roleRep);
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
                    .orElseThrow(() -> new PrivilegeNotFoundException("Privilege with ID '" + privilegeId + "' not found"));

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




    private RoleDTO mapToRoleDTO(RoleRepresentation role) {
        String roleName = role.getName();
        String displayName = roleName.startsWith(ROLE_PREFIX)
                ? roleName.substring(ROLE_PREFIX_LENGTH)
                : roleName;

        return RoleDTO.builder()
                .id(role.getId())
                .name(roleName)
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
}

