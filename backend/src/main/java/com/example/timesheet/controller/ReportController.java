package com.example.timesheet.controller;

import com.example.timesheet.dto.response.ProjectSummaryReportResponse;
import com.example.timesheet.dto.response.TimesheetReportPageResponse;
import com.example.timesheet.dto.response.UserSummaryReportResponse;
import com.example.timesheet.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST Controller for report management (T142).
 * Handles timesheet report queries and CSV export.
 * Only MANAGER, EXECUTIVE, PM roles can access these endpoints.
 * 
 * Corresponds to User Story 4 - 部門工時報表 (Department Reports)
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "工時報表 API (Manager / Executive / PM)")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Get paginated timesheet report for a department.
     * 
     * Supports filtering by:
     * - Department ID (required)
     * - Date range (optional)
     * - User ID (optional)
     * - Project ID (optional)
     * 
     * @param departmentId required department ID
     * @param startDate optional start date (ISO format: YYYY-MM-DD)
     * @param endDate optional end date (ISO format: YYYY-MM-DD)
     * @param userId optional user ID to filter by
     * @param projectId optional project ID to filter by
     * @param page page number (0-indexed, default: 0)
     * @param size page size (default: 20)
     * @return paginated timesheet report
     */
    @GetMapping("/timesheets")
    @PreAuthorize("hasAnyRole('MANAGER', 'EXECUTIVE', 'PM')")
    @Operation(
        summary = "Get timesheet report with optional filters",
        description = "Retrieve timesheet entries for a department with date, user, and project filters"
    )
    public ResponseEntity<TimesheetReportPageResponse> getTimesheetReport(
            @Parameter(description = "Department ID", required = true)
            @RequestParam Long departmentId,
            
            @Parameter(description = "Start date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String endDate,
            
            @Parameter(description = "User ID to filter by")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Project ID to filter by")
            @RequestParam(required = false) Long projectId,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching timesheet report for department: {}", departmentId);

        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate, DATE_FORMATTER) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate, DATE_FORMATTER) : null;
        Pageable pageable = PageRequest.of(page, size);

        TimesheetReportPageResponse response = reportService.getTimesheetReport(
            departmentId,
            parsedStartDate,
            parsedEndDate,
            userId,
            projectId,
            pageable
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Export timesheet report as CSV file.
     * 
     * Supports the same filters as getTimesheetReport.
     * 
     * @param departmentId required department ID
     * @param startDate optional start date (ISO format: YYYY-MM-DD)
     * @param endDate optional end date (ISO format: YYYY-MM-DD)
     * @param userId optional user ID to filter by
     * @param projectId optional project ID to filter by
     * @return CSV file content
     */
    @GetMapping("/timesheets/export")
    @PreAuthorize("hasAnyRole('MANAGER', 'EXECUTIVE', 'PM')")
    @Operation(
        summary = "Export timesheet report as CSV",
        description = "Download timesheet report as CSV file with optional filters"
    )
    public ResponseEntity<String> exportTimesheetReportCsv(
            @Parameter(description = "Department ID", required = true)
            @RequestParam Long departmentId,
            
            @Parameter(description = "Start date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String endDate,
            
            @Parameter(description = "User ID to filter by")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Project ID to filter by")
            @RequestParam(required = false) Long projectId) {
        
        log.info("Exporting timesheet report as CSV for department: {}", departmentId);

        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate, DATE_FORMATTER) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate, DATE_FORMATTER) : null;

        String csvContent = reportService.generateTimesheetReportCsv(
            departmentId,
            parsedStartDate,
            parsedEndDate,
            userId,
            projectId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "timesheet-report.csv");

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }

    /**
     * Get project summary report for a department.
     * 
     * Shows aggregated hours and entry count by project.
     * 
     * @param departmentId required department ID
     * @param startDate optional start date (ISO format: YYYY-MM-DD)
     * @param endDate optional end date (ISO format: YYYY-MM-DD)
     * @return list of project summaries
     */
    @GetMapping("/projects/summary")
    @PreAuthorize("hasAnyRole('MANAGER', 'EXECUTIVE', 'PM')")
    @Operation(
        summary = "Get project summary report",
        description = "Retrieve aggregated timesheet data grouped by project"
    )
    public ResponseEntity<List<ProjectSummaryReportResponse>> getProjectSummary(
            @Parameter(description = "Department ID", required = true)
            @RequestParam Long departmentId,
            
            @Parameter(description = "Start date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String endDate) {
        
        log.info("Fetching project summary report for department: {}", departmentId);

        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate, DATE_FORMATTER) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate, DATE_FORMATTER) : null;

        List<ProjectSummaryReportResponse> response = reportService.getProjectSummaryReport(
            departmentId,
            parsedStartDate,
            parsedEndDate
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get user summary report for a department.
     * 
     * Shows aggregated hours and entry count by user.
     * 
     * @param departmentId required department ID
     * @param startDate optional start date (ISO format: YYYY-MM-DD)
     * @param endDate optional end date (ISO format: YYYY-MM-DD)
     * @return list of user summaries
     */
    @GetMapping("/users/summary")
    @PreAuthorize("hasAnyRole('MANAGER', 'EXECUTIVE', 'PM')")
    @Operation(
        summary = "Get user summary report",
        description = "Retrieve aggregated timesheet data grouped by user"
    )
    public ResponseEntity<List<UserSummaryReportResponse>> getUserSummary(
            @Parameter(description = "Department ID", required = true)
            @RequestParam Long departmentId,
            
            @Parameter(description = "Start date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date (ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) String endDate) {
        
        log.info("Fetching user summary report for department: {}", departmentId);

        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate, DATE_FORMATTER) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate, DATE_FORMATTER) : null;

        List<UserSummaryReportResponse> response = reportService.getUserSummaryReport(
            departmentId,
            parsedStartDate,
            parsedEndDate
        );

        return ResponseEntity.ok(response);
    }
}
