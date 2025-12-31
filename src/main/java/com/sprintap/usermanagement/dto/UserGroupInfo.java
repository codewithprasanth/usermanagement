package com.sprintap.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for group information in user responses.
 * Contains both group ID and name for frontend convenience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupInfo {
    private String groupId;
    private String groupName;
}

