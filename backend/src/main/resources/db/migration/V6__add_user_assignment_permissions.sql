-- Migration V6: Add user assignment permissions
-- This migration adds the missing 'user:assign_roles' and 'user:assign_groups' permissions
-- that are required by the UserRoleAssignment and UserGroupAssignment components

-- Add the missing user assignment permissions
INSERT INTO permission (name, description, resource_type, action, created_at, updated_at, created_by, updated_by)
VALUES 
    ('user:assign_roles', 'Assign roles to users', 'user', 'assign_roles', NOW(), NOW(), 'system', 'system'),
    ('user:assign_groups', 'Assign user groups to users', 'user', 'assign_groups', NOW(), NOW(), 'system', 'system')
ON CONFLICT (name) DO NOTHING;

-- Assign these permissions to Super Administrator role (which gets all permissions)
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by)
SELECT r.role_id, p.permission_id, NOW(), NOW(), 'system', 'system'
FROM role r, permission p
WHERE r.name = 'Super Administrator' 
AND p.name IN ('user:assign_roles', 'user:assign_groups')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Also assign to System Administrator role for user management
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by)
SELECT r.role_id, p.permission_id, NOW(), NOW(), 'system', 'system'
FROM role r, permission p
WHERE r.name = 'System Administrator' 
AND p.name IN ('user:assign_roles', 'user:assign_groups')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Also assign to User Administrator role for user management
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at, created_by, updated_by)
SELECT r.role_id, p.permission_id, NOW(), NOW(), 'system', 'system'
FROM role r, permission p
WHERE r.name = 'User Administrator' 
AND p.name IN ('user:assign_roles', 'user:assign_groups')
ON CONFLICT (role_id, permission_id) DO NOTHING;