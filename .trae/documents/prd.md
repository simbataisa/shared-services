# Shared Services Permission Management System - Development Specification

## 1. PROJECT OVERVIEW

### 1.1 System Purpose

Build a comprehensive multi-tenant permission management system (AHSS - Advanced Healthcare Security System) that manages users, roles, permissions, and products across multiple organizations with a hierarchical Product → Module → Role architecture.

### 1.2 Developer Information

- **Developer Name**: Dennis
- **Development System**: macOS (Intel)
- **Communication Language**: English

### 1.3 Key Business Requirements

- Multi-tenant architecture supporting BUSINESS_IN, BUSINESS_OUT, and INDIVIDUAL tenant types
- Hierarchical permission model: Products contain Modules, Modules contain Roles
- User groups (permission groups) with team-based role assignments
- Support for subscription-based, quota-based, and affiliate plans
- SSO integration with OAuth2 providers
- Comprehensive audit logging for all operations

---

## 2. TECHNOLOGY STACK

### 2.1 Frontend Stack

```yaml
Framework: React 18+ with TypeScript 5+
Build Tool: Vite 5+
UI Components: Shadcn/UI (with Radix UI primitives)
Styling: TailwindCSS 3+
Routing: React Router DOM 6+
State Management: Zustand 4+
HTTP Client: Axios
Authentication: JWT + OAuth2
Form Handling: React Hook Form + Zod validation
Icons: Lucide React
Date Handling: date-fns
```

### 2.2 Backend Stack

```yaml
Java Version: 21 (LTS)
Framework: Spring Boot 3.3+
Security: Spring Security 6+ with JWT
Authentication Server: Spring Authorization Server
Build Tool: Gradle 8+ (preferred) or Maven 3.9+
Architecture: Layered (Controller → Service → Repository → Model)

Key Dependencies:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Authorization Server
  - Spring Boot Actuator
  - Spring Cloud Gateway
  - Spring Cache (Redis)
  - Resilience4j
  - Flyway
  - Lombok
  - MapStruct
```

### 2.3 Testing Stack

```yaml
Unit Testing: JUnit 5 + Mockito + AssertJ
Integration Testing: TestContainers (PostgreSQL, Redis)
API Testing: Karate BDD
Performance Testing: Gatling
Test Reporting: Allure Report
Code Coverage: JaCoCo (minimum 80%)
Code Quality: SonarQube (Quality Gate must pass)
```

### 2.4 Infrastructure

```yaml
Database: PostgreSQL 16+
Cache: Redis 7+
API Gateway: Spring Cloud Gateway
  - Rate Limiting (Redis backend)
  - Request/Response logging
  - Circuit breaker integration
Resilience: Resilience4j
  - Circuit Breaker
  - Retry
  - Rate Limiter
  - Bulkhead
Monitoring:
  - Prometheus (metrics)
  - Grafana (dashboards)
  - Spring Boot Actuator (health checks)
Logging: SLF4J + Logback
  - JSON structured logging
  - Correlation IDs
  - Log aggregation ready
Documentation: Swagger/OpenAPI 3.0
Email: Resend API integration
Containerization:
  - Docker
  - Docker Compose (local dev)
  - Kubernetes (production)
```

---

## 3. DATABASE SCHEMA

### 3.1 Core Tables

