package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Department entity.
 * Provides CRUD operations and custom queries for department management.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by name.
     *
     * @param name the department name
     * @return Optional containing the department if found
     */
    Optional<Department> findByName(String name);
    
    /**
     * Check if a department with the given name exists.
     *
     * @param name the department name to check
     * @return true if department exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find all departments managed by a specific user.
     *
     * @param managerId the manager's user ID
     * @return list of departments managed by the user
     */
    List<Department> findByManagerId(Long managerId);
    
    /**
     * Find all departments with no assigned manager.
     *
     * @return list of departments without a manager
     */
    @Query("SELECT d FROM Department d WHERE d.manager IS NULL")
    List<Department> findDepartmentsWithoutManager();
    
    /**
     * Search departments by name (case-insensitive partial match).
     *
     * @param name the name to search for
     * @param pageable pagination information
     * @return page of departments matching the name search
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Department> searchByName(@Param("name") String name, Pageable pageable);
    
    /**
     * Count the number of members in a department.
     *
     * @param departmentId the department ID
     * @return number of users in the department
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countMembersByDepartmentId(@Param("departmentId") Long departmentId);
}
