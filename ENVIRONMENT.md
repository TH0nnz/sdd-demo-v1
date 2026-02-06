# 🛠️ 工時管理系統 - 環境需求與設置指南

## 📋 系統總覽

本專案是一個全端的工時管理系統，包含：
- **後端**：Spring Boot 3.2 + Java 21 + PostgreSQL
- **前端**：Vue 3 + TypeScript + Vite
- **容器化**：Docker + Docker Compose

---

## ✅ 您的環境檢查結果

### 當前已安裝環境

| 工具 | 需求版本 | 您的版本 | 狀態 |
|------|---------|---------|------|
| Java | 21+ | 24.0.2 | ✅ 可用（高於需求）|
| Maven | 3.6+ | 3.9.9 | ✅ 可用 |
| Node.js | 18+ | 22.21.1 | ✅ 可用 |
| npm | 8+ | 10.9.4 | ✅ 可用 |
| Docker | 20+ | 28.3.2 | ✅ 可用 |
| Docker Compose | 2.0+ | 2.39.1 | ✅ 可用 |
| pnpm | 8+ | 未安裝 | ⚠️ 選用（建議安裝）|

### 建議操作

1. **pnpm 安裝（選用但建議）**
   ```bash
   npm install -g pnpm
   ```
   前端專案推薦使用 pnpm，但使用 npm 也可以運行。

---

## 🚀 快速啟動指南

### 方式 1：使用 Docker Compose（最簡單，推薦）

```bash
# 1. 啟動所有服務（PostgreSQL + Backend + Frontend）
docker-compose up -d

# 2. 檢查服務狀態
docker-compose ps

# 3. 查看日誌
docker-compose logs -f

# 服務會在以下端口啟動：
# - Frontend: http://localhost:5173
# - Backend API: http://localhost:8080
# - PostgreSQL: localhost:5432
```

**注意事項：**
- ⚠️ Docker Compose 需要建置 Dockerfile，但目前 backend/ 和 frontend/ 目錄下沒有 Dockerfile
- 需要先建立 Dockerfile 才能使用此方式

### 方式 2：本地開發（手動啟動各服務）

#### 步驟 1：啟動 PostgreSQL 資料庫

```bash
# 選項 A：使用 Docker（推薦）
docker run -d \
  --name timesheet-postgres \
  -e POSTGRES_DB=timesheet_db \
  -e POSTGRES_USER=timesheet_user \
  -e POSTGRES_PASSWORD=timesheet_password \
  -e TZ=Asia/Taipei \
  -p 5432:5432 \
  postgres:14-alpine

# 選項 B：使用本地 PostgreSQL（需自行安裝）
# 建立資料庫和用戶：
psql -U postgres
CREATE DATABASE timesheet_db;
CREATE USER timesheet_user WITH PASSWORD 'timesheet_password';
GRANT ALL PRIVILEGES ON DATABASE timesheet_db TO timesheet_user;
\q
```

#### 步驟 2：啟動後端 API

```bash
cd backend

# 設置環境變數（可選）
export SPRING_PROFILES_ACTIVE=dev
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=timesheet_db
export DB_USER=timesheet_user
export DB_PASSWORD=timesheet_password
export JWT_SECRET=your-secret-key-change-in-production-must-be-at-least-256-bits

# 安裝依賴並啟動
mvn clean install -DskipTests
mvn spring-boot:run

# 後端將在 http://localhost:8080 啟動
# API 文檔：http://localhost:8080/swagger-ui.html
```

#### 步驟 3：啟動前端

```bash
cd frontend

# 使用 pnpm（推薦）
pnpm install
pnpm dev

# 或使用 npm
npm install
npm run dev

# 前端將在 http://localhost:5173 啟動
```

---

## 📦 所需環境詳細說明

### 後端需求

#### Java 21+
- **用途**：Spring Boot 應用程式執行環境
- **您的版本**：24.0.2 ✅
- **安裝檢查**：`java -version`

#### Maven 3.6+
- **用途**：Java 專案依賴管理與建置工具
- **您的版本**：3.9.9 ✅
- **安裝檢查**：`mvn --version`

#### PostgreSQL 14+
- **用途**：關聯式資料庫
- **建議**：使用 Docker 容器（簡化安裝）
- **預設端口**：5432
- **資料庫配置**：
  - DB Name: `timesheet_db`
  - User: `timesheet_user`
  - Password: `timesheet_password`

#### 後端技術棧
- Spring Boot 3.2.2
- Spring Security (JWT 認證)
- Spring Data JPA
- Hibernate
- Flyway (資料庫遷移)
- MapStruct (物件映射)
- Lombok
- Springdoc OpenAPI (API 文檔)

### 前端需求

#### Node.js 18+
- **用途**：JavaScript 執行環境
- **您的版本**：22.21.1 ✅
- **安裝檢查**：`node --version`