```sql
-- Enums
CREATE TYPE tenant_type AS ENUM ('BUSINESS_IN', 'BUSINESS_OUT', 'INDIVIDUAL');
CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DISCONTINUED');
CREATE TYPE module_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED', 'DISCONTINUED');
CREATE TYPE role_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'DEPRECATED');
CREATE TYPE plan_type AS ENUM ('SUBSCRIPTION', 'QUOTABASED', 'AFFILIATE');
CREATE TYPE plan_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED', 'DISCONTINUED', 'PENDING_PAYMENT', 'PENDING_RENEW', 'OVERDUE');

-- Tenant Management
CREATE TABLE tenant (
    tenant_id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    name TEXT NOT NULL,
    type tenant_type NOT NULL,
    organization_id BIGINT REFERENCES organization(org_id),
    tenant_status tenant_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE organization (
    org_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    name TEXT NOT NULL,
    parent_org_id BIGINT REFERENCES organization(org_id),
    country VARCHAR(3),
    path TEXT NOT NULL, -- Materialized path for hierarchy
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- Entity (User) Management
CREATE TABLE entity (
    entity_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    parent_entity_id BIGINT REFERENCES entity(entity_id),
    path TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE tenant_entity (
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    entity_id BIGINT REFERENCES entity(entity_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    PRIMARY KEY (tenant_id, entity_id)
);

-- Product & Module Management
CREATE TABLE product (
    product_id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    product_name TEXT NOT NULL,
    description TEXT,
    product_status product_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE module (
    module_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(product_id),
    name TEXT NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    module_status module_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- Role & Permission Management
CREATE TABLE role (
    role_id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    name TEXT NOT NULL,
    description TEXT,
    role_status role_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    UNIQUE(module_id, name)
);

CREATE TABLE permission (
    permission_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    resource_type TEXT,
    action TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE role_permission (
    role_id BIGINT REFERENCES role(role_id),
    permission_id BIGINT REFERENCES permission(permission_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- User Group (Permission Group) Management
CREATE TABLE user_group (
    user_group_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    deleted_at TIMESTAMP
);

CREATE TABLE user_group_member (
    user_group_id BIGINT REFERENCES user_group(user_group_id),
    entity_id BIGINT REFERENCES entity(entity_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_group_id, entity_id)
);

CREATE TABLE group_module_role (
    group_module_role_id BIGSERIAL PRIMARY KEY,
    user_group_id BIGINT NOT NULL REFERENCES user_group(user_group_id),
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    role_id BIGINT NOT NULL REFERENCES role(role_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    UNIQUE(user_group_id, module_id, role_id)
);

-- Profile (User-Role Assignment)
CREATE TABLE profile (
    profile_id BIGSERIAL PRIMARY KEY,
    entity_id BIGINT NOT NULL REFERENCES entity(entity_id),
    role_id BIGINT NOT NULL REFERENCES role(role_id),
    username TEXT NOT NULL,
    username_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- SSO Provider
CREATE TABLE sso_provider (
    sso_provider_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    client_id TEXT NOT NULL,
    client_secret TEXT NOT NULL,
    discovery_url TEXT NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenant(tenant_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- Plan & Package Management
CREATE TABLE plan (
    plan_id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    discount_rate DECIMAL(5,2),
    start_date DATE NOT NULL,
    end_date DATE,
    plan_type plan_type NOT NULL,
    plan_status plan_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE package (
    package_id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plan(plan_id),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    package_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE package_module (
    package_module_id BIGSERIAL PRIMARY KEY,
    package_id BIGINT NOT NULL REFERENCES package(package_id),
    module_id BIGINT NOT NULL REFERENCES module(module_id),
    price DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE tenant_plan (
    tenant_id BIGINT REFERENCES tenant(tenant_id),
    plan_id BIGINT REFERENCES plan(plan_id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    PRIMARY KEY (tenant_id, plan_id)
);
```

### 3.2 Indexes

```sql
-- Performance indexes
CREATE INDEX idx_tenant_code ON tenant(tenant_code);
CREATE INDEX idx_tenant_status ON tenant(tenant_status);
CREATE INDEX idx_organization_tenant ON organization(tenant_id);
CREATE INDEX idx_organization_parent ON organization(parent_org_id);
CREATE INDEX idx_entity_path ON entity USING GIST(path gist_trgm_ops);
CREATE INDEX idx_module_product ON module(product_id);
CREATE INDEX idx_role_module ON role(module_id);
CREATE INDEX idx_user_group_deleted ON user_group(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_group_module_role_group ON group_module_role(user_group_id);
CREATE INDEX idx_group_module_role_module ON group_module_role(module_id);
```

---

## 4. BACKEND API SPECIFICATIONS

### 4.1 RESTful API Design Principles

- Use proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Resource-oriented URLs
- Consistent response format
- Proper HTTP status codes
- API versioning via URL path (e.g., `/api/v1/`)
- Pagination for list endpoints (page, size, sort)
- Filter and search capabilities
- HATEOAS links where appropriate

### 4.2 Response Format

