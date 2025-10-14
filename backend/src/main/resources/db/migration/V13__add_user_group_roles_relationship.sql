-- =====================================================
-- V13: Add User Group to Roles Many-to-Many Relationship
-- =====================================================

-- Create user_group_roles junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_group_roles (
    user_group_id BIGINT NOT NULL REFERENCES user_group(user_group_id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role(role_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    PRIMARY KEY (user_group_id, role_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_group_roles_user_group_id ON user_group_roles(user_group_id);
CREATE INDEX IF NOT EXISTS idx_user_group_roles_role_id ON user_group_roles(role_id);

-- Add comments for documentation
COMMENT ON TABLE user_group_roles IS 'Many-to-many relationship between user groups and roles';
COMMENT ON COLUMN user_group_roles.user_group_id IS 'Foreign key to user_group table';
COMMENT ON COLUMN user_group_roles.role_id IS 'Foreign key to role table';
COMMENT ON COLUMN user_group_roles.created_at IS 'Timestamp when the relationship was created';
COMMENT ON COLUMN user_group_roles.updated_at IS 'Timestamp when the relationship was last updated';
COMMENT ON COLUMN user_group_roles.created_by IS 'User who created this relationship';
COMMENT ON COLUMN user_group_roles.updated_by IS 'User who last updated this relationship';

-- Insert some sample data to demonstrate the relationship
-- System Administrators group gets Super Administrator role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'System Administrators' 
AND r.name = 'Super Administrator'
ON CONFLICT (user_group_id, role_id) DO NOTHING;

-- Tenant Administrators group gets Tenant Administrator role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'Tenant Administrators' 
AND r.name = 'Tenant Administrator'
ON CONFLICT (user_group_id, role_id) DO NOTHING;

-- Product Managers group gets Product Manager role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'Product Managers' 
AND r.name = 'Product Manager'
ON CONFLICT (user_group_id, role_id) DO NOTHING;

-- Role Managers group gets RBAC Administrator role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'Role Managers' 
AND r.name = 'RBAC Administrator'
ON CONFLICT (user_group_id, role_id) DO NOTHING;

-- User Managers group gets User Administrator role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'User Managers' 
AND r.name = 'User Administrator'
ON CONFLICT (user_group_id, role_id) DO NOTHING;

-- Demo Users group gets Standard User role
INSERT INTO user_group_roles (user_group_id, role_id, created_by, updated_by)
SELECT ug.user_group_id, r.role_id, 'system', 'system'
FROM user_group ug, role r
WHERE ug.name = 'Demo Users' 
AND r.name = 'Standard User'
ON CONFLICT (user_group_id, role_id) DO NOTHING;