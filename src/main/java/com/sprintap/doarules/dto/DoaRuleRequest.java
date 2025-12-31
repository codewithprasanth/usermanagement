package com.sprintap.doarules.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating and updating DOA rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoaRuleRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Entity is required")
    @Size(max = 255, message = "Entity must not exceed 255 characters")
    private String entity;

    @NotNull(message = "Approval level is required")
    @Min(value = 1, message = "Approval level must be at least 1")
    private Integer approvalLevel;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum amount must be greater than or equal to 0")
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.01", message = "Maximum amount must be greater than 0")
    private BigDecimal maxAmount;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    @Size(max = 255, message = "Vendor code must not exceed 255 characters")
    private String vendorCode;

    @Size(max = 255, message = "PO number must not exceed 255 characters")
    private String poNumber;

    @Size(max = 255, message = "Classification must not exceed 255 characters")
    private String classification;

    @Builder.Default
    private Boolean enabled = true;

    /**
     * Custom validation method to check if maxAmount > minAmount
     */
    @AssertTrue(message = "Maximum amount must be greater than minimum amount")
    public boolean isValidAmountRange() {
        if (minAmount == null || maxAmount == null) {
            return true; // Let @NotNull handle null validation
        }
        return maxAmount.compareTo(minAmount) > 0;
    }
}