```json
{
  "success": true,
  "data": {},
  "message": "Operation successful",
  "timestamp": "2025-10-05T12:00:00Z",
  "path": "/api/v1/user-groups"
}

// Error Response
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": [
      {
        "field": "name",
        "message": "Name is required"
      }
    ]
  },
  "timestamp": "2025-10-05T12:00:00Z",
  "path": "/api/v1/user-groups"
}
```

### 4.3 Core API Endpoints

#### Permission Group Management

```yaml
POST   /api/v1/user-groups                    # Create permission group
GET    /api/v1/user-groups                    # List all permission groups (paginated)
GET    /api/v1/user-groups/{id}               # Get permission group by ID
PUT    /api/v1/user-groups/{id}               # Update permission group
DELETE /api/v1/user-groups/{id}               # Soft delete permission group
POST   /api/v1/user-groups/{id}/members       # Add members to group
DELETE /api/v1/user-groups/{id}/members/{entityId}  # Remove member
POST   /api/v1/user-groups/{id}/roles         # Assign module roles to group
DELETE /api/v1/user-groups/{id}/roles/{roleId}  # Remove role assignment
GET    /api/v1/user-groups/{id}/permissions   # Get effective permissions
```

#### Product & Module Management

```yaml
POST   /api/v1/products                       # Create product
GET    /api/v1/products                       # List all products
GET    /api/v1/products/{id}                  # Get product by ID
PUT    /api/v1/products/{id}                  # Update product
GET    /api/v1/products/{id}/modules          # Get all modules for product
POST   /api/v1/modules                        # Create module
GET    /api/v1/modules/{id}                   # Get module by ID
PUT    /api/v1/modules/{id}                   # Update module
GET    /api/v1/modules/{id}/roles             # Get all roles for module
```

#### Role & Permission Management

```yaml
POST   /api/v1/roles                          # Create role
GET    /api/v1/roles                          # List all roles
GET    /api/v1/roles/{id}                     # Get role by ID
PUT    /api/v1/roles/{id}                     # Update role
POST   /api/v1/roles/{id}/permissions         # Assign permissions to role
DELETE /api/v1/roles/{id}/permissions/{permissionId}  # Remove permission
GET    /api/v1/permissions                    # List all permissions
POST   /api/v1/permissions                    # Create permission
```

#### Tenant Management

```yaml
POST   /api/v1/tenants                        # Create tenant
GET    /api/v1/tenants                        # List all tenants
GET    /api/v1/tenants/{id}                   # Get tenant by ID
PUT    /api/v1/tenants/{id}                   # Update tenant
PATCH  /api/v1/tenants/{id}/status            # Update tenant status
GET    /api/v1/tenants/{id}/organizations     # Get tenant organizations
```

#### Authentication & Authorization

```yaml
POST   /api/v1/auth/login                     # User login
POST   /api/v1/auth/logout                    # User logout
POST   /api/v1/auth/refresh                   # Refresh token
GET    /api/v1/auth/me                        # Get current user info
POST   /api/v1/auth/oauth2/{provider}         # OAuth2 login
GET    /api/v1/auth/oauth2/callback           # OAuth2 callback
```

### 4.4 Request/Response Examples

#### Create Permission Group

```http
POST /api/v1/user-groups
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "name": "Benefit Configurator Member",
  "description": "Group for benefit configuration team",
  "teams": ["AIA Benefit Configurator"],
  "members": [123, 456, 789],
  "moduleRoles": [
    {
      "moduleId": 1,
      "roleIds": [1, 2]
    },
    {
      "moduleId": 2,
      "roleIds": [5]
    }
  ]
}

Response: 201 Created
{
  "success": true,
  "data": {
    "userGroupId": 100,
    "name": "Benefit Configurator Member",
    "description": "Group for benefit configuration team",
    "teams": ["AIA Benefit Configurator"],
    "memberCount": 3,
    "moduleRoleCount": 3,
    "createdAt": "2025-10-05T12:00:00Z"
  },
  "message": "Permission group created successfully"
}
```

---

## 5. BACKEND ARCHITECTURE

### 5.1 Layer Structure

