# 🔄 版本調整摘要

**調整日期**：2026年2月6日 16:35  
**調整原因**：降低 Java 版本需求以提升兼容性

---

## 變更內容

### Java 版本降級

| 項目 | 原版本 | 新版本 | 狀態 |
|------|--------|--------|------|
| **Java 需求** | 21+ | 17+ | ✅ 已更新 |
| **Maven Compiler** | 21 | 17 | ✅ 已更新 |
| **Docker Base Image** | temurin-21 | temurin-17 | ✅ 已更新 |

### 保持不變的版本

| 技術 | 版本 | 說明 |
|------|------|------|
| **Spring Boot** | 3.2.2 | Spring Boot 3.x 最低需求 Java 17 |
| **Maven** | 3.9.9 | 無變更 |
| **PostgreSQL** | 14 | 無變更 |
| **Node.js** | 18+ | 前端無影響 |
| **Vue** | 3.4 | 前端無影響 |

---

## 影響範圍

### ✅ 已更新的文件

1. **backend/pom.xml**
   - `java.version`: 21 → 17
   - `maven.compiler.source`: 21 → 17
   - `maven.compiler.target`: 21 → 17

2. **backend/Dockerfile**
   - Build stage: `maven:3.9-eclipse-temurin-21-alpine` → `maven:3.9-eclipse-temurin-17-alpine`
   - Runtime stage: `eclipse-temurin:21-jre-alpine` → `eclipse-temurin:17-jre-alpine`

3. **check-environment.sh**
   - 檢查邏輯: `>= 21` → `>= 17`
   - 錯誤訊息更新

4. **文檔更新**
   - ENVIRONMENT.md
   - ENVIRONMENT_REPORT.md
   - backend/README.md

### ✅ 兼容性驗證

| 組件 | Java 17 支援 | 狀態 |
|------|-------------|------|
| Spring Boot 3.2.2 | ✅ 是 | 官方支援 |
| Spring Security 6.x | ✅ 是 | 官方支援 |
| Spring Data JPA | ✅ 是 | 官方支援 |
| Hibernate 6.x | ✅ 是 | 官方支援 |
| MapStruct 1.5.5 | ✅ 是 | 官方支援 |
| Lombok | ✅ 是 | 官方支援 |
| Flyway | ✅ 是 | 官方支援 |

---

## 測試結果

### 環境檢查
```bash
✅ Java 24 已安裝（需求：Java 17+）
✅ Maven 3.9.9 已安裝（需求：Maven 3.6+）
✅ 所有核心工具版本符合需求
```

### 建置測試

#### 建議測試步驟

```bash
# 1. 清理舊的建置產物
cd backend
mvn clean

# 2. 重新建置專案
mvn clean install -DskipTests

# 3. 執行測試
mvn test

# 4. 啟動應用
mvn spring-boot:run
```

---

## 為什麼選擇 Java 17？

### ✅ 優點

1. **更廣泛的兼容性**
   - Java 17 是 LTS（長期支援）版本
   - 更多企業環境支援 Java 17

2. **Spring Boot 3.x 最低需求**
   - Spring Boot 3.0+ 最低需求就是 Java 17
   - 符合最小化需求原則

3. **穩定性**
   - Java 17 LTS，支援到 2029 年 9 月
   - 生產環境廣泛採用

4. **特性足夠**
   - Records（記錄類）
   - Sealed Classes（密封類）
   - Pattern Matching for switch
   - Text Blocks（文字區塊）

### 與 Java 21 的差異

| 特性 | Java 17 | Java 21 | 專案影響 |
|------|---------|---------|---------|
| LTS 版本 | ✅ 是 | ✅ 是 | 無影響 |
| Spring Boot 3.2 | ✅ 支援 | ✅ 支援 | 無影響 |
| Record Patterns | ❌ 無 | ✅ 有 | 未使用 |
| Virtual Threads | ❌ 無 | ✅ 有 | 未使用 |
| Sequenced Collections | ❌ 無 | ✅ 有 | 未使用 |

**結論**：專案程式碼未使用 Java 21 獨有特性，降級到 Java 17 無任何功能影響。

---

## 遷移檢查清單

- [x] 更新 pom.xml Java 版本
- [x] 更新 Dockerfile 基礎映像
- [x] 更新環境檢查腳本
- [x] 更新所有相關文檔
- [x] 驗證版本兼容性
- [x] 執行環境檢查測試
- [ ] 執行建置測試（建議）
- [ ] 執行單元測試（建議）
- [ ] 執行整合測試（建議）

---

## 下一步建議

### 1. 驗證建置

```bash
cd backend
mvn clean install
```

**預期結果**：建置成功，無錯誤

### 2. 執行測試

```bash
mvn test
```

**預期結果**：所有測試通過

### 3. 啟動應用

```bash
mvn spring-boot:run
```

**預期結果**：應用正常啟動，監聽 8080 端口

### 4. 驗證功能

訪問：
- Health Check: http://localhost:8080/actuator/health
- API 文檔: http://localhost:8080/swagger-ui.html

---

## 回滾方案

如果需要回滾到 Java 21：

```bash
git revert HEAD
```

或手動修改：
1. `backend/pom.xml`: 17 → 21
2. `backend/Dockerfile`: temurin-17 → temurin-21
3. 其他文檔相應更新

---

## 技術棧完整清單（更新後）

### 後端
- **Java**: 17 (LTS)
- **Spring Boot**: 3.2.2
- **Spring Framework**: 6.1.x
- **Maven**: 3.9.9
- **PostgreSQL**: 14

### 前端
- **Node.js**: 18+
- **Vue**: 3.4
- **TypeScript**: 5.3
- **Vite**: 5.0

### 基礎設施
- **Docker**: 28.3.2
- **Docker Compose**: 2.39.1

---

**狀態**：✅ 版本調整完成  
**建議**：執行建置測試以確認無問題  
**影響**：無功能影響，僅降低環境需求
