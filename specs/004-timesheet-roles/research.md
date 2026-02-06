# Research Report: 報工系統角色權限管理

**Date**: 2026年2月6日  
**Feature**: 004-timesheet-roles  
**Status**: Completed

## Executive Summary

本報告針對報工系統角色權限管理功能的技術實現進行研究，涵蓋 Spring Security RBAC 實現、JDK 24 相容性、前後端整合模式、資料庫設計模式、業務邏輯實現等關鍵技術決策。所有研究任務已完成，技術方案已確定。

---

## Research Tasks

### 1. Spring Security 6.2+ RBAC Implementation

**Decision**: 使用 Spring Security 的方法級安全（Method Security）結合自訂權限評估器實現細粒度 RBAC

**Rationale**:
- Spring Security 6.2+ 提供了強大的 `@PreAuthorize` 和 `@PostAuthorize` 註解，支援 SpEL 表達式
- 方法級安全可以直接在 Service 或 Controller 層控制存取權限，無需在每個方法內手動檢查
- 自訂 `PermissionEvaluator` 可以實現複雜的業務規則檢查（例如：PM 只能管理被指派的專案）
- 使用 `SecurityContextHolder` 可以輕鬆獲取當前使用者的角色和身份資訊

**Implementation Approach**:
```java
// 配置方法級安全
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            PermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionExpressionHandler();
        handler.setPermissionEvaluator(customPermissionEvaluator);
        return handler;
    }
}

// 自訂權限評估器
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        // 實現業務規則檢查
        // 例如：檢查使用者是否為專案的 PM
    }
}

// 使用範例
@PreAuthorize("hasRole('PM') and hasPermission(#projectId, 'PROJECT', 'MANAGE')")
public void createTask(Long projectId, TaskDto taskDto) {
    // ...
}
```

**Alternatives Considered**:
- **URL-based security only**: 僅使用 URL 級別的權限控制，但這無法處理複雜的業務規則（如 PM 只能管理自己的專案）
- **Manual permission checks in code**: 在每個方法內手動檢查權限，但這會導致程式碼重複且難以維護
- **Aspect-Oriented Programming (AOP)**: 使用 AOP 實現橫切關注點，但 Spring Security 的方法級安全本質上就是基於 AOP，無需重複造輪

**References**:
- Spring Security 6.2 Documentation: Method Security
- Baeldung: Spring Method Security

---

### 2. JDK 24 與 Spring Boot 3.2+ 相容性

**Decision**: 使用 JDK 24 與 Spring Boot 3.3+ (最新穩定版)，並啟用 `--enable-preview` 以使用預覽功能

**Rationale**:
- Spring Boot 3.3+ 正式支援 JDK 21+ 的新特性（Virtual Threads, Pattern Matching, Record Patterns）
- JDK 24 是 JDK 21 LTS 之後的最新版本，Spring Framework 6.1+ 已經過測試並支援
- JDK 24 的 Virtual Threads (Project Loom) 可以顯著提升並行處理能力，適合處理高並行請求
- 需要注意某些第三方函式庫可能尚未完全支援 JDK 24，需要進行相容性測試

