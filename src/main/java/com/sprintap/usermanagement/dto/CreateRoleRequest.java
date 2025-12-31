package com.sprintap.usermanagement.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
    private String description;
    private List<String> privilegeIds;
}