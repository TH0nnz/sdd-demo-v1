import { test, expect } from '@playwright/test'

const BASE_URL = process.env.VITE_API_URL || 'http://localhost:5173'

test.describe('工時管理系統 - 關鍵使用者流程', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page
    await page.goto(`${BASE_URL}/login`)
  })

  test('執行人員登入與填報工時流程', async ({ page }) => {
    // Step 1: Login
    await page.fill('input[type="email"]', 'employee@example.com')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button[type="submit"]')

    // Verify redirect to dashboard
    await expect(page).toHaveURL(`${BASE_URL}/`)
    await expect(page.locator('h1')).toContainText('儀表板')

    // Step 2: Navigate to timesheet form
    await page.click('button:has-text("填報工時")')
    await expect(page).toHaveURL(`${BASE_URL}/timesheets/new`)

    // Step 3: Fill timesheet form
    await page.fill('input[name="workDate"]', '2026-02-06')
    await page.fill('input[name="workHours"]', '8.0')
    await page.fill('select[name="projectId"]', 'project-001')
    await page.fill('select[name="taskId"]', 'task-001')
    await page.fill('textarea[name="remarks"]', '完成任務 A 和 B')

    // Step 4: Submit form
    await page.click('button:has-text("提交")')

    // Verify success message
    await expect(page.locator('.success-message')).toBeVisible()
    await expect(page).toHaveURL(`${BASE_URL}/timesheets`)
  })

  test('專案經理建立與管理專案', async ({ page }) => {
    // Step 1: Login as PM
    await page.fill('input[type="email"]', 'pm@example.com')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button[type="submit"]')

    // Step 2: Navigate to projects
    await page.click('a:has-text("專案管理")')
    await expect(page).toHaveURL(`${BASE_URL}/projects`)

    // Step 3: Create new project
    await page.click('button:has-text("新增專案")')
    await expect(page).toHaveURL(`${BASE_URL}/projects/new`)

    // Fill project form
    await page.fill('input[name="name"]', 'Test Project')
    await page.fill('input[name="code"]', 'PROJ-001')
    await page.fill('input[name="startDate"]', '2026-02-01')
    await page.fill('input[name="endDate"]', '2026-03-31')
    await page.fill('textarea[name="description"]', 'This is a test project')

    // Submit form
    await page.click('button:has-text("建立")')

    // Verify success
    await expect(page.locator('.success-message')).toBeVisible()
  })

  test('管理層查看工時報表', async ({ page }) => {
    // Step 1: Login as Manager
    await page.fill('input[type="email"]', 'manager@example.com')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button[type="submit"]')

    // Step 2: Navigate to reports
    await page.click('a:has-text("報表查詢")')
    await expect(page).toHaveURL(`${BASE_URL}/reports`)

    // Step 3: Open timesheet report
    await page.click('button:has-text("工時報表")')
    await expect(page).toHaveURL(`${BASE_URL}/reports/timesheets`)

    // Step 4: Filter and view data
    await page.fill('input[name="startDate"]', '2026-02-01')
    await page.fill('input[name="endDate"]', '2026-02-28')
    await page.click('button:has-text("查詢")')

    // Verify report loaded
    await expect(page.locator('table')).toBeVisible()
    await expect(page.locator('tbody tr')).toHaveCount(5)
  })

  test('更改密碼流程', async ({ page }) => {
    // Step 1: Login
    await page.fill('input[type="email"]', 'employee@example.com')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button[type="submit"]')

    // Step 2: Navigate to profile
    await page.click('a:has-text("個人資料")')
    await expect(page).toHaveURL(`${BASE_URL}/profile`)

    // Step 3: Click change password
    await page.click('button:has-text("變更")')
    await expect(page).toHaveURL(`${BASE_URL}/change-password`)

    // Fill change password form
    await page.fill('input[name="oldPassword"]', 'password123')
    await page.fill('input[name="newPassword"]', 'newPassword456!')
    await page.fill('input[name="confirmPassword"]', 'newPassword456!')

    // Submit form
    await page.click('button:has-text("變更密碼")')

    // Verify success
    await expect(page.locator('.success-message')).toBeVisible()
  })

  test('未授權訪問應被拒絕', async ({ page }) => {
    // Try to access protected route without login
    await page.goto(`${BASE_URL}/timesheets`)

    // Should redirect to login
    await expect(page).toHaveURL(`${BASE_URL}/login`)
  })

  test('無效角色訪問應被拒絕', async ({ page }) => {
    // Step 1: Login as employee
    await page.fill('input[type="email"]', 'employee@example.com')
    await page.fill('input[type="password"]', 'password123')
    await page.click('button[type="submit"]')

    // Step 2: Try to access HR only page
    await page.goto(`${BASE_URL}/users`)

    // Should redirect to dashboard or show access denied
    await expect(page).not.toHaveURL(`${BASE_URL}/users`)
  })

  test('頁面不存在應顯示 404', async ({ page }) => {
    // Navigate to non-existent page
    await page.goto(`${BASE_URL}/non-existent-page`)

    // Should see 404 page
    await expect(page.locator('text=404')).toBeVisible()
    await expect(page.locator('text=頁面不存在')).toBeVisible()
  })
})
