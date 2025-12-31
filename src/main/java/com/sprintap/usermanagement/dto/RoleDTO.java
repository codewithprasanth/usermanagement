package com.sprintap.usermanagement.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private String id;
    private String name;
    private String displayName;  // Name without 'role_' prefix for UI display
    private String description;
    private boolean composite;
}