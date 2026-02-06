package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.TimeRequest;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.TimeRequestStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.TimeRequestRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.ApproveTimeRequestRequest;
import com.example.timesheet.dto.request.CreateTimeRequestRequest;
import com.example.timesheet.dto.response.TimeRequestResponse;
import com.example.timesheet.mapper.TimeRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for TimeRequest management.
 * Handles time request creation, approval, and rejection.
 * Only MANAGER can approve/reject requests.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TimeRequestService {
    
    private final TimeRequestRepository timeRequestRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final TimeRequestMapper timeRequestMapper;
    
    /**
     * Create a time request for additional project hours.
     * Typically submitted by PM when project needs more time.
     *
     * @param request Time request creation details
     * @param requesterId ID of the user creating the request (PM)
     * @return Created time request details
     * @throws IllegalArgumentException if project not found
     */
    public TimeRequestResponse createTimeRequest(CreateTimeRequestRequest request, Long requesterId) {
        log.info("Creating time request for project: {} by user: {}", request.getProjectId(), requesterId);
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found"));
        
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + request.getProjectId()));
        
        TimeRequest timeRequest = TimeRequest.builder()
            .project(project)
            .requestedHours(request.getRequestedHours())
            .reason(request.getReason())
            .status(TimeRequestStatus.PENDING)
            .requester(requester)
            .build();
        
        timeRequest = timeRequestRepository.save(timeRequest);
        log.info("Time request created with id: {}", timeRequest.getId());
        
        return timeRequestMapper.entityToResponse(timeRequest);
    }
    
    /**
     * Get time request details by ID.
     *
     * @param requestId Time request ID
     * @return Time request details
     * @throws IllegalArgumentException if request not found
     */
    public TimeRequestResponse getTimeRequestById(Long requestId) {
        log.debug("Fetching time request with id: {}", requestId);
        
        TimeRequest timeRequest = timeRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Time request not found with id: " + requestId));
        
        return timeRequestMapper.entityToResponse(timeRequest);
    }
    
    /**
     * Get pending time requests for manager approval with pagination.
     *
     * @param pageable Pagination parameters
     * @return Paginated pending request list
     */
    public Page<TimeRequestResponse> getPendingTimeRequests(Pageable pageable) {
        log.debug("Fetching pending time requests");
        
        Page<TimeRequest> page = timeRequestRepository.findByStatus(TimeRequestStatus.PENDING, pageable);
        return page.map(timeRequestMapper::entityToResponse);
    }
    
    /**
     * Get time requests for a project with pagination.
     *
     * @param projectId Project ID
     * @param pageable Pagination parameters
     * @return Paginated request list
     */
    public Page<TimeRequestResponse> getTimeRequestsByProject(Long projectId, Pageable pageable) {
        log.debug("Fetching time requests for project: {}", projectId);
        
        Page<TimeRequest> page = timeRequestRepository.findByProjectId(projectId, pageable);
        return page.map(timeRequestMapper::entityToResponse);
    }
    
    /**
     * Get time requests submitted by a requester with pagination.
     *
     * @param requesterId Requester user ID
     * @param pageable Pagination parameters
     * @return Paginated request list
     */
    public Page<TimeRequestResponse> getTimeRequestsByRequester(Long requesterId, Pageable pageable) {
        log.debug("Fetching time requests for requester: {}", requesterId);
        
        Page<TimeRequest> page = timeRequestRepository.findByRequesterId(requesterId, pageable);
        return page.map(timeRequestMapper::entityToResponse);
    }
    
    /**
     * Approve or reject a time request.
     * Only MANAGER can approve/reject requests.
     *
     * @param requestId Time request ID
     * @param request Approval/rejection details
     * @param approverId ID of the manager approving/rejecting
     * @return Updated time request details
     * @throws IllegalArgumentException if request not found, approver is not MANAGER, or request already decided
     */
    public TimeRequestResponse approveTimeRequest(Long requestId, ApproveTimeRequestRequest request, Long approverId) {
        log.info("Processing time request: {} (approved: {}) by user: {}", 
            requestId, request.getApproved(), approverId);
        
        // Verify user is MANAGER
        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not found"));
        
        if (approver.getRole() != UserRole.MANAGER) {
            throw new IllegalArgumentException("Only MANAGER role can approve/reject time requests");
        }
        
        // Get the time request
        TimeRequest timeRequest = timeRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Time request not found with id: " + requestId));
        
        // Verify request is still pending
        if (timeRequest.getStatus() != TimeRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                String.format("Time request has already been %s", timeRequest.getStatus().toString().toLowerCase())
            );
        }
        
        // Set common fields
        timeRequest.setApprover(approver);
        timeRequest.setApprovedAt(LocalDateTime.now());
        timeRequest.setApprovalReason(request.getReason());
        
        // Handle approval/rejection
        if (request.getApproved()) {
            timeRequest.setStatus(TimeRequestStatus.APPROVED);
            
            // Add hours to project
            Project project = timeRequest.getProject();
            projectService.addHours(project.getId(), timeRequest.getRequestedHours());
            
            log.info("Time request approved: {}. Added {} hours to project: {}",
                requestId, timeRequest.getRequestedHours(), project.getId());
        } else {
            timeRequest.setStatus(TimeRequestStatus.REJECTED);
            log.info("Time request rejected: {}", requestId);
        }
        
        timeRequest = timeRequestRepository.save(timeRequest);
        
        return timeRequestMapper.entityToResponse(timeRequest);
    }
}
