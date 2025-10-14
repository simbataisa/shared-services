# Database Schema Refactoring Documentation

## Overview
This document outlines the comprehensive database schema refactoring performed on the shared-services application, specifically focusing on roles, permissions, modules, and their relationships. The refactoring addressed orphaned resource types, standardized naming conventions, and improved data consistency.

## Migration History

### V10: Fix Orphaned Permission Resource Types
**File:** `V10__fix_orphaned_permission_resource_types.sql`

#### Problem Identified
- Permissions existed with `resource_type` values that didn't correspond to actual modules in the system
- Orphaned resource types: `MODULE_MGMT`, `PRODUCT_MGMT`, `SYSTEM_CONFIG`
- These permissions were linked to `CORE_CONFIG` and `CORE_DASHBOARD` modules but had incorrect resource types

#### Solution Implemented
```sql
-- Updated orphaned permissions to use correct resource types with unique naming
UPDATE permission 
SET resource_type = 'CORE_CONFIG', 
    name = 'CORE_CONFIG:module_' || SUBSTRING(name FROM POSITION(':' IN name) + 1)
WHERE resource_type = 'MODULE_MGMT';

UPDATE permission 
SET resource_type = 'CORE_DASHBOARD', 
    name = 'CORE_DASHBOARD:product_' || SUBSTRING(name FROM POSITION(':' IN name) + 1)
WHERE resource_type = 'PRODUCT_MGMT';

UPDATE permission 
SET resource_type = 'CORE_CONFIG', 
    name = 'CORE_CONFIG:system_' || SUBSTRING(name FROM POSITION(':' IN name) + 1)
WHERE resource_type = 'SYSTEM_CONFIG';
```

#### Results
- All permissions now have valid `resource_type` values that match existing modules
- Unique naming convention prevents conflicts: `CORE_CONFIG:module_admin`, `CORE_DASHBOARD:product_admin`

### V11: Update Module Codes to Management Format
**File:** `V11__update_module_codes_to_mgmt.sql`

#### Changes Made
1. **Module Code Updates:**
   - `CORE_CONFIG` → `MODULE_MGMT`
   - `CORE_DASHBOARD` → `PRODUCT_MGMT`

2. **Permission Updates:**
   - Updated `resource_type` to match new module codes
   - Updated permission names to reflect new module codes

```sql
-- Update module codes
UPDATE module SET code = 'MODULE_MGMT', name = 'Module Management' WHERE code = 'CORE_CONFIG';
UPDATE module SET code = 'PRODUCT_MGMT', name = 'Product Management' WHERE code = 'CORE_DASHBOARD';

-- Update permission resource_type and names
UPDATE permission 
SET resource_type = 'MODULE_MGMT', 
    name = REPLACE(name, 'CORE_CONFIG:', 'MODULE_MGMT:')
WHERE resource_type = 'CORE_CONFIG';

UPDATE permission 
SET resource_type = 'PRODUCT_MGMT', 
    name = REPLACE(name, 'CORE_DASHBOARD:', 'PRODUCT_MGMT:')
WHERE resource_type = 'CORE_DASHBOARD';
```

### V12: Fix Permission Names Format
**File:** `V12__fix_permission_names_format.sql`

#### Problem Identified
Permission names didn't follow the standard `module.code:action` template:
- ❌ `PRODUCT_MGMT:product_create` (redundant prefix)
- ❌ `MODULE_MGMT:module_admin` (redundant prefix)
- ✅ `PRODUCT_MGMT:create` (correct format)

#### Solution Implemented
1. **Removed Redundant Prefixes:**
```sql
-- Fix PRODUCT_MGMT permissions
UPDATE permission 
SET name = REPLACE(name, 'PRODUCT_MGMT:product_', 'PRODUCT_MGMT:')
WHERE resource_type = 'PRODUCT_MGMT' AND name LIKE 'PRODUCT_MGMT:product_%';

-- Fix MODULE_MGMT permissions
UPDATE permission 
SET name = REPLACE(name, 'MODULE_MGMT:module_', 'MODULE_MGMT:')
WHERE resource_type = 'MODULE_MGMT' AND name LIKE 'MODULE_MGMT:module_%';
```

