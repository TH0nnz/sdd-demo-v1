package com.example.timesheet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration.
 * 
 * Provides interactive API documentation at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * Includes:
 * - JWT Bearer authentication scheme
 * - API metadata (version, description, contact)
 * - Server URLs for different environments
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.name:工時管理系統}")
    private String appName;
    
    @Value("${app.description:企業內部工時管理系統，支援專案與任務的時數分配、工時填報、進度監控與報表查詢。}")
    private String appDescription;
    
    @Bean
    public OpenAPI customOpenAPI() {
        // Define JWT Security Scheme
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title(appName + " API")
                        .version(appVersion)
                        .description(appDescription + "\n\n" +
                                "### 認證方式\n" +
                                "使用 JWT Bearer Token 認證。\n" +
                                "在 Authorization header 中添加: `Bearer {token}`\n\n" +
                                "### 角色權限\n" +
                                "- **MANAGER (管理層)**: 創建/修改/關閉專案，審批時數申請\n" +
                                "- **PM (專案經理)**: 創建/修改任務，申請額外時數\n" +
                                "- **DEPT_HEAD (部門主管)**: 查詢部門成員工時報表\n" +
                                "- **EXECUTIVE (執行人員)**: 填報工時，標記任務完成\n" +
                                "- **HR (人力資源)**: 管理用戶，分配角色\n\n" +
                                "### 測試帳號\n" +
                                "- HR: hr.wang@company.com / password123\n" +
                                "- Manager: manager.zhang@company.com / password123\n" +
                                "- PM: pm.wu@company.com / password123\n" +
                                "- Executive: exec.zhao@company.com / password123\n" +
                                "- Dept Head: depthead.lin@company.com / password123")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@company.com"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https://company.com/license")))
                
                // Server URLs
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api-staging.company.com")
                                .description("Staging Server"),
                        new Server()
                                .url("https://api.company.com")
                                .description("Production Server")
                ))
                
                // Security Scheme (JWT Bearer)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("請輸入 JWT token（無需 'Bearer ' 前綴）")))
                
                // Apply security globally
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
