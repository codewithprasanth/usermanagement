package com.sprintap.doarules.service;

import com.sprintap.doarules.dto.DoaRuleRequest;
import com.sprintap.doarules.dto.DoaRuleResponse;
import com.sprintap.doarules.dto.ToggleStatusResponse;
import com.sprintap.doarules.entity.DoaRule;
import com.sprintap.doarules.exception.DoaRuleNotFoundException;
import com.sprintap.doarules.mapper.DoaRuleMapper;
import com.sprintap.doarules.repository.DoaRuleRepository;
import com.sprintap.doarules.repository.DoaRuleSpecification;
import com.sprintap.usermanagement.entity.User;
import com.sprintap.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing DOA rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoaRuleService {

    private final DoaRuleRepository doaRuleRepository;
    private final DoaRuleMapper doaRuleMapper;
    private final UserRepository userRepository;

    /**
     * Get all DOA rules with filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<DoaRuleResponse> getAllDoaRules(
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder,
            UUID userId,
            String entity,
            String currency,
            String classification,
            Boolean isActive,
            Boolean enabled) {

        log.info("Fetching DOA rules with filters - page: {}, size: {}, sortBy: {}, sortOrder: {}",
                page, size, sortBy, sortOrder);

        // Create sort
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Create specification with filters
        Specification<DoaRule> spec = DoaRuleSpecification.withFilters(
                userId, entity, currency, classification, isActive, enabled);

        // Fetch data
        Page<DoaRule> doaRulesPage = doaRuleRepository.findAll(spec, pageable);

        // Convert to response DTOs with user details
        return doaRulesPage.map(doaRule -> {
            User user = userRepository.findById(doaRule.getUserId()).orElse(null);
            return doaRuleMapper.toResponse(doaRule, user);
        });
    }

    /**
     * Get DOA rule by ID
     */
    @Transactional(readOnly = true)
    public DoaRuleResponse getDoaRuleById(UUID id) {
        log.info("Fetching DOA rule with id: {}", id);

        DoaRule doaRule = doaRuleRepository.findById(id)
                .orElseThrow(() -> new DoaRuleNotFoundException("DOA rule not found with id: " + id));

        User user = userRepository.findById(doaRule.getUserId()).orElse(null);
        return doaRuleMapper.toResponse(doaRule, user);
    }

    /**
     * Create a new DOA rule
     */
    @Transactional
    public DoaRuleResponse createDoaRule(DoaRuleRequest request, UUID createdByUserId) {
        log.info("Creating new DOA rule for user: {}, entity: {}", request.getUserId(), request.getEntity());

        // TODO: Validate if user exists in users table

        DoaRule doaRule = doaRuleMapper.toEntity(request, createdByUserId);
        DoaRule savedDoaRule = doaRuleRepository.save(doaRule);

        log.info("DOA rule created successfully with id: {}", savedDoaRule.getId());

        User user = userRepository.findById(savedDoaRule.getUserId()).orElse(null);
        return doaRuleMapper.toResponse(savedDoaRule, user);
    }

    /**
     * Update an existing DOA rule
     */
    @Transactional
    public DoaRuleResponse updateDoaRule(UUID id, DoaRuleRequest request) {
        log.info("Updating DOA rule with id: {}", id);

        DoaRule existingDoaRule = doaRuleRepository.findById(id)
                .orElseThrow(() -> new DoaRuleNotFoundException("DOA rule not found with id: " + id));

        // TODO: Validate if user exists in users table

        doaRuleMapper.updateEntity(existingDoaRule, request);
        DoaRule updatedDoaRule = doaRuleRepository.save(existingDoaRule);

        log.info("DOA rule updated successfully with id: {}", updatedDoaRule.getId());

        User user = userRepository.findById(updatedDoaRule.getUserId()).orElse(null);
        return doaRuleMapper.toResponse(updatedDoaRule, user);
    }

    /**
     * Delete a DOA rule (soft delete)
     */
    @Transactional
    public void deleteDoaRule(UUID id) {
        log.info("Deleting DOA rule with id: {}", id);

        DoaRule doaRule = doaRuleRepository.findById(id)
                .orElseThrow(() -> new DoaRuleNotFoundException("DOA rule not found with id: " + id));

        doaRule.setIsActive(false);
        doaRuleRepository.save(doaRule);

        log.info("DOA rule soft deleted successfully with id: {}", id);
    }

    /**
     * Toggle DOA rule status (enable/disable)
     */
    @Transactional
    public ToggleStatusResponse toggleDoaRuleStatus(UUID id, Boolean enabled) {
        log.info("Toggling status for DOA rule id: {} to enabled: {}", id, enabled);

        DoaRule doaRule = doaRuleRepository.findById(id)
                .orElseThrow(() -> new DoaRuleNotFoundException("DOA rule not found with id: " + id));

        doaRule.setEnabled(enabled);
        DoaRule updatedDoaRule = doaRuleRepository.save(doaRule);

        log.info("DOA rule status toggled successfully for id: {}", id);

        return ToggleStatusResponse.builder()
                .id(updatedDoaRule.getId())
                .enabled(updatedDoaRule.getEnabled())
                .updatedAt(updatedDoaRule.getUpdatedAt())
                .build();
    }

    /**
     * Get DOA rules by user ID
     */
    @Transactional(readOnly = true)
    public Page<DoaRuleResponse> getDoaRulesByUserId(UUID userId, Integer page, Integer size) {
        log.info("Fetching DOA rules for user id: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DoaRule> doaRulesPage = doaRuleRepository.findByUserId(userId, pageable);

        return doaRulesPage.map(doaRule -> {
            User user = userRepository.findById(doaRule.getUserId()).orElse(null);
            return doaRuleMapper.toResponse(doaRule, user);
        });
    }

    /**
     * Get DOA rules by entity
     */
    @Transactional(readOnly = true)
    public Page<DoaRuleResponse> getDoaRulesByEntity(String entity, Integer page, Integer size) {
        log.info("Fetching DOA rules for entity: {}", entity);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DoaRule> doaRulesPage = doaRuleRepository.findByEntity(entity, pageable);

        return doaRulesPage.map(doaRule -> {
            User user = userRepository.findById(doaRule.getUserId()).orElse(null);
            return doaRuleMapper.toResponse(doaRule, user);
        });
    }
}

