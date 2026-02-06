package com.example.timesheet.domain.enums;

/**
 * User roles in the timesheet management system.
 * Each role has specific permissions and responsibilities.
 * 
 * Roles follow the specification in data-model.md:
 * EXECUTIVE, PM, MANAGER, EMPLOYEE, HR
 */
public enum UserRole {
    /**
     * 管理層 (Executive Level) - Can create/modify/close projects, approve time requests
     * Highest level of authority in the system
     */
    EXECUTIVE,
    
    /**
     * 專案經理 (Project Manager) - Can create/modify tasks, request additional hours
     * Manages project scope and resource allocation
     */
    PM,
    
    /**
     * 部門主管 (Department Manager) - Can view department members' timesheets
     * Oversees departmental operations and reporting
     */
    MANAGER,
    
    /**
     * 執行人員 (Employee) - Can log timesheets, mark tasks as completed
     * Primary users who perform project work and log time
     */
    EMPLOYEE,
    
    /**
     * 人力資源 (Human Resources) - Can manage users, assign roles
     * Manages user accounts and role assignments
     */
    HR
}
