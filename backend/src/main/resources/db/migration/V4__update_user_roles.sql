-- V4__update_user_roles.sql
-- Migration to update user roles to match specification
-- Old: MANAGER, PM, DEPT_HEAD, EXECUTIVE, HR
-- New: EXECUTIVE, PM, MANAGER, EMPLOYEE, HR
-- 
-- Semantic mapping:
-- MANAGER (管理層) → EXECUTIVE (highest authority)
-- DEPT_HEAD (部門主管) → MANAGER (department oversight)
-- EXECUTIVE (執行人員) → EMPLOYEE (workers who log time)
-- PM, HR remain unchanged

-- Step 1: Add a temporary column to store new role values
ALTER TABLE users ADD COLUMN new_role VARCHAR(20);

-- Step 2: Map old roles to new roles
UPDATE users SET new_role = CASE
    WHEN role = 'MANAGER' THEN 'EXECUTIVE'
    WHEN role = 'DEPT_HEAD' THEN 'MANAGER'
    WHEN role = 'EXECUTIVE' THEN 'EMPLOYEE'
    WHEN role = 'PM' THEN 'PM'
    WHEN role = 'HR' THEN 'HR'
    ELSE role -- fallback (should not happen)
END;

-- Step 3: Drop the old CHECK constraint
ALTER TABLE users DROP CONSTRAINT chk_user_role;

-- Step 4: Update the role column from new_role
UPDATE users SET role = new_role;

-- Step 5: Drop the temporary column
ALTER TABLE users DROP COLUMN new_role;

-- Step 6: Add the new CHECK constraint with updated values
ALTER TABLE users ADD CONSTRAINT chk_user_role 
    CHECK (role IN ('EXECUTIVE', 'PM', 'MANAGER', 'EMPLOYEE', 'HR'));

-- Update comments to reflect new role names
COMMENT ON TABLE users IS '用戶資料表 - 支援五種角色 (EXECUTIVE, PM, MANAGER, EMPLOYEE, HR)';
COMMENT ON COLUMN users.role IS '用戶角色: EXECUTIVE(管理層), PM(專案經理), MANAGER(部門主管), EMPLOYEE(執行人員), HR(人資)';
