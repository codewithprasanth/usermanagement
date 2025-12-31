package com.sprintap.usermanagement.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    private String description;
    @NotNull(message = "Privileges to add cannot be null")
    private List<String> privilegeIdsToAdd;
    @NotNull(message = "Privileges to remove cannot be null")
    private List<String> privilegeIdsToRemove;
}