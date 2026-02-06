# Research: 工時管理系統技術選型

**Generated**: 2026-02-06 | **Phase**: 0 (Outline & Research)

## Overview

本文件記錄工時管理系統的技術選型研究過程，基於專案需求確定使用 **JDK 21 + Spring Boot + PostgreSQL + Vue 3** 技術棧，並針對關鍵技術決策提供最佳實踐建議。

---

## 1. 後端語言與框架選擇

### 決策：Java 21 (LTS) with Spring Boot 3.2+

**已確定技術棧**：JDK 21 + Spring Boot，理由如下：

**核心優勢**：
1. **企業級穩定性**：Spring Boot 是企業內部系統的工業標準，成熟度極高，長期維護有保障
2. **強型別安全**：Java 編譯時型別檢查，避免大量執行時錯誤，提升程式碼品質與可維護性
3. **效能與並發**：Java 21 的 Virtual Threads (Project Loom) 提供輕量級並發，輕鬆支援 500+ 並發用戶
4. **ORM 工業標準**：Hibernate/JPA 提供強大的 ORM 功能，樂觀鎖定 (@Version) 原生支援
5. **Spring 完整生態**：
   - Spring Security：成熟的認證授權機制
   - Spring Data JPA：簡化資料存取層
   - Spring AOP：宣告式交易管理、權限攔截
   - Spring Validation：統一的資料驗證
6. **測試框架完善**：JUnit 5 + Mockito + TestContainers 提供完整測試解決方案
7. **招聘與團隊**：Java/Spring Boot 開發者市場成熟，企業內部培訓資源豐富

**技術棧細節**：
```xml
<!-- 核心依賴版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.2</version>
</parent>

<properties>
    <java.version>21</java.version>
    <lombok.version>1.18.30</lombok.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <springdoc.version>2.3.0</springdoc.version>
</properties>

<dependencies>
    <!-- Web API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- JPA + PostgreSQL -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- 安全認證 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- 驗證 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- 工具 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>
    
    <!-- API 文檔 -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
    
    <!-- 測試 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 2. 前端框架選擇

### 決策：Vue 3 with Composition API + TypeScript

**已確定技術棧**：Vue 3，理由如下：

**核心優勢**：
1. **學習與維護成本低**：Vue 3 語法直觀，模板結構清晰，新接手的開發者能快速上手
2. **Composition API**：提供與 React Hooks 類似的邏輯組合能力，但更靈活且型別推導更強
3. **官方生態完整**：Vue Router (路由)、Pinia (狀態管理)、Vite (建構工具) 官方整合，版本相容性有保障
4. **企業級 UI 元件**：Element Plus 或 Ant Design Vue 提供完整的繁體中文支援與企業級元件（表單、表格、日期選擇器）
5. **效能優異**：Proxy-based 響應式系統效率高，Virtual DOM 優化良好
6. **TypeScript 支援**：Vue 3 完全用 TypeScript 重寫，型別推導能力優秀
7. **開發體驗**：Vue DevTools, Vite HMR (熱模組替換) 提供流暢的開發體驗

**技術棧細節**：
```json
{
  "dependencies": {
    "vue": "^3.4.15",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "element-plus": "^2.5.4",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.5",
    "@tanstack/vue-query": "^5.17.19",
    "dayjs": "^1.11.10",
    "@vueuse/core": "^10.7.2"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.3",
    "vite": "^5.0.11",
    "typescript": "^5.3.3",
    "@vue/tsconfig": "^0.5.1",
    "vitest": "^1.2.0",
    "@vue/test-utils": "^2.4.3",
    "happy-dom": "^12.10.3",
    "playwright": "^1.40.1",
    "eslint": "^8.56.0",
    "prettier": "^3.1.1"
  }
}
```

---

## 3. 測試策略與框架

### 後端測試 (Java/Spring Boot)

**單元測試**：JUnit 5 + Mockito
```java
@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {
    @Mock
    private TimesheetRepository timesheetRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @InjectMocks
    private TimesheetService timesheetService;
    
    @Test
    @DisplayName("應該成功建立工時記錄並扣除任務剩餘時數")
    void shouldCreateTimesheetAndDeductTaskHours() {
        // Given
        Task task = Task.builder()
            .id(1L)
            .estimatedHours(new BigDecimal("20.0"))
            .usedHours(new BigDecimal("0.0"))
            .build();
            
        TimesheetRequest request = new TimesheetRequest(
            1L, // taskId
            LocalDate.now(),
            LocalTime.of(9, 0),
            LocalTime.of(12, 0)
        );
        
        // When & Then
        // ...測試邏輯
    }
}
```

**整合測試**：Spring Boot Test + TestContainers
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
                        "date": "2026-02-06",
                        "startTime": "09:00",
                        "endTime": "12:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.hours").value(3.0));
    }
}
```

