package com.example.timesheet.controller;

import com.example.timesheet.dto.response.DepartmentDetailResponse;
import com.example.timesheet.dto.response.DepartmentResponse;
import com.example.timesheet.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for department management.
 * Handles department CRUD operations and department information queries.
 * Available to HR and department heads.
 */
@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "部門管理 API (HR、部門主管)")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }
    
    /**
     * Get all departments with pagination.
     * 
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated department list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'DEPT_HEAD', 'MANAGER')")
    @Operation(summary = "查詢部門列表")
    public ResponseEntity<Page<DepartmentResponse>> listDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Listing departments");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentResponse> response = departmentService.getAllDepartments(pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific department by ID.
     * 
     * @param departmentId the department ID
     * @return department response
     */
    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('HR', 'DEPT_HEAD', 'MANAGER', 'PM')")
    @Operation(summary = "取得部門詳情")
    public ResponseEntity<DepartmentResponse> getDepartment(@PathVariable Long departmentId) {
        log.info("Getting department: {}", departmentId);
        
        try {
            DepartmentResponse response = departmentService.getDepartmentById(departmentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get detailed department information including member count.
     * 
     * @param departmentId the department ID
     * @return department detail response
     */
    @GetMapping("/{departmentId}/detail")
    @PreAuthorize("hasAnyRole('HR', 'DEPT_HEAD', 'MANAGER')")
    @Operation(summary = "取得部門詳細資訊", description = "包含部門成員數量和主管信息")
    public ResponseEntity<DepartmentDetailResponse> getDepartmentDetail(@PathVariable Long departmentId) {
        log.info("Getting department detail: {}", departmentId);
        
        try {
            DepartmentDetailResponse response = departmentService.getDepartmentDetail(departmentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
