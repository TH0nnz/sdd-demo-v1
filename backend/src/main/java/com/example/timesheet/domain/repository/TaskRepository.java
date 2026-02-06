package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Task entity.
 * Provides database access and query methods for tasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find all tasks for a project with pagination
     * @param projectId Project ID
     * @param pageable Pagination parameters
     * @return Page of tasks
     */
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    
    /**
     * Find tasks by project and status
     * @param projectId Project ID
     * @param status Task status
     * @param pageable Pagination parameters
     * @return Page of tasks
     */
    Page<Task> findByProjectIdAndStatus(Long projectId, String status, Pageable pageable);
    
    /**
     * Find tasks assigned to a user
     * @param assigneeId User ID (assignee)
     * @param pageable Pagination parameters
     * @return Page of tasks
     */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);
    
    /**
     * Find tasks by assignee and status
     * @param assigneeId User ID (assignee)
     * @param status Task status
     * @param pageable Pagination parameters
     * @return Page of tasks
     */
    Page<Task> findByAssigneeIdAndStatus(Long assigneeId, String status, Pageable pageable);
    
    /**
     * Count completed tasks for a project
     * @param projectId Project ID
     * @return Number of completed tasks
     */
    long countByProjectIdAndStatus(Long projectId, String status);
}