```
src/main/java/com/ahss/
├── config/                    # Configuration classes
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
├── controller/                # REST Controllers
│   ├── UserGroupController.java
│   ├── ProductController.java
│   ├── RoleController.java
│   └── AuthController.java
├── dto/                       # Data Transfer Objects
│   ├── request/
│   │   ├── CreateUserGroupRequest.java
│   │   └── UpdateUserGroupRequest.java
│   └── response/
│       ├── UserGroupResponse.java
│       └── ApiResponse.java
├── entity/                    # JPA Entities
│   ├── UserGroup.java
│   ├── Product.java
│   ├── Module.java
│   └── Role.java
├── repository/                # JPA Repositories
│   ├── UserGroupRepository.java
│   ├── ProductRepository.java
│   └── RoleRepository.java
├── service/                   # Business Logic
│   ├── UserGroupService.java
│   ├── ProductService.java
│   └── impl/
│       ├── UserGroupServiceImpl.java
│       └── ProductServiceImpl.java
├── security/                  # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── exception/                 # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ValidationException.java
├── mapper/                    # MapStruct mappers
│   ├── UserGroupMapper.java
│   └── ProductMapper.java
└── util/                      # Utility classes
    ├── DateUtil.java
    └── SecurityUtil.java
```

### 5.2 Required Annotations & Best Practices

#### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/user-groups")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Groups", description = "Permission group management APIs")
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_GROUP_CREATE')")
    @Operation(summary = "Create new permission group")
    public ResponseEntity<ApiResponse<UserGroupResponse>> create(
            @Valid @RequestBody CreateUserGroupRequest request) {
        // Implementation
    }
}
```

#### Service Layer

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;

    @Transactional
    @Cacheable(value = "userGroups", key = "#id")
    public UserGroupResponse create(CreateUserGroupRequest request) {
        // Implementation with proper logging
        log.info("Creating user group: {}", request.getName());
        // Business logic
    }
}
```

#### Repository Layer

```java
@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>,
                                             JpaSpecificationExecutor<UserGroup> {

    Optional<UserGroup> findByNameAndDeletedAtIsNull(String name);

    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.members WHERE ug.id = :id")
    Optional<UserGroup> findByIdWithMembers(@Param("id") Long id);

    Page<UserGroup> findAllByDeletedAtIsNull(Pageable pageable);
}
```

### 5.3 Exception Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        // Handle validation errors
    }
}
```

---

## 6. FRONTEND ARCHITECTURE

### 6.1 Project Structure

```
src/
├── assets/                    # Static assets
├── components/                # Reusable components
│   ├── ui/                   # Shadcn/UI components
│   ├── layout/               # Layout components
│   │   ├── Header.tsx
│   │   ├── Sidebar.tsx
│   │   └── Layout.tsx
│   └── common/               # Common components
│       ├── DataTable.tsx
│       ├── SearchInput.tsx
│       └── ConfirmDialog.tsx
├── features/                  # Feature modules
│   ├── auth/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── stores/
│   ├── user-groups/
│   │   ├── components/
│   │   │   ├── UserGroupList.tsx
│   │   │   ├── CreateUserGroup.tsx
│   │   │   └── UserGroupDetail.tsx
│   │   ├── hooks/
│   │   │   └── useUserGroups.ts
│   │   ├── services/
│   │   │   └── userGroupService.ts
│   │   └── stores/
│   │       └── userGroupStore.ts
│   └── products/
├── hooks/                     # Global hooks
│   ├── useAuth.ts
│   ├── usePermissions.ts
│   └── useToast.ts
├── lib/                       # Utilities
│   ├── api.ts                # Axios configuration
│   ├── utils.ts              # Helper functions
│   └── constants.ts          # Constants
├── routes/                    # Route definitions
│   ├── index.tsx
│   ├── ProtectedRoute.tsx
│   └── routes.tsx
├── stores/                    # Zustand stores
│   ├── authStore.ts
│   └── uiStore.ts
├── types/                     # TypeScript types
│   ├── api.ts
│   ├── user-group.ts
│   └── common.ts
├── App.tsx
└── main.tsx
```

### 6.2 Key Frontend Requirements

#### State Management with Zustand

```typescript
// stores/userGroupStore.ts
import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";

interface UserGroupState {
  userGroups: UserGroup[];
  selectedGroup: UserGroup | null;
  isLoading: boolean;
  error: string | null;

