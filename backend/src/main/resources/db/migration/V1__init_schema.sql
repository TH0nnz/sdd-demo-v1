-- V1__init_schema.sql
-- Initial database schema for Timesheet Management System
-- Timezone: Asia/Taipei (UTC+8)
-- Database: PostgreSQL 14+

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- Table: departments
-- Description: Organizational departments
-- ============================================================================
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    manager_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_department_name_length CHECK (LENGTH(name) >= 2)
);

CREATE INDEX idx_department_manager ON departments(manager_id);
COMMENT ON TABLE departments IS '部門資料表';
COMMENT ON COLUMN departments.manager_id IS '部門主管用戶 ID (外鍵到 users.id)';

-- ============================================================================
-- Table: users
-- Description: System users with role-based access control
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_user_name_length CHECK (LENGTH(name) >= 2),
    CONSTRAINT chk_user_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$'),
    CONSTRAINT chk_user_role CHECK (role IN ('EXECUTIVE', 'PM', 'MANAGER', 'EMPLOYEE', 'HR')),
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_department ON users(department_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_active ON users(active);
COMMENT ON TABLE users IS '用戶資料表 - 支援五種角色 (EXECUTIVE, PM, MANAGER, EMPLOYEE, HR)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt 加密密碼';
COMMENT ON COLUMN users.role IS '用戶角色: EXECUTIVE(管理層), PM(專案經理), MANAGER(部門主管), EMPLOYEE(執行人員), HR(人資)';

-- Add foreign key constraint for department manager
ALTER TABLE departments ADD CONSTRAINT fk_department_manager 
    FOREIGN KEY (manager_id) REFERENCES users(id);

-- ============================================================================
-- Table: projects
-- Description: Projects with hours allocation
-- ============================================================================
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    total_hours NUMERIC(10, 2) NOT NULL,
    allocated_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    used_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    pm_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_project_name_length CHECK (LENGTH(name) >= 3),
    CONSTRAINT chk_project_total_hours CHECK (total_hours > 0),
    CONSTRAINT chk_project_allocated_hours CHECK (allocated_hours >= 0),
    CONSTRAINT chk_project_used_hours CHECK (used_hours >= 0),
    CONSTRAINT chk_project_hours_limit CHECK (allocated_hours <= total_hours),
    CONSTRAINT chk_project_status CHECK (status IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT fk_project_pm FOREIGN KEY (pm_id) REFERENCES users(id),
    CONSTRAINT fk_project_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_project_pm ON projects(pm_id);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_created_by ON projects(created_by);
COMMENT ON TABLE projects IS '專案資料表 - 管理層創建、PM 管理';
COMMENT ON COLUMN projects.total_hours IS '專案總時數';
COMMENT ON COLUMN projects.allocated_hours IS '已分配給任務的時數';
COMMENT ON COLUMN projects.used_hours IS '已實際使用的時數 (工時記錄總和)';

-- ============================================================================
-- Table: tasks
-- Description: Tasks within projects, assigned to executives
-- ============================================================================
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    estimated_hours NUMERIC(10, 2) NOT NULL,
    used_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    project_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_task_name_length CHECK (LENGTH(name) >= 3),
    CONSTRAINT chk_task_estimated_hours CHECK (estimated_hours > 0),
    CONSTRAINT chk_task_used_hours CHECK (used_hours >= 0),
    CONSTRAINT chk_task_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES users(id)
);

CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assignee ON tasks(assignee_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_project_status ON tasks(project_id, status);
COMMENT ON TABLE tasks IS '任務資料表 - PM 創建、執行人員完成';
COMMENT ON COLUMN tasks.estimated_hours IS '預估時數 (從專案分配)';
COMMENT ON COLUMN tasks.used_hours IS '已使用時數 (工時記錄總和)';

-- ============================================================================
-- Table: timesheet_entries
-- Description: Work hour entries by executives
-- ============================================================================
CREATE TABLE timesheet_entries (
    id BIGSERIAL PRIMARY KEY,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    hours NUMERIC(5, 2) NOT NULL,
    lunch_deducted BOOLEAN NOT NULL DEFAULT FALSE,
    lunch_hours NUMERIC(3, 2) NOT NULL DEFAULT 0,
    description TEXT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_timesheet_hours CHECK (hours >= 0.5 AND hours <= 24),
    CONSTRAINT chk_timesheet_lunch_hours CHECK (lunch_hours >= 0 AND lunch_hours <= 1),
    CONSTRAINT chk_timesheet_time_range CHECK (end_time > start_time),
    CONSTRAINT chk_timesheet_work_date CHECK (work_date <= CURRENT_DATE),
    CONSTRAINT fk_timesheet_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_timesheet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_timesheet_user ON timesheet_entries(user_id);
CREATE INDEX idx_timesheet_task ON timesheet_entries(task_id);
CREATE INDEX idx_timesheet_work_date ON timesheet_entries(work_date);
CREATE INDEX idx_timesheet_user_date ON timesheet_entries(user_id, work_date);
CREATE INDEX idx_timesheet_created_at ON timesheet_entries(created_at);
COMMENT ON TABLE timesheet_entries IS '工時記錄資料表 - 執行人員填報';
COMMENT ON COLUMN timesheet_entries.hours IS '實際工作時數 (已扣除午休)';
COMMENT ON COLUMN timesheet_entries.lunch_deducted IS '是否跨午休時段 (12:00-13:00)';
COMMENT ON COLUMN timesheet_entries.lunch_hours IS '扣除的午休時數 (通常為 1 小時)';

-- ============================================================================
-- Table: time_requests
-- Description: Requests for additional project hours
-- ============================================================================
CREATE TABLE time_requests (
    id BIGSERIAL PRIMARY KEY,
    requested_hours NUMERIC(10, 2) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approval_comment TEXT,
    project_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    approver_id BIGINT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_time_request_hours CHECK (requested_hours > 0),
    CONSTRAINT chk_time_request_reason_length CHECK (LENGTH(reason) >= 10),
    CONSTRAINT chk_time_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_time_request_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_time_request_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_time_request_approver FOREIGN KEY (approver_id) REFERENCES users(id)
);

CREATE INDEX idx_time_request_project ON time_requests(project_id);
CREATE INDEX idx_time_request_requester ON time_requests(requester_id);
CREATE INDEX idx_time_request_status ON time_requests(status);
COMMENT ON TABLE time_requests IS '時數申請資料表 - PM 申請、管理層審批';
COMMENT ON COLUMN time_requests.requested_hours IS '申請增加的時數';

-- ============================================================================
-- Triggers: Auto-update timestamps
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_timesheet_entries_updated_at BEFORE UPDATE ON timesheet_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Views: Common queries optimization
-- ============================================================================
CREATE OR REPLACE VIEW v_project_summary AS
SELECT 
    p.id,
    p.name,
    p.status,
    p.total_hours,
    p.allocated_hours,
    p.used_hours,
    (p.total_hours - p.allocated_hours) AS available_hours,
    pm.name AS pm_name,
    COUNT(DISTINCT t.id) AS task_count,
    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) AS completed_task_count,
    p.created_at,
    p.updated_at
FROM projects p
LEFT JOIN users pm ON p.pm_id = pm.id
LEFT JOIN tasks t ON p.id = t.project_id
GROUP BY p.id, p.name, p.status, p.total_hours, p.allocated_hours, p.used_hours, pm.name, p.created_at, p.updated_at;

COMMENT ON VIEW v_project_summary IS '專案摘要視圖 - 包含任務統計';

-- ============================================================================
-- End of migration
-- ============================================================================
