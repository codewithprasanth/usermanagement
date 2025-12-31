package com.sprintap.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing users in the database
 * This table is synchronized with Keycloak user data
 */
@Entity
@Table(name = "users", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "default_entity_id")
    private UUID defaultEntityId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "username", length = 90)
    private String username;
}

