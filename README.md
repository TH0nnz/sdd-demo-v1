# 報工系統 - 角色權限管理系統

> Timesheet Management System with Role-Based Access Control

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![Coverage](https://img.shields.io/badge/coverage-80%25+-green)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

## 📋 專案概述

報工系統是一個企業級的工時管理系統，支援多角色權限控制、專案管理、任務分配、工時填報、時數申請審批等功能。系統採用前後端分離架構，提供 RESTful API 和現代化的 Web 介面。

### 主要功能

- 🔐 **五種角色權限管理**
  - **EXECUTIVE** (管理層): 創建專案、審批時數申請、總覽報表
  - **PM** (專案經理): 管理專案與任務、分配工時、追蹤進度
  - **MANAGER** (部門主管): 查詢部門工時報表、匯出數據
  - **EMPLOYEE** (執行人員): 填報工時、完成任務、申請額外時數
  - **HR** (人力資源): 管理使用者帳號、部門維護

- 📊 **完整工時管理**
  - 每日工時填報（8小時/天）
  - 自動午休時間扣除（1小時）
  - 3個工作日內可編輯限制
  - 工時精度控制（0.5小時）
  - 任務時數不足提醒

- 📈 **專案與任務管理**
  - 專案創建與時數分配
  - 任務拆解與預估時數
  - 任務完成度追蹤
  - 時數消耗監控

- 🔔 **即時通知系統**
  - 任務指派通知
  - 時數不足警告
  - 申請審批結果

## 🏗️ 技術架構

### 後端技術棧

- **框架**: Spring Boot 3.2.2
- **Java 版本**: JDK 17
- **資料庫**: PostgreSQL 18.1
- **安全性**: Spring Security + JWT
- **數據庫遷移**: Flyway
- **API 文件**: SpringDoc OpenAPI (Swagger)
- **測試**: JUnit 5, Testcontainers, MockMvc
- **監控**: Spring Boot Actuator + Prometheus

### 前端技術棧

- **框架**: Vue 3 (Composition API)
- **語言**: TypeScript
- **構建工具**: Vite
- **狀態管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客戶端**: Axios
- **UI 庫**: Element Plus / Tailwind CSS
- **代碼規範**: ESLint + Prettier

### 架構圖

```
┌─────────────────────────────────────────────────────────────────┐
│                          Frontend (Vue 3)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Login View   │  │ Dashboard    │  │ Admin Panel  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                  │
│         └──────────────────┴──────────────────┘                 │
│                            │                                     │
│                     ┌──────▼──────┐                             │
│                     │  API Client  │                             │
│                     │   (Axios)    │                             │
└─────────────────────┴──────────────┴─────────────────────────────┘
                             │
                             │ REST API (HTTP/JSON)
                             │
┌─────────────────────────────▼─────────────────────────────────────┐
│                      Backend (Spring Boot)                         │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    Security Layer                           │   │
│  │  JWT Authentication Filter → Authorization                 │   │
│  └────────────────────────────────────────────────────────────┘   │
│                             │                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                   Controller Layer                          │   │
│  │  Auth | Project | Task | Timesheet | Report | User         │   │
│  └────────────────────────────────────────────────────────────┘   │
│                             │                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    Service Layer                            │   │
│  │  Business Logic + Permission Checks + Calculations          │   │
│  └────────────────────────────────────────────────────────────┘   │
│                             │                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                  Repository Layer                           │   │
│  │  Spring Data JPA Repositories                               │   │
│  └────────────────────────────────────────────────────────────┘   │
└────────────────────────────────┬───────────────────────────────────┘
                                 │
                     ┌───────────▼───────────┐
                     │  PostgreSQL Database  │
                     │  - Users              │
                     │  - Departments        │
                     │  - Projects           │
                     │  - Tasks              │
                     │  - Timesheet Entries  │
                     │  - Hours Requests     │
                     │  - Notifications      │
                     │  - Audit Logs         │
                     └───────────────────────┘
```

## 🚀 快速開始

### 前置需求

- **JDK 17** 或更高版本
- **Maven 3.9+**
- **Node.js 20+** 和 **npm**
- **PostgreSQL 18.1** 或更高版本
- **Docker & Docker Compose** (可選，推薦使用)

### 使用 Docker Compose (推薦)

```bash
# 1. Clone 專案
git clone <repository-url>
cd sdd-demo-v1

# 2. 啟動所有服務
docker-compose up -d

# 3. 查看服務狀態
docker-compose ps

# 4. 查看日誌
docker-compose logs -f backend

# 訪問應用
# - 前端: http://localhost:3000
# - 後端 API: http://localhost:8080
# - API 文件: http://localhost:8080/swagger-ui.html
```

### 手動安裝

#### 1. 設定資料庫

```bash
# 啟動 PostgreSQL
docker-compose up -d database

# 或手動創建資料庫
createdb timesheet_db
```

#### 2. 啟動後端

```bash
cd backend

# 設定環境變數 (可選)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=timesheet_db
export DB_USER=timesheet_user
export DB_PASSWORD=timesheet_password
export JWT_SECRET=your-secret-key-change-in-production

# 編譯並運行
mvn clean install
mvn spring-boot:run

# 後端將運行在 http://localhost:8080
```

#### 3. 啟動前端

```bash
cd frontend

# 安裝依賴
npm install

# 開發模式運行
npm run dev

# 前端將運行在 http://localhost:5173
```

### 測試帳號

系統預設提供以下測試帳號（密碼均為 `password123`）:

| 角色 | Email | 姓名 | 權限 |
|------|-------|------|------|
| EXECUTIVE | manager.zhang@company.com | 張經理 | 創建專案、審批時數申請 |
| PM | pm.wu@company.com | 吳PM | 管理專案與任務 |
| MANAGER | depthead.lin@company.com | 林主管 | 查詢部門報表 |
| EMPLOYEE | exec.zhao@company.com | 趙工程師 | 填報工時 |
| HR | hr.wang@company.com | 王小明 | 管理使用者 |

## 📚 API 文件

### Swagger UI

啟動後端後，訪問 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 查看完整的 API 文件。

### 主要端點

```
認證 (Authentication)
├── POST   /api/auth/login         # 登入
├── POST   /api/auth/logout        # 登出
└── GET    /api/auth/me            # 取得當前使用者資訊

執行人員 (Employee)
├── GET    /api/employee/tasks                    # 取得我的任務
├── GET    /api/employee/timesheets               # 取得我的工時記錄
├── POST   /api/employee/timesheets               # 填報工時
├── PUT    /api/employee/timesheets/{id}          # 更新工時記錄
└── POST   /api/employee/tasks/{id}/complete      # 完成任務

專案經理 (PM)
├── GET    /api/pm/projects                       # 取得我的專案
├── POST   /api/pm/projects/{id}/tasks            # 建立任務
├── PUT    /api/pm/tasks/{id}                     # 更新任務
├── POST   /api/pm/tasks/{id}/close               # 關閉任務
└── GET    /api/pm/hours-requests                 # 查看時數申請

管理層 (Executive)
├── POST   /api/executive/projects                       # 創建專案
├── PUT    /api/executive/projects/{id}                  # 更新專案
├── POST   /api/executive/projects/{id}/close           # 關閉專案
├── GET    /api/executive/hours-requests                # 查看所有時數申請
├── POST   /api/executive/hours-requests/{id}/approve   # 審批通過
└── POST   /api/executive/hours-requests/{id}/reject    # 審批拒絕

部門主管 (Manager)
├── GET    /api/manager/department/employees      # 查詢部門成員
├── GET    /api/manager/department/timesheets     # 查詢部門工時
├── GET    /api/manager/department/reports        # 產生部門報表
└── GET    /api/manager/department/reports/export # 匯出報表

人力資源 (HR)
├── GET    /api/hr/users                  # 查詢所有使用者
├── POST   /api/hr/users                  # 建立使用者
├── PUT    /api/hr/users/{id}             # 更新使用者
├── POST   /api/hr/users/{id}/activate    # 啟用帳號
└── POST   /api/hr/users/{id}/deactivate  # 停用帳號

通知 (Notifications)
├── GET    /api/notifications/unread      # 取得未讀通知
└── POST   /api/notifications/{id}/read   # 標記為已讀
```

詳細的端點說明、請求/回應範例請參考 [API 規格文件](specs/004-timesheet-roles/contracts/api-spec.yaml)。

## 🧪 測試

### 運行後端測試

```bash
cd backend

# 運行所有測試
mvn test

# 運行特定測試類
mvn test -Dtest=TimeCalculationServiceTest

# 產生測試覆蓋率報告
mvn test jacoco:report

# 查看覆蓋率報告
open target/site/jacoco/index.html
```

### 運行前端測試

```bash
cd frontend

# 單元測試
npm run test:unit

# E2E 測試
npm run test:e2e
```

### 測試覆蓋率目標

- **整體覆蓋率**: ≥ 80%
- **關鍵業務邏輯**: 100%
  - 工時計算 (TimeCalculationService)
  - 權限檢查 (PermissionEvaluator)
  - 資料驗證 (Validators)

## 📊 監控與日誌

### 健康檢查

```bash
# 應用健康狀態
curl http://localhost:8080/actuator/health

# 詳細資訊 (需認證)
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/actuator/health
```

### Metrics (Prometheus)

```bash
# Prometheus 格式的 metrics
curl http://localhost:8080/actuator/prometheus
```

### 應用日誌

```bash
# 查看容器日誌
docker-compose logs -f backend

# 查看特定時間範圍
docker-compose logs --since 30m backend
```

## 🔒 安全性

### JWT 認證

系統使用 JWT (JSON Web Token) 進行身份認證：

- **Access Token 有效期**: 24 小時
- **Token 格式**: `Bearer <token>`
- **Token 儲存**: 客戶端 LocalStorage/SessionStorage

### 密碼策略

- 使用 BCrypt 加密（強度 10）
- 生產環境必須更換 JWT Secret
- 建議密碼長度 ≥ 8 字元

### CORS 設定

預設允許的來源：
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:8080`

生產環境請設定 `CORS_ORIGINS` 環境變數。

### 權限矩陣

詳細的角色權限對照表請參考 [角色權限矩陣](docs/role-permissions.md)。

## 📁 專案結構

```
sdd-demo-v1/
├── backend/                          # 後端專案
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/timesheet/
│   │   │   │   ├── config/          # 配置類別
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── domain/
│   │   │   │   │   ├── entity/      # JPA 實體
│   │   │   │   │   └── repository/  # 資料存取層
│   │   │   │   ├── dto/             # 資料傳輸物件
│   │   │   │   ├── exception/       # 異常處理
│   │   │   │   ├── mapper/          # Entity ↔ DTO 轉換
│   │   │   │   ├── security/        # 安全性組件
│   │   │   │   ├── service/         # 業務邏輯層
│   │   │   │   └── util/            # 工具類別
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway 遷移腳本
│   │   │       └── application.yml  # 應用配置
│   │   └── test/                    # 單元測試與整合測試
│   └── pom.xml                      # Maven 配置
│
├── frontend/                        # 前端專案
│   ├── src/
│   │   ├── api/                     # API 客戶端
│   │   ├── components/              # Vue 組件
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # Pinia 狀態管理
│   │   ├── views/                   # 頁面視圖
│   │   └── utils/                   # 工具函數
│   ├── tests/                       # 測試
│   └── package.json                 # NPM 配置
│
├── specs/004-timesheet-roles/       # 功能規格
│   ├── spec.md                      # 需求規格
│   ├── plan.md                      # 實作計畫
│   ├── tasks.md                     # 任務清單
│   ├── data-model.md                # 資料模型
│   ├── research.md                  # 技術研究
│   ├── quickstart.md                # 快速開始指南
│   └── contracts/
│       └── api-spec.yaml            # OpenAPI 規格
│
├── docs/                            # 文件
│   └── role-permissions.md          # 角色權限矩陣
│
├── docker-compose.yml               # Docker Compose 配置
└── README.md                        # 本文件
```

## 🔧 設定

### 環境變數

後端環境變數 (backend):

```bash
# 資料庫
DB_HOST=localhost
DB_PORT=5432
DB_NAME=timesheet_db
DB_USER=timesheet_user
DB_PASSWORD=timesheet_password

# JWT
JWT_SECRET=your-secret-key-change-in-production-must-be-at-least-256-bits

# Server
SERVER_PORT=8080

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:5173
```

前端環境變數 (frontend):

```bash
# API 端點
VITE_API_BASE_URL=http://localhost:8080/api
```

### 應用配置

主要配置文件：`backend/src/main/resources/application.yml`

```yaml
application:
  timezone: Asia/Taipei
  lunch-break:
    start: "12:00"
    end: "13:00"
  timesheet:
    editable-days: 3      # 可編輯工時的工作日數
    precision: 0.5        # 工時精度（小時）
```

## 🤝 貢獻

### 開發流程

1. Fork 本專案
2. 創建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交變更 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 開啟 Pull Request

### 代碼規範

- **後端**: 遵循 [Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)
- **前端**: 遵循 [Vue Style Guide](https://vuejs.org/style-guide/)
- **Commit Message**: 遵循 [Conventional Commits](https://www.conventionalcommits.org/)

### Pull Request 檢查清單

- [ ] 代碼通過所有測試 (`mvn test` 和 `npm test`)
- [ ] 測試覆蓋率 ≥ 80%
- [ ] 通過 Checkstyle 和 ESLint 檢查
- [ ] 更新相關文件
- [ ] 添加/更新單元測試

## 📝 許可證

本專案採用 MIT 許可證 - 詳見 [LICENSE](LICENSE) 文件

## 📞 聯繫方式

- **專案負責人**: Development Team
- **Email**: dev@example.com
- **專案主頁**: [GitHub Repository](https://github.com/your-org/sdd-demo-v1)

## 🙏 致謝

感謝所有為本專案做出貢獻的開發者！

---

**Built with ❤️ by the Development Team**
