-- Migration V5: Add missing permissions for frontend compatibility
-- This migration adds the missing 'user-groups:read' permission and updates 'tenant:read' to 'tenants:read'
-- to match frontend permission requirements

-- Add the missing user-groups:read permission
INSERT INTO permission (name, description, resource_type, action, created_at, updated_at, created_by, updated_by)
VALUES ('user-groups:read', 'View user groups', 'user_group', 'read', NOW(), NOW(), 'system', 'system')
ON CONFLICT (name) DO NOTHING;

-- Update tenant:read to tenants:read to match frontend expectations
UPDATE permission 
SET name = 'tenants:read', 
    description = 'View tenants information',
    updated_at = NOW(),
    updated_by = 'system'
WHERE name = 'tenant:read';

-- Assign user-groups:read permission to Super Administrator role
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by)
SELECT r.role_id, p.permission_id, NOW(), NOW(), 'system', 'system'
FROM role r, permission p
WHERE r.name = 'Super Administrator' AND p.name = 'user-groups:read'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Add additional permissions that might be needed for user groups management
INSERT INTO permission (name, description, resource_type, action, created_at, updated_at, created_by, updated_by)
VALUES 
    ('user-groups:create', 'Create user groups', 'user_group', 'create', NOW(), NOW(), 'system', 'system'),
    ('user-groups:update', 'Update user groups', 'user_group', 'update', NOW(), NOW(), 'system', 'system'),
    ('user-groups:delete', 'Delete user groups', 'user_group', 'delete', NOW(), NOW(), 'system', 'system'),
    ('user-groups:admin', 'Full user groups administration', 'user_group', 'admin', NOW(), NOW(), 'system', 'system')
ON CONFLICT (name) DO NOTHING;

-- Add additional tenant permissions for consistency
INSERT INTO permission (name, description, resource_type, action, created_at, updated_at, created_by, updated_by)
VALUES 
    ('tenants:create', 'Create tenants', 'tenant', 'create', NOW(), NOW(), 'system', 'system'),
    ('tenants:update', 'Update tenants', 'tenant', 'update', NOW(), NOW(), 'system', 'system'),
    ('tenants:delete', 'Delete tenants', 'tenant', 'delete', NOW(), NOW(), 'system', 'system'),
    ('tenants:admin', 'Full tenants administration', 'tenant', 'admin', NOW(), NOW(), 'system', 'system')
ON CONFLICT (name) DO NOTHING;

-- Assign all new permissions to Super Administrator role
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by)
SELECT r.role_id, p.permission_id, NOW(), NOW(), 'system', 'system'
FROM role r, permission p
WHERE r.name = 'Super Administrator' 
AND p.name IN (
    'user-groups:create', 
    'user-groups:update', 
    'user-groups:delete', 
    'user-groups:admin',
    'tenants:create',
    'tenants:update', 
    'tenants:delete', 
    'tenants:admin'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;