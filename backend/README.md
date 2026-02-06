# 工時管理系統 - 後端 API

Spring Boot REST API for the comprehensive timesheet management system.

## 快速開始

### 前置需求

- Java 17+
- Maven 3.6+
- MySQL 5.7+ 或 PostgreSQL 12+
- Git

### 本地開發設定

```bash
# 1. Clone repository
git clone <repository-url>
cd timesheet-management-system/backend

# 2. 設定環境變數
cp .env.example .env
# 編輯 .env 文件配置資料庫連接、JWT 密鑰等

# 3. 安裝依賴並建置
mvn clean install

# 4. 執行資料庫遷移
mvn flyway:migrate

# 5. 啟動應用程式
mvn spring-boot:run
```

應用程式將在 `http://localhost:8080` 啟動

## 專案結構

```
backend/
├── src/main/java/com/example/timesheet/
│   ├── controller/          # REST API 端點
│   ├── service/             # 業務邏輯
│   ├── repository/          # 資料存取層
│   ├── entity/              # JPA 實體
│   ├── dto/                 # 數據傳輸對象
│   ├── exception/           # 自訂異常
│   ├── security/            # 安全配置與認證
│   ├── config/              # Spring 配置
│   └── util/                # 工具類
├── src/main/resources/
│   ├── application.yml      # 應用配置
│   ├── application-dev.yml  # 開發環境配置
│   ├── application-prod.yml # 生產環境配置
│   └── db/migration/        # Flyway 資料庫遷移
├── src/test/
│   └── java/                # 單元與集成測試
└── pom.xml                  # Maven 配置
```

## API 文檔

完整的 API 文檔可在以下 URL 訪問：

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API JSON**: `http://localhost:8080/v3/api-docs`

### 主要 API 端點

#### 認證
- `POST /api/auth/login` - 用戶登入
- `POST /api/auth/change-password` - 變更密碼
- `GET /api/auth/me` - 獲取當前用戶

#### 工時管理
- `GET /api/timesheets` - 查詢工時列表
- `POST /api/timesheets` - 建立新工時
- `PUT /api/timesheets/{id}` - 編輯工時
- `DELETE /api/timesheets/{id}` - 刪除工時
- `POST /api/timesheets/calculate-preview` - 計算工時預覽

#### 專案管理
- `GET /api/projects` - 查詢專案列表
- `POST /api/projects` - 建立專案
- `PUT /api/projects/{id}` - 編輯專案
- `DELETE /api/projects/{id}` - 刪除專案

#### 任務管理
- `GET /api/tasks` - 查詢任務列表
- `POST /api/tasks` - 建立任務
- `PUT /api/tasks/{id}` - 編輯任務
- `DELETE /api/tasks/{id}` - 刪除任務

#### 用戶管理
- `GET /api/users` - 查詢用戶列表
- `POST /api/users` - 建立用戶
- `PUT /api/users/{id}` - 編輯用戶
- `DELETE /api/users/{id}` - 刪除用戶

#### 報表
- `GET /api/reports/timesheets` - 工時報表
- `GET /api/reports/projects` - 專案報表
- `GET /api/reports/users` - 人員報表

## 測試

### 執行單元測試

```bash
mvn test
```

### 執行集成測試

```bash
mvn verify -P integration-test
```

### 生成代碼覆蓋報告

```bash
mvn clean test jacoco:report
# 報告位置: target/site/jacoco/index.html
```

### 測試覆蓋目標

- **整體覆蓋率**: 80% 最低
- **關鍵路徑**: 100%
  - 工時計算邏輯
  - 權限檢查
  - 編輯窗口驗證

## 應用配置

### application.yml

```yaml
spring:
  application:
    name: timesheet-management
  datasource:
    url: jdbc:mysql://localhost:3306/timesheet_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  flyway:
    baselineOnMigrate: true
    locations: classpath:db/migration

server:
  port: 8080
  servlet:
    context-path: /

# JWT Configuration
app:
  jwtSecret: ${JWT_SECRET:your-secret-key-change-in-production}
  jwtExpirationMs: 86400000  # 24 hours

# Logging
logging:
  level:
    root: INFO
    com.example.timesheet: DEBUG
```

