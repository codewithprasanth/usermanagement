package com.sprintap.usermanagement.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeDTO {
    private String id;
    private String name;
    private String displayName;  // Name without 'priv_' prefix for UI display
    private String description;
}