# 工時管理系統 - 開發者快速上手指南

**技術棧**: JDK 21 + Spring Boot 3.2 + PostgreSQL 14 + Vue 3  
**目標受眾**: 後端與前端開發人員  
**預計完成時間**: 30 分鐘

---

## 目錄

1. [環境需求](#環境需求)
2. [專案結構](#專案結構)
3. [快速啟動](#快速啟動)
4. [開發流程](#開發流程)
5. [測試執行](#測試執行)
6. [常用命令](#常用命令)
7. [開發技巧](#開發技巧)
8. [常見問題](#常見問題)

---

## 環境需求

### 必要工具

| 工具 | 版本需求 | 安裝驗證命令 | 下載連結 |
|------|---------|-------------|---------|
| JDK | 21+ | `java -version` | [Eclipse Temurin](https://adoptium.net/) |
| Maven | 3.9+ | `mvn -version` | [Maven](https://maven.apache.org/download.cgi) |
| Node.js | 20+ | `node --version` | [Node.js](https://nodejs.org/) |
| pnpm | 8+ | `pnpm --version` | `npm install -g pnpm` |
| Docker Desktop | 最新版 | `docker --version` | [Docker](https://www.docker.com/products/docker-desktop) |
| Git | 2.x | `git --version` | [Git](https://git-scm.com/) |

### 推薦 IDE

**後端開發**:
- [IntelliJ IDEA Ultimate](https://www.jetbrains.com/idea/) (推薦)
  - 內建 Spring Boot 支援
  - 優秀的 JPA/Hibernate 工具
  - 整合測試執行器
- [Visual Studio Code](https://code.visualstudio.com/)
  - 需安裝 Extension Pack for Java

**前端開發**:
- [Visual Studio Code](https://code.visualstudio.com/) (推薦)
  - 必裝套件：Volar, ESLint, Prettier
- [WebStorm](https://www.jetbrains.com/webstorm/)

### macOS 快速安裝

```bash
# 使用 Homebrew 安裝所有工具
brew install --cask temurin@21
brew install maven
brew install node@20
brew install --cask docker
npm install -g pnpm

# 驗證安裝
java -version   # 應顯示 openjdk version "21.x.x"
mvn -version    # 應顯示 Maven 3.9.x
node -v         # 應顯示 v20.x.x
pnpm -v         # 應顯示 8.x.x
docker -v       # 應顯示 Docker version 24.x.x
```

---

## 專案結構

### 後端專案結構 (Spring Boot)

```
timesheet-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/timesheet/
│   │   │       ├── TimesheetApplication.java          # 主程式入口
│   │   │       ├── config/                            # 配置類別
│   │   │       │   ├── SecurityConfig.java            # Spring Security 配置
│   │   │       │   ├── OpenApiConfig.java             # API 文檔配置
│   │   │       │   └── AsyncConfig.java               # 異步任務配置
│   │   │       ├── domain/                            # 領域模型層
│   │   │       │   ├── entity/                        # JPA 實體
│   │   │       │   │   ├── User.java
│   │   │       │   │   ├── Department.java
│   │   │       │   │   ├── Project.java
│   │   │       │   │   ├── Task.java
│   │   │       │   │   ├── TimesheetEntry.java
│   │   │       │   │   └── TimeRequest.java
│   │   │       │   ├── enums/                         # 列舉型別
│   │   │       │   │   ├── UserRole.java
│   │   │       │   │   ├── ProjectStatus.java
│   │   │       │   │   ├── TaskStatus.java
│   │   │       │   │   └── TimeRequestStatus.java
│   │   │       │   └── repository/                    # JPA Repository
│   │   │       │       ├── UserRepository.java
│   │   │       │       ├── ProjectRepository.java
│   │   │       │       ├── TaskRepository.java
│   │   │       │       ├── TimesheetRepository.java
│   │   │       │       └── TimeRequestRepository.java
│   │   │       ├── service/                           # 業務邏輯層
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── ProjectService.java
│   │   │       │   ├── TaskService.java
│   │   │       │   ├── TimesheetService.java
│   │   │       │   ├── TimeRequestService.java
│   │   │       │   └── ReportService.java
│   │   │       ├── controller/                        # REST API 控制器
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── UserController.java
│   │   │       │   ├── ProjectController.java
│   │   │       │   ├── TaskController.java
│   │   │       │   ├── TimesheetController.java
│   │   │       │   ├── TimeRequestController.java
│   │   │       │   └── ReportController.java
│   │   │       ├── dto/                               # 資料傳輸物件
│   │   │       │   ├── request/                       # 請求 DTO
│   │   │       │   └── response/                      # 回應 DTO
│   │   │       ├── mapper/                            # Entity <-> DTO 轉換
│   │   │       │   └── (MapStruct 產生的類別)
│   │   │       ├── exception/                         # 自訂例外
│   │   │       │   ├── BusinessException.java
│   │   │       │   ├── EntityNotFoundException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── security/                          # 安全相關
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── CustomUserDetailsService.java
│   │   │       └── util/                              # 工具類別
│   │   │           ├── WorkHoursCalculator.java       # 工時計算工具
│   │   │           └── DateUtils.java
│   │   └── resources/
│   │       ├── application.yml                         # 主配置檔
│   │       ├── application-dev.yml                     # 開發環境配置
│   │       ├── application-prod.yml                    # 生產環境配置
│   │       └── db/
│   │           └── migration/                          # Flyway 資料庫遷移
│   │               ├── V1__init_schema.sql
│   │               ├── V2__insert_sample_data.sql
│   │               └── V3__add_indexes.sql
│   └── test/
│       ├── java/
│       │   └── com/example/timesheet/
│       │       ├── integration/                        # 整合測試
│       │       │   ├── AuthIntegrationTest.java
│       │       │   ├── ProjectIntegrationTest.java
│       │       │   ├── TaskIntegrationTest.java
│       │       │   └── TimesheetIntegrationTest.java
│       │       ├── service/                            # 單元測試
│       │       │   ├── ProjectServiceTest.java
│       │       │   ├── TaskServiceTest.java
│       │       │   └── TimesheetServiceTest.java
│       │       └── util/                               # 工具類別測試
│       │           └── WorkHoursCalculatorTest.java
│       └── resources/
│           ├── application-test.yml                    # 測試環境配置
│           └── test-data.sql                           # 測試資料
├── pom.xml                                             # Maven 依賴配置
└── README.md                                           # 專案說明
```

### 前端專案結構 (Vue 3)

```
timesheet-frontend/
├── src/
│   ├── main.ts                                         # 應用程式入口
│   ├── App.vue                                         # 根元件
│   ├── router/
│   │   └── index.ts                                    # 路由配置
│   ├── stores/                                         # Pinia 狀態管理
│   │   ├── auth.ts                                     # 認證狀態
│   │   ├── project.ts                                  # 專案狀態
│   │   ├── task.ts                                     # 任務狀態
│   │   └── timesheet.ts                                # 工時狀態
│   ├── views/                                          # 頁面元件
│   │   ├── auth/
│   │   │   ├── LoginView.vue
│   │   │   └── ChangePasswordView.vue
│   │   ├── projects/
│   │   │   ├── ProjectListView.vue
│   │   │   ├── ProjectDetailView.vue
│   │   │   └── ProjectDashboardView.vue
│   │   ├── tasks/
│   │   │   ├── TaskListView.vue
│   │   │   └── TaskDetailView.vue
│   │   ├── timesheets/
│   │   │   ├── TimesheetListView.vue
│   │   │   ├── TimesheetFormView.vue
│   │   │   └── TimesheetCalendarView.vue
│   │   ├── reports/
│   │   │   ├── TimesheetReportView.vue
│   │   │   └── ProjectReportView.vue
│   │   └── users/
│   │       ├── UserListView.vue
│   │       └── UserFormView.vue
│   ├── components/                                     # 可重用元件
│   │   ├── common/
│   │   │   ├── AppHeader.vue
│   │   │   ├── AppSidebar.vue
│   │   │   └── LoadingSpinner.vue
│   │   ├── projects/
│   │   │   ├── ProjectCard.vue
│   │   │   └── ProjectForm.vue
│   │   ├── tasks/
│   │   │   ├── TaskCard.vue
│   │   │   └── TaskForm.vue
│   │   └── timesheets/
│   │       ├── TimesheetForm.vue
│   │       └── WorkHoursCalculator.vue
│   ├── composables/                                    # 組合式函式
│   │   ├── useAuth.ts
│   │   ├── useWorkHoursCalculator.ts
│   │   └── usePagination.ts
│   ├── api/                                            # API 呼叫
│   │   ├── client.ts                                   # Axios 配置
│   │   ├── auth.ts
│   │   ├── projects.ts
│   │   ├── tasks.ts
│   │   ├── timesheets.ts
│   │   └── reports.ts
│   ├── types/                                          # TypeScript 型別
│   │   ├── auth.ts
│   │   ├── project.ts
│   │   ├── task.ts
│   │   ├── timesheet.ts
│   │   └── common.ts
│   ├── utils/                                          # 工具函式
│   │   ├── date.ts
│   │   ├── format.ts
│   │   └── validation.ts
│   └── assets/                                         # 靜態資源
│       ├── styles/
│       │   ├── main.css
│       │   └── variables.css
│       └── images/
├── tests/
│   ├── unit/                                           # 單元測試
│   │   ├── components/
│   │   │   ├── TimesheetForm.spec.ts
│   │   │   └── TaskCard.spec.ts
│   │   └── composables/
│   │       └── useWorkHoursCalculator.spec.ts
│   └── e2e/                                            # E2E 測試
│       ├── login.spec.ts
│       ├── timesheet.spec.ts
│       └── project.spec.ts
├── package.json                                        # npm 套件配置
├── vite.config.ts                                      # Vite 建構配置
├── tsconfig.json                                       # TypeScript 配置
├── playwright.config.ts                                # Playwright 配置
└── README.md                                           # 專案說明
```

---

## 快速啟動

### Step 1: 複製專案

```bash
# 複製 Git 儲存庫
git clone https://github.com/your-org/timesheet-system.git
cd timesheet-system

# 查看專案結構
ls -la
# 應該看到 timesheet-backend/ 和 timesheet-frontend/ 兩個資料夾
```

### Step 2: 啟動 PostgreSQL 資料庫

```bash
# 使用 Docker Compose 啟動資料庫
cd timesheet-backend
docker-compose up -d postgres

# 驗證資料庫已啟動
docker ps
# 應該看到 postgres:14 container 正在執行

# 查看資料庫日誌
docker-compose logs -f postgres
```

**docker-compose.yml** 範例：

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: timesheet-db
    environment:
      POSTGRES_DB: timesheet_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

### Step 3: 啟動後端 (Spring Boot)

```bash
cd timesheet-backend

# 安裝依賴並執行測試（首次執行）
mvn clean install

# 啟動開發伺服器（熱重載）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用 Maven Wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**預期輸出**：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.2.2)

2026-02-06 10:00:00.123  INFO 12345 --- [main] c.e.t.TimesheetApplication : Started TimesheetApplication in 5.678 seconds
2026-02-06 10:00:00.125  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
```

**驗證後端啟動成功**：

```bash
# 測試健康檢查端點
curl http://localhost:8080/actuator/health

# 輸出應為：{"status":"UP"}

# 開啟 Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Step 4: 啟動前端 (Vue 3)

```bash
# 開啟新終端機視窗
cd timesheet-frontend

# 安裝依賴（首次執行）
pnpm install

# 啟動開發伺服器
pnpm dev
```

**預期輸出**：

```
VITE v5.0.11  ready in 1234 ms

➜  Local:   http://localhost:5173/
➜  Network: http://192.168.1.100:5173/
➜  press h to show help
```

**驗證前端啟動成功**：

```bash
# 開啟瀏覽器
open http://localhost:5173
```

### Step 5: 測試系統

**預設測試帳號**（自動建立於資料庫遷移 `V2__insert_sample_data.sql`）：

| 角色 | 電子郵件 | 密碼 | 用途 |
|------|---------|------|------|
| 管理層 | manager@example.com | password123 | 建立專案、審批時數申請 |
| PM | pm@example.com | password123 | 管理任務、申請時數 |
| 部門主管 | dept-head@example.com | password123 | 查看部門工時報表 |
| 執行人員 | executive@example.com | password123 | 填報工時、標記任務完成 |
| HR | hr@example.com | password123 | 管理用戶、分配角色 |

**測試流程**：

1. 使用 `executive@example.com` 登入
2. 導航到「填報工時」頁面
3. 選擇任務、日期、時間
4. 提交工時記錄
5. 驗證工時計算與午休扣除是否正確

---

## 開發流程

### 後端開發工作流程

#### 1. 建立新功能分支

```bash
git checkout -b feature/add-notification-service
```

#### 2. 開發新功能（TDD 流程）

**Step 1: 寫測試（紅燈）**

```java
// src/test/java/com/example/timesheet/service/NotificationServiceTest.java
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private EmailSender emailSender;
    
    @InjectMocks
    private NotificationService notificationService;
    
    @Test
    @DisplayName("應該在任務剩餘時數不足時發送通知給 PM")
    void shouldNotifyPmWhenTaskHoursInsufficient() {
        // Given
        Task task = Task.builder()
            .id(1L)
            .name("開發登入功能")
            .remainingHours(new BigDecimal("2.0"))
            .build();
        
        User pm = User.builder()
            .id(2L)
            .email("pm@example.com")
            .build();
        
        // When
        notificationService.notifyTaskHoursInsufficient(task, pm);
        
        // Then
        verify(emailSender).send(
            eq("pm@example.com"),
            eq("任務時數不足警告"),
            contains("開發登入功能")
        );
    }
}
```

**Step 2: 執行測試（應該失敗）**

```bash
mvn test -Dtest=NotificationServiceTest
# 預期：Tests run: 1, Failures: 1
```

**Step 3: 實作功能（綠燈）**

```java
// src/main/java/com/example/timesheet/service/NotificationService.java
@Service
public class NotificationService {
    
    @Autowired
    private EmailSender emailSender;
    
    public void notifyTaskHoursInsufficient(Task task, User pm) {
        String subject = "任務時數不足警告";
        String body = String.format(
            "任務「%s」的剩餘時數僅剩 %.1f 小時，建議增加時數分配。",
            task.getName(),
            task.getRemainingHours()
        );
        
        emailSender.send(pm.getEmail(), subject, body);
    }
}
```

**Step 4: 再次執行測試（應該通過）**

```bash
mvn test -Dtest=NotificationServiceTest
# 預期：Tests run: 1, Failures: 0
```

**Step 5: 重構程式碼**

```java
// 提取常數、優化程式碼結構
private static final String INSUFFICIENT_HOURS_SUBJECT = "任務時數不足警告";
private static final String INSUFFICIENT_HOURS_BODY_TEMPLATE = 
    "任務「%s」的剩餘時數僅剩 %.1f 小時，建議增加時數分配。";
```

#### 3. 執行完整測試套件

```bash
# 執行所有單元測試
mvn test

# 執行所有整合測試
mvn verify -P integration-test

# 執行測試並生成覆蓋率報告
mvn clean test jacoco:report
# 報告位置：target/site/jacoco/index.html
```

#### 4. 檢查程式碼品質

```bash
# 執行 Checkstyle 靜態分析
mvn checkstyle:check

# 執行 SpotBugs 偵測潛在 Bug
mvn spotbugs:check

# 執行 PMD 分析
mvn pmd:check
```

#### 5. 提交程式碼

```bash
git add .
git commit -m "feat: add notification service for task hours warning"
git push origin feature/add-notification-service
```

### 前端開發工作流程

#### 1. 建立新元件（TDD 流程）

**Step 1: 寫測試**

```typescript
// tests/unit/components/NotificationBadge.spec.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import NotificationBadge from '@/components/NotificationBadge.vue'

describe('NotificationBadge', () => {
  it('應該顯示未讀通知數量', () => {
    const wrapper = mount(NotificationBadge, {
      props: {
        count: 5
      }
    })
    
    expect(wrapper.find('.badge').text()).toBe('5')
  })
  
  it('當數量超過 99 時應該顯示 99+', () => {
    const wrapper = mount(NotificationBadge, {
      props: {
        count: 150
      }
    })
    
    expect(wrapper.find('.badge').text()).toBe('99+')
  })
})
```

**Step 2: 執行測試（應該失敗）**

```bash
pnpm test:unit NotificationBadge
# 預期：FAIL tests/unit/components/NotificationBadge.spec.ts
```

**Step 3: 實作元件**

```vue
<!-- src/components/NotificationBadge.vue -->
<template>
  <div class="notification-badge">
    <el-badge :value="displayCount" :max="99">
      <el-icon><Bell /></el-icon>
    </el-badge>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bell } from '@element-plus/icons-vue'

interface Props {
  count: number
}

const props = defineProps<Props>()

const displayCount = computed(() => {
  return props.count > 99 ? '99+' : props.count.toString()
})
</script>
```

**Step 4: 執行測試（應該通過）**

```bash
pnpm test:unit NotificationBadge
# 預期：PASS tests/unit/components/NotificationBadge.spec.ts
```

#### 2. 開發 API 整合

```typescript
// src/api/notifications.ts
import client from './client'
import type { Notification, PageResponse } from '@/types'

export const notificationApi = {
  /**
   * Get user's notifications
   */
  async getNotifications(page = 0, size = 20): Promise<PageResponse<Notification>> {
    const response = await client.get('/api/notifications', {
      params: { page, size }
    })
    return response.data
  },
  
  /**
   * Mark notification as read
   */
  async markAsRead(notificationId: number): Promise<void> {
    await client.put(`/api/notifications/${notificationId}/read`)
  },
  
  /**
   * Get unread notification count
   */
  async getUnreadCount(): Promise<number> {
    const response = await client.get('/api/notifications/unread-count')
    return response.data.count
  }
}
```

#### 3. 執行前端測試

```bash
# 執行單元測試
pnpm test:unit

# 執行單元測試（監聽模式）
pnpm test:unit --watch

# 執行 E2E 測試
pnpm test:e2e

# 執行測試並生成覆蓋率報告
pnpm test:unit --coverage
# 報告位置：coverage/index.html
```

#### 4. 程式碼檢查與格式化

```bash
# 執行 ESLint 檢查
pnpm lint

# 自動修復 ESLint 錯誤
pnpm lint:fix

# 執行 Prettier 格式化
pnpm format

# TypeScript 型別檢查
pnpm type-check
```

#### 5. 建構生產版本

```bash
# 建構生產版本
pnpm build

# 預覽生產版本
pnpm preview
```

---

## 測試執行

### 後端測試

#### 單元測試

```bash
# 執行所有單元測試
mvn test

# 執行特定測試類別
mvn test -Dtest=TimesheetServiceTest

# 執行特定測試方法
mvn test -Dtest=TimesheetServiceTest#shouldCreateTimesheetAndDeductTaskHours

# 跳過測試
mvn install -DskipTests
```

#### 整合測試（使用 TestContainers）

```bash
# 執行整合測試（需要 Docker）
mvn verify -P integration-test

# 查看 TestContainers 日誌
docker logs $(docker ps -q --filter ancestor=postgres:14)
```

**整合測試範例**：

```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TimesheetIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("timesheet_test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "EXECUTIVE")
    void shouldCreateTimesheet() throws Exception {
        mockMvc.perform(post("/api/timesheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "taskId": 1,
                        "workDate": "2026-02-06",
                        "startTime": "09:00",
                        "endTime": "12:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.hours").value(3.0))
            .andExpect(jsonPath("$.lunchDeducted").value(false));
    }
}
```

#### 測試覆蓋率報告

```bash
# 生成 JaCoCo 覆蓋率報告
mvn clean test jacoco:report

# 開啟報告
open target/site/jacoco/index.html
```

### 前端測試

#### 單元測試（Vitest）

```bash
# 執行所有單元測試
pnpm test:unit

# 監聽模式（檔案變更自動重新執行）
pnpm test:unit --watch

# 生成覆蓋率報告
pnpm test:unit --coverage
open coverage/index.html
```

#### E2E 測試（Playwright）

```bash
# 安裝 Playwright 瀏覽器（首次執行）
pnpm exec playwright install

# 執行 E2E 測試
pnpm test:e2e

# 開啟 UI 模式（互動式測試）
pnpm exec playwright test --ui

# 執行特定測試檔案
pnpm exec playwright test tests/e2e/login.spec.ts

# 生成測試報告
pnpm exec playwright show-report
```

**E2E 測試範例**：

```typescript
// tests/e2e/timesheet.spec.ts
import { test, expect } from '@playwright/test'

test('執行人員應該能成功填報工時', async ({ page }) => {
  // 登入
  await page.goto('http://localhost:5173/login')
  await page.fill('input[name="email"]', 'executive@example.com')
  await page.fill('input[name="password"]', 'password123')
  await page.click('button[type="submit"]')
  
  // 等待導航到首頁
  await expect(page).toHaveURL('http://localhost:5173/dashboard')
  
  // 導航到工時填報頁面
  await page.click('text=填報工時')
  await expect(page).toHaveURL(/\/timesheets\/create/)
  
  // 填寫表單
  await page.selectOption('select[name="task"]', '1')
  await page.fill('input[name="date"]', '2026-02-06')
  await page.fill('input[name="startTime"]', '09:00')
  await page.fill('input[name="endTime"]', '14:00')
  
  // 驗證工時計算預覽
  await expect(page.locator('.calculated-hours')).toContainText('4.0 小時')
  await expect(page.locator('.lunch-alert')).toContainText('已自動扣除午休時間 1.0 小時')
  
  // 提交表單
  await page.click('button:text("提交")')
  
  // 驗證成功訊息
  await expect(page.locator('.el-message--success')).toContainText('工時記錄已成功建立')
})
```

---

## 常用命令

### 後端常用命令

```bash
# 清理專案
mvn clean

# 編譯專案
mvn compile

# 執行測試
mvn test

# 打包應用程式（JAR）
mvn package

# 跳過測試打包
mvn package -DskipTests

# 執行應用程式（開發模式）
mvn spring-boot:run

# 執行應用程式（指定 profile）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 執行資料庫遷移（Flyway）
mvn flyway:migrate

# 清除資料庫並重新遷移
mvn flyway:clean flyway:migrate

# 生成 API 文檔（OpenAPI）
mvn springdoc-openapi:generate

# 查看依賴樹
mvn dependency:tree

# 更新依賴版本
mvn versions:display-dependency-updates
```

### 前端常用命令

```bash
# 安裝依賴
pnpm install

# 啟動開發伺服器
pnpm dev

# 建構生產版本
pnpm build

# 預覽生產版本
pnpm preview

# 執行 Lint 檢查
pnpm lint

# 自動修復 Lint 錯誤
pnpm lint:fix

# 執行格式化
pnpm format

# 執行單元測試
pnpm test:unit

# 執行 E2E 測試
pnpm test:e2e

# TypeScript 型別檢查
pnpm type-check

# 分析建構產出大小
pnpm build --report
```

### Docker 常用命令

```bash
# 啟動所有服務
docker-compose up -d

# 查看執行中的容器
docker ps

# 查看服務日誌
docker-compose logs -f postgres

# 停止所有服務
docker-compose down

# 停止並移除 volumes
docker-compose down -v

# 重新建構並啟動
docker-compose up -d --build

# 進入 PostgreSQL 容器
docker exec -it timesheet-db psql -U postgres -d timesheet_db

# 備份資料庫
docker exec timesheet-db pg_dump -U postgres timesheet_db > backup.sql

# 還原資料庫
cat backup.sql | docker exec -i timesheet-db psql -U postgres timesheet_db
```

---

## 開發技巧

### 後端開發技巧

#### 1. 使用 Lombok 減少樣板程式碼

```java
// 不使用 Lombok（冗長）
public class User {
    private Long id;
    private String name;
    
    public User() {}
    
    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public boolean equals(Object o) { /* ... */ }
    @Override
    public int hashCode() { /* ... */ }
    @Override
    public String toString() { /* ... */ }
}

// 使用 Lombok（簡潔）
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    private Long id;
    private String name;
}
```

#### 2. 使用 MapStruct 自動生成 DTO 轉換

```java
@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    ProjectResponse toResponse(Project project);
    
    List<ProjectResponse> toResponseList(List<Project> projects);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Project toEntity(CreateProjectRequest request);
}

// 使用方式
@Service
public class ProjectService {
    @Autowired
    private ProjectMapper projectMapper;
    
    public ProjectResponse createProject(CreateProjectRequest request) {
        Project project = projectMapper.toEntity(request);
        // ... 業務邏輯
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }
}
```

#### 3. 使用 @Query 優化查詢效能

```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // 使用 JOIN FETCH 避免 N+1 查詢問題
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " +
           "JOIN FETCH t.assignee a " +
           "WHERE p.pm.id = :pmId")
    List<Task> findTasksByPmIdWithDetails(@Param("pmId") Long pmId);
    
    // 使用 DTO Projection 減少資料載入
    @Query("SELECT new com.example.timesheet.dto.TaskSummaryDTO(" +
           "t.id, t.name, t.estimatedHours, t.usedHours, t.status) " +
           "FROM Task t WHERE t.assignee.id = :assigneeId")
    List<TaskSummaryDTO> findTaskSummariesByAssigneeId(@Param("assigneeId") Long assigneeId);
}
```

#### 4. 使用 Spring Cache 提升效能

```java
@Service
public class ReportService {
    
    // 快取報表結果（5 分鐘過期）
    @Cacheable(value = "timesheetReports", key = "#startDate + '_' + #endDate")
    public TimesheetReportResponse getTimesheetReport(LocalDate startDate, LocalDate endDate) {
        // 執行複雜查詢...
        return report;
    }
    
    // 清除快取
    @CacheEvict(value = "timesheetReports", allEntries = true)
    public void clearReportCache() {
        // 當資料更新時清除快取
    }
}
```

### 前端開發技巧

#### 1. 使用 Composables 重用邏輯

```typescript
// composables/useWorkHoursCalculator.ts
import { computed, Ref } from 'vue'
import dayjs from 'dayjs'

export function useWorkHoursCalculator(
  startTime: Ref<string>,
  endTime: Ref<string>
) {
  const result = computed(() => {
    if (!startTime.value || !endTime.value) return null
    
    const start = dayjs(`2000-01-01 ${startTime.value}`)
    const end = dayjs(`2000-01-01 ${endTime.value}`)
    
    // 計算邏輯...
    return {
      rawHours,
      lunchDeducted,
      lunchHours,
      calculatedHours
    }
  })
  
  return result
}

// 在元件中使用
<script setup lang="ts">
import { ref } from 'vue'
import { useWorkHoursCalculator } from '@/composables/useWorkHoursCalculator'

const startTime = ref('09:00')
const endTime = ref('14:00')

const hoursResult = useWorkHoursCalculator(startTime, endTime)
</script>
```

#### 2. 使用 Pinia 管理全域狀態

```typescript
// stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  
  const isAuthenticated = computed(() => !!token.value)
  const isManager = computed(() => user.value?.role === 'MANAGER')
  
  async function login(email: string, password: string) {
    const response = await authApi.login(email, password)
    token.value = response.token
    user.value = response.user
    localStorage.setItem('token', response.token)
  }
  
  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }
  
  return {
    user,
    token,
    isAuthenticated,
    isManager,
    login,
    logout
  }
})
```

#### 3. 使用 TanStack Query (Vue Query) 管理伺服器狀態

```typescript
// composables/useProjects.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { projectApi } from '@/api/projects'
import type { Project, CreateProjectRequest } from '@/types'

export function useProjects() {
  const queryClient = useQueryClient()
  
  // 查詢專案列表
  const { data: projects, isLoading, error } = useQuery({
    queryKey: ['projects'],
    queryFn: projectApi.getProjects
  })
  
  // 建立專案 Mutation
  const createProjectMutation = useMutation({
    mutationFn: (request: CreateProjectRequest) => projectApi.createProject(request),
    onSuccess: () => {
      // 自動重新取得專案列表
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      ElMessage.success('專案建立成功')
    },
    onError: (error) => {
      ElMessage.error('專案建立失敗')
    }
  })
  
  return {
    projects,
    isLoading,
    error,
    createProject: createProjectMutation.mutate
  }
}
```

#### 4. 使用 VueUse 工具函式

```vue
<script setup lang="ts">
import { useLocalStorage, useDebounceFn, useIntersectionObserver } from '@vueuse/core'

// 持久化狀態到 LocalStorage
const searchQuery = useLocalStorage('search-query', '')

// 防抖搜尋
const search = useDebounceFn((query: string) => {
  // 執行搜尋 API 呼叫
  console.log('Searching for:', query)
}, 500)

// 無限捲動
const target = ref(null)
useIntersectionObserver(target, ([{ isIntersecting }]) => {
  if (isIntersecting) {
    // 載入更多資料
    loadMore()
  }
})
</script>
```

---

## 常見問題

### Q1: 後端啟動失敗：`Cannot connect to database`

**原因**：PostgreSQL 尚未啟動或連線配置錯誤

**解決方案**：

```bash
# 檢查 Docker 容器狀態
docker ps
# 如果沒有看到 postgres container，執行：
docker-compose up -d postgres

# 檢查資料庫連線
docker exec -it timesheet-db psql -U postgres -d timesheet_db -c "SELECT 1;"

# 檢查 application-dev.yml 配置
# 確認連線 URL、用戶名、密碼是否正確
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timesheet_db
    username: postgres
    password: postgres
```

### Q2: 前端 API 呼叫失敗：`CORS error`

**原因**：後端未配置 CORS 允許前端 Origin

**解決方案**：

```java
// 後端配置 CORS
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### Q3: 測試執行失敗：`TestContainers could not start container`

**原因**：Docker Desktop 未啟動或權限不足

**解決方案**：

```bash
# 確認 Docker 正在執行
docker info

# macOS 確認 Docker Desktop 是否已啟動
open -a Docker

# 確認有足夠的資源配置
# Docker Desktop > Preferences > Resources
# 建議至少：CPU: 2, Memory: 4GB
```

### Q4: Maven 編譯錯誤：`package lombok does not exist`

**原因**：Lombok 插件未正確安裝

**解決方案**：

```bash
# IntelliJ IDEA
# 1. Settings > Plugins > 搜尋 "Lombok" 並安裝
# 2. Settings > Build, Execution, Deployment > Compiler > Annotation Processors
#    勾選 "Enable annotation processing"

# VS Code
# 安裝 "Lombok Annotations Support for VS Code" 套件

# 重新執行 Maven
mvn clean install
```

### Q5: 前端型別錯誤：`Type instantiation is excessively deep`

**原因**：TypeScript 型別推導過於複雜

**解決方案**：

```typescript
// 明確定義型別，避免過度推導
// ❌ 錯誤寫法
const projects = ref([])

// ✅ 正確寫法
const projects = ref<Project[]>([])
```

### Q6: E2E 測試失敗：`Timeout waiting for element`

**原因**：元素載入速度較慢或選擇器錯誤

**解決方案**：

```typescript
// 增加等待時間
await page.waitForSelector('text=填報工時', { timeout: 10000 })

// 使用更穩定的選擇器
await page.click('[data-testid="create-timesheet-button"]')

// 元件中添加 data-testid
<el-button data-testid="create-timesheet-button">提交</el-button>
```

### Q7: 資料庫遷移失敗：`Flyway validation error`

**原因**：已執行的遷移檔案被修改

**解決方案**：

```bash
# 開發環境：清除資料庫並重新遷移
mvn flyway:clean flyway:migrate

# 生產環境：建立新的遷移檔案修復問題
# 絕不修改已執行的遷移檔案！
# 範例：V4__fix_user_table.sql
```

---

## 額外資源

### 官方文檔

- [Spring Boot 官方文檔](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vue 3 官方文檔](https://vuejs.org/)
- [Element Plus 文檔](https://element-plus.org/)
- [PostgreSQL 官方文檔](https://www.postgresql.org/docs/)

### 學習資源

- [Baeldung - Spring Boot 教學](https://www.baeldung.com/spring-boot)
- [Vue Mastery - Vue 3 課程](https://www.vuemastery.com/)
- [TestContainers 指南](https://testcontainers.com/guides/)

### 社群支援

- Stack Overflow: [spring-boot](https://stackoverflow.com/questions/tagged/spring-boot), [vue.js](https://stackoverflow.com/questions/tagged/vue.js)
- GitHub Discussions: [專案 GitHub](https://github.com/your-org/timesheet-system/discussions)
- 內部 Slack Channel: #timesheet-dev

---

**文件版本**: 1.0  
**最後更新**: 2026年2月6日  
**維護人員**: 開發團隊  
**回饋**: 如有任何問題或建議，請在 GitHub Issues 提出
