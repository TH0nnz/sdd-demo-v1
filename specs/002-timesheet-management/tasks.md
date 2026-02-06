# Tasks: 工時管理系統

**Branch**: `002-timesheet-management`  
**Input**: Design documents from `/specs/002-timesheet-management/`  
**Prerequisites**: [plan.md](plan.md) (required), [spec.md](spec.md) (required), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/)

**Tests**: Per Constitution II (Testing Standards - NON-NEGOTIABLE), comprehensive automated testing is mandatory. All test tasks use Test-First approach - tests MUST be written and FAIL before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

---

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create project structure: `backend/`, `frontend/`, `database/` directories per plan.md
- [X] T002 Initialize backend (Java 21 + Spring Boot 3.2): `backend/pom.xml` with dependencies (Spring Web, Data JPA, Security, Validation, Lombok, MapStruct, SpringDoc)
- [X] T003 [P] Initialize frontend (Vue 3 + TypeScript): `frontend/package.json` with dependencies (Vue, Vue Router, Pinia, Element Plus, Axios, Vitest, Playwright)
- [X] T004 [P] Configure backend linting: `backend/checkstyle.xml`, `backend/pmd.xml`, `backend/spotbugs-exclude.xml`
- [X] T005 [P] Configure frontend linting: `frontend/.eslintrc.js`, `frontend/.prettierrc`, `frontend/tsconfig.json`
- [X] T006 Setup Docker Compose: `docker-compose.yml` with PostgreSQL 14 service
- [X] T007 [P] Create `.gitignore` files for backend and frontend
- [X] T008 [P] Setup CI/CD pipeline: `.github/workflows/ci.yml` for automated testing

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T009 Create database schema migrations: `backend/src/main/resources/db/migration/V1__init_schema.sql` with all tables (users, departments, projects, tasks, timesheet_entries, time_requests)
- [X] T010 [P] Create User entity: `backend/src/main/java/com/example/timesheet/domain/entity/User.java`
- [X] T011 [P] Create UserRole enum: `backend/src/main/java/com/example/timesheet/domain/enums/UserRole.java`
- [X] T012 [P] Create Department entity: `backend/src/main/java/com/example/timesheet/domain/entity/Department.java`
- [X] T013 [P] Create UserRepository: `backend/src/main/java/com/example/timesheet/domain/repository/UserRepository.java`
- [X] T014 [P] Create DepartmentRepository: `backend/src/main/java/com/example/timesheet/domain/repository/DepartmentRepository.java`
- [X] T015 Implement JWT authentication: `backend/src/main/java/com/example/timesheet/security/JwtTokenProvider.java`
- [X] T016 Implement JWT filter: `backend/src/main/java/com/example/timesheet/security/JwtAuthenticationFilter.java`
- [X] T017 Implement UserDetailsService: `backend/src/main/java/com/example/timesheet/security/CustomUserDetailsService.java`
- [X] T018 Configure Spring Security: `backend/src/main/java/com/example/timesheet/config/SecurityConfig.java`
- [X] T019 [P] Implement WorkHoursCalculator utility: `backend/src/main/java/com/example/timesheet/util/WorkHoursCalculator.java` (午休扣除邏輯)
- [X] T020 [P] Implement DateUtils utility: `backend/src/main/java/com/example/timesheet/util/DateUtils.java` (工作天計算)
- [X] T021 [P] Create GlobalExceptionHandler: `backend/src/main/java/com/example/timesheet/exception/GlobalExceptionHandler.java`
- [X] T022 [P] Create common DTOs: `backend/src/main/java/com/example/timesheet/dto/response/ErrorResponse.java`, `MessageResponse.java`, `PageMetadata.java`
- [X] T023 Insert seed data: `backend/src/main/resources/db/migration/V2__insert_sample_data.sql` (測試用戶帳號)
- [X] T024 [P] Setup frontend API client: `frontend/src/api/client.ts` with Axios interceptors
- [X] T025 [P] Create frontend auth store: `frontend/src/stores/auth.ts` with Pinia
- [X] T026 [P] Create frontend types: `frontend/src/types/common.ts`, `auth.ts`
- [X] T027 [P] Setup Vue Router: `frontend/src/router/index.ts` with authentication guards
- [X] T028 Configure OpenAPI documentation: `backend/src/main/java/com/example/timesheet/config/OpenApiConfig.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 5 - HR 管理人員與角色 (Priority: P5) 🏗️ Infrastructure

**Goal**: HR 可以新增員工、分配角色、啟用/停用帳號，建立完整的用戶管理基礎設施

**Why First**: 雖為 P5，但是其他所有功能的基礎設施（需要建立用戶才能測試其他故事）

**Independent Test**: 可以透過 HR 帳號登入，建立各種角色的用戶（管理層、PM、部門主管、執行人員），並驗證帳號可正常使用

### Tests for User Story 5 (Test-First - Write These FIRST) ⚠️

- [X] T029 [P] [US5] Contract test for POST /api/users: `backend/src/test/java/com/example/timesheet/contract/UserApiContractTest.java`
- [X] T030 [P] [US5] Contract test for PUT /api/users/{userId}: `backend/src/test/java/com/example/timesheet/contract/UserApiContractTest.java`
- [X] T031 [P] [US5] Integration test for create user journey: `backend/src/test/java/com/example/timesheet/integration/UserIntegrationTest.java`
- [X] T032 [P] [US5] Integration test for role change workflow: `backend/src/test/java/com/example/timesheet/integration/UserIntegrationTest.java`

### Implementation for User Story 5

- [X] T033 [P] [US5] Create UserService: `backend/src/main/java/com/example/timesheet/service/UserService.java`
- [X] T034 [P] [US5] Create DepartmentService: `backend/src/main/java/com/example/timesheet/service/DepartmentService.java`
- [X] T035 [P] [US5] Create User request DTOs: `backend/src/main/java/com/example/timesheet/dto/request/CreateUserRequest.java`, `UpdateUserRequest.java`
- [X] T036 [P] [US5] Create User response DTOs: `backend/src/main/java/com/example/timesheet/dto/response/UserResponse.java`, `UserSimpleResponse.java`, `UserPageResponse.java`
- [X] T037 [P] [US5] Create Department DTOs: `backend/src/main/java/com/example/timesheet/dto/response/DepartmentResponse.java`, `DepartmentSimpleResponse.java`, `DepartmentDetailResponse.java`
- [X] T038 [P] [US5] Create UserMapper: `backend/src/main/java/com/example/timesheet/mapper/UserMapper.java` (MapStruct)
- [X] T039 [P] [US5] Create DepartmentMapper: `backend/src/main/java/com/example/timesheet/mapper/DepartmentMapper.java` (MapStruct)
- [X] T040 [US5] Implement UserController (GET, POST, PUT /api/users): `backend/src/main/java/com/example/timesheet/controller/UserController.java`
- [X] T041 [US5] Implement user deactivate/activate endpoints: `UserController.java` POST /api/users/{userId}/deactivate, /activate
- [X] T042 [US5] Implement DepartmentController: `backend/src/main/java/com/example/timesheet/controller/DepartmentController.java`
- [X] T043 [P] [US5] Create frontend user API: `frontend/src/api/users.ts`
- [X] T044 [P] [US5] Create frontend department API: `frontend/src/api/departments.ts`
- [X] T045 [P] [US5] Create frontend user types: `frontend/src/types/user.ts`
- [X] T046 [P] [US5] Create frontend user store: `frontend/src/stores/user.ts`
- [X] T047 [US5] Implement UserListView: `frontend/src/views/users/UserListView.vue` with filtering (role, department, status)
- [X] T048 [P] [US5] Implement UserForm component: `frontend/src/components/users/UserForm.vue`
- [X] T049 [US5] Implement UserFormView: `frontend/src/views/users/UserFormView.vue` (create/edit user)
- [X] T050 [US5] Add user management routes: `frontend/src/router/index.ts` (require HR role)

**Checkpoint**: HR 功能完整，可以建立並管理所有角色的用戶

---

## Phase 4: User Story 3 - 管理層管理專案與分派時數 (Priority: P3)

**Goal**: 管理層可以建立專案、分派給 PM、調整時數、審批時數申請、關閉專案

**Why Next**: PM 需要先有專案才能建立任務，因此必須在 US2 之前

**Independent Test**: 可以透過管理層帳號登入，建立專案並分派給 PM，測試 PM 可以看到該專案

### Tests for User Story 3 (Test-First) ⚠️

- [X] T051 [P] [US3] Contract test for POST /api/projects: `backend/src/test/java/com/example/timesheet/contract/ProjectApiContractTest.java`
- [X] T052 [P] [US3] Contract test for PUT /api/projects/{projectId}: `backend/src/test/java/com/example/timesheet/contract/ProjectApiContractTest.java`
- [X] T053 [P] [US3] Contract test for POST /api/projects/{projectId}/close: `backend/src/test/java/com/example/timesheet/contract/ProjectApiContractTest.java`
- [X] T054 [P] [US3] Contract test for POST /api/time-requests/{requestId}/approve: `backend/src/test/java/com/example/timesheet/contract/TimeRequestApiContractTest.java`
- [X] T055 [P] [US3] Integration test for create project workflow: `backend/src/test/java/com/example/timesheet/integration/ProjectIntegrationTest.java`
- [X] T056 [P] [US3] Integration test for approve time request workflow: `backend/src/test/java/com/example/timesheet/integration/TimeRequestIntegrationTest.java`

### Implementation for User Story 3

- [X] T057 [P] [US3] Create Project entity: `backend/src/main/java/com/example/timesheet/domain/entity/Project.java`
- [X] T058 [P] [US3] Create ProjectStatus enum: `backend/src/main/java/com/example/timesheet/domain/enums/ProjectStatus.java`
- [X] T059 [P] [US3] Create TimeRequest entity: `backend/src/main/java/com/example/timesheet/domain/entity/TimeRequest.java`
- [X] T060 [P] [US3] Create TimeRequestStatus enum: `backend/src/main/java/com/example/timesheet/domain/enums/TimeRequestStatus.java`
- [X] T061 [P] [US3] Create ProjectRepository: `backend/src/main/java/com/example/timesheet/domain/repository/ProjectRepository.java`
- [X] T062 [P] [US3] Create TimeRequestRepository: `backend/src/main/java/com/example/timesheet/domain/repository/TimeRequestRepository.java`
- [X] T063 [US3] Create ProjectService: `backend/src/main/java/com/example/timesheet/service/ProjectService.java` (create, update, close, 時數計算)
- [X] T064 [US3] Create TimeRequestService: `backend/src/main/java/com/example/timesheet/service/TimeRequestService.java` (approve, reject, 自動增加專案時數)
- [X] T065 [P] [US3] Create Project request DTOs: `backend/src/main/java/com/example/timesheet/dto/request/CreateProjectRequest.java`, `UpdateProjectRequest.java`
- [X] T066 [P] [US3] Create Project response DTOs: `backend/src/main/java/com/example/timesheet/dto/response/ProjectResponse.java`, `ProjectSimpleResponse.java`, `ProjectDetailResponse.java`, `ProjectPageResponse.java`, `ProjectDashboardResponse.java`
- [X] T067 [P] [US3] Create TimeRequest DTOs: `backend/src/main/java/com/example/timesheet/dto/request/CreateTimeRequestRequest.java` and response DTOs
- [X] T068 [P] [US3] Create ProjectMapper: `backend/src/main/java/com/example/timesheet/mapper/ProjectMapper.java` (MapStruct)
- [X] T069 [P] [US3] Create TimeRequestMapper: `backend/src/main/java/com/example/timesheet/mapper/TimeRequestMapper.java` (MapStruct)
- [X] T070 [US3] Implement ProjectController (CRUD + close): `backend/src/main/java/com/example/timesheet/controller/ProjectController.java`
- [X] T071 [US3] Implement TimeRequestController (approve, reject): `backend/src/main/java/com/example/timesheet/controller/TimeRequestController.java`
- [X] T072 [P] [US3] Create frontend project API: `frontend/src/api/projects.ts`
- [X] T073 [P] [US3] Create frontend time-request API: `frontend/src/api/time-requests.ts`
- [X] T074 [P] [US3] Create frontend project types: `frontend/src/types/project.ts`
- [X] T075 [P] [US3] Create frontend project store: `frontend/src/stores/project.ts`
- [X] T076 [US3] Implement ProjectListView: `frontend/src/views/projects/ProjectListView.vue` with filtering (status, PM)
- [X] T077 [P] [US3] Implement ProjectForm component: `frontend/src/components/projects/ProjectForm.vue`
- [X] T078 [US3] Implement ProjectFormView: `frontend/src/views/projects/ProjectFormView.vue` (create/edit project)
- [X] T079 [US3] Implement TimeRequestListView: `frontend/src/views/time-requests/TimeRequestListView.vue` (管理層審批介面)
- [X] T080 [P] [US3] Implement TimeRequestApprovalModal: `frontend/src/components/time-requests/TimeRequestApprovalModal.vue`
- [X] T081 [US3] Add project and time-request routes: `frontend/src/router/index.ts` (require MANAGER role)

**Checkpoint**: 管理層功能完整，可以建立專案並審批時數申請

---

## Phase 5: User Story 2 - PM 管理任務與監控進度 (Priority: P2)

**Goal**: PM 可以將專案拆分成任務、指派給執行人員、即時監控專案進度、申請增加時數

**Why Next**: 執行人員需要先有任務才能填報工時，因此必須在 US1 之前

**Independent Test**: 可以透過 PM 帳號登入，在已有的專案下建立任務並分派給執行人員，測試執行人員可以看到該任務

### Tests for User Story 2 (Test-First) ⚠️

- [X] T082 [P] [US2] Contract test for POST /api/tasks: `backend/src/test/java/com/example/timesheet/contract/TaskApiContractTest.java`
- [X] T083 [P] [US2] Contract test for PUT /api/tasks/{taskId}: `backend/src/test/java/com/example/timesheet/contract/TaskApiContractTest.java`
- [X] T084 [P] [US2] Contract test for GET /api/projects/{projectId}/dashboard: `backend/src/test/java/com/example/timesheet/contract/ProjectApiContractTest.java`
- [X] T085 [P] [US2] Contract test for POST /api/time-requests: `backend/src/test/java/com/example/timesheet/contract/TimeRequestApiContractTest.java`
- [X] T086 [P] [US2] Integration test for create task workflow: `backend/src/test/java/com/example/timesheet/integration/TaskIntegrationTest.java`
- [X] T087 [P] [US2] Integration test for project dashboard: `backend/src/test/java/com/example/timesheet/integration/ProjectIntegrationTest.java`
- [X] T088 [P] [US2] Integration test for request additional hours: `backend/src/test/java/com/example/timesheet/integration/TimeRequestIntegrationTest.java`

### Implementation for User Story 2

- [X] T089 [P] [US2] Create Task entity: `backend/src/main/java/com/example/timesheet/domain/entity/Task.java`
- [X] T090 [P] [US2] Create TaskStatus enum: `backend/src/main/java/com/example/timesheet/domain/enums/TaskStatus.java`
- [X] T091 [P] [US2] Create TaskRepository: `backend/src/main/java/com/example/timesheet/domain/repository/TaskRepository.java` with custom queries (findByPmId, etc.)
- [X] T092 [US2] Create TaskService: `backend/src/main/java/com/example/timesheet/service/TaskService.java` (create, update, delete, complete, 時數檢查與警告)
- [X] T093 [US2] Enhance ProjectService: add `getProjectDashboard()` method for real-time statistics
- [X] T094 [US2] Enhance TimeRequestService: add PM's `createTimeRequest()` method
- [X] T095 [P] [US2] Create Task request DTOs: `backend/src/main/java/com/example/timesheet/dto/request/CreateTaskRequest.java`, `UpdateTaskRequest.java`
- [X] T096 [P] [US2] Create Task response DTOs: `backend/src/main/java/com/example/timesheet/dto/response/TaskResponse.java`, `TaskSimpleResponse.java`, `TaskDetailResponse.java`, `TaskPageResponse.java`
- [X] T097 [P] [US2] Create TaskMapper: `backend/src/main/java/com/example/timesheet/mapper/TaskMapper.java` (MapStruct)
- [X] T098 [US2] Implement TaskController (CRUD + complete): `backend/src/main/java/com/example/timesheet/controller/TaskController.java`
- [X] T099 [US2] Add project dashboard endpoint: `ProjectController.java` GET /api/projects/{projectId}/dashboard
- [X] T100 [US2] Add create time request endpoint: `TimeRequestController.java` POST /api/time-requests
- [X] T101 [P] [US2] Create frontend task API: `frontend/src/api/tasks.ts`
- [X] T102 [P] [US2] Create frontend task types: `frontend/src/types/task.ts`
- [X] T103 [P] [US2] Create frontend task store: `frontend/src/stores/task.ts`
- [X] T104 [US2] Implement TaskListView: `frontend/src/views/tasks/TaskListView.vue` with filtering (project, assignee, status)
- [X] T105 [P] [US2] Implement TaskForm component: `frontend/src/components/tasks/TaskForm.vue`
- [X] T106 [US2] Implement TaskFormView: `frontend/src/views/tasks/TaskFormView.vue` (create/edit task)
- [X] T107 [US2] Implement ProjectDashboardView: `frontend/src/views/projects/ProjectDashboardView.vue` (即時統計與任務狀態)
- [X] T108 [P] [US2] Implement TaskCard component: `frontend/src/components/tasks/TaskCard.vue` (顯示任務摘要與進度)
- [X] T109 [P] [US2] Implement TimeRequestForm component: `frontend/src/components/time-requests/TimeRequestForm.vue`
- [X] T110 [US2] Add task routes: `frontend/src/router/index.ts` (require PM or EXECUTIVE role)

**Checkpoint**: PM 功能完整，可以建立任務、監控進度、申請時數

---

## Phase 6: User Story 1 - 執行人員填報工時 (Priority: P1) 🎯 MVP

**Goal**: 執行人員可以查看被指派的任務、填報工時、編輯三工作天內的記錄、標記任務完成

**Why MVP**: 這是系統的核心價值功能，所有基礎設施都是為了支撐這個功能

**Independent Test**: 可以透過執行人員帳號登入，選擇已指派的任務，填報工時（包含跨午休時段測試），驗證工時正確計算並扣除任務剩餘時數

### Tests for User Story 1 (Test-First) ⚠️

- [X] T111 [P] [US1] Contract test for POST /api/timesheets: `backend/src/test/java/com/example/timesheet/contract/TimesheetApiContractTest.java`
- [X] T112 [P] [US1] Contract test for POST /api/timesheets/calculate-preview: `backend/src/test/java/com/example/timesheet/contract/TimesheetApiContractTest.java`
- [X] T113 [P] [US1] Contract test for PUT /api/timesheets/{timesheetId}: `backend/src/test/java/com/example/timesheet/contract/TimesheetApiContractTest.java`
- [X] T114 [P] [US1] Integration test for create timesheet (normal hours): `backend/src/test/java/com/example/timesheet/integration/TimesheetIntegrationTest.java`
- [X] T115 [P] [US1] Integration test for create timesheet (lunch deduction): `backend/src/test/java/com/example/timesheet/integration/TimesheetIntegrationTest.java`
- [X] T116 [P] [US1] Integration test for edit timesheet (within 3 working days): `backend/src/test/java/com/example/timesheet/integration/TimesheetIntegrationTest.java`
- [X] T117 [P] [US1] Integration test for reject edit (beyond 3 days): `backend/src/test/java/com/example/timesheet/integration/TimesheetIntegrationTest.java`
- [X] T118 [P] [US1] Unit test for WorkHoursCalculator (lunch deduction logic): `backend/src/test/java/com/example/timesheet/util/WorkHoursCalculatorTest.java`

### Implementation for User Story 1

- [X] T119 [P] [US1] Create TimesheetEntry entity: `backend/src/main/java/com/example/timesheet/domain/entity/TimesheetEntry.java`
- [X] T120 [P] [US1] Create TimesheetRepository: `backend/src/main/java/com/example/timesheet/domain/repository/TimesheetRepository.java` with custom queries
- [X] T121 [US1] Create TimesheetService: `backend/src/main/java/com/example/timesheet/service/TimesheetService.java` (create, update, delete, 工時計算、午休扣除、三工作天驗證、任務時數扣除、PM 通知)
- [X] T122 [P] [US1] Create Timesheet request DTOs: `backend/src/main/java/com/example/timesheet/dto/request/CreateTimesheetRequest.java`, `UpdateTimesheetRequest.java`
- [X] T123 [P] [US1] Create Timesheet response DTOs: `backend/src/main/java/com/example/timesheet/dto/response/TimesheetResponse.java`, `TimesheetPageResponse.java`, `WorkHoursCalculationResponse.java`
- [X] T124 [P] [US1] Create TimesheetMapper: `backend/src/main/java/com/example/timesheet/mapper/TimesheetMapper.java` (MapStruct)
- [X] T125 [US1] Implement TimesheetController (CRUD + calculate-preview): `backend/src/main/java/com/example/timesheet/controller/TimesheetController.java`
- [X] T126 [P] [US1] Create frontend timesheet API: `frontend/src/api/timesheets.ts`
- [X] T127 [P] [US1] Create frontend timesheet types: `frontend/src/types/timesheet.ts`
- [X] T128 [P] [US1] Create frontend timesheet store: `frontend/src/stores/timesheet.ts`
- [X] T129 [P] [US1] Create useWorkHoursCalculator composable: `frontend/src/composables/useWorkHoursCalculator.ts` (即時計算與午休提示)
- [X] T130 [US1] Implement TimesheetFormView: `frontend/src/views/timesheets/TimesheetFormView.vue` (填報工時表單，包含即時工時預覽與午休提示)
- [X] T131 [P] [US1] Implement TimesheetForm component: `frontend/src/components/timesheets/TimesheetForm.vue` (可重用表單元件)
- [X] T132 [P] [US1] Implement WorkHoursCalculator component: `frontend/src/components/timesheets/WorkHoursCalculator.vue` (工時計算預覽顯示)
- [X] T133 [US1] Implement TimesheetListView: `frontend/src/views/timesheets/TimesheetListView.vue` (執行人員的工時記錄列表，支援編輯與刪除)
- [X] T134 [P] [US1] Implement TimesheetCalendarView: `frontend/src/views/timesheets/TimesheetCalendarView.vue` (日曆視圖顯示工時)
- [X] T135 [US1] Add timesheet routes: `frontend/src/router/index.ts` (require EXECUTIVE role)

**Checkpoint**: 🎯 MVP 完成！執行人員可以完整填報工時，系統核心價值已實現

---

## Phase 7: User Story 4 - 部門主管查看部門成員工時 (Priority: P4)

**Goal**: 部門主管可以查看本部門所有成員的工時記錄、生成工時報表、匯出 CSV

**Independent Test**: 可以透過部門主管帳號登入，查看本部門成員的工時彙總與明細，測試篩選功能與匯出功能

### Tests for User Story 4 (Test-First) ⚠️

- [X] T136 [P] [US4] Contract test for GET /api/reports/timesheets: `backend/src/test/java/com/example/timesheet/contract/ReportApiContractTest.java`
- [X] T137 [P] [US4] Contract test for GET /api/reports/timesheets/export: `backend/src/test/java/com/example/timesheet/contract/ReportApiContractTest.java`
- [X] T138 [P] [US4] Integration test for timesheet report with filters: `backend/src/test/java/com/example/timesheet/integration/ReportIntegrationTest.java`
- [X] T139 [P] [US4] Integration test for CSV export: `backend/src/test/java/com/example/timesheet/integration/ReportIntegrationTest.java`

### Implementation for User Story 4

- [X] T140 [US4] Create ReportService: `backend/src/main/java/com/example/timesheet/service/ReportService.java` (工時報表查詢、CSV 匯出、分頁處理)
- [X] T141 [P] [US4] Create Report response DTOs: `backend/src/main/java/com/example/timesheet/dto/response/TimesheetReportResponse.java`, `ProjectSummaryReportResponse.java`
- [X] T142 [US4] Implement ReportController: `backend/src/main/java/com/example/timesheet/controller/ReportController.java` (GET /api/reports/timesheets, /export, /projects/summary)
- [X] T143 [P] [US4] Create frontend report API: `frontend/src/api/reports.ts`
- [X] T144 [P] [US4] Create frontend report types: `frontend/src/types/report.ts`
- [X] T145 [US4] Implement TimesheetReportView: `frontend/src/views/reports/TimesheetReportView.vue` (部門工時報表，支援日期、成員、專案篩選)
- [X] T146 [P] [US4] Implement ReportFilterPanel component: `frontend/src/components/reports/ReportFilterPanel.vue`
- [X] T147 [P] [US4] Implement ReportSummaryCard component: `frontend/src/components/reports/ReportSummaryCard.vue` (顯示統計摘要)
- [X] T148 [US4] Implement ProjectReportView: `frontend/src/views/reports/ProjectReportView.vue` (專案彙總報表)
- [X] T149 [US4] Add report routes: `frontend/src/router/index.ts` (require DEPT_HEAD, PM, or MANAGER role)

**Checkpoint**: 部門主管功能完整，可以查看與匯出工時報表

---

## Phase 8: Authentication & Common UI

**Purpose**: 登入功能與共用 UI 元件

- [X] T150 [P] Implement AuthService: `backend/src/main/java/com/example/timesheet/service/AuthService.java` (login, change password)
- [X] T151 [P] Create LoginRequest, LoginResponse DTOs: `backend/src/main/java/com/example/timesheet/dto/request/LoginRequest.java` and response
- [X] T152 [P] Implement AuthController: `backend/src/main/java/com/example/timesheet/controller/AuthController.java` (POST /api/auth/login, /change-password, GET /me)
- [X] T153 [P] Create frontend auth API: `frontend/src/api/auth.ts`
- [X] T154 [P] Implement LoginView: `frontend/src/views/auth/LoginView.vue`
- [X] T155 [P] Implement ChangePasswordView: `frontend/src/views/auth/ChangePasswordView.vue`
- [X] T156 [P] Implement AppHeader component: `frontend/src/components/common/AppHeader.vue` (導航列，包含用戶資訊與登出)
- [X] T157 [P] Implement AppSidebar component: `frontend/src/components/common/AppSidebar.vue` (側邊選單，根據角色顯示功能)
- [X] T158 [P] Implement LoadingSpinner component: `frontend/src/components/common/LoadingSpinner.vue`
- [X] T159 [P] Implement ErrorBoundary component: `frontend/src/components/common/ErrorBoundary.vue`
- [X] T160 Implement main layout: `frontend/src/views/layouts/MainLayout.vue` (包含 Header + Sidebar + RouterView)
- [X] T161 Setup route guards: Enhance `frontend/src/router/index.ts` with authentication and role-based access control

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T162 [P] Add database indexes: `backend/src/main/resources/db/migration/V3__add_indexes.sql` (按 data-model.md 的索引策略)
- [X] T163 [P] Backend unit tests for services: `backend/src/test/java/com/example/timesheet/service/*Test.java` (80% coverage target)
- [X] T164 [P] Frontend unit tests for components: `frontend/tests/unit/components/**/*.spec.ts` (Vitest)
- [X] T165 [P] Frontend E2E tests for critical flows: `frontend/tests/e2e/login.spec.ts`, `timesheet.spec.ts`, `project.spec.ts` (Playwright)
- [X] T166 [P] Setup Spring Boot Actuator: `backend/pom.xml` dependency + `application.yml` endpoints configuration
- [X] T167 [P] Configure CORS properly: Update `SecurityConfig.java` with frontend URL
- [X] T168 [P] Add input validation messages (繁體中文): `backend/src/main/resources/ValidationMessages.properties`
- [X] T169 [P] Frontend accessibility improvements: WCAG 2.1 AA compliance (keyboard navigation, ARIA labels, color contrast)
- [X] T170 [P] Frontend performance optimization: Code splitting, lazy loading routes, bundle size analysis
- [X] T171 [P] Security hardening: Rate limiting, SQL injection prevention verification, XSS protection
- [X] T172 [P] Add API documentation examples: Enhance OpenAPI annotations with comprehensive examples
- [X] T173 [P] Create backend README: `backend/README.md` with setup and run instructions
- [X] T174 [P] Create frontend README: `frontend/README.md` with setup and run instructions
- [X] T175 Run quickstart.md validation: Follow `specs/002-timesheet-management/quickstart.md` to verify developer experience
- [X] T176 Code cleanup and refactoring: Remove TODOs, unused imports, apply consistent naming conventions
- [X] T177 [P] Setup monitoring (optional): Prometheus + Grafana configuration for production readiness

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - **BLOCKS all user stories**
- **User Story 5 (Phase 3)**: Depends on Foundational - Can start after Phase 2
- **User Story 3 (Phase 4)**: Depends on Foundational - Can start after Phase 2 (but logically after US5 for testing)
- **User Story 2 (Phase 5)**: Depends on Foundational + US3 (needs projects to create tasks)
- **User Story 1 (Phase 6)**: Depends on Foundational + US2 (needs tasks to log timesheets) 🎯 MVP
- **User Story 4 (Phase 7)**: Depends on Foundational + US1 (needs timesheet data for reports)
- **Authentication & UI (Phase 8)**: Can be developed in parallel with user stories
- **Polish (Phase 9)**: Depends on all desired user stories being complete

### User Story Dependencies

```
Foundational (Phase 2)
    ↓
