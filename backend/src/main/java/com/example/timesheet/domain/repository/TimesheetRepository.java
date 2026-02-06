package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.TimesheetEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TimesheetEntry entity (T120).
 * Provides database access for timesheet records with custom queries.
 */
@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetEntry, Long> {
    
    /**
     * Find timesheets for a user within a date range.
     * 
     * @param userId the user ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param pageable pagination info
     * @return page of timesheets
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.user.id = :userId " +
           "AND t.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    Page<TimesheetEntry> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    /**
     * Find timesheet for a specific user, task, and date.
     * 
     * @param userId the user ID
     * @param taskId the task ID
     * @param workDate the work date
     * @return optional timesheet
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.user.id = :userId " +
           "AND t.task.id = :taskId " +
           "AND t.workDate = :workDate")
    Optional<TimesheetEntry> findByUserAndTaskAndDate(
        @Param("userId") Long userId,
        @Param("taskId") Long taskId,
        @Param("workDate") LocalDate workDate
    );
    
    /**
     * Find all timesheets for a user on a specific date.
     * 
     * @param userId the user ID
     * @param workDate the work date
     * @return list of timesheets for that date
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.user.id = :userId " +
           "AND t.workDate = :workDate " +
           "ORDER BY t.startTime ASC")
    List<TimesheetEntry> findByUserAndDate(
        @Param("userId") Long userId,
        @Param("workDate") LocalDate workDate
    );
    
    /**
     * Find all timesheets for a specific task.
     * 
     * @param taskId the task ID
     * @param pageable pagination info
     * @return page of timesheets
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.task.id = :taskId " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    Page<TimesheetEntry> findByTaskId(
        @Param("taskId") Long taskId,
        Pageable pageable
    );
    
    /**
     * Find timesheets for a task assigned to a specific user.
     * 
     * @param taskId the task ID
     * @param userId the user ID
     * @param pageable pagination info
     * @return page of timesheets
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.task.id = :taskId " +
           "AND t.user.id = :userId " +
           "ORDER BY t.workDate DESC, t.createdAt DESC")
    Page<TimesheetEntry> findByTaskIdAndUserId(
        @Param("taskId") Long taskId,
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    /**
     * Calculate total hours worked by a user for a specific task.
     * 
     * @param taskId the task ID
     * @param userId the user ID
     * @return total hours (0.0 if no timesheets)
     */
    @Query("SELECT COALESCE(SUM(t.calculatedHours), 0) FROM TimesheetEntry t " +
           "WHERE t.task.id = :taskId " +
           "AND t.user.id = :userId")
    Double calculateTotalHoursForTaskAndUser(
        @Param("taskId") Long taskId,
        @Param("userId") Long userId
    );
    
    /**
     * Find the most recent timesheet entry for a user.
     * 
     * @param userId the user ID
     * @return optional timesheet
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.user.id = :userId " +
           "ORDER BY t.workDate DESC, t.createdAt DESC " +
           "LIMIT 1")
    Optional<TimesheetEntry> findMostRecentByUserId(@Param("userId") Long userId);
    
    /**
     * Find timesheets created within a date range.
     * Useful for auditing and reporting.
     * 
     * @param fromDate start date
     * @param toDate end date
     * @param pageable pagination info
     * @return page of timesheets
     */
    @Query("SELECT t FROM TimesheetEntry t " +
           "WHERE t.createdAt >= :fromDate " +
           "AND t.createdAt <= :toDate " +
           "ORDER BY t.createdAt DESC")
    Page<TimesheetEntry> findByCreatedAtRange(
        @Param("fromDate") java.time.LocalDateTime fromDate,
        @Param("toDate") java.time.LocalDateTime toDate,
        Pageable pageable
    );
}
