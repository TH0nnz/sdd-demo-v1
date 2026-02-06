# 工時管理系統 - 前端應用

Vue 3 + TypeScript 前端應用，提供完整的工時管理界面。

## 快速開始

### 前置需求

- Node.js 16+ 或 18+
- pnpm 7+ (推薦) 或 npm 8+
- Git

### 本地開發設定

```bash
# 1. Clone repository
git clone <repository-url>
cd timesheet-management-system/frontend

# 2. 安裝依賴
pnpm install
# 或使用 npm
npm install

# 3. 建立環境配置文件
cp .env.example .env.local

# 4. 配置 API 端點 (編輯 .env.local)
VITE_API_URL=http://localhost:8080

# 5. 啟動開發伺服器
pnpm dev
```

應用程式將在 `http://localhost:5173` 啟動

## 專案結構

```
frontend/
├── src/
│   ├── api/                 # API 服務層
│   ├── components/          # Vue 組件
│   │   ├── common/          # 通用組件 (Header, Sidebar, etc.)
│   │   ├── timesheets/      # 工時相關組件
│   │   ├── projects/        # 專案相關組件
│   │   ├── tasks/           # 任務相關組件
│   │   ├── reports/         # 報表組件
│   │   └── ...
│   ├── stores/              # Pinia 狀態管理
│   ├── types/               # TypeScript 類型定義
│   ├── views/               # 頁面組件
│   │   ├── auth/            # 認證頁面
│   │   ├── layouts/         # 佈局模板
│   │   ├── timesheets/      # 工時頁面
│   │   ├── projects/        # 專案頁面
│   │   ├── reports/         # 報表頁面
│   │   └── ...
│   ├── router/              # Vue Router 配置
│   ├── hooks/               # Vue 組合式函數
│   ├── utils/               # 工具函數
│   ├── styles/              # 全局樣式
│   ├── App.vue              # 根組件
│   └── main.ts              # 應用入口
├── tests/
│   ├── unit/                # 單元測試
│   └── e2e/                 # 端到端測試
├── public/                  # 靜態資源
├── .env.example             # 環境變數示例
├── vite.config.ts           # Vite 配置
├── vitest.config.ts         # Vitest 配置
├── playwright.config.ts     # Playwright 配置
├── tsconfig.json            # TypeScript 配置
└── package.json             # 依賴管理
```

## 開發指南

### 開發伺服器

```bash
# 啟動帶熱重載的開發伺服器
pnpm dev

# 構建應用（生產環境）
pnpm build

# 本地預覽生產版本
pnpm preview
```

### 代碼質量

```bash
# 運行 ESLint 檢查代碼
pnpm lint

# 自動修復 ESLint 錯誤
pnpm lint:fix

# 運行 Prettier 格式化代碼
pnpm format

# 檢查代碼類型
pnpm type-check
```

### 測試

```bash
# 運行單元測試
pnpm test:unit

# 運行單元測試並生成覆蓋報告
pnpm test:unit:coverage

# 運行端到端測試
pnpm test:e2e

# 運行所有測試
pnpm test
```

## 核心功能

### 認證與授權

- 用戶登入/登出
- JWT Token 管理
- 基於角色的訪問控制 (RBAC)
- 密碼更改功能

### 工時管理

- 工時填報與編輯
- 工時日曆視圖
- 工時列表查詢
- 午餐扣除計算
- 3 日編輯窗口驗證

### 專案管理

- 專案建立與編輯
- 專案詳情查看
- 專案儀表板
- 進度跟蹤

### 任務管理

- 任務建立與編輯
- 任務指派
- 任務狀態管理
- 進度監控

### 報表分析

- 工時報表
- 專案報表
- 人員報表
- 數據匯出

### 用戶管理

- 用戶列表
- 用戶建立與編輯
- 角色分配
- 部門管理

## 狀態管理 (Pinia)

應用使用 Pinia 進行狀態管理：

```
stores/
├── auth.ts              # 認證狀態
├── timesheets.ts        # 工時狀態
├── projects.ts          # 專案狀態
├── tasks.ts             # 任務狀態
├── reports.ts           # 報表狀態
└── ui.ts                # UI 狀態
```

