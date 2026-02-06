package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.repository.TaskRepository;
import com.example.timesheet.domain.repository.TimesheetRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.dto.response.TimesheetPageResponse;
import com.example.timesheet.dto.response.TimesheetResponse;
import com.example.timesheet.dto.response.WorkHoursCalculationResponse;
import com.example.timesheet.mapper.TimesheetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimesheetService.
 * Tests timesheet creation, updates, deletion, and work hours calculations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetService Tests")
class TimesheetServiceTest {
    
    @Mock
    private TimesheetRepository timesheetRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TimesheetMapper timesheetMapper;
    
    @InjectMocks
    private TimesheetService timesheetService;
    
    private User testUser;
    private Task testTask;
    private TimesheetEntry testEntry;
    
    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        
        // Setup test task
        testTask = new Task();
        testTask.setId(100L);
        testTask.setName("Test Task");
        testTask.setEstimatedHours(10.0);
        testTask.setUsedHours(5.0);
        
        // Setup test timesheet entry
        testEntry = TimesheetEntry.builder()
            .id(1L)
            .user(testUser)
            .task(testTask)
            .workDate(LocalDate.now())
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(17, 0))
            .calculatedHours(new BigDecimal("7.0"))
            .lunchDeducted(true)
            .build();
    }
    
    @Nested
    @DisplayName("Calculate Preview Tests")
    class CalculatePreviewTests {
        
        @Test
        @DisplayName("Should calculate preview successfully for valid times")
        void shouldCalculatePreviewForValidTimes() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setStartTime(LocalTime.of(9, 0));
            request.setEndTime(LocalTime.of(14, 0));
            
            WorkHoursCalculationResponse response = timesheetService.calculatePreview(request);
            
            assertThat(response).isNotNull();
            assertThat(response.getValid()).isTrue();
            assertThat(response.getCalculatedHours()).isEqualByComparingTo(new BigDecimal("4.0"));
            assertThat(response.getLunchDeducted()).isTrue();
        }
        
        @Test
        @DisplayName("Should reject when end time equals start time")
        void shouldRejectWhenEndTimeEqualsStartTime() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            LocalTime time = LocalTime.of(9, 0);
            request.setStartTime(time);
            request.setEndTime(time);
            
            WorkHoursCalculationResponse response = timesheetService.calculatePreview(request);
            
            assertThat(response.getValid()).isFalse();
            assertThat(response.getValidationMessage()).contains("結束時間必須晚於開始時間");
        }
        
        @Test
        @DisplayName("Should reject when end time is before start time")
        void shouldRejectWhenEndTimeBeforeStartTime() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setStartTime(LocalTime.of(17, 0));
            request.setEndTime(LocalTime.of(9, 0));
            
            WorkHoursCalculationResponse response = timesheetService.calculatePreview(request);
            
            assertThat(response.getValid()).isFalse();
            assertThat(response.getValidationMessage()).contains("結束時間必須晚於開始時間");
        }
    }
    
    @Nested
    @DisplayName("Create Timesheet Tests")
    class CreateTimesheetTests {
        
        @Test
        @DisplayName("Should create timesheet successfully")
        void shouldCreateTimesheetSuccessfully() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setTaskId(100L);
            request.setWorkDate(LocalDate.now());
            request.setStartTime(LocalTime.of(9, 0));
            request.setEndTime(LocalTime.of(14, 0));
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
            when(timesheetRepository.save(any(TimesheetEntry.class))).thenReturn(testEntry);
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(timesheetMapper.toResponse(any(TimesheetEntry.class))).thenReturn(new TimesheetResponse());
            
            TimesheetResponse response = timesheetService.createTimesheet(request, 1L);
            
            assertThat(response).isNotNull();
            verify(timesheetRepository).save(any(TimesheetEntry.class));
        }
        
        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setTaskId(100L);
            request.setWorkDate(LocalDate.now());
            request.setStartTime(LocalTime.of(9, 0));
            request.setEndTime(LocalTime.of(14, 0));
            
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> timesheetService.createTimesheet(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        }
        
        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setTaskId(100L);
            request.setWorkDate(LocalDate.now());
            request.setStartTime(LocalTime.of(9, 0));
            request.setEndTime(LocalTime.of(14, 0));
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(taskRepository.findById(100L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> timesheetService.createTimesheet(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");
        }
    }
    
    @Nested
    @DisplayName("Update Timesheet Tests")
    class UpdateTimesheetTests {
        
        @Test
        @DisplayName("Should update timesheet successfully")
        void shouldUpdateTimesheetSuccessfully() {
            UpdateTimesheetRequest request = new UpdateTimesheetRequest();
            request.setStartTime(LocalTime.of(10, 0));
            request.setEndTime(LocalTime.of(15, 0));
            
            testEntry.setWorkDate(LocalDate.now());
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(timesheetRepository.findById(1L)).thenReturn(Optional.of(testEntry));
            when(timesheetRepository.save(any(TimesheetEntry.class))).thenReturn(testEntry);
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(timesheetMapper.toResponse(any(TimesheetEntry.class))).thenReturn(new TimesheetResponse());
            
            TimesheetResponse response = timesheetService.updateTimesheet(1L, request, 1L);
            
            assertThat(response).isNotNull();
            verify(timesheetRepository).save(any(TimesheetEntry.class));
        }
        
        @Test
        @DisplayName("Should throw exception when user is not owner")
        void shouldThrowExceptionWhenUserIsNotOwner() {
            UpdateTimesheetRequest request = new UpdateTimesheetRequest();
            request.setStartTime(LocalTime.of(10, 0));
            request.setEndTime(LocalTime.of(15, 0));
            
            User otherUser = new User();
            otherUser.setId(2L);
            
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(timesheetRepository.findById(1L)).thenReturn(Optional.of(testEntry));
            
            assertThatThrownBy(() -> timesheetService.updateTimesheet(1L, request, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot edit timesheet of another user");
        }
    }
    
    @Nested
    @DisplayName("Delete Timesheet Tests")
    class DeleteTimesheetTests {
        
        @Test
        @DisplayName("Should delete timesheet successfully")
        void shouldDeleteTimesheetSuccessfully() {
            when(timesheetRepository.findById(1L)).thenReturn(Optional.of(testEntry));
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            
            timesheetService.deleteTimesheet(1L, 1L);
            
            verify(timesheetRepository).delete(testEntry);
        }
        
        @Test
        @DisplayName("Should throw exception when user is not owner")
        void shouldThrowExceptionWhenUserIsNotOwner() {
            when(timesheetRepository.findById(1L)).thenReturn(Optional.of(testEntry));
            
            assertThatThrownBy(() -> timesheetService.deleteTimesheet(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete timesheet of another user");
        }
    }
    
    @Nested
    @DisplayName("Get Timesheet Tests")
    class GetTimesheetTests {
        
        @Test
        @DisplayName("Should get timesheet by ID successfully")
        void shouldGetTimesheetByIdSuccessfully() {
            when(timesheetRepository.findById(1L)).thenReturn(Optional.of(testEntry));
            when(timesheetMapper.toResponse(testEntry)).thenReturn(new TimesheetResponse());
            
            TimesheetResponse response = timesheetService.getTimesheetById(1L);
            
            assertThat(response).isNotNull();
            verify(timesheetRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Should get user timesheets with pagination")
        void shouldGetUserTimesheetsWithPagination() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            Pageable pageable = PageRequest.of(0, 20);
            Page<TimesheetEntry> page = new PageImpl<>(Collections.singletonList(testEntry));
            
            when(timesheetRepository.findByUserIdAndDateRange(1L, startDate, endDate, pageable))
                .thenReturn(page);
            when(timesheetMapper.toResponse(any(TimesheetEntry.class)))
                .thenReturn(new TimesheetResponse());
            
            TimesheetPageResponse response = timesheetService.getUserTimesheets(1L, startDate, endDate, pageable);
            
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }
}
