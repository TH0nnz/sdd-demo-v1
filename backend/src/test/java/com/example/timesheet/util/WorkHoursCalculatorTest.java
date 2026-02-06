package com.example.timesheet.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for WorkHoursCalculator utility (T118).
 * Tests lunch deduction logic, hour rounding, and validation.
 */
@DisplayName("WorkHoursCalculator Tests")
class WorkHoursCalculatorTest {

    @Test
    @DisplayName("Should calculate normal work hours without lunch deduction")
    void testNormalWorkHoursWithoutLunch() {
        // Work from 09:00 to 11:00 (no lunch overlap)
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(11, 0));
        
        assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(result.lunchDeducted()).isFalse();
        assertThat(result.lunchHours()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should deduct lunch when work period overlaps 12:00-13:00")
    void testLunchDeductionWhenOverlapLunch() {
        // Work from 10:00 to 14:00 (overlaps lunch 12:00-13:00)
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(10, 0), LocalTime.of(14, 0));
        
        // Total 4 hours - 1 hour lunch = 3 hours
        assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(result.lunchDeducted()).isTrue();
        assertThat(result.lunchHours()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Should deduct lunch when work starts exactly at lunch")
    void testLunchDeductionWhenStartsAtLunch() {
        // Work from 12:00 to 15:00
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(12, 0), LocalTime.of(15, 0));
        
        // Total 3 hours - 1 hour lunch = 2 hours
        assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(result.lunchDeducted()).isTrue();
    }

    @Test
    @DisplayName("Should deduct lunch when work ends exactly at lunch end")
    void testLunchDeductionWhenEndsAtLunchEnd() {
        // Work from 09:00 to 13:00
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(13, 0));
        
        // Total 4 hours - 1 hour lunch = 3 hours
        assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(result.lunchDeducted()).isTrue();
    }

    @Test
    @DisplayName("Should not deduct lunch when work ends before lunch")
    void testNoLunchDeductionWhenEndsBeforeLunch() {
        // Work from 09:00 to 11:59
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(11, 59));
        
        assertThat(result.lunchDeducted()).isFalse();
    }

    @Test
    @DisplayName("Should not deduct lunch when work starts at or after lunch end")
    void testNoLunchDeductionWhenStartsAfterLunch() {
        // Work from 13:00 to 17:00
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(13, 0), LocalTime.of(17, 0));
        
        assertThat(result.lunchDeducted()).isFalse();
        assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("4.0"));
    }

    @Test
    @DisplayName("Should handle partial lunch overlap (partial hour)")
    void testPartialLunchOverlap() {
        // Work from 11:30 to 12:30 (1 hour, entirely within/overlapping lunch)
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(LocalTime.of(11, 30), LocalTime.of(12, 30));
        
        // Total 1 hour - 1 hour lunch = 0 hours, but at least rounds to valid increment
        assertThat(result.lunchDeducted()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Should round to nearest 0.5 hour")
    @CsvSource({
        "1.2,1.0",
        "1.3,1.5",
        "1.7,1.5",
        "1.8,2.0",
        "2.0,2.0",
        "2.25,2.5",
        "2.74,2.5",
        "2.75,3.0"
    })
    void testRoundingToNearestHalf(String input, String expected) {
        BigDecimal rounded = WorkHoursCalculator.roundToNearestHalf(new BigDecimal(input));
        assertThat(rounded).isEqualByComparingTo(new BigDecimal(expected));
    }

    @ParameterizedTest
    @DisplayName("Should validate 0.5 hour increments")
    @CsvSource({
        "0.5,true",
        "1.0,true",
        "1.5,true",
        "2.0,true",
        "2.5,true",
        "1.2,false",
        "1.3,false",
        "1.7,false",
        "0.0,true"
    })
    void testValidHoursIncrement(String value, boolean expected) {
        boolean valid = WorkHoursCalculator.isValidHoursIncrement(new BigDecimal(value));
        assertThat(valid).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should reject negative hours for validation")
    void testRejectNegativeHours() {
        boolean valid = WorkHoursCalculator.isValidHoursIncrement(new BigDecimal("-1.0"));
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject null hours for validation")
    void testRejectNullHours() {
        boolean valid = WorkHoursCalculator.isValidHoursIncrement(null);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when end time before start time")
    void testExceptionWhenEndTimeBeforeStart() {
        assertThatThrownBy(() -> 
            WorkHoursCalculator.calculate(LocalTime.of(14, 0), LocalTime.of(10, 0))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when end time equals start time")
    void testExceptionWhenEndTimeEqualsStart() {
        assertThatThrownBy(() -> 
            WorkHoursCalculator.calculate(LocalTime.of(10, 0), LocalTime.of(10, 0))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should detect lunch period overlap")
    void testLunchPeriodDetection() {
        // Overlapping cases
        assertThat(WorkHoursCalculator.shouldDeductLunch(
            LocalTime.of(11, 30), LocalTime.of(12, 30)
        )).isTrue();

        assertThat(WorkHoursCalculator.shouldDeductLunch(
            LocalTime.of(12, 0), LocalTime.of(13, 0)
        )).isTrue();

        // Non-overlapping cases
        assertThat(WorkHoursCalculator.shouldDeductLunch(
            LocalTime.of(9, 0), LocalTime.of(11, 59)
        )).isFalse();

        assertThat(WorkHoursCalculator.shouldDeductLunch(
            LocalTime.of(13, 1), LocalTime.of(17, 0)
        )).isFalse();
    }

    @Test
    @DisplayName("Should return work hours result with message")
    void testWorkHoursResultMessage() {
        WorkHoursCalculator.WorkHoursResult resultWithLunch = 
            WorkHoursCalculator.calculate(LocalTime.of(10, 0), LocalTime.of(14, 0));
        
        String message = resultWithLunch.getMessage();
        assertThat(message)
            .contains("3.0")
            .contains("午休")
            .contains("1.0");

        WorkHoursCalculator.WorkHoursResult resultWithoutLunch = 
            WorkHoursCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(11, 0));
        
        String messageNoLunch = resultWithoutLunch.getMessage();
        assertThat(messageNoLunch)
            .contains("2.0")
            .doesNotContain("午休");
    }
}