## 安全性

### 認證與授權

- JWT Token 認證
- 基於角色的訪問控制 (RBAC)
- 密碼加密使用 BCrypt

### 支援的角色

1. **ROLE_EXECUTIVE** - 執行人員 (填報工時)
2. **ROLE_PM** - 專案經理 (管理任務)
3. **ROLE_MANAGER** - 管理層 (批准申請、查看報表)
4. **ROLE_DEPT_HEAD** - 部門主管 (查看部門工時)
5. **ROLE_HR** - 人力資源 (用戶管理)

### 安全最佳實踐

- 所有 API 端點使用 HTTPS (生產環境)
- CORS 配置受限於前端域名
- 速率限制防止濫用
- SQL 注入防護已實施
- XSS 保護已啟用

## 業務規則

### 工時填報

1. **工作時數**
   - 範圍: 0 ~ 24 小時
   - 精度: 小數點後一位

2. **午餐扣除**
   - 允許值: 0, 0.5, 1 小時

3. **編輯窗口**
   - 僅能編輯3個工作日內的工時
   - 超出期限需要管理層批准

### 專案管理

1. **專案代碼**
   - 唯一且不可變
   - 格式: 字母、數字、下劃線

2. **時間範圍**
   - 開始日期 ≤ 結束日期
   - 不能是未來日期 (除了計劃項目)

### 數據驗證

所有輸入數據都經過驗證:
- 非空檢查
- 格式驗證
- 業務規則驗證
- 日期範圍檢查

## 性能優化

### 資料庫索引

應用以下索引策略優化查詢性能:

```sql
-- 工時表 (最常查詢)
CREATE INDEX idx_timesheet_employee_month ON timesheets(employee_id, work_date);
CREATE INDEX idx_timesheet_status ON timesheets(status);

-- 專案表
CREATE INDEX idx_project_manager ON projects(manager_id);
CREATE INDEX idx_project_date_range ON projects(start_date, end_date);

-- 任務表
CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_assignee ON tasks(assignee_id);
```

### 緩存策略

- 用戶信息: 5 分鐘
- 部門列表: 10 分鐘
- 專案列表: 15 分鐘

## 監控與日誌

### 健康檢查

```bash
curl http://localhost:8080/actuator/health
```

### 性能指標

```bash
curl http://localhost:8080/actuator/metrics
```

### 日誌配置

- **日誌級別**: INFO (生產) / DEBUG (開發)
- **日誌格式**: JSON (生產) / TEXT (開發)
- **日誌輸出**: 文件 + 控制台

日誌文件位置: `logs/application.log`

## 部署

### Docker 部署

```bash
# 建置 Docker 鏡像
docker build -t timesheet-api:latest .

# 執行容器
docker run -d \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/timesheet_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET=your-secret-key \
  -p 8080:8080 \
  timesheet-api:latest
```

### Kubernetes 部署

見 `k8s/` 目錄中的配置文件

## 故障排除

### 常見問題

1. **資料庫連接失敗**
   ```
   檢查數據庫服務是否運行
   驗證連接字符串和憑證
   檢查防火牆規則
   ```

2. **JWT 驗證失敗**
   ```
   確保 JWT_SECRET 環境變數已設定
   檢查 Token 是否過期
   驗證 Token 格式是否正確
   ```

3. **權限被拒絕**
   ```
   驗證用戶角色配置
   檢查 @PreAuthorize 注解
   查看安全配置
   ```

## 貢獻指南

1. Fork 專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 許可證

此項目採用 MIT 許可證 - 詳見 LICENSE 文件

## 聯絡方式

- **Issue Tracker**: GitHub Issues
- **Email**: support@example.com
- **文檔**: https://docs.example.com

---

**最後更新**: 2026年2月6日  
**API 版本**: 1.0.0  
**Java 版本**: 11+  
**Spring Boot 版本**: 2.7.0+
