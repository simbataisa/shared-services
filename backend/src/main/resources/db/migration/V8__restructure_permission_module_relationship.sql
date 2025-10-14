-- Migration V8: Restructure permission-module relationship
-- This migration:
-- 1. Drops the group_module_role table (no longer needed since roles are decoupled from modules)
-- 2. Adds module_id foreign key to permission table to establish one-to-many relationship
-- 3. Updates existing permissions to follow module:action naming convention

-- Drop the group_module_role table and its indexes
DROP INDEX IF EXISTS idx_group_module_role_group;
DROP INDEX IF EXISTS idx_group_module_role_module;
DROP TABLE IF EXISTS group_module_role;

-- Add module_id column to permission table
ALTER TABLE permission ADD COLUMN module_id BIGINT;

-- Add foreign key constraint to link permissions to modules
ALTER TABLE permission ADD CONSTRAINT fk_permission_module 
    FOREIGN KEY (module_id) REFERENCES module(module_id);

-- Create index for the new foreign key
CREATE INDEX IF NOT EXISTS idx_permission_module ON permission(module_id);

-- Update existing permissions to follow resource_type:action naming convention
-- Set resource_type to module.code and name to resource_type:action format

-- Update user permissions to be linked to User Administration module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'USER_ADMIN' LIMIT 1),
    resource_type = 'USER_ADMIN',
    name = 'USER_ADMIN:' || action
WHERE name LIKE 'user:%';

-- First, remove role-permission relationships for duplicate tenant permissions that will be deleted
DELETE FROM role_permission WHERE permission_id IN (
    SELECT permission_id FROM permission 
    WHERE name LIKE 'tenant:%' AND EXISTS (
        SELECT 1 FROM permission p2 WHERE p2.name = REPLACE(permission.name, 'tenant:', 'tenants:')
    )
);

-- Then delete duplicate tenant permissions (keep tenants: versions, remove tenant: versions)
DELETE FROM permission WHERE name LIKE 'tenant:%' AND EXISTS (
    SELECT 1 FROM permission p2 WHERE p2.name = REPLACE(permission.name, 'tenant:', 'tenants:')
);

-- Update remaining tenant permissions to be linked to Tenant Administration module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'TENANT_ADMIN' LIMIT 1),
    resource_type = 'TENANT_ADMIN',
    name = 'TENANT_ADMIN:' || action
WHERE name LIKE 'tenant%:%';

-- Update role permissions to be linked to Role Management module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'RBAC_ROLES' LIMIT 1),
    resource_type = 'RBAC_ROLES',
    name = 'RBAC_ROLES:' || action
WHERE name LIKE 'role:%';

-- Update permission permissions to be linked to Permission Management module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'RBAC_PERMISSIONS' LIMIT 1),
    resource_type = 'RBAC_PERMISSIONS',
    name = 'RBAC_PERMISSIONS:' || action
WHERE name LIKE 'permission:%';

-- Update module permissions to be linked to System Configuration module (no specific module management)
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'CORE_CONFIG' LIMIT 1),
    resource_type = 'MODULE_MGMT',
    name = 'MODULE_MGMT:' || action
WHERE name LIKE 'module:%';

-- Update product permissions to be linked to Dashboard module (no specific product management)
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'CORE_DASHBOARD' LIMIT 1),
    resource_type = 'PRODUCT_MGMT',
    name = 'PRODUCT_MGMT:' || action
WHERE name LIKE 'product:%';

-- Update user-groups permissions to be linked to User Groups module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'USER_GROUPS' LIMIT 1),
    resource_type = 'USER_GROUPS',
    name = 'USER_GROUPS:' || action
WHERE name LIKE 'user-groups:%';

-- Update system permissions to be linked to System Configuration module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'CORE_CONFIG' LIMIT 1),
    resource_type = 'SYSTEM_CONFIG',
    name = 'SYSTEM_CONFIG:' || action
WHERE name LIKE 'system:%';

-- Update audit permissions to be linked to Audit Logging module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'CORE_AUDIT' LIMIT 1),
    resource_type = 'CORE_AUDIT',
    name = 'CORE_AUDIT:' || action
WHERE name LIKE 'audit:%';

-- Update analytics permissions to be linked to User Analytics module
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'ANALYTICS_USER' LIMIT 1),
    resource_type = 'ANALYTICS_USER',
    name = 'ANALYTICS_USER:' || action
WHERE name LIKE 'analytics:%' 
AND EXISTS (SELECT 1 FROM module WHERE code = 'ANALYTICS_USER');

-- For any remaining permissions without module_id, assign them to Dashboard module as default
UPDATE permission 
SET module_id = (SELECT module_id FROM module WHERE code = 'CORE_DASHBOARD' LIMIT 1),
    resource_type = 'CORE_DASHBOARD',
    name = 'CORE_DASHBOARD:' || action
WHERE module_id IS NULL;

-- Add NOT NULL constraint to module_id after all permissions have been assigned
ALTER TABLE permission ALTER COLUMN module_id SET NOT NULL;

-- Add comment to document the new relationship
COMMENT ON COLUMN permission.module_id IS 'Foreign key linking permission to its associated module';
COMMENT ON TABLE permission IS 'Permissions are now linked to modules with naming convention module_name:action';