**Implementation Approach**:
```xml
<!-- pom.xml -->
<properties>
    <java.version>24</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
    <maven.compiler.source>24</maven.compiler.source>
    <maven.compiler.target>24</maven.compiler.target>
    <maven.compiler.release>24</maven.compiler.release>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>--enable-preview</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Alternatives Considered**:
- **JDK 21 LTS**: 更保守的選擇，長期支援，但缺少 JDK 24 的新特性
- **JDK 17 LTS**: 最穩定的選擇，但缺少 Virtual Threads 等現代特性

**Known Issues**:
- 部分 Hibernate/JPA 函式庫可能在 JDK 24 上有相容性問題，需要升級到最新版本
- Testcontainers 需要確保使用最新版本以支援 JDK 24

**References**:
- Spring Boot 3.3.0 Release Notes
- JDK 24 Release Notes
- Spring Framework 6.1 JDK 21+ Support

---

### 3. Vue 3 與 Spring Boot REST API 整合模式

**Decision**: 使用 JWT (JSON Web Token) 進行無狀態認證，前端在 HTTP Header 中攜帶 JWT，後端使用 Spring Security JWT Filter 驗證

**Rationale**:
- JWT 是前後端分離架構的標準認證方式，無狀態且易於擴展
- Spring Security 提供了完善的 JWT 支援（透過 `spring-security-oauth2-resource-server`）
- Vue 3 可以使用 Axios Interceptor 自動在每個請求中添加 JWT
- CORS 配置由 Spring Boot 統一管理，前端無需額外配置

**Implementation Approach**:

**Backend (Spring Boot)**:
```java
// Security Configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                .requestMatchers("/api/pm/**").hasRole("PM")
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/executive/**").hasRole("EXECUTIVE")
                .requestMatchers("/api/hr/**").hasRole("HR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Vite dev server
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Frontend (Vue 3)**:
```typescript
// api/client.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
});

// Request Interceptor: 添加 JWT
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: 處理 401 錯誤
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

**Alternatives Considered**:
- **Session-based authentication**: 傳統的 Session + Cookie 方式，但不適合前後端分離架構
- **OAuth 2.0 / OpenID Connect**: 提供更完整的授權框架，但對於內部系統過於複雜

**References**:
- Spring Security OAuth2 Resource Server Documentation
- JWT.io
- Vue 3 + Axios Best Practices

---

### 4. PostgreSQL 角色權限資料庫設計模式

**Decision**: 使用「使用者-角色」多對一關係（簡化版 RBAC），角色資訊直接儲存在 `users` 表的 `role` 欄位

**Rationale**:
- 規格明確指出每個使用者僅有一個角色，不支援多角色或動態角色指派
- 簡化的資料庫設計可以提升查詢效能，減少 JOIN 操作
- 使用 Enum 或 VARCHAR 儲存角色名稱，配合 JPA `@Enumerated` 註解實現類型安全
- 未來如需擴展為多角色系統，可以遷移到「使用者-角色」多對多關係表

**Database Schema**:
```sql
-- 使用者表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('EXECUTIVE', 'EMPLOYEE', 'PM', 'MANAGER', 'HR')),
    department_id BIGINT REFERENCES departments(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_department ON users(department_id);
CREATE INDEX idx_users_email ON users(email);

-- 角色權限矩陣在應用層或配置檔案中定義，不需要獨立的 permissions 表
```

**JPA Entity**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

public enum UserRole {
    EXECUTIVE,   // 管理層
    PM,          // 專案經理
    MANAGER,     // 部門主管
    EMPLOYEE,    // 執行人員
    HR           // 人力資源
}
```

**Alternatives Considered**:
- **多對多角色表**: 建立 `roles` 和 `user_roles` 表，支援多角色，但規格中明確不需要
- **權限表**: 建立 `permissions` 表儲存細粒度權限，但規格中角色權限是固定的，不需要動態配置
- **PostgreSQL Row-Level Security (RLS)**: 使用資料庫層級的權限控制，但會增加複雜度且不易測試

**References**:
- PostgreSQL Best Practices for Role-Based Access Control
- JPA Entity Design Patterns
- Spring Data JPA Auditing

---

### 5. 工時計算和「過去三個工作天」業務邏輯實現

**Decision**: 建立 `WorkdayCalculator` 工具類別，排除週末（六、日），使用 Java Time API 進行日期計算

**Rationale**:
- Java 8+ 的 `java.time` API 提供了強大的日期計算功能（`LocalDate`, `DayOfWeek`）
- 將工作天計算邏輯封裝在獨立的工具類別中，便於測試和重用
- 排除週末的邏輯簡單且效能高，無需外部函式庫
- 未來如需支援國定假日，可以擴展為查詢假日表

**Implementation Approach**:
```java
@Component
public class WorkdayCalculator {
    
    /**
     * 檢查指定日期是否在過去三個工作天內（不含今天）
     * 過去三個工作天的定義：排除週末（六、日）後倒數三天
     * 
     * 例如：今天是週三，過去三個工作天為：週二、週一、上週五
     *       今天是週一，過去三個工作天為：上週五、上週四、上週三
     */
    public boolean isWithinPastThreeWorkdays(LocalDate date) {
        LocalDate today = LocalDate.now();
        
        // 無法編輯今天或未來的日期
        if (date.isAfter(today) || date.isEqual(today)) {
            return false;
        }
        
        // 計算過去三個工作天的最早日期
        LocalDate earliestEditableDate = getEarliestEditableDate(today);
        
        // 檢查日期是否在範圍內且不是週末
        return !date.isBefore(earliestEditableDate) && !isWeekend(date);
    }
    
    private LocalDate getEarliestEditableDate(LocalDate today) {
        int workdaysToGoBack = 3;
        LocalDate date = today;
        
        while (workdaysToGoBack > 0) {
            date = date.minusDays(1);
            if (!isWeekend(date)) {
                workdaysToGoBack--;
            }
        }
        
        return date;
    }
    
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 計算兩個日期之間的工作天數（排除週末）
     */
    public long countWorkdaysBetween(LocalDate start, LocalDate end) {
        return Stream.iterate(start, date -> date.plusDays(1))
                     .limit(ChronoUnit.DAYS.between(start, end) + 1)
                     .filter(date -> !isWeekend(date))
                     .count();
    }
}

// 使用範例
@Service
public class TimesheetService {
    
    @Autowired
    private WorkdayCalculator workdayCalculator;
    
