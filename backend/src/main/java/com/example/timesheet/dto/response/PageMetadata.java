package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata DTO.
 * 
 * Provides pagination information for list endpoints.
 * 
 * Example usage in paginated response:
 * {
 *   "data": [...],
 *   "page": {
 *     "currentPage": 1,
 *     "pageSize": 20,
 *     "totalElements": 150,
 *     "totalPages": 8,
 *     "hasNext": true,
 *     "hasPrevious": false
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {
    
    /**
     * Current page number (1-indexed)
     */
    private Integer currentPage;
    
    /**
     * Number of items per page
     */
    private Integer pageSize;
    
    /**
     * Total number of items across all pages
     */
    private Long totalElements;
    
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
     * Create PageMetadata from Spring Data Page object.
     *
     * @param page Spring Data Page
     * @param <T> Entity type
     * @return PageMetadata
     */
    public static <T> PageMetadata from(org.springframework.data.domain.Page<T> page) {
        return PageMetadata.builder()
                .currentPage(page.getNumber() + 1) // Convert 0-indexed to 1-indexed
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
