# SDD Demo V1 Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-02-06

## Active Technologies

- JDK 24 + Spring Boot 4.0.2 + PostgreSQL 18.1 (004-timesheet-roles)
- Vue 3 + TypeScript (004-timesheet-roles)
- Spring Security 7.0.2 + Spring Data JPA 4.0 + JWT (004-timesheet-roles)

## Project Structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── timesheet/
│   │   │               ├── config/           # Spring Security 配置, CORS 配置
│   │   │               ├── security/         # 角色權限定義, JWT 處理, UserDetailsService
│   │   │               ├── model/            # JPA Entities
│   │   │               ├── repository/       # Spring Data JPA Repositories
│   │   │               ├── service/          # Business logic
│   │   │               ├── controller/       # REST API Controllers
│   │   │               ├── dto/              # Request/Response DTOs
│   │   │               ├── exception/        # Custom exceptions
│   │   │               └── util/             # Utilities
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/                # Flyway migration scripts
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── timesheet/
│                       ├── controller/       # API Integration Tests
│                       ├── service/          # Service Unit Tests
│                       └── repository/       # Repository Tests
├── pom.xml
└── Dockerfile

frontend/
├── src/
│   ├── api/                  # API client modules
│   ├── components/           # Vue components
│   ├── views/                # Page-level Vue components
│   ├── router/               # Vue Router 配置
│   ├── stores/               # Pinia stores
│   ├── types/                # TypeScript 類型定義
│   └── utils/                # Utility functions
├── tests/
│   ├── unit/                 # Vue component unit tests
│   └── e2e/                  # E2E tests
├── package.json
└── vite.config.ts

database/
├── migrations/               # Database schema migrations
└── seeds/                    # Seed data
```

## Commands

```bash
# Backend (Spring Boot + JDK 24)
cd backend
mvn clean test                    # Run all tests
mvn clean package                 # Build JAR
mvn spring-boot:run               # Start dev server
mvn checkstyle:check              # Code style check
java --enable-preview -jar target/timesheet-backend-1.0.0.jar  # Run with JDK 24 preview features

# Database (PostgreSQL + Flyway)
mvn flyway:migrate                # Run database migrations
mvn flyway:info                   # Check migration status
psql -U postgres -d timesheet_db  # Connect to database

# Frontend (Vue 3 + TypeScript)
cd frontend
npm install                       # Install dependencies
npm run dev                       # Start dev server (Vite)
npm run build                     # Build for production
npm run test:unit                 # Run unit tests
npm run test:e2e                  # Run E2E tests
npm run lint                      # ESLint check

# Docker Compose
docker-compose up -d              # Start all services
docker-compose down               # Stop all services
docker-compose logs -f backend    # View backend logs
```

## Code Style

### Java (JDK 24)
- Follow Java Code Conventions and Spring Boot Best Practices
- Use Checkstyle configuration in `backend/checkstyle.xml`
- Enable JDK 24 preview features with `--enable-preview`
- Keep cyclomatic complexity ≤ 10 (NFR-016)
- Use meaningful variable and method names
- Add javadoc for public APIs
- Use `@PreAuthorize` for role-based access control
- Prefer constructor injection over field injection

### TypeScript/Vue 3
- Follow Vue 3 Composition API style guide
- Use TypeScript strict mode
- Use ESLint and Prettier for code formatting
- Prefer `<script setup>` syntax in Vue components
- Use Pinia for state management
- Keep components small and focused (Single Responsibility)

### SQL (PostgreSQL)
- Use Flyway for schema migrations
- Name migration files: `V{version}__{description}.sql`
- Use lowercase with underscores for table/column names
- Always add indexes for foreign keys
- Document complex queries with comments

### Testing
- Minimum 80% code coverage (NON-NEGOTIABLE)
- 100% coverage for time calculation logic
- Write tests before implementation (Test-First)
- Use meaningful test names: `shouldDoSomething_whenCondition_thenExpectedResult`
- Use `@SpringBootTest` for integration tests
- Use Testcontainers for database tests

## Recent Changes

- 004-timesheet-roles: Added JDK 24 + Spring Boot 4.0.2 + Spring Security 7.0.2 + Spring Data JPA 4.0 + PostgreSQL 18.1 + Vue 3 (role-based access control, JWT authentication, fine-grained permissions)

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
