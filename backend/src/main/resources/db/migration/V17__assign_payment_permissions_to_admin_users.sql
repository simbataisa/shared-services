-- =====================================================
-- Assign Payment Permissions to Admin Users
-- Version: V17
-- Description: Assign payment management permissions to superadmin@ahss.com and admin@ahss.com users
-- =====================================================

-- Ensure payment permissions are assigned to Super Administrator role (for superadmin@ahss.com)
-- This should already be done in V16, but we'll ensure it's complete
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'Super Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp 
    WHERE rp.role_id = r.role_id 
    AND rp.permission_id = p.permission_id
);

-- Ensure payment permissions are assigned to System Administrator role (for admin@ahss.com)
-- Assign all payment permissions except admin-level permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'System Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
AND p.action IN ('read', 'create', 'update', 'verify', 'cancel', 'refund', 'void')
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp 
    WHERE rp.role_id = r.role_id 
    AND rp.permission_id = p.permission_id
);

-- Verify user role assignments exist (these should already exist from V4)
-- Ensure superadmin@ahss.com has Super Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by)
SELECT u.user_id, r.role_id, 'system', 'system'
FROM users u
CROSS JOIN role r
WHERE u.email = 'superadmin@ahss.com'
AND r.name = 'Super Administrator'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.user_id 
    AND ur.role_id = r.role_id
);

-- Ensure admin@ahss.com has System Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by)
SELECT u.user_id, r.role_id, 'system', 'system'
FROM users u
CROSS JOIN role r
WHERE u.email = 'admin@ahss.com'
AND r.name = 'System Administrator'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.user_id 
    AND ur.role_id = r.role_id
);

-- Add comments for documentation
COMMENT ON TABLE role_permission IS 'Junction table linking roles to their permissions';
COMMENT ON TABLE user_roles IS 'Junction table linking users to their assigned roles';

-- Log the completion of permission assignments
DO $$
BEGIN
    RAISE NOTICE 'Payment permissions have been assigned to admin users:';
    RAISE NOTICE '- superadmin@ahss.com: All payment permissions via Super Administrator role';
    RAISE NOTICE '- admin@ahss.com: Payment management permissions via System Administrator role';
END $$;