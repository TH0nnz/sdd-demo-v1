package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.dto.response.TimesheetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.format.DateTimeFormatter;

/**
 * MapStruct mapper for TimesheetEntry (T124).
 * Converts between domain entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface TimesheetMapper {
    
    TimesheetMapper INSTANCE = Mappers.getMapper(TimesheetMapper.class);
    
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "task.name", target = "taskName")
    @Mapping(source = "task.project.id", target = "projectId")
    @Mapping(source = "task.project.name", target = "projectName")
    @Mapping(target = "createdAt", expression = "java(entry.getCreatedAt() != null ? entry.getCreatedAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    @Mapping(target = "updatedAt", expression = "java(entry.getUpdatedAt() != null ? entry.getUpdatedAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    @Mapping(target = "message", expression = "java(formatMessage(entry))")
    TimesheetResponse toResponse(TimesheetEntry entry);
    
    default String formatMessage(TimesheetEntry entry) {
        if (entry.getLunchDeducted()) {
            return String.format("工時: %.1f 小時 (已自動扣除午休)",
                entry.getCalculatedHours().doubleValue());
        }
        return String.format("工時: %.1f 小時", entry.getCalculatedHours().doubleValue());
    }
}