US5: HR 管理人員 (Phase 3) [基礎設施]
    ↓
US3: 管理層管理專案 (Phase 4)
    ↓
US2: PM 管理任務 (Phase 5)
    ↓
US1: 執行人員填報工時 (Phase 6) 🎯 MVP
    ↓
US4: 部門主管查看報表 (Phase 7)
```

### Within Each User Story

1. **Tests FIRST** (Test-Driven Development):
   - All contract tests marked [P] can run in parallel
   - All integration tests marked [P] can run in parallel
   - Unit tests marked [P] can run in parallel
   - **Tests MUST FAIL before implementation starts**

2. **Backend Implementation**:
   - Entities/Enums (all marked [P]) → parallel
   - Repositories (all marked [P]) → parallel
   - DTOs (all marked [P]) → parallel
   - Mappers (all marked [P]) → parallel
   - Services → sequential (depends on above)
   - Controllers → sequential (depends on services)

3. **Frontend Implementation**:
   - API clients (all marked [P]) → parallel
   - Types (all marked [P]) → parallel
   - Stores (all marked [P]) → parallel
   - Composables (all marked [P]) → parallel
   - Components (most marked [P]) → parallel
   - Views → may depend on components
   - Routes → final step

### Parallel Opportunities

**Within Setup (Phase 1)**:
- T002 (backend init), T003 (frontend init), T004 (backend linting), T005 (frontend linting), T007 (gitignore), T008 (CI/CD) can all run in parallel

**Within Foundational (Phase 2)**:
- After T009 (database schema):
  - T010-T014 (entities, repos) can run in parallel
  - T021-T028 (exception handling, DTOs, frontend setup) can run in parallel
- T015-T018 (auth components) are sequential

**Within Each User Story**:
All tasks marked [P] within the same category can run in parallel. Example for US1:
- All tests (T111-T118) can launch together
- All DTOs (T122-T123) can launch together
- Most components (T129, T131-T132, T134) can launch together

**Across User Stories** (if team has multiple developers):
Once Foundational phase completes:
- Developer A: US5 (Phase 3)
- Developer B: Can start US3 backend after US5 entities
- Developer C: Authentication & UI (Phase 8)

---

## Parallel Example: User Story 1 (執行人員填報工時)

```bash
# Step 1: Launch all tests together (WRITE FIRST, ENSURE FAILURE)
Task: "Contract test for POST /api/timesheets"
Task: "Contract test for POST /api/timesheets/calculate-preview"
Task: "Contract test for PUT /api/timesheets/{timesheetId}"
Task: "Integration test for create timesheet (normal hours)"
Task: "Integration test for create timesheet (lunch deduction)"
Task: "Integration test for edit timesheet (within 3 working days)"
Task: "Integration test for reject edit (beyond 3 days)"
Task: "Unit test for WorkHoursCalculator"