  setUserGroups: (groups: UserGroup[]) => void;
  selectGroup: (group: UserGroup | null) => void;
  addUserGroup: (group: UserGroup) => void;
  updateUserGroup: (id: number, updates: Partial<UserGroup>) => void;
  deleteUserGroup: (id: number) => void;
}

export const useUserGroupStore = create<UserGroupState>()(
  devtools(
    persist(
      (set) => ({
        userGroups: [],
        selectedGroup: null,
        isLoading: false,
        error: null,

        setUserGroups: (groups) => set({ userGroups: groups }),
        selectGroup: (group) => set({ selectedGroup: group }),
        // ... other actions
      }),
      { name: "user-group-storage" }
    )
  )
);
```

#### API Service Layer

```typescript
// services/userGroupService.ts
import { api } from "@/lib/api";
import type { UserGroup, CreateUserGroupRequest } from "@/types";

export const userGroupService = {
  getAll: async (
    params?: PaginationParams
  ): Promise<PaginatedResponse<UserGroup>> => {
    const { data } = await api.get("/user-groups", { params });
    return data;
  },

  getById: async (id: number): Promise<UserGroup> => {
    const { data } = await api.get(`/user-groups/${id}`);
    return data.data;
  },

  create: async (request: CreateUserGroupRequest): Promise<UserGroup> => {
    const { data } = await api.post("/user-groups", request);
    return data.data;
  },

  update: async (
    id: number,
    request: UpdateUserGroupRequest
  ): Promise<UserGroup> => {
    const { data } = await api.put(`/user-groups/${id}`, request);
    return data.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/user-groups/${id}`);
  },
};
```

#### Custom Hooks

```typescript
// hooks/useUserGroups.ts
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { userGroupService } from "@/features/user-groups/services";
import { useToast } from "@/hooks/useToast";

export const useUserGroups = () => {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data, isLoading, error } = useQuery({
    queryKey: ["user-groups"],
    queryFn: userGroupService.getAll,
  });

  const createMutation = useMutation({
    mutationFn: userGroupService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-groups"] });
      toast({ title: "Success", description: "Permission group created" });
    },
    onError: (error) => {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  return {
    userGroups: data?.data || [],
    isLoading,
    error,
    createUserGroup: createMutation.mutate,
  };
};
```

#### Form Validation with Zod

```typescript
// schemas/userGroupSchema.ts
import { z } from "zod";

export const createUserGroupSchema = z.object({
  name: z.string().min(3, "Name must be at least 3 characters").max(100),
  description: z.string().optional(),
  teams: z.array(z.string()).min(1, "At least one team is required"),
  members: z.array(z.number()).optional(),
  moduleRoles: z
    .array(
      z.object({
        moduleId: z.number(),
        roleIds: z
          .array(z.number())
          .min(1, "At least one role must be selected"),
      })
    )
    .min(1, "At least one module role assignment is required"),
});

export type CreateUserGroupFormData = z.infer<typeof createUserGroupSchema>;
```

#### Protected Routes

```typescript
// routes/ProtectedRoute.tsx
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";

interface ProtectedRouteProps {
  requiredPermission?: string;
}

export const ProtectedRoute = ({ requiredPermission }: ProtectedRouteProps) => {
  const { isAuthenticated, hasPermission } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredPermission && !hasPermission(requiredPermission)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};
```

---

## 7. SECURITY REQUIREMENTS

### 7.1 Authentication & Authorization

- JWT-based authentication with access and refresh tokens
- Access token expiry: 15 minutes
- Refresh token expiry: 7 days
- Secure token storage (httpOnly cookies for web)
- OAuth2 integration for SSO providers (Google, Azure AD, etc.)
- Role-based access control (RBAC)
- Permission-based authorization at API endpoint level
- Multi-tenant isolation (data segregation by tenant_id)

### 7.2 Security Headers

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy("default-src 'self'")
            .xssProtection()
            .frameOptions().deny()
        )
        .csrf(csrf -> csrf.disable()) // Use token-based auth
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // ... other configurations
}
```

### 7.3 Input Validation & Sanitization

- Validate all user inputs using Bean Validation (JSR-380)
- Sanitize inputs to prevent XSS attacks
- Use parameterized queries to prevent SQL injection
- Implement rate limiting on sensitive endpoints
- Log security events (failed logins, unauthorized access attempts)

### 7.4 Data Protection

- Encrypt sensitive data at rest (use PostgreSQL encryption)
- Use HTTPS/TLS for all communications
- Implement field-level encryption for PII
- Mask sensitive data in logs
- Implement data retention and deletion policies

---

## 8. TESTING REQUIREMENTS

### 8.1 Unit Testing

- **Target**: 80%+ code coverage (measured by JaCoCo)
- **Scope**: All service layer methods, utility classes, mappers
- **Framework**: JUnit 5 + Mockito + AssertJ

```java
@ExtendWith(MockitoExtension.class)
class UserGroupServiceImplTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private UserGroupMapper userGroupMapper;

    @InjectMocks
    private UserGroupServiceImpl userGroupService;

    @Test
    @DisplayName("Should create user group successfully")
    void shouldCreateUserGroupSuccessfully() {
        // Given
        CreateUserGroupRequest request = new CreateUserGroupRequest();
        request.setName("Test Group");

        UserGroup userGroup = new UserGroup();
        userGroup.setName("Test Group");

        when(userGroupRepository.save(any())).thenReturn(userGroup);

        // When
        UserGroupResponse response = userGroupService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Group");
        verify(userGroupRepository, times(1)).save(any());
    }
}
```

### 8.2 Integration Testing

- **Framework**: Spring Boot Test + TestContainers
- **Scope**: API endpoints, database interactions, external service integrations

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class UserGroupControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUserGroup() throws Exception {
        String requestBody = """
            {
                "name": "Test Group",
                "teams": ["Team A"]
            }
            """;

        mockMvc.perform(post("/api/v1/user-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Test Group"));
    }
}
```

