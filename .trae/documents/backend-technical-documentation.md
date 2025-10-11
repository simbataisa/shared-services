# Backend Technical Documentation

## Overview

The AHSS Shared Services backend is a Spring Boot application built with Java 21, providing REST APIs for managing products, modules, roles, permissions, and user groups in a multi-tenant environment.

## Technology Stack

### Core Technologies
- **Java Version**: 21
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **Architecture**: Controller-Service-Repository-Model layers

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- JWT (JSON Web Tokens) - io.jsonwebtoken:jjwt-api:0.11.5
- Flyway Database Migration
- PostgreSQL Driver
- H2 Database (for testing)

## Architecture

### Layer Structure
```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Entity/Model Layer (Database entities)
```

### Package Structure
```
com.ahss/
├── controller/          # REST Controllers
├── service/            # Service interfaces
├── service/impl/       # Service implementations
├── repository/         # JPA Repositories
├── entity/            # JPA Entities
├── dto/               # Data Transfer Objects
├── security/          # Security configuration
└── config/            # Application configuration
```

## Database Configuration

### PostgreSQL (Production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sharedservices
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: postgres
    hikari:
      # Connection pool settings to prevent thread starvation
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      # Performance optimizations
      auto-commit: false
      connection-test-query: SELECT 1
      validation-timeout: 5000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### H2 (Development/Testing)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
  flyway:
    enabled: false
```

## Database Schema

### Core Entities

#### Product
- **Table**: `product`
- **Primary Key**: `product_id` (BIGSERIAL)
- **Fields**:
  - `product_code` (VARCHAR(50), UNIQUE)
  - `product_name` (TEXT)
  - `description` (TEXT)
  - `product_status` (ENUM: DRAFT, ACTIVE, INACTIVE)
  - Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`

#### Module
- **Table**: `module`
- **Primary Key**: `module_id` (BIGSERIAL)
- **Foreign Keys**: `product_id` → `product(product_id)`
- **Fields**:
  - `name` (TEXT)
  - `code` (VARCHAR(50), UNIQUE)
  - `description` (TEXT)
  - `module_status` (ENUM: DRAFT, ACTIVE, INACTIVE)
  - Audit fields

#### Role
- **Table**: `role`
- **Primary Key**: `role_id` (BIGSERIAL)
- **Foreign Keys**: `module_id` → `module(module_id)`
- **Fields**:
  - `name` (TEXT)
  - `description` (TEXT)
  - `role_status` (ENUM: DRAFT, ACTIVE, INACTIVE, DEPRECATED)
  - Audit fields
- **Constraints**: UNIQUE(module_id, name)

#### Permission
- **Table**: `permission`
- **Primary Key**: `permission_id` (BIGSERIAL)
- **Fields**:
  - `name` (TEXT, UNIQUE)
  - `description` (TEXT)
  - `resource_type` (TEXT)
  - `action` (TEXT)
  - Audit fields

#### User Group
- **Table**: `user_group`
- **Primary Key**: `user_group_id` (BIGSERIAL)
- **Fields**:
  - `name` (TEXT)
  - `description` (TEXT)
  - `deleted_at` (TIMESTAMP) - Soft delete
  - Audit fields

### Multi-tenant Support

#### Tenant
- **Table**: `tenant`
- **Primary Key**: `tenant_id` (BIGSERIAL)
- **Fields**:
  - `tenant_code` (VARCHAR(50), UNIQUE)
  - `name` (TEXT)
  - `tenant_status` (ENUM)
  - Audit fields

#### Organization
- **Table**: `organization`
- **Primary Key**: `org_id` (BIGSERIAL)
- **Foreign Keys**: `tenant_id`, `parent_org_id` (self-reference)
- **Fields**:
  - `name` (TEXT)
  - `country` (VARCHAR(3))
  - `path` (TEXT) - Hierarchical path
  - Audit fields

### Relationship Tables

#### Role-Permission Mapping
- **Table**: `role_permission`
- **Composite Key**: (role_id, permission_id)

#### User Group Members
- **Table**: `user_group_member`
- **Composite Key**: (user_group_id, entity_id)

#### Group Module Role Assignment
- **Table**: `group_module_role`
- **Primary Key**: `group_module_role_id`
- **Unique Constraint**: (user_group_id, module_id, role_id)

## API Endpoints

