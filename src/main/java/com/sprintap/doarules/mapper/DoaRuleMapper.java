package com.sprintap.doarules.mapper;

import com.sprintap.doarules.dto.DoaRuleRequest;
import com.sprintap.doarules.dto.DoaRuleResponse;
import com.sprintap.doarules.entity.DoaRule;
import com.sprintap.usermanagement.entity.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between DOA rule entities and DTOs
 */
@Component
public class DoaRuleMapper {

    /**
     * Convert DoaRuleRequest to DoaRule entity
     */
    public DoaRule toEntity(DoaRuleRequest request, UUID createdByUserId) {
        return DoaRule.builder()
                .userId(request.getUserId())
                .entity(request.getEntity())
                .approvalLevel(request.getApprovalLevel())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .currency(request.getCurrency())
                .vendorCode(request.getVendorCode())
                .poNumber(request.getPoNumber())
                .classification(request.getClassification())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .isActive(true)
                .createdByUserId(createdByUserId)
                .build();
    }

    /**
     * Update existing DoaRule entity from DoaRuleRequest
     */
    public void updateEntity(DoaRule entity, DoaRuleRequest request) {
        entity.setUserId(request.getUserId());
        entity.setEntity(request.getEntity());
        entity.setApprovalLevel(request.getApprovalLevel());
        entity.setMinAmount(request.getMinAmount());
        entity.setMaxAmount(request.getMaxAmount());
        entity.setCurrency(request.getCurrency());
        entity.setVendorCode(request.getVendorCode());
        entity.setPoNumber(request.getPoNumber());
        entity.setClassification(request.getClassification());
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
    }

    /**
     * Convert DoaRule entity to DoaRuleResponse
     */
    public DoaRuleResponse toResponse(DoaRule entity) {
        return toResponse(entity, null);
    }

    /**
     * Convert DoaRule entity to DoaRuleResponse with user details
     */
    public DoaRuleResponse toResponse(DoaRule entity, User user) {
        DoaRuleResponse.DoaRuleResponseBuilder builder = DoaRuleResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fromAmount(entity.getMinAmount())
                .toAmount(entity.getMaxAmount())
                .currency(entity.getCurrency())
                .vendorCode(entity.getVendorCode())
                .poNumber(entity.getPoNumber())
                .approverLevel(entity.getApprovalLevel() != null ? entity.getApprovalLevel().toString() : null)
                .classification(entity.getClassification())
                .entity(entity.getEntity())
                .isActive(entity.getIsActive())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdByUserId(entity.getCreatedByUserId());

        // Populate user details if available
        if (user != null) {
            builder.userName(user.getFullName())
                    .emailId(user.getEmail())
                    .country(null); // TODO: Fetch country from user attributes or separate table
        }

        return builder.build();
    }
}

