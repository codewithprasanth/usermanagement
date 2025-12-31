package com.sprintap.usermanagement.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Request DTO for creating a new group.
 * Groups are created with a name and optional list of users.
 * Roles and privileges must be assigned separately after group creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;

    /**
     * Optional list of user IDs to add to the group upon creation
     */
    private List<String> userIds;
}
