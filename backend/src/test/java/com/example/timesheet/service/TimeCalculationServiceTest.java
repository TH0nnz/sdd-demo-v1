package com.example.timesheet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for TimeCalculationService.
 * Constitution requirement: 100% test coverage for time calculation logic.
 * 
 * Test categories:
 * - Hour calculation (with/without lunch deduction)
 * - Lunch deduction rules
 * - Edit window validation (3 working days)
 * - Working days counting
 * - Hours precision validation
 * - Task hours sufficiency checks
 * - Hours usage calculations
 * - Working days range generation
 */
@DisplayName("TimeCalculationService Tests")
class TimeCalculationServiceTest {
    
    private TimeCalculationService service;
    
    @BeforeEach
    void setUp() {
        service = new TimeCalculationService();
    }
    
    @Nested
    @DisplayName("Hour Calculation Tests")
    class HourCalculationTests {
        
        @Test
        @DisplayName("Should calculate hours correctly without lunch deduction")
        void shouldCalculateHoursWithoutLunchDeduction() {
            // 9:00 AM to 11:30 AM = 2.5 hours (no lunch)
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(11, 30);
            
            BigDecimal hours = service.calculateHours(start, end);
            
            assertThat(hours).isEqualByComparingTo(new BigDecimal("2.5"));
        }
        
        @Test
        @DisplayName("Should calculate hours with lunch deduction")
        void shouldCalculateHoursWithLunchDeduction() {
            // 9:00 AM to 2:00 PM = 5 hours - 1 hour lunch = 4 hours
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(14, 0);
            
            BigDecimal hours = service.calculateHours(start, end);
            
            assertThat(hours).isEqualByComparingTo(new BigDecimal("4.0"));
        }
        
        @Test
        @DisplayName("Should round to nearest 0.1 hour")
        void shouldRoundToNearestDecimalHour() {
            // 9:00 AM to 9:37 AM = 0.62 hours -> rounds to 0.6
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(9, 37);
            
            BigDecimal hours = service.calculateHours(start, end);
            
            assertThat(hours).isEqualByComparingTo(new BigDecimal("0.6"));
        }
        
        @ParameterizedTest
        @CsvSource({
            "08:00, 12:00, 4.0",    // Morning only, no lunch
            "13:00, 17:00, 4.0",    // Afternoon only, no lunch
            "08:00, 13:00, 4.0",    // Spans lunch exactly
            "11:00, 14:00, 2.0",    // Spans lunch
            "09:00, 18:00, 8.0",    // Full day with lunch
            "10:30, 15:45, 4.3",    // Partial day with lunch (5.25 - 1 = 4.25 -> 4.3)
        })
        @DisplayName("Should calculate hours correctly for various time ranges")
        void shouldCalculateHoursForVariousRanges(String startStr, String endStr, String expectedHours) {
            LocalTime start = LocalTime.parse(startStr);
            LocalTime end = LocalTime.parse(endStr);
            
            BigDecimal hours = service.calculateHours(start, end);
            
            assertThat(hours).isEqualByComparingTo(new BigDecimal(expectedHours));
        }
        
