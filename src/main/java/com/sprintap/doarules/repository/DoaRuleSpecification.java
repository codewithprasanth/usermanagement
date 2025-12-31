package com.sprintap.doarules.repository;

import com.sprintap.doarules.entity.DoaRule;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Specification builder for DOA rules filtering
 */
public class DoaRuleSpecification {

    /**
     * Create a dynamic specification based on filter parameters
     */
    public static Specification<DoaRule> withFilters(
            UUID userId,
            String entity,
            String currency,
            String classification,
            Boolean isActive,
            Boolean enabled) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }

            if (entity != null && !entity.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("entity")),
                        entity.toUpperCase()
                ));
            }

            if (currency != null && !currency.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("currency")),
                        currency.toUpperCase()
                ));
            }

            if (classification != null && !classification.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("classification")),
                        classification.toUpperCase()
                ));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (enabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