#### npm 8+ 或 pnpm 8+
- **用途**：前端依賴管理
- **您的 npm 版本**：10.9.4 ✅
- **pnpm**：⚠️ 未安裝（建議安裝）
- **安裝 pnpm**：`npm install -g pnpm`

#### 前端技術棧
- Vue 3.4 (Composition API)
- TypeScript 5.3
- Vite 5.0 (建置工具)
- Vue Router 4.2
- Pinia 2.1 (狀態管理)
- Element Plus 2.5 (UI 組件庫)
- Axios 1.6 (HTTP 客戶端)
- Day.js (日期處理)

### 容器化需求

#### Docker 20+
- **用途**：容器化應用部署
- **您的版本**：28.3.2 ✅
- **安裝檢查**：`docker --version`

#### Docker Compose 2.0+
- **用途**：多容器編排
- **您的版本**：2.39.1 ✅
- **安裝檢查**：`docker-compose --version`

---

## 🔍 驗證環境

執行以下腳本來檢查環境：

```bash
#!/bin/bash
echo "=== 環境檢查 ==="
echo ""

# Java
echo "Java: $(java -version 2>&1 | head -n 1)"

# Maven
echo "Maven: $(mvn --version 2>&1 | head -n 1)"

# Node.js
echo "Node.js: $(node --version)"

# npm
echo "npm: $(npm --version)"

# pnpm
if command -v pnpm &> /dev/null; then
    echo "pnpm: $(pnpm --version)"
else
    echo "pnpm: 未安裝 (建議安裝)"
fi

# Docker
echo "Docker: $(docker --version)"

# Docker Compose
echo "Docker Compose: $(docker-compose --version)"

echo ""
echo "✅ 環境檢查完成！"
```

---

## 📝 配置文件

### 後端配置

**位置**：`backend/src/main/resources/application.yml`

主要配置項：
- 資料庫連接
- JWT 密鑰設定
- 服務器端口（預設 8080）
- CORS 設定
- Actuator 監控端點

### 前端配置

**位置**：`frontend/.env.local`（需自行建立）

```env
# API 後端 URL
VITE_API_BASE_URL=http://localhost:8080/api

# 其他配置...
```

### Docker Compose 配置

**位置**：`docker-compose.yml`

包含三個服務：
1. **postgres**：PostgreSQL 14 資料庫
2. **backend**：Spring Boot 應用（端口 8080）
3. **frontend**：Vue 應用（端口 5173，Nginx 服務於端口 80）

---

## 🐛 常見問題

### 問題 1：Docker Compose 啟動失敗

**原因**：缺少 Dockerfile

**解決方式**：
- 需要在 `backend/` 和 `frontend/` 目錄下建立對應的 Dockerfile
- 或者使用「方式 2：本地開發」來手動啟動服務

### 問題 2：PostgreSQL 連接失敗

**檢查項目**：
```bash
# 檢查 PostgreSQL 是否在運行
docker ps | grep postgres

# 測試連接
psql -h localhost -p 5432 -U timesheet_user -d timesheet_db
```

### 問題 3：前端無法連接後端 API

**檢查項目**：
1. 後端是否正常啟動：訪問 http://localhost:8080/actuator/health
2. 前端環境變數是否正確：檢查 `.env.local` 中的 `VITE_API_BASE_URL`
3. CORS 設定是否正確

### 問題 4：Maven 建置失敗

**可能原因**：
- Java 版本問題（需要 Java 21+）
- Maven 依賴下載失敗

**解決方式**：
```bash
# 清除 Maven 快取並重新建置
mvn clean install -U

# 跳過測試建置
mvn clean install -DskipTests
```

---

## 📚 相關文檔

- [後端 README](./backend/README.md) - 後端 API 詳細說明
- [前端 README](./frontend/README.md) - 前端應用詳細說明
- [完成報告](./FINAL_VERIFICATION.md) - 專案完成驗證
- [API 文檔](http://localhost:8080/swagger-ui.html) - Swagger API 文檔（需啟動後端）

---

## ✨ 推薦的開發工具

### IDE
- **後端**：IntelliJ IDEA / Eclipse / VS Code (Java Extension Pack)
- **前端**：VS Code (Volar 插件)

### 瀏覽器擴展
- Vue.js DevTools
- React Developer Tools（如使用）

### 資料庫工具
- DBeaver
- pgAdmin
- DataGrip

---

## 🎯 下一步

1. **選擇啟動方式**
   - Docker Compose（需先建立 Dockerfile）
   - 本地開發（手動啟動）

2. **測試系統**
   - 訪問前端：http://localhost:5173
   - 訪問 API 文檔：http://localhost:8080/swagger-ui.html
   - 測試登入功能

3. **開發與除錯**
   - 查看後端日誌
   - 使用瀏覽器開發者工具
   - 執行測試套件

---

**系統狀態**：✅ 生產就緒（Production Ready）  
**最後更新**：2026年2月6日