2. **Handled Conflicts:**
```sql
-- Rename system_config to avoid conflicts
UPDATE permission 
SET name = 'MODULE_MGMT:config'
WHERE resource_type = 'MODULE_MGMT' AND name = 'MODULE_MGMT:system_config';

-- Remove duplicate permissions
DELETE FROM role_permission 
WHERE permission_id IN (
    SELECT permission_id FROM permission 
    WHERE resource_type = 'MODULE_MGMT' 
    AND name IN ('MODULE_MGMT:system_admin', 'MODULE_MGMT:system_read')
);

DELETE FROM permission 
WHERE resource_type = 'MODULE_MGMT' 
AND name IN ('MODULE_MGMT:system_admin', 'MODULE_MGMT:system_read');
```

## Final Schema State

### Modules
| module_id | code | name |
|-----------|------|------|
| 1 | PRODUCT_MGMT | Product Management |
| 2 | MODULE_MGMT | Module Management |

### Permissions (Final Format)
| Name | Resource Type | Action |
|------|---------------|--------|
| MODULE_MGMT:admin | MODULE_MGMT | admin |
| MODULE_MGMT:config | MODULE_MGMT | config |
| MODULE_MGMT:create | MODULE_MGMT | create |
| MODULE_MGMT:delete | MODULE_MGMT | delete |
| MODULE_MGMT:read | MODULE_MGMT | read |
| MODULE_MGMT:update | MODULE_MGMT | update |
| PRODUCT_MGMT:admin | PRODUCT_MGMT | admin |
| PRODUCT_MGMT:create | PRODUCT_MGMT | create |
| PRODUCT_MGMT:delete | PRODUCT_MGMT | delete |
| PRODUCT_MGMT:read | PRODUCT_MGMT | read |
| PRODUCT_MGMT:update | PRODUCT_MGMT | update |

## Naming Convention Standards

### Permission Naming Template
```
{module.code}:{action}
```

**Examples:**
- ✅ `MODULE_MGMT:create`
- ✅ `PRODUCT_MGMT:admin`
- ❌ `MODULE_MGMT:module_create` (redundant prefix)
- ❌ `PRODUCT_MGMT:product_admin` (redundant prefix)

### Module Code Standards
- Use descriptive, uppercase codes with underscores
- Follow pattern: `{DOMAIN}_{TYPE}` (e.g., `MODULE_MGMT`, `PRODUCT_MGMT`)
- Ensure codes are unique and meaningful

## API Impact

### Endpoints Affected
- `GET /api/v1/permissions` - Returns updated permission names
- All permission-related endpoints now use the standardized naming format

### Response Format
```json
{
  "name": "MODULE_MGMT:admin",
  "moduleId": 2,
  "moduleName": "Module Management",
  "action": "admin",
  "resourceType": "MODULE_MGMT"
}
```

## Database Constraints

### Foreign Key Relationships
- `role_permission.permission_id` → `permission.permission_id`
- `permission.module_id` → `module.module_id`

### Unique Constraints
- `permission.name` - Ensures no duplicate permission names
- `module.code` - Ensures unique module codes

## Verification Steps

1. **Database Verification:**
```sql
-- Check all permissions follow naming convention
SELECT name, resource_type, action 
FROM permission 
WHERE resource_type IN ('MODULE_MGMT', 'PRODUCT_MGMT') 
ORDER BY name;
```

2. **API Verification:**
```bash
# Test permissions endpoint
curl -s "http://localhost:8080/api/v1/permissions" | jq '.[] | select(.name | startswith("MODULE_MGMT") or startswith("PRODUCT_MGMT"))'
```

## Best Practices Established

1. **Consistent Naming:** All permissions follow `module.code:action` format
2. **Data Integrity:** Foreign key constraints maintained throughout migrations
3. **Conflict Resolution:** Systematic approach to handling duplicate names
4. **Migration Safety:** Each migration includes rollback considerations
5. **Documentation:** Clear comments in migration files explaining changes

## Future Considerations

1. **New Modules:** Follow established naming conventions
2. **Permission Creation:** Use standardized `module.code:action` format
3. **Migration Strategy:** Always handle foreign key constraints when modifying permissions
4. **Testing:** Verify both database and API functionality after schema changes

## Rollback Strategy

If rollback is needed, migrations should be reverted in reverse order:
1. Revert V12 (restore original permission names)
2. Revert V11 (restore original module codes)
3. Revert V10 (restore original orphaned state if needed)

**Note:** Rollback scripts should be created and tested before applying migrations in production.