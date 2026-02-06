package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.TimeRequest;
import com.example.timesheet.dto.response.TimeRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for TimeRequest entity conversions.
 * Converts between TimeRequest entities and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TimeRequestMapper {
    
    /**
     * Convert TimeRequest entity to TimeRequestResponse DTO.
     *
     * @param timeRequest the time request entity
     * @return TimeRequestResponse DTO
     */
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "requester.name", target = "requesterName")
    @Mapping(source = "approver.id", target = "approverId")
    @Mapping(source = "approver.name", target = "approverName")
    @Mapping(source = "status", target = "status")
    TimeRequestResponse entityToResponse(TimeRequest timeRequest);
    
    /**
     * Convert list of TimeRequest entities to list of TimeRequestResponse DTOs.
     *
     * @param timeRequests list of time request entities
     * @return list of TimeRequestResponse DTOs
     */
    List<TimeRequestResponse> entitiesToResponses(List<TimeRequest> timeRequests);
}
