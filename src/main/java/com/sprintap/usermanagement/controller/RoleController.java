package com.sprintap.usermanagement.controller;

import com.sprintap.usermanagement.dto.CreateRoleRequest;
import com.sprintap.usermanagement.dto.PrivilegeDTO;
import com.sprintap.usermanagement.dto.RoleDTO;
import com.sprintap.usermanagement.dto.UpdateRoleRequest;
import com.sprintap.usermanagement.service.RoleService;
import com.sprintap.usermanagement.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for role and privilege management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("REST request to create role: {}", request.getRoleName());
        RoleDTO createdRole = roleService.createRole(request);
        return ResponseHelper.created("Role created successfully", "role", createdRole);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable String roleId) {
        log.info("REST request to delete role with ID: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseHelper.ok("Role deleted successfully");
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("REST request to update role with ID: {}", roleId);
        RoleDTO updatedRole = roleService.updateRole(roleId, request);
        return ResponseHelper.ok("Role updated successfully", "role", updatedRole);
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("REST request to get all roles");

        List<RoleDTO> roles = roleService.getAllRoles();

        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}/privileges")
    public ResponseEntity<List<PrivilegeDTO>> getPrivilegesForRole(@PathVariable String roleId) {
        log.info("REST request to get privileges for role with ID: {}", roleId);

        List<PrivilegeDTO> privileges = roleService.getPrivilegesForRole(roleId);

        return ResponseEntity.ok(privileges);
    }

    @GetMapping("/privileges")
    public ResponseEntity<List<PrivilegeDTO>> getAllPrivileges() {
        log.info("REST request to get all privileges");

        List<PrivilegeDTO> privileges = roleService.getAllPrivileges();

        return ResponseEntity.ok(privileges);
    }
}

