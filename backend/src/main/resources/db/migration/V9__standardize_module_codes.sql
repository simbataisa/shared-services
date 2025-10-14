-- V9: Standardize module codes from _admin to _mgmt pattern
-- Update module.code and module.name to use consistent management terminology
-- Update permission.resource_type and permission.name to reflect new module codes

-- Update USER_ADMIN to USER_MGMT
UPDATE module 
SET code = 'USER_MGMT', 
    name = 'User Management'
WHERE code = 'USER_ADMIN';

-- Update TENANT_ADMIN to TENANT_MGMT  
UPDATE module 
SET code = 'TENANT_MGMT', 
    name = 'Tenant Management'
WHERE code = 'TENANT_ADMIN';

-- Update RBAC_ROLES to ROLE_MGMT for consistency
UPDATE module 
SET code = 'ROLE_MGMT', 
    name = 'Role Management'
WHERE code = 'RBAC_ROLES';

-- Update RBAC_PERMISSIONS to PERMISSION_MGMT for consistency
UPDATE module 
SET code = 'PERMISSION_MGMT', 
    name = 'Permission Management'
WHERE code = 'RBAC_PERMISSIONS';

-- Update RBAC_ACCESS to ACCESS_MGMT for consistency
UPDATE module 
SET code = 'ACCESS_MGMT', 
    name = 'Access Management'
WHERE code = 'RBAC_ACCESS';

-- Update USER_GROUPS to GROUP_MGMT for consistency
UPDATE module 
SET code = 'GROUP_MGMT', 
    name = 'Group Management'
WHERE code = 'USER_GROUPS';

-- Now update permissions to use the new module codes
-- Update USER_ADMIN permissions to USER_MGMT
UPDATE permission 
SET resource_type = 'USER_MGMT',
    name = REPLACE(name, 'USER_ADMIN:', 'USER_MGMT:')
WHERE resource_type = 'USER_ADMIN';

-- Update TENANT_ADMIN permissions to TENANT_MGMT
UPDATE permission 
SET resource_type = 'TENANT_MGMT',
    name = REPLACE(name, 'TENANT_ADMIN:', 'TENANT_MGMT:')
WHERE resource_type = 'TENANT_ADMIN';

-- Update RBAC_ROLES permissions to ROLE_MGMT
UPDATE permission 
SET resource_type = 'ROLE_MGMT',
    name = REPLACE(name, 'RBAC_ROLES:', 'ROLE_MGMT:')
WHERE resource_type = 'RBAC_ROLES';

-- Update RBAC_PERMISSIONS permissions to PERMISSION_MGMT
UPDATE permission 
SET resource_type = 'PERMISSION_MGMT',
    name = REPLACE(name, 'RBAC_PERMISSIONS:', 'PERMISSION_MGMT:')
WHERE resource_type = 'RBAC_PERMISSIONS';

-- Update USER_GROUPS permissions to GROUP_MGMT
UPDATE permission 
SET resource_type = 'GROUP_MGMT',
    name = REPLACE(name, 'USER_GROUPS:', 'GROUP_MGMT:')
WHERE resource_type = 'USER_GROUPS';