**契約測試**：Spring Cloud Contract
```groovy
Contract.make {
    description "建立工時記錄"
    request {
        method POST()
        url "/api/timesheets"
        body([
            taskId: 1,
            date: "2026-02-06",
            startTime: "09:00",
            endTime: "12:00"
        ])
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 201
        body([
            id: $(regex('[0-9]+')),
            taskId: 1,
            hours: 3.0
        ])
        headers {
            contentType(applicationJson())
        }
    }
}
```

### 前端測試 (Vue 3)

**單元測試**：Vitest + Vue Test Utils
```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TimesheetForm from '@/components/TimesheetForm.vue'

describe('TimesheetForm', () => {
  it('應該自動計算工時', async () => {
    const wrapper = mount(TimesheetForm)
    
    await wrapper.find('input[name="startTime"]').setValue('09:00')
    await wrapper.find('input[name="endTime"]').setValue('12:00')
    
    expect(wrapper.find('.calculated-hours').text()).toBe('3.0 小時')
  })
  
  it('應該顯示午休扣除提示', async () => {
    const wrapper = mount(TimesheetForm)
    
    // 跨越午休時段
    await wrapper.find('input[name="startTime"]').setValue('11:00')
    await wrapper.find('input[name="endTime"]').setValue('14:00')
    
    expect(wrapper.find('.lunch-alert').exists()).toBe(true)
    expect(wrapper.find('.lunch-alert').text()).toContain('已自動扣除午休時間 1 小時')
  })
})
```

**E2E 測試**：Playwright
```typescript
import { test, expect } from '@playwright/test'

test('執行人員應該能成功填報工時', async ({ page }) => {
  // 登入
  await page.goto('http://localhost:5173/login')
  await page.fill('input[name="email"]', 'exec@example.com')
  await page.fill('input[name="password"]', 'password123')
  await page.click('button[type="submit"]')
  
  // 導航到工時填報頁面
  await page.click('text=填報工時')
  
  // 填寫表單
  await page.selectOption('select[name="task"]', '1')
  await page.fill('input[name="date"]', '2026-02-06')
  await page.fill('input[name="startTime"]', '09:00')
  await page.fill('input[name="endTime"]', '12:00')
  
  // 提交
  await page.click('button:text("提交")')
  
  // 驗證成功訊息
  await expect(page.locator('.el-message--success')).toContainText('工時記錄已成功建立')
})
```

---

## 4. 資料庫設計最佳實踐

### 樂觀鎖定實作 (JPA @Version)

**Entity 定義**：
```java
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;
    
    @Column(name = "used_hours", precision = 10, scale = 2)
    private BigDecimal usedHours;
    
    /**
     * 樂觀鎖定版本號
     * JPA 會自動在每次更新時遞增此欄位
     * 若版本號不匹配則拋出 OptimisticLockException
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
    
    // 其他欄位...
}
```

**Service 層處理**：
```java
@Service
@Transactional
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        try {
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("任務不存在"));
            
            // 檢查前端傳來的版本號（可選）
            if (request.getVersion() != null && !request.getVersion().equals(task.getVersion())) {
                throw new OptimisticLockException("資料已被其他用戶修改，請重新載入");
            }
            
            // 更新欄位
            task.setName(request.getName());
            task.setEstimatedHours(request.getEstimatedHours());
            
            // JPA 會自動檢查版本號並遞增
            Task updated = taskRepository.save(task);
            
            return TaskMapper.toResponse(updated);
            
        } catch (OptimisticLockException e) {
            // 版本衝突處理
            throw new ConflictException("資料已被修改，請重新載入後再試一次");
        }
    }
}
```

