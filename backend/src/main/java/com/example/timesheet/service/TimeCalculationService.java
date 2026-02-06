package com.example.timesheet.service;

import com.example.timesheet.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for time calculation and validation.
 * Handles business logic for working days, hour calculations, and time validation.
 */
@Service
@Slf4j
public class TimeCalculationService {
    
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final int EDIT_WINDOW_WORKING_DAYS = 3;
    private static final BigDecimal HOURS_PRECISION = new BigDecimal("0.1");
    
    /**
     * Calculate worked hours between start and end time, with automatic lunch deduction.
     * Lunch hour (12:00-13:00) is deducted if the work period spans it.
     * 
     * @param startTime Work start time
     * @param endTime Work end time
     * @return Calculated hours with lunch deduction applied
     */
    public BigDecimal calculateHours(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Calculate total duration
        Duration duration = Duration.between(startTime, endTime);
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        // Check if lunch hour should be deducted
        boolean lunchDeducted = shouldDeductLunch(startTime, endTime);
        if (lunchDeducted) {
            totalHours = totalHours.subtract(BigDecimal.ONE);
        }
        
        // Round to nearest 0.1 hour
        return totalHours.divide(HOURS_PRECISION, 0, RoundingMode.HALF_UP)
            .multiply(HOURS_PRECISION);
    }
    
    /**
     * Determine if lunch hour should be deducted.
     * Lunch is deducted if the work period spans from before 12:00 to after 13:00.
     * 
     * @param startTime Work start time
     * @param endTime Work end time
     * @return true if lunch should be deducted, false otherwise
     */
    public boolean shouldDeductLunch(LocalTime startTime, LocalTime endTime) {
        return (startTime.isBefore(LUNCH_START) || startTime.equals(LUNCH_START)) 
            && (endTime.isAfter(LUNCH_END) || endTime.equals(LUNCH_END));
    }
    
    /**
     * Validate if a date is within the editable window (past 3 working days).
     * 
     * @param workDate The date to validate
     * @return true if the date is editable, false otherwise
     */
    public boolean isWithinEditWindow(LocalDate workDate) {
        LocalDate today = LocalDate.now();
        
        // Cannot edit future dates
        if (workDate.isAfter(today)) {
            return false;
        }
        
        // Count working days back from today
        int workingDaysBack = (int) countWorkingDaysBetween(workDate, today);
        
        return workingDaysBack <= EDIT_WINDOW_WORKING_DAYS;
    }
    
    /**
     * Count working days between two dates (exclusive of end date).
     * Weekends are excluded.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return Number of working days
     */
    public int countWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        // If start equals end, return 0 (no days in between)
        if (startDate.equals(endDate)) {
            return 0;
        }
        
        // DateUtils.countWorkingDays is inclusive, so subtract 1 day from end to make it exclusive
        return (int) DateUtils.countWorkingDays(startDate, endDate.minusDays(1));
    }
    
    /**
     * Get the earliest date that is still within the edit window.
     * 
     * @return The earliest editable date
     */
    public LocalDate getEarliestEditableDate() {
        LocalDate today = LocalDate.now();
        LocalDate current = today;
        int workingDaysCount = 0;
        
        while (workingDaysCount < EDIT_WINDOW_WORKING_DAYS) {
            current = current.minusDays(1);
            if (DateUtils.isWorkingDay(current)) {
                workingDaysCount++;
            }
        }
        
        return current;
    }
    
    /**
     * Validate if hours value meets precision requirements (0.1 hour increments).
     * 
     * @param hours Hours value to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidHoursPrecision(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Check if the value is a multiple of 0.1
        BigDecimal remainder = hours.remainder(HOURS_PRECISION);
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Check if task has sufficient hours remaining for a new timesheet entry.
     * 
     * @param estimatedHours Task's estimated hours
     * @param usedHours Task's currently used hours
     * @param additionalHours Additional hours to be added
     * @return true if sufficient hours available, false otherwise
     */
    public boolean hasSufficientHours(Double estimatedHours, Double usedHours, BigDecimal additionalHours) {
        if (estimatedHours == null || usedHours == null || additionalHours == null) {
            return false;
        }
        
        BigDecimal remaining = BigDecimal.valueOf(estimatedHours)
            .subtract(BigDecimal.valueOf(usedHours));
        
        return remaining.compareTo(additionalHours) >= 0;
    }
    
    /**
     * Calculate remaining hours for a task.
     * 
     * @param estimatedHours Task's estimated hours
     * @param usedHours Task's currently used hours
     * @return Remaining hours (0 if negative)
     */
    public BigDecimal calculateRemainingHours(Double estimatedHours, Double usedHours) {
        if (estimatedHours == null || usedHours == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal remaining = BigDecimal.valueOf(estimatedHours)
            .subtract(BigDecimal.valueOf(usedHours));
        
        return remaining.max(BigDecimal.ZERO);
    }
    
    /**
     * Calculate percentage of hours used.
     * 
     * @param estimatedHours Task's estimated hours
     * @param usedHours Task's currently used hours
     * @return Percentage used (0-100)
     */
    public BigDecimal calculateHoursUsedPercentage(Double estimatedHours, Double usedHours) {
        if (estimatedHours == null || estimatedHours == 0 || usedHours == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(usedHours)
            .divide(BigDecimal.valueOf(estimatedHours), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if task hours are running low (>= 80% used).
     * 
     * @param estimatedHours Task's estimated hours
     * @param usedHours Task's currently used hours
     * @return true if hours are running low, false otherwise
     */
    public boolean areHoursRunningLow(Double estimatedHours, Double usedHours) {
        BigDecimal percentage = calculateHoursUsedPercentage(estimatedHours, usedHours);
        return percentage.compareTo(BigDecimal.valueOf(80)) >= 0;
    }
    
    /**
     * Get list of working days in a date range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of working days
     */
    public List<LocalDate> getWorkingDaysInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            if (DateUtils.isWorkingDay(current)) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
}
