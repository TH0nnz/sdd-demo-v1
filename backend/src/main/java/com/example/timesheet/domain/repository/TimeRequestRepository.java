package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.TimeRequest;
import com.example.timesheet.domain.enums.TimeRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TimeRequest entity.
 * Provides database access and query methods for time requests.
 */
@Repository
public interface TimeRequestRepository extends JpaRepository<TimeRequest, Long> {
    
    /**
     * Find time requests by project with pagination
     * @param projectId Project ID
     * @param pageable Pagination parameters
     * @return Page of time requests
     */
    Page<TimeRequest> findByProjectId(Long projectId, Pageable pageable);
    
    /**
     * Find time requests by requester with pagination
     * @param requesterId Requester user ID
     * @param pageable Pagination parameters
     * @return Page of time requests
     */
    Page<TimeRequest> findByRequesterId(Long requesterId, Pageable pageable);
    
    /**
     * Find time requests by status with pagination
     * @param status Time request status
     * @param pageable Pagination parameters
     * @return Page of time requests with given status
     */
    Page<TimeRequest> findByStatus(TimeRequestStatus status, Pageable pageable);
    
    /**
     * Find time requests by project and status
     * @param projectId Project ID
     * @param status Time request status
     * @param pageable Pagination parameters
     * @return Page of time requests
     */
    Page<TimeRequest> findByProjectIdAndStatus(Long projectId, TimeRequestStatus status, Pageable pageable);
}