**異常處理器**：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(
                "OPTIMISTIC_LOCK_ERROR",
                "資料已被其他用戶修改，請重新載入最新資料後再試一次",
                null
            ));
    }
}
```

**前端處理**：
```typescript
// Pinia Store
export const useTaskStore = defineStore('task', {
  state: () => ({
    currentTask: null as Task | null
  }),
  
  actions: {
    async updateTask(taskId: number, updates: Partial<Task>) {
      try {
        const response = await api.put(`/api/tasks/${taskId}`, {
          ...updates,
          version: this.currentTask?.version // 帶上版本號
        })
        
        // 更新本地版本號
        this.currentTask = response.data
        
        ElMessage.success('更新成功')
      } catch (error) {
        if (error.response?.status === 409) {
          ElMessageBox.confirm(
            '資料已被其他用戶修改，是否要重新載入最新資料？',
            '資料衝突',
            {
              confirmButtonText: '重新載入',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(async () => {
            // 重新載入最新資料
            await this.fetchTask(taskId)
          })
        } else {
          ElMessage.error('更新失敗')
        }
      }
    }
  }
})
```

---

## 5. 工時計算與午休扣除

### 後端實作 (Java)

**工具類別**：
```java
public class WorkHoursCalculator {
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final BigDecimal LUNCH_HOURS = new BigDecimal("1.0");
    private static final BigDecimal HOUR_PRECISION = new BigDecimal("0.5");
    
    /**
     * 計算工時，自動扣除午休時段
     * 
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return 工時計算結果
     */
    public static WorkHoursResult calculate(LocalTime startTime, LocalTime endTime) {
        // 計算原始工時
        Duration duration = Duration.between(startTime, endTime);
        BigDecimal rawHours = new BigDecimal(duration.toMinutes())
            .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        
        // 檢查是否跨越午休時段
        boolean crossesLunch = startTime.isBefore(LUNCH_END) && endTime.isAfter(LUNCH_START);
        BigDecimal lunchDeducted = BigDecimal.ZERO;
        
        if (crossesLunch) {
            // 計算午休重疊時間
            LocalTime overlapStart = startTime.isBefore(LUNCH_START) ? LUNCH_START : startTime;
            LocalTime overlapEnd = endTime.isAfter(LUNCH_END) ? LUNCH_END : endTime;
            
            Duration lunchOverlap = Duration.between(overlapStart, overlapEnd);
            BigDecimal overlapHours = new BigDecimal(lunchOverlap.toMinutes())
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
            
            lunchDeducted = overlapHours.min(LUNCH_HOURS);
        }
        
        BigDecimal calculatedHours = rawHours.subtract(lunchDeducted);
        
        // 四捨五入到 0.5 小時
        BigDecimal rounded = calculatedHours
            .divide(HOUR_PRECISION, 0, RoundingMode.HALF_UP)
            .multiply(HOUR_PRECISION);
        
        return WorkHoursResult.builder()
            .startTime(startTime)
            .endTime(endTime)
            .rawHours(rawHours)
            .lunchDeducted(crossesLunch)
            .lunchHours(lunchDeducted)
            .calculatedHours(rounded)
            .build();
    }
}

@Data
@Builder
public class WorkHoursResult {
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal rawHours;
    private boolean lunchDeducted;
    private BigDecimal lunchHours;
    private BigDecimal calculatedHours;
}
```

**API 回應**：
```java
@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {
    
    @PostMapping("/calculate-preview")
    public ResponseEntity<WorkHoursResult> calculatePreview(
            @RequestBody @Valid CalculateRequest request) {
        WorkHoursResult result = WorkHoursCalculator.calculate(
            request.getStartTime(),
            request.getEndTime()
        );
        return ResponseEntity.ok(result);
    }
}
```

### 前端實作 (Vue 3)

**組合式函式**：
```typescript
// composables/useWorkHoursCalculator.ts
import { computed, Ref } from 'vue'
import dayjs from 'dayjs'

export function useWorkHoursCalculator(
  startTime: Ref<string>,
  endTime: Ref<string>
) {
  const LUNCH_START = '12:00'
  const LUNCH_END = '13:00'
  
  const result = computed(() => {
    if (!startTime.value || !endTime.value) {
      return null
    }
    
    const start = dayjs(`2000-01-01 ${startTime.value}`)
    const end = dayjs(`2000-01-01 ${endTime.value}`)
    const lunchStart = dayjs(`2000-01-01 ${LUNCH_START}`)
    const lunchEnd = dayjs(`2000-01-01 ${LUNCH_END}`)
    
    // 計算原始工時
    const rawMinutes = end.diff(start, 'minute')
    const rawHours = rawMinutes / 60
    
    // 檢查是否跨越午休
    const crossesLunch = start.isBefore(lunchEnd) && end.isAfter(lunchStart)
    
    let lunchHours = 0
    if (crossesLunch) {
      const overlapStart = start.isBefore(lunchStart) ? lunchStart : start
      const overlapEnd = end.isAfter(lunchEnd) ? lunchEnd : end
      const overlapMinutes = overlapEnd.diff(overlapStart, 'minute')
      lunchHours = Math.min(overlapMinutes / 60, 1)
    }
    
    const calculatedHours = rawHours - lunchHours
    const rounded = Math.round(calculatedHours * 2) / 2 // 四捨五入到 0.5
    
    return {
      rawHours: Math.round(rawHours * 10) / 10,
      lunchDeducted: crossesLunch,
      lunchHours: Math.round(lunchHours * 10) / 10,
      calculatedHours: rounded
    }
  })
  
  return result
}
```

**元件使用**：
```vue
<template>
  <el-form @submit.prevent="handleSubmit">
    <el-form-item label="起始時間">
      <el-time-picker
        v-model="form.startTime"
        format="HH:mm"
        :step="'00:30'"
      />
    </el-form-item>
    
    <el-form-item label="結束時間">
      <el-time-picker
        v-model="form.endTime"
        format="HH:mm"
        :step="'00:30'"
      />
    </el-form-item>
    
    <!-- 工時預覽 -->
    <el-alert
      v-if="hoursResult"
      :type="hoursResult.lunchDeducted ? 'info' : 'success'"
      :closable="false"
    >
      <template v-if="hoursResult.lunchDeducted">
        <p>已自動扣除午休時間 {{ hoursResult.lunchHours }} 小時（12:00-13:00）</p>
        <p>原始工時：{{ hoursResult.rawHours }} 小時 → 實際工時：<strong>{{ hoursResult.calculatedHours }} 小時</strong></p>
      </template>
      <template v-else>
        <p>預估工時：<strong>{{ hoursResult.calculatedHours }} 小時</strong></p>
      </template>
    </el-alert>
    
    <el-button type="primary" native-type="submit">提交</el-button>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkHoursCalculator } from '@/composables/useWorkHoursCalculator'

const form = ref({
  startTime: '',
  endTime: ''
})

const startTimeStr = computed(() => form.value.startTime)
const endTimeStr = computed(() => form.value.endTime)

const hoursResult = useWorkHoursCalculator(startTimeStr, endTimeStr)
</script>
```

---

## 6. 權限控制架構

### Spring Security 實作

**安全配置**：
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/projects/**").hasAnyRole("MANAGER", "PM")
                .requestMatchers("/api/tasks/**").hasAnyRole("PM", "EXECUTIVE")
                .requestMatchers("/api/timesheets/**").hasAnyRole("EXECUTIVE", "PM", "DEPT_HEAD")
                .requestMatchers("/api/users/**").hasRole("HR")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**方法級權限**：
```java
@Service
public class TimesheetService {
    
    /**
     * 取得工時記錄，自動根據角色過濾
     */
    @PreAuthorize("hasAnyRole('EXECUTIVE', 'PM', 'DEPT_HEAD', 'MANAGER')")
    public Page<TimesheetResponse> getTimesheets(
            TimesheetFilter filter,
            Pageable pageable,
            Authentication auth) {
        
        User currentUser = (User) auth.getPrincipal();
        Specification<Timesheet> spec = TimesheetSpecification.empty();
        
        // 根據角色自動添加過濾條件
        switch (currentUser.getRole()) {
            case EXECUTIVE:
                // 執行人員只能看到自己的工時
                spec = spec.and(TimesheetSpecification.byUserId(currentUser.getId()));
                break;
                
            case PM:
                // PM 只能看到自己管理的專案相關工時
                spec = spec.and(TimesheetSpecification.byPmId(currentUser.getId()));
                break;
                
            case DEPT_HEAD:
                // 部門主管只能看到本部門的工時
                spec = spec.and(TimesheetSpecification.byDepartmentId(currentUser.getDepartmentId()));
                break;
                
            case MANAGER:
            case HR:
                // 管理層和 HR 可以看到所有資料（無額外過濾）
                break;
        }
        
        // 應用用戶提供的其他過濾條件
        if (filter.getStartDate() != null) {
            spec = spec.and(TimesheetSpecification.afterDate(filter.getStartDate()));
        }
        // ... 其他過濾條件
        
        Page<Timesheet> timesheets = timesheetRepository.findAll(spec, pageable);
        return timesheets.map(TimesheetMapper::toResponse);
    }
}
```

---

## 7. 報表生成與匯出策略

### 同步小報表 + 非同步大報表

**Controller**：
```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    /**
     * 小報表（< 1000 筆）：同步回應 CSV
     */
    @GetMapping("/timesheets/export")
    @PreAuthorize("hasAnyRole('DEPT_HEAD', 'PM', 'MANAGER')")
    public ResponseEntity<Resource> exportTimesheetsSmall(
            @ModelAttribute TimesheetFilter filter,
            Authentication auth) throws IOException {
        
        List<Timesheet> timesheets = timesheetService.getTimesheets(filter, auth);
        
        // 生成 CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            // 寫入 BOM 以支援 Excel 開啟時正確顯示中文
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            writer.writeNext(new String[]{"日期", "姓名", "專案", "任務", "工時"});
            
            for (Timesheet ts : timesheets) {
                writer.writeNext(new String[]{
                    ts.getDate().toString(),
                    ts.getUser().getName(),
                    ts.getTask().getProject().getName(),
                    ts.getTask().getName(),
                    ts.getHours().toString()
                });
            }
        }
        
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"timesheets_" + LocalDate.now() + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(resource);
    }
    
    /**
     * 大報表（> 1000 筆）：非同步任務
     */
    @PostMapping("/timesheets/export-async")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ExportTaskResponse> exportTimesheetsAsync(
            @RequestBody TimesheetFilter filter,
            Authentication auth) {
        
        String taskId = UUID.randomUUID().toString();
        
        // 使用 Spring @Async 非同步執行
        reportService.generateLargeReportAsync(taskId, filter, auth);
        
        return ResponseEntity.accepted()
            .body(ExportTaskResponse.builder()
                .taskId(taskId)
                .status("PROCESSING")
                .estimatedTime("2-5 minutes")
                .downloadUrl("/api/reports/download/" + taskId)
                .build());
    }
    
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String taskId) {
        // 從檔案系統或資料庫檢索已完成的報表
        // ...
    }
}
```

**非同步服務**：
```java
@Service
public class ReportService {
    
