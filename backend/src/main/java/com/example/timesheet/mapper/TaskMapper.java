package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.dto.response.TaskDetailResponse;
import com.example.timesheet.dto.response.TaskResponse;
import com.example.timesheet.dto.response.TaskSimpleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Task entity conversions.
 * Converts between Task entities and various DTOs.
 * Handles nested object mapping for project and assignee information.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskMapper {
    
    /**
     * Convert Task entity to TaskResponse DTO.
     * Maps basic task information without nested object details.
     *
     * @param task the task entity
     * @return TaskResponse DTO
     */
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "assignee.id", target = "assigneeId")
    TaskResponse entityToResponse(Task task);
    
    /**
     * Convert Task entity to TaskDetailResponse DTO.
     * Includes nested project and assignee information.
     *
     * @param task the task entity
     * @return TaskDetailResponse DTO
     */
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.name", target = "assigneeName")
    @Mapping(source = "assignee.email", target = "assigneeEmail")
    TaskDetailResponse entityToDetailResponse(Task task);
    
    /**
     * Convert Task entity to TaskSimpleResponse DTO.
     * Minimal fields for use in lists and nested responses.
     *
     * @param task the task entity
     * @return TaskSimpleResponse DTO
     */
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "assignee.id", target = "assigneeId")
    TaskSimpleResponse entityToSimpleResponse(Task task);
    
    /**
     * Convert list of Task entities to list of TaskResponse DTOs.
     *
     * @param tasks list of task entities
     * @return list of TaskResponse DTOs
     */
    List<TaskResponse> entitiesToResponses(List<Task> tasks);
    
    /**
     * Convert list of Task entities to list of TaskDetailResponse DTOs.
     *
     * @param tasks list of task entities
     * @return list of TaskDetailResponse DTOs
     */
    List<TaskDetailResponse> entitiesToDetailResponses(List<Task> tasks);
    
    /**
     * Convert list of Task entities to list of TaskSimpleResponse DTOs.
     *
     * @param tasks list of task entities
     * @return list of TaskSimpleResponse DTOs
     */
    List<TaskSimpleResponse> entitiesToSimpleResponses(List<Task> tasks);
}
