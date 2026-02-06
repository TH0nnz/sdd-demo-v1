package com.example.timesheet.mapper;

import com.example.timesheet.domain.entity.User;
import com.example.timesheet.dto.request.CreateUserRequest;
import com.example.timesheet.dto.request.UpdateUserRequest;
import com.example.timesheet.dto.response.UserResponse;
import com.example.timesheet.dto.response.UserSimpleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for User entity conversions.
 * Converts between User entities and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    
    /**
     * Convert User entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    @Mapping(source = "active", target = "active")
    UserResponse userToUserResponse(User user);
    
    /**
     * Convert User entity to UserSimpleResponse DTO.
     *
     * @param user the user entity
     * @return UserSimpleResponse DTO
     */
    UserSimpleResponse userToUserSimpleResponse(User user);
    
    /**
     * Convert list of User entities to UserResponse DTOs.
     *
     * @param users list of user entities
     * @return list of UserResponse DTOs
     */
    List<UserResponse> usersToUserResponses(List<User> users);
    
    /**
     * Update User entity from CreateUserRequest.
     * This is used when we have a created user but need to map from request.
     *
     * @param request the create user request
     * @param target the target user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    @Mapping(target = "timesheetEntries", ignore = true)
    @Mapping(target = "managedProjects", ignore = true)
    @Mapping(target = "timeRequests", ignore = true)
    void updateUserFromCreateRequest(CreateUserRequest request, @MappingTarget User target);
    
    /**
     * Update User entity from UpdateUserRequest.
     * Merges update fields into existing user.
     *
     * @param request the update user request
     * @param target the target user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    @Mapping(target = "timesheetEntries", ignore = true)
    @Mapping(target = "managedProjects", ignore = true)
    @Mapping(target = "timeRequests", ignore = true)
    void updateUserFromUpdateRequest(UpdateUserRequest request, @MappingTarget User target);
}