    @Async
    @Transactional(readOnly = true)
    public void generateLargeReportAsync(
            String taskId,
            TimesheetFilter filter,
            Authentication auth) {
        
        try {
            Path outputPath = Paths.get("/tmp/reports", taskId + ".csv");
            Files.createDirectories(outputPath.getParent());
            
            try (CSVWriter writer = new CSVWriter(
                    new OutputStreamWriter(
                        Files.newOutputStream(outputPath),
                        StandardCharsets.UTF_8))) {
                
                // 寫入 CSV header
                writer.writeNext(new String[]{"日期", "姓名", "專案", "任務", "工時"});
                
                // 分批查詢（避免記憶體溢出）
                int batchSize = 1000;
                int page = 0;
                
                Page<Timesheet> batch;
                do {
                    PageRequest pageRequest = PageRequest.of(page++, batchSize);
                    batch = timesheetRepository.findAll(
                        TimesheetSpecification.fromFilter(filter),
                        pageRequest
                    );
                    
                    for (Timesheet ts : batch.getContent()) {
                        writer.writeNext(new String[]{
                            ts.getDate().toString(),
                            ts.getUser().getName(),
                            ts.getTask().getProject().getName(),
                            ts.getTask().getName(),
                            ts.getHours().toString()
                        });
                    }
                } while (batch.hasNext());
            }
            
            // 更新任務狀態為完成
            updateExportTaskStatus(taskId, "COMPLETED", outputPath.toString());
            
        } catch (Exception e) {
            log.error("生成報表失敗: {}", taskId, e);
            updateExportTaskStatus(taskId, "FAILED", null);
        }
    }
}
```

---

## 8. 技術棧總結

### 後端 (Java + Spring Boot)
```xml
<!-- pom.xml 主要依賴 -->
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.2.2</spring-boot.version>
</properties>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 前端 (Vue 3)
```json
{
  "name": "timesheet-frontend",
  "version": "0.1.0",
  "dependencies": {
    "vue": "^3.4.15",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "element-plus": "^2.5.4",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.5",
    "@tanstack/vue-query": "^5.17.19",
    "dayjs": "^1.11.10",
    "@vueuse/core": "^10.7.2"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.3",
    "vite": "^5.0.11",
    "typescript": "^5.3.3",
    "@vue/tsconfig": "^0.5.1",
    "vitest": "^1.2.0",
    "@vue/test-utils": "^2.4.3",
    "happy-dom": "^12.10.3",
    "playwright": "^1.40.1",
    "eslint": "^8.56.0",
    "eslint-plugin-vue": "^9.20.1",
    "prettier": "^3.1.1"
  }
}
```

