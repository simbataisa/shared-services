-- =====================================================
-- Payment Permissions
-- Version: V16
-- Description: Add permissions for payment management
-- =====================================================

-- Create payment management module first
INSERT INTO module (name, code, description, module_status, product_id, created_at, updated_at)
VALUES
    ('Payment Management', 'PAYMENT_MGMT', 'Payment processing and management system', 'ACTIVE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Insert payment management permissions with module_id
INSERT INTO permission (name, description, resource_type, action, module_id, created_at, updated_at)
SELECT 
    perm.name,
    perm.description,
    perm.resource_type,
    perm.action,
    m.module_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (VALUES
    -- Payment Request Permissions
    ('PAYMENT_MGMT:read', 'View payment requests and transactions', 'PAYMENT_MGMT', 'read'),
    ('PAYMENT_MGMT:create', 'Create new payment requests', 'PAYMENT_MGMT', 'create'),
    ('PAYMENT_MGMT:update', 'Update payment requests', 'PAYMENT_MGMT', 'update'),
    ('PAYMENT_MGMT:delete', 'Delete payment requests', 'PAYMENT_MGMT', 'delete'),
    ('PAYMENT_MGMT:verify', 'Verify payment requests', 'PAYMENT_MGMT', 'verify'),
    ('PAYMENT_MGMT:void', 'Void completed payments', 'PAYMENT_MGMT', 'void'),
    ('PAYMENT_MGMT:refund', 'Process payment refunds', 'PAYMENT_MGMT', 'refund'),
    ('PAYMENT_MGMT:cancel', 'Cancel pending payment requests', 'PAYMENT_MGMT', 'cancel'),
    ('PAYMENT_MGMT:admin', 'Full administrative access to payment management', 'PAYMENT_MGMT', 'admin')
) AS perm(name, description, resource_type, action)
CROSS JOIN module m
WHERE m.code = 'PAYMENT_MGMT'
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to Super Administrator role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'Super Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
ON CONFLICT DO NOTHING;

-- Assign basic permissions to System Administrator role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'System Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
AND p.action IN ('read', 'create', 'update', 'verify', 'cancel')
ON CONFLICT DO NOTHING;

COMMENT ON COLUMN permission.resource_type IS 'Resource type for permission (e.g., PAYMENT_MGMT)';
COMMENT ON COLUMN permission.action IS 'Action allowed by permission (e.g., read, create, verify, void, refund)';