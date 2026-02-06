package com.example.timesheet.service;

import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.TimesheetRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Report management (T140).
 * Handles timesheet report queries, CSV export, and pagination.
 * 
 * Business Rules:
 * - Only DEPT_HEAD, MANAGER, PM can access reports
 * - Reports are filtered by department
 * - Support date range filtering
 * - Support user and project filtering
 * - Generate CSV exports
 * - Calculate summary statistics
 * 
 * Corresponds to User Story 4 - 部門工時報表 (Department Reports)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get paginated timesheet report for a department with optional filters.
     * 
     * @param departmentId department to filter by
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @param userId optional user to filter by
     * @param projectId optional project to filter by
     * @param pageable pagination information
     * @return page of timesheet report entries
     */
    public TimesheetReportPageResponse getTimesheetReport(
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            Long userId,
            Long projectId,
            Pageable pageable) {
        
        log.debug("Generating timesheet report for department: {}, date range: {} to {}", 
            departmentId, startDate, endDate);

        // Get all timesheet entries and apply filters
        List<TimesheetEntry> allEntries = timesheetRepository.findAll();
        
        List<TimesheetEntry> filteredEntries = allEntries.stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(departmentId))
            .filter(entry -> startDate == null || !entry.getWorkDate().isBefore(startDate))
            .filter(entry -> endDate == null || !entry.getWorkDate().isAfter(endDate))
            .filter(entry -> userId == null || entry.getUser().getId().equals(userId))
            .filter(entry -> projectId == null || entry.getTask().getProject().getId().equals(projectId))
            .sorted(Comparator.comparing(TimesheetEntry::getWorkDate).reversed())
            .collect(Collectors.toList());

        // Apply pagination manually since we're filtering in memory
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredEntries.size());

        List<TimesheetReportResponse> pageContent = filteredEntries.subList(start, end)
            .stream()
            .map(this::toTimesheetReportResponse)
            .collect(Collectors.toList());

        // Create PageMetadata
        PageMetadata pageMetadata = PageMetadata.builder()
            .currentPage(pageable.getPageNumber())
            .pageSize(pageable.getPageSize())
            .totalPages((int) Math.ceil((double) filteredEntries.size() / pageable.getPageSize()))
            .totalElements((long) filteredEntries.size())
            .hasNext(end < filteredEntries.size())
            .hasPrevious(pageable.getPageNumber() > 0)
            .build();

        return TimesheetReportPageResponse.builder()
            .content(pageContent)
            .page(pageMetadata)
            .build();
    }

    /**
     * Generate CSV content for timesheet report.
     * 
     * @param departmentId department to filter by
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @param userId optional user to filter by
     * @param projectId optional project to filter by
     * @return CSV formatted string
     */
    public String generateTimesheetReportCsv(
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            Long userId,
            Long projectId) {
        
        log.debug("Generating CSV export for timesheet report");

        // Get all timesheet entries and apply filters
        List<TimesheetEntry> allEntries = timesheetRepository.findAll();
        
        List<TimesheetEntry> filteredEntries = allEntries.stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(departmentId))
            .filter(entry -> startDate == null || !entry.getWorkDate().isBefore(startDate))
            .filter(entry -> endDate == null || !entry.getWorkDate().isAfter(endDate))
            .filter(entry -> userId == null || entry.getUser().getId().equals(userId))
            .filter(entry -> projectId == null || entry.getTask().getProject().getId().equals(projectId))
            .sorted(Comparator.comparing(TimesheetEntry::getWorkDate))
            .collect(Collectors.toList());

        // Build CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Date,User,Project,Task,Hours\n");

        for (TimesheetEntry entry : filteredEntries) {
            csv.append(escapeCsvValue(entry.getWorkDate().toString()))
                .append(",").append(escapeCsvValue(entry.getUser().getName()))
                .append(",").append(escapeCsvValue(entry.getTask().getProject().getName()))
                .append(",").append(escapeCsvValue(entry.getTask().getName()))
                .append(",").append(entry.getCalculatedHours())
                .append("\n");
        }

        return csv.toString();
    }

    /**
     * Get project summary report for a department.
     * 
     * @param departmentId department to filter by
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @return list of project summary entries
     */
    public List<ProjectSummaryReportResponse> getProjectSummaryReport(
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        
        log.debug("Generating project summary report for department: {}", departmentId);

        // Get all timesheet entries and apply filters
        List<TimesheetEntry> allEntries = timesheetRepository.findAll();
        
        List<TimesheetEntry> filteredEntries = allEntries.stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(departmentId))
            .filter(entry -> startDate == null || !entry.getWorkDate().isBefore(startDate))
            .filter(entry -> endDate == null || !entry.getWorkDate().isAfter(endDate))
            .collect(Collectors.toList());

        // Group by project and calculate summary statistics
        Map<Long, List<TimesheetEntry>> groupedByProject = filteredEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getTask().getProject().getId()));

        return groupedByProject.entrySet()
            .stream()
            .map(entry -> {
                TimesheetEntry firstEntry = entry.getValue().get(0);
                BigDecimal totalHours = entry.getValue()
                    .stream()
                    .map(TimesheetEntry::getCalculatedHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Set<Long> uniqueUsers = entry.getValue()
                    .stream()
                    .map(e -> e.getUser().getId())
                    .collect(Collectors.toSet());

                return ProjectSummaryReportResponse.builder()
                    .projectId(firstEntry.getTask().getProject().getId())
                    .projectName(firstEntry.getTask().getProject().getName())
                    .totalHours(totalHours.toPlainString())
                    .entryCount(entry.getValue().size())
                    .assignedUserCount((long) uniqueUsers.size())
                    .build();
            })
            .sorted(Comparator.comparing(ProjectSummaryReportResponse::getTotalHours).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get user summary report for a department.
     * 
     * @param departmentId department to filter by
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @return list of user summary entries
     */
    public List<UserSummaryReportResponse> getUserSummaryReport(
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        
        log.debug("Generating user summary report for department: {}", departmentId);

        // Get all timesheet entries and apply filters
        List<TimesheetEntry> allEntries = timesheetRepository.findAll();
        
        List<TimesheetEntry> filteredEntries = allEntries.stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(departmentId))
            .filter(entry -> startDate == null || !entry.getWorkDate().isBefore(startDate))
            .filter(entry -> endDate == null || !entry.getWorkDate().isAfter(endDate))
            .collect(Collectors.toList());

        // Group by user and calculate summary statistics
        Map<Long, List<TimesheetEntry>> groupedByUser = filteredEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getUser().getId()));

        return groupedByUser.entrySet()
            .stream()
            .map(entry -> {
                TimesheetEntry firstEntry = entry.getValue().get(0);
                BigDecimal totalHours = entry.getValue()
                    .stream()
                    .map(TimesheetEntry::getCalculatedHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Set<Long> uniqueProjects = entry.getValue()
                    .stream()
                    .map(e -> e.getTask().getProject().getId())
                    .collect(Collectors.toSet());

                return UserSummaryReportResponse.builder()
                    .userId(firstEntry.getUser().getId())
                    .userName(firstEntry.getUser().getName())
                    .totalHours(totalHours.toPlainString())
                    .entryCount(entry.getValue().size())
                    .projectCount(uniqueProjects.size())
                    .build();
            })
            .sorted(Comparator.comparing(UserSummaryReportResponse::getTotalHours).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Convert TimesheetEntry to TimesheetReportResponse DTO.
     */
    private TimesheetReportResponse toTimesheetReportResponse(TimesheetEntry entry) {
        return TimesheetReportResponse.builder()
            .id(entry.getId())
            .date(entry.getWorkDate().toString())
            .userName(entry.getUser().getName())
            .userId(entry.getUser().getId())
            .projectName(entry.getTask().getProject().getName())
            .projectId(entry.getTask().getProject().getId())
            .taskName(entry.getTask().getName())
            .taskId(entry.getTask().getId())
            .hours(entry.getCalculatedHours())
            .build();
    }

    /**
     * Escape CSV values to handle special characters.
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
