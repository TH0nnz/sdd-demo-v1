package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Project entity.
 * Provides database access and query methods for projects.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find all projects assigned to a PM with pagination and filtering
     * @param pmId PM user ID
     * @param status Project status filter (optional)
     * @param pageable Pagination parameters
     * @return Page of projects
     */
    Page<Project> findByPmId(Long pmId, Pageable pageable);
    
    /**
     * Find projects by status
     * @param status Project status
     * @param pageable Pagination parameters
     * @return Page of projects with given status
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    
    /**
     * Find projects by PM and status
     * @param pmId PM user ID
     * @param status Project status
     * @param pageable Pagination parameters
     * @return Page of projects
     */
    Page<Project> findByPmIdAndStatus(Long pmId, ProjectStatus status, Pageable pageable);
    
    /**
     * Find all active projects
     * @return List of active projects
     */
    List<Project> findByStatus(ProjectStatus status);
}
