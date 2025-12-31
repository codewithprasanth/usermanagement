package com.sprintap.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for role information in user responses.
 * Contains role ID, full name, and display name (without prefix) for frontend convenience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleInfo {
    private String roleId;
    private String roleName;
    private String roleDisplayName;
}

