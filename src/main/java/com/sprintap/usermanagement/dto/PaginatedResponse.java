package com.sprintap.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper
 * Contains the data list and pagination metadata
 *
 * @param <T> The type of data in the list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    /**
     * The actual data list for the current page
     */
    private List<T> data;

    /**
     * Pagination metadata
     */
    private Pagination pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        /**
         * Current page number (1-based)
         */
        private Integer currentPage;

        /**
         * Number of items per page
         */
        private Integer pageSize;

        /**
         * Total number of items across all pages
         */
        private Integer totalItems;

        /**
         * Total number of pages
         */
        private Integer totalPages;

        /**
         * Whether there is a next page
         */
        private Boolean hasNext;

        /**
         * Whether there is a previous page
         */
        private Boolean hasPrevious;

        /**
         * Field used for sorting
         */
        private String sortBy;

        /**
         * Sort order (asc/desc)
         */
        private String sortOrder;
    }
}

