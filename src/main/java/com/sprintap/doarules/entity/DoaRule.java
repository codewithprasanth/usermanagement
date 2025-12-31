package com.sprintap.doarules.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing DOA (Delegation of Authority) rules
 */
@Entity
@Table(name = "doa_rules", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doa_rule_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "entity", nullable = false, length = 255)
    private String entity;

    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    @Column(name = "min_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "vendor_code", length = 255)
    private String vendorCode;

    @Column(name = "po_number", length = 255)
    private String poNumber;

    @Column(name = "classification", length = 255)
    private String classification;

    @Builder.Default
    @Column(name = "enabled")
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    // Transient fields for joined data (not persisted)
    @Transient
    private String userName;

    @Transient
    private String emailId;

    @Transient
    private String country;

    @Transient
    private String companyCode;
}

