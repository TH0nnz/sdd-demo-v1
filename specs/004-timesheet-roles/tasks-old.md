# 004-Timesheet-Roles: 任務分解 (Phase 2)

## 概述

此文件提供「報工系統角色權限管理」功能的詳細任務分解，涵蓋後端 Spring Boot 與前端 Vue 3 的開發工作。任務將依照 5 個開發階段進行，以確保有序且高效的開發流程，同時滿足所有功能與非功能需求。

## 開發流程階段概覽

本專案將遵循以下五個主要開發階段，每個階段都包含後端與前端的相關任務：

1.  **資料層與基礎配置 (Backend Data Layer & Infrastructure)**
2.  **後端業務邏輯與服務 (Backend Business Logic & Services)**
3.  **後端 API 層與安全 (Backend API Layer & Security)**
4.  **前端介面與整合 (Frontend UI & Integration)**
5.  **整合測試、部署與文件 (Integration Testing, Deployment & Documentation)**

---

## 詳細任務分解

### Phase 2.1: 後端資料層與基礎配置

**目標**: 建立穩固的資料庫結構與 JPA 實體，確保後端能與 PostgreSQL 進行正確的資料互動。

#### 後端任務 (`backend/`)

*   **Task 2.1.1: 更新 `pom.xml` 依賴**
    *   確認 Spring Boot 4.0.2, Spring Security 7.0.2, Spring Data JPA 4.0, PostgreSQL 18.1 Driver 等依賴版本正確。
    *   新增 Flyway DB Migration 依賴。
    *   新增 Lombok 依賴。
*   **Task 2.1.2: 定義 Flyway 資料庫遷移腳本**
    *   建立 `db/migration/V001__Initial_Schema.sql`。
    *   根據 `data-model.md` 建立 `users`, `departments`, `projects`, `tasks`, `timesheet_entries`, `hours_requests`, `notifications`, `audit_logs` 表格。
    *   為 `users` 表格添加 `role` 欄位與索引。
    *   設置所有外鍵約束與唯一約束。
*   **Task 2.1.3: 實作 JPA 實體**
    *   根據 `data-model.md` 為 `User`, `Department`, `Project`, `Task`, `TimesheetEntry`, `HoursRequest`, `Notification`, `AuditLog` 建立 JPA `@Entity` 類別。
    *   確保所有實體具有正確的欄位、關係（`@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`）與級聯操作。
    *   實作 `BaseEntity` 包含 `id`, `createdAt`, `updatedAt` 並應用 `@MappedSuperclass` 和 `@EntityListeners(AuditingEntityListener.class)`。
    *   為 `User` 實體定義 `Role` enum。
    *   實作所有驗證註解 (e.g., `@NotNull`, `@Size`, `@Email`)。
*   **Task 2.1.4: 實作 Repository 層**
    *   為每個 JPA 實體建立 Spring Data JPA `JpaRepository` 介面。
    *   添加必要的基本查詢方法 (e.g., `findByUsername`, `findByDepartmentId`, `findByProjectId`)。
    *   實作自訂查詢方法 (若有需要)。
*   **Task 2.1.5: 配置 PostgreSQL 資料庫連線**
    *   在 `application.yml` 或 `application-test.yml` 中配置 PostgreSQL 資料庫連線資訊。
    *   設定 Flyway 自動遷移。
    *   配置 JPA 相關屬性 (e.g., `hibernate.ddl-auto=none`, `hibernate.format_sql=true`)。
*   **Task 2.1.6: 建立初始資料 (Seeds)**
    *   建立測試與開發環境所需的初始使用者（包含不同角色）、部門、專案等資料。
    *   可使用 `data.sql` 或 `CommandLineRunner` 實現。

### Phase 2.2: 後端業務邏輯與服務

**目標**: 實作核心業務邏輯，包含角色權限控制、工時計算、通知處理與稽核日誌。

#### 後端任務 (`backend/`)

*   **Task 2.2.1: 實作業務服務**
    *   為每個領域建立服務類別 (e.g., `UserService`, `ProjectService`, `TimesheetService`, `DepartmentService`, `ReportService`, `TaskService`, `HoursRequestService`, `NotificationService`, `AuditLogService`)。
    *   實作 `spec.md` 中定義的所有功能需求 (FR-001 to FR-034)。
    *   處理業務邏輯、驗證與異常。
