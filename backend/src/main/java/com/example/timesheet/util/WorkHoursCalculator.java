package com.example.timesheet.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

/**
 * Utility class for calculating work hours with automatic lunch break deduction.
 * 
 * Business Rules:
 * - Minimum time unit: 0.5 hours (30 minutes)
 * - Fixed lunch break: 12:00-13:00 (1 hour)
 * - Automatic deduction when work period overlaps lunch break
 * - All calculations rounded to nearest 0.5 hour
 */
@Slf4j
public class WorkHoursCalculator {
    
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final BigDecimal LUNCH_DURATION_HOURS = BigDecimal.ONE; // 1 hour
    private static final BigDecimal MIN_TIME_UNIT = new BigDecimal("0.5"); // 30 minutes
    
    private WorkHoursCalculator() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Calculate work hours between start and end time with automatic lunch deduction.
     * 
     * Rules:
     * 1. Calculate total hours = end - start
     * 2. If period overlaps lunch break (12:00-13:00), deduct 1 hour
     * 3. Round to nearest 0.5 hour
     * 4. Return result with lunch deduction flag
     * 
     * @param startTime work start time
     * @param endTime work end time
     * @return WorkHoursResult containing hours and lunch deduction flag
     * @throws IllegalArgumentException if endTime is before or equal to startTime
     */
    public static WorkHoursResult calculate(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException(
                "結束時間必須晚於開始時間。開始: " + startTime + ", 結束: " + endTime
            );
        }
        
        // Calculate total hours
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        BigDecimal totalHours = new BigDecimal(totalMinutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        
        // Check if lunch break should be deducted
        boolean lunchDeducted = shouldDeductLunch(startTime, endTime);
        BigDecimal lunchHours = lunchDeducted ? LUNCH_DURATION_HOURS : BigDecimal.ZERO;
        
        // Calculate net hours
        BigDecimal netHours = totalHours.subtract(lunchHours);
        
        // Ensure non-negative
        if (netHours.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Calculated negative hours: {}. Setting to 0.", netHours);
            netHours = BigDecimal.ZERO;
        }
        
        // Round to nearest 0.5 hour
        BigDecimal roundedHours = roundToNearestHalf(netHours);
        
        log.debug("Calculated work hours: start={}, end={}, total={}, lunch={}, net={}, rounded={}", 
                  startTime, endTime, totalHours, lunchHours, netHours, roundedHours);
        
        return new WorkHoursResult(roundedHours, lunchDeducted, lunchHours);
    }
    
    /**
     * Check if the work period overlaps with lunch break.
     * 
     * Lunch break is 12:00-13:00.
     * Overlap occurs if:
     * - Start time is before 13:00 AND
     * - End time is after 12:00
     * 
     * @param startTime work start time
     * @param endTime work end time
     * @return true if lunch should be deducted
     */
    public static boolean shouldDeductLunch(LocalTime startTime, LocalTime endTime) {
        return startTime.isBefore(LUNCH_END) && endTime.isAfter(LUNCH_START);
    }
    
    /**
     * Round hours to nearest 0.5 hour.
     * 
     * Examples:
     * - 1.2 hours -> 1.0 hours
     * - 1.3 hours -> 1.5 hours
     * - 1.7 hours -> 1.5 hours
     * - 1.8 hours -> 2.0 hours
     * 
     * @param hours the hours to round
     * @return rounded hours to nearest 0.5
     */
    public static BigDecimal roundToNearestHalf(BigDecimal hours) {
        // Divide by 0.5, round to nearest integer, multiply by 0.5
        BigDecimal divided = hours.divide(MIN_TIME_UNIT, 0, RoundingMode.HALF_UP);
        return divided.multiply(MIN_TIME_UNIT).setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * Validate if hours value conforms to 0.5 hour increment.
     * 
     * @param hours the hours to validate
     * @return true if valid (multiple of 0.5)
     */
    public static boolean isValidHoursIncrement(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        BigDecimal remainder = hours.remainder(MIN_TIME_UNIT);
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Result object containing calculated work hours and lunch deduction info.
     */
    public record WorkHoursResult(
        BigDecimal hours,
        boolean lunchDeducted,
        BigDecimal lunchHours
    ) {
        public String getMessage() {
            if (lunchDeducted) {
                return String.format("工時: %.1f 小時 (已自動扣除午休 %.1f 小時)", 
                                    hours.doubleValue(), lunchHours.doubleValue());
            }
            return String.format("工時: %.1f 小時", hours.doubleValue());
        }
    }
}
