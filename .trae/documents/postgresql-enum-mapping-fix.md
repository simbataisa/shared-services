# PostgreSQL Enum Mapping Fix Documentation

## Overview
This document details the resolution of a critical PostgreSQL enum mapping issue that was preventing role creation and other entity operations in the shared-services backend application.

## Problem Description

### Initial Issue
- **Error**: `SQL Error: 42704, SQLState: 42704` - `ERROR: type "rolestatus" does not exist`
- **Impact**: Role creation API endpoints returning HTTP 500 Internal Server Error
- **Root Cause**: Hibernate's `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` annotation was incorrectly mapping Java enum class names to PostgreSQL enum types

### Technical Details
The application was using Hibernate-specific annotations that attempted to cast enum values using the Java class name (e.g., `RoleStatus`) instead of the actual PostgreSQL enum type name (e.g., `role_status`). This caused SQL generation issues where Hibernate would try to cast to non-existent types.

**Example of problematic SQL generated:**
```sql
-- Hibernate was generating (incorrect):
INSERT INTO role (..., role_status, ...) VALUES (..., 'ACTIVE'::rolestatus, ...)

-- Instead of the correct:
INSERT INTO role (..., role_status, ...) VALUES (..., 'ACTIVE'::role_status, ...)
```

## Solution Implemented

### 1. Replaced Hibernate-Specific Annotations
Changed from `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` to standard JPA `@Enumerated(EnumType.STRING)` with explicit `columnDefinition`.

### 2. Updated Entity Classes
The following entities were updated to use consistent enum mapping:

#### Role Entity
```java
// Before (problematic):
@JdbcTypeCode(SqlTypes.NAMED_ENUM)
@Column(name = "role_status", nullable = false)
private RoleStatus roleStatus = RoleStatus.DRAFT;

// After (fixed):
@Enumerated(EnumType.STRING)
@Column(name = "role_status", nullable = false, columnDefinition = "role_status")
private RoleStatus roleStatus = RoleStatus.DRAFT;
```

#### Product Entity
```java
// Updated mapping:
@Enumerated(EnumType.STRING)
@Column(name = "product_status", nullable = false, columnDefinition = "product_status")
private ProductStatus productStatus = ProductStatus.DRAFT;
```

#### User Entity
```java
// Updated mapping:
@Enumerated(EnumType.STRING)
@Column(name = "user_status", nullable = false, columnDefinition = "user_status")
private UserStatus userStatus = UserStatus.ACTIVE;
```

#### Module Entity
```java
// Updated mapping:
@Enumerated(EnumType.STRING)
@Column(name = "module_status", nullable = false, columnDefinition = "module_status")
private ModuleStatus moduleStatus = ModuleStatus.DRAFT;
```

### 3. Removed Unused Imports
Cleaned up Hibernate-specific imports from all entity classes:
```java
// Removed these imports:
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
```

### 4. Enhanced Hibernate Logging
Added detailed SQL logging configuration in `application.yml` for debugging:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Database Schema
The PostgreSQL database contains the following enum types that correspond to the entity status fields:

- `role_status` - Used by Role entity
- `product_status` - Used by Product entity  
- `user_status` - Used by User entity
- `module_status` - Used by Module entity

These enum types are created via Flyway migrations and contain values like:
- `DRAFT`, `ACTIVE`, `INACTIVE`, `DEPRECATED` (for most status enums)

## Testing and Validation

### Successful Test Cases
After implementing the fix, the following operations work correctly:

1. **Role Creation**:
   ```bash
   curl -X POST http://localhost:8080/api/roles \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Test Role",
       "description": "Test Description", 
       "moduleId": 11
     }'
   ```
   **Result**: HTTP 201 Created with proper role data

2. **Enum Value Persistence**: Enum values are correctly stored and retrieved from PostgreSQL

3. **SQL Generation**: Hibernate now generates correct SQL with proper enum type casting

## Key Learnings

### Why the Fix Works
1. **Standard JPA Compliance**: Using `@Enumerated(EnumType.STRING)` follows JPA standards
2. **Explicit Type Definition**: `columnDefinition` explicitly tells Hibernate the PostgreSQL enum type name
3. **Consistent Mapping**: All entities now use the same enum mapping approach

### Best Practices Established
1. **Use Standard JPA Annotations**: Prefer JPA annotations over Hibernate-specific ones when possible
2. **Explicit Column Definitions**: Always specify `columnDefinition` for PostgreSQL enum types
3. **Consistent Entity Patterns**: Maintain consistent annotation patterns across all entities
4. **Enhanced Logging**: Keep SQL logging enabled during development for debugging

## Configuration Changes

### Application Configuration
Updated `application.yml` with:
- Enhanced Hibernate SQL logging
- CamelCaseToUnderscoresNamingStrategy (already configured)

### No Database Changes Required
The fix only required application-level changes. No database schema modifications were needed since the PostgreSQL enum types were already correctly defined.

## Future Considerations

### Maintenance
- When adding new enum fields, always use the established pattern:
  ```java
  @Enumerated(EnumType.STRING)
  @Column(name = "field_name", columnDefinition = "enum_type_name")
  ```

### Migration Strategy
- This fix can be applied to other similar projects experiencing PostgreSQL enum mapping issues
- The solution is backward compatible and doesn't require database migrations

## Resolution Timeline
- **Issue Identified**: PostgreSQL enum type casting errors
- **Root Cause Analysis**: Hibernate annotation incompatibility  
- **Solution Implementation**: Replaced with standard JPA annotations
- **Testing & Validation**: Confirmed successful role creation and enum persistence
- **Documentation**: Created this comprehensive guide

## Status
✅ **RESOLVED** - All enum mapping issues have been fixed and tested successfully.

---
*Document created: October 5, 2025*  
*Last updated: October 5, 2025*  
*Author: Development Team*