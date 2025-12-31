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
public class UpdateGroupUsersRequest {
    @NotNull(message = "Users to add cannot be null")
    private List<String> userIdsToAdd;
    @NotNull(message = "Users to remove cannot be null")
    private List<String> userIdsToRemove;
}