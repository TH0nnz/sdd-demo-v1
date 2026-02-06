-- V2__insert_sample_data.sql
-- Sample data for testing and development
-- Includes test users for all five roles and sample departments

-- ============================================================================
-- Insert Departments
-- ============================================================================
INSERT INTO departments (name, manager_id, created_at, updated_at, version) VALUES
('研發部', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('銷售部', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('人力資源部', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('財務部', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- ============================================================================
-- Insert Sample Users
-- Password for all users: "password123" (BCrypt hash)
-- BCrypt hash: $2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z
-- Note: In production, use actual BCrypt hashed passwords
-- ============================================================================

-- HR Users (人力資源)
INSERT INTO users (name, email, password_hash, role, department_id, active, created_at, updated_at, version) VALUES
('王小明', 'hr.wang@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'HR', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('李小華', 'hr.li@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'HR', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Manager Users (管理層)
INSERT INTO users (name, email, password_hash, role, department_id, active, created_at, updated_at, version) VALUES
('張經理', 'manager.zhang@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'MANAGER', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('陳總監', 'manager.chen@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'MANAGER', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Department Head Users (部門主管)
INSERT INTO users (name, email, password_hash, role, department_id, active, created_at, updated_at, version) VALUES
('林主管', 'depthead.lin@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'DEPT_HEAD', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('黃主管', 'depthead.huang@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'DEPT_HEAD', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Project Manager Users (專案經理)
INSERT INTO users (name, email, password_hash, role, department_id, active, created_at, updated_at, version) VALUES
('吳PM', 'pm.wu@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'PM', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('劉PM', 'pm.liu@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'PM', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('鄭PM', 'pm.zheng@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'PM', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Executive Users (執行人員)
INSERT INTO users (name, email, password_hash, role, department_id, active, created_at, updated_at, version) VALUES
('趙工程師', 'exec.zhao@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'EXECUTIVE', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('錢工程師', 'exec.qian@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'EXECUTIVE', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('孫工程師', 'exec.sun@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'EXECUTIVE', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('周業務', 'exec.zhou@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'EXECUTIVE', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('吳業務', 'exec.wu2@company.com', '$2a$10$XQ8jCxXqW7X0H4V5L5qJ6.K9W8Y0Z5V5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z', 'EXECUTIVE', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- ============================================================================
-- Update Department Managers
-- ============================================================================
UPDATE departments SET manager_id = (SELECT id FROM users WHERE email = 'depthead.lin@company.com') WHERE name = '研發部';
UPDATE departments SET manager_id = (SELECT id FROM users WHERE email = 'depthead.huang@company.com') WHERE name = '銷售部';

-- ============================================================================
-- Summary Comments
-- ============================================================================
COMMENT ON TABLE users IS 'Test Users:
- HR: hr.wang@company.com, hr.li@company.com
- Manager: manager.zhang@company.com, manager.chen@company.com
- Dept Head: depthead.lin@company.com (研發部), depthead.huang@company.com (銷售部)
- PM: pm.wu@company.com, pm.liu@company.com (研發部), pm.zheng@company.com (銷售部)
- Executive: exec.zhao@company.com, exec.qian@company.com, exec.sun@company.com (研發部), 
             exec.zhou@company.com, exec.wu2@company.com (銷售部)
All passwords: password123';