### 8.3 API Testing with Karate

```gherkin
Feature: User Group Management

  Background:
    * url baseUrl
    * def authToken = call read('classpath:auth.feature')

  Scenario: Create user group
    Given path 'user-groups'
    And header Authorization = 'Bearer ' + authToken
    And request { name: 'Test Group', teams: ['Team A'] }
    When method post
    Then status 201
    And match response.data.name == 'Test Group'
```

### 8.4 Performance Testing with Gatling

```scala
class UserGroupSimulation extends Simulation {
  val httpProtocol = http.baseUrl("http://localhost:8080/api/v1")

  val scn = scenario("User Group Load Test")
    .exec(http("Create User Group")
      .post("/user-groups")
      .header("Authorization", "Bearer ${token}")
      .body(StringBody("""{"name":"Test ${id}"}"""))
      .check(status.is(201)))

  setUp(
    scn.inject(rampUsers(100) during (60 seconds))
  ).protocols(httpProtocol)
}
```

---

## 9. CODE QUALITY & STANDARDS

### 9.1 SonarQube Quality Gates

```yaml
Bugs: 0
Vulnerabilities: 0
Code Smells: < 50
Coverage: > 80%
Duplications: < 3%
Maintainability Rating: A
Reliability Rating: A
Security Rating: A
```

### 9.2 Code Style

- **Java**: Google Java Style Guide or Spring conventions
- **TypeScript**: Airbnb + Prettier
- **Naming Conventions**:
  - Classes: PascalCase
  - Methods/Variables: camelCase
  - Constants: UPPER_SNAKE_CASE
  - Database tables: snake_case
  - API endpoints: kebab-case

### 9.3 Documentation Requirements

- JavaDoc for all public APIs
- Swagger/OpenAPI annotations for all endpoints
- README.md with setup instructions
- API documentation with examples
- Architecture decision records (ADRs)

---

## 10. MONITORING & OBSERVABILITY

### 10.1 Logging Strategy

```java
@Slf4j
public class UserGroupServiceImpl implements UserGroupService {

    public UserGroupResponse create(CreateUserGroupRequest request) {
        log.info("Creating user group: name={}", request.getName());

        try {
            // Business logic
            log.debug("User group created successfully: id={}", result.getId());
            return result;
        } catch (Exception e) {
            log.error("Failed to create user group: name={}", request.getName(), e);
            throw e;
        }
    }
}
```

