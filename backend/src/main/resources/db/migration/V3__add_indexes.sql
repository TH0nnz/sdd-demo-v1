-- Migration V3: Add database indexes for performance optimization
-- Generated: 2026年2月6日
-- Based on: data-model.md indexing strategy

-- Indexes for User table
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_department ON users(department_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_created_at ON users(created_at DESC);

-- Indexes for Department table
CREATE INDEX idx_department_code ON departments(code UNIQUE);
CREATE INDEX idx_department_head ON departments(head_id);

-- Indexes for Project table
CREATE INDEX idx_project_name ON projects(name);
CREATE INDEX idx_project_manager ON projects(manager_id);
CREATE INDEX idx_project_department ON projects(department_id);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_created_at ON projects(created_at DESC);
CREATE INDEX idx_project_date_range ON projects(start_date, end_date);

-- Indexes for Task table
CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assignee ON tasks(assignee_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_created_at ON tasks(created_at DESC);
CREATE INDEX idx_task_date_range ON tasks(start_date, end_date);

-- Indexes for Timesheet table (CRITICAL for performance)
CREATE INDEX idx_timesheet_employee ON timesheets(employee_id);
CREATE INDEX idx_timesheet_date ON timesheets(work_date DESC);
CREATE INDEX idx_timesheet_status ON timesheets(status);
CREATE INDEX idx_timesheet_project ON timesheets(project_id);
CREATE INDEX idx_timesheet_task ON timesheets(task_id);
CREATE INDEX idx_timesheet_created_at ON timesheets(created_at DESC);
CREATE INDEX idx_timesheet_date_range ON timesheets(work_date DESC, employee_id);
CREATE INDEX idx_timesheet_date_status ON timesheets(work_date DESC, status);

-- Indexes for TimeRequest table
CREATE INDEX idx_time_request_user ON time_requests(user_id);
CREATE INDEX idx_time_request_approver ON time_requests(approver_id);
CREATE INDEX idx_time_request_status ON time_requests(status);
CREATE INDEX idx_time_request_created_at ON time_requests(created_at DESC);

-- Indexes for WorkHours table
CREATE INDEX idx_work_hours_employee ON work_hours(employee_id);
CREATE INDEX idx_work_hours_month ON work_hours(year, month);

-- Composite indexes for common queries
CREATE INDEX idx_timesheet_employee_month ON timesheets(employee_id, DATE_TRUNC('month', work_date));
CREATE INDEX idx_project_status_date ON projects(status, start_date DESC);

-- Indexes for audit trail
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(created_at DESC);
