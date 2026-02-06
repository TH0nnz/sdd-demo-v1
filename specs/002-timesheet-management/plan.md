# Implementation Plan: 工時管理系統

**Branch**: `002-timesheet-management` | **Date**: 2026-02-06 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-timesheet-management/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

企業內部工時管理系統，支援五種角色（管理層、PM、部門主管、執行人員、HR）協同管理專案時數分配、任務指派與工時填報。核心功能包括：專案與任務的階層式時數管理、執行人員工時填報（支援三工作天內編輯）、PM 即時進度監控、部門主管工時報表查詢、HR 人員權限管理。系統採用樂觀鎖定處理並發，自動扣除固定午休時段（12:00-13:00），工時精度為 0.5 小時，目標達成 99% 可用性並支援 500 並發用戶。

## Technical Context

**Language/Version**: Java 21 (LTS) with Spring Boot 3.2+  
**Primary Dependencies**: Spring Boot Web, Spring Data JPA, Spring Security, Hibernate, Lombok, MapStruct; 前端 Vue 3 with Composition API + TypeScript  
**Storage**: PostgreSQL 14+ (需要 ACID 交易支援、複雜 JOIN 查詢、樂觀鎖定機制 @Version)  
**Testing**: JUnit 5, Mockito, Spring Boot Test, TestContainers (PostgreSQL); 前端 Vitest + Vue Test Utils  
**Target Platform**: Web 應用 - 瀏覽器端 (Chrome/Firefox/Safari 最新兩版本) + Linux 伺服器 (Docker 容器化部署)
**Project Type**: web (前端 + 後端分離架構，RESTful API 或 GraphQL)  
**Performance Goals**: 
- API 回應時間: < 200ms (p95) 簡單查詢，< 1000ms (p95) 複雜彙總
- 頁面載入時間: < 1 秒工時填報介面，< 2 秒儀表板刷新
- 報表匯出: < 10 秒（10,000 筆資料以內）
- 並發支援: 500 並發用戶同時填報工時  

**Constraints**: 
- 99% 可用性（每月停機 < 7.2 小時），需要 health check 和自動重啟機制
- 資料完整性：工時記錄不可丟失，所有時數計算必須使用資料庫交易
- 安全性：基於角色的嚴格權限控制，密碼使用 bcrypt 加密，所有操作需審計日誌
- 合規性：工時記錄超過三工作天不可編輯，但必須永久保存供查詢
- 時區處理：統一使用 Asia/Taipei (UTC+8)
- 午休扣除：跨 12:00-13:00 自動扣除 1 小時，需明確 UI 提示  

**Scale/Scope**: 
- 用戶規模: ~500 並發用戶，預期總用戶數 1,000-2,000
- 資料規模: 每日 ~2,000 筆工時記錄，年度累積 ~500,000 筆
- 專案數量: ~100 個活躍專案，每個專案 10-50 個任務
- 角色數量: 5 種固定角色，不支援自訂角色
- 報表複雜度: 部門彙總、專案彙總、個人明細、時間範圍篩選

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Code Quality Standards ✓
- [x] Code follows established language-specific style guides (將根據選定語言配置 linter)
- [x] Linting and formatting tools configured (將配置 ESLint/Prettier 或等效工具)
- [x] Code review process established (所有 PR 需至少一位 reviewer 批准)
- [x] Complexity metrics defined (將使用 SonarQube 或等效工具監控複雜度)

### Testing Standards (NON-NEGOTIABLE) ✓
- [x] Test strategy defined (unit, integration, contract)
  - 單元測試：所有業務邏輯、時數計算、權限驗證
  - 整合測試：所有 API 端點、資料庫操作
  - 契約測試：前後端 API 契約、角色權限契約
  - E2E 測試：關鍵流程（登入、填報工時、創建任務）
- [x] Minimum 80% code coverage target set (關鍵路徑要求 100% 覆蓋率)
- [x] Test-first approach planned for acceptance criteria (所有 User Story 先寫測試)
- [x] CI/CD pipeline includes automated test execution (GitHub Actions 或等效 CI 工具)

### User Experience Consistency ✓
- [x] UI patterns and terminology documented (將在 quickstart.md 與 design system 中定義)
- [x] Error handling strategy defined
  - 表單驗證：即時反饋，明確指出錯誤欄位
  - API 錯誤：統一格式 `{error: string, code: string, details?: object}`
  - 用戶友善訊息：不暴露技術細節，提供可操作建議
- [x] Accessibility requirements (WCAG 2.1 AA) planned
  - 鍵盤導航：Tab 鍵順序合理，Enter 提交表單
  - 螢幕閱讀器：語意化 HTML，ARIA 標籤
  - 顏色對比：符合 WCAG AA 標準
- [x] User feedback mechanisms identified
  - 載入狀態：骨架屏或 spinner
  - 操作回饋：toast 通知（成功/失敗）
  - 進度指示：大型操作顯示進度條