### Authentication
- **Base Path**: `/api/v1/auth`
- **POST** `/login` - User authentication (returns JWT token)
  - **Request Body**: `{"username": "string", "password": "string"}`
  - **Response**: `{"data": {"token": "jwt_token"}, "message": "Login successful", "success": true}`
  - **Status Codes**: 
    - 200: Successful login
    - 400: Invalid credentials or account locked
    - 401: Invalid credentials
    - 500: Internal server error

### Users
- **Base Path**: `/api/v1/users`
- **GET** `/` - Get all active users (paginated)
- **POST** `/` - Create new user
- **GET** `/{id}` - Get user by ID
- **PUT** `/{id}` - Update user
- **DELETE** `/{id}` - Delete user (soft delete)
- **PATCH** `/{id}/activate` - Activate user
- **PATCH** `/{id}/deactivate` - Deactivate user
- **PATCH** `/{id}/change-password` - Change user password
- **GET** `/{id}/roles` - Get user roles
- **POST** `/{id}/roles` - Assign role to user
- **DELETE** `/{id}/roles/{roleId}` - Remove role from user

### User Groups
- **Base Path**: `/api/v1/user-groups`
- **GET** `/` - Get paginated list of user groups
- **POST** `/` - Create new user group
- **GET** `/{id}` - Get user group by ID
- **PUT** `/{id}` - Update user group
- **DELETE** `/{id}` - Delete user group (soft delete)

### Products
- **Base Path**: `/api/products`
- **GET** `/` - Get all active products
- **POST** `/` - Create new product
- **GET** `/{id}` - Get product by ID
- **PUT** `/{id}` - Update product
- **DELETE** `/{id}` - Delete product
- **PATCH** `/{id}/activate` - Activate product
- **PATCH** `/{id}/deactivate` - Deactivate product

### Modules
- **Base Path**: `/api/modules`
- **GET** `/` - Get all active modules
- **POST** `/` - Create new module
- **GET** `/{id}` - Get module by ID
- **PUT** `/{id}` - Update module
- **DELETE** `/{id}` - Delete module
- **PATCH** `/{id}/activate` - Activate module
- **PATCH** `/{id}/deactivate` - Deactivate module

### Roles
- **Base Path**: `/api/roles`
- **GET** `/` - Get all active roles
- **POST** `/` - Create new role
- **GET** `/{id}` - Get role by ID
- **PUT** `/{id}` - Update role
- **DELETE** `/{id}` - Delete role
- **PATCH** `/{id}/activate` - Activate role
- **PATCH** `/{id}/deactivate` - Deactivate role

### Permissions
- **Base Path**: `/api/permissions`
- **GET** `/` - Get all active permissions
- **POST** `/` - Create new permission
- **GET** `/{id}` - Get permission by ID
- **PUT** `/{id}` - Update permission
- **DELETE** `/{id}` - Delete permission
- **PATCH** `/{id}/activate` - Activate permission
- **PATCH** `/{id}/deactivate` - Deactivate permission

### User Groups
- **Base Path**: `/api/v1/user-groups`
- **GET** `/` - Get paginated list of user groups
- **POST** `/` - Create new user group

### Dashboard
- **Base Path**: `/api/v1/dashboard`
- **GET** `/stats` - Get dashboard statistics (total counts, active entities, etc.)
- **GET** `/recent-activities` - Get recent activities and changes in the system

### Tenants (Multi-tenant Support)
- **Base Path**: `/api/v1/tenants` (Note: Controller not yet implemented)
- **Planned Endpoints**:
  - **GET** `/` - Get all active tenants
  - **POST** `/` - Create new tenant
  - **GET** `/{id}` - Get tenant by ID
  - **PUT** `/{id}` - Update tenant
  - **DELETE** `/{id}` - Delete tenant
  - **PATCH** `/{id}/activate` - Activate tenant
  - **PATCH** `/{id}/deactivate` - Deactivate tenant

## Security Configuration

### JWT Authentication
- **Token Provider**: `JwtTokenProvider`
- **Secret Key**: Static key for development (should be externalized)
- **Expiration**: 1 day (86400000 ms)
- **Algorithm**: HS256
- **Token Structure**:
  ```json
  {
    "sub": "user@example.com",
    "userId": 1,
    "username": "username",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["System Administrator", "Super Administrator"],
    "permissions": ["users:read", "users:create", "tenants:read", "tenants:create", ...],
    "isAdmin": true,
    "isSuperAdmin": true,
    "iat": 1234567890,
    "exp": 1234567890
  }
  ```

### Permission System
The application uses a comprehensive permission-based access control system:

