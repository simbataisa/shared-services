-- =====================================================
-- V4: Seed User Data with Admin and Test Accounts
-- =====================================================

-- Insert admin and test users
-- Note: Passwords are hashed using BCrypt with strength 12
-- Plain text passwords for reference:
-- - admin: admin123
-- - superadmin: superadmin123
-- - testuser: testuser123
-- - demo: demo123

INSERT INTO users (username, email, password, first_name, last_name, user_status, email_verified, created_by, updated_by) VALUES
-- Admin Users
('admin', 'admin@ahss.com', '$2a$12$fe11/7dbJGWP.XSY6e7ISei4cF2hGtvC9bL35Is.oYiGdTCxmfhHa', 'System', 'Administrator', 'ACTIVE', true, 'system', 'system'),
('superadmin', 'superadmin@ahss.com', '$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe', 'Super', 'Administrator', 'ACTIVE', true, 'system', 'system'),
('tenant.admin', 'tenant.admin@ahss.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i', 'Tenant', 'Administrator', 'ACTIVE', true, 'system', 'system'),
('product.manager', 'product.manager@ahss.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i', 'Product', 'Manager', 'ACTIVE', true, 'system', 'system'),
('role.manager', 'role.manager@ahss.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i', 'Role', 'Manager', 'ACTIVE', true, 'system', 'system'),
('user.manager', 'user.manager@ahss.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPdkzjKWw0/0i', 'User', 'Manager', 'ACTIVE', true, 'system', 'system'),

-- Test Users
('testuser', 'testuser@test.com', '$2a$12$0/jmU7PpLnF/QrcxUg2UfuLI7NjWz4sebrUMzj95zaim9MvyB4kEK', 'Test', 'User', 'ACTIVE', true, 'system', 'system'),
('demo', 'demo@demo.com', '$2a$12$DSINAnFAV9WFn/R3gfceqOJJMvur6UexfeX1tn0UObxaj.5eZNTMi', 'Demo', 'User', 'ACTIVE', true, 'system', 'system'),
('john.doe', 'john.doe@example.com', '$2a$12$0/jmU7PpLnF/QrcxUg2UfuLI7NjWz4sebrUMzj95zaim9MvyB4kEK', 'John', 'Doe', 'ACTIVE', true, 'system', 'system'),
('jane.smith', 'jane.smith@example.com', '$2a$12$0/jmU7PpLnF/QrcxUg2UfuLI7NjWz4sebrUMzj95zaim9MvyB4kEK', 'Jane', 'Smith', 'ACTIVE', true, 'system', 'system'),

-- Inactive/Test Status Users
('inactive.user', 'inactive@test.com', '$2a$12$0/jmU7PpLnF/QrcxUg2UfuLI7NjWz4sebrUMzj95zaim9MvyB4kEK', 'Inactive', 'User', 'INACTIVE', false, 'system', 'system'),
('pending.user', 'pending@test.com', '$2a$12$0/jmU7PpLnF/QrcxUg2UfuLI7NjWz4sebrUMzj95zaim9MvyB4kEK', 'Pending', 'User', 'PENDING_VERIFICATION', false, 'system', 'system');

-- Assign roles to users
-- Super Administrator gets Super Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'superadmin'), (SELECT role_id FROM role WHERE name = 'Super Administrator'), 'system', 'system');

-- System Administrator gets System Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'admin'), (SELECT role_id FROM role WHERE name = 'System Administrator'), 'system', 'system');

-- Tenant Administrator gets Tenant Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'tenant.admin'), (SELECT role_id FROM role WHERE name = 'Tenant Administrator'), 'system', 'system');

-- Product Manager gets Product Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'product.manager'), (SELECT role_id FROM role WHERE name = 'Product Administrator'), 'system', 'system');

-- Role Manager gets RBAC Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'role.manager'), (SELECT role_id FROM role WHERE name = 'RBAC Administrator'), 'system', 'system');

-- User Manager gets User Administrator role
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'user.manager'), (SELECT role_id FROM role WHERE name = 'User Administrator'), 'system', 'system');

-- Test users get standard user roles (if they exist)
INSERT INTO user_roles (user_id, role_id, created_by, updated_by) VALUES
((SELECT user_id FROM users WHERE username = 'testuser'), (SELECT role_id FROM role WHERE name = 'Standard User' LIMIT 1), 'system', 'system'),
((SELECT user_id FROM users WHERE username = 'demo'), (SELECT role_id FROM role WHERE name = 'Standard User' LIMIT 1), 'system', 'system'),
((SELECT user_id FROM users WHERE username = 'john.doe'), (SELECT role_id FROM role WHERE name = 'Standard User' LIMIT 1), 'system', 'system'),
((SELECT user_id FROM users WHERE username = 'jane.smith'), (SELECT role_id FROM role WHERE name = 'Standard User' LIMIT 1), 'system', 'system');

-- Add users to user groups
-- System Administrators Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT user_id FROM users WHERE username = 'admin'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'System Administrators'), (SELECT user_id FROM users WHERE username = 'superadmin'), 'system', 'system');

-- Tenant Administrators Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Tenant Administrators'), (SELECT user_id FROM users WHERE username = 'tenant.admin'), 'system', 'system');

-- Product Managers Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Product Managers'), (SELECT user_id FROM users WHERE username = 'product.manager'), 'system', 'system');

-- RBAC Managers Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'RBAC Managers'), (SELECT user_id FROM users WHERE username = 'role.manager'), 'system', 'system');

-- Standard Users Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT user_id FROM users WHERE username = 'user.manager'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT user_id FROM users WHERE username = 'testuser'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT user_id FROM users WHERE username = 'john.doe'), 'system', 'system'),
((SELECT user_group_id FROM user_group WHERE name = 'Standard Users'), (SELECT user_id FROM users WHERE username = 'jane.smith'), 'system', 'system');

-- Demo Users Group
INSERT INTO user_group_users (user_group_id, user_id, created_by, updated_by) VALUES
((SELECT user_group_id FROM user_group WHERE name = 'Demo Users'), (SELECT user_id FROM users WHERE username = 'demo'), 'system', 'system');

-- =====================================================
-- User Account Summary
-- =====================================================
-- 
-- Admin Accounts Created:
-- - superadmin@ahss.com (username: superadmin, password: superadmin123) - Super Administrator
-- - admin@ahss.com (username: admin, password: admin123) - System Administrator  
-- - tenant.admin@ahss.com (username: tenant.admin, password: admin123) - Tenant Administrator
-- - product.manager@ahss.com (username: product.manager, password: admin123) - Product Administrator
-- - role.manager@ahss.com (username: role.manager, password: admin123) - RBAC Administrator
-- - user.manager@ahss.com (username: user.manager, password: admin123) - User Administrator
-- 
-- Test Accounts:
-- - testuser@test.com (username: testuser, password: testuser123) - Standard User
-- - demo@demo.com (username: demo, password: demo123) - Demo User
-- - john.doe@example.com (username: john.doe, password: testuser123) - Standard User
-- - jane.smith@example.com (username: jane.smith, password: testuser123) - Standard User
-- 
-- Special Status Accounts:
-- - inactive@test.com (username: inactive.user, password: testuser123) - INACTIVE status
-- - pending@test.com (username: pending.user, password: testuser123) - PENDING_VERIFICATION status
-- 
-- =====================================================