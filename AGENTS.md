# AGENTS.md - Coding Agent Guidelines

## Project Overview

配置历史管理模块 - Configuration history management system with Spring Boot backend and Vue 3 frontend.

## Tech Stack

### Backend
- Java 17, Spring Boot 3.2.0, MyBatis-Plus 3.5.7
- H2 (dev) / MySQL (prod), SpringDoc OpenAPI
- Lombok for boilerplate reduction

### Frontend
- Vue 3 + TypeScript + Vite
- Ant Design Vue, Monaco Editor, Axios, Pinia

---

## Build Commands

### Backend (Maven)

```bash
# Build (skip tests)
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Run tests
mvn test

# Run single test class
mvn test -Dtest=ConfigServiceTest

# Run single test method
mvn test -Dtest=ConfigServiceTest#testCreateAndGetConfig

# Start application (development - recommended)
mvn spring-boot:run

# Start application (production)
java -jar target/config-history-1.0.0.jar
```

### Frontend (npm)

```bash
cd frontend

# Install dependencies
npm install

# Development server (port 3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

---

## Project Structure

```
config-history/
├── src/main/java/com/example/config/
│   ├── controller/          # REST controllers
│   ├── service/             # Service interfaces & implementations
│   ├── mapper/              # MyBatis-Plus mappers
│   ├── entity/              # JPA/MyBatis entities
│   ├── dto/                 # Data Transfer Objects
│   ├── common/              # Common utilities (Result, PageResult, exceptions)
│   └── history/             # Independent history module (can be extracted)
│       ├── annotation/      # @HistoryTrack, @HistoryField
│       ├── aspect/          # AOP for history tracking
│       ├── service/         # History services
│       └── controller/      # History REST API
├── src/main/resources/
│   ├── application.yml      # Main config (H2 by default)
│   └── mapper/              # MyBatis XML files
├── frontend/
│   ├── src/
│   │   ├── api/             # Axios API calls
│   │   ├── components/      # Vue components
│   │   ├── views/           # Page views
│   │   ├── types/           # TypeScript interfaces
│   │   └── stores/          # Pinia stores
│   └── package.json
└── sql/init.sql             # Database initialization
```

---

## Backend Code Style

### Imports
- Import specific classes, avoid wildcard imports
- Order: java.* → javax.* → third-party → project packages

### Entities
```java
@Data                                    // Lombok
@TableName("table_name")                 // MyBatis-Plus
@HistoryTrack(entityName = "...")       // For history tracking
public class Entity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @HistoryField(displayName = "Field")
    private String field;
    
    @TableLogic
    private Integer deleted;             // Soft delete: 0=active, 1=deleted
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
```

### Services
```java
@Service
@RequiredArgsConstructor                  // Constructor injection via Lombok
public class ServiceImpl implements Service {
    private final SomeMapper someMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)  // Always rollback on exception
    public void method() { ... }
}
```

### Controllers
```java
@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class Controller {
    private final Service service;
    
    @GetMapping("/{id}")
    public Result<DTO> getById(@PathVariable Long id) { ... }
}
```

### DTOs
```java
@Data
@Builder                                 // Use for response DTOs
public class ResponseDTO {
    private Long id;
    private String name;
}
```

### Error Handling
- Throw `RuntimeException` with Chinese message for business errors
- Use `GlobalExceptionHandler` for consistent error responses
- Never catch and swallow exceptions silently

### Naming Conventions
- Classes: PascalCase (ConfigService, ConfigItem)
- Methods: camelCase (getConfigById, createConfig)
- Constants: UPPER_SNAKE_CASE
- Database: snake_case (config_item, config_history)
- API paths: kebab-case or lowercase (/api/v1/configs)

---

## Frontend Code Style

### TypeScript
- Strict mode enabled: `noUnusedLocals`, `noUnusedParameters`
- Use interfaces for types, not type aliases
- Path alias: `@/*` maps to `src/*`

### API Layer
```typescript
// src/api/resource.ts
import { get, post, put, del } from './request'
import type { PageResult } from './request'
import type { Item, Request } from '@/types'

const BASE = '/resource'

export const resourceApi = {
  list: (page = 1): Promise<PageResult<Item>> => 
    get(BASE, { params: { page } }),
  
  get: (id: number): Promise<Item> => 
    get(`${BASE}/${id}`),
  
  create: (data: Request): Promise<Item> => 
    post(BASE, data),
}
```

### Vue Components
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { PropType } from 'vue'

const props = defineProps<{
  id: number
  title?: string
}>()

const emit = defineEmits<{
  (e: 'change', value: string): void
}>()

const loading = ref(false)

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="component">
    <!-- Template content -->
  </div>
</template>

<style scoped>
/* Scoped styles only */
</style>
```

### CSS Conventions
- Use scoped styles in Vue components
- Class names: kebab-case
- Use Ant Design Vue's design tokens when possible

---

## Testing Guidelines

### Backend Tests
- Use `@SpringBootTest` for integration tests
- Use JUnit 5 assertions: `assertNotNull()`, `assertEquals()`
- Test class naming: `*Test.java`

### Running Tests
```bash
# All tests
mvn test

# Single test class
mvn test -Dtest=ConfigServiceTest

# Single test method
mvn test -Dtest=ConfigServiceTest#testCreateAndGetConfig
```

---

## API Conventions

### Request Format
- GET: Query parameters
- POST/PUT: JSON body
- DELETE: Path parameter

### Response Format
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### Pagination
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

---

## Key Files

| File | Purpose |
|------|---------|
| `application.yml` | Main config, H2 by default, MySQL for prod |
| `pom.xml` | Maven dependencies |
| `frontend/package.json` | Frontend dependencies |
| `frontend/tsconfig.app.json` | TypeScript strict config |
| `frontend/vite.config.ts` | Vite build config |

---

## Development URLs

| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Frontend Dev | http://localhost:3000 |

---

## Common Patterns

### Adding a new entity
1. Create entity in `entity/` with `@Data`, `@TableName`, `@HistoryTrack`
2. Create mapper in `mapper/` extending `BaseMapper<Entity>`
3. Create DTOs in `dto/`
4. Create service interface and implementation
5. Create controller with REST endpoints
6. Add history tracking with `@HistoryField` annotations

### Adding a new frontend feature
1. Create TypeScript types in `types/index.ts`
2. Create API functions in `api/`
3. Create component in `components/`
4. Add route in `router/index.ts` if needed