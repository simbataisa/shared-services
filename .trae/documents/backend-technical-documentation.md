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

## Security Configuration

### JWT Authentication
- **Token Provider**: `JwtTokenProvider`
- **Secret Key**: Static key for development (should be externalized)
- **Expiration**: 1 day (86400000 ms)
- **Algorithm**: HS512

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

### Recent Fixes
- **Role Status Enum**: Fixed PostgreSQL enum mapping issue by using `@Enumerated(EnumType.STRING)` with `columnDefinition = "role_status"`
- **Hibernate Configuration**: Added `hibernate.jdbc.lob.non_contextual_creation: true` for better PostgreSQL compatibility
- **Role Endpoints**: All role management endpoints (CRUD, activation/deactivation) verified and working correctly

### Next Steps
1. Implement proper security with role-based access
2. Add comprehensive error handling and validation
3. Implement audit logging
4. Add API documentation with Swagger/OpenAPI
5. Add comprehensive test coverage
6. Implement caching with Redis
7. Add monitoring and health checks

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