### 基礎設施
- **資料庫**: PostgreSQL 14+ (Docker container)
- **容器化**: Docker + Docker Compose
- **CI/CD**: GitHub Actions / GitLab CI
- **監控**: Spring Boot Actuator + Prometheus + Grafana (optional)

---

## 9. 風險評估與緩解

| 風險 | 嚴重性 | 機率 | 緩解策略 |
|------|--------|------|---------|
| Spring Boot 啟動時間過長 | 中 | 低 | 使用 Spring Boot 3.x 原生支援、Virtual Threads、合理配置 Bean 懶載入 |
| JPA N+1 查詢問題 | 高 | 中 | 使用 @EntityGraph、JOIN FETCH、Hibernate 查詢提示；code review 嚴格檢查 |
| 記憶體佔用過高 | 中 | 低 | JVM heap 調校（-Xmx512m）、使用 Projection 減少資料載入、分頁查詢 |
| 樂觀鎖定衝突頻繁 | 低 | 低 | 工時系統衝突機率低；前端提供清晰的衝突提示與重試機制 |
| Vue 3 生態元件不足 | 低 | 低 | Element Plus 提供完整企業級元件；必要時可混用 Headless UI |

---

## 10. 開發環境設定

### Java/Spring Boot 本地開發

**必要工具**：
- JDK 21 (推薦使用 Eclipse Temurin 或 Amazon Corretto)
- Maven 3.9+ 或 Gradle 8+
- IntelliJ IDEA Ultimate (社群版也可，但 Ultimate 提供更好的 Spring 支援)
- Docker Desktop (執行 PostgreSQL container)

