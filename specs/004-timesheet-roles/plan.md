````markdown
# Implementation Plan: 報工系統角色權限管理

**Branch**: `004-timesheet-roles` | **Date**: 2026年2月6日 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-timesheet-roles/spec.md`

## Summary

本功能實現報工系統的角色權限管理，支援五種角色（管理層、PM、部門主管、執行人員、HR），每個角色擁有特定的權限和功能。核心功能包括：執行人員工時填報、PM 專案與任務管理、管理層專案創建與時數審批、部門主管工時查詢、HR 使用者管理。系統採用前後端分離架構，使用 Spring Boot 建構後端 API，Vue 建構前端介面，PostgreSQL 作為資料庫，實現細粒度的角色基礎存取控制（RBAC）。

## Technical Context

**Language/Version**: JDK 24  
**Primary Dependencies**: Spring Boot 4.0.2, Spring Security 7.0.2, Spring Data JPA 4.0, PostgreSQL 18.1 Driver, Bean Validation  
**Storage**: PostgreSQL 18.1
**Testing**: JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL), MockMvc, AssertJ  
**Target Platform**: Linux server (Docker containers), 瀏覽器端 (Chrome, Firefox, Safari)  
**Project Type**: Web application (前後端分離架構 - Spring Boot REST API + Vue 3 SPA)  
**Performance Goals**: 
  - API 回應時間: < 200ms (p95) 簡單查詢, < 1000ms (p95) 複雜操作
  - 工時填報提交: < 500ms (p95)
  - 專案儀表板載入: < 2秒 (p95)
  - 部門報表查詢: < 3秒 (p95, 100人以內)
  - 並行用戶支援: ≥ 200 users

**Constraints**: 
  - 執行人員僅能編輯過去三個工作天的工時記錄
  - 嚴格的角色基礎存取控制（RBAC）- 每個角色有明確的資料存取範圍
  - 時數不足時強制拒絕工時填報（無法透支）
  - 有工時記錄的任務無法刪除（僅能關閉）
  - 程式碼覆蓋率 ≥ 80%
  - 時數計算邏輯 100% 測試覆蓋率

**Scale/Scope**: 
  - 中小型企業內部使用（50-200 使用者）
  - 5 種角色類型
  - 8 個核心實體（User, Department, Project, Task, Timesheet Entry, Hours Request, Notification, Audit Log）
  - 預期同時支援 200 個並行使用者

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Code Quality Standards ✅
- [x] Code follows established language-specific style guides (Java Code Conventions, Spring Boot Best Practices)
- [x] Linting and formatting tools configured (Checkstyle configured in backend/checkstyle.xml)
- [x] Code review process established (Pull request workflow)
- [x] Complexity metrics defined (NFR-016: 循環複雜度 ≤ 10)

### Testing Standards (NON-NEGOTIABLE) ✅
- [x] Test strategy defined (Unit tests for business logic, Integration tests for API endpoints, Contract tests for role permissions)
- [x] Minimum 80% code coverage target set (NFR-014, 時數計算邏輯 100% 覆蓋率)
- [x] Test-first approach planned for acceptance criteria (每個 User Story 的 Acceptance Scenarios 將先轉換為測試)
- [x] CI/CD pipeline includes automated test execution (Maven test phase in build)

### User Experience Consistency ✅
- [x] UI patterns and terminology documented (Vue components structure, 角色專屬介面)
- [x] Error handling strategy defined (NFR-008: 通俗語言錯誤提示, ErrorResponse schema in API spec)
- [x] Accessibility requirements (WCAG 2.1 AA) planned (NFR-007: 即時驗證和錯誤提示)
- [x] User feedback mechanisms identified (NFR-033: 即時通知功能, 資料庫輪詢機制)

### Performance Requirements ✅
- [x] Performance targets defined (NFR-001 to NFR-004: 回應時間和並行用戶數)
- [x] Performance testing approach outlined (JMeter for load testing, Spring Actuator for monitoring)
- [x] Monitoring and instrumentation planned (Spring Boot Actuator, Prometheus metrics, SQL query logging)
- [x] Performance budgets established (API 回應時間預算已定義, 詳見 NFR section)

**Constitution Gate Status (Phase 1 Re-evaluation)**: ✅ PASS

所有 Constitution 要求已滿足並在 Phase 1 設計中詳細規劃：
- ✅ data-model.md 定義了完整的資料模型、驗證規則和索引策略
- ✅ contracts/api-spec.yaml 提供了完整的 OpenAPI 3.0 規格，包含所有角色端點和錯誤處理
- ✅ research.md 詳細記錄了技術決策和最佳實踐
- ✅ quickstart.md 提供了完整的開發人員指南，包含測試和除錯流程
- ✅ .github/agents/copilot-instructions.md 更新了 AI agent 上下文，包含程式碼風格和測試標準

準備進入實施階段（Phase 2）。

## Project Structure

### Documentation (this feature)

```text
specs/004-timesheet-roles/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (待生成)
├── data-model.md        # Phase 1 output (待生成)
├── quickstart.md        # Phase 1 output (待生成)
├── contracts/           # Phase 1 output (待生成)
│   └── api-spec.yaml    # OpenAPI 3.0 規格
├── spec.md              # Feature specification (已存在)
├── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
└── checklists/          # Requirements checklist (已存在)
    └── requirements.md
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── timesheet/
│   │   │               ├── config/           # Spring Security 配置, CORS 配置
│   │   │               ├── security/         # 角色權限定義, JWT 處理, UserDetailsService
│   │   │               ├── model/            # JPA Entities (User, Department, Project, Task, TimesheetEntry, HoursRequest, Notification, AuditLog)
│   │   │               ├── repository/       # Spring Data JPA Repositories
│   │   │               ├── service/          # Business logic (TimeCalculationService, PermissionService, NotificationService)
│   │   │               ├── controller/       # REST API Controllers (角色專屬端點)
│   │   │               ├── dto/              # Request/Response DTOs
│   │   │               ├── exception/        # Custom exceptions and global exception handler
│   │   │               └── util/             # Utilities (DateUtils for workday calculation)
│   │   └── resources/
│   │       ├── application.yml               # Spring Boot 配置
│   │       ├── ValidationMessages.properties # 驗證訊息（繁體中文）
│   │       └── db/
│   │           └── migration/                # Flyway 或 Liquibase migration scripts
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── timesheet/
│       │               ├── controller/       # API Integration Tests (MockMvc)
│       │               ├── service/          # Service Unit Tests
│       │               ├── repository/       # Repository Tests (Testcontainers)
│       │               └── security/         # Permission Tests (各角色權限驗證)
│       └── resources/
│           └── application-test.yml          # Test configuration
├── pom.xml                                    # Maven dependencies
└── Dockerfile

