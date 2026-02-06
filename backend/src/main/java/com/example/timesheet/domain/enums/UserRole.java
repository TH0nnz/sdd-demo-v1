package com.example.timesheet.domain.enums;

/**
 * User roles in the timesheet management system.
 * Each role has specific permissions and responsibilities.
 */
public enum UserRole {
    /**
     * 管理層 - Can create/modify/close projects, approve time requests
     * Highest level of authority in the system
     */
    MANAGER,
    
    /**
     * 專案經理 (Project Manager) - Can create/modify tasks, request additional hours
     * Manages project scope and resource allocation
     */
    PM,
    
    /**
     * 部門主管 (Department Head) - Can view department members' timesheets
     * Oversees departmental operations and reporting
     */
    DEPT_HEAD,
    
    /**
     * 執行人員 (Executive/Employee) - Can log timesheets, mark tasks as completed
     * Primary users who perform project work and log time
     */
    EXECUTIVE,
    
    /**
     * 人力資源 (Human Resources) - Can manage users, assign roles
     * Manages user accounts and role assignments
     */
    HR
}
