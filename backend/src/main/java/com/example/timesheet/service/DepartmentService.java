package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.dto.response.DepartmentDetailResponse;
import com.example.timesheet.dto.response.DepartmentResponse;
import com.example.timesheet.mapper.DepartmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing departments.
 * Handles department CRUD operations and department member queries.
 */
@Service
@Slf4j
@Transactional
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    
    public DepartmentService(DepartmentRepository departmentRepository,
                            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }
    
    /**
     * Get department by ID.
     *
     * @param departmentId the department ID
     * @return department response
     * @throws IllegalArgumentException if department not found
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("部門不存在"));
        return departmentMapper.departmentToDepartmentResponse(department);
    }
    
    /**
     * Get all departments with pagination.
     *
     * @param pageable pagination information
     * @return paginated department responses
     */
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        Page<Department> page = departmentRepository.findAll(pageable);
        List<DepartmentResponse> content = page.getContent()
                .stream()
                .map(departmentMapper::departmentToDepartmentResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
    
    /**
     * Get department detail response with member count.
     *
     * @param departmentId the department ID
     * @return department detail response
     * @throws IllegalArgumentException if department not found
     */
    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentDetail(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("部門不存在"));
        return departmentMapper.departmentToDepartmentDetailResponse(department);
    }
}
