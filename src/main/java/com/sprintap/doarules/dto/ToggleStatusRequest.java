package com.sprintap.doarules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for toggling DOA rule status (enable/disable)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleStatusRequest {

    private Boolean enabled;
}

