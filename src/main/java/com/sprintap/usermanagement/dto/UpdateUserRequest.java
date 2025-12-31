package com.sprintap.usermanagement.dto;
import jakarta.validation.constraints.Email;
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
public class UpdateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;
    private String entityCode;
    private String countryCode;
    private List<String> roleIdsToAdd;
    private List<String> roleIdsToRemove;
    private List<String> groupIdsToAdd;
    private List<String> groupIdsToRemove;
}