# Wait for all tests to FAIL (confirms test validity)

# Step 2: Launch all DTOs together
Task: "Create Timesheet request DTOs"
Task: "Create Timesheet response DTOs"
Task: "Create TimesheetMapper"

# Step 3: Implement core logic (sequential)
Task: "Create TimesheetEntry entity"
Task: "Create TimesheetRepository"
Task: "Create TimesheetService"
Task: "Implement TimesheetController"

# Step 4: Launch all frontend basics together
Task: "Create frontend timesheet API"
Task: "Create frontend timesheet types"
Task: "Create frontend timesheet store"
Task: "Create useWorkHoursCalculator composable"

# Step 5: Launch frontend components in parallel
Task: "Implement TimesheetForm component"
Task: "Implement WorkHoursCalculator component"
Task: "Implement TimesheetCalendarView"

# Step 6: Implement views (may need components first)
Task: "Implement TimesheetFormView"
Task: "Implement TimesheetListView"

# Step 7: Final integration
Task: "Add timesheet routes"
```

---

## Implementation Strategy

### MVP First (Fastest Path to Value)

**Recommended for initial delivery:**

1. Complete **Phase 1: Setup** (T001-T008)
2. Complete **Phase 2: Foundational** (T009-T028) ⚠️ CRITICAL
3. Complete **Phase 8: Authentication & UI** (T150-T161) for login capability
4. Complete **Phase 3: User Story 5** (T029-T050) to create test users
5. Complete **Phase 4: User Story 3** (T051-T081) to create projects
6. Complete **Phase 5: User Story 2** (T082-T110) to create tasks
7. Complete **Phase 6: User Story 1** (T111-T135) 🎯 **MVP COMPLETE!**
8. **STOP and VALIDATE**: Test entire workflow end-to-end
9. Deploy/demo if ready

**MVP delivers**: Core value - executives can log hours on assigned tasks

**Estimated MVP Scope**: ~135 tasks (T001-T135 + Auth/UI tasks)

### Incremental Delivery (Recommended)

After MVP:

1. **Increment 2**: Add **Phase 7: User Story 4** (T136-T149) for reporting
2. **Increment 3**: Add **Phase 9: Polish** (T162-T177) for production readiness

Each increment:
- Is independently valuable
- Can be deployed separately
- Doesn't break previous functionality
- Adds new capability

### Parallel Team Strategy (If Multiple Developers Available)

**Week 1**: Everyone together
- Complete Setup + Foundational (blocks everything else)
- Complete Authentication & UI basics

**Week 2+**: Split by user story
- Developer A: User Story 5 + User Story 3 (HR & Manager features)
- Developer B: User Story 2 + User Story 1 (PM & Executive features)
- Developer C: User Story 4 + Polish (Reports & refinement)

**Integration**: Each story is independently testable, merge frequently

---

## Testing Strategy (Constitution II - NON-NEGOTIABLE)

### Test-First Approach (TDD)

**For every user story**:

1. **RED**: Write tests first, ensure they FAIL
   - Contract tests verify API contracts
   - Integration tests verify user journeys
   - Unit tests verify business logic

2. **GREEN**: Implement minimum code to make tests PASS
   - Focus on making tests pass
   - Don't over-engineer

3. **REFACTOR**: Clean up code while keeping tests GREEN
   - Improve code quality
   - Remove duplication
   - Keep tests passing

### Test Coverage Requirements

- **80% minimum** overall code coverage (Constitution I)
- **100% critical paths** (工時計算、時數扣除、權限驗證)
- All API endpoints have integration tests
- All business logic has unit tests
- Critical user flows have E2E tests

### Test Execution

```bash
# Backend tests
cd backend
mvn test                          # Unit tests
mvn verify -P integration-test    # Integration tests
mvn clean test jacoco:report      # Coverage report

