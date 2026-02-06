package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response DTO.
 * 
 * Provides consistent error format across all API endpoints:
 * {
 *   "timestamp": "2026-02-06T14:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "輸入資料驗證失敗",
 *   "path": "/api/users",
 *   "details": {
 *     "email": "電子郵件格式不正確",
 *     "name": "姓名不可為空"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private Integer status;
    
    /**
     * Error type (e.g., "Bad Request", "Not Found")
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * Additional error details (field-level errors, etc.)
     */
    private Map<String, String> details;
}
