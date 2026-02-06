package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email address (used for authentication).
     *
     * @param email the user's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all users by role.
     *
     * @param role the user role to filter by
     * @return list of users with the specified role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find all users by department ID.
     *
     * @param departmentId the department ID
     * @return list of users in the specified department
     */
    List<User> findByDepartmentId(Long departmentId);
    
    /**
     * Find all users by role and department ID.
     *
     * @param role the user role
     * @param departmentId the department ID
     * @return list of users matching both criteria
     */
    List<User> findByRoleAndDepartmentId(UserRole role, Long departmentId);
    
    /**
     * Find all active users.
     *
     * @return list of active users
     */
    List<User> findByActiveTrue();
    
    /**
     * Find all inactive users.
     *
     * @return list of inactive users
     */
    List<User> findByActiveFalse();
    
    /**
     * Find users with pagination and optional filters.
     *
     * @param role optional role filter
     * @param departmentId optional department filter
     * @param active optional active status filter
     * @param pageable pagination information
     * @return page of users matching the filters
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:departmentId IS NULL OR u.department.id = :departmentId) AND " +
           "(:active IS NULL OR u.active = :active)")
    Page<User> findByFilters(
        @Param("role") UserRole role,
        @Param("departmentId") Long departmentId,
        @Param("active") Boolean active,
        Pageable pageable
    );
    
    /**
     * Search users by name (case-insensitive partial match).
     *
     * @param name the name to search for
     * @param pageable pagination information
     * @return page of users matching the name search
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> searchByName(@Param("name") String name, Pageable pageable);
    
    /**
     * Find users by role with active filter.
     *
     * @param role the user role
     * @param active the active status
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findByRoleAndActive(UserRole role, Boolean active, Pageable pageable);
    
    /**
     * Find users by department ID with active filter.
     *
     * @param departmentId the department ID
     * @param active the active status
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findByDepartmentIdAndActive(Long departmentId, Boolean active, Pageable pageable);
    
    /**
     * Find users by role, department ID and active filter.
     *
     * @param role the user role
     * @param departmentId the department ID
     * @param active the active status
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findByRoleAndDepartmentIdAndActive(UserRole role, Long departmentId, Boolean active, Pageable pageable);
    
    /**
     * Find users by active status.
     *
     * @param active the active status
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findByActive(Boolean active, Pageable pageable);
}
