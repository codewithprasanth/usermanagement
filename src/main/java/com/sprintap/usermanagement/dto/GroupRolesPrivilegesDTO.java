package com.sprintap.usermanagement.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRolesPrivilegesDTO {
    private List<RoleDTO> roles;
    private List<PrivilegeDTO> privileges;
}