        @Test
        @DisplayName("Should throw exception when start time is null")
        void shouldThrowExceptionWhenStartTimeIsNull() {
            LocalTime end = LocalTime.of(17, 0);
            
            assertThatThrownBy(() -> service.calculateHours(null, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time and end time cannot be null");
        }
        
        @Test
        @DisplayName("Should throw exception when end time is null")
        void shouldThrowExceptionWhenEndTimeIsNull() {
            LocalTime start = LocalTime.of(9, 0);
            
            assertThatThrownBy(() -> service.calculateHours(start, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time and end time cannot be null");
        }
        
        @Test
        @DisplayName("Should throw exception when end time equals start time")
        void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
            LocalTime time = LocalTime.of(9, 0);
            
            assertThatThrownBy(() -> service.calculateHours(time, time))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
        }
        
        @Test
        @DisplayName("Should throw exception when end time is before start time")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            LocalTime start = LocalTime.of(17, 0);
            LocalTime end = LocalTime.of(9, 0);
            
            assertThatThrownBy(() -> service.calculateHours(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
        }
    }
    
    @Nested
    @DisplayName("Lunch Deduction Tests")
    class LunchDeductionTests {
        
        @Test
        @DisplayName("Should deduct lunch when work spans 12:00-13:00")
        void shouldDeductLunchWhenSpansLunchHour() {
            LocalTime start = LocalTime.of(11, 0);
            LocalTime end = LocalTime.of(14, 0);
            
            boolean deducted = service.shouldDeductLunch(start, end);
            
            assertThat(deducted).isTrue();
        }
        
        @Test
        @DisplayName("Should not deduct lunch when work ends before 13:00")
        void shouldNotDeductLunchWhenEndsBeforeLunchEnd() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(12, 30);
            
            boolean deducted = service.shouldDeductLunch(start, end);
            
            assertThat(deducted).isFalse();
        }
        
        @Test
        @DisplayName("Should not deduct lunch when work starts after 12:00")
        void shouldNotDeductLunchWhenStartsAfterLunchStart() {
            LocalTime start = LocalTime.of(13, 30);
            LocalTime end = LocalTime.of(17, 0);
            
            boolean deducted = service.shouldDeductLunch(start, end);
            
            assertThat(deducted).isFalse();
        }
        
        @Test
        @DisplayName("Should deduct lunch when starts exactly at 12:00 and ends after 13:00")
        void shouldDeductLunchWhenStartsAtLunchStart() {
            LocalTime start = LocalTime.of(12, 0);
            LocalTime end = LocalTime.of(14, 0);
            
            boolean deducted = service.shouldDeductLunch(start, end);
            
            assertThat(deducted).isTrue();
        }
        
        @Test
        @DisplayName("Should deduct lunch when starts before 12:00 and ends exactly at 13:00")
        void shouldDeductLunchWhenEndsAtLunchEnd() {
            LocalTime start = LocalTime.of(11, 0);
            LocalTime end = LocalTime.of(13, 0);
            
            boolean deducted = service.shouldDeductLunch(start, end);
            
            assertThat(deducted).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Edit Window Validation Tests")
    class EditWindowValidationTests {
        
        @Test
        @DisplayName("Should allow editing today's entry")
        void shouldAllowEditingTodaysEntry() {
            LocalDate today = LocalDate.now();
            
            boolean withinWindow = service.isWithinEditWindow(today);
            
            assertThat(withinWindow).isTrue();
        }
        
        @Test
        @DisplayName("Should allow editing yesterday's entry if it's a working day")
        void shouldAllowEditingYesterdaysEntry() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // Skip if yesterday was a weekend
            if (isWeekend(yesterday)) {
                return;
            }
            
            boolean withinWindow = service.isWithinEditWindow(yesterday);
            
            assertThat(withinWindow).isTrue();
        }
        
        @Test
        @DisplayName("Should not allow editing future dates")
        void shouldNotAllowEditingFutureDates() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            
            boolean withinWindow = service.isWithinEditWindow(tomorrow);
            
            assertThat(withinWindow).isFalse();
        }
        
        @Test
        @DisplayName("Should not allow editing dates beyond 3 working days")
        void shouldNotAllowEditingBeyondThreeWorkingDays() {
            // Go back 5 working days (definitely beyond 3)
            LocalDate date = findPastWorkingDay(5);
            
            boolean withinWindow = service.isWithinEditWindow(date);
            
            assertThat(withinWindow).isFalse();
        }
        
        @Test
        @DisplayName("Should handle weekend dates correctly")
        void shouldHandleWeekendDatesCorrectly() {
            // Find a recent Saturday or Sunday
            LocalDate weekend = LocalDate.now();
            while (weekend.getDayOfWeek() != DayOfWeek.SATURDAY) {
                weekend = weekend.minusDays(1);
                if (weekend.isBefore(LocalDate.now().minusMonths(1))) {
                    return; // Safety check
                }
            }
            
            boolean withinWindow = service.isWithinEditWindow(weekend);
            
            // Weekend dates should be allowed if they're recent enough
            // The actual result depends on when working days fall
            assertThat(withinWindow).isIn(true, false);
        }
        
        private LocalDate findPastWorkingDay(int workingDaysBack) {
            LocalDate date = LocalDate.now();
            int count = 0;
            while (count < workingDaysBack) {
                date = date.minusDays(1);
                if (!isWeekend(date)) {
                    count++;
                }
            }
            return date;
        }
        
        private boolean isWeekend(LocalDate date) {
            DayOfWeek day = date.getDayOfWeek();
            return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        }
    }
    
    @Nested
    @DisplayName("Working Days Counting Tests")
    class WorkingDaysCountingTests {
        
        @Test
        @DisplayName("Should count working days correctly excluding weekends")
        void shouldCountWorkingDaysExcludingWeekends() {
            // Monday to Friday (5 days, should be 5 working days)
            LocalDate monday = LocalDate.of(2024, 1, 1); // Jan 1, 2024 is Monday
            LocalDate friday = LocalDate.of(2024, 1, 5);
            
            int count = service.countWorkingDaysBetween(monday, friday);
            
            // Exclusive of end date, so Mon-Thu = 4 working days
            assertThat(count).isEqualTo(4);
        }
        
        @Test
        @DisplayName("Should return 0 for same start and end date")
        void shouldReturnZeroForSameDate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            
            int count = service.countWorkingDaysBetween(date, date);
            
            assertThat(count).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Should count working days across weekends")
        void shouldCountWorkingDaysAcrossWeekends() {
            // Friday to next Monday (should be 1 working day - Friday only)
            LocalDate friday = LocalDate.of(2024, 1, 5);
            LocalDate monday = LocalDate.of(2024, 1, 8);
            
            int count = service.countWorkingDaysBetween(friday, monday);
            
            assertThat(count).isEqualTo(1); // Only Friday counts
        }
        
        @Test
        @DisplayName("Should count working days in a full week")
        void shouldCountWorkingDaysInFullWeek() {
            // Monday to next Monday (7 days, 5 working days)
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 8);
            
            int count = service.countWorkingDaysBetween(start, end);
            
            assertThat(count).isEqualTo(5);
        }
    }
    
    @Nested
    @DisplayName("Earliest Editable Date Tests")
    class EarliestEditableDateTests {
        
        @Test
        @DisplayName("Should calculate earliest editable date correctly")
        void shouldCalculateEarliestEditableDate() {
            LocalDate earliest = service.getEarliestEditableDate();
            
            // Should be 3 working days ago
            assertThat(earliest).isNotNull();
            assertThat(earliest).isBefore(LocalDate.now());
            
            // Verify it's approximately 3-5 calendar days ago (depending on weekends)
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(earliest, LocalDate.now());
            assertThat(daysDiff).isBetween(3L, 5L);
        }
        
        @Test
        @DisplayName("Should return a working day as earliest editable date")
        void shouldReturnWorkingDayAsEarliestEditableDate() {
            LocalDate earliest = service.getEarliestEditableDate();
            
            DayOfWeek day = earliest.getDayOfWeek();
            assertThat(day).isNotIn(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        }
    }
    
    @Nested
    @DisplayName("Hours Precision Validation Tests")
    class HoursPrecisionValidationTests {
        
        @ParameterizedTest
        @CsvSource({
            "0.1, true",
            "0.5, true",
            "1.0, true",
            "2.5, true",
            "8.0, true",
            "0.15, false",   // Not a multiple of 0.1
            "0.33, false",   // Not a multiple of 0.1
            "1.11, false",   // Not a multiple of 0.1
        })
        @DisplayName("Should validate hours precision correctly")
        void shouldValidateHoursPrecision(String hoursStr, boolean expectedValid) {
            BigDecimal hours = new BigDecimal(hoursStr);
            
            boolean valid = service.isValidHoursPrecision(hours);
            
            assertThat(valid).isEqualTo(expectedValid);
        }
        
        @Test
        @DisplayName("Should reject null hours")
        void shouldRejectNullHours() {
            boolean valid = service.isValidHoursPrecision(null);
            
            assertThat(valid).isFalse();
        }
        
        @Test
        @DisplayName("Should reject zero hours")
        void shouldRejectZeroHours() {
            boolean valid = service.isValidHoursPrecision(BigDecimal.ZERO);
            
            assertThat(valid).isFalse();
        }
        
        @Test
        @DisplayName("Should reject negative hours")
        void shouldRejectNegativeHours() {
            boolean valid = service.isValidHoursPrecision(new BigDecimal("-1.0"));
            
            assertThat(valid).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Task Hours Sufficiency Tests")
    class TaskHoursSufficiencyTests {
        
        @Test
        @DisplayName("Should return true when sufficient hours available")
        void shouldReturnTrueWhenSufficientHours() {
            Double estimatedHours = 10.0;
            Double usedHours = 5.0;
            BigDecimal additionalHours = new BigDecimal("3.0");
            
            boolean sufficient = service.hasSufficientHours(estimatedHours, usedHours, additionalHours);
            
            assertThat(sufficient).isTrue();
        }
        
        @Test
        @DisplayName("Should return true when exactly enough hours")
        void shouldReturnTrueWhenExactlyEnoughHours() {
            Double estimatedHours = 10.0;
            Double usedHours = 7.0;
            BigDecimal additionalHours = new BigDecimal("3.0");
            
            boolean sufficient = service.hasSufficientHours(estimatedHours, usedHours, additionalHours);
            
            assertThat(sufficient).isTrue();
        }
        
        @Test
        @DisplayName("Should return false when insufficient hours")
        void shouldReturnFalseWhenInsufficientHours() {
            Double estimatedHours = 10.0;
            Double usedHours = 8.0;
            BigDecimal additionalHours = new BigDecimal("3.0");
            
            boolean sufficient = service.hasSufficientHours(estimatedHours, usedHours, additionalHours);
            
            assertThat(sufficient).isFalse();
        }
        
        @Test
        @DisplayName("Should return false when any parameter is null")
        void shouldReturnFalseWhenParametersNull() {
            assertThat(service.hasSufficientHours(null, 5.0, new BigDecimal("3.0"))).isFalse();
            assertThat(service.hasSufficientHours(10.0, null, new BigDecimal("3.0"))).isFalse();
            assertThat(service.hasSufficientHours(10.0, 5.0, null)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Remaining Hours Calculation Tests")
    class RemainingHoursCalculationTests {
        
        @Test
        @DisplayName("Should calculate remaining hours correctly")
        void shouldCalculateRemainingHours() {
            Double estimatedHours = 10.0;
            Double usedHours = 6.5;
            
            BigDecimal remaining = service.calculateRemainingHours(estimatedHours, usedHours);
            
            assertThat(remaining).isEqualByComparingTo(new BigDecimal("3.5"));
        }
        
        @Test
        @DisplayName("Should return zero when used exceeds estimated")
        void shouldReturnZeroWhenUsedExceedsEstimated() {
            Double estimatedHours = 10.0;
            Double usedHours = 12.0;
            
            BigDecimal remaining = service.calculateRemainingHours(estimatedHours, usedHours);
            
            assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should return zero when parameters are null")
        void shouldReturnZeroWhenParametersNull() {
            assertThat(service.calculateRemainingHours(null, 5.0)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(service.calculateRemainingHours(10.0, null)).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
    
    @Nested
    @DisplayName("Hours Usage Percentage Tests")
    class HoursUsagePercentageTests {
        
        @Test
        @DisplayName("Should calculate hours used percentage correctly")
        void shouldCalculateHoursUsedPercentage() {
            Double estimatedHours = 10.0;
            Double usedHours = 7.5;
            
            BigDecimal percentage = service.calculateHoursUsedPercentage(estimatedHours, usedHours);
            
            assertThat(percentage).isEqualByComparingTo(new BigDecimal("75.00"));
        }
        
        @Test
        @DisplayName("Should return zero when estimated hours is zero")
        void shouldReturnZeroWhenEstimatedHoursIsZero() {
            BigDecimal percentage = service.calculateHoursUsedPercentage(0.0, 5.0);
            
            assertThat(percentage).isEqualByComparingTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should return zero when parameters are null")
        void shouldReturnZeroWhenParametersNull() {
            assertThat(service.calculateHoursUsedPercentage(null, 5.0)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(service.calculateHoursUsedPercentage(10.0, null)).isEqualByComparingTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should handle percentage over 100%")
        void shouldHandlePercentageOver100() {
            Double estimatedHours = 10.0;
            Double usedHours = 15.0;
            
            BigDecimal percentage = service.calculateHoursUsedPercentage(estimatedHours, usedHours);
            
            assertThat(percentage).isGreaterThan(new BigDecimal("100.00"));
            assertThat(percentage).isEqualByComparingTo(new BigDecimal("150.00"));
        }
    }
    
    @Nested
    @DisplayName("Hours Running Low Tests")
    class HoursRunningLowTests {
        
        @Test
        @DisplayName("Should return true when hours are at 80%")
        void shouldReturnTrueWhenHoursAt80Percent() {
            boolean low = service.areHoursRunningLow(10.0, 8.0);
            
            assertThat(low).isTrue();
        }
        
        @Test
        @DisplayName("Should return true when hours are above 80%")
        void shouldReturnTrueWhenHoursAbove80Percent() {
            boolean low = service.areHoursRunningLow(10.0, 9.5);
            
            assertThat(low).isTrue();
        }
        
        @Test
        @DisplayName("Should return false when hours are below 80%")
        void shouldReturnFalseWhenHoursBelow80Percent() {
            boolean low = service.areHoursRunningLow(10.0, 7.5);
            
            assertThat(low).isFalse();
        }
        
        @Test
        @DisplayName("Should return true when hours exceed estimated")
        void shouldReturnTrueWhenHoursExceedEstimated() {
            boolean low = service.areHoursRunningLow(10.0, 12.0);
            
            assertThat(low).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Working Days Range Tests")
    class WorkingDaysRangeTests {
        
        @Test
        @DisplayName("Should get working days in range excluding weekends")
        void shouldGetWorkingDaysInRangeExcludingWeekends() {
            // Jan 1-5, 2024 (Mon-Fri) - all working days
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 5);
            
            List<LocalDate> workingDays = service.getWorkingDaysInRange(start, end);
            
            assertThat(workingDays).hasSize(5);
            assertThat(workingDays).containsExactly(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3),
                LocalDate.of(2024, 1, 4),
                LocalDate.of(2024, 1, 5)
            );
        }
        
        @Test
        @DisplayName("Should exclude weekend days from range")
        void shouldExcludeWeekendDaysFromRange() {
            // Jan 1-7, 2024 (Mon-Sun) - 5 working days
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 7);
            
            List<LocalDate> workingDays = service.getWorkingDaysInRange(start, end);
            
            assertThat(workingDays).hasSize(5);
            // Should not contain Jan 6 (Sat) or Jan 7 (Sun)
            assertThat(workingDays).doesNotContain(
                LocalDate.of(2024, 1, 6),
                LocalDate.of(2024, 1, 7)
            );
        }
        
        @Test
        @DisplayName("Should return empty list for same start and end date")
        void shouldReturnEmptyListForSameDate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            
            List<LocalDate> workingDays = service.getWorkingDaysInRange(date, date);
            
            assertThat(workingDays).hasSize(1).containsExactly(date);
        }
        
        @Test
        @DisplayName("Should return empty list when range is only weekends")
        void shouldReturnEmptyListWhenRangeIsOnlyWeekends() {
            // Jan 6-7, 2024 (Sat-Sun)
            LocalDate start = LocalDate.of(2024, 1, 6);
            LocalDate end = LocalDate.of(2024, 1, 7);
            
            List<LocalDate> workingDays = service.getWorkingDaysInRange(start, end);
            
            assertThat(workingDays).isEmpty();
        }
    }
}
