# 🚀 快速啟動指南

## 環境檢查結果

✅ **您的環境已就緒！**

通過項目：16 項  
警告項目：3 項（非關鍵）  
失敗項目：0 項

### 警告說明
1. **pnpm 未安裝**：可選但建議安裝，前端可用 npm 替代
2. **PostgreSQL 客戶端未安裝**：可選，Docker 已包含資料庫
3. **端口 5432 已被佔用**：需要關閉現有的 PostgreSQL 服務

---

## 選擇啟動方式

### 🐳 方式 A：Docker Compose（推薦，一鍵啟動）

```bash
# 1. 停止佔用 5432 端口的服務（如果有）
# macOS/Linux:
sudo lsof -ti:5432 | xargs kill -9
# 或者修改 docker-compose.yml 中的端口映射為 "5433:5432"

# 2. 啟動所有服務
docker-compose up -d

# 3. 查看服務狀態
docker-compose ps

# 4. 查看日誌
docker-compose logs -f

# 5. 停止服務
docker-compose down
```

**服務訪問地址：**
- 前端：http://localhost:5173
- 後端 API：http://localhost:8080
- API 文檔：http://localhost:8080/swagger-ui.html
- PostgreSQL：localhost:5432

---

### 💻 方式 B：本地開發（手動啟動）

#### 步驟 1：啟動資料庫

**選項 1 - 使用 Docker（推薦）**
```bash
docker run -d \
  --name timesheet-postgres \
  -e POSTGRES_DB=timesheet_db \
  -e POSTGRES_USER=timesheet_user \
  -e POSTGRES_PASSWORD=timesheet_password \
  -e TZ=Asia/Taipei \
  -p 5433:5432 \
  postgres:14-alpine
```

**選項 2 - 使用本地 PostgreSQL**
```sql
-- 連接到 PostgreSQL
psql -U postgres

-- 建立資料庫和用戶
CREATE DATABASE timesheet_db;
CREATE USER timesheet_user WITH PASSWORD 'timesheet_password';
GRANT ALL PRIVILEGES ON DATABASE timesheet_db TO timesheet_user;
\q
```

#### 步驟 2：啟動後端（新終端機）

```bash
cd backend

# 建置並啟動
mvn clean install -DskipTests
mvn spring-boot:run

# 或者使用 IDE (IntelliJ IDEA / Eclipse)
# 直接運行 TimesheetApplication.java
```

**驗證後端：**
- 健康檢查：http://localhost:8080/actuator/health
- API 文檔：http://localhost:8080/swagger-ui.html

#### 步驟 3：啟動前端（新終端機）

```bash
cd frontend

# 安裝依賴（首次運行）
npm install
# 或使用 pnpm（如果已安裝）
pnpm install

# 啟動開發服務器
npm run dev
# 或
pnpm dev
```

**訪問前端：** http://localhost:5173

---

## 📝 預設測試帳號

系統初始化後會建立以下測試帳號：

| 角色 | 帳號 | 密碼 | 權限 |
|------|------|------|------|
| 系統管理員 | admin | admin123 | 所有權限 |
| HR 經理 | hr_manager | hr123 | HR 管理 |
| 專案經理 | pm_user | pm123 | 專案管理 |
| 一般員工 | employee | emp123 | 查看/提交工時 |

---

## 🧪 驗證系統

### 1. 檢查後端 API

```bash
# 健康檢查
curl http://localhost:8080/actuator/health

# 登入測試
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. 檢查前端

訪問 http://localhost:5173 並嘗試登入。

### 3. 檢查資料庫

```bash
# 如果使用 Docker
docker exec -it timesheet-postgres psql -U timesheet_user -d timesheet_db

# 或使用本地 psql
psql -h localhost -p 5432 -U timesheet_user -d timesheet_db

# 查看資料表
\dt
```

---

## 🔧 常見問題解決

### 問題 1：端口 5432 被佔用

**解決方式 A - 停止現有服務**
```bash
# macOS/Linux
sudo lsof -ti:5432 | xargs kill -9

# Windows
netstat -ano | findstr :5432
taskkill /PID <PID> /F
```

**解決方式 B - 使用其他端口**
修改 `docker-compose.yml`：
```yaml
postgres:
  ports:
    - "5433:5432"  # 使用 5433 端口
```

同時修改後端配置 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/timesheet_db
```

### 問題 2：Maven 建置失敗

```bash
# 清除快取並重新建置
cd backend
mvn clean install -U -DskipTests

# 如果還是失敗，檢查 Java 版本
java -version  # 需要 21+
```

### 問題 3：前端依賴安裝失敗

```bash
cd frontend

# 清除快取
rm -rf node_modules package-lock.json

# 重新安裝
npm install

# 或使用 pnpm
pnpm install
```

### 問題 4：Docker 建置失敗

```bash
# 清除 Docker 快取
docker-compose down -v
docker system prune -a

# 重新建置
docker-compose build --no-cache
docker-compose up -d
```

---

## 📚 下一步

1. **閱讀文檔**
   - [後端 README](./backend/README.md)
   - [前端 README](./frontend/README.md)
   - [環境需求詳細說明](./ENVIRONMENT.md)

2. **開發指南**
   - API 文檔：http://localhost:8080/swagger-ui.html
   - 查看 `specs/` 目錄了解需求與設計

3. **測試**
   ```bash
   # 後端測試
   cd backend && mvn test
   
   # 前端測試
   cd frontend && npm run test:unit
   ```

4. **部署**
   - 參考 `.github/workflows/` 查看 CI/CD 配置
   - 使用 Docker Compose 進行生產部署

---

## 💡 開發建議

- **IDE**：
  - 後端：IntelliJ IDEA（推薦）、Eclipse、VS Code
  - 前端：VS Code（安裝 Volar 插件）

- **資料庫工具**：
  - DBeaver、pgAdmin、DataGrip

- **API 測試**：
  - Postman、Insomnia、Thunder Client

- **瀏覽器插件**：
  - Vue.js DevTools
  - JSON Formatter

---

**最後更新**：2026年2月6日  
**系統狀態**：✅ 生產就緒（Production Ready）
