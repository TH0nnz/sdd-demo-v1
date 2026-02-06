package com.example.timesheet.controller;

import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.ApproveTimeRequestRequest;
import com.example.timesheet.dto.request.CreateTimeRequestRequest;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.TimeRequestResponse;
import com.example.timesheet.service.TimeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for time request management.
 * Handles time request creation, approval, and rejection.
 * EXECUTIVE users approve/reject time requests.
 * PM users create time requests for additional hours.
 * 
 * Corresponds to User Story 3 - 管理層審批時數申請
 */
@RestController
@RequestMapping("/api/time-requests")
@Tag(name = "TimeRequests", description = "時數申請 API (Manager/PM)")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class TimeRequestController {
    
    private final TimeRequestService timeRequestService;
    private final UserRepository userRepository;
    
    public TimeRequestController(TimeRequestService timeRequestService, UserRepository userRepository) {
        this.timeRequestService = timeRequestService;
        this.userRepository = userRepository;
    }
    
    /**
     * Get all pending time requests for manager approval.
     * Only EXECUTIVE can view all pending requests.
     * 
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated pending request list
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "查詢待審批時數申請")
    public ResponseEntity<Page<TimeRequestResponse>> getPendingTimeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching pending time requests - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimeRequestResponse> response = timeRequestService.getPendingTimeRequests(pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time request details by ID.
     * 
     * @param requestId Time request ID
     * @return time request details
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('EXECUTIVE', 'PM')")
    @Operation(summary = "取得時數申請詳細資訊")
    public ResponseEntity<TimeRequestResponse> getTimeRequest(@PathVariable Long requestId) {
        log.debug("Fetching time request: {}", requestId);
        
        TimeRequestResponse response = timeRequestService.getTimeRequestById(requestId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time requests for a project.
     * 
     * @param projectId Project ID
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated time request list for the project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('EXECUTIVE', 'PM')")
    @Operation(summary = "查詢專案的時數申請")
    public ResponseEntity<Page<TimeRequestResponse>> getTimeRequestsByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching time requests for project: {} - page: {}, size: {}", projectId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimeRequestResponse> response = timeRequestService.getTimeRequestsByProject(projectId, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time requests submitted by current user.
     * 
     * @param page page number (0-indexed)
     * @param size page size
     * @param authentication current authenticated user
     * @return paginated time request list for the requester
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('PM', 'EXECUTIVE')")
    @Operation(summary = "查詢我的時數申請")
    public ResponseEntity<Page<TimeRequestResponse>> getMyTimeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.debug("Fetching time requests for user: {} - page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TimeRequestResponse> response = timeRequestService.getTimeRequestsByRequester(userId, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new time request for additional project hours.
     * PM submits requests when project needs more time.
     * 
     * @param request time request creation details
     * @param authentication current authenticated user
     * @return created time request
     */
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @Operation(summary = "建立時數申請")
    public ResponseEntity<TimeRequestResponse> createTimeRequest(
            @Valid @RequestBody CreateTimeRequestRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Creating time request for project: {} by user: {}", request.getProjectId(), userId);
        
        TimeRequestResponse response = timeRequestService.createTimeRequest(request, userId);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    /**
     * Approve or reject a time request.
     * Only EXECUTIVE can approve/reject requests.
     * Approved requests will auto-increment project total hours.
     * 
     * @param requestId Time request ID
     * @param request approval/rejection details
     * @param authentication current authenticated user
     * @return updated time request
     */
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "審批時數申請")
    public ResponseEntity<TimeRequestResponse> approveTimeRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ApproveTimeRequestRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Processing time request: {} (approved: {}) by user: {}", 
            requestId, request.getApproved(), userId);
        
        TimeRequestResponse response = timeRequestService.approveTimeRequest(requestId, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exception handler for IllegalArgumentException.
     * Maps business logic errors to appropriate HTTP status codes.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Business logic error: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(MessageResponse.error(ex.getMessage()));
    }
}