    public void submitTimesheet(LocalDate date, Long taskId, Double hours) {
        if (!workdayCalculator.isWithinPastThreeWorkdays(date)) {
            throw new BusinessException("僅能編輯過去三個工作天的工時記錄");
        }
        // ...
    }
}
```

**Test Coverage**:
```java
@SpringBootTest
class WorkdayCalculatorTest {
    
    @Autowired
    private WorkdayCalculator calculator;
    
    @Test
    void testIsWithinPastThreeWorkdays_Wednesday() {
        // 假設今天是週三 2026-02-11
        LocalDate today = LocalDate.of(2026, 2, 11);
        
        // 過去三個工作天：週二 2/10, 週一 2/9, 上週五 2/6
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 10))); // 週二
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 9)));  // 週一
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 6)));  // 上週五
        
        // 上週四及更早的日期不在範圍內
        assertFalse(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 5))); // 上週四
        
        // 今天和未來的日期不能編輯
        assertFalse(calculator.isWithinPastThreeWorkdays(today));
        assertFalse(calculator.isWithinPastThreeWorkdays(today.plusDays(1)));
    }
    
    @Test
    void testIsWithinPastThreeWorkdays_Monday() {
        // 假設今天是週一 2026-02-09
        LocalDate today = LocalDate.of(2026, 2, 9);
        
        // 過去三個工作天：上週五 2/6, 上週四 2/5, 上週三 2/4
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 6)));  // 上週五
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 5)));  // 上週四
        assertTrue(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 4)));  // 上週三
        
        // 上週二及更早的日期不在範圍內
        assertFalse(calculator.isWithinPastThreeWorkdays(LocalDate.of(2026, 2, 3))); // 上週二
    }
}
```

**Alternatives Considered**:
- **Joda-Time**: 舊版 Java 日期函式庫，已被 Java 8+ Time API 取代
- **外部假日 API**: 整合第三方假日資料庫，但規格中明確不考慮國定假日
- **資料庫計算**: 在 SQL 中實現工作天計算，但會降低可測試性和可維護性

**References**:
- Java Time API Documentation
- Baeldung: Working with Java LocalDate

---

### 6. 通知系統實現方式

**Decision**: 使用資料庫輪詢 + 定期查詢實現簡易通知系統，不使用 WebSocket 或 Server-Sent Events (SSE)

**Rationale**:
- 規格中要求「即時通知」，但考慮到系統規模（50-200 使用者）和使用頻率，不需要真正的即時推送
- 使用資料庫儲存通知記錄，前端每 30 秒輪詢一次未讀通知，技術簡單且可靠
- 避免引入 WebSocket 的複雜性（連接管理、狀態同步、load balancing）
- 如果未來需要真正的即時推送，可以升級為 SSE 或 WebSocket

**Database Schema**:
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,  -- TASK_ASSIGNED, HOURS_REQUEST_APPROVED, PROJECT_PM_CHANGED, etc.
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),  -- PROJECT, TASK, HOURS_REQUEST
    related_entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);
```

**Implementation Approach**:

**Backend (Spring Boot)**:
```java
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public void notifyTaskAssigned(User employee, Task task) {
        Notification notification = Notification.builder()
            .user(employee)
            .type(NotificationType.TASK_ASSIGNED)
            .title("新任務指派：" + task.getName())
            .message(String.format("PM %s 指派了一個新任務給你", task.getProject().getPm().getName()))
            .relatedEntityType("TASK")
            .relatedEntityId(task.getId())
            .build();
        notificationRepository.save(notification);
    }
    
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException("通知不存在"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("無權存取此通知");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

**Frontend (Vue 3)**:
```typescript
// stores/notification.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { fetchUnreadNotifications, markNotificationAsRead } from '@/api/notifications';

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([]);
  const pollingInterval = ref<number | null>(null);

  const unreadCount = computed(() => notifications.value.length);

  const startPolling = () => {
    if (pollingInterval.value) return;
    
    // 立即獲取一次
    fetchNotifications();
    
    // 每 30 秒輪詢一次
    pollingInterval.value = setInterval(() => {
      fetchNotifications();
    }, 30000);
  };

  const stopPolling = () => {
    if (pollingInterval.value) {
      clearInterval(pollingInterval.value);
      pollingInterval.value = null;
    }
  };

  const fetchNotifications = async () => {
    try {
      const data = await fetchUnreadNotifications();
      notifications.value = data;
    } catch (error) {
      console.error('獲取通知失敗', error);
    }
  };

  const markAsRead = async (id: number) => {
    try {
      await markNotificationAsRead(id);
      notifications.value = notifications.value.filter(n => n.id !== id);
    } catch (error) {
      console.error('標記通知為已讀失敗', error);
    }
  };

  return {
    notifications,
    unreadCount,
    startPolling,
    stopPolling,
    markAsRead,
  };
});

