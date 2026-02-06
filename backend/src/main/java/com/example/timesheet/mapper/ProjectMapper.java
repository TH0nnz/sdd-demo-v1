package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.dto.response.ProjectDashboardResponse;
import com.example.timesheet.dto.response.ProjectDetailResponse;
import com.example.timesheet.dto.response.ProjectSimpleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Project entity conversions.
 * Converts between Project entities and various DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {
    
    /**
     * Convert Project entity to ProjectDetailResponse DTO.
     *
     * @param project the project entity
     * @return ProjectDetailResponse DTO
     */
    @Mapping(source = "pm.id", target = "pmId")
    @Mapping(source = "pm.name", target = "pmName")
    @Mapping(source = "status", target = "status", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ProjectDetailResponse entityToDetailResponse(Project project);
    
    /**
     * Convert Project entity to ProjectSimpleResponse DTO.
     *
     * @param project the project entity
     * @return ProjectSimpleResponse DTO
     */
    @Mapping(source = "pm.id", target = "pmId")
    @Mapping(source = "pm.name", target = "pmName")
    @Mapping(source = "status", target = "status")
    ProjectSimpleResponse entityToSimpleResponse(Project project);
    
    /**
     * Convert Project entity to ProjectDashboardResponse DTO.
     * For dashboard display with calculated metrics.
     *
     * @param project the project entity
     * @return ProjectDashboardResponse DTO
     */
    @Mapping(source = "status", target = "status")
    ProjectDashboardResponse entityToDashboardResponse(Project project);
    
    /**
     * Convert list of Project entities to list of ProjectDetailResponse DTOs.
     *
     * @param projects list of project entities
     * @return list of ProjectDetailResponse DTOs
     */
    List<ProjectDetailResponse> entitiesToDetailResponses(List<Project> projects);
    
    /**
     * Convert list of Project entities to list of ProjectSimpleResponse DTOs.
     *
     * @param projects list of project entities
     * @return list of ProjectSimpleResponse DTOs
     */
    List<ProjectSimpleResponse> entitiesToSimpleResponses(List<Project> projects);
}
