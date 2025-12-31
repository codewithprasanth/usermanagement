package com.sprintap.usermanagement.controller;

import com.sprintap.usermanagement.dto.*;
import com.sprintap.usermanagement.service.GroupService;
import com.sprintap.usermanagement.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for group management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * POST /api/groups : Create a new group
     *
     * Creates a new group with the specified name and optional list of users.
     * <p>
     * Note: Roles and privileges cannot be assigned during group creation.
     * Use the PUT /api/groups/{groupId}/roles-privileges endpoint to assign roles and privileges after creation.
     * </p>
     *
     * @param request the group creation request containing:
     *                - groupName (required): The name of the group
     *                - userIds (optional): List of user IDs to add to the group
     * @return ResponseEntity with the created group details
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        log.info("REST request to create group: {}", request.getGroupName());
        GroupDTO createdGroup = groupService.createGroup(request);
        return ResponseHelper.created("Group created successfully", "group", createdGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable String groupId) {
        log.info("REST request to delete group: {}", groupId);
        groupService.deleteGroup(groupId);
        return ResponseHelper.ok("Group deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        log.info("REST request to get all groups");

        List<GroupDTO> groups = groupService.getAllGroups();

        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}/roles-privileges")
    public ResponseEntity<GroupRolesPrivilegesDTO> getRolesAndPrivilegesForGroup(@PathVariable String groupId) {
        log.info("REST request to get roles and privileges for group: {}", groupId);

        GroupRolesPrivilegesDTO rolesPrivileges = groupService.getRolesAndPrivilegesForGroup(groupId);

        return ResponseEntity.ok(rolesPrivileges);
    }

    @GetMapping("/{groupId}/users")
    public ResponseEntity<List<UserDTO>> getUsersInGroup(@PathVariable String groupId) {
        log.info("REST request to get users for group: {}", groupId);

        List<UserDTO> users = groupService.getUsersInGroup(groupId);

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{groupId}/users")
    public ResponseEntity<Map<String, Object>> updateGroupUsers(
            @PathVariable String groupId,
            @Valid @RequestBody UpdateGroupUsersRequest request) {
        log.info("REST request to update users for group: {}", groupId);
        groupService.updateGroupUsers(groupId, request);
        return ResponseHelper.ok("Group users updated successfully");
    }

    @PutMapping("/{groupId}/roles-privileges")
    public ResponseEntity<Map<String, Object>> updateGroupRolesAndPrivileges(
            @PathVariable String groupId,
            @Valid @RequestBody UpdateGroupRolesPrivilegesRequest request) {
        log.info("REST request to update roles and privileges for group: {}", groupId);
        groupService.updateGroupRolesAndPrivileges(groupId, request);
        return ResponseHelper.ok("Group roles and privileges updated successfully");
    }
}

