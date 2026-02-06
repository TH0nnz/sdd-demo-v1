package com.example.timesheet.util;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for date and working day calculations.
 * 
 * Business Rules:
 * - Working days: Monday to Friday
 * - Weekends: Saturday and Sunday are non-working days
 * - Holidays: Configurable public holidays (future enhancement)
 * - Timezone: Asia/Taipei (UTC+8)
 */
@Slf4j
public class DateUtils {
    
    /**
     * Configurable holiday set (can be loaded from database in future)
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = new HashSet<>();
    
    private DateUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Check if a given date is a working day.
     * 
     * A working day is:
     * - Monday to Friday
     * - Not a public holiday
     * 
     * @param date the date to check
     * @return true if working day, false if weekend or holiday
     */
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Check if weekend
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // Check if public holiday
        return !PUBLIC_HOLIDAYS.contains(date);
    }
    
    /**
     * Calculate number of working days between two dates (inclusive).
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return number of working days
     * @throws IllegalArgumentException if endDate is before startDate
     */
    public static long countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "結束日期不能早於開始日期。開始: " + startDate + ", 結束: " + endDate
            );
        }
        
        long workingDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            if (isWorkingDay(current)) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
    
    /**
     * Add working days to a given date.
     * 
     * Skips weekends and public holidays.
     * 
     * @param startDate the starting date
     * @param workingDaysToAdd number of working days to add
     * @return the resulting date
     * @throws IllegalArgumentException if workingDaysToAdd is negative
     */
    public static LocalDate addWorkingDays(LocalDate startDate, int workingDaysToAdd) {
        if (workingDaysToAdd < 0) {
            throw new IllegalArgumentException("工作天數不能為負數: " + workingDaysToAdd);
        }
        
        LocalDate result = startDate;
        int daysAdded = 0;
        
        while (daysAdded < workingDaysToAdd) {
            result = result.plusDays(1);
            if (isWorkingDay(result)) {
                daysAdded++;
            }
        }
        
        return result;
    }
    
    /**
     * Subtract working days from a given date.
     * 
     * Skips weekends and public holidays.
     * 
     * @param startDate the starting date
     * @param workingDaysToSubtract number of working days to subtract
     * @return the resulting date
     * @throws IllegalArgumentException if workingDaysToSubtract is negative
     */
    public static LocalDate subtractWorkingDays(LocalDate startDate, int workingDaysToSubtract) {
        if (workingDaysToSubtract < 0) {
            throw new IllegalArgumentException("工作天數不能為負數: " + workingDaysToSubtract);
        }
        
        LocalDate result = startDate;
        int daysSubtracted = 0;
        
        while (daysSubtracted < workingDaysToSubtract) {
            result = result.minusDays(1);
            if (isWorkingDay(result)) {
                daysSubtracted++;
            }
        }
        
        return result;
    }
    
    /**
     * Check if a date is within N working days from today (inclusive).
     * 
     * Used for validating timesheet edit permission (3 working days).
     * 
     * @param date the date to check
     * @param workingDaysThreshold the working days threshold (e.g., 3)
     * @return true if date is within threshold
     */
    public static boolean isWithinWorkingDays(LocalDate date, int workingDaysThreshold) {
        LocalDate today = LocalDate.now();
        
        // Cannot edit future dates
        if (date.isAfter(today)) {
            return false;
        }
        
        // Calculate working days between date and today
        long workingDaysDiff = countWorkingDays(date, today);
        
        // Check if within threshold (inclusive, so <= not <)
        return workingDaysDiff <= workingDaysThreshold;
    }
    
    /**
     * Get the earliest date that is still editable based on working days threshold.
     * 
     * For threshold = 3, returns 3 working days ago from today.
     * 
     * @param workingDaysThreshold the working days threshold
     * @return the earliest editable date
     */
    public static LocalDate getEarliestEditableDate(int workingDaysThreshold) {
        return subtractWorkingDays(LocalDate.now(), workingDaysThreshold);
    }
    
    /**
     * Add a public holiday to the system.
     * 
     * @param holiday the holiday date
     */
    public static void addPublicHoliday(LocalDate holiday) {
        PUBLIC_HOLIDAYS.add(holiday);
        log.info("Added public holiday: {}", holiday);
    }
    
    /**
     * Remove a public holiday from the system.
     * 
     * @param holiday the holiday date
     */
    public static void removePublicHoliday(LocalDate holiday) {
        PUBLIC_HOLIDAYS.remove(holiday);
        log.info("Removed public holiday: {}", holiday);
    }
    
    /**
     * Clear all public holidays.
     */
    public static void clearPublicHolidays() {
        PUBLIC_HOLIDAYS.clear();
        log.info("Cleared all public holidays");
    }
    
    /**
     * Get all configured public holidays.
     * 
     * @return set of public holiday dates
     */
    public static Set<LocalDate> getPublicHolidays() {
        return new HashSet<>(PUBLIC_HOLIDAYS);
    }
}
