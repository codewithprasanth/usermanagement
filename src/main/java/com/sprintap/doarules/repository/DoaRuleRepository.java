package com.sprintap.doarules.repository;

import com.sprintap.doarules.entity.DoaRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for DOA rules entity
 */
@Repository
public interface DoaRuleRepository extends JpaRepository<DoaRule, UUID>, JpaSpecificationExecutor<DoaRule> {

    /**
     * Find all DOA rules by user ID with pagination
     */
    Page<DoaRule> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all DOA rules by entity with pagination
     */
    Page<DoaRule> findByEntity(String entity, Pageable pageable);

    /**
     * Find DOA rule by ID and active status
     */
    @Query("SELECT d FROM DoaRule d WHERE d.id = :id AND d.isActive = true")
    DoaRule findByIdAndIsActiveTrue(@Param("id") UUID id);

    /**
     * Find all active DOA rules with pagination
     */
    Page<DoaRule> findByIsActiveTrue(Pageable pageable);
}