### 10.2 Metrics with Micrometer

```java
@Component
public class UserGroupMetrics {
    private final Counter userGroupCreated;
    private final Timer userGroupCreateTimer;

    public UserGroupMetrics(MeterRegistry registry) {
        this.userGroupCreated = Counter.builder("user.group.created")
            .description("Number of user groups created")
            .register(registry);

        this.userGroupCreateTimer = Timer.builder("user.group.create.time")
            .description("Time to create user group")
            .register(registry);
    }
}
```

### 10.3 Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Check database connection
        return Health.up().withDetail("database", "PostgreSQL").build();
    }
}
```

---

## 11. DEPLOYMENT & DEVOPS

### 11.1 Docker Configuration

**Backend Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 11.2 Docker Compose (Development)

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: ahss
      POSTGRES_USER: ahss
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ahss
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

### 11.3 Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ahss-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ahss-backend
  template:
    metadata:
      labels:
        app: ahss-backend
    spec:
      containers:
        - name: ahss-backend
          image: ahss-backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "production"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
```

---

## 12. DEVELOPMENT WORKFLOW

### 12.1 Git Workflow

- **Branching Strategy**: GitFlow
  - `main`: Production-ready code
  - `develop`: Integration branch
  - `feature/*`: Feature development
  - `bugfix/*`: Bug fixes
  - `hotfix/*`: Production hotfixes
  - `release/*`: Release preparation

### 12.2 Commit Message Convention

```
<type>(<scope>): <subject>

<body>

<footer>

Types: feat, fix, docs, style, refactor, test, chore
Example: feat(user-groups): add permission group creation API
```

### 12.3 CI/CD Pipeline

```yaml
# GitHub Actions / GitLab CI
stages:
  - build
  - test
  - quality
  - deploy

build:
  - Compile Java code
  - Build Docker images

test:
  - Run unit tests
  - Run integration tests
  - Generate coverage report

quality:
  - SonarQube analysis
  - Security scan (OWASP Dependency Check)
  - Code style check

deploy:
  - Deploy to staging (develop branch)
  - Deploy to production (main branch, manual approval)
```

---

## 13. SPECIFIC IMPLEMENTATION REQUIREMENTS

### 13.1 Permission Group Creation Flow

1. Frontend submits form with group name, teams, and module-role selections
2. Backend validates request (all required fields, valid IDs)
3. Create `user_group` record
4. For each team name, create or link entities
5. Create `user_group_member` records for members
6. Create `group_module_role` records for module-role assignments
7. Return complete permission group with all relationships
8. Send email notification to group members (using Resend)

### 13.2 Permission Resolution

When a user logs in:

1. Fetch user's entity_id
2. Find all user_groups where entity is a member
3. For each group, fetch all group_module_role assignments
4. For each role, fetch all role_permission assignments
5. Aggregate unique permissions
6. Store in JWT claims and Redis cache
7. Frontend checks permissions before rendering UI elements

### 13.3 Multi-Tenancy Implementation

- Add `@TenantFilter` to all repository queries
- Use Hibernate filter or query interceptor
- Extract tenant_id from JWT token
- Automatically inject into all queries: `WHERE tenant_id = :currentTenantId`
- Prevent cross-tenant data access

### 13.4 Caching Strategy

```java
// Cache configuration
@Cacheable(value = "userGroups", key = "#id", unless = "#result == null")
public UserGroupResponse getById(Long id) { ... }

@CacheEvict(value = "userGroups", key = "#id")
public void update(Long id, UpdateUserGroupRequest request) { ... }

@CacheEvict(value = "userGroups", allEntries = true)
public void create(CreateUserGroupRequest request) { ... }
```

---

## 14. DELIVERABLES

### 14.1 Backend Deliverables

- [ ] Complete Spring Boot application with all layers
- [ ] Flyway migration scripts for database schema
- [ ] Comprehensive unit tests (80%+ coverage)
- [ ] Integration tests with TestContainers
- [ ] Karate BDD API tests
- [ ] Swagger/OpenAPI documentation
- [ ] Docker and Kubernetes deployment files
- [ ] Prometheus metrics configuration
- [ ] Grafana dashboards (JSON exports)
- [ ] README with setup instructions

### 14.2 Frontend Deliverables

- [ ] React + TypeScript application with Vite
- [ ] All UI screens matching provided designs
- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Form validation with error handling
- [ ] Loading states and error boundaries
- [ ] Zustand stores for state management
- [ ] API service layer with Axios
- [ ] Custom hooks for data fetching
- [ ] Unit tests for components
- [ ] Storybook for component documentation
- [ ] README with setup instructions

### 14.3 Documentation Deliverables

- [ ] API documentation (Swagger UI)
- [ ] Database schema diagram (ERD)
- [ ] Architecture decision records (ADRs)
- [ ] Deployment guide
- [ ] User manual
- [ ] Developer onboarding guide

---

## 15. QUALITY CHECKLIST

Before considering the project complete, verify:

- [ ] All API endpoints are working and documented
- [ ] Database migrations run successfully
- [ ] Unit test coverage > 80%
- [ ] Integration tests pass
- [ ] SonarQube quality gate passes
- [ ] Security vulnerabilities scanned and resolved
- [ ] Docker images build successfully
- [ ] Kubernetes deployments work
- [ ] Frontend builds without errors
- [ ] All forms have validation
- [ ] Error handling is comprehensive
- [ ] Logging is properly implemented
- [ ] Monitoring dashboards are functional
- [ ] Documentation is complete and accurate
- [ ] Code follows style guidelines
- [ ] No hardcoded credentials or secrets
- [ ] Environment variables are properly configured
- [ ] Performance meets requirements (< 200ms API response)
- [ ] Redis caching is working
- [ ] Email notifications are sent correctly
- [ ] OAuth2 authentication works
- [ ] Multi-tenancy isolation is enforced
- [ ] All screens are responsive
- [ ] Accessibility (WCAG 2.1 AA) standards met

---

## 16. SUCCESS CRITERIA

The project is considered successful when:

1. **Functional**: All user stories and requirements are implemented
2. **Performance**: API responses < 200ms (95th percentile)
3. **Reliability**: 99.9% uptime in production
4. **Security**: Passes security audit with no critical vulnerabilities
5. **Quality**: Meets all code quality gates (SonarQube, coverage, etc.)
6. **Usability**: User acceptance testing passes with > 90% satisfaction
7. **Maintainability**: Code is well-documented and follows best practices
8. **Scalability**: System handles 10,000+ concurrent users
9. **Deployment**: Automated CI/CD pipeline working smoothly
10. **Monitoring**: Full observability with metrics, logs, and traces

---

## 17. ADDITIONAL NOTES

- Use Lombok to reduce boilerplate code
- Implement global exception handling with proper error codes
- Use MapStruct for DTO-Entity mapping
- Implement soft delete for all major entities
- Add created_by, updated_by, created_at, updated_at to all tables
- Use correlation IDs for distributed tracing
- Implement request/response logging in API Gateway
- Use feature flags for gradual rollouts
- Implement database connection pooling (HikariCP)
- Use pessimistic locking for critical operations
- Implement retry mechanism with exponential backoff
- Add health check endpoints for all external dependencies
- Use circuit breaker for external API calls
- Implement graceful shutdown
- Add database read replicas for read-heavy operations
- Use database partitioning for large tables (if needed)
- Implement full-text search with PostgreSQL FTS or Elasticsearch
- Add WebSocket support for real-time notifications (future enhancement)

---

## FINAL INSTRUCTIONS FOR AI CODER

1. **Start with the database**: Create Flyway migrations first
2. **Build backend incrementally**:
   - Start with entities and repositories
   - Then services and controllers
   - Finally add security and caching
3. **Test as you go**: Write tests alongside code
4. **Frontend follows backend**: Build UI after APIs are stable
5. **Document everything**: Add comments, JavaDoc, and README updates
6. **Follow the tech stack exactly**: No substitutions unless critical
7. **Ask for clarification**: If requirements are unclear, ask before implementing
8. **Code quality first**: Don't compromise on quality for speed
9. **Security is paramount**: Validate all inputs, sanitize all outputs
10. **Think multi-tenant**: Every feature must respect tenant isolation

Good luck, Dennis! Build something amazing! 🚀