*   **Task 2.2.2: 實作角色權限控制邏輯**
    *   在服務層使用 `@PreAuthorize` 註解來保護方法，確保只有具有特定角色的使用者才能執行操作。
    *   範例: `@PreAuthorize("hasRole('PM') or hasRole('MANAGER') and @securityService.isManagerOfDepartment(#departmentId)")`
    *   實作 `SecurityService` 輔助類別，用於更複雜的自訂權限判斷 (e.g., 檢查是否為部門經理、專案經理)。
*   **Task 2.2.3: 實作工時計算與工作天邏輯**
    *   在 `TimesheetService` 中實作工時計算邏輯，排除週末。
    *   確保符合 NFR-005 (工時計算準確性)。
*   **Task 2.2.4: 實作通知系統**
    *   實作 `NotificationService` 處理通知的建立、發送與讀取。
    *   使用資料庫輪詢機制，每 30 秒檢查新通知 (NFR-007)。
*   **Task 2.2.5: 實作稽核日誌**
    *   利用 Spring AOP 實作稽核日誌切面，攔截關鍵操作。
    *   記錄 `AuditLog` 實體，包含操作者、時間、操作類型、實體 ID、變更前後的 JSONB 資料。
    *   確保符合 NFR-006 (稽核日誌完整性)。

### Phase 2.3: 後端 API 層與安全

**目標**: 建立符合 OpenAPI 規範的 RESTful API 端點，並配置 JWT 認證與授權機制。

#### 後端任務 (`backend/`)

*   **Task 2.3.1: 實作 RESTful Controller**
    *   根據 `api-spec.yaml` 為每個領域建立 Controller 類別 (e.g., `UserController`, `ProjectController`, `TimesheetController`, `AuthController`)。
    *   實作所有定義的 API 端點。
    *   使用 `@Validated` 進行輸入資料驗證。
    *   確保 HTTP 方法與狀態碼符合 RESTful 原則。
*   **Task 2.3.2: 實作 Request/Response DTOs**
    *   根據 `api-spec.yaml` 定義所有 Request 和 Response DTO 類別。
    *   使用 Bean Validation 註解確保資料完整性。
*   **Task 2.3.3: 配置 Spring Security**
    *   實作 JWT 認證過濾器。
    *   配置 `SecurityFilterChain`，禁用 CSRF，設定無狀態 Session。
    *   定義公共路徑 (e.g., `/api/auth/**`) 與受保護路徑。
    *   配置 `AuthenticationManager` 與 `PasswordEncoder`。
    *   實作 `UserDetailsServiceImpl`。
*   **Task 2.3.4: 實作 JWT 產生與驗證**
    *   建立 JWT 工具類別，用於產生和解析 JWT Token。
    *   將 JWT Token 添加到 HTTP 請求頭。
*   **Task 2.3.5: 配置 CORS**
    *   在 Spring Boot 應用中配置 CORS 策略，允許前端 Vue 應用程式的跨域請求。
    *   根據 `quickstart.md` 的建議進行配置。

### Phase 2.4: 前端介面與整合

**目標**: 實作 Vue 3 前端介面，並與後端 API 進行整合，實現角色導向的 UI 呈現。

#### 前端任務 (`frontend/`)

*   **Task 2.4.1: 初始化 Vue 3 專案與依賴**
    *   確保 Vue 3, TypeScript, Pinia, Vue Router, Axios 等依賴正確安裝。
*   **Task 2.4.2: 配置 Vue Router**
    *   根據不同角色定義動態路由。
    *   實作路由守衛，確保只有具備足夠權限的使用者才能訪問特定頁面。
*   **Task 2.4.3: 實作 Pinia 狀態管理**
    *   建立 `auth.ts` Store 處理使用者登入、登出、Token 管理與使用者角色資訊。
    *   建立其他 Store (e.g., `department.ts`, `project.ts`, `timesheet.ts`) 管理應用程式狀態。
*   **Task 2.4.4: 實作 API 服務整合**
    *   在 `api/client.ts` 中配置 Axios 實例與請求/回應攔截器，處理 JWT Token 的添加與錯誤處理。
    *   為每個後端服務建立對應的 API 服務模組 (e.g., `api/users.ts`, `api/timesheets.ts`)。