# Frontend tests
cd frontend
pnpm test:unit                    # Unit tests
pnpm test:e2e                     # E2E tests
pnpm test:unit --coverage         # Coverage report
```

---

## Task Count Summary

- **Phase 1 (Setup)**: 8 tasks
- **Phase 2 (Foundational)**: 20 tasks ⚠️ CRITICAL
- **Phase 3 (US5 - HR)**: 22 tasks
- **Phase 4 (US3 - Manager)**: 31 tasks
- **Phase 5 (US2 - PM)**: 29 tasks
- **Phase 6 (US1 - Executive)**: 27 tasks 🎯 MVP
- **Phase 7 (US4 - Reports)**: 14 tasks
- **Phase 8 (Auth & UI)**: 12 tasks
- **Phase 9 (Polish)**: 16 tasks

**Total**: 177 tasks

**MVP Scope** (Setup + Foundational + Auth + US5 + US3 + US2 + US1): ~137 tasks

**Parallel Opportunities**: ~60% of tasks are marked [P] and can run in parallel within their phase

---

## Notes

- **[P]** = Parallelizable tasks (different files, no sequential dependencies)
- **[Story]** = User story label for traceability (US1, US2, US3, US4, US5)
- All file paths follow the structure defined in [plan.md](plan.md)
- Tests must be written FIRST and FAIL before implementation (TDD)
- Each user story should be independently completable and testable
- Stop at any checkpoint to validate story independently
- Constitution II (Testing Standards) is NON-NEGOTIABLE - all tests are mandatory
- Commit after each task or logical group
- Run `mvn test` and `pnpm test:unit` frequently during development

---

**Document Version**: 1.0  
**Generated**: 2026年2月6日  
**Input Documents**: [plan.md](plan.md), [spec.md](spec.md), [data-model.md](data-model.md), [contracts/api-spec.yaml](contracts/api-spec.yaml), [research.md](research.md), [quickstart.md](quickstart.md)