### 使用示例

```typescript
import { useAuthStore } from '@/stores/auth'

export default {
  setup() {
    const auth = useAuthStore()
    
    // 訪問狀態
    console.log(auth.isAuthenticated)
    
    // 調用 actions
    auth.login(email, password)
  }
}
```

## API 集成

### API 服務層

```
api/
├── auth.ts              # 認證 API
├── timesheets.ts        # 工時 API
├── projects.ts          # 專案 API
├── tasks.ts             # 任務 API
├── reports.ts           # 報表 API
└── users.ts             # 用戶 API
```

### 使用示例

```typescript
import { getTimesheets, createTimesheet } from '@/api/timesheets'

// 查詢工時列表
const timesheets = await getTimesheets({
  startDate: '2026-02-01',
  endDate: '2026-02-28'
})

// 建立新工時
const newTimesheet = await createTimesheet({
  workDate: '2026-02-06',
  workHours: 8.0,
  projectId: 'proj-001'
})
```

## 組件開發

### 組件示例

```vue
<template>
  <div class="my-component">
    <h2>{{ title }}</h2>
    <button @click="handleClick">{{ buttonText }}</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const title = ref('My Component')
const buttonText = ref('Click Me')

const handleClick = () => {
  console.log('Button clicked')
}
</script>

<style scoped lang="scss">
.my-component {
  padding: 1rem;
  
  h2 {
    color: #333;
  }
  
  button {
    padding: 0.5rem 1rem;
    background: #1976d2;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
}
</style>
```

## 路由配置

### 路由結構

```typescript
// src/router/index.ts
const routes = [
  {
    path: '/login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '/timesheets',
        component: () => import('@/views/timesheets/TimesheetListView.vue'),
        meta: { allowedRoles: ['ROLE_EXECUTIVE'] }
      }
      // ... more routes
    ]
  }
]
```

## 環境配置

### .env 變數

```
# API 配置
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000

# 應用配置
VITE_APP_TITLE=工時管理系統
VITE_APP_VERSION=1.0.0

# 特性開關
VITE_ENABLE_ANALYTICS=false
VITE_ENABLE_NOTIFICATIONS=true
```

## 性能優化

### 代碼分割

路由級別的代碼分割已自動配置:

```typescript
// 自動進行代碼分割
const component = () => import('@/views/TimesheetView.vue')
```

### 懶加載

長列表和大型組件使用虛擬滾動:

```vue
<template>
  <virtual-scroller
    :items="largeList"
    :item-size="50"
  >
    <template v-slot="{ item }">
      <div>{{ item.name }}</div>
    </template>
  </virtual-scroller>
</template>
```

### 包大小分析

```bash
# 分析包大小
pnpm build --analyze

# 或使用 visualizer
npm install -D rollup-plugin-visualizer
```

## 浏览器支持

- Chrome (latest)
- Firefox (latest)
- Safari 12+
- Edge (latest)

## 無障礙訪問

遵循 WCAG 2.1 AA 標準:

- ✅ 鍵盤導航支持
- ✅ ARIA 標籤
- ✅ 適當的顏色對比度
- ✅ 屏幕讀者兼容性

## 安全性

- XSS 防護 (Vue 3 自動轉義)
- CSRF Token 處理
- 安全的 HTTP 頭設置
- 敏感數據不存儲在 localStorage (使用 sessionStorage)

## 故障排除

### 常見問題

1. **API 連接失敗**
   ```
   檢查 VITE_API_URL 是否正確配置
   確認後端服務正在運行
   檢查 CORS 配置
   ```

2. **編譯錯誤**
   ```bash
   # 清空緩存並重新安裝
   rm -rf node_modules package-lock.json
   pnpm install
   pnpm dev
   ```

3. **組件不顯示**
   ```
   檢查組件是否正確導入
   驗證路由配置
   檢查控制台錯誤信息
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
**應用版本**: 1.0.0  
**Vue 版本**: 3.3.0+  
**TypeScript 版本**: 5.0+