*   **Task 2.4.5: 建立共用 UI 元件**
    *   建立通用表格、表單、按鈕、通知訊息等元件。
    *   實作基於使用者角色的條件渲染邏輯，隱藏或顯示特定 UI 元素或功能。
*   **Task 2.4.6: 實作角色導向的頁面與視圖**
    *   根據 `api-spec.yaml` 與 `spec.md`，為 Executive, PM, Manager, Employee, HR 角色設計並實作專屬介面或功能頁面。
    *   例如:
        *   **Executive 儀表板**: 顯示整體專案進度、資源利用率報告。
        *   **PM 專案管理**: 建立、分配、查看專案與任務。
        *   **Manager 部門管理**: 批准部門成員的工時與請假。
        *   **Employee 工時填報**: 填報個人工時，提交請假申請。
        *   **HR 人事管理**: 管理使用者帳戶、部門資訊。
*   **Task 2.4.7: 整合通知系統**
    *   前端定時發送請求輪詢後端通知 API。
    *   在 UI 上顯示新通知。

### Phase 2.5: 整合測試、部署與文件

**目標**: 確保整個應用程式的穩定性、功能性，並提供完整的開發與部署指南。

#### 後端任務 (`backend/`)

*   **Task 2.5.1: 實作單元測試**
    *   為所有服務層、Repository 層與工具類別編寫 JUnit 5 單元測試。
    *   使用 Mockito 模擬依賴。
    *   確保核心業務邏輯 (如工時計算) 達到 100% 覆蓋率 (NFR-005)。
*   **Task 2.5.2: 實作整合測試**
    *   使用 Spring Boot Test 和 Testcontainers (PostgreSQL) 編寫整合測試。
    *   測試資料庫互動、服務層與 API 層的整合。
    *   使用 MockMvc 測試 Controller 端點。
    *   確保整體測試覆蓋率達到 80% 以上 (NFR-004)。
*   **Task 2.5.3: 更新 Dockerfile**
    *   確認 `backend/Dockerfile` 符合 JDK 24 最佳實踐，並優化建置流程。

#### 前端任務 (`frontend/`)

*   **Task 2.5.4: 實作前端單元測試**
    *   為所有 Vue 元件、Pinia Store 與工具類別編寫單元測試 (例如使用 Vitest)。
*   **Task 2.5.5: 實作 E2E 測試**
    *   使用 Cypress 或 Playwright 編寫 E2E 測試，覆蓋關鍵使用者流程 (例如登入、工時填報、報告查看)。
*   **Task 2.5.6: 更新 Dockerfile 與 Nginx 配置**
    *   確認 `frontend/Dockerfile` 符合 Vue 3 應用部署最佳實踐。
    *   更新 `nginx.conf` 以正確代理後端 API 請求與處理前端靜態文件。

#### 跨領域任務

*   **Task 2.5.7: 更新 `docker-compose.yml`**
    *   整合前端與後端服務，包含 PostgreSQL 資料庫。
    *   配置服務依賴與網路。
*   **Task 2.5.8: 更新 `README.md` 文件**
    *   提供完整的專案設定、啟動、測試與部署指南。
    *   包含所有最新的技術堆疊資訊。
*   **Task 2.5.9: 程式碼審查**
    *   進行前後端程式碼審查，確保程式碼品質、風格一致性與最佳實踐。
*   **Task 2.5.10: 效能測試與優化**
    *   執行壓力測試與負載測試，驗證 API < 200ms (p95), 工時填報 < 500ms, 儀表板 < 2s, 支援 200 並行用戶 (NFR-001 to NFR-003)。
    *   根據測試結果進行效能優化。
*   **Task 2.5.11: 安全性審查**
    *   進行 JWT 實現、RBAC 邏輯、資料庫存取控制等安全性審查。

## 預計時間線 (估計)

- **Phase 2.1**: 2-3 天
- **Phase 2.2**: 3-4 天
- **Phase 2.3**: 2-3 天
- **Phase 2.4**: 4-5 天
- **Phase 2.5**: 3-4 天

**總計**: 約 14-19 個工作天

## 完成標誌

- `tasks.md` 文件已生成並包含上述所有任務。
- 所有任務已明確定義，可供開發團隊進行實作。