### Performance Requirements ✓
- [x] Performance targets defined (response times, resource limits)
  - API 回應：< 200ms (p95) 簡單查詢，< 1s (p95) 複雜操作
  - 頁面載入：< 1s 工時填報，< 2s 儀表板
  - 資料庫：連線池最大 50，查詢 timeout 5s
  - 記憶體：容器限制 512MB（後端），256MB（前端構建）
- [x] Performance testing approach outlined
  - 負載測試：使用 k6 或 JMeter 模擬 500 並發用戶
  - 資料庫測試：使用 EXPLAIN ANALYZE 優化查詢
  - 前端測試：使用 Lighthouse 監控效能分數
- [x] Monitoring and instrumentation planned
  - APM：Prometheus + Grafana 或等效工具
  - 日誌：結構化日誌（JSON 格式），集中收集
  - 追蹤：關鍵操作的 tracing（時數計算、權限檢查）
- [x] Performance budgets established
  - Bundle size: < 200KB (gzipped) 初始載入
  - API payload: < 100KB 單次回應
  - 資料庫查詢: < 100ms 單一查詢（無複雜 JOIN）

## Project Structure

### Documentation (this feature)

```text
specs/002-timesheet-management/
├── spec.md              # 功能規格（已完成，含 Clarifications）
├── plan.md              # 本文件 (/speckit.plan 命令輸出)
├── research.md          # Phase 0 輸出（技術選型研究）
├── data-model.md        # Phase 1 輸出（資料模型設計）
├── quickstart.md        # Phase 1 輸出（開發者快速上手指南）
├── contracts/           # Phase 1 輸出（API 契約定義）
│   ├── api-spec.yaml    # OpenAPI/Swagger 規格
│   └── schemas/         # JSON Schema 定義
└── tasks.md             # Phase 2 輸出 (/speckit.tasks 命令 - 本命令不建立)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── models/          # 資料模型（User, Project, Task, TimesheetEntry, Department, TimeRequest）
│   ├── services/        # 業務邏輯（ProjectService, TaskService, TimesheetService, AuthService）
│   ├── api/             # API 路由與控制器
│   │   ├── auth/        # 登入、權限驗證
│   │   ├── projects/    # 專案管理 API
│   │   ├── tasks/       # 任務管理 API
│   │   ├── timesheets/  # 工時填報 API
│   │   ├── reports/     # 報表與匯出 API
│   │   └── users/       # 人員管理 API
│   ├── middleware/      # 權限檢查、錯誤處理、日誌記錄
│   ├── utils/           # 工具函式（時間計算、午休扣除、樂觀鎖定）
│   └── db/              # 資料庫遷移、種子資料
├── tests/
│   ├── unit/            # 單元測試（業務邏輯、工具函式）
│   ├── integration/     # 整合測試（API 端點、資料庫操作）
│   └── contract/        # 契約測試（API 介面驗證）
├── Dockerfile
├── docker-compose.yml
└── package.json (或 requirements.txt, pom.xml 取決於語言選擇)

frontend/
├── src/
│   ├── components/      # 可重用 UI 元件
│   │   ├── forms/       # 表單元件（DatePicker, TimePicker, TaskSelector）
│   │   ├── layouts/     # 佈局元件（Header, Sidebar, Footer）
│   │   └── common/      # 通用元件（Button, Card, Table, Modal）
│   ├── pages/           # 頁面元件（按角色組織）
│   │   ├── executive/   # 執行人員頁面（工時填報、任務列表）
│   │   ├── pm/          # PM 頁面（專案儀表板、任務管理）
│   │   ├── manager/     # 管理層頁面（專案創建、時數審批）
│   │   ├── department/  # 部門主管頁面（部門報表）
│   │   └── hr/          # HR 頁面（人員管理）
│   ├── services/        # API 呼叫封裝
│   ├── hooks/           # React Hooks（或 Composition API）
│   ├── stores/          # 狀態管理（使用者資訊、權限、快取）
│   ├── utils/           # 前端工具函式（格式化、驗證）
│   └── styles/          # 全域樣式、主題配置
├── tests/
│   ├── unit/            # 元件單元測試
│   ├── integration/     # 頁面整合測試
│   └── e2e/             # 端對端測試（Playwright 或 Cypress）
├── public/
├── Dockerfile
└── package.json

database/
├── migrations/          # 資料庫遷移腳本（版本控制）
└── seeds/               # 測試用種子資料
```

**Structure Decision**: 選擇 Web 應用架構（Option 2），前後端完全分離。理由：
1. **職責分離**：前端專注 UI/UX，後端專注業務邏輯和資料完整性
2. **獨立擴展**：前端靜態檔案可用 CDN，後端可水平擴展
3. **技術靈活性**：前後端可獨立選擇最適合的技術棧
4. **開發效率**：前後端團隊可並行開發，透過 API 契約協作
5. **測試便利性**：API 可獨立測試，前端可使用 mock API 開發

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
