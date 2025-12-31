package com.sprintap.doarules.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for toggle status operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleStatusResponse {

    private UUID id;
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}

