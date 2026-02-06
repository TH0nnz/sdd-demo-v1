# Quick Start Guide: 報工系統角色權限管理

**Feature**: 004-timesheet-roles  
**Date**: 2026年2月6日  
**Target Audience**: 開發人員

## 概述

本指南幫助開發人員快速設置開發環境並開始實作報工系統的角色權限管理功能。本功能涉及前後端分離架構，後端使用 Spring Boot + PostgreSQL，前端使用 Vue 3。

---

## 前置需求

### 必要工具

- **JDK 24** - [下載頁面](https://jdk.java.net/24/)
- **Maven 3.9+** - [安裝指南](https://maven.apache.org/install.html)
- **Node.js 20+** - [下載頁面](https://nodejs.org/)
- **PostgreSQL 16+** - [下載頁面](https://www.postgresql.org/download/)
- **Docker & Docker Compose** (可選，用於容器化部署) - [安裝指南](https://docs.docker.com/get-docker/)
- **Git** - [下載頁面](https://git-scm.com/downloads)

### IDE 推薦

- **IntelliJ IDEA** (後端開發) - 支援 JDK 24 和 Spring Boot
- **VS Code** (前端開發) - 推薦安裝 Volar, ESLint, Prettier 擴充功能

---

## 快速設定

### 1. Clone 專案並切換到功能分支

```bash
# Clone 專案
git clone <repository-url>
cd sdd-demo-v1

# 切換到功能分支
git checkout 004-timesheet-roles

# 如果分支不存在，則創建
git checkout -b 004-timesheet-roles
```

### 2. 設定資料庫

#### 方式 A: 使用 Docker Compose (推薦)

```bash
# 啟動 PostgreSQL 容器
docker-compose up -d database

# 確認資料庫運行
docker-compose ps
```

#### 方式 B: 手動安裝 PostgreSQL

```bash
# macOS
brew install postgresql@16
brew services start postgresql@16

# Ubuntu/Debian
sudo apt-get install postgresql-16
sudo systemctl start postgresql

# 建立資料庫
createdb timesheet_db
createdb timesheet_db_test
```

### 3. 配置後端

```bash
cd backend

# 複製配置文件範本（如果有的話）
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 編輯配置文件，設定資料庫連接
# application.yml
```

**application.yml 範本**:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timesheet_db
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate  # 使用 Flyway 管理 schema
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

security:
  jwt:
    secret: your-secret-key-change-in-production
    expiration: 86400000  # 24 hours

logging:
  level:
    com.example.timesheet: DEBUG
```

### 4. 啟動後端服務

```bash
cd backend

# 使用 Maven 啟動
mvn spring-boot:run

# 或者編譯後執行
mvn clean package -DskipTests
java --enable-preview -jar target/timesheet-backend-1.0.0.jar
```

後端服務將在 `http://localhost:8080` 啟動。

### 5. 設定並啟動前端

```bash
cd frontend

# 安裝相依套件
npm install

# 啟動開發伺服器
npm run dev
```

前端服務將在 `http://localhost:5173` 啟動。

---

## 資料庫遷移與種子資料

### 執行資料庫遷移

Flyway 會在應用啟動時自動執行遷移腳本。遷移腳本位於 `backend/src/main/resources/db/migration/`。

```bash
# 檢查遷移狀態
mvn flyway:info

# 手動執行遷移
mvn flyway:migrate

# 清空資料庫（開發環境）
mvn flyway:clean
```

### 載入種子資料

```bash
# 執行種子資料腳本
psql -U postgres -d timesheet_db -f database/seeds/001_users_and_departments.sql
psql -U postgres -d timesheet_db -f database/seeds/002_projects_and_tasks.sql
```

**預設測試帳號**:

| 角色 | Email | 密碼 | 描述 |
|------|-------|------|------|
| EXECUTIVE | executive@example.com | password123 | 管理層 |
| PM | pm@example.com | password123 | 專案經理 |
| MANAGER | manager@example.com | password123 | 部門主管 |
| EMPLOYEE | employee@example.com | password123 | 執行人員 |
| HR | hr@example.com | password123 | 人力資源 |

---

## 測試

### 後端測試

```bash
cd backend

# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=TimesheetServiceTest

# 執行測試並生成覆蓋率報告
mvn test jacoco:report
# 報告位於: target/site/jacoco/index.html

# 僅執行單元測試
mvn test -Dgroups=unit

# 僅執行整合測試
mvn test -Dgroups=integration
```

### 前端測試

```bash
cd frontend

# 執行單元測試
npm run test:unit

# 執行 E2E 測試
npm run test:e2e

# 執行測試並生成覆蓋率報告
npm run test:coverage
```

---

## 開發工作流程

### 1. 建立新的實體 (Entity)

**步驟**:

1. 在 `backend/src/main/java/com/example/timesheet/model/` 建立 JPA Entity 類別
2. 在 `backend/src/main/resources/db/migration/` 建立對應的 Flyway 遷移腳本
3. 在 `backend/src/main/java/com/example/timesheet/repository/` 建立 Repository 介面
4. 編寫單元測試驗證實體映射

**範例**:

```java
// User.java
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Size(min = 2, max = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    // ... getters, setters
}

// UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
}
```

### 2. 實作 Service 層業務邏輯

**步驟**:

1. 在 `backend/src/main/java/com/example/timesheet/service/` 建立 Service 類別
2. 注入所需的 Repository
3. 實作業務邏輯並加入權限檢查
4. 編寫單元測試（使用 Mockito）

**範例**:

```java
@Service
@Transactional
public class TimesheetService {
    
    @Autowired
    private TimesheetEntryRepository timesheetRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private WorkdayCalculator workdayCalculator;
    
    public TimesheetEntryDto submitTimesheet(CreateTimesheetEntryRequest request, Long userId) {
        // 1. 驗證日期
        if (!workdayCalculator.isWithinPastThreeWorkdays(request.getDate())) {
            throw new BusinessException("僅能編輯過去三個工作天的工時記錄");
        }
        
        // 2. 檢查任務剩餘時數
        Task task = taskRepository.findById(request.getTaskId())
            .orElseThrow(() -> new NotFoundException("任務不存在"));
        
        if (task.getRemainingHours().compareTo(request.getHours()) < 0) {
            throw new BusinessException("任務時數不足");
        }
        
        // 3. 建立工時記錄
        TimesheetEntry entry = new TimesheetEntry();
        entry.setDate(request.getDate());
        entry.setHours(request.getHours());
        entry.setDescription(request.getDescription());
        entry.setTask(task);
        entry.setUser(userRepository.findById(userId).get());
        
        // 4. 更新任務已使用時數
        task.setUsedHours(task.getUsedHours().add(request.getHours()));
        
        TimesheetEntry saved = timesheetRepository.save(entry);
        return toDto(saved);
    }
}
```

### 3. 實作 REST Controller

**步驟**:

1. 在 `backend/src/main/java/com/example/timesheet/controller/` 建立 Controller 類別
2. 使用 Spring Security 的 `@PreAuthorize` 註解控制權限
3. 定義 DTO (Data Transfer Object) 在 `dto/` 目錄
4. 編寫整合測試（使用 MockMvc）

**範例**:

```java
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    
    @Autowired
    private TimesheetService timesheetService;
    
    @PostMapping("/timesheets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetEntryDto> submitTimesheet(
            @RequestBody @Valid CreateTimesheetEntryRequest request,
            Authentication auth) {
        
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        TimesheetEntryDto dto = timesheetService.submitTimesheet(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
```

### 4. 加入自訂權限檢查

對於複雜的權限邏輯（例如：PM 只能管理自己的專案），使用自訂 `PermissionEvaluator`：

```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }
        
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        
        if ("PROJECT".equals(targetDomainObject) && "MANAGE".equals(permission)) {
            Long projectId = (Long) targetDomainObject;
            Project project = projectRepository.findById(projectId).orElse(null);
            return project != null && project.getPm().getId().equals(principal.getId());
        }
        
        return false;
    }
}

// 使用範例
@PreAuthorize("hasRole('PM') and hasPermission(#projectId, 'PROJECT', 'MANAGE')")
public TaskDto createTask(Long projectId, CreateTaskRequest request) {
    // ...
}
```

### 5. 實作前端組件

**步驟**:

1. 在 `frontend/src/components/` 建立 Vue 組件
2. 在 `frontend/src/api/` 建立 API 客戶端函式
3. 在 `frontend/src/stores/` 建立 Pinia store（如需要）
4. 編寫組件單元測試

**範例 - API 客戶端**:

```typescript
// src/api/timesheets.ts
import apiClient from './client';
import type { TimesheetEntry, CreateTimesheetEntryRequest } from '@/types/timesheet';

export async function submitTimesheet(request: CreateTimesheetEntryRequest): Promise<TimesheetEntry> {
  const response = await apiClient.post<TimesheetEntry>('/employee/timesheets', request);
  return response.data;
}

export async function getMyTimesheets(startDate?: string, endDate?: string): Promise<TimesheetEntry[]> {
  const response = await apiClient.get<TimesheetEntry[]>('/employee/timesheets', {
    params: { startDate, endDate },
  });
  return response.data;
}
```

**範例 - Vue 組件**:

```vue
<!-- src/components/timesheets/TimesheetForm.vue -->
<template>
  <form @submit.prevent="handleSubmit">
    <div class="form-group">
      <label for="date">工時日期</label>
      <input 
        id="date" 
        v-model="form.date" 
        type="date" 
        required 
        :max="today"
      />
    </div>
    
    <div class="form-group">
      <label for="taskId">任務</label>
      <select id="taskId" v-model="form.taskId" required>
        <option v-for="task in tasks" :key="task.id" :value="task.id">
          {{ task.name }} (剩餘: {{ task.remainingHours }}h)
        </option>
      </select>
    </div>
    
    <div class="form-group">
      <label for="hours">工時（小時）</label>
      <input 
        id="hours" 
        v-model.number="form.hours" 
        type="number" 
        step="0.5" 
        min="0.5" 
        max="24" 
        required 
      />
    </div>
    
    <div class="form-group">
      <label for="description">工作描述</label>
      <textarea 
        id="description" 
        v-model="form.description" 
        required 
        minlength="5"
      ></textarea>
    </div>
    
    <button type="submit" :disabled="submitting">提交</button>
    <div v-if="error" class="error">{{ error }}</div>
  </form>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { submitTimesheet } from '@/api/timesheets';
import { getMyTasks } from '@/api/tasks';
import type { Task } from '@/types/task';

const form = ref({
  date: '',
  taskId: null as number | null,
  hours: 0,
  description: '',
});

const tasks = ref<Task[]>([]);
const submitting = ref(false);
const error = ref('');
const today = new Date().toISOString().split('T')[0];

onMounted(async () => {
  tasks.value = await getMyTasks();
});

const handleSubmit = async () => {
  submitting.value = true;
  error.value = '';
  
  try {
    await submitTimesheet(form.value);
    // 成功後重置表單或導航
  } catch (err: any) {
    error.value = err.response?.data?.message || '提交失敗';
  } finally {
    submitting.value = false;
  }
};
</script>
```

---

## API 測試

### 使用 curl

```bash
# 登入取得 JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"employee@example.com","password":"password123"}'

# 使用 token 存取受保護的端點
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/employee/tasks \
  -H "Authorization: Bearer $TOKEN"

# 提交工時
curl -X POST http://localhost:8080/api/employee/timesheets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-02-05",
    "taskId": 1,
    "hours": 8.0,
    "description": "完成使用者登入功能的前端實作"
  }'
```

### 使用 Postman 或 Insomnia

1. 匯入 OpenAPI 規格文件 `specs/004-timesheet-roles/contracts/api-spec.yaml`
2. 設定環境變數：
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: (登入後取得的 JWT)
3. 在 Authorization 標籤中設定 Bearer Token

---

## 常見問題與疑難排解

### 問題 1: 後端啟動失敗 - Flyway migration 錯誤

**症狀**: 應用啟動時報告 Flyway migration 失敗

**解決方案**:

```bash
# 檢查資料庫連線
psql -U postgres -d timesheet_db -c "SELECT 1"

# 清空資料庫並重新遷移（開發環境）
mvn flyway:clean flyway:migrate

# 檢查遷移歷史
mvn flyway:info
```

### 問題 2: JDK 24 預覽功能未啟用

**症狀**: 編譯時報告語法錯誤（如果使用了 JDK 24 預覽功能）

**解決方案**:

確保 `pom.xml` 中配置了 `--enable-preview`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>--enable-preview</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

執行時也需要加上參數：

```bash
java --enable-preview -jar target/timesheet-backend-1.0.0.jar
```

### 問題 3: CORS 錯誤

**症狀**: 前端請求後端 API 時報告 CORS 錯誤

**解決方案**:

確認 `SecurityConfig.java` 中正確配置了 CORS：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### 問題 4: JWT Token 過期或無效

**症狀**: API 請求返回 401 Unauthorized

**解決方案**:

1. 檢查 token 是否過期（預設 24 小時）
2. 重新登入取得新 token
3. 確認 `application.yml` 中的 JWT secret 配置正確

### 問題 5: 測試失敗 - Testcontainers 無法啟動

**症狀**: 整合測試執行時 Testcontainers 無法啟動 PostgreSQL 容器

**解決方案**:

```bash
# 確認 Docker 正在運行
docker ps

# 如果使用 macOS，確認 Docker Desktop 已啟動

# 手動拉取 PostgreSQL 映像
docker pull postgres:16

# 清理舊容器
docker system prune -a
```

---

## 效能監控與除錯

### 啟用 Spring Boot Actuator

在 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

在 `application.yml` 中配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

存取端點：

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`

### SQL 查詢除錯

在 `application.yml` 中啟用 SQL 日誌：

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## 建議的開發順序

1. **Phase 1: 資料模型與資料庫** (1-2 天)
   - [ ] 建立所有 JPA Entity
   - [ ] 編寫 Flyway migration 腳本
   - [ ] 建立 Repository 介面
   - [ ] 編寫 Repository 測試

2. **Phase 2: 核心業務邏輯** (2-3 天)
   - [ ] 實作 Service 層（TimeCalculator, PermissionEvaluator）
   - [ ] 實作工時計算邏輯
   - [ ] 實作權限檢查邏輯
   - [ ] 編寫 Service 單元測試

3. **Phase 3: API 端點** (2-3 天)
   - [ ] 實作所有 REST Controller
   - [ ] 配置 Spring Security 與 JWT
   - [ ] 編寫 Controller 整合測試
   - [ ] 測試權限控制

4. **Phase 4: 前端實作** (3-4 天)
   - [ ] 建立 API 客戶端
   - [ ] 實作角色專屬頁面和組件
   - [ ] 實作路由守衛
   - [ ] 編寫前端測試

5. **Phase 5: 整合與測試** (2-3 天)
   - [ ] 前後端整合測試
   - [ ] E2E 測試
   - [ ] 效能測試
   - [ ] 修復 bug

---

## 參考資源

### 文件

- [Implementation Plan](plan.md)
- [Data Model](data-model.md)
- [API Specification](contracts/api-spec.yaml)
- [Research Report](research.md)

### 外部資源

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Vue 3 Documentation](https://vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

**Last Updated**: 2026年2月6日  
**Maintainer**: Development Team
