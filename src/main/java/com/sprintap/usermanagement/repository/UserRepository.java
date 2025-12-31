package com.sprintap.usermanagement.repository;

import com.sprintap.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     */
    User findByEmail(String email);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
}

