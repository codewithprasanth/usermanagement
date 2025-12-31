package com.sprintap.doarules.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for DOA rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoaRuleResponse {

    private UUID id;
    private String userName;
    private UUID userId;
    private String country;
    private String emailId;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private String currency;
    private String vendorCode;
    private String poNumber;
    private String approverLevel;
    private String classification;
    private List<String> userCountries;
    private String companyCode;
    private String entity;
    private Boolean isActive;
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    private UUID createdByUserId;
}