frontend/
├── src/
│   ├── api/                  # API client modules (已存在: auth, departments, projects, reports, tasks, time-requests, timesheets, users)
│   ├── components/           # Vue components
│   │   ├── common/           # 共用組件 (已存在)
│   │   ├── forms/            # 表單組件 (已存在)
│   │   ├── layouts/          # 佈局組件 (已存在)
│   │   ├── projects/         # 專案相關組件 (已存在)
│   │   ├── reports/          # 報表組件 (已存在)
│   │   ├── tasks/            # 任務組件 (已存在)
│   │   ├── time-requests/    # 時數申請組件 (已存在)
│   │   ├── timesheets/       # 工時表組件 (已存在)
│   │   └── users/            # 使用者管理組件 (已存在)
│   ├── views/                # Page-level Vue components (已依角色組織)
│   │   ├── auth/             # 登入/註冊頁面 (已存在)
│   │   ├── layouts/          # 佈局頁面 (已存在)
│   │   ├── profile/          # 個人資料頁面 (已存在)
│   │   ├── departments/      # 部門管理頁面 (已存在)
│   │   ├── executive/        # 執行人員專屬頁面 (需擴充角色功能)
│   │   ├── hr/               # HR 專屬頁面 (需擴充角色功能)
│   │   ├── manager/          # 部門主管專屬頁面 (需擴充角色功能)
│   │   ├── pm/               # PM 專屬頁面 (需擴充角色功能)
│   │   ├── projects/         # 專案頁面 (已存在)
│   │   ├── reports/          # 報表頁面 (已存在)
│   │   ├── tasks/            # 任務頁面 (已存在)
│   │   ├── time-requests/    # 時數申請頁面 (已存在)
│   │   ├── timesheets/       # 工時頁面 (已存在)
│   │   └── users/            # 使用者管理頁面 (已存在)
│   ├── router/               # Vue Router 配置 (已存在，需擴充角色路由守衛)
│   ├── stores/               # Pinia stores (已存在: auth, department, project, report, task, timesheet, user)
│   ├── types/                # TypeScript 類型定義 (已存在)
│   └── utils/                # Utility functions
├── tests/
│   ├── unit/                 # Vue component unit tests (已存在部分)
│   ├── integration/          # Integration tests
│   └── e2e/                  # E2E tests (已存在 critical-flows.spec.ts)
├── package.json
├── vite.config.ts
└── Dockerfile

database/
├── migrations/               # Database schema migrations (需新增角色權限相關表結構)
└── seeds/                    # Seed data (需新增測試用使用者和角色資料)
```

**Structure Decision**: 專案採用前後端分離架構，後端使用 Spring Boot 建構 RESTful API，前端使用 Vue 3 建構單頁應用程式（SPA）。現有的基礎架構已經就緒（資料夾結構、基本組件），本次功能將擴充角色權限管理功能，包括：1) 後端新增細粒度的權限控制和角色專屬業務邏輯，2) 前端新增角色專屬的介面和導航邏輯，3) 資料庫新增角色和權限相關表結構。

## Complexity Tracking

> **無違規項目** - 本功能符合所有 Constitution 要求，無需特殊豁免。

````
