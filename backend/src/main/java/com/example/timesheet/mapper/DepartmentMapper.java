package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.dto.response.DepartmentDetailResponse;
import com.example.timesheet.dto.response.DepartmentResponse;
import com.example.timesheet.dto.response.DepartmentSimpleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Department entity conversions.
 * Converts between Department entities and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class}
)
public interface DepartmentMapper {
    
    /**
     * Convert Department entity to DepartmentResponse DTO.
     *
     * @param department the department entity
     * @return DepartmentResponse DTO
     */
    DepartmentResponse departmentToDepartmentResponse(Department department);
    
    /**
     * Convert Department entity to DepartmentSimpleResponse DTO.
     *
     * @param department the department entity
     * @return DepartmentSimpleResponse DTO
     */
    DepartmentSimpleResponse departmentToDepartmentSimpleResponse(Department department);
    
    /**
     * Convert Department entity to DepartmentDetailResponse DTO.
     * This includes computed fields like member count.
     *
     * @param department the department entity
     * @return DepartmentDetailResponse DTO
     */
    DepartmentDetailResponse departmentToDepartmentDetailResponse(Department department);
    
    /**
     * Convert list of Department entities to DepartmentResponse DTOs.
     *
     * @param departments list of department entities
     * @return list of DepartmentResponse DTOs
     */
    List<DepartmentResponse> departmentsToDepartmentResponses(List<Department> departments);
}
