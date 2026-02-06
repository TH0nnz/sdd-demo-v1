package com.example.timesheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple message response DTO.
 * 
 * Used for operations that don't return data but need to confirm success.
 * 
 * Example:
 * {
 *   "message": "用戶已成功停用"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    /**
     * Success or info message
     */
    private String message;
    
    /**
     * Create success message response
     */
    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
    
    /**
     * Create error message response
     */
    public static MessageResponse error(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
}
