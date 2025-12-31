package com.sprintap.usermanagement.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private String id;
    private String name;
    private Integer userCount;
}