**application.yml 配置**：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timesheet_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate  # 生產環境務必使用 validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  security:
    user:
      name: admin
      password: admin123

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.example.timesheet: DEBUG
    org.hibernate.SQL: DEBUG
```

### Vue 3 本地開發

**環境設定**：
```bash
# Node.js 20+
node --version  # v20.x.x

# pnpm (推薦) 或 npm
pnpm install

# 開發伺服器
pnpm dev  # http://localhost:5173
```

**vite.config.ts**：
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 結論

本研究確認採用 **JDK 21 + Spring Boot 3.2 + PostgreSQL + Vue 3** 技術棧，這是企業級工時管理系統的成熟穩定選擇，完全滿足以下需求：

✅ **99% 可用性**：Spring Boot 的成熟度與 Java 的穩定性保障  
✅ **500 並發用戶**：Virtual Threads 提供高效並發處理  
✅ **< 2s 回應時間**：JPA 優化 + 資料庫索引 + 分頁查詢  
✅ **強型別安全**：Java 編譯時檢查 + Vue 3 TypeScript  
✅ **完整測試支援**：JUnit 5 + TestContainers + Vitest + Playwright  
✅ **企業級維護性**：Spring 完整生態 + 豐富的開發人才市場

**下一步**：進入 Phase 1，基於此技術選型設計詳細的資料模型與 API 契約。