#### Permission Naming Convention
- **Format**: `{resource}:{action}`
- **Resources**: users, tenants, roles, permissions, modules, products, audit, system
- **Actions**: read, create, update, delete, admin

#### Current Permissions
- **User Management**: `users:read`, `users:create`, `users:update`, `users:delete`, `users:admin`
- **Tenant Management**: `tenants:read`, `tenants:create`, `tenants:update`, `tenants:delete`, `tenants:admin`
- **Role Management**: `role:read`, `role:create`, `role:update`, `role:delete`, `role:admin`
- **Permission Management**: `permission:read`, `permission:create`, `permission:update`, `permission:delete`
- **Module Management**: `module:read`, `module:create`, `module:update`, `module:delete`
- **Product Management**: `product:read`, `product:create`, `product:update`, `product:delete`
- **User Groups**: `user-groups:read`, `user-groups:create`, `user-groups:update`, `user-groups:delete`
- **Audit**: `audit:read`
- **System**: `system:read`, `system:admin`

#### Recent Permission Updates
- **V5 Migration**: Added plural tenant permissions (`tenants:*`) to match frontend expectations
- **Frontend Compatibility**: Both singular (`tenant:*`) and plural (`tenants:*`) permissions exist for backward compatibility
- **User Groups**: Added complete CRUD permissions for user groups management

### Security Rules
```java
// Public endpoints
"/api/v1/auth/**" - permitAll()
"/h2-console/**" - permitAll()
"/api/**" - permitAll() // Currently open for development

// Default
anyRequest() - permitAll() // Currently open for development
```

### CORS Configuration
- **Allowed Origins**: `http://localhost:5173` (Frontend development server)
- **Applied to**: All API controllers via `@CrossOrigin` annotation

## Service Layer Implementation

### Common Pattern
All service implementations follow the same pattern:
- **Active Filter**: Only return entities where `active = true`
- **DTO Conversion**: Convert entities to DTOs for API responses
- **Validation**: Input validation using Bean Validation
- **Transaction Management**: `@Transactional` for data consistency

### Example Service Methods
```java
// Get all active entities
public List<EntityDto> getAllActiveEntities() {
    return repository.findAll()
        .stream()
        .filter(entity -> entity.getActive())
        .map(this::convertToDto)
        .collect(Collectors.toList());
}

// Activate/Deactivate pattern
public void activateEntity(Long id) {
    Entity entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
    entity.setActive(true);
    repository.save(entity);
}
```

## Data Transfer Objects (DTOs)

### Common DTO Structure
```java
public class EntityDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Getters and setters
}
```

### Response Wrapper
```java
public class ApiResponse<T> {
    private T data;
    private String message;
    private String path;
    private boolean success;
    // Factory methods: ok(), error()
}
```

## Database Migration

### Flyway Configuration
- **Location**: `src/main/resources/db/migration`
- **Naming Convention**: `V{version}__{description}.sql`
- **Current Version**: V1__init.sql (Initial schema)

### Migration Features
- Complete schema creation
- Indexes for performance optimization
- Enum types for status fields
- Foreign key constraints
- Audit fields on all tables

## Development Notes

### Current State
- **Database**: Configured for both PostgreSQL (production) and H2 (development)
- **Security**: Currently permissive for development (all endpoints open)
- **Field Migration**: Recently migrated from `isActive` to `active` field
- **Testing**: All CRUD operations verified and working
- **Enum Mapping**: PostgreSQL enum types properly mapped using `@Enumerated(EnumType.STRING)`
- **Role Status**: Enhanced with DEPRECATED status for better lifecycle management

### Known Issues
- JWT secret should be externalized to environment variables
- Security configuration is too permissive for production
- Need to implement proper role-based access control
- Missing comprehensive error handling

### Recent Fixes and Updates

#### Status Management System Enhancement (Latest)
- **Enhancement**: Improved status management across all entities with consistent UI components
- **Components**: Enhanced status display and management through standardized status cards
- **Features**:
  - Unified status display with color-coded indicators
  - Interactive status change buttons with permission controls
  - Consistent "Danger Zone" sections for entity deletion
  - Improved user experience with confirmation dialogs
- **Entities Affected**: Products, Roles, Users, Tenants, Permissions, User Groups
- **Impact**: Provides consistent status management interface across the entire application

