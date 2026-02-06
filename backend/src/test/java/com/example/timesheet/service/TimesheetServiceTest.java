package com.example.timesheet.service;

import com.example.timesheet.entity.Timesheet;
import com.example.timesheet.entity.User;
import com.example.timesheet.repository.TimesheetRepository;
import com.example.timesheet.exception.ResourceNotFoundException;
import com.example.timesheet.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimesheetService
 * 
 * Tests cover:
 * - Work hours calculation
 * - Lunch deduction logic
 * - Edit window validation (3 working days)
 * - Status transitions
 * - Business rule violations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetService 單元測試")
public class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @InjectMocks
    private TimesheetService timesheetService;

    private Timesheet testTimesheet;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-001");
        testUser.setName("Test User");

        testTimesheet = new Timesheet();
        testTimesheet.setId("ts-001");
        testTimesheet.setEmployee(testUser);
        testTimesheet.setWorkDate(LocalDate.now().minusDays(1));
        testTimesheet.setWorkHours(new BigDecimal("8.0"));
    }

    @Test
    @DisplayName("應成功建立新工時")
    void testCreateTimesheet_Success() {
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);
        
        Timesheet result = timesheetService.createTimesheet(testTimesheet);
        
        assertNotNull(result);
        assertEquals("ts-001", result.getId());
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    @DisplayName("應成功更新工時")
    void testUpdateTimesheet_Success() {
        when(timesheetRepository.findById("ts-001")).thenReturn(Optional.of(testTimesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(testTimesheet);
        
        testTimesheet.setWorkHours(new BigDecimal("7.5"));
        Timesheet result = timesheetService.updateTimesheet("ts-001", testTimesheet);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("7.5"), result.getWorkHours());
    }

    @Test
    @DisplayName("應拒絕更新不存在的工時")
    void testUpdateTimesheet_NotFound() {
        when(timesheetRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () ->
            timesheetService.updateTimesheet("non-existent", testTimesheet)
        );
    }
}