// App.vue (or layout component)
import { useNotificationStore } from '@/stores/notification';
import { onMounted, onUnmounted } from 'vue';

const notificationStore = useNotificationStore();

onMounted(() => {
  notificationStore.startPolling();
});

onUnmounted(() => {
  notificationStore.stopPolling();
});
```

**Alternatives Considered**:
- **WebSocket**: 真正的雙向即時通訊，但增加系統複雜度（需要 WebSocket Gateway、連接管理、狀態同步）
- **Server-Sent Events (SSE)**: 單向即時推送，比 WebSocket 簡單，但仍需要處理連接管理
- **第三方推送服務**: 如 Firebase Cloud Messaging，但對於內部系統過於複雜

**References**:
- Spring Boot + Polling Pattern
- Vue 3 Composition API + Polling Best Practices

---

### 7. 稽核日誌實現模式

**Decision**: 使用 Spring Data JPA Auditing + AOP (Aspect-Oriented Programming) 實現自動化稽核日誌記錄

**Rationale**:
- Spring Data JPA 提供了 `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` 註解，自動記錄建立和修改時間/人
- 使用 AOP 可以在不侵入業務邏輯的情況下自動記錄關鍵操作（建立、修改、刪除）
- 稽核日誌儲存在獨立的 `audit_logs` 表中，避免污染業務表
- 記錄完整的變更內容（JSON 格式），便於追溯和稽核

**Database Schema**:
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE
    entity_type VARCHAR(50) NOT NULL,  -- PROJECT, TASK, TIMESHEET_ENTRY
    entity_id BIGINT NOT NULL,
    change_details JSONB,  -- 儲存變更內容 (舊值 -> 新值)
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id, created_at DESC);
```

**Implementation Approach**:

**Enable JPA Auditing**:
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return Optional.of(principal.getId());
        };
    }
}

// Base Entity with Auditing
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
```

**AOP for Detailed Audit Logging**:
```java
@Aspect
@Component
public class AuditLoggingAspect {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void logAuditedMethod(JoinPoint joinPoint, Audited audited, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        AuditLog log = AuditLog.builder()
            .userId(principal.getId())
            .action(audited.action())
            .entityType(audited.entityType())
            .entityId(extractEntityId(result))
            .changeDetails(serializeChangeDetails(joinPoint.getArgs()))
            .ipAddress(request.getRemoteAddr())
            .userAgent(request.getHeader("User-Agent"))
            .build();
        
        auditLogRepository.save(log);
    }
    
    private Long extractEntityId(Object result) {
        // 從返回結果中提取 entity ID
        if (result instanceof BaseEntity) {
            return ((BaseEntity) result).getId();
        }
        return null;
    }
    
    private String serializeChangeDetails(Object[] args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}

// Custom Annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
    String entityType();
}

// Usage Example
@Service
public class ProjectService {
    
    @Audited(action = "CREATE", entityType = "PROJECT")
    public ProjectDto createProject(CreateProjectRequest request) {
        // ...
    }
    
    @Audited(action = "UPDATE", entityType = "PROJECT")
    public ProjectDto updateProject(Long projectId, UpdateProjectRequest request) {
        // ...
    }
}
```

**Alternatives Considered**:
- **Hibernate Envers**: JPA 的稽核框架，自動追蹤實體變更歷史，但會為每個表建立對應的 `_AUD` 表，增加資料庫複雜度
- **Manual Logging**: 在每個業務方法中手動記錄日誌，但會導致程式碼重複且容易遺漏
- **Database Triggers**: 使用資料庫觸發器記錄變更，但會增加資料庫負擔且難以測試

**References**:
- Spring Data JPA Auditing Documentation
- Spring AOP Documentation
- Baeldung: Spring Boot Auditing

---

## Technology Stack Summary

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 24 |
| Framework | Spring Boot | 3.3+ |
| Security | Spring Security | 6.2+ |
| Data Access | Spring Data JPA | 3.2+ |
| Database | PostgreSQL | 16+ |
| Testing | JUnit 5, Mockito, Testcontainers | Latest |
| Frontend | Vue 3 | 3.x |
| Build Tool | Maven | 3.9+ |
| API Documentation | OpenAPI 3.0 | - |

---

## Next Steps

Phase 0 研究已完成，所有關鍵技術決策已確定。下一步進入 Phase 1：

1. **生成 data-model.md**: 詳細定義所有實體的欄位、關聯和驗證規則
2. **生成 API contracts**: 撰寫 OpenAPI 3.0 規格，定義所有 REST API 端點
3. **生成 quickstart.md**: 提供開發人員快速開始指南
4. **更新 agent context**: 將新技術決策加入 AI agent 上下文

---

**Review Date**: 2026年2月6日  
**Status**: Approved for Phase 1