#### HikariCP Connection Pool Optimization
- **Issue**: HikariCP thread starvation warning: "Thread starvation or clock leap detected (housekeeper delta=15m39s452ms)"
- **Root Cause**: Default HikariCP configuration was insufficient for the application's database connection needs, leading to connection pool exhaustion
- **Solution**: 
  - Added comprehensive HikariCP configuration in `application.yml`
  - Set `maximum-pool-size: 20` (increased from default 10)
  - Configured `minimum-idle: 5` for maintaining idle connections
  - Added connection timeouts and lifecycle management settings
  - Enabled leak detection with 60-second threshold
  - Optimized performance with `auto-commit: false` and connection validation
- **Impact**: Eliminated thread starvation warnings and improved database connection management performance

#### Permission System Alignment
- **Issue**: Frontend permission checks were failing due to mismatch between singular (`tenant:read`) and plural (`tenants:read`) permission names
- **Root Cause**: JWT tokens contained plural permissions (`tenants:*`) while frontend code expected singular (`tenant:*`)
- **Solution**: 
  - Updated frontend permission checks to use plural forms (`tenants:read`, `tenants:create`, `tenants:update`, `tenants:delete`)
  - Added V5 migration to include both singular and plural tenant permissions for compatibility
  - Updated `usePermissions.ts`, `Dashboard.tsx`, `TenantDetail.tsx`, and `TenantList.tsx` components
- **Impact**: Fixed "Tenant" menu visibility for superadmin and admin users

#### Authentication System
- **JWT Token Generation**: Enhanced `JwtTokenProvider.generateTokenWithUserInfo()` to include comprehensive user information
- **Token Claims**: Includes userId, username, firstName, lastName, roles, permissions, isAdmin, isSuperAdmin flags
- **Login Endpoint**: `/api/v1/auth/login` with proper error handling and account status validation
- **Security Filter**: `JwtAuthenticationFilter` processes Bearer tokens and sets Spring Security context

#### Database Schema Updates
- **V5 Migration**: Added missing permissions for frontend compatibility
- **Tenant Permissions**: Added plural forms (`tenants:*`) alongside existing singular forms (`tenant:*`)
- **User Groups**: Added complete CRUD permissions (`user-groups:*`)
- **Role Assignments**: Automatically assigned new permissions to Super Administrator role

### Known Issues
- JWT secret should be externalized to environment variables
- Security configuration is too permissive for production (currently all endpoints are open)
- Tenant controller not yet implemented (only database schema exists)
- Need to implement proper role-based access control at endpoint level
- Missing comprehensive error handling and validation
- JWT Authentication Filter is currently disabled in SecurityConfig for development

### Next Steps
1. **Implement Tenant Controller**: Create REST endpoints for tenant management
2. **Enable JWT Security**: Uncomment JWT filter in SecurityConfig and implement proper endpoint security
3. **Role-Based Access Control**: Implement `@PreAuthorize` annotations on controller methods
4. **Environment Configuration**: Externalize JWT secret and database credentials
5. **Error Handling**: Add comprehensive error handling with proper HTTP status codes
6. **API Documentation**: Add Swagger/OpenAPI documentation
7. **Testing**: Add comprehensive unit and integration tests
8. **Monitoring**: Implement health checks and monitoring endpoints

## Build and Run

### Prerequisites
- Java 21
- Gradle
- PostgreSQL (for production) or H2 (for development)

### Build Commands
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Environment Variables
```bash
# Database configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sharedservices
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## API Testing

### Sample Requests

#### Authentication
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password"}'
```

#### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "description": "Test description"}'
```

#### Create Role
```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Role", "moduleId": 1, "roleStatus": "ACTIVE"}'
```

#### Get All Roles
```bash
curl -X GET http://localhost:8080/api/roles
```

#### Get Role by ID
```bash
curl -X GET http://localhost:8080/api/roles/1
```

#### Update Role
```bash
curl -X PUT http://localhost:8080/api/roles/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Role", "moduleId": 1, "roleStatus": "INACTIVE"}'
```

#### Activate/Deactivate Role
```bash
# Activate role
curl -X PATCH http://localhost:8080/api/roles/1/activate

# Deactivate role
curl -X PATCH http://localhost:8080/api/roles/1/deactivate
```

## Monitoring and Logging

### Logging Configuration
- **Level**: INFO for Spring Security
- **Format**: Standard Spring Boot logging format
- **SQL Logging**: Enabled with `hibernate.format_sql: true`

### Health Checks
- **Endpoint**: `/actuator/health` (when Spring Boot Actuator is added)
- **Database**: Connection health monitoring
- **Application**: